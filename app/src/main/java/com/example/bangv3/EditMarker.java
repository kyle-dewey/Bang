package com.example.bangv3;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

//import com.berico.coords.Coordinates;
import com.example.bangv3.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.util.ArrayList;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.bangv3.databinding.ActivityMapsBinding;

public class EditMarker extends FragmentActivity {

    public static String EXTRA_ID = "marker id";
    private int idNumber;
    private EventMarkerSvcImpl eventMarkerSvcImpl;
    private EventMarker eventMarker;


    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_marker);

        //declares all of the views
        EditText markerNameEditText = (EditText) findViewById(R.id.markerName);
        TextView markerMGRSTextView = (TextView) findViewById(R.id.markerMGRSTextView);
        EditText markerdescriptionEditText = (EditText) findViewById(R.id.markerDescriptionEditText);
        TextView markerNEWTextView = (TextView) findViewById(R.id.NEWTextView);
        TextView markerPrimaryKFactorTextView = (TextView) findViewById(R.id.PrimaryKFactorTextView);
        TextView markerSecondaryKFactorTextView = (TextView) findViewById(R.id.SecondaryKFactorTextView);
        CheckBox markerPlotHazCheckBox = (CheckBox) findViewById(R.id.CircleCheckBox);

        //gets the intent and the extra
        Intent intent = getIntent();
        idNumber = Integer.parseInt(intent.getStringExtra(EXTRA_ID));

        //intializes the service layer
        eventMarkerSvcImpl = new EventMarkerSvcImpl(getApplicationContext());

        //reads the eventMarkers from the .bin
        try {
            eventMarkerSvcImpl.readObject();
        }catch(IOException e){

        }

        //finds the eventMarker by its id in the arrayList
        eventMarker = eventMarkerSvcImpl.findEventMarkerByID(idNumber);

        //populates the different views in the layout with the relivaint marker data
        markerNameEditText.setText(eventMarker.getName());


        markerMGRSTextView.setText(eventMarker.getLatPosition() + ", " + eventMarker.getLongPosition());

        //shunted out until i can get the library to load
        //markerMGRSTextView.setText(Coordinates.mgrsFromLatLon(eventMarker.getLatPosition(),eventMarker.getLongPosition()));
        markerdescriptionEditText.setText(eventMarker.getDescription());
        markerNEWTextView.setText("NEW: " + Double.toString(eventMarker.getNEW()) + " lbs");
        markerPrimaryKFactorTextView.setText("Red Circle: K" + eventMarker.getPrimaryCircleKFactor());
        markerSecondaryKFactorTextView.setText("Green Circle: K" + eventMarker.getSecondaryCircleKFactor());
        markerPlotHazCheckBox.setChecked(eventMarker.getDisplayHazard());

    }

    //send the user to the blast frag calculator
    public void onClickAddExplosiveHazard(View view){
        System.out.println("shuntted out until everything else is working");

        onClickSave(view);
        //intializes an intent to pass to another activity
        Intent intent = new Intent(this , BlastFragCalc2_0.class);
        intent.putExtra(EditMarker.EXTRA_ID, idNumber);
        startActivity(intent);

    }

    //deletes the marker from the EventMarkerSvcImpl
    public void onClickDelete(View view){

        eventMarkerSvcImpl.printListContent();

        for(int i = 0 ; i < eventMarkerSvcImpl.getList().size() ; i ++ ){

            if(eventMarkerSvcImpl.getList().get(i).getID() == idNumber){

                eventMarkerSvcImpl.getList().remove(i);

            }

        }

        eventMarkerSvcImpl.printListContent();
        eventMarkerSvcImpl.writeObject();
        eventMarkerSvcImpl.clearList();

        //intializes an intent to pass to another activity
        Intent intent = new Intent(this , MapsActivity.class);
        startActivity(intent);
    }

    //saves the new modifies the name and description fields to what has been added in the EditTexts
    public void onClickSave(View view){

        EditText markerNameEditText = (EditText) findViewById(R.id.markerName);
        EditText markerdescriptionEditText = (EditText) findViewById(R.id.markerDescriptionEditText);
        CheckBox plotHazard = (CheckBox) findViewById(R.id.CircleCheckBox);

        String newName = markerNameEditText.getText().toString();
        String newDescription = markerdescriptionEditText.getText().toString();

        //iterates through the arrayList looking for the eventMarker with the correct ID
        for(int i = 0; i < eventMarkerSvcImpl.getList().size() ; i++ ){

            if(eventMarkerSvcImpl.getList().get(i).getID() == idNumber){

                //adds the new data items to the arraylist
                eventMarkerSvcImpl.getList().get(i).setDescription(newDescription);
                eventMarkerSvcImpl.getList().get(i).setMarkerName(newName);
                eventMarkerSvcImpl.getList().get(i).setDisplayHazard(plotHazard.isChecked());
                System.out.println("new name and description set");

            }
        }

        //writes the new data to the .bin file
        eventMarkerSvcImpl.writeObject();

        //intializes an intent to pass to another activity
        Intent intent = new Intent(this , MapsActivity.class);
        startActivity(intent);
    }

    //sends the user back to the grg on back press. prevents them from navigating back into the
    //blast frag calculator.
    @Override
    public void onBackPressed(){

        //intializes an intent to pass to another activity
        Intent intent = new Intent(this , MapsActivity.class);
        startActivity(intent);

    }
}
