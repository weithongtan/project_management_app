package utar.edu.project_management_app;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class UploadProfilePicActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri uriImage;
    private ImageView backBtn, imageViewUploadPic;
    private FirebaseAuth authProfile;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile_pic);

        backBtn = findViewById(R.id.btn_back);
        backBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
            onBackPressed();
            }
        });

        Button chooseProfilePic = findViewById(R.id.upload_pic_choose_button);
        Button uploadProfilePic = findViewById(R.id.upload_pic_button);
        imageViewUploadPic = findViewById(R.id.imageView_profile_dp);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("DisplayPics");
        Uri uri = firebaseUser.getPhotoUrl();
        //Set User's current DP in ImageView (if uploaded)
        Picasso.get().load(uri).into(imageViewUploadPic);
        //Choosing image to upload
        chooseProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
        //Upload image to database
        uploadProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadPic();
            }
        });

    }

    private void UploadPic() {
        if(uriImage!= null){
            //save image with uid
            StorageReference fileReference =  storageReference.child(authProfile.getUid() + "." + getFileExtension(uriImage));

            //upload image to storage
            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri = uriImage;
                            firebaseUser=authProfile.getCurrentUser();

                            //set the display image
                            UserProfileChangeRequest profileUpdates =  new UserProfileChangeRequest.Builder().setPhotoUri(downloadUri).build();
                            firebaseUser.updateProfile(profileUpdates);
                        }
                    });
                    Toast.makeText(UploadProfilePicActivity.this, "Upload Successfully", Toast.LENGTH_SHORT).show();
                    // Finish the activity and navigate to the ProfileFragment
                    finishActivityAndNavigateToProfileFragment();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadProfilePicActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(UploadProfilePicActivity.this, "No file is selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void finishActivityAndNavigateToProfileFragment() {
        // Navigate to MainActivity with flag indicating to go to ProfileFragment
        Intent intent = new Intent(UploadProfilePicActivity.this, MainActivity.class);
        intent.putExtra("fragmentToLoad", "profile");
        startActivity(intent);
        finish(); // Finish the UploadProfilePicActivity
    }
    private String getFileExtension(Uri uriImage) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uriImage));
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            uriImage = data.getData();
            // Grant persistable URI permission
            getContentResolver().takePersistableUriPermission(uriImage, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //imageViewUploadPic.setImageURI(uriImage);
            try {
                // Decode the image file into a Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriImage);

                // Resize the Bitmap to your desired dimensions
                int targetWidth = 200; // Set your desired width
                int targetHeight = 200; // Set your desired height
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false);

                // Set the resized Bitmap to your ImageView
                imageViewUploadPic.setImageBitmap(resizedBitmap);

            } catch (IOException e) {
                e.printStackTrace();
                // Handle error
            }
        }
    }
}