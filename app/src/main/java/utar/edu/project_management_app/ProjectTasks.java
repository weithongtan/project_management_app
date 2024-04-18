package utar.edu.project_management_app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.Button;
import android.widget.EditText;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import java.util.List;
import java.util.Map;

import utar.edu.project_management_app.model.Task;

public class ProjectTasks extends AppCompatActivity implements ProjectTasksCreationBottomSheetDialogFragment.OnDialogDismissListener{

    private Task task;
    private List<Task> tasks;
    private TextView openBottomSheetButton;
    private Spinner spinnerOptions;
    private List<ImageView> dropDownButtonSectionList;
    private List<LinearLayout> kanbanList;
    private String projectId ;

    private String viewType;

    private ImageView addMember;

    private List<String> projectEmails = new ArrayList<>();;
    private List<String> newInvitedEmails = new ArrayList<>();;

    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_tasks);
        // include the menu layout
        View includedLayout = LayoutInflater.from(this).inflate(R.layout.activity_project_tasks_menu, null);

        // Retrieve the project ID from the intent
        // Todo get project id from here (junyi)
        projectId = getIntent().getStringExtra("projectId");
        String projectName = getIntent().getStringExtra("projectName");


        TextView projectname = findViewById(R.id.project_name);
        projectname.setText(projectName);

        findViewById(R.id.btn_to_do).setRotation(90);
        findViewById(R.id.btn_pending).setRotation(90);
        findViewById(R.id.btn_done).setRotation(90);

        // change view
        spinnerOptions = findViewById(R.id.SectionOptions);
        // add drop down item
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.view, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOptions.setAdapter(adapter);

        //spinner listener -- hide list or kanban depending on which is selected
        spinnerOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item from the spinner
                String selectedItem = parent.getItemAtPosition(position).toString();
                // Hide or show view based on the selected item
                if (selectedItem.equals("List")) {
                    viewType = "List";
                    findViewById(R.id.listView).setVisibility(View.VISIBLE);
                    findViewById(R.id.kanbanView).setVisibility(View.GONE);
                } else if (selectedItem.equals("Kanban")) {
                    viewType = "Kanban";
                    findViewById(R.id.kanbanView).setVisibility(View.VISIBLE);
                    findViewById(R.id.listView).setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });

        // Return to main screen
        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
                finish();
            }
        );

        addMember = findViewById(R.id.btn_invite_member);
        addMember.setOnClickListener(view -> inviteMember());

        dropDownButtonSectionList = new ArrayList<>();
        dropDownButtonSectionList.add(findViewById(R.id.btn_to_do));
        dropDownButtonSectionList.add(findViewById(R.id.btn_pending));
        dropDownButtonSectionList.add(findViewById(R.id.btn_done));

        kanbanList = new ArrayList<>();
        kanbanList.add(findViewById(R.id.kanbanToDo));
        kanbanList.add(findViewById(R.id.kanbanPending));
        kanbanList.add(findViewById(R.id.kanbanDone));

        // create new task button
        openBottomSheetButton = findViewById(R.id.btn_create_task);

        openBottomSheetButton.setOnClickListener(v -> {
            // Show the bottom sheet dialog
            ProjectTasksCreationBottomSheetDialogFragment bottomSheet = new ProjectTasksCreationBottomSheetDialogFragment();
            Bundle args = new Bundle();
            args.putString("projectId", projectId); // Pass the project ID to the fragment
            bottomSheet.setArguments(args);
            bottomSheet.setOnDialogDismissListener(this); // Set the listener
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });


        getCurrentProjectEmail();

    }
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list of tasks

        refreshTaskList();
        getTask();
    }


    private void getTask(){
        DatabaseReference tasksRef = database.child("task");
        Query query = tasksRef.orderByChild("projectId").equalTo(projectId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tasks = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Task task = snapshot.getValue(Task.class);
                    addTask(task);
                    tasks.add(task);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors

            }
        });
    }
    private void refreshTaskList(){
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        // Find the index of each section header
        int toDoIndex = tableLayout.indexOfChild(findViewById(R.id.row_todo));
        int pendingIndex = tableLayout.indexOfChild(findViewById(R.id.row_pending));
        // Remove rows below each section header
        removeRowsBelowIndex(tableLayout, toDoIndex, pendingIndex);


        int pendingIndex1 = tableLayout.indexOfChild(findViewById(R.id.row_pending));
        int doneIndex1 = tableLayout.indexOfChild(findViewById(R.id.row_done));
        removeRowsBelowIndex(tableLayout, pendingIndex1, doneIndex1);


        int doneIndex2 = tableLayout.indexOfChild(findViewById(R.id.row_done));
        removeRowsBelowIndex(tableLayout, doneIndex2, tableLayout.getChildCount());

        //remove all kanban items
        for (LinearLayout linearLayout : kanbanList) {
            linearLayout.removeAllViews();
        }
        //add title back to the views
        for (int i = 0; i < kanbanList.size(); i++){
            TextView textView = new TextView(this);
            textView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setPadding(dpToPx(30), dpToPx(10), dpToPx(30), dpToPx(10));

            String[] sections = getResources().getStringArray(R.array.section);
            textView.setText(sections[i]);

            kanbanList.get(i).addView(textView);
        }


    }
    // to show the current invited email to add member screen
    private void getCurrentProjectEmail(){
        DatabaseReference emailsRef  = database.child("projects").child(projectId).child("emails");

        emailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                projectEmails.clear();

                // Iterate through all children, these are your emails
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String email = snapshot.getValue(String.class);  // Get the value of the email
                    System.out.println(email);
                    projectEmails.add(email);  // Add it to your list
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors

            }
        });
    }
    private void inviteMember() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_invite_member, null);
        dialogBuilder.setView(dialogView);


        EditText editTextEmail = dialogView.findViewById(R.id.editTextEmail);
        LinearLayout emailsContainer = dialogView.findViewById(R.id.emailsContainer);
        Button buttonAdd = dialogView.findViewById(R.id.addButton);
        Button buttonDone = dialogView.findViewById(R.id.buttonDone);

        // display existing email
        for (String email:projectEmails) {
            addEmailView(emailsContainer,email);
        }

        buttonAdd.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString().trim();
            if (!email.isEmpty()) {
                newInvitedEmails.add(email);// need to update the invited user's projectids
                projectEmails.add(email);// to add into project emails
                addEmailView(emailsContainer, email);
                editTextEmail.setText(""); // Clear input field after adding
            } else {
                editTextEmail.setError("Email cannot be empty");
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check user's email with newInvitedEmails, if yes update user's project id
                // update project's useremail list with projectemails

                System.out.println(newInvitedEmails);
                System.out.println(projectEmails);
                if (!newInvitedEmails.isEmpty()){
                    processInvitationsAndUpdateProjectEmails();
                }

                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }
    private void processInvitationsAndUpdateProjectEmails() {
        DatabaseReference usersRef = database.child("Registered Users");

        // Fetch all user emails first
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> userEmailToIdMap = new HashMap<>();

                // Collect all user emails and their IDs
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String email = userSnapshot.child("email").getValue(String.class);
                    if (newInvitedEmails.contains(email)){
                        userEmailToIdMap.put(email, userSnapshot.getKey());
                    }
                }

                Iterator<String> iterator = newInvitedEmails.iterator();
                while (iterator.hasNext()) {
                    String item = iterator.next();
                    if (!userEmailToIdMap.containsKey(item)) {
                        iterator.remove();
                        projectEmails.remove(item);
                    }
                }

                for (Map.Entry<String, String> entry : userEmailToIdMap.entrySet()) {
                    String userid = entry.getValue();

                    // Update the database with the userid for the corresponding email
                    DatabaseReference userProjectIdRef = usersRef.child(userid).child("ProjectId");
                    // Assuming you want to update a value in the database, use setValue() method
                    userProjectIdRef.child(projectId).setValue(true);
                }

                // Update project emails in Firebase after cleanup
                updateProjectEmailsInFirebase();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching users", databaseError.toException());
            }
        });
    }

    private void updateProjectEmailsInFirebase() {
        DatabaseReference projectEmailsRef = database.child("projects").child(projectId).child("emails");
        projectEmailsRef.setValue(projectEmails).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Firebase", "Project emails updated successfully.");
            } else {
                Log.e("Firebase", "Failed to update project emails.", task.getException());
            }
        });
    }


    private void addEmailView(LinearLayout container, String email) {
        View emailView = LayoutInflater.from(this).inflate(R.layout.email_item, null);
        TextView emailText = emailView.findViewById(R.id.emailText);
        TextView deleteButton = emailView.findViewById(R.id.deleteButton);

        emailText.setText(email);
        if (email.equals(getCurrentUserEmail())){
            deleteButton.setVisibility(View.GONE);
        }
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                container.removeView(emailView);
                projectEmails.remove(email);
                newInvitedEmails.remove(email);
            }
        });

        container.addView(emailView);
    }
    private String getCurrentUserEmail() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getEmail();
        }
        return null;
    }
    private void removeRowsBelowIndex(TableLayout tableLayout, int startIndex, int endIndex) {
        if (tableLayout == null) {
            Log.e("ProjectTasks", "TableLayout is null");
            return;
        }

        // Remove rows from startIndex + 1 to endIndex - 1
        for (int i = endIndex - 1; i > startIndex; i--) {
            View row = tableLayout.getChildAt(i);
            if (row == null) {
                Log.e("ProjectTasks", "Row at index " + i + " is null");
            } else {
                tableLayout.removeViewAt(startIndex+1);
            }
        }
    }

    private void toggleRowsBelowIndex(TableLayout tableLayout, int startIndex, int endIndex) {
        if (tableLayout == null) {
            Log.e("ProjectTasks", "TableLayout is null");
            return;
        }

        // Toggle visibility of rows from startIndex + 1 to endIndex - 1
        for (int i = startIndex + 1; i < endIndex; i++) {
            View row = tableLayout.getChildAt(i);
            if (row == null) {
                Log.e("ProjectTasks", "Row at index " + i + " is null");
            } else {
                row.setVisibility(row.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        }
    }

    public void dropDownShowDetail(View view) {
        ImageView clickedSection = (ImageView) view;
        TableLayout tableLayout = findViewById(R.id.tableLayout);

        // Get the indices of the section rows
        int toDoIndex = tableLayout.indexOfChild(findViewById(R.id.row_todo));
        int pendingIndex = tableLayout.indexOfChild(findViewById(R.id.row_pending));
        int doneIndex = tableLayout.indexOfChild(findViewById(R.id.row_done));

        // Determine which section was clicked and toggle the rows below it
        if (clickedSection.getId() == R.id.btn_to_do) {
            toggleRowsBelowIndex(tableLayout, toDoIndex, pendingIndex);
        } else if (clickedSection.getId() == R.id.btn_pending) {
            toggleRowsBelowIndex(tableLayout, pendingIndex, doneIndex);
        } else if (clickedSection.getId() == R.id.btn_done) {
            toggleRowsBelowIndex(tableLayout, doneIndex, tableLayout.getChildCount());
        }

        // Update the arrow direction
        if (clickedSection.getTag().equals("true")) {
            clickedSection.setRotation(0); // Set the angle of rotation to 0 degrees
            clickedSection.setTag("false");
        } else {
            clickedSection.setRotation(90); // Set the angle of rotation to 90 degrees
            clickedSection.setTag("true");
        }
    }



    private void addTask(Task task) {
        String taskName = task.getTaskName();
        String dueDate = task.getDueDate();
        String section = task.getSection();
        String priority = task.getPriority();
        String creationDate = task.getTimeCreation();

        LinearLayout kanban;
        // Find the table row to add the new task based on the section
        TableRow tableRow;

        switch (section) {
            case "To Do":
                kanban = kanbanList.get(0);
                tableRow = findViewById(R.id.row_todo);
                break;
            case "Pending":
                kanban = kanbanList.get(1);
                tableRow = findViewById(R.id.row_pending);
                break;
            case "Done":
                kanban = kanbanList.get(2);
                tableRow = findViewById(R.id.row_done);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + section);
        }

        // set color as task priority
        int color;
        switch (priority) {
            case "LOW":
                color = Color.GREEN;
                break;
            case "MEDIUM":
                color = Color.YELLOW;
                break;
            case "HIGH":
                color = Color.RED;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + priority);
        }

        // Create a new table row to add
        TableRow newRow = new TableRow(this);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        newRow.setLayoutParams(layoutParams);

        // for show and unshow  purpose
        newRow.setTag(R.id.SectionOptions, section);

        // Add task name and due date to the new row
        TextView emptyView = new TextView(this);
        emptyView.setText("");
        emptyView.setPadding(8, 8, 8, 8);
        newRow.addView(emptyView);


        // Set margins for the taskNameTextView
        TableRow.LayoutParams taskNameLayoutParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        taskNameLayoutParams.setMargins(10, 10, 10, 10);

        TextView taskNameTextView = new TextView(this);
        taskNameTextView.setTag(task.getTaskId());
        taskNameTextView.setLayoutParams(taskNameLayoutParams);
        taskNameTextView.setPaintFlags(taskNameTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        taskNameTextView.setText(taskName);
        taskNameTextView.setTextColor(Color.BLUE);
        taskNameTextView.setBackgroundColor(color);
        taskNameTextView.setPadding(25, 8, 8, 8);
        newRow.addView(taskNameTextView);

        View divider = new View(this);
        TableRow.LayoutParams dividerParams = new TableRow.LayoutParams(1, TableRow.LayoutParams.MATCH_PARENT);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(Color.parseColor("#9A9A9A"));
        newRow.addView(divider);

        TextView assigneeTextView = new TextView(this); // Placeholder for assignee, leave empty
        assigneeTextView.setText(creationDate);
        assigneeTextView.setPadding(20, 8, 8, 8);
        newRow.addView(assigneeTextView);

        View divider2 = new View(this);
        divider2.setLayoutParams(dividerParams);
        divider2.setBackgroundColor(Color.parseColor("#9A9A9A"));
        newRow.addView(divider2);

        TextView dueDateTextView = new TextView(this);
        dueDateTextView.setText(dueDate);
        dueDateTextView.setPadding(25, 8, 8, 8);
        newRow.addView(dueDateTextView);


        //inflate tasks for kanban view
        LayoutInflater inflater = LayoutInflater.from(this);
        View kanbanTask = inflater.inflate(R.layout.kanban_task, null);
        kanbanTask.setTag(task.getTaskId());

        //add details of task
        TextView kanbanTaskNameTextView = kanbanTask.findViewById(R.id.kanbanTaskName);
        TextView kanbanTaskAssigneeTextView = kanbanTask.findViewById(R.id.kanbanTaskAssignee);
        TextView kanbanTaskDateTextView = kanbanTask.findViewById(R.id.kanbanTaskDate);
        kanbanTaskNameTextView.setText(taskName);
        kanbanTaskAssigneeTextView.setText("234");
        kanbanTaskDateTextView.setText(dueDate);

        //On click listener for tasks
        View.OnClickListener taskClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ProjectTasks.this, TaskDetailActivity.class);
                i.putExtra("clickedTask",  task);
                i.putExtra("ProjectMembersEmail", projectEmails.toArray(new String[0]));
                startActivity(i);
            }
        };

        //add listener for both list and kanban
        kanbanTask.setOnClickListener(taskClick);
        taskNameTextView.setOnClickListener(taskClick);


        // Add the new row below the corresponding section
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        int index = tableLayout.indexOfChild(tableRow) + 1; // Get the index to insert the new row below the section
        tableLayout.addView(newRow, index);

        //add to corresponding section of kanban
        kanban.addView(kanbanTask);
    }

    @Override
    public void onDialogDismissed() {
        onResume(); // Refresh the task list when the dialog is dismissed
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
