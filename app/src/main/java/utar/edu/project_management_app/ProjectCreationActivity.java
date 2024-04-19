package utar.edu.project_management_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ProjectCreationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_creation);

        // Create Button Click Listener
        TextView createButton = findViewById(R.id.btn_create_project);


        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement project creation logic here


            }
        });

        // Cancel Button Click Listener
        TextView cancelButton = findViewById(R.id.btn_cancel_project);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement logic to cancel project creation and navigate back
            }
        });
    }

}