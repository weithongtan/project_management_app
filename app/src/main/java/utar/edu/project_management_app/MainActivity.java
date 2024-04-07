package utar.edu.project_management_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth mAuth;
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        authProfile = FirebaseAuth.getInstance();
//        if(authProfile.getCurrentUser() != null){
//            Toast.makeText(MainActivity.this, "Already logged in", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(MainActivity.this, MainActivity.class));
//            finish();
//        }
//        else{
//            Toast.makeText(MainActivity.this, "You can login now", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(MainActivity.this, LoginActivity.class));
//            finish();
//        }



        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // as soon as the application opens the first
        // fragment should be shown to the user
        // Check if the intent contains a flag to load the ProfileFragment
        String fragmentToLoad = getIntent().getStringExtra("fragmentToLoad");
        if (fragmentToLoad != null && fragmentToLoad.equals("profile")) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProjectListFragment()).commit();
        }



    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        // By using switch we can easily get
        // the selected fragment
        // by using there id.
        Fragment selectedFragment = null;
        int itemId = item.getItemId();
        if (itemId == R.id.home) {
            selectedFragment = new ProjectListFragment();
        } else if (itemId == R.id.mytask) {
            selectedFragment = new MyTasksFragment();
        } else if (itemId == R.id.profile) {
            selectedFragment = new ProfileFragment();
        }
        // It will help to replace the
        // one fragment to other.
        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }
        return true;
    };

//    @Override
//    protected void onStart(){
//        super.onStart();
//        if(authProfile.getCurrentUser() != null){
//            Toast.makeText(MainActivity.this, "Already logged in", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(MainActivity.this, MainActivity.class));
//            finish();
//        }
//        else{
//            Toast.makeText(MainActivity.this, "You can login now", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(MainActivity.this, LoginActivity.class));
//            finish();
//        }
//    }
}
