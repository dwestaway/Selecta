package com.foodfriend.selecta;

/**
 * Created by Dan on 21/02/2018.
 */
import android.*;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.foodfriend.selecta.AccountActivity.LoginActivity;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.app.Activity.RESULT_OK;

public class Tab1Profile extends Fragment {

    private Button search, placePicker, dateButton;
    private TextView name;
    private ImageView profileImg;

    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    private FirebaseUser user;

    private DatePickerDialog.OnDateSetListener mDateSetListener;


    private Spinner spinner;
    ArrayAdapter<CharSequence> adapter;

    //EditText that autocompletes
    private AutoCompleteTextView autoComplete;
    private ArrayAdapter<String> foodAdapter;

    private FusedLocationProviderClient mFusedLocationClient;

    private final static int PLACE_PICKER_REQUEST = 1;

    String dateChosen = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        return inflater.inflate(R.layout.fragment_tab1profile, container, false);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {


        super.onActivityCreated(savedInstanceState);

        //Prevent activity from starting with the keyboard open
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //Get the Firebase authenticatpr
        auth = FirebaseAuth.getInstance();
        //Get the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();


        //If user is null, go back to login screen
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        //sendLocation();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users");

        //Get reference to all components in the activitys layout
        name = getView().findViewById(R.id.userName);
        search = getView().findViewById(R.id.searchButton);
        profileImg = getView().findViewById(R.id.profileImage);
        //placePicker = getView().findViewById(R.id.placeButton);
        //dateButton = getView().findViewById(R.id.dateButton);


        //get current user
        user = FirebaseAuth.getInstance().getCurrentUser();


        setupAutoComplete();

        //create drop down menu for match options
        spinner = getView().findViewById(R.id.spinner);
        adapter = ArrayAdapter.createFromResource(getActivity(),R.array.Matches,R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //create drop down menu for gender options
        spinner = getView().findViewById(R.id.spinnerGender);
        adapter = ArrayAdapter.createFromResource(getActivity(),R.array.Gender,R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        //Find food friend button click
        search.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {

                //get users chosen place of interest
                String poi = autoComplete.getText().toString().trim();
                //get users chosen time to eat
                String time = spinner.getSelectedItem().toString();

                //If food poi field is empty, alert the user
                if (TextUtils.isEmpty(poi))
                {
                    Toast.makeText(getActivity(), "Please choose where you would like to eat", Toast.LENGTH_SHORT).show();
                    return;
                }
                //If a time is not chosen, alert the user
                if (time.equals("Choose a Time to Eat:"))
                {
                    Toast.makeText(getActivity(), "Please choose a time to eat", Toast.LENGTH_SHORT).show();
                    return;
                }
                //If date is not chosen, alert user
                if (dateChosen.equals(""))
                {
                    Toast.makeText(getActivity(), "Please choose a date", Toast.LENGTH_SHORT).show();
                    return;
                }

                //send users GPS location to database
                sendLocation();

                //set database values: food place of interest and time
                mDatabase.child("users").child(user.getUid()).child("foodPOI").setValue(poi);
                mDatabase.child("users").child(user.getUid()).child("time").setValue(time);

                //String date = getDate();
                String date = dateChosen;

                //send todays date to user data on server database
                mDatabase.child("users").child(user.getUid()).child("date").setValue(date);

                Snackbar.make(getActivity().findViewById(android.R.id.content), "Tap a Matched User to Start a Chat", Snackbar.LENGTH_LONG).show();

                //Change to matches tab
                TabbedActivity.mViewPager.setCurrentItem(1);
            }
        });
        /*
        //Map button click
        placePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                Intent intent;

                try {
                    intent = builder.build(getActivity());
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
                }
                catch (GooglePlayServicesRepairableException e)
                {
                    e.printStackTrace();
                }
                catch (GooglePlayServicesNotAvailableException e)
                {
                    e.printStackTrace();
                }

            }
        });
        */
        /*
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar cal = Calendar.getInstance();

                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(getContext(), android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener,year,month,day);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        */

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                month = month + 1;

                dateChosen = day + "/" + month + "/" + year;

                dateButton.setText(dateChosen);

            }
        };



        //Load data from database from page load
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //get user id
                String uid = user.getUid();
                //get users name
                String firstName = (String) dataSnapshot.child(uid).child("name").getValue();
                //get user profile image
                String imageUrl = (String) dataSnapshot.child(uid).child("profileImage").getValue();

                //set user name text
                name.setText(firstName);


                //Check if user has uploaded image
                if(!TextUtils.isEmpty(imageUrl))
                {
                    //Load image into imageView, use placeholder image before user image is loaded
                    Picasso.with(getActivity()).load(imageUrl).placeholder(R.drawable.profileimage).into(profileImg);
                }
                else if (TextUtils.isEmpty(imageUrl))
                {
                    //Use placeholder image if user has no profile image
                    Picasso.with(getActivity()).load(R.drawable.profileimage).into(profileImg);
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Toast.makeText(getActivity(), "Database Error", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //set up auto complete EditText box using an adapter and array of strings
    private void setupAutoComplete()
    {
        //Get array of common food places of interest
        String[] foodPOIarray = getResources().getStringArray(R.array.foodPOI);

        //Create adapter to use a list as autocomplete for the text box
        foodAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice, foodPOIarray);


        autoComplete = getView().findViewById(R.id.foodChoice);
        autoComplete.setAdapter(foodAdapter);
        autoComplete.setThreshold(1);

        //When user clicks outside of the keyboard/auto complete text view, hide the keyboard
        autoComplete.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                if(!focused)
                {
                    hideKeyboard(view);
                }
            }
        });
    }

    //send users current location to the server
    public void sendLocation()
    {
        /*
        GPSLocation gps = new GPSLocation(getContext());
        Location location = gps.getLocation();

        if(location != null) {

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            //send users location to database
            mDatabase.child("users").child(user.getUid()).child("longitude").setValue(longitude);
            mDatabase.child("users").child(user.getUid()).child("latitude").setValue(latitude);
        }
        */

        //check for location permission, if false then ask user for it
        if(ContextCompat.checkSelfPermission(getActivity(), ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //Toast.makeText(TabbedActivity.this, "Please enable GPS", Toast.LENGTH_LONG).show();

            ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_COARSE_LOCATION}, 1);
        }

        //get location using gps/wifi
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object

                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            //send users location to database
                            mDatabase.child("users").child(user.getUid()).child("longitude").setValue(longitude);
                            mDatabase.child("users").child(user.getUid()).child("latitude").setValue(latitude);
                        }

                    }
                });

    }

    //result of user using place picker activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == PLACE_PICKER_REQUEST)
        {
            if(resultCode == RESULT_OK)
            {
                Place place = PlacePicker.getPlace(getActivity(), data);

                String placeName = String.format("%s", place.getName());

                //get place type chosen
                for(int i : place.getPlaceTypes())
                {
                    //if place type is one of the following
                    if(i == place.TYPE_FOOD || i == place.TYPE_RESTAURANT || i == place.TYPE_MEAL_TAKEAWAY || i == place.TYPE_MEAL_DELIVERY || i == place.TYPE_SHOPPING_MALL)
                    {
                        autoComplete.setText(placeName);
                    }
                }

            }
        }
    }

    //Hide the soft keyboard method
    public void hideKeyboard(View view)
    {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);

        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public String getDate()
    {
        //get current date and send to database
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String currentDate = sdf.format(today);

        return currentDate;
    }






}
