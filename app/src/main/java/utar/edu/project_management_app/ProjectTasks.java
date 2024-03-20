package utar.edu.project_management_app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ProjectTasks extends AppCompatActivity {

    TextView openBottomSheetButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_tasks);

        View includedLayout = LayoutInflater.from(this).inflate(R.layout.activity_project_tasks_menu, null);

        openBottomSheetButton = findViewById(R.id.btn_create_task);
        openBottomSheetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheet();
            }
        });

    }
    private void showBottomSheet() {
        // Create an instance of BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        // Inflate the layout for the bottom sheet
        View bottomSheetView = getLayoutInflater().inflate(R.layout.task_creation_bottom_sheet, null);

        // Set the view for the bottom sheet
        bottomSheetDialog.setContentView(bottomSheetView);

        // Show the bottom sheet
        bottomSheetDialog.show();
    }
}
