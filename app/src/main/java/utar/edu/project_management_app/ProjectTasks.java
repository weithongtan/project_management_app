package utar.edu.project_management_app;


import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

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
        openBottomSheetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the bottom sheet dialog
                TaskCreationActivity bottomSheetDialog = TaskCreationActivity.newInstance();
                bottomSheetDialog.show(getSupportFragmentManager());
            }
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


}
