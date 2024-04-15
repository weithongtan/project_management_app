package utar.edu.project_management_app;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import utar.edu.project_management_app.model.Task;

public class MyTaskListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> tasks;
    Map<String, List<Task>> sectionTasks;
    List<String> expandableListTitle;
    private String userId;
    private FirebaseAuth authProfile;
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    public MyTaskListAdapter(Context context) {
        this.context = context;
        sectionTasks = new HashMap<>();
        sectionTasks.put("To Do",new ArrayList<>());
        sectionTasks.put("Pending",new ArrayList<>());
        sectionTasks.put("Done",new ArrayList<>());
        expandableListTitle = Arrays.asList("To Do", "Pending", "Done");
        getTask();
    }

    private void getTask(){
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser==null){
            Toast.makeText(context, "Something went wrong! User's details are not available", Toast.LENGTH_LONG).show();
        }
        else {
            userId = firebaseUser.getUid();
            try {
                DatabaseReference tasksRef = database.child("Registered Users");
                Query query = tasksRef.child(userId).child("taskId");
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        tasks = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String taskId = snapshot.getKey(); // Getting the taskId from the snapshot key
                            tasks.add(taskId);
                        }
                        fetchTasks();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle possible errors

                    }
                });


            }catch (Exception e){
                Toast.makeText(context, "No Tasks Found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchTasks() {
        DatabaseReference tasksRef = database.child("task");
        for (String taskId : tasks) {
            Query query = tasksRef.orderByKey().equalTo(taskId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Task task = snapshot.getValue(Task.class);
                        String section = task.getSection();
                        List<Task> sectionList = sectionTasks.getOrDefault(section, new ArrayList<>());
                        sectionList.add(task);
                        sectionTasks.put(section, sectionList);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle possible errors
                }
            });
        }
    }
    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        String section = expandableListTitle.get(listPosition);
        // Get tasks for this section
        List<Task> tasksInSection = sectionTasks.get(section);
        // Return the task at the expanded list position
        return tasksInSection.get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        Task task = (Task) getChild(listPosition, expandedListPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item, null);
        }

        TextView expandedListTextView = (TextView) convertView
                .findViewById(R.id.expandedListItem);
        expandedListTextView.setText(task.getTaskName());

        TextView expandedListTextView1 = (TextView) convertView
                .findViewById(R.id.expandedListItem1);
        expandedListTextView1.setText(task.getTimeCreation());

        TextView expandedListTextView2 = (TextView) convertView
                .findViewById(R.id.expandedListItem2);
        expandedListTextView2.setText(task.getDueDate());
        convertView.setTag(task.getTaskId());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, TaskDetailActivity.class);
                i.putExtra("clickedTask",  task);
                i.putExtra("ProjectMembersEmail", task.getUserEmails().toArray(new String[0]));
                context.startActivity(i);
            }
        });
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        String section = expandableListTitle.get(listPosition);
        List<Task> tasksInSection = sectionTasks.get(section);
        if (tasksInSection != null){
            return tasksInSection.size();
        }
        else{
            return 0;
        }
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.expandableListTitle.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.expandableListTitle.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, null);
        }
        TextView listTitleTextView = (TextView) convertView
                .findViewById(R.id.listTitle);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}