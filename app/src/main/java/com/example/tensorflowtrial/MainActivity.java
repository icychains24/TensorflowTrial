package com.example.tensorflowtrial;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Trace;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.example.tensorflowtrial.MySQLiteHelper;
import com.example.tensorflowtrial.R;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class MainActivity extends Activity {


//    static {
//        System.loadLibrary("tensorflow_inference");
//    }
//
//    private TensorFlowInferenceInterface inferenceInterface;
//
//    private static final String MODEL_FILE = "file:///android_asset/optimized_tfdroid.pb";
//    private static final String INPUT_NODE = "input_1902";
//    private static final String OUTPUT_NODE = "output_1902";
//    private static final int[] INPUT_SIZE = {1,3000};

    private static final String TAG = "PlayHelloActivity";
    private final static String GMAIL_SCOPE = "https://www.googleapis.com/auth/gmail.readonly";
    private final static String SCOPE = "oauth2:" +  GMAIL_SCOPE;


    private TextView mOut;
    private ListView lView;
    private ProgressBar spinner;
    private ArrayList<String> l;
    private ArrayList<String> b;
    private ArrayList<String> dateList;
    private ArrayList<String> fList;
    private ArrayList<String> fullEmailList;

    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
    public static final String PREFS_NAME = "PrimeFile";
    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//            inferenceInterface=new TensorFlowInferenceInterface(getAssets(),MODEL_FILE);

        String email = loadSavedPreferences();
        if(!email.equals("EmailStuff")){
            mEmail = loadSavedPreferences();
//            Log.d("Email2", mEmail);
        }

        MySQLiteHelper db = new MySQLiteHelper(this);
        l = new ArrayList<String>();
        b = new ArrayList<String>();
        final ArrayList<String> dateList = new ArrayList<String>();
        fList = new ArrayList<String>();
        fullEmailList=new ArrayList<String>();
        List<Email> list = db.getAllBooks();

        int size=list.size();

        final int[] INPUT_SIZE = {1,3};

        for(Email e : list){
            l.add(e.getSubject());
            b.add(e.getBody());
            fList.add(e.getAuthor());
            dateList.add(e.getDateTime());

            //fullemaillist has subject and body..concatenate other details if needed.
            fullEmailList.add(e.getSubject()+e.getBody());
        }

//        String s=fullEmailList.get(3).toString().trim();
//        final byte[] bytearray1 = s.getBytes();

//        System.out.println("byte array"+bytearray);
//        byte[] res={0,0};

//        Trace.beginSection("feed");
//        inferenceInterface.feed(INPUT_NODE,bytearray1,1,370);
//        Trace.endSection();
//
//        inferenceInterface.run(new String[]{OUTPUT_NODE});
//
//        inferenceInterface.fetch(OUTPUT_NODE,res);




//        System.out.println("Result"+Byte.toString(res[0])+Byte.toString(res[1])+Byte.toString(res[2]));


//        }





        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, l );

        mOut = (TextView) findViewById(R.id.message);
        lView = (ListView) findViewById(R.id.listView);

        spinner = (ProgressBar)findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);
        lView.setAdapter(arrayAdapter);
        lView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent i = new Intent(getApplicationContext(), com.example.tensorflowtrial.InfoActivity.class);
                i.putExtra("body",b.get(position));
                i.putExtra("subject", l.get(position));
                i.putExtra("from", fList.get(position));
                i.putExtra("date",dateList.get(position));
                startActivity(i);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                savePreferences("email", mEmail);
//                Log.d("Email1", "Putting " + mEmail + " into prefs");
                getUsername();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "You must pick an account", Toast.LENGTH_SHORT).show();
            }
        } else if ((requestCode == REQUEST_CODE_RECOVER_FROM_AUTH_ERROR ||
                requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR)
                && resultCode == RESULT_OK) {
            handleAuthorizeResult(resultCode, data);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleAuthorizeResult(int resultCode, Intent data) {
        if (data == null) {
            show("Unknown error, click the button again");
            return;
        }
        if (resultCode == RESULT_OK) {
            Log.i(TAG, "Retrying");
            getTask(this, mEmail, SCOPE).execute();
            return;
        }
        if (resultCode == RESULT_CANCELED) {
            show("User rejected authorization.");
            return;
        }
        show("Unknown error, click the button again");
    }

    /** Called by button in the layout */
    public void greetTheUser(View view) {
        getUsername();
    }

    private void savePreferences(String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private String loadSavedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = sharedPreferences.getString("email", "EmailStuff");
        return name;
    }

    /** Attempt to get the user name. If the email address isn't known yet,
     * then call pickUserAccount() method so the user can pick an account.
     */

    private void getUsername() {
        if (mEmail == null) {
            pickUserAccount();
        } else {
            if (isDeviceOnline()) {
                getTask(MainActivity.this, mEmail, SCOPE).execute();
            } else {
                Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Starts an activity in Google Play Services so the user can pick an account */
    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    /** Checks whether the device currently has a network connection */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }


    /**
     * This method is a hook for background threads and async tasks that need to update the UI.
     * It does this by launching a runnable under the UI thread.
     */
    public void show(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mOut.setText(message);
            }
        });
    }


    public void list(final ArrayList<String> l) {
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, l );
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lView.setAdapter(arrayAdapter);
            }
        });
    }

    public void showSpinner(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(View.VISIBLE);
            }
        });
    }

    public void hideSpinner(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(View.GONE);
            }
        });
    }

    public void setItemListener(final ArrayList<String> b, final ArrayList<String> s, /*, final ArrayList<String> fromList*/ArrayList<String> author, ArrayList<String> dateTimeList){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lView.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Intent i = new Intent(getApplicationContext(), com.example.tensorflowtrial.InfoActivity.class);
                        i.putExtra("body",b.get(position));
                        i.putExtra("subject", s.get(position));
                        i.putExtra("from", fList.get(position));
                        i.putExtra("date",dateList.get(position));
                        // shreeya - PUT DATE, FROM TO , ETC HERE!!!!!!!!!
                        startActivity(i);
                    }
                });
            }
        });
    }

    /**
     * This method is a hook for background threads and async tasks that need to provide the
     * user a response UI when an exception occurs.
     */

    public void handleException(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e instanceof GooglePlayServicesAvailabilityException) {
                    // The Google Play services APK is old, disabled, or not present.
                    // Show a dialog created by Google Play services that allows
                    // the user to update the APK
                    int statusCode = ((GooglePlayServicesAvailabilityException)e)
                            .getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                            MainActivity.this,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                    dialog.show();
                } else if (e instanceof UserRecoverableAuthException) {
                    // Unable to authenticate, such as when the user has not yet granted
                    // the app access to the account, but the user can fix this.
                    // Forward the user to an activity in Google Play services.
                    Intent intent = ((UserRecoverableAuthException)e).getIntent();
                    startActivityForResult(intent,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                }
            }
        });
    }

    /**
     * Note: This approach is for demo purposes only. Clients would normally not get tokens in the
     * background from a Foreground activity.
     */
    private com.example.tensorflowtrial.GetNameTask getTask(
            MainActivity activity, String email, String scope) {

        return new com.example.tensorflowtrial.GetNameTask(activity, email, scope);

    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }


}
