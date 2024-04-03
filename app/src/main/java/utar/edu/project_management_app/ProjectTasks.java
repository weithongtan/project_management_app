package utar.edu.project_management_app;


import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.view.LayoutInflater;



import androidx.appcompat.app.AppCompatActivity;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProjectTasks extends AppCompatActivity {

    private TextView openBottomSheetButton;
    private Spinner spinnerOptions;
    private List<ImageView> dropDownButtonSectionList;

    HashMap<String, Object> tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_tasks);

        // include the menu layout
        View includedLayout = LayoutInflater.from(this).inflate(R.layout.activity_project_tasks_menu, null);

        // change view
        spinnerOptions = findViewById(R.id.spinnerOptions);
        // add drop down item
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.view, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOptions.setAdapter(adapter);

        dropDownButtonSectionList = new ArrayList<>();
        dropDownButtonSectionList.add(findViewById(R.id.btn_to_do));
        dropDownButtonSectionList.add(findViewById(R.id.btn_pending));
        dropDownButtonSectionList.add(findViewById(R.id.btn_done));

        // create new task button
        openBottomSheetButton = findViewById(R.id.btn_create_task);
        tasks = new HashMap<String, Object>();
        openBottomSheetButton.setOnClickListener(v -> {
            // Show the bottom sheet dialog
            ProjectTasksCreationBottomSheetDialogFragment bottomSheet = new ProjectTasksCreationBottomSheetDialogFragment();
            bottomSheet.setTaskSubmitListerner(taskcreate -> {
                tasks = taskcreate;
                // Update textView6 with the task details
                TextView tv = findViewById(R.id.textView6);
                // Assuming you want to display the task name and due date
                String taskName = tasks.get("Task Name").toString();
                String dueDate = tasks.get("Due Date").toString();
                String section = tasks.get("Section").toString();
                String description = tasks.get("Description").toString();
                addTask();
            });
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });

    }

    public void dropDownShowDetail(View view){
        ImageView clickedSection = findViewById(view.getId());

        if (clickedSection.getTag().equals("false")) {
            // Rotate the arrow to point downwards
            clickedSection.setRotation(90); // Set the angle of rotation to 180 degrees
            clickedSection.setTag("true");
            // Update the tag to reflect the new arrow direction
        } else {
            clickedSection.setRotation(0); // Set the angle of rotation to 0 degrees
            clickedSection.setTag("false"); // Update the tag to reflect the new arrow direction
        }


    }

    private void addTask(){
        String taskName = tasks.get("Task Name").toString();
        String dueDate = tasks.get("Due Date").toString();
        String section = tasks.get("Section").toString();

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

        // Create a new table row to add
        TableRow newRow = new TableRow(this);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        newRow.setLayoutParams(layoutParams);



        // Add task name and due date to the new row
        TextView empptyView = new TextView(this);
        empptyView.setText("");
        empptyView.setPadding(8, 8, 8, 8);
        newRow.addView(empptyView);

        TextView taskNameTextView = new TextView(this);
        taskNameTextView.setText(taskName);
        taskNameTextView.setPadding(8, 8, 8, 8);
        newRow.addView(taskNameTextView);

        View divider = new View(this);
        TableRow.LayoutParams dividerParams = new TableRow.LayoutParams(1, TableRow.LayoutParams.MATCH_PARENT);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(Color.parseColor("#9A9A9A"));
        newRow.addView(divider);

        TextView assigneeTextView = new TextView(this); // Placeholder for assignee, leave empty
        assigneeTextView.setText("234");
        assigneeTextView.setPadding(8, 8, 8, 8);
        newRow.addView(assigneeTextView);

        View divider2 = new View(this);
        divider2.setLayoutParams(dividerParams);
        divider2.setBackgroundColor(Color.parseColor("#9A9A9A"));
        newRow.addView(divider2);

        TextView dueDateTextView = new TextView(this);
        dueDateTextView.setText(dueDate);
        dueDateTextView.setPadding(8, 8, 8, 8);
        newRow.addView(dueDateTextView);

        // Add the new row below the corresponding section
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        int index = tableLayout.indexOfChild(tableRow) + 1; // Get the index to insert the new row below the section
        tableLayout.addView(newRow, index);
    }


}
