package com.example.bangv3;


import java.util.ArrayList;
import java.io.*;
import android.content.Context;
import android.os.Environment;

public class EventMarkerSvcImpl {


    private int currentID;
    private ArrayList<EventMarker> list;
    private String fileName;
    private Context context;

    //constructors
    public EventMarkerSvcImpl(Context newContext){

        fileName =  "eventMarker.bin";
        currentID = 0;
        list = new ArrayList<>();
        context = newContext;

    }
    public EventMarkerSvcImpl(Context newContext, String newFileName){

        currentID = 0 ;
        list = new ArrayList<>();
        fileName = newFileName;
        context = newContext;

    }

    //getters
    public ArrayList<EventMarker> getList(){

        return list;
    }
    public String getFileName(){
        return fileName;
    }
    public int getCurrentID(){return currentID; }

    //addition method: adds an activityList item
    public void addEventMarker(EventMarker newEventMarker){

        System.out.println("addEventMarker() CALLED");
        currentID++;
        list.add(newEventMarker);

    }

    //remove method: removes an activityList item
    public void removeEventMarker(EventMarker aEventMarker){

        System.out.println("removeEventMarker() CALLED");
        list.remove(aEventMarker);

    }

    //allows you to search the list of activityList objects by name
    public EventMarker findEventMarkerByName(String name){

        System.out.println("findEventMarkerByName() CALLED");
        for(int n = 0 ; n < list.size() ; n++){

            if(list.get(n).getName().compareTo(name) == 0){

                return list.get(n);

            }
        }

        return null;
    }

    //returns  the marker with the id that is provided as an argumet
    public EventMarker findEventMarkerByID(int id){

        System.out.println("findEventMarkerByID() CALLED");
        for(int n = 0 ; n < list.size() ; n++){

            if(list.get(n).getID() == id){

                return list.get(n);

            }
        }

        return null;
    }

    //prints the contents of the list
    public void printListContent(){

        System.out.println("printListContent() CALLED");
        //iterates through the activittyList array
        for(int x = 0 ; x < list.size(); x++){

            //checks to make sure the list isn't null
            if(list.get(x)!= null){

                //prints the name of the list
                System.out.println(list.get(x).getName());
                System.out.println("  " + list.get(x).getDescription());
                System.out.println("  " + list.get(x).getLatPosition() + " , " + list.get(x).getLongPosition());


            }

        }


    }

    //clears the list
    public void clearList(){
        System.out.println("clearList() CALLED");
        list.clear();
    }

    //reads data from a binary file and popualtes the list
    public void readObject() throws IOException{

        System.out.println("readObject() CALLED");
        System.out.println("Reading data from: " + fileName);

        int highestID = 0 ;
        //opens the object input stream
        ObjectInputStream ois = new ObjectInputStream(context.openFileInput(fileName));

        //clears the arraylist before it is written to.
        list.clear();

        try {
            //creates a temp list to recieve the deserialized list
            ArrayList<EventMarker> temp = (ArrayList<EventMarker>) ois.readObject();

            //copies the contents of temp into list
            //iterates through the activityList array
            for (int x = 0; x < temp.size(); x++) {

                //checks to make sure the temp isn't null
                if (temp.get(x) != null) {

                    //creates a working activityList object to store the feilds
                    // EventMarker tempMarker = new activityList(temp.get(x).getName(), temp.get(x).getDescription());

                    list.add(temp.get(x));

                    if(temp.get(x).getID() > highestID){

                        highestID = temp.get(x).getID();
                    }


                }

            }


            //closes the input stream
            ois.close();
            //fis.close();
        }catch(ClassNotFoundException e){

            System.out.println("Class not found exception: " );
        }

        currentID = highestID + 1;



    }

    //writes the existing list to a binary file
    public void writeObject() {

        try{
            File outFile = new File(fileName);
            //opens the object output stream
            ObjectOutputStream oos = new ObjectOutputStream(context.openFileOutput(fileName, 0));

            oos.writeObject(list);

            System.out.println("Writing data to: " + fileName);

            //closes the object output stream.
            oos.close();
            //fos.close();
        }catch(IOException fnfe){

            System.out.println("File not found inside writeObject()");

        }


    }


}
