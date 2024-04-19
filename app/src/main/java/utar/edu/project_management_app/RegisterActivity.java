package utar.edu.project_management_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {
    private EditText signupUsername, signupEmail, signupPassword, confirmPw;
    private TextView loginRedirectText, registerTV;
    private static final String TAG = "RegisterActivity";
    FirebaseDatabase database;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        signupEmail = findViewById(R.id.email_input);
        signupUsername = findViewById(R.id.username_input);
        signupPassword = findViewById(R.id.password_input);
        confirmPw = findViewById(R.id.confirmPw);
        loginRedirectText = findViewById(R.id.loginTV);
        registerTV = findViewById(R.id.registerBtn);

        registerTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                String email = signupEmail.getText().toString();
                String username = signupUsername.getText().toString();
                String password = signupPassword.getText().toString();
                String confirmPassword = confirmPw.getText().toString();

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(RegisterActivity.this, "Please enter email", Toast.LENGTH_SHORT).show();
                    signupEmail.setError("Email is required");
                    signupEmail.requestFocus();
                }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Toast.makeText(RegisterActivity.this, "Please enter valid email", Toast.LENGTH_SHORT).show();
                    signupEmail.setError("Valid email is required");
                    signupEmail.requestFocus();
                } else if(TextUtils.isEmpty(username)){
                    Toast.makeText(RegisterActivity.this, "Please enter username", Toast.LENGTH_SHORT).show();
                    signupUsername.setError("Username is required");
                    signupUsername.requestFocus();
                }else if(TextUtils.isEmpty(password)){
                    Toast.makeText(RegisterActivity.this, "Please enter username", Toast.LENGTH_SHORT).show();
                    signupPassword.setError("Password is required");
                    signupPassword.requestFocus();
                }else if(password.length()<6){
                    Toast.makeText(RegisterActivity.this, "Password should at least 6 characters", Toast.LENGTH_SHORT).show();
                    signupPassword.setError("Password is weak");
                    signupPassword.requestFocus();
                }else if(TextUtils.isEmpty(confirmPassword)){
                    Toast.makeText(RegisterActivity.this, "Please enter username", Toast.LENGTH_SHORT).show();
                    confirmPw.setError("Password is required");
                    confirmPw.requestFocus();
                }else if(!password.equals(confirmPassword)){
                    Toast.makeText(RegisterActivity.this, "Please enter same password", Toast.LENGTH_SHORT).show();
                    confirmPw.setError("Password is not same");
                    confirmPw.requestFocus();
                    confirmPw.clearComposingText();
                    signupPassword.clearComposingText();
                }else{
                    registerUser(username, email, password);
                }
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    private void registerUser(String username, String email, String password){
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Get FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String fcmToken = task.getResult();

                        // Create User Profile
                        auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {

                                            FirebaseUser firebaseuser = auth.getCurrentUser();

                                            // Storing info to Real-time database
                                            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(username, email, fcmToken);

                                            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
                                            referenceProfile.child(firebaseuser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        // Send verification email
                                                        firebaseuser.sendEmailVerification();
                                                        Toast.makeText(RegisterActivity.this, "Register Successfully. Please verify your email.", Toast.LENGTH_LONG).show();
                                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                        // To prevent returning to registration when back button pressed
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        Toast.makeText(RegisterActivity.this, "Register Failed. Please try again.", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });

                                        } else {
                                            try {
                                                throw task.getException();
                                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                                signupEmail.setError("Your email is invalid or already in use");
                                                signupEmail.requestFocus();
                                            } catch (FirebaseAuthUserCollisionException e) {
                                                signupEmail.setError("Your email is already registered");
                                                signupEmail.requestFocus();
                                            } catch (Exception e) {
                                                Log.e(TAG, e.getMessage());
                                                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }
                                });
                    } else {
                        Log.e(TAG, "Failed to get FCM token", task.getException());
                        Toast.makeText(RegisterActivity.this, "Failed to get FCM token", Toast.LENGTH_LONG).show();
                    }
                });
    }

}