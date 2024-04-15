package utar.edu.project_management_app;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ProjectListFragment extends Fragment implements ProjectCreationBottomSheetDialogFragment.OnProjectCreatedListener {

    private String currentUserId;
    private boolean isProjectUpdated = false;
    private EditText searchEditText;
    private FrameLayout rootLayout;
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
                            // Handle error
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

        // Find the root layout by its id

        view.findViewById(R.id.searchEngine).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is not needed anymore
            }
        });





        view.findViewById(R.id.filterButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement filter functionality here
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
        DatabaseReference userProjectsRef = FirebaseDatabase.getInstance().getReference().child("Registered Users").child(currentUserId).child("ProjectId");
        userProjectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                clearUI();

                // Iterate through the project IDs associated with the current user
                for (DataSnapshot projectSnapshot : dataSnapshot.getChildren()) {
                    String projectId = projectSnapshot.getKey();

                    // Query the project details based on the project ID
                    DatabaseReference projectRef = FirebaseDatabase.getInstance().getReference().child("projects").child(projectId);
                    projectRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String projectName = snapshot.child("Project Name").getValue(String.class);
                            String selectedDate = snapshot.child("Due Date").getValue(String.class);

                            // If the search query is empty or matches the project name, update the UI
                            if (searchQuery.isEmpty() || projectName.toLowerCase().contains(searchQuery.toLowerCase())) {
                                updateUIWithProject(projectId, projectName, selectedDate);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle error
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }




    private void updateUIWithProject(String projectId, String projectName, String selectedDate) {
        TableLayout tableLayout = requireView().findViewById(R.id.tableLayout);

        // Check if a row with the same project ID already exists
        TableRow existingRow = findRowByProjectId(projectId);
        if (existingRow != null) {
            // If a row exists, update the existing row with the new project details
            TextView projectTextView = (TextView) existingRow.getChildAt(0);
            projectTextView.setText(projectName);
            // You can update other fields if needed
            return; // Exit the method to avoid adding a new row
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
                        tableLayout.removeView(newRow);

                        DatabaseReference projectRef = FirebaseDatabase.getInstance().getReference().child("projects").child(projectId);
//                        List<String> useremail = projectRef.child("emails");
                        projectRef.removeValue();

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

}
