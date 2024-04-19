package utar.edu.project_management_app;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import utar.edu.project_management_app.model.Task;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONException;
import org.json.JSONObject;

public class MyTasksFragment extends Fragment{

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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_my_tasks, container, false);
        expandableListView = rootView.findViewById(R.id.expandableListView);
        expandableListAdapter = new MyTaskListAdapter(requireActivity());
        expandableListView.setAdapter(expandableListAdapter);


        FirebaseStorage storage = FirebaseStorage.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        StorageReference storageRef = storage.getReference().child("DisplayPics");
        String userId = user.getUid();
        String profilePicPath = userId + ".jpg";
        ImageView profileImageView = rootView.findViewById(R.id.profile_view);
        // Get download URL for the profile picture
        storageRef.child(profilePicPath).getDownloadUrl().addOnSuccessListener(uri -> {

            Picasso.get().load(uri).transform(new CircleTransform()).into(profileImageView);

        });
        sendNotification("ej8rpP3GTUuojI5Ba1Nbh_:APA91bHjUS9oTwlgbFBpLff5bvOrgCVhxLUGdy4ZMfs10eKG_C7Mr8nN7fniRbpPtQnzbjFt1aqpwuflVh0T7W9hRVPjSabHizG6pSNck4GaEVZIwZLq3U2sDWpebjBwHczmyBPiuQWX", "test_user", "mobile app", "project");

        return rootView;

    }

    private void sendNotification(String fcmToken, String username, String typeName, String type) {
        // Create a Callable to handle the network request
        Callable<Void> callable = () -> {
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json");

            // Create a message to send
            JSONObject jsonNotif = new JSONObject();
            JSONObject jsonMsg = new JSONObject();
            try {
                jsonMsg.put("title", "New " + type);
                jsonMsg.put("body", username + " has added to a new " + type + " " + typeName);
                jsonNotif.put("to", fcmToken);
                jsonNotif.put("notification", jsonMsg);
            } catch (JSONException e) {
                Log.e("Error", "Error creating notification JSON");
            }

            RequestBody requestBody = RequestBody.create(mediaType, jsonNotif.toString());
            Request request = new Request.Builder()
                    .url("https://fcm.googleapis.com/fcm/send")
                    .post(requestBody)
                    .addHeader("Authorization", "key=AAAAHvWPMxw:APA91bEatuoNQbGQvaytx-0q-rGh2XYzIJX-_HbImOdb_GAQ1JKyw2U9EmiTw8HyUihcxjBAhD5_2_A2JHq_m9Hy4wOlHdWH1MYyBMD2-Drq7Jo-RdNEs6ricjjnnGLqQv5PZpDB9ZXV")
                    .addHeader("Content-Type", "application/json")
                    .build();

            // Execute the request and handle the response
            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    Log.e("Error", "Failed to send notification: " + response.message());
                } else {
                    Log.d("Success", "Notification sent successfully");
                }
            } catch (IOException e) {
                Log.e("Error", "IOException during network call: " + e.getMessage());
            }

            return null;
        };

        // Create an ExecutorService to execute the Callable in the background
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Void> future = executor.submit(callable);

        // Optionally, handle the future if needed (e.g., for error handling or result retrieval)
        try {
            future.get(); // Wait for the Callable to complete
        } catch (Exception e) {
            Log.e("Error", "Error executing Callable: " + e.getMessage());
        } finally {
            executor.shutdown(); // Shut down the executor
        }
    }
    public void sendNotification1(Context context, String fcmToken, String username, String typeName, String type) {

        // Create an intent for the notification
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        // Create a notification using NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "add_user")
                .setSmallIcon(R.drawable.ic_notification)  // Set the icon for the notification
                .setContentTitle("New " + type)    // Set the title for the notification
                .setContentText(username + " has added to to a new " + type + " " + typeName)     // Set the text for the notification
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)  // Set the intent to launch when the notification is tapped
                .setAutoCancel(true);             // Automatically cancel the notification when the user taps it
        // Issue the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

}