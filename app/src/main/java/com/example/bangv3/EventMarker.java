package com.example.bangv3;

import java.io.Serializable;
import android.content.Context;



public class EventMarker implements Serializable{

    private int ID;
    private String markerName;
    private String description;
    private double latPosition;
    private double longPosition;
    private double NEW;
    private double primaryHazRadius;
    private double secondaryHazRadius;
    private double distanceMeters;
    private double bearing;
    private boolean stacked;
    private boolean displayHazard;
    private String caseType;
    private Context mContext ;

    //used to display the k-factor being represented by the circles on the map
    private String primaryCircleKFactor;
    private String secondaryCircleKFactor;



    //constructors
    public EventMarker( int newID, String newName,double newLatPosition, double newLongPosition, String newDescription){

        ID = newID;
        markerName = newName;

        //set the description equal to the string asset at defaultDescriptionBlurb
        //description = "Add a description: This can be anything you think is important about this place or event";
        description = newDescription;
        latPosition = newLatPosition;
        longPosition = newLongPosition;
        NEW = 0.0;
        primaryHazRadius = 0.0;
        secondaryHazRadius = 0.0;
        distanceMeters = 0.0;
        bearing = 0.0;
        stacked = false;
        caseType = "Non-Robust";
        displayHazard = false;
        primaryCircleKFactor = " ";
        secondaryCircleKFactor = " ";

    }


    //getters
    public int getID(){
        return ID;
    }
    public String getName(){
        return markerName;
    }
    public String getDescription(){
        return description;
    }
    public double getLatPosition(){return latPosition;}
    public double getLongPosition(){ return longPosition;}
    public double getNEW(){
        return NEW;
    }
    public double getPrimaryHazRadius(){
        return primaryHazRadius;
    }
    public double getSecondaryHazRadius(){
        return secondaryHazRadius;
    }
    public double getDistanceMeters(){
        return distanceMeters;
    }
    public double getBearing(){
        return bearing;
    }
    public boolean getStacked(){ return stacked;}
    public String getCaseType(){return caseType; }
    public boolean getDisplayHazard(){return displayHazard;}
    public String getPrimaryCircleKFactor(){return primaryCircleKFactor;}
    public String getSecondaryCircleKFactor(){return secondaryCircleKFactor;}

    //setters
    public void setMarkerName(String newMarkerName){

        markerName = newMarkerName;
    }
    public void setDescription(String newDescription){
        description = newDescription;
    }
    public void setLatPosition(double newLatPosition){latPosition = newLatPosition;}
    public void setLongPosition(double newLongPosition){ longPosition = newLongPosition;}
    public void setNEW(double newNEW){
        NEW = newNEW;
    }
    public void setPrimaryHazRadius(double newPrimaryHazRadius){
        primaryHazRadius = newPrimaryHazRadius;
    }
    public void setSecondaryHazRadius(double newSecondaryHazRadius){
        secondaryHazRadius = newSecondaryHazRadius;
    }
    public void setDistanceMeters(double newDistance){
        distanceMeters = newDistance ;
    }
    public void setBearing(double newBearing){
        bearing = newBearing ;
    }
    public void setStacked(boolean newStacked){
        stacked = newStacked;
    }
    public void setCaseType(String newCaseType){
        caseType = newCaseType;
    }
    public void setDisplayHazard(boolean h){displayHazard = h;}
    public void setPrimaryCircleKFactor(String n ){ primaryCircleKFactor = n;}
    public void setSecondaryCircleKFactor(String m){secondaryCircleKFactor = m;}

}
