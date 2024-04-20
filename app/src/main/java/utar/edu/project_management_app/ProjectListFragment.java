package utar.edu.project_management_app;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import utar.edu.project_management_app.model.Task;

public class ProjectListFragment extends Fragment implements ProjectCreationBottomSheetDialogFragment.OnProjectCreatedListener {

    private String currentUserId;
    private boolean isProjectUpdated = false;
    private EditText searchEditText;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_list, container, false);
        currentUserId = getCurrentUserId();
        searchEditText = view.findViewById(R.id.searchProjectEditText);

        // Call the method to set up search functionality
        setupSearchFunctionality();
        DatabaseReference userProjectsRef = FirebaseDatabase.getInstance().getReference().child("Registered Users").child(currentUserId).child("ProjectId");
        userProjectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                clearUI();

                for (DataSnapshot projectSnapshot : dataSnapshot.getChildren()) {
                    String projectId = projectSnapshot.getKey();
                    DatabaseReference projectRef = FirebaseDatabase.getInstance().getReference().child("projects").child(projectId);
                    projectRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String projectName = snapshot.child("projectName").getValue(String.class);
                            String selectedDate = snapshot.child("dueDate").getValue(String.class);
                            updateUIWithProject(projectId, projectName, selectedDate);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        // update user's profile pic
        String currentUserId = getCurrentUserId();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference profilePictureRef = storage.getReference().child("DisplayPics/").child(currentUserId + ".jpg");
        profilePictureRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ImageView profileImageView = view.findViewById(R.id.profile_view);
                Picasso.get()
                        .load(uri)
                        .transform(new CircleTransform())
                        .into(profileImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.e("ProjectListFragment", "Failed to retrieve profile picture: " + exception.getMessage());
            }
        });

        // Add listener to the tasks node for real-time updates
        DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference().child("task");
        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot projectSnapshot : dataSnapshot.getChildren()) {
                    String projectId = projectSnapshot.child("projectId").getValue(String.class);
                    calculateProgressPercent(projectId);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        view.findViewById(R.id.filterButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Filter Projects by");
                String[] filterOptions = {"All", "Progress >= 50%", "Progress < 50%"};
                builder.setItems(filterOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: // All
                                filterProjects(false, true);
                                break;
                            case 1: // Progress >= 50%
                                filterProjects(true, false);
                                break;
                            case 2: // Progress < 50%
                                filterProjects(false, false);
                                break;
                        }
                    }
                });
                builder.show();
            }
        });

        TextView openBottomSheetButton = view.findViewById(R.id.btn_create_project);
        openBottomSheetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProjectCreationBottomSheetDialogFragment bottomSheetDialog = new ProjectCreationBottomSheetDialogFragment();
                bottomSheetDialog.setOnProjectCreatedListener(ProjectListFragment.this);
                bottomSheetDialog.show(getChildFragmentManager(), "ProjectCreationBottomSheet");
            }
        });
        return view;
    }

    private void clearUI() {
        if (getView() != null) {
            TableLayout tableLayout = getView().findViewById(R.id.tableLayout);
            tableLayout.removeAllViews();
        } else {
            Log.e("ProjectListFragment", "View is not available when trying to clear UI.");
        }
    }

    private String getCurrentUserId() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        }
        return null;
    }

    private void setupSearchFunctionality() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Perform search operation with the current text input
                searchProjects(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });
    }

    private void searchProjects(String searchQuery) {
        TableLayout tableLayout = requireView().findViewById(R.id.tableLayout);
        boolean foundMatch = false;
        // Iterate through the visible rows in the UI
        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            View child = tableLayout.getChildAt(i);
            if (child instanceof TableRow) {
                TableRow row = (TableRow) child;
                TextView projectTextView = (TextView) row.getChildAt(0);
                String projectName = projectTextView.getText().toString();

                // If the search query is empty or matches the project name, keep the row visible
                if (searchQuery.isEmpty() || projectName.toLowerCase().contains(searchQuery.toLowerCase())) {
                    row.setVisibility(View.VISIBLE);
                    foundMatch = true;
                } else {
                    // If the project name does not match the search query, hide the row
                    row.setVisibility(View.GONE);

                }
            }
        }
        // If no project matches the search query, display "No project found" message
        if (!foundMatch) {
            // Display a message indicating that no project matches the search query
            Toast.makeText(requireContext(), "No project found", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUIWithProject(String projectId, String projectName, String selectedDate) {
        TableLayout tableLayout = requireView().findViewById(R.id.tableLayout);

        // Check if a row with the same project ID already exists
        TableRow existingRow = findRowByProjectId(projectId);
        if (existingRow != null) {
            // If a row exists, update the existing row with the new project details
            TextView projectTextView = (TextView) existingRow.getChildAt(0);
            projectTextView.setText(projectName);
            return;
        }
        // If no existing row is found, create a new row and add it to the UI
        TableRow newRow = new TableRow(requireContext());
        newRow.setTag(projectId);

        TextView projectTextView = new TextView(requireContext());
        projectTextView.setText(projectName);
        projectTextView.setLayoutParams(new TableRow.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        projectTextView.setGravity(Gravity.CENTER_VERTICAL);
        projectTextView.setPadding(30, 8, 50, 8);

        // set click event to navigate to project tasks
        projectTextView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), ProjectTasks.class);
                i.putExtra("projectId", projectId);
                i.putExtra("projectName",projectName);
                startActivity(i);
            }
        });

        TextView progressPercentTextView = new TextView(requireContext());
        progressPercentTextView.setText("0%");
        progressPercentTextView.setLayoutParams(new TableRow.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        progressPercentTextView.setGravity(Gravity.CENTER_VERTICAL);
        progressPercentTextView.setPadding(110, 8, 8, 8);

        int edit_icon_width = 85;
        int edit_icon_height = 85;
        ImageView editImageView = new ImageView(requireContext());
        editImageView.setImageResource(R.drawable.edit);
        editImageView.setLayoutParams(new TableRow.LayoutParams(
                edit_icon_width,
                edit_icon_height
        ));

        ImageView deleteImageView = new ImageView(requireContext());
        deleteImageView.setImageResource(R.drawable.delete);
        deleteImageView.setLayoutParams(new TableRow.LayoutParams(
                edit_icon_width,
                edit_icon_height
        ));

        newRow.addView(projectTextView);
        newRow.addView(progressPercentTextView);
        newRow.addView(editImageView);
        newRow.addView(deleteImageView);

        tableLayout.addView(newRow);

        deleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setMessage("Confirm to delete this project?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        DatabaseReference projectRef = FirebaseDatabase.getInstance().getReference().child("projects").child(projectId);
                        DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference().child("task");
                        tasksRef.orderByChild("projectId").equalTo(projectId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                                    taskSnapshot.getRef().removeValue();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });

                        // Remove the project itself
                        projectRef.removeValue();

                        // Remove the project from the user's projects list
                        DatabaseReference userProjectRef = FirebaseDatabase.getInstance().getReference().child("Registered Users").child(currentUserId).child("ProjectId").child(projectId);
                        userProjectRef.removeValue();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        editImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog(projectId, projectName, selectedDate);
            }
        });

        // Calculate and update progress percentage
        calculateProgressPercent(projectId);
    }
    private void calculateProgressPercent(String projectId) {
        DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference().child("task");
        taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalTasks = 0;
                int completedTasks = 0;

                for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    if (task != null && task.getProjectId().equals(projectId)) {
                        totalTasks++;
                        if ("Done".equals(task.getSection())) {
                            completedTasks++;
                        }
                    }
                }

                int progressPercent = totalTasks > 0 ? (int) ((completedTasks / (double) totalTasks) * 100) : 0;
                updateProgressTextView(projectId, progressPercent);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void updateProgressTextView(String projectId, int progressPercent) {
        TableRow rowToUpdate = findRowByProjectId(projectId);
        if (rowToUpdate != null) {
            TextView progressTextView = (TextView) rowToUpdate.getChildAt(1);
            progressTextView.setText(progressPercent + "%");
        }
    }
    private TableRow findRowByProjectId(String projectId) {
        TableLayout tableLayout = requireView().findViewById(R.id.tableLayout);
        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            View child = tableLayout.getChildAt(i);
            if (child instanceof TableRow) {
                TableRow row = (TableRow) child;
                if (projectId.equals(row.getTag())) {
                    return row;
                }
            }
        }
        return null;
    }

    private void showEditDialog(String projectId, String projectName, String selectedDate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.edit_project_dialog, null);
        builder.setView(dialogView);

        EditText projectNameEditText = dialogView.findViewById(R.id.editTextProjectname);
        EditText dueDateEditText = dialogView.findViewById(R.id.editTextDueDate);
        Button saveButton = dialogView.findViewById(R.id.buttonSave);
        Button cancelButton = dialogView.findViewById(R.id.buttonCancel);

        projectNameEditText.setText(projectName);
        dueDateEditText.setText(selectedDate);

        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updatedProjectName = projectNameEditText.getText().toString().trim();
                String updatedDueDate = dueDateEditText.getText().toString().trim();
                updateProjectDetails(projectId, updatedProjectName, updatedDueDate);
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void updateProjectDetails(String projectId, String updatedProjectName, String updatedDueDate) {
        // Update project details in the UI
        TableRow rowToUpdate = findRowByProjectId(projectId);
        if (rowToUpdate != null) {
            TextView projectTextView = (TextView) rowToUpdate.getChildAt(0);
            projectTextView.setText(updatedProjectName);
        }

        // Update project details in the Firebase Realtime Database
        DatabaseReference projectRef = FirebaseDatabase.getInstance().getReference().child("projects").child(projectId);
        projectRef.child("projectName").setValue(updatedProjectName);
        projectRef.child("dueDate").setValue(updatedDueDate);

        // Show a toast message to indicate that the project details were updated successfully
        Toast.makeText(requireContext(), "Project details updated successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProjectCreated(String projectId, String projectName) {
        isProjectUpdated = true;

        DatabaseReference updatedProjectRef = FirebaseDatabase.getInstance().getReference().child("projects").child(projectId);
        updatedProjectRef.child("Project Name").setValue(projectName);

    }
    private void filterProjects(boolean greaterThanOrEqual50, boolean showAll) {
        TableLayout tableLayout = requireView().findViewById(R.id.tableLayout);

        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            View child = tableLayout.getChildAt(i);
            if (child instanceof TableRow) {
                TableRow row = (TableRow) child;
                TextView progressTextView = (TextView) row.getChildAt(1);
                String progressString = progressTextView.getText().toString().replace("%", "");
                int progress = Integer.parseInt(progressString);

                // Check if the row should be visible based on the filter criteria
                if (!showAll && ((greaterThanOrEqual50 && progress < 50) || (!greaterThanOrEqual50 && progress >= 50))) {
                    row.setVisibility(View.GONE);
                } else {
                    row.setVisibility(View.VISIBLE);
                }
            }
        }
    }

}
