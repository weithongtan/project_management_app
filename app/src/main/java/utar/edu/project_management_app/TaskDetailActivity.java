package utar.edu.project_management_app;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utar.edu.project_management_app.model.Task;


public class TaskDetailActivity extends AppCompatActivity {

    Task clickedTask;
    LinearLayout btn_back,memberItem;
    TextView tv_taskname,tv_creationDate, tv_dueDate, tv_description,edit_duedate, btn_edit_description;
    Spinner priority, section;
    EditText et_description;
    ImageView  delete, invite_member;
    List<String> projectEmails;
    List<String> newInvitedEmails;
    String currentEmail;
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_task_detail);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentEmail = currentUser.getEmail();
        btn_back = findViewById(R.id.btn_back);

        clickedTask = (Task) getIntent().getSerializableExtra("clickedTask");

        String[] projectEmailsArray = getIntent().getStringArrayExtra("ProjectMembersEmail");
        projectEmails = Arrays.asList(projectEmailsArray);

        delete = findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();

            }
        });

        edit_duedate = findViewById(R.id.edit_duedate);
        edit_duedate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDatePickerDialog();
            }
        });

        btn_edit_description = findViewById(R.id.btn_edit_description);
        tv_description = findViewById(R.id.tv_description);
        et_description = findViewById(R.id.et_description);
        btn_edit_description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (btn_edit_description.getText().toString() == "Edit"){
                    btn_edit_description.setText("Done");
                    tv_description.setVisibility(View.GONE);
                    et_description.setVisibility(View.VISIBLE);
                    et_description.setText(tv_description.getText());
                    et_description.requestFocus();

                }else{
                    btn_edit_description.setText("Edit");
                    tv_description.setText(et_description.getText());
                    tv_description.setVisibility(View.VISIBLE);
                    et_description.setVisibility(View.GONE);
                }
//                isChanged = true;
            }
        });
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Set values on the clickedTask
                clickedTask.setDueDate(tv_dueDate.getText().toString());
                clickedTask.setPriority(priority.getSelectedItem().toString());
                clickedTask.setSection(section.getSelectedItem().toString());
                clickedTask.setDescription(tv_description.getText().toString());

                // Prepare database updates
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/task/" + clickedTask.getTaskId(), clickedTask);

                // Perform the updates asynchronously
                DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                database.updateChildren(childUpdates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // All updates completed successfully
                        // Handle the newInvitedEmails update
                        if (newInvitedEmails != null) {
                            DatabaseReference usersRef = database.child("Registered Users");
                            String taskID = clickedTask.getTaskId();

                            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                        String userEmail = userSnapshot.child("email").getValue(String.class);
                                        if (newInvitedEmails.contains(userEmail)) {
                                            DatabaseReference userTaskRef = userSnapshot.getRef().child("taskId").child(taskID);
                                            userTaskRef.setValue(true);
                                        }
                                    }
                                    SendNotification.getTokens(database, newInvitedEmails, tokens -> {
                                        // This block will execute once all tokens are ready
                                        for (String token : tokens) {
                                            // Send notification using the token
                                            SendNotification.sendNotification(token, currentEmail, clickedTask.getTaskName(), "task");
                                        }
                                    });
                                    // Once all asynchronous operations are complete, finish the activity
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("Firebase", "Error fetching user data", error.toException());
                                    // Consider error handling here
                                }
                            });
                        } else {
                            // If newInvitedEmails is null, you can finish the activity directly
                            finish();
                        }
                    } else {
                        // Handle errors in the database update here
                        Log.e("Firebase", "Error updating database", task.getException());
                    }
                });
            }
        });

        invite_member = findViewById(R.id.addMember);
        invite_member.setOnClickListener(view -> inviteMember());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh UI components that display task details
        updateTaskDetailsUI();
    }

    private void updateTaskDetailsUI() {
        // Assuming all data fields are updated, refresh the UI components:
        fillDetail();  // Re-populate details to reflect any changes made
    }

    private void fillDetail() {
        tv_taskname = findViewById(R.id.tv_task_name);
        tv_description = findViewById(R.id.tv_description);
        tv_dueDate = findViewById(R.id.tv_duedate);
        tv_creationDate = findViewById(R.id.tv_datecreation);
        et_description =findViewById(R.id.et_description);
        memberItem = findViewById(R.id.memberItem);


        // Set the due date from the clickedTask object
        String date_creation = clickedTask.getTimeCreation();


        // change view
        section = findViewById(R.id.spinner_section);

        // add drop down item
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.section, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        section.setAdapter(adapter);

        int selectionSectionPosition = adapter.getPosition(clickedTask.getSection());
        section.setSelection(selectionSectionPosition);

        priority = findViewById(R.id.spinner_priority);
        // add drop down item
        ArrayAdapter<CharSequence> priority_adapter = ArrayAdapter.createFromResource(this,
                R.array.priority, android.R.layout.simple_spinner_item);
        priority_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priority.setAdapter(priority_adapter);

        int selectedPriorityPosition = priority_adapter.getPosition(clickedTask.getPriority());
        priority.setSelection(selectedPriorityPosition);

        System.out.println(clickedTask.getUserEmails().toString());
        LinearLayout emailsContainer = findViewById(R.id.memberItem);
        emailsContainer.removeAllViews();
        for(int i = 0;i<clickedTask.getUserEmails().size(); i++){
            addEmailView(emailsContainer, clickedTask.getUserEmails().get(i));
        }


        // Set the text views with the task details
        tv_taskname.setText(clickedTask.getTaskName());
        tv_creationDate.setText(date_creation);
        tv_dueDate.setText(clickedTask.getDueDate());
        tv_description.setText(clickedTask.getDescription());
        et_description.setText(clickedTask.getDescription());
    }
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DatabaseReference taskRef = database.child("task").child(clickedTask.getTaskId());
                        taskRef.removeValue();
                        DatabaseReference projectTaskRef = database.child("projects").child(clickedTask.getProjectId()).child("taskId").child(clickedTask.getTaskId());
                        projectTaskRef.removeValue();

                        removeTaskIdFromUsers();

                        DatabaseReference taskCounterRef = database.child("taskCounter");


                        taskCounterRef.runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                Integer currentValue = mutableData.getValue(Integer.class);
                                if (currentValue != null && currentValue > 0) {
                                    mutableData.setValue(currentValue - 1);
                                }
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                                if (committed) {
                                    Log.d("TaskDetailActivity", "Task counter decremented successfully");
                                } else {
                                    Log.e("TaskDetailActivity", "Failed to decrement task counter", databaseError.toException());
                                }
                            }
                        });
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                    }
                });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void showDatePickerDialog() {
        // Get current date to set as default in DatePickerDialog
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        tv_dueDate = findViewById(R.id.tv_duedate);

        // Create DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Update TextView with selected date
                        String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        tv_dueDate.setText(date);
                        tv_dueDate.setTextColor(Color.BLACK);
//                        isChanged = true;
                    }
                }, year, month, dayOfMonth);


        // Show DatePickerDialog
        datePickerDialog.show();
    }
    private void inviteMember() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_invite_member, null);
        dialogBuilder.setView(dialogView);


        EditText editText = dialogView.findViewById(R.id.editTextEmail);

        AutoCompleteTextView autoCompleteTextView = new AutoCompleteTextView(this);

        // Copy properties from the EditText to the AutoCompleteTextView
        autoCompleteTextView.setId(editText.getId());
        autoCompleteTextView.setLayoutParams(editText.getLayoutParams());
        autoCompleteTextView.setHint(editText.getHint());
        autoCompleteTextView.setImeOptions(editText.getImeOptions());
        autoCompleteTextView.setInputType(editText.getInputType());
        autoCompleteTextView.setText(editText.getText());
        autoCompleteTextView.setSelection(editText.getSelectionStart(), editText.getSelectionEnd());

        List<String> availableEmails = new ArrayList<>();

        for (String availableEmail: projectEmails
             ) {
            if (!clickedTask.getUserEmails().contains(availableEmail)){
                availableEmails.add(availableEmail);
            }
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, availableEmails);
        autoCompleteTextView.setAdapter(adapter);

        // Replace the EditText with the AutoCompleteTextView in the parent view
        ViewGroup parentView = (ViewGroup) editText.getParent();
        int index = parentView.indexOfChild(editText);
        parentView.removeView(editText);
        parentView.addView(autoCompleteTextView, index);

        LinearLayout emailsContainer = dialogView.findViewById(R.id.emailsContainer);
        Button buttonAdd = dialogView.findViewById(R.id.addButton);
        Button buttonDone = dialogView.findViewById(R.id.buttonDone);

        for (String invitedEmail:clickedTask.getUserEmails()
             ) {
            addEmailView(emailsContainer,invitedEmail);
        }

        newInvitedEmails = new ArrayList<>();
        buttonAdd.setOnClickListener(view -> {
            String email = autoCompleteTextView.getText().toString().trim();
            if (!email.isEmpty()) {
                List<String> userId = clickedTask.getUserEmails();
                userId.add(userId.size(), email);
                clickedTask.setUserEmails(userId);
                newInvitedEmails.add(email);

                addEmailView(emailsContainer, email);
                autoCompleteTextView.setText(""); // Clear input field after adding
//                isChanged = true;
            } else {
                autoCompleteTextView.setError("Email cannot be empty");
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        buttonDone.setOnClickListener(view -> {
            // Assuming updates to clickedTask are made and persisted somewhere in the above code
            alertDialog.dismiss();  // Dismiss the dialog

            // Trigger a refresh or update if necessary (see Step 2 for how this can be handled)
            onResume();   // Indicate that changes were made if returning from this activity
        });
        alertDialog.show();
    }


    private void addEmailView(LinearLayout container, String email) {
        View emailView = LayoutInflater.from(this).inflate(R.layout.email_item, null);
        TextView emailText = emailView.findViewById(R.id.emailText);
        TextView deleteButton = emailView.findViewById(R.id.deleteButton);

        if (email.equals(currentEmail)){
            deleteButton.setVisibility(View.GONE);
        }

        emailText.setText(email);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                container.removeView(emailView);
                List<String> userId = clickedTask.getUserEmails();
                if (userId != null && userId.contains(email)) {
                    userId.remove(email);
                    clickedTask.setUserEmails(userId);
//                    isChanged = true;
                }
                // Ensure this list is also properly managed

            }
        });


        container.addView(emailView);
    }
    public void removeTaskIdFromUsers() {
        List<String> selectedTaskUserEmails = clickedTask.getUserEmails(); // List of emails to check against
        String taskID = clickedTask.getTaskId(); // Task ID to remove

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Registered Users");

        // Listen for data in "Registered Users"
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userEmail = userSnapshot.child("email").getValue(String.class); // Assuming there is an "email" field under each user

                    if (selectedTaskUserEmails.contains(userEmail)) {
                        // This user's email is in the list of selected task user emails
                        DatabaseReference userTaskRef = userSnapshot.getRef().child("taskId").child(taskID);

                        // Remove the taskId from this user's list
                        userTaskRef.removeValue().addOnSuccessListener(aVoid -> {
                            Log.d("Firebase", "Task ID removed successfully for user with email: " + userEmail);
                        }).addOnFailureListener(e -> {
                            Log.e("Firebase", "Failed to remove task ID for user with email: " + userEmail, e);
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching user data", databaseError.toException());
            }
        });
    }

}
