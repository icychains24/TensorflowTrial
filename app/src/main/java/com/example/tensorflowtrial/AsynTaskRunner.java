package com.example.tensorflowtrial;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.tensorflowtrial.Connector;

import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * Created by Shreeya on 28-02-2018.
 */

//for network on main thread exception
public class AsynTaskRunner extends AsyncTask<String, Void, String[]>{
    String output;
//    @Override
//    protected Void doInBackground(Void... params) {
//
//        JSONObject jsb= new JSONObject();
//        try {
//            Connector cnn = new Connector("http://192.168.2.4:5000/add?num1=10&num2=20");
//
//            output = cnn.getDetails();
//            Log.d("msg",output);
//            jsb = new JSONObject(output);
//
//            Log.d("jsb",jsb.getString("data"));
//

    @Override
    protected String[] doInBackground(String... strings) {


        JSONObject jsb= new JSONObject();
        try {
            Log.d("strings....",strings[0]);
            Connector cnn = new Connector("http://192.168.1.8:5000/?"+strings[0]);
            String output = cnn.getDetails();
            Log.d("msg",output);
            jsb = new JSONObject(output);

            Log.d("jsb",jsb.toString());

        }catch (Exception ex){
            // ex.printStackTrace();
        }



        String arr[] = {"abc","asfsa"};
        return arr;
    }
//        }catch (Exception ex){
//            ex.printStackTrace();
//        }
//
//        return null;
//    }

/*
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(Void);

       // Log.d("msg",output);
    }
    */

    /*
    @Override
    protected String doInBackground(String... strings) {



        JSONObject jsb= new JSONObject();
        try {
            Connector cnn = new Connector("http://192.168.2.4:5000/add?num1=10&num2=20");
            String output = cnn.getDetails();
            Log.d("msg",output);
            jsb = new JSONObject(output);

            Log.d("jsb",jsb.toString());

        }catch (Exception ex){
            ex.printStackTrace();
        }

        return jsb.toString();
    }*/
}
