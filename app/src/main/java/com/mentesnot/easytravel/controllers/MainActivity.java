/*************************************************************************************************
 * JANUARY 8, 2018
 * Mentesnot Aboset
 * ************************************************************************************************/
package com.mentesnot.easytravel.controllers;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.mentesnot.easytravel.database.DatabaseHelper;
import com.mentesnot.easytravel.HelperUtils.HelperUtilities;
import com.mentesnot.easytravel.R;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private SharedPreferences sharedPreferences;

    private SQLiteOpenHelper databaseHelper;
    private SQLiteDatabase db;
    private Cursor cursor;
    private Intent intent;

    private int currentTab;

    //date picker dialog
    private DatePickerDialog datePickerDialog1;
    private DatePickerDialog datePickerDialog2;
    private DatePickerDialog datePickerDialog3;

    //current date
    private int year;
    private int month;
    private int day;

    //traveller count
    private int oneWayTravellerCount = 1;
    private int roundTravellerCount = 1;

    //traveller count view
    private TextView numTraveller;

    //add and remove image button controls in the dialog
    private ImageButton imgBtnAdd;
    private ImageButton imgBtnRemove;

    //custom dialog view
    private View dialogLayout;

    //one way UI controls
   //       btnOneWayNumTraveller

    private Button btnTypeCredit;
    private Button btnSousTypeCredit;
    //round trip UI controls


    private Button btnRoundDepartureDatePicker;
    private Button btnRoundReturnDatePicker;
    private Button btnRoundClass;
    private Button btnRoundNumTraveller;

    //search button
    private Button btnSearch;

    private int tempOneWaySelectedClassID = 0;
    private int tempRoundSelectedClassID = 0;
    private String oneWayDepartureDate, roundDepartureDate, roundReturnDate;
    private View header;
    private ImageView imgProfile;
    private int clientID;
    private int tempYear;
    private int tempMonth;
    private int tempDay;

    private boolean isValidOneWayDate = true;
    private boolean isValidRoundDate = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //navigation drawer manager
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        header = navigationView.getHeaderView(0);

        clientID = clientID();

        //tab host manager
        final TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
        tabHost.setup();

        //Tab 1
        TabHost.TabSpec spec = tabHost.newTabSpec("Tab One");
        spec.setContent(R.id.tab1);
        spec.setIndicator("One way");
        tabHost.addTab(spec);

        //Tab 2
//        spec = tabHost.newTabSpec("Tab Two");
//        spec.setContent(R.id.tab2);
//        spec.setIndicator("Round Trip");
//        tabHost.addTab(spec);


        //tab text color
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(getResources().getColor(R.color.colorInverted));
        }


        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                currentTab = tabHost.getCurrentTab();
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, DatabaseHelper.CITIES);

        //one way form



        btnTypeCredit = (Button) findViewById(R.id.btnType);
        btnSousTypeCredit = (Button) findViewById(R.id.btnSousType);


        //round trip form


        btnSearch = (Button) findViewById(R.id.btnSearch);
        imgProfile = (ImageView) header.findViewById(R.id.imgProfile);


        year = HelperUtilities.currentYear();
        month = HelperUtilities.currentMonth();
        day = HelperUtilities.currentDay();

        drawerProfileInfo();
        loadImage(clientID);




        //one way number of travellers on click listener
        btnTypeCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                oneWayNumTravellerDialog().show();
            }
        });
        btnSousTypeCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                oneWayNumTravellerDialog().show();//DARG prevoir 2eme dialogue
            }
        });
        //round trip class selector on click listener
//        btnRoundClass.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                roundClassPickerDialog().show();
//            }
//        });

        // round trip number of traveller on click listener
//        btnRoundNumTraveller.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                roundNumTravellerDialog().show();
//            }
//        });

        //searches available flights on click
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //call search method here

                if (currentTab == 0) {

                    if (isValidOneWayInput() && isValidOneWayDate) {
                        searchOneWayFlight();

                    }

                } else if (currentTab == 1) {

                    if (isValidRoundInput() && isValidRoundDate) {
                        searchRoundFlight();
                    }
                }

            }
        });

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handles navigation view item on clicks
        int id = item.getItemId();

        if (id == R.id.nav_itinerary) {
            Intent intent = new Intent(getApplicationContext(), ItineraryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_security) {
            Intent intent = new Intent(getApplicationContext(), SecurityActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_logout) {

            getApplicationContext().getSharedPreferences(LoginActivity.MY_PREFERENCES, 0).edit().clear().commit();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();

        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //one way class picker dialog
    public Dialog oneWayClassPickerDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final CharSequence[] classList = {"Economy", "Business"}; //temp data, should be retrieved from database


        builder.setTitle("Select Class")
                .setSingleChoiceItems(classList, tempOneWaySelectedClassID, new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        tempOneWaySelectedClassID = id;
                        //get selected class here



                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });




        return builder.create();
    }


    //round class picker dialog
    public Dialog roundClassPickerDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final CharSequence[] classList = {"Economy", "Business"}; //temp data, should be retrieved from database


        builder.setTitle("Select Class")
                .setSingleChoiceItems(classList, tempRoundSelectedClassID, new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        tempRoundSelectedClassID = id;
                        //get selected class here
                        btnRoundClass.setText(classList[id].toString());


                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        btnRoundClass.setText(classList[tempRoundSelectedClassID].toString());


        return builder.create();
    }

    //number of travellers dialog (one way)
    public Dialog oneWayNumTravellerDialog() {


        dialogLayout = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Number of travellers")
                .setView(dialogLayout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //get number of traveller here
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        imgBtnRemove = (ImageButton) dialogLayout.findViewById(R.id.imgBtnRemove);
        imgBtnAdd = (ImageButton) dialogLayout.findViewById(R.id.imgBtnAdd);
        numTraveller = (TextView) dialogLayout.findViewById(R.id.txtNumber);

        imgBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                oneWayTravellerCount++;
                numTraveller.setText(String.valueOf(oneWayTravellerCount));
                btnTypeCredit.setText(String.valueOf(oneWayTravellerCount) + " Credit");
                btnSousTypeCredit.setText(String.valueOf(oneWayTravellerCount) + " S.Credit");
            }
        });

        imgBtnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (oneWayTravellerCount > 1) {
                    oneWayTravellerCount--;
                }
                numTraveller.setText(String.valueOf(oneWayTravellerCount));
                btnTypeCredit.setText(String.valueOf(oneWayTravellerCount) + " Credit");
                btnSousTypeCredit.setText(String.valueOf(oneWayTravellerCount) + " S.Credit");
            }
        });


        numTraveller.setText(String.valueOf(oneWayTravellerCount));

        return builder.create();
    }

    //number of travellers dialog (round trip)
    public Dialog roundNumTravellerDialog() {

        dialogLayout = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Number of travellers")
                .setView(dialogLayout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //get number of traveller here
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        imgBtnRemove = (ImageButton) dialogLayout.findViewById(R.id.imgBtnRemove);
        imgBtnAdd = (ImageButton) dialogLayout.findViewById(R.id.imgBtnAdd);
        numTraveller = (TextView) dialogLayout.findViewById(R.id.txtNumber);

        imgBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                roundTravellerCount++;
                numTraveller.setText(String.valueOf(roundTravellerCount));
                btnRoundNumTraveller.setText(String.valueOf(roundTravellerCount) + " Traveller");
            }
        });

        imgBtnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (roundTravellerCount > 1) {
                    roundTravellerCount--;
                }
                numTraveller.setText(String.valueOf(roundTravellerCount));
                btnRoundNumTraveller.setText(String.valueOf(roundTravellerCount) + " Traveller");
            }
        });

        numTraveller.setText(String.valueOf(roundTravellerCount));

        return builder.create();
    }


    public DatePickerDialog datePickerDialog(int datePickerId) {


        return null;
    }

    public DatePickerDialog.OnDateSetListener getOneWayDepartureDatePickerListener() {
        return new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int startYear, int startMonth, int startDay) {

                //get one way departure date here

                oneWayDepartureDate = startYear + "-" + (startMonth + 1) + "-" + startDay;


            }
        };
    }

    public DatePickerDialog.OnDateSetListener getRoundDepartureDatePickerListener() {
        return new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int startYear, int startMonth, int startDay) {

                tempYear = startYear;
                tempMonth = startMonth;
                tempDay = startDay;

                //get round trip departure date here
                roundDepartureDate = startYear + "-" + (startMonth + 1) + "-" + startDay;
                btnRoundDepartureDatePicker.setText(HelperUtilities.formatDate(startYear, startMonth, startDay));
            }
        };
    }

    public DatePickerDialog.OnDateSetListener getRoundReturnDatePickerListener() {
        return new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int startYear, int startMonth, int startDay) {

                String departureDate = tempYear + "-" + (tempMonth + 1) + "-" + tempDay;
                String returnDate = startYear + "-" + (startMonth + 1) + "-" + startDay;

                if (HelperUtilities.compareDate(departureDate, returnDate)) {
                    datePickerAlert().show();
                    isValidRoundDate = false;
                } else {
                    isValidRoundDate = true;
                    //get round trip return date here
                    roundReturnDate = startYear + "-" + (startMonth + 1) + "-" + startDay;
                    btnRoundReturnDatePicker.setText(HelperUtilities.formatDate(startYear, startMonth, startDay));
                }
            }
        };
    }

    public Dialog datePickerAlert() {
        return new AlertDialog.Builder(this)
                .setMessage("Please select a valid return date. The return date cannot be before the departure date.")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                }).create();
    }

    public Dialog datePickerOneAlert() {
        return new AlertDialog.Builder(this)
                .setMessage("Please select a departure date.")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                }).create();
    }

    public Dialog datePickerTwoAlert() {
        return new AlertDialog.Builder(this)
                .setMessage("Please select a return date.")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                }).create();
    }


    public void searchOneWayFlight() {

        intent = new Intent(getApplicationContext(), OneWayFlightListActivity.class);

        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        getApplicationContext().getSharedPreferences("PREFS", 0).edit().clear().commit();

        SharedPreferences.Editor editor = sharedPreferences.edit();


        editor.putInt("CURRENT_TAB", currentTab);
        //editor.putString("ORIGIN", HelperUtilities.filter(txtOneWayFrom.getText().toString().trim()));
        //editor.putString("DESTINATION", HelperUtilities.filter(txtOneWayTo.getText().toString().trim()));
        editor.putString("DEPARTURE_DATE", oneWayDepartureDate);
       // editor.putString("FLIGHT_CLASS", btnOneWayClass.getText().toString());
        editor.putInt("ONEWAY_NUM_TRAVELLER", oneWayTravellerCount);

        editor.commit();

        startActivity(intent);


    }

    public void searchRoundFlight() {
        intent = new Intent(getApplicationContext(), OutboundFlightListActivity.class);

        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        getApplicationContext().getSharedPreferences("PREFS", 0).edit().clear().commit();
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("CURRENT_TAB", currentTab);
        //editor.putString("ORIGIN", HelperUtilities.filter(txtRoundFrom.getText().toString().trim()));
        //editor.putString("DESTINATION", HelperUtilities.filter(txtRoundTo.getText().toString().trim()));
        editor.putString("DEPARTURE_DATE", roundDepartureDate);
        editor.putString("RETURN_DATE", roundReturnDate);
        //editor.putString("FLIGHT_CLASS", btnOneWayClass.getText().toString());
        editor.putInt("ROUND_NUM_TRAVELLER", roundTravellerCount);


        editor.commit();

        startActivity(intent);
    }

    public void drawerProfileInfo() {
        try {

            TextView profileName = (TextView) header.findViewById(R.id.profileName);
            TextView profileEmail = (TextView) header.findViewById(R.id.profileEmail);


            databaseHelper = new DatabaseHelper(getApplicationContext());
            db = databaseHelper.getReadableDatabase();

            cursor = DatabaseHelper.selectClientJoinAccount(db, clientID);


            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                String fName = cursor.getString(0);
                String lName = cursor.getString(1);
                String email = cursor.getString(4);

                String fullName = fName + " " + lName;

                profileName.setText(fullName);
                profileEmail.setText(email);

            }


        } catch (SQLiteException ex) {
            Toast.makeText(getApplicationContext(), "Database unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    //loads image on create
    public void loadImage(int clientID) {
        try {
            databaseHelper = new DatabaseHelper(getApplicationContext());
            db = databaseHelper.getReadableDatabase();


            cursor = DatabaseHelper.selectImage(db, clientID);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();

                // Create a bitmap from the byte array
                if (cursor.getBlob(0) != null) {
                    byte[] image = cursor.getBlob(0);

                    imgProfile.setImageBitmap(HelperUtilities.decodeSampledBitmapFromByteArray(image, 300, 300));
                }

            }


        } catch (SQLiteException ex) {
            Toast.makeText(getApplicationContext(), "Database unavailable", Toast.LENGTH_SHORT).show();
        }

    }

    public int clientID() {
        LoginActivity.sharedPreferences = getSharedPreferences(LoginActivity.MY_PREFERENCES, Context.MODE_PRIVATE);
        clientID = LoginActivity.sharedPreferences.getInt(LoginActivity.CLIENT_ID, 0);
        return clientID;
    }


    //validates user input
    public boolean isValidOneWayInput() {
//        if (HelperUtilities.isEmptyOrNull(txtOneWayFrom.getText().toString())) {
//            txtOneWayFrom.setError("Please enter the origin");
//            return false;
//        } else if (!HelperUtilities.isString(txtOneWayFrom.getText().toString())) {
//            txtOneWayFrom.setError("Please enter a valid origin");
//            return false;
//        }
//
//        if (HelperUtilities.isEmptyOrNull(txtOneWayTo.getText().toString())) {
//            txtOneWayTo.setError("Please enter the destination");
//            return false;
//        } else if (!HelperUtilities.isString(txtOneWayTo.getText().toString())) {
//            txtOneWayTo.setError("Please enter a valid destination");
//            return false;
//        }
//
//        if (btnOneWayDepartureDatePicker.getText().toString().equalsIgnoreCase("departure date")) {
//            datePickerOneAlert().show();
//            return false;
//        }
        return true;

    }


    //validates user input
    public boolean isValidRoundInput() {



        if (btnRoundDepartureDatePicker.getText().toString().equalsIgnoreCase("departure date")) {
            datePickerOneAlert().show();
            return false;
        }

        if (btnRoundReturnDatePicker.getText().toString().equalsIgnoreCase("return date")) {
            datePickerTwoAlert().show();
            return false;
        }
        return true;

    }


}
