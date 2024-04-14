package utar.edu.project_management_app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private List<String> expandableListTitle;
    private HashMap<String, List<String>> expandableListDetail;
    private List<Task> tasks;
    private String userId;
    private FirebaseAuth authProfile;
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    public MyTaskListAdapter(Context context, List<String> expandableListTitle,
                                       HashMap<String, List<String>> expandableListDetail) {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
        this.expandableListDetail = expandableListDetail;
    }

//    private void getTask(){
//        authProfile = FirebaseAuth.getInstance();
//        FirebaseUser firebaseUser = authProfile.getCurrentUser();
//
//        if (firebaseUser==null){
//            Toast.makeText(context, "Something went wrong! User's details are not available", Toast.LENGTH_LONG).show();
//        }
//        else {
//            userId = firebaseUser.getUid();
//            DatabaseReference tasksRef = database.child("task");
//            Query query = tasksRef.orderByChild("userId").equalTo(userId);
//            tasksRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    tasks = new ArrayList<>();
//                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                        Task task = snapshot.getValue(Task.class);
//                        tasks.add(task);
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                    // Handle possible errors
//
//                }
//            });
//        }
//    }
    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String expandedListText = (String) getChild(listPosition, expandedListPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item, null);
        }

        String[] expandedListTextArray = expandedListText.split(";");
        TextView expandedListTextView = (TextView) convertView
                .findViewById(R.id.expandedListItem);
        expandedListTextView.setText(expandedListTextArray[0]);

        TextView expandedListTextView1 = (TextView) convertView
                .findViewById(R.id.expandedListItem1);
        expandedListTextView1.setText(expandedListTextArray[1]);

        TextView expandedListTextView2 = (TextView) convertView
                .findViewById(R.id.expandedListItem2);
        expandedListTextView2.setText(expandedListTextArray[2]);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(expandedListTextArray[0],expandedListTextArray[1]);
//                Task clickedtask = new Task();
//                for (int i = 0;i<tasks.size();i++){
//                    if (v.getTag() == tasks.get(i).getTaskId()){
//                        clickedtask = tasks.get(i);
//                        break;
//                    }
//                }
//
//
//                Intent i = new Intent(context, TaskDetailActivity.class);
//                i.putExtra("clickedTask",  clickedtask);
//                context.startActivity(i);
            }
        });
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .size();
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