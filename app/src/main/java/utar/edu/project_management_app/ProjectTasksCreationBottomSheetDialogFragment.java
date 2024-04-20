package utar.edu.project_management_app;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import utar.edu.project_management_app.model.Task;

public class ProjectTasksCreationBottomSheetDialogFragment extends BottomSheetDialogFragment{


    private Spinner sectionOptions, priorityOptions;
    private TextView submitTaskButton, dueDate;
    private EditText taskName, description;

    DatabaseReference database = FirebaseDatabase.getInstance().getReference();



    public interface OnDialogDismissListener {
        void onDialogDismissed();
    }
    private OnDialogDismissListener dismissListener;

    public void setOnDialogDismissListener(OnDialogDismissListener listener) {
        this.dismissListener = listener;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.task_creation_bottom_sheet, container, false);

        // Retrieve the project ID from the arguments
        Bundle args = getArguments();
        String projectId = args.getString("projectId", "");

        // find view for used id
        sectionOptions = view.findViewById(R.id.SectionOptions);
        priorityOptions = view.findViewById(R.id.PriorityOptions);
        dueDate = view.findViewById(R.id.selectedDateTextView);
        LinearLayout selectDateButton = view.findViewById(R.id.filterButton);
        submitTaskButton = view.findViewById(R.id.btn_create_task);
        taskName = view.findViewById(R.id.ev_task_name);
        description = view.findViewById(R.id.ev_description);

        // Spinner for to do, pending and complete
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.section, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sectionOptions.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(requireContext(),
                R.array.priority, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priorityOptions.setAdapter(adapter2);


        // date picker
        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        DatabaseReference counterRef = database.child("taskCounter");


        submitTaskButton.setOnClickListener(v->{


            // for read the task counter from db to ensure multiple user wont create same task id
            counterRef.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    Integer currentValue = mutableData.getValue(Integer.class);
                    if (currentValue == null) {
                        mutableData.setValue(1);
                    } else {
                        mutableData.setValue(currentValue + 1);
                    }
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                    if (committed) {
                        if(isFilled()){

                            // update database
                            Task newTask = new Task(UUID.randomUUID().toString(),taskName.getText().toString(),dueDate.getText().toString(),
                                    priorityOptions.getSelectedItem().toString(),sectionOptions.getSelectedItem().toString(),
                                    description.getText().toString(),projectId);
                            newTask.getUserEmails().add(getCurrentUserEmail());

                            // update task and project in realtime database
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/task/" + newTask.getTaskId(), newTask);
                            childUpdates.put("/projects/"+newTask.getProjectId()+"/taskId/" + newTask.getTaskId(), true);
                            childUpdates.put("/Registered Users/" + getCurrentUserId()+"/taskId/" + newTask.getTaskId(), true);

                            database.updateChildren(childUpdates);

                            dismiss();

                        }

                    }
                }
            });

        });
        return view;
    }

    // get current user email
    private String getCurrentUserEmail() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getEmail();
        }
        return null;
    }
    // get current user id
    private String getCurrentUserId() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        }
        return null;
    }
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dismissListener != null) {
            dismissListener.onDialogDismissed();
        }
    }

    // to ensure the important task detail is filled before adding
    private boolean isFilled() {
        boolean isTaskNameEmpty = taskName.getText().toString().isEmpty();
        boolean isDateEmpty = !Character.isDigit(dueDate.getText().toString().charAt(0));

        if (isTaskNameEmpty) {
            taskName.setHint(taskName.getHint() + "*");
            taskName.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            taskName.setHintTextColor(Color.parseColor("#85FF0000"));
        } else {
            taskName.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));// Reset to default or desired color when not empty

        }

        if (isDateEmpty) {
            dueDate.setText(dueDate.getText() + "*");
            dueDate.setTextColor(Color.RED);
        } else {
            dueDate.setTextColor(Color.BLACK); // Reset to default or desired color when not empty
        }

        return !isTaskNameEmpty && !isDateEmpty;
    }

    // show to pick the date
    private void showDatePickerDialog() {
        // Get current date to set as default in DatePickerDialog
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Update TextView with selected date
                        dueDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                        dueDate.setTextColor(Color.BLACK);
                    }
                }, year, month, dayOfMonth);

        // Show DatePickerDialog
        datePickerDialog.show();
    }
}

