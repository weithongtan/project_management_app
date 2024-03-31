package utar.edu.project_management_app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyTasksFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;

    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    HashMap<String, List<String>> expandableListDetail;

    public MyTasksFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyTasksFragment.
     */
    // TODO: Rename and change types and number of parameters
//    public static MyTasksFragment newInstance(String param1, String param2) {
//        MyTasksFragment fragment = new MyTasksFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_my_tasks, container, false);
        expandableListView = rootView.findViewById(R.id.expandableListView);
        expandableListDetail = getData();
        expandableListTitle = new ArrayList<String>(expandableListDetail.keySet());
        expandableListAdapter = new MyTaskListAdapter(requireActivity(), expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);
        return rootView;
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