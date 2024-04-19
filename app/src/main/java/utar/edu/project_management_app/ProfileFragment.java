package utar.edu.project_management_app;

import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.ContextCompat.getSystemService;
import android.app.NotificationManager;
import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class ProfileFragment extends Fragment {

    private TextView profileEmail, profileUsername, logoutTV, editProfileTV, changePwTV;
    private String username, email;
    private ImageButton editProfile;
    private ImageView profileIV,profileThumbnail,buttonBack;
    private EditText editTextUsername,editTextCurPassword, editTextNewPassword, editTextConfirmPassword;
    private Button buttonSave, buttonSavePw;
    private Target profileImageTarget;
    private FirebaseAuth authProfile;
    private static final String TAG = "ProfileActivity";
    private AlertDialog editProfileDialog, editUsernameDialog, editPasswordDialog;
    private SwitchCompat notificationSwitch;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileEmail = view.findViewById(R.id.profileEmail);
        profileUsername = view.findViewById(R.id.profileUsername);
        editProfile = view.findViewById(R.id.editButton);
        logoutTV = view.findViewById(R.id.logout);
        editProfileTV = view.findViewById(R.id.editProfile);
        changePwTV = view.findViewById(R.id.changePw);
        profileIV = view.findViewById(R.id.userPhoto);
        profileThumbnail = view.findViewById(R.id.profile_view);
        buttonBack = view.findViewById(R.id.btn_back);

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new ProjectListFragment()).commit();
            }
        });

        //Change profile pic
        profileIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (getActivity(), UploadProfilePicActivity.class);
                startActivity(intent);
            }
        });

        //Check user login status
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        
        if (firebaseUser==null){
            Toast.makeText(getActivity(), "Something went wrong! User's details are not available", Toast.LENGTH_LONG).show();
        }
        else{
            checkIfEmailVerified(firebaseUser);
            showUserProfile(firebaseUser);
        }

        //Edit username button
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditUsernameDialog();
            }
        });
        //Edit profile option
        editProfileTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });
        changePwTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditPasswordDialog();
            }
        });
        logoutTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authProfile.signOut();
                Toast.makeText(getActivity(), "Logged Out", Toast.LENGTH_LONG).show();
                Intent intent = new Intent (getActivity(), LoginActivity.class);

                //clear stack to prevent back
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        //Storing shared preference for notification
        notificationSwitch = view.findViewById(R.id.notificationSwitch);

        // Check if the notification permission is granted
        boolean notificationEnabled = NotificationManagerCompat.from(requireContext()).areNotificationsEnabled();
        // Get the user's notification preference from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean userNotificationPreference = sharedPreferences.getBoolean("notificationPreference", false);

        // Set the initial state of the switch based on the notification permission and user's preference
        notificationSwitch.setChecked(notificationEnabled && userNotificationPreference);

        // Set a listener to detect when the switch state changes
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // User turned on notifications

                    // Save user's notification preference
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("notificationPreference", true);
                    editor.apply();

                } else {
                    // User turned off notifications
                    // Save user's notification preference
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("notificationPreference", false);
                    editor.apply();

                }
            }
        });

        return view;

    }
    private void showEditPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Inflate the layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.edit_password_dialog, null);
        builder.setView(dialogView);
        builder.setCancelable(true);

        editTextCurPassword = dialogView.findViewById(R.id.editTextCurPassword);
        editTextNewPassword = dialogView.findViewById(R.id.editTextNewPassword);
        editTextConfirmPassword = dialogView.findViewById(R.id.editTextConfirmPassword);
        buttonSavePw = dialogView.findViewById(R.id.buttonSavePassword);
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser =authProfile.getCurrentUser();

        buttonSavePw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String curPw = editTextCurPassword.getText().toString();
                String newPw = editTextNewPassword.getText().toString();
                String confirmPw = editTextConfirmPassword.getText().toString();

                if(TextUtils.isEmpty(newPw)) {
                    Toast.makeText(getActivity(), "Please enter new password", Toast.LENGTH_SHORT).show();
                    editTextNewPassword.setError("New password is required");
                    editTextNewPassword.requestFocus();
                }else if(TextUtils.isEmpty(curPw)) {
                    Toast.makeText(getActivity(), "Please enter current password", Toast.LENGTH_SHORT).show();
                    editTextCurPassword.setError("Current password is required");
                    editTextCurPassword.requestFocus();
                }else if(TextUtils.isEmpty(confirmPw)) {
                    Toast.makeText(getActivity(), "Please confirm password", Toast.LENGTH_SHORT).show();
                    editTextConfirmPassword.setError("Confirm password is required");
                    editTextConfirmPassword.requestFocus();
                }else{

                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), curPw);
                    firebaseUser.reauthenticate(credential)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Reauthentication successful, update password
                                    if (newPw.equals(confirmPw)) {
                                        firebaseUser.updatePassword(newPw)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(getActivity(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                                                        editPasswordDialog.dismiss();
                                                        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                                                        transaction.replace(R.id.fragment_container, new ProfileFragment()).commit();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getActivity(), "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(getActivity(), "Password not match", Toast.LENGTH_SHORT).show();
                                        editTextConfirmPassword.setError("Password not match");
                                        editTextConfirmPassword.requestFocus();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), "Reauthentication failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }
        });

        editPasswordDialog = builder.create();
        editPasswordDialog.show();
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Profile");

        // Inflate the layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.edit_profile_dialog, null);
        builder.setView(dialogView);

        // Find the TextViews representing the options
        TextView changeUsernameOption = dialogView.findViewById(R.id.changeUsernameOption);
        TextView changeProfilePictureOption = dialogView.findViewById(R.id.changeProfilePictureOption);

        changeUsernameOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProfileDialog.dismiss();
                showEditUsernameDialog();
            }
        });

        changeProfilePictureOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (getActivity(), UploadProfilePicActivity.class);
                startActivity(intent);
            }
        });

        editProfileDialog = builder.create();
        editProfileDialog.show();
    }

    private void showEditUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Profile");

        // Inflate the layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.edit_username_dialog, null);
        builder.setView(dialogView);
        builder.setCancelable(true);

        editTextUsername = dialogView.findViewById(R.id.editTextUsername);
        buttonSave = dialogView.findViewById(R.id.buttonSave);
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser =authProfile.getCurrentUser();


        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUsername = editTextUsername.getText().toString();
                String email = firebaseUser.getEmail();

                // Get FCM token
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                String fcmToken = task.getResult();

                                //Storing info to Real-time database
                                ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(newUsername, email, fcmToken);

                                DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
                                referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            editUsernameDialog.dismiss();
                                            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                                            transaction.replace(R.id.fragment_container, new ProfileFragment()).commit();

                                        }else {
                                            Toast.makeText(getActivity(), "Failed to update username", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                Log.e(TAG, "Failed to get FCM token", task.getException());
                                Toast.makeText(getActivity(), "Failed to update username", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });


        editUsernameDialog = builder.create();
        editUsernameDialog.show();
    }

    //For user right after registration
    private void checkIfEmailVerified(FirebaseUser firebaseUser) {
        if(!firebaseUser.isEmailVerified()){
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email now. You cannot login without email verification.");

        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //to email app in new window
                startActivity(intent);
            }
        });
        AlertDialog alertDialog =  builder.create();
        alertDialog.show();
    }

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();

        //Extracting user reference from database for "Registered Users"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if(readUserDetails != null){
                    username = readUserDetails.username;
                    email = firebaseUser.getEmail();

                    profileEmail.setText(email);
                    profileUsername.setText(username);

                    //Set user profile pic
                    showProfilePic();
                }else{
                    Toast.makeText(getActivity(), "Something went wrong!", Toast.LENGTH_LONG).show();
                }
            }
            private void showProfilePic(){
                // Retrieve the current user's ID
                String currentUserId = firebaseUser.getUid();
                // Retrieve the reference to the profile picture in Firebase Storage
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference profilePictureRef = storage.getReference().child("DisplayPics/").child(currentUserId + ".jpg");

                // Get the download URL of the profile picture
                profilePictureRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Load the profile picture using Picasso with circular transformation

                        Picasso.get()
                                .load(uri)
                                .transform(new CircleTransform()) // Apply circular transformation
                                .into(profileIV);
                        Picasso.get()
                                .load(uri)
                                .transform(new CircleTransform()) // Apply circular transformation
                                .into(profileThumbnail);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        Log.e("ProjectListFragment", "Failed to retrieve profile picture: " + exception.getMessage());
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Something went wrong!", Toast.LENGTH_LONG).show();
            }
        });
    }

}