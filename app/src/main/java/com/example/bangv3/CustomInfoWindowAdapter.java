package com.example.bangv3;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter{


    private final View mWindow;
    private Context mContext;
    private Marker currMarker;
    private EventMarkerSvcImpl eventMarkerSvcImpl;



    //constructor
    public CustomInfoWindowAdapter(Context context, Marker currentMarker) {
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
        currMarker = currentMarker;
        eventMarkerSvcImpl = new EventMarkerSvcImpl(context);

    }



    // code that populates the different text fields within the information window
    private void rendowWindowText(Marker marker, View view){

        String snippet = marker.getSnippet();
        LatLng latLng = marker.getPosition();
        String mgrs = String.valueOf(latLng.latitude) + ", " + String.valueOf(latLng.longitude);
        String identifier = marker.getTitle();
        String title = marker.getTitle();


        //sets the mgrs field in the custom info window by convert the lat/long to mgrs
        TextView markerMGRS = view.findViewById(R.id.markerMGRS);
        markerMGRS.setText(mgrs);


        //sets the distance and bearing into the marker snippet after calculating
        TextView tvSnippet = view.findViewById(R.id.snippet);

        //pulls the marker name from the preferences and writes it to the info window
        TextView markerNameTextView =  view.findViewById(R.id.markerId);

        System.out.println("CustomInfo window debug");
        eventMarkerSvcImpl.printListContent();

        try {

            if (!snippet.equals("")) {
                tvSnippet.setText(MapsActivity.writeSnippet(currMarker.getPosition(), marker.getPosition()));
            }



            int markerID = Integer.parseInt(marker.getTitle());
            try {
                //commented out to see if this is deleteing the items
                //eventMarkerSvcImpl.clearList();
                eventMarkerSvcImpl.readObject();
            } catch (IOException i) {

            }

            String markerName = "temp " + Integer.toString(markerID) + " ";
            for (int i = 0; i < eventMarkerSvcImpl.getList().size(); i++) {

                System.out.println("id: " + i);
                if (markerID == eventMarkerSvcImpl.getList().get(i).getID()) {

                    markerName = eventMarkerSvcImpl.getList().get(i).getName();
                }
            }

            markerNameTextView.setText(markerName);

        }catch(NullPointerException i){

            tvSnippet.setText("                                                    ");
            markerNameTextView.setText("Current position");

        }

    }


    @Override
    public View getInfoWindow(Marker marker) {

        rendowWindowText(marker, mWindow);
        return mWindow;

    }

    @Override
    public View getInfoContents(Marker marker) {
        rendowWindowText(marker, mWindow);
        return mWindow;
    }


}
