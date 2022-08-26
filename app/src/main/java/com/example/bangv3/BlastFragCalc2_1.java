package com.example.bangv3;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
//depricated library
//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class BlastFragCalc2_1 extends AppCompatActivity {


    public static String EXTRA_ID = "marker id";

    public static String EXTRA_TOTAL_NEW = "total new";
    public static String EXTRA_STACKED = "stacked";
    public static String EXTRA_CASE_TYPE = "heavy case";
    public static String EXTRA_TEST = "test";

    int markerID;
    double totalNew;
    boolean stacked;
    String caseType;
    double [] kFactor = new double[]{14,25,50,100,328,500,625};
    String workingLine;
    double distanceFeet =1;
    double distanceMeters =2;
    double hazFragMeters;
    double hazFragFeet;
    double maxFragMeters;
    double maxFragFeet;

    double alpha;
    double beta;
    double charlie;
    double delta;
    double maxAlpha;
    double maxBeta;
    double maxCharlie;
    double maxDelta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blast_frag_calc2_1);

        Intent intent = getIntent();
        markerID = intent.getIntExtra(EXTRA_ID, 0);


        try {
            EventMarkerSvcImpl eventMarkerSvcImpl = new EventMarkerSvcImpl(getApplicationContext());
            eventMarkerSvcImpl.readObject();
            EventMarker eventMarker = eventMarkerSvcImpl.findEventMarkerByID(markerID);


            totalNew = eventMarker.getNEW();
            stacked = eventMarker.getStacked();
            caseType = eventMarker.getCaseType();

        }catch(IOException i){

        }

       //switch statement to set the variables for hazfrag calc
        //all done in feet converted later
        switch (caseType){
            case "Non-Robust":

                alpha = 5.524;
                beta = 0.309;
                charlie = -0.0299;
                delta = 0.002;
                maxAlpha = 6.726;
                maxBeta = 0.29251;
                maxCharlie = -0.01086;
                maxDelta = 0.0;

                break;

            case "Robust":

                alpha = 5.449;
                beta = 0.276;
                charlie = -0.015;
                delta = 0.00077;
                maxAlpha = 7.581;
                maxBeta = 0.208;
                maxCharlie = -0.014;
                maxDelta = 0.0006;

                break;

            case "EHC":

                alpha = 5.666;
                beta = 0.238;
                charlie = -0.018;
                delta = 0.001;
                maxAlpha = 7.887;
                maxBeta = 0.219;
                maxCharlie = -0.020;
                maxDelta = 0.001;

                break;

            default:

                alpha = 0.0;
                beta = 0.0;
                charlie = 0.0;
                delta = 0.0;
                maxAlpha = 0.0;
                maxBeta = 0.0;
                maxCharlie = 0.0;
                maxDelta = 0.0;

        }

        //test the spinner and switch statement code
        int duration = Toast.LENGTH_SHORT;
        Context context = getApplicationContext();
        Toast.makeText(context, alpha + " " + beta + " " + charlie + " " + delta, duration).show();

        TextView blastTable = (TextView) findViewById(R.id.kfactorTable);
        TextView fragTable = (TextView) findViewById(R.id.fragTable);


        //formats and builds the k-factor table
        StringBuilder blastTableBuilder = new StringBuilder();
        blastTableBuilder.append("K-Factor Table: ");
        for(int i = 0 ; i < kFactor.length ; i++){


            distanceFeet = kFactor[i] * Math.cbrt(totalNew);
            distanceMeters = distanceFeet/ 3.28;

            blastTableBuilder.append("\n\tK");
            workingLine = String.format("\t%03.0f\t\t%5.0f%-8s\t\t\t\t%05.0f%6s", kFactor[i],
                    distanceMeters, " meters " , distanceFeet , " feet");


            blastTableBuilder.append(workingLine);

        }

        blastTable.setText(blastTableBuilder);

        //formats and builds the HAZ Frag Max Frag table
        StringBuilder fragTableBuild = new StringBuilder();

        hazFragFeet = Math.exp(alpha + beta * (Math.log(totalNew)) + charlie * Math.pow(Math.log(totalNew), 2) + delta * Math.pow(Math.log(totalNew), 3));
        maxFragFeet = Math.exp(maxAlpha + maxBeta * (Math.log(totalNew)) + maxCharlie * Math.pow(Math.log(totalNew), 2) + maxDelta * Math.pow(Math.log(totalNew), 3));


        //adds a stacked multiplyer
        if(stacked){
           maxFragFeet = maxFragFeet * 1.33;
           hazFragFeet = hazFragFeet * 1.33;
        }

        //converts feet to meters
        maxFragMeters = maxFragFeet / 3.28;
        hazFragMeters = hazFragFeet / 3.28;


        fragTableBuild.append("Frag Table:\n");

        workingLine = String.format("%5.0f%-8s\t\t\t\t%05.0f%6s",
                hazFragMeters, " meters " , hazFragFeet , " feet");
        fragTableBuild.append("\tHaz Frag: " + workingLine);


        workingLine = String.format("%5.0f%-8s\t\t\t\t%05.0f%6s",
                maxFragMeters, " meters " , maxFragFeet , " feet");
        fragTableBuild.append("\n\tMax Frag: " + workingLine);

        fragTable.setText(fragTableBuild);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }


    public void onClickPlotToGoogleEarth(View view){
        double hazardRadius;
        double hazardRadiusSec;
        double selectedKFactor;
        double selectedKFactorSec;
        double distanceFt;
        double distanceM;
        String hazRadiusSpinnerSelection;
        String hazRadiusSpinnerSelectionSec;
        int duration = Toast.LENGTH_SHORT;

        Context context = getApplicationContext();

        try {
            EventMarkerSvcImpl eventMarkerSvcImpl = new EventMarkerSvcImpl(getApplicationContext());
            eventMarkerSvcImpl.readObject();
            EventMarker eventMarker = eventMarkerSvcImpl.findEventMarkerByID(markerID);


        //sets the hazard radius based on the selected value in the spinner
        Spinner hazRadiusView = (Spinner) findViewById(R.id.spinnerKFactor1);
        hazRadiusSpinnerSelection = hazRadiusView.getSelectedItem().toString();

        if(hazRadiusSpinnerSelection.compareTo("K-Factor") == 0){

            Toast.makeText(context, "Please select a k-factor or a fragmentation distance.", duration).show();
            return;

        }else if(hazRadiusSpinnerSelection.compareTo("MAX FRAG") == 0){

            hazardRadius = maxFragMeters;
            eventMarker.setPrimaryHazRadius(hazardRadius);
            eventMarker.setPrimaryCircleKFactor("MAX FRAG");


        }else if(hazRadiusSpinnerSelection.compareTo("HAZ FRAG") == 0){

            hazardRadius = hazFragMeters;
            eventMarker.setPrimaryHazRadius(hazardRadius);
            eventMarker.setPrimaryCircleKFactor("HAZ FRAG");


        }else{

            selectedKFactor = Double.parseDouble(hazRadiusSpinnerSelection);

            distanceFt = selectedKFactor * Math.cbrt(totalNew);
            distanceM = distanceFt/ 3.28;

            hazardRadius = distanceM;
            eventMarker.setPrimaryCircleKFactor(hazRadiusSpinnerSelection);
            eventMarker.setPrimaryHazRadius(hazardRadius);


        }

        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ handles secondary haz area @@@@@@@@@@@@@@@@@@@@@@@@@@@
        //sets the hazard radius for outer haz area based on the selected value in the spinner
        Spinner hazRadiusViewSec = (Spinner) findViewById(R.id.spinnerKFactor2);
        hazRadiusSpinnerSelectionSec = hazRadiusViewSec.getSelectedItem().toString();

        if(hazRadiusSpinnerSelectionSec.compareTo("K-Factor") == 0){

            hazardRadiusSec = 0;
            //return;

        }else if(hazRadiusSpinnerSelectionSec.compareTo("MAX FRAG") == 0){

            hazardRadiusSec = maxFragMeters;
            eventMarker.setSecondaryHazRadius(hazardRadiusSec);
            eventMarker.setSecondaryCircleKFactor("MAX FRAG");

        }else if(hazRadiusSpinnerSelectionSec.compareTo("HAZ FRAG") == 0){

            hazardRadiusSec = hazFragMeters;
            eventMarker.setSecondaryHazRadius(hazardRadiusSec);
            eventMarker.setSecondaryCircleKFactor("HAZ FRAG");

        }else{

            selectedKFactor = Double.parseDouble(hazRadiusSpinnerSelectionSec);

            distanceFt = selectedKFactor * Math.cbrt(totalNew);
            distanceM = distanceFt/ 3.28;

            hazardRadiusSec = distanceM;
            eventMarker.setSecondaryHazRadius(hazardRadiusSec);

            eventMarker.setSecondaryCircleKFactor(hazRadiusSpinnerSelectionSec);

        }

        eventMarker.setDisplayHazard(true);

        eventMarkerSvcImpl.writeObject();

        //intializes an intent to pass to another activity
        Intent intent = new Intent(this , MapsActivity.class);
        startActivity(intent);

        }catch(IOException i){


        }

    }
}
