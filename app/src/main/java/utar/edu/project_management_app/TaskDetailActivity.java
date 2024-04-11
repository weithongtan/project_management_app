package utar.edu.project_management_app;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import utar.edu.project_management_app.model.Task;


public class TaskDetailActivity extends AppCompatActivity {

    Task clickedTask;
    LinearLayout btn_back;
    TextView tv_taskname,tv_creationDate, tv_dueDate, tv_description,edit_duedate, btn_edit_description;
    Spinner priority, section;
    EditText et_description;
    ImageView  delete;

    boolean isChanged = false;
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_task_detail);

        btn_back = findViewById(R.id.btn_back);

        clickedTask = (Task) getIntent().getSerializableExtra("clickedTask");

        fillDetail();

        delete = findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();

            }
        });

        edit_duedate = findViewById(R.id.edit_duedate);
        edit_duedate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        btn_edit_description = findViewById(R.id.btn_edit_description);
        tv_description = findViewById(R.id.tv_description);
        et_description = findViewById(R.id.et_description);
        btn_edit_description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (btn_edit_description.getText().toString() == "Edit"){
                    btn_edit_description.setText("Done");
                    tv_description.setVisibility(View.GONE);
                    et_description.setVisibility(View.VISIBLE);
                    et_description.setText(tv_description.getText());
                    et_description.requestFocus();

                }else{
                    btn_edit_description.setText("Edit");
                    tv_description.setText(et_description.getText());
                    tv_description.setVisibility(View.VISIBLE);
                    et_description.setVisibility(View.GONE);
                }
                isChanged = true;
            }
        });
        btn_back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                if (isChanged){
                    clickedTask.setDueDate(tv_dueDate.getText().toString());
                    clickedTask.setPriority(priority.getSelectedItem().toString());
                    clickedTask.setSection(section.getSelectedItem().toString());
                    clickedTask.setDescription(tv_description.getText().toString());

                    // update task and project in realtime database
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/task/" + clickedTask.getTaskId(), clickedTask);

                    database.updateChildren(childUpdates);

                }

                finish();
            }
        });
    }
    private void fillDetail() {
        tv_taskname = findViewById(R.id.tv_task_name);
        tv_description = findViewById(R.id.tv_description);
        tv_dueDate = findViewById(R.id.tv_duedate);
        tv_creationDate = findViewById(R.id.tv_datecreation);
        et_description =findViewById(R.id.et_description);

        // Set the due date from the clickedTask object
        String date_creation = clickedTask.getTimeCreation();


        // change view
        section = findViewById(R.id.spinner_section);

        // add drop down item
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.section, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        section.setAdapter(adapter);

        int selectionSectionPosition = adapter.getPosition(clickedTask.getSection());
        section.setSelection(selectionSectionPosition);

        priority = findViewById(R.id.spinner_priority);
        // add drop down item
        ArrayAdapter<CharSequence> priority_adapter = ArrayAdapter.createFromResource(this,
                R.array.priority, android.R.layout.simple_spinner_item);
        priority_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priority.setAdapter(priority_adapter);

        int selectedPriorityPosition = priority_adapter.getPosition(clickedTask.getPriority());
        priority.setSelection(selectedPriorityPosition);


        // Set the text views with the task details
        tv_taskname.setText(clickedTask.getTaskName());
        tv_creationDate.setText(date_creation);
        tv_dueDate.setText(clickedTask.getDueDate());
        tv_description.setText(clickedTask.getDescription());
        et_description.setText(clickedTask.getDescription());
    }
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DatabaseReference taskRef = database.child("task").child(clickedTask.getTaskId());
                        taskRef.removeValue();
                        DatabaseReference projectTaskRef = database.child("project").child(clickedTask.getProjectId()).child("taskId").child(clickedTask.getTaskId());
                        projectTaskRef.removeValue();
                        DatabaseReference taskCounterRef = database.child("taskCounter");
                        taskCounterRef.runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                Integer currentValue = mutableData.getValue(Integer.class);
                                if (currentValue != null && currentValue > 0) {
                                    mutableData.setValue(currentValue - 1);
                                }
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                                if (committed) {
                                    Log.d("TaskDetailActivity", "Task counter decremented successfully");
                                } else {
                                    Log.e("TaskDetailActivity", "Failed to decrement task counter", databaseError.toException());
                                }
                            }
                        });
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                    }
                });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void showDatePickerDialog() {
        // Get current date to set as default in DatePickerDialog
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        tv_dueDate = findViewById(R.id.tv_duedate);

        // Create DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Update TextView with selected date
                        String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        tv_dueDate.setText(date);
                        tv_dueDate.setTextColor(Color.BLACK);
                        isChanged = true;
                    }
                }, year, month, dayOfMonth);


        // Show DatePickerDialog
        datePickerDialog.show();
    }

}
