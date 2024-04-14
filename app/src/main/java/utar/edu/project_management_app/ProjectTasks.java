package utar.edu.project_management_app;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.view.LayoutInflater;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import utar.edu.project_management_app.model.Project;
import utar.edu.project_management_app.model.Task;
import utar.edu.project_management_app.model.User;

public class ProjectTasks extends AppCompatActivity implements ProjectTasksCreationBottomSheetDialogFragment.OnDialogDismissListener{

    private Task task;
    private List<Task> tasks;
    private TextView openBottomSheetButton;
    private Spinner spinnerOptions;
    private List<ImageView> dropDownButtonSectionList;
    private String projectId ;
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


        findViewById(R.id.btn_to_do).setRotation(90);
        findViewById(R.id.btn_pending).setRotation(90);
        findViewById(R.id.btn_done).setRotation(90);

        // Todo kanban (Junyi)
        // change view
        spinnerOptions = findViewById(R.id.SectionOptions);
        // add drop down item
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.view, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOptions.setAdapter(adapter);

        // Return to main screen
        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
                finish();
            }
        );

        dropDownButtonSectionList = new ArrayList<>();
        dropDownButtonSectionList.add(findViewById(R.id.btn_to_do));
        dropDownButtonSectionList.add(findViewById(R.id.btn_pending));
        dropDownButtonSectionList.add(findViewById(R.id.btn_done));

        // create new task button
        openBottomSheetButton = findViewById(R.id.btn_create_task);

        openBottomSheetButton.setOnClickListener(v -> {
            // Show the bottom sheet dialog
            ProjectTasksCreationBottomSheetDialogFragment bottomSheet = new ProjectTasksCreationBottomSheetDialogFragment();
            Bundle args = new Bundle();
            args.putString("projectId", projectId); // Pass the project ID to the fragment
            bottomSheet.setArguments(args);
            bottomSheet.setOnDialogDismissListener(this); // Set the listener

//            bottomSheet.setTaskSubmitListerner(taskcreate -> {
//                addTask(taskcreate);
//
//
//            });
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });



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
        tasksRef.addListenerForSingleValueEvent(new ValueEventListener() {
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


        // Find the table row to add the new task based on the section
        TableRow tableRow;

        switch (section) {
            case "To Do":
                tableRow = findViewById(R.id.row_todo);
                break;
            case "Pending":
                tableRow = findViewById(R.id.row_pending);
                break;
            case "Done":
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
        assigneeTextView.setText("234");
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

        taskNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Task clickedtask = new Task();
                for (int i = 0;i<tasks.size();i++){
                    if (v.getTag() == tasks.get(i).getTaskId()){
                        clickedtask = tasks.get(i);
                        break;
                    }
                }


                Intent i = new Intent(ProjectTasks.this, TaskDetailActivity.class);
                i.putExtra("clickedTask",  clickedtask);
                startActivity(i);
            }
        });

        // Add the new row below the corresponding section
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        int index = tableLayout.indexOfChild(tableRow) + 1; // Get the index to insert the new row below the section
        tableLayout.addView(newRow, index);
    }

    @Override
    public void onDialogDismissed() {
        onResume(); // Refresh the task list when the dialog is dismissed
    }
}
