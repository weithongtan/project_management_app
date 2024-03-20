package utar.edu.project_management_app;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class TaskCreationActivity extends AppCompatActivity {

    Button pickDateButton;
    Calendar calendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_creation_bottom_sheet);

        pickDateButton = findViewById(R.id.pickDateButton);
        calendar = Calendar.getInstance();

        pickDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

    private void showDatePickerDialog() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(TaskCreationActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Do something with the selected date
                        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        Toast.makeText(TaskCreationActivity.this, "Selected Date: " +
                                selectedDate, Toast.LENGTH_SHORT).show();
                    }
                }, year, month, dayOfMonth);

        // Set max date to prevent selecting future dates
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

}
