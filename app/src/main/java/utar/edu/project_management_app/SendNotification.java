package utar.edu.project_management_app;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendNotification {

    public interface TokensCallback {
        void onTokensReady(List<String> tokens);
    }
    public static void getTokens(DatabaseReference database, List<String> newInvitedEmails,TokensCallback callback) {
        DatabaseReference UsersRef = database.child("Registered Users");
        List<String> tokens = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(newInvitedEmails.size()); // Track completed queries

        for (String email : newInvitedEmails) {
            Query query = UsersRef.orderByChild("email").equalTo(email);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Iterate through the data snapshot to find the matching email
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        // Retrieve the token associated with the email
                        String token = userSnapshot.child("fcmtoken").getValue(String.class);
                        if (token != null) {
                            tokens.add(token);
                        }
                    }

                    // Decrement the counter and check if all queries are complete
                    if (counter.decrementAndGet() == 0) {
                        // All queries have been completed, invoke the callback with the list of tokens
                        callback.onTokensReady(tokens);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle any database errors here
                    Log.e("Firebase", "Database error: " + databaseError.getMessage());
                }
            });
        }
    }
    public static void sendNotification(String fcmToken, String username, String typeName, String type) {
        // Create a Callable to handle the network request
        Callable<Void> callable = () -> {
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json");

            // Create a message to send
            JSONObject jsonNotif = new JSONObject();
            JSONObject jsonMsg = new JSONObject();
            try {
                jsonMsg.put("title", "New " + type);
                jsonMsg.put("body", username + " has added you to a new " + type + " " + typeName);
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

        // Handle the future if needed (e.g., for error handling or result retrieval)
        try {
            future.get(); // Wait for the Callable to complete
        } catch (Exception e) {
            Log.e("Error", "Error executing Callable: " + e.getMessage());
        } finally {
            executor.shutdown(); // Shut down the executor
        }
    }
}
