package utar.edu.project_management_app;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;
import java.util.HashMap;

public class ProjectTasksCreationBottomSheetDialogFragment extends BottomSheetDialogFragment{


    private Spinner spinnerOptions;
    private TextView submitTaskButton, dueDate;
    private EditText taskName, description;
    private OnTaskSubmitListener taskSubmitListener;
    private HashMap<String, Object> tasks;

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

        // find view for used id
        spinnerOptions = view.findViewById(R.id.spinnerOptions);
        dueDate = view.findViewById(R.id.selectedDateTextView);
        LinearLayout selectDateButton = view.findViewById(R.id.filterButton);
        submitTaskButton = view.findViewById(R.id.btn_create_task);
        taskName = view.findViewById(R.id.ev_task_name);
        description = view.findViewById(R.id.ev_description);

        // Spinner for todo, pending and complete
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.section, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOptions.setAdapter(adapter);

        // date picker
        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        tasks = new HashMap<String, Object>();
        submitTaskButton.setOnClickListener(v->{
            if(taskSubmitListener != null){
                tasks.put("Task Name", taskName.getText());
                tasks.put("Due Date", dueDate.getText());
                tasks.put("Section", spinnerOptions.getSelectedItem());
                tasks.put("Description", description.getText());
                taskSubmitListener.onTaskSubmit(tasks);
                dismiss();

            }
        });

        return view;
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
                    }
                }, year, month, dayOfMonth);

        // Show DatePickerDialog
        datePickerDialog.show();
    }
}

