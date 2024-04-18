package utar.edu.project_management_app;
import android.app.DatePickerDialog;
import utar.edu.project_management_app.model.Project;
import utar.edu.project_management_app.model.User;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProjectCreationBottomSheetDialogFragment newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProjectCreationBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private TextView selectedDateTextView, listView, kanbanView, create_project_btn, cancel_project_btn;
    private EditText project_name;
    private HashMap<String, Object> projects;
    private OnProjectCreatedListener projectCreatedListener;

    String defaultView = "List"; //default view
    // Get reference to the current user's projects node
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private int projectCount = 0;
    public interface OnProjectCreatedListener {
        void onProjectCreated(String projectId, String projectName);
    }

    public void setOnProjectCreatedListener(OnProjectCreatedListener listener) {
        this.projectCreatedListener = listener;
    }

    public static ProjectCreationBottomSheetDialogFragment newInstance(String projectName, String selectedDate) {
        ProjectCreationBottomSheetDialogFragment fragment = new ProjectCreationBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putString("projectName", projectName);
        args.putString("selectedDate", selectedDate);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.activity_project_creation, container, false);
        View view = inflater.inflate(R.layout.activity_project_creation, container, false);

        selectedDateTextView = view.findViewById(R.id.selectedDateTextView);
        project_name = view.findViewById(R.id.project_name);
        create_project_btn = view.findViewById(R.id.btn_create_project);
        cancel_project_btn = view.findViewById(R.id.btn_cancel_project);
        LinearLayout selectDateButton = view.findViewById(R.id.dateButton);


        if (getArguments() != null) {
            String projectName = getArguments().getString("projectName");
            String selectedDate = getArguments().getString("selectedDate");
            if (projectName != null && selectedDate != null) {
                project_name.setText(projectName);
                selectedDateTextView.setText(selectedDate);
            }
        }
        create_project_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String proj_name = project_name.getText().toString();
                String date = selectedDateTextView.getText().toString();
                String view = defaultView;
                String defaultDateText = "Selected Date: Not set";
                //String username = "elyn";
                //projectCount++;
                if (proj_name.isEmpty()) {
                    project_name.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                    project_name.setHintTextColor(Color.parseColor("#85FF0000"));
                    Toast.makeText(getActivity(), "Please fill out the project name", Toast.LENGTH_SHORT).show();
                    project_name.setError("Project Name is required");
                    return;
                }

                if (date.equals(defaultDateText)) {
                    selectedDateTextView.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                    selectedDateTextView.setHintTextColor(Color.parseColor("#85FF0000"));
                    Toast.makeText(getActivity(), "Please select a date", Toast.LENGTH_SHORT).show();
                    selectedDateTextView.setError("Project due date is required");
                    return;
                }

                String currentUserId = getCurrentUserId();

                if (!proj_name.isEmpty() && !date.equals(defaultDateText)) {


                    addProjectToDB(proj_name, date, view);
                    dismiss();

                } else {
                    Toast.makeText(getActivity(), "User not authenticated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancel_project_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });


        return view;
    }

    private void addProjectToDB(String projName, String date, String view) {

        // Get the current user's ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // initialized project class
        Project newProject = new Project();


        // Get reference to the current user's projects node
        DatabaseReference currentUserProjectsRef = FirebaseDatabase.getInstance().getReference().child("Registered Users").child(userId).child("projects");
        // Push the project data under the user's projects node with the incremented project counter as the key
        DatabaseReference newProjectRef = currentUserProjectsRef.push();
        String projectId = newProjectRef.getKey();  // Get the generated project ID

        newProject.setProjectName(projName);
        newProject.setDueDate(date);
        newProject.setProjectId(projectId);
        newProject.getEmails().add(getCurrentUserEmail());

        // Store the project details under the generated project ID in the projects table
        DatabaseReference projectsRef = FirebaseDatabase.getInstance().getReference().child("projects").child(projectId);
        projectsRef.setValue(newProject);

        // Update the user's projectsId list
        DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("Registered Users").child(userId);
        currentUserRef.child("ProjectId").child(projectId).setValue(true); // Add project ID to the user's projectsId list

        /*if (projectCreatedListener != null) {
            projectCreatedListener.onProjectCreated(projectId, projName);
        }*/

    }

    private String getCurrentUserId() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        }
        return null;
    }
    private String getCurrentUserEmail() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getEmail();
        }
        return null;
    }

    private boolean isFilled() {
        boolean isProjectNameEmpty = project_name.getText().toString().isEmpty();
        boolean isDateEmpty = !Character.isDigit(selectedDateTextView.getText().toString().charAt(0));


        if (isProjectNameEmpty) {
            project_name.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            project_name.setHintTextColor(Color.parseColor("#85FF0000"));
            project_name.setError("Please fill out the project name");
        } else {
            project_name.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));// Reset to default or desired color when not empty

        }

        if (isDateEmpty) {
            selectedDateTextView.setTextColor(Color.RED);
            selectedDateTextView.setError("Please select date");
        } else {
            selectedDateTextView.setTextColor(Color.BLACK); // Reset to default or desired color when not empty
        }

        return !isProjectNameEmpty && !isDateEmpty;
    }

    private void showDatePickerDialog() {
        // Get current date to set as default in DatePickerDialog
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // Update TextView with selected date
                selectedDateTextView.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
            }
        }, year, month, dayOfMonth);

        // Show DatePickerDialog
        datePickerDialog.show();

    }


}