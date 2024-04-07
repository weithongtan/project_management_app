package utar.edu.project_management_app;
import android.app.DatePickerDialog;
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

import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;

import utar.edu.project_management_app.model.Task;
import utar.edu.project_management_app.model.User;

public class ProjectTasksCreationBottomSheetDialogFragment extends BottomSheetDialogFragment{


    private Spinner sectionOptions, priorityOptions;
    private TextView submitTaskButton, dueDate;
    private EditText taskName, description;
    private OnTaskSubmitListener taskSubmitListener;
    private HashMap<String, Object> tasks;

    DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    public interface OnTaskSubmitListener{
        void onTaskSubmit(HashMap<String, Object> tasks);
    }

    public void setTaskSubmitListerner(OnTaskSubmitListener listener){
        this.taskSubmitListener = listener;
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

        description.setText(projectId);
        // Spinner for todo, pending and complete
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

        tasks = new HashMap<String, Object>();
        submitTaskButton.setOnClickListener(v->{
            if(taskSubmitListener != null && isFilled()){
                taskName.setBackgroundColor(Color.RED);
                taskName.setBackgroundTintList(ColorStateList.valueOf(Color.RED));

                tasks.put("Task Name", taskName.getText());
                tasks.put("Due Date", dueDate.getText());
                tasks.put("Section", sectionOptions.getSelectedItem());
                tasks.put("Priority", priorityOptions.getSelectedItem());
                tasks.put("Description", description.getText());
                tasks.put("Time Creation", LocalDateTime.now());
                taskSubmitListener.onTaskSubmit(tasks);
                DatabaseReference counterRef = database.child("taskCounter");
                counterRef.setValue(0);
//                Task task = new Task(taskName.getText(),dueDate.getText())
//                User user = new User("001", "fuwejh","fcuwje", "gmail");
//                database.child("users").child(user.getUserId()).setValue(user);
                dismiss();

            }
        });

        return view;
    }
    private boolean isFilled() {
        boolean isTaskNameEmpty = taskName.getText().toString().isEmpty();
        boolean isDateEmpty = !Character.isDigit(dueDate.getText().toString().charAt(0));

        if (isTaskNameEmpty) {
            taskName.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            taskName.setHintTextColor(Color.parseColor("#85FF0000"));
        } else {
            taskName.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));// Reset to default or desired color when not empty

        }

        if (isDateEmpty) {
            dueDate.setTextColor(Color.RED);
        } else {
            dueDate.setTextColor(Color.BLACK); // Reset to default or desired color when not empty
        }

        return !isTaskNameEmpty && !isDateEmpty;
    }

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

