package utar.edu.project_management_app;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import java.util.Calendar;


public class TaskCreationActivity extends DialogFragment {

    private TextView selectedDateTextView;
    private Spinner spinnerOptions;
    public static TaskCreationActivity newInstance() {
        return new TaskCreationActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.task_creation_bottom_sheet, container, false);

        spinnerOptions = view.findViewById(R.id.spinnerOptions);
        selectedDateTextView = view.findViewById(R.id.selectedDateTextView);
        LinearLayout selectDateButton = view.findViewById(R.id.filterButton);

        // View spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.section, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOptions.setAdapter(adapter);

        // date pivkrt
        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
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
                        selectedDateTextView.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                    }
                }, year, month, dayOfMonth);

        // Show DatePickerDialog
        datePickerDialog.show();
    }

    // Convenience method to show the bottom sheet dialog
    public void show(FragmentManager fragmentManager) {
        super.show(fragmentManager, "bottomSheet");
    }
}
