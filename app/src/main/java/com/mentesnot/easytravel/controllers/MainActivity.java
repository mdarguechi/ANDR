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
import android.widget.ListView;
import android.widget.SeekBar;
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




    //traveller count
    private int CreditCount = 0;
    private int SubCreditCount = 0;

    //traveller count view
    private TextView numTraveller;
    private TextView budgetDemande;

    //add and remove image button controls in the dialog
    private ImageButton imgBtnAdd;
    private ImageButton imgBtnRemove;
    private SeekBar AmountScroll;
    //custom dialog view
    private View dialogLayout;

    private Button btnTypeCredit;
    private Button btnSousTypeCredit;
    private Button btnBudjet;
    private Button btnPeriode;
    //round trip UI controls




    //search button
    private Button btnSearch;

    private int tempOneWaySelectedClassID = 0;

    private View header;
    private ImageView imgProfile;
    private int clientID;


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
        btnBudjet = (Button) findViewById(R.id.btnBudget);
        btnPeriode = (Button)  findViewById(R.id.btnPeriode);
        //round trip form


        btnSearch = (Button) findViewById(R.id.btnSearch);
        imgProfile = (ImageView) header.findViewById(R.id.imgProfile);



        drawerProfileInfo();
        loadImage(clientID);




        //one way number of travellers on click listener
        btnTypeCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CreditTypeDialog().show();
            }
        });
        btnSousTypeCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SousCreditType().show();//DARG prevoir 2eme dialogue
            }
        });

        btnBudjet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreditAmount().show();//DARG prevoir 3eme dialogue
            }
        });
        btnPeriode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreditPeriode().show();//DARG prevoir 3eme dialogue
            }
        });


        //searches available flights on click
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //call search method here

                if (currentTab == 0) {

                    //if (isValidOneWayInput() && isValidOneWayDate) {
                        searchOneWayFlight();

                   // }

                }

            }
        });

    }
    public void searchOneWayFlight() {

        intent = new Intent(getApplicationContext(), OneWayFlightListActivity.class);

        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        getApplicationContext().getSharedPreferences("PREFS", 0).edit().clear().commit();

        SharedPreferences.Editor editor = sharedPreferences.edit();



        startActivity(intent);


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



    public int Indices = -1;
    //number of travellers dialog (one way)
    public Dialog CreditTypeDialog() {


        dialogLayout = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Type de Crédit :")
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
        final String[] TypeCreditStr = {
                "Intrants agricoles",
                "moyens de transport",
                "Elevage (bétail)",
                "Industries alimentaires",
                "Marchandises",
                "Outils et équipements divers"
        };
        imgBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreditCount++;
                if (CreditCount > TypeCreditStr.length-1)
                {
                    CreditCount = 0;
                }
                Indices = CreditCount;
                numTraveller.setText(TypeCreditStr[CreditCount]);
                btnTypeCredit.setText(TypeCreditStr[CreditCount]);
               // btnSousTypeCredit.setText(String.valueOf(oneWayTravellerCount) + " S.Credit");
            }
        });

        imgBtnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreditCount--;
                if (CreditCount <0) {
                    CreditCount = 5;
                }
                Indices = CreditCount;
                numTraveller.setText(TypeCreditStr[CreditCount]);
                btnTypeCredit.setText(TypeCreditStr[CreditCount]);
                //btnSousTypeCredit.setText(String.valueOf(oneWayTravellerCount) + " S.Credit");
            }
        });


        numTraveller.setText(TypeCreditStr[CreditCount]);

        return builder.create();
    }
    //number of travellers dialog (one way)
    public Dialog SousCreditType() {


        dialogLayout = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        SubCreditCount =0;
        builder.setTitle("Cible ")
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

        if (Indices == -1) {
            Toast.makeText(this, "Veuillez choisir un secteur d'activité", Toast.LENGTH_SHORT).show();
            return builder.create();

        }


        imgBtnRemove = (ImageButton) dialogLayout.findViewById(R.id.imgBtnRemove);
        imgBtnAdd = (ImageButton) dialogLayout.findViewById(R.id.imgBtnAdd);
        final String SousCreditTypeStr[][] = new String[6][];
        SousCreditTypeStr[0] = new String[] {"engrais","graines et semence","outillage et machinerie"};
        SousCreditTypeStr[1] = new String[] {"Moyens de transport"};
        SousCreditTypeStr[2] = new String[] {"bétail","volaille","poisson"};
        SousCreditTypeStr[3] = new String[] {"Marchandises et équipements"};
        SousCreditTypeStr[4] = new String[] {"produits alimentaires","produits de consommation","matériels électroniques"};
        SousCreditTypeStr[5] = new String[] {"Outils et équipements divers"};

        numTraveller = (TextView) dialogLayout.findViewById(R.id.txtNumber);

        imgBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubCreditCount++;
                if (SubCreditCount >  SousCreditTypeStr[Indices].length-1)
                {
                    SubCreditCount = 0;
                }
                numTraveller.setText(SousCreditTypeStr[Indices][SubCreditCount]);
                btnSousTypeCredit.setText(SousCreditTypeStr[Indices][SubCreditCount]);
            }
        });

        imgBtnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubCreditCount--;
                if (SubCreditCount <0) {
                    SubCreditCount = SousCreditTypeStr[Indices].length - 1;
                }
                //numTraveller.setText(String.valueOf(oneWayTravellerCount));
                numTraveller.setText(SousCreditTypeStr[Indices][SubCreditCount]);
                btnSousTypeCredit.setText(SousCreditTypeStr[Indices][SubCreditCount]);
            }
        });


        btnSousTypeCredit.setText(SousCreditTypeStr[Indices][SubCreditCount]);

        return builder.create();
    }

    public Dialog CreditAmount() {


        //AmountScroll.setMin(1000);


        dialogLayout = getLayoutInflater().inflate(R.layout.custom_dialog_jauges, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        SubCreditCount =0;
        builder.setTitle("Montant")
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

        budgetDemande = (TextView) dialogLayout.findViewById(R.id.txtBudget);

        AmountScroll = (SeekBar) dialogLayout.findViewById(R.id.seekBar);

        AmountScroll.setMax(20000);
        AmountScroll.incrementProgressBy(100);
        AmountScroll.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                progress = progress / 100;
                progress = progress * 100;
                progressChangedValue = progress;

                budgetDemande.setText(String.valueOf(progressChangedValue));
                btnBudjet.setText(String.valueOf(progressChangedValue));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                budgetDemande.setText(String.valueOf(progressChangedValue));
                btnBudjet.setText(String.valueOf(progressChangedValue+" TND"));
            }
        });





        return builder.create();
    }

    public Dialog CreditPeriode() {


        //AmountScroll.setMin(1000);


        dialogLayout = getLayoutInflater().inflate(R.layout.custom_dialog_jauges, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        SubCreditCount =0;
        builder.setTitle("Periode")
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

        budgetDemande = (TextView) dialogLayout.findViewById(R.id.txtBudget);

        AmountScroll = (SeekBar) dialogLayout.findViewById(R.id.seekBar);

        AmountScroll.setMax(36);
        AmountScroll.incrementProgressBy(1);
        AmountScroll.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                progress = progress / 1;
                progress = progress * 1;
                progressChangedValue = progress;

                budgetDemande.setText(String.valueOf(progressChangedValue));
                btnPeriode.setText(String.valueOf(progressChangedValue));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                budgetDemande.setText(String.valueOf(progressChangedValue));
                btnPeriode.setText(String.valueOf(progressChangedValue+" Mois"));
            }
        });





        return builder.create();
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




        return true;

    }


}
