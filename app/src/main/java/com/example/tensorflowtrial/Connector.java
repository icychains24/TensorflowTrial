package com.example.tensorflowtrial;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Shreeya on 28-02-2018.
 */

public class Connector {

    String username = "admin";
    String password = "password";
    //URL url = new URL("http://localhost:5000/read/json/ledger/F6"); //put flask URL here

    //HttpURLConnection urlconn = (HttpURLConnection) url.openConnection();
    //skip
//
//    String credentials = username + ":" + password;
//    String credBase64 = Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT).replace("\n", "");

    String url_string="";

    // InputStream is = urlconn.getInputStream();

    //BufferedReader reader = new BufferedReader(new InputStreamReader(is));


    public Connector(String url_string) throws IOException {
        this.url_string = url_string;
    }

    public String getDetails(){
        String line = "";
        String line2 = "";
        try{

            URL url = new URL(url_string);

            HttpURLConnection urlconn = (HttpURLConnection) url.openConnection();
//            Log.d("Authroization key" , credBase64);
//            urlconn.setRequestProperty("Authorization","Basic " + credBase64);
//            urlconn.setRequestProperty();
            urlconn.connect();
            InputStream is = urlconn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            while ((line = reader.readLine()) != null) {
                line2 += line.toString();
            }
            Log.d("asg",line2);
            urlconn.disconnect();
        }catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        return line2;
    }


}
