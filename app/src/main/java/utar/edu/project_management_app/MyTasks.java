package utar.edu.project_management_app;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MyTasks extends AppCompatActivity {

    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    HashMap<String, List<String>> expandableListDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tasks);
        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        expandableListDetail = getData();
        expandableListTitle = new ArrayList<String>(expandableListDetail.keySet());
        expandableListAdapter = new MyTaskListAdapter(this, expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);

    }

    public static HashMap<String, List<String>> getData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        List<String> todo = new ArrayList<String>();
        String[] todo1 = {"Task1", "project 1", "21 May"};
        todo.add(String.join(";",todo1));
        String[] todo2 = {"Task2", "project 1", "21 May"};
        todo.add(String.join(";",todo2));

        List<String> pending = new ArrayList<String>();
        String[] pending1 = {"Task3", "project 1", "21 May"};
        pending.add(String.join(";",pending1));
        String[] pending2 = {"Task4", "project 1", "21 May"};
        pending.add(String.join(";",pending2));

        List<String> done = new ArrayList<String>();
        String[] done1 = {"Task5", "project 1", "21 May"};
        done.add(String.join(";",done1));
        String[] done2 = {"Task6", "project 1", "21 May"};
        done.add(String.join(";",done2));

        expandableListDetail.put("To-Do", todo);
        expandableListDetail.put("Pending", pending);
        expandableListDetail.put("Done", done);
        return expandableListDetail;
    }

}