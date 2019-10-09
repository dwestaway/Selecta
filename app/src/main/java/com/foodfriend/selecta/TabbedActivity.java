package com.foodfriend.selecta;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.foodfriend.selecta.AccountActivity.LoginActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.foodfriend.selecta.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class TabbedActivity extends AppCompatActivity {


    private SectionsPagerAdapter mSectionsPagerAdapter;


    public static ViewPager mViewPager;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference database;
    private DatabaseReference ref;

    private final static int GalleryPick = 1;
    private StorageReference storageReference;

    private String currentUserID;

    private FusedLocationProviderClient mFusedLocationClient;

    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        //get auth instance and current users id
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();

        user = FirebaseAuth.getInstance().getCurrentUser();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //get database reference
        database = FirebaseDatabase.getInstance().getReference();
        ref = database.child("users").child(currentUserID);

        //Create a storage reference for profile images
        storageReference = FirebaseStorage.getInstance().getReference().child("ProfileImages");

        //Initialize ads
        MobileAds.initialize(this,"ca-app-pub-3235243213995142~8206055010");

        //find view and add an advert to the adView
        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //Create the tab layout (fragment tabs)
        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        //Create tabs
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        sendLocation();
    }

    //Inflate the options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_tabbed,menu);

        return super.onCreateOptionsMenu(menu);
    }

    //Drop down options menu items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //If signout button is clicked
        if(item.getItemId() == R.id.sign_out)
        {
            FirebaseAuth.getInstance().signOut();

            Toast.makeText(getApplicationContext(), "User signed out", Toast.LENGTH_SHORT).show();


            finish();
        }
        //If change profile image button is clicked
        if(item.getItemId() == R.id.changeImage)
        {
            //get intent for the gallery
            Intent galleryIntent = new Intent();

            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*"); //set gallery to only be images

            startActivityForResult(galleryIntent, GalleryPick); //Open gallery

            Toast.makeText(getApplicationContext(), "Choose your image", Toast.LENGTH_SHORT).show();

        }
        if(item.getItemId() == R.id.help)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(TabbedActivity.this);
            View view = getLayoutInflater().inflate(R.layout.dialog_help, null);

            Button confirmButton = view.findViewById(R.id.confirmButton);


            //Create dialog
            builder.setView(view);
            final AlertDialog dialog = builder.create();
            dialog.show();

            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
        }
        //If change password button is clicked
        if(item.getItemId() == R.id.changePassword)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(TabbedActivity.this);
            View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);

            final EditText password1 = view.findViewById(R.id.password);
            final EditText password2 = view.findViewById(R.id.repeatPassword);
            final EditText currentPassword = view.findViewById(R.id.oldPassword);
            Button confirmButton = view.findViewById(R.id.confirmButton);



            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //if password fields are not empty
                    if(!password1.getText().toString().isEmpty() && !password2.getText().toString().isEmpty())
                    {
                        //if passwords are the same
                        if(password1.getText().toString().trim().equals(password2.getText().toString().trim()))
                        {
                            //if password is at least 6 characters
                            if(password1.getText().toString().trim().length() > 5)
                            {
                                String email = user.getEmail();
                                String oldPassword = currentPassword.getText().toString().trim();
                                //get users current credentials
                                AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);
                                //reauthenticate user with current credentials
                                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            //Update password
                                            user.updatePassword(password2.getText().toString().trim())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(TabbedActivity.this, "Password updated, please sign in with new password", Toast.LENGTH_SHORT).show();
                                                                auth.signOut();
                                                            } else {
                                                                Toast.makeText(TabbedActivity.this, "Failed to update password!", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });


                            }
                            else
                            {
                                Toast.makeText(TabbedActivity.this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            Toast.makeText(TabbedActivity.this, "Passwords are not the same.", Toast.LENGTH_SHORT).show();
                        }

                    }
                    else
                    {
                        Toast.makeText(TabbedActivity.this, "Password fields mut not be empty", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //Create dialog
            builder.setView(view);
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GalleryPick && resultCode == RESULT_OK && data != null)
        {

            Uri imageUri = data.getData();

            //Launch crop activity, set aspect ratio to 1:1 so image is square
            CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                Uri resultUri = result.getUri();

                //Save image with current users id as the name, then check if image is successfully uploaded
                StorageReference path = storageReference.child(currentUserID + ".jpg");
                path.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(TabbedActivity.this, "Saving profile image...", Toast.LENGTH_SHORT).show();

                            //get the image url and save it in the users data in database
                            String downloadUrl = task.getResult().getDownloadUrl().toString();
                            ref.child("profileImage").setValue(downloadUrl);

                        }
                        else
                        {
                            Toast.makeText(TabbedActivity.this, "Error occurred uploading profile image.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }
    }

    //Code for tabs using Pager Adapter, allows user to click between tabs
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    Tab1Profile tab1 = new Tab1Profile();
                    return tab1;
                case 1:
                    Tab2Matches tab2 = new Tab2Matches();
                    return tab2;
                case 2:
                    Tab3Messenger tab3 = new Tab3Messenger();
                    return tab3;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

    }

    void sendLocation()
    {


        //test this
        //get GPS location
        /*GPSLocation gps = new GPSLocation(getApplication());
        Location location = gps.getLocation();

        if(location != null)
        {

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();


            database.child("users").child(user.getUid()).child("longitude").setValue(longitude);
            database.child("users").child(user.getUid()).child("latitude").setValue(latitude);
        }

        */


        //check for location permission, if false then ask user for it
        if(ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //Toast.makeText(TabbedActivity.this, "Please enable GPS", Toast.LENGTH_LONG).show();

            ActivityCompat.requestPermissions(this, new String[]{ACCESS_COARSE_LOCATION}, 1);
        }

            //get location using gps/wifi
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object

                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                database.child("users").child(user.getUid()).child("longitude").setValue(longitude);
                                database.child("users").child(user.getUid()).child("latitude").setValue(latitude);
                            }
                            else if(location == null)
                            {
                                Toast.makeText(TabbedActivity.this, "Location error, check GPS is enabled", Toast.LENGTH_LONG).show();
                            }
                        }
                    });




    }

    // this listener will be called when there is change in firebase user session
    FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            //if user is signed out
            if (user == null) {

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);

                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);

                finish();
            }

        }

    };

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
}
