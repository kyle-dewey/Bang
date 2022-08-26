package com.example.bangv3;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
//i think this library is deprecated
//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class BlastFragCalc2_0 extends AppCompatActivity {

    public static String EXTRA_ID = "marker id";

    int markerID;
    double totalNewTNT = 0.0;
    double totalNewAddition = 0.0;
    double tntEquivlant = 1.0;
    double quantity = 0.0;
    double unitConversion = 1.0;

    boolean calculating = false;

    StringBuilder explListBuilder = new StringBuilder("Explosives List: ");
    String totalNewString;
    String inputWeight;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blast_frag_calc2_0);

        TextView explosiveListView = (TextView) findViewById(R.id.explosiveList);
        explosiveListView.setMovementMethod(new ScrollingMovementMethod());

        EditText quantityView = (EditText) findViewById(R.id.quantity);
        quantityView.setText("1");

        Intent intent = getIntent();
        markerID = intent.getIntExtra(EXTRA_ID, 0);


    }


    //code that executes when add button is clicked
    public void onClickAdd(View view){


        //gets the value inputed for weight from the editText
        EditText weightView = (EditText) findViewById(R.id.weightedit);

        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        TextView explosiveListView = (TextView) findViewById(R.id.explosiveList);

        //asks the user to enter a weight if they have neglected to do so
        inputWeight = weightView.getText().toString();
        if(inputWeight == null || inputWeight.trim().equals("") && explosiveListView.getText().toString().compareTo("Explosives List: ")==0){

            Toast.makeText(context, "Please enter a weight.", duration).show();
            return;

        }

        //makes sure input is a double
        //origin of the toast bug
            try {

                Double.valueOf(inputWeight);

            } catch (NumberFormatException numberFormatException) {

                if(inputWeight == null ){

                    return;

                }

                Toast.makeText(context, "Please enter a valid number for weight.", duration).show();
                return;

            }

            double weight = Double.parseDouble(weightView.getText().toString());

            if (weight <= 0) {

                Toast.makeText(context, "Please enter a positive number for weight.", duration).show();
                return;

            }

        //gets the value inputed for quantity from the editText
        EditText quantityView = (EditText) findViewById(R.id.quantity);
        String inputQuantity = quantityView.getText().toString();

        //makes sure input is a double
        try {

            Double.valueOf(inputQuantity);

        } catch (NumberFormatException numberFormatException) {

            Toast.makeText(context, "Please enter a valid number for quantity.", duration).show();
            return;

        }

        quantity = Double.parseDouble(quantityView.getText().toString());

        //gets the value from the units spinner
        Spinner unitSpinner = (Spinner) findViewById(R.id.spinnerunit);
        String units = unitSpinner.getSelectedItem().toString();

        //gets the value from the explosives spinner
        Spinner explosiveSpinner = (Spinner) findViewById(R.id.spinnerexplosivetype);
        String explosiveType = explosiveSpinner.getSelectedItem().toString();

        //builds an textview so that i can change the text of the explosives list
        TextView explosiveTotalView = (TextView) findViewById(R.id.totalNew);


        /* easter egg removed for professional version of the app
        if(explosiveType.compareTo("Nitroglycerine ,1.81")== 0 && weight == 666){

            Toast.makeText(context, "GAME OF GNAR", duration).show();


            Intent intent = new Intent(this , GameOfGnar1_0.class);
            startActivity(intent);

        }

         */



        explListBuilder.append("\n (" + quantity + ") ");
        explListBuilder.append(explosiveType + " " + "X ");
        explListBuilder.append(weight + " " + units);

        explosiveListView.setText(explListBuilder.toString());

        if(units.compareTo("kgs")== 0){

            unitConversion = 2.205;

        }


        tntEquivlant = parseExplosiveType(explosiveType);

        totalNewAddition = quantity * weight * tntEquivlant * unitConversion;

        totalNewTNT = totalNewTNT + totalNewAddition;

        totalNewString = String.format("%6.3f", totalNewTNT);

        explosiveTotalView.setText("Total TNT equivalent: " + totalNewString + " lbs");


        weightView.setText(null);
        quantityView.setText("1");


    }

    //code that executes when reset button is clicked
    public void onClickResetExplosiveList(View view){


        explListBuilder.setLength(0);
        explListBuilder.append("Explosive List: ");
        totalNewTNT = 0.000;

        //builds an textview so that i can change the text of the explosives list
        TextView explosiveListView = (TextView) findViewById(R.id.explosiveList);

        //builds an textview so that i can change the text of the explosives list
        TextView explosiveTotalView = (TextView) findViewById(R.id.totalNew);


        explosiveListView.setText(explListBuilder.toString());
        explosiveTotalView.setText("Total TNT equivalent: " + totalNewTNT + " lbs");
        ;

    }

    //code that executes when calculate button is clicked
    public void onClickCalculate(View view){

        boolean stacked;
        String caseType;

        EditText weightView = (EditText) findViewById(R.id.weightedit);
        if(weightView.getText() != null){

            calculating = true;
            this.onClickAdd(findViewById(R.id.addExplosive));

        }

        CheckBox stackedCheckBox = (CheckBox) findViewById(R.id.checkBoxStacked);
        stacked = stackedCheckBox.isChecked();

        //gets the value from the case type spinner
        Spinner caseTypeSpinner = (Spinner) findViewById(R.id.spinnercasetype);
        caseType = caseTypeSpinner.getSelectedItem().toString();

        //writes the new object properties to the database
        try {

            EventMarkerSvcImpl eventMarkerSvcImpl = new EventMarkerSvcImpl(getApplicationContext());
            eventMarkerSvcImpl.readObject();
            EventMarker eventMarker = eventMarkerSvcImpl.findEventMarkerByID(markerID);
            eventMarker.setStacked(stacked);
            eventMarker.setCaseType(caseType);
            eventMarker.setNEW(totalNewTNT);
            eventMarkerSvcImpl.writeObject();


        }catch(IOException i){


        }

        Intent intent = new Intent(this , BlastFragCalc2_1.class);
        intent.putExtra(EditMarker.EXTRA_ID, markerID);
        startActivity(intent);


    }

    //parses the inputed string selested from the expl. type spinner into tnt equivelency
    public Double parseExplosiveType(String workingLine){

        double tntEquiv;
        String[] workingArray = workingLine.split(",");

        tntEquiv = Double.parseDouble(workingArray[1]);

        return tntEquiv;

    }
}
