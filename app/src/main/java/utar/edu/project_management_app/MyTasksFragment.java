package utar.edu.project_management_app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import utar.edu.project_management_app.model.Task;

public class MyTasksFragment extends Fragment {

    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<Task> tasks;

    ImageView profileThumbnail;
    public MyTasksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // update profile pic on mytask
        FirebaseStorage storage = FirebaseStorage.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        StorageReference storageRef = storage.getReference().child("DisplayPics");
        String userId = user.getUid();
        String profilePicPath = userId + ".jpg";
        // Get download URL for the profile picture
        storageRef.child(profilePicPath).getDownloadUrl().addOnSuccessListener(uri -> {
            ImageView profileImageView = requireActivity().findViewById(R.id.profile_view);
            Picasso.get().load(uri).transform(new CircleTransform()).into(profileImageView);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_my_tasks, container, false);
        expandableListView = rootView.findViewById(R.id.expandableListView);
        expandableListAdapter = new MyTaskListAdapter(requireActivity());
        expandableListView.setAdapter(expandableListAdapter);

        ImageView backButton = rootView.findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new ProjectListFragment()).commit();
        });

        return rootView;

    }


}