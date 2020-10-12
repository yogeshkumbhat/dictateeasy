package com.example.connect_easy;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class loginAction extends AppCompatActivity implements View.OnClickListener {
    private static final int SDCARD_PERMISSION = 1;
    private Button buttonScan,buttonText;
    private String loginURL= "http://192.168.0.107:3000/api/users/UUID_login";
    private String updateDeviceCountURL="http://192.168.0.107:3000/api/users/update_device_prefix";
    static RequestQueue requestQueue;
    public static final String Shared_prefs="sharedPrefs";
    private static SharedPreferences sharedPreferences;
    private Boolean loginStatus=false;
    String username="";
    //qr code scanner object
    private IntentIntegrator qrScan;
    VideoView vid;
    MediaPlayer mMediaPlayer;
    int mcurrentvideoPosition;
    String deviceName="";
    protected void onCreate(Bundle savedInstanceState) {
        //allow storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            //Write permission is required so that folder picker can create new folder.
            //If you just want to pick files, Read permission is enough.

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        SDCARD_PERMISSION);
            }
        }
        loadData();

        requestQueue=Volley.newRequestQueue(this);
        deviceName= android.os.Build.MODEL;
        deviceName=deviceName.trim().toLowerCase();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_action);


        //view objects
        buttonScan=(Button)findViewById(R.id.scan_button);
        buttonText=(Button)findViewById(R.id.text_button);


        //action listeners
        buttonScan.setOnClickListener(this);
        buttonText.setOnClickListener(this);
    }
    @Override
    public void onClick(View view)
    {
        //initiating the qr code scan
        Log.e("selected action",view.getId()+"");
        switch(view.getId()) {
            case R.id.scan_button:
                //intializing scan object
                qrScan = new IntentIntegrator(this);
                qrScan.initiateScan();
                break;
            case R.id.text_button:
                startActivity(new Intent(this, MainActivity.class));
                break;
        }

    }
    //Getting the scan results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {
                //if qr contains data
                Log.e("scanner_data",result.getContents());
                submit(result.getContents());

            }
        } else {

            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    //login_type 1 for manual login and 2 for qr code scan login
    private void submit(String UUID) {
        //RequestQueue requestQueue =Volley.newRequestQueue(this);

        Map<String, String> postParam = new HashMap<String, String>();

        postParam.put("UUID", UUID);




        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                loginURL, new JSONObject(postParam),
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        parseData(response.toString());
                        Log.d("serverresponse", response.toString());
                        //  msgResponse.setText(response.toString());
                        //hideProgressDialog();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("errrortype","login_error");
                VolleyLog.d("errorresponse", "Error: " + error.getMessage());
                // hideProgressDialog();
            }
        }) {

            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }




        };
        requestQueue.add(jsonObjReq);
    }
    public void parseData(String response) {
        try {

            JSONObject jsonObject = new JSONObject(response);
               Log.e("device name",deviceName);
            if (jsonObject.getString("success").equals("1")) {
                JSONObject dataobj = jsonObject.getJSONObject("data");
                Log.i("success_val",dataobj.getString("id")+"ramu kaka");
                if(deviceName.equals("psp2100"))
                {
                    //check if the directory present\
                    //create prefix
                    String userFullName=dataobj.getString("username_for_prefix");

                    String[] userNameArr=userFullName.trim().split("_");
                    Log.e("username",userNameArr[0].charAt(0)+" ");
                    String generatedPrefix="intitials";
                    //check prefix in database
                    if(dataobj.getString("prefix").trim().equals(""))
                    {
                        if(userNameArr.length<2)
                        {
                            if(userNameArr[0].length()>=3)
                            {
                                generatedPrefix=userNameArr[0].charAt(0)+""+userNameArr[0].charAt(1)+""+userNameArr[0].charAt(2)+"";
                            }
                        }
                        else if(userNameArr.length==2)
                        {
                            generatedPrefix=userNameArr[0].charAt(0)+""+userNameArr[1].charAt(0)+""+userNameArr[1].charAt(userNameArr[1].length()-1)+"";
                            Log.e("generatedprefix",generatedPrefix);
                        }
                        else if(userNameArr.length==3)
                        {
                            generatedPrefix=userNameArr[0].charAt(0)+""+userNameArr[2].charAt(0)+""+userNameArr[2].charAt(userNameArr[2].length()-1)+"";
                        }
                        generatedPrefix=generatedPrefix+"_"+dataobj.getString("id");
                    }
                   else
                    {
                        generatedPrefix=dataobj.getString("prefix");
                    }

                    Log.e("generatedprefix",generatedPrefix);
                    String Path="/storage/A2AF-14EF/Android/data/com.speech/files/dictations";
                    if(new File(Path).exists())
                    {
                        Log.e("selecteddir","directory found");
                        saveData(true,dataobj.getString("username"),Path,dataobj.getString("user_id"),generatedPrefix,dataobj.getString("id"));
                        loadData();
                        Toast.makeText(getApplicationContext(),"Login Successfull",Toast.LENGTH_SHORT).show();
                    }
                    /*String selectedDirectory=
                    */
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //set data in shared reference and make them private
    public void saveData(Boolean loginStatus,String username,String selectedDirectory, String deviceID,String prefix,String uid)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(Shared_prefs, MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean("panadem.loginStatus", loginStatus);
        editor.putString("panadem.username",username);
        editor.putString(" panadem.deviceId",deviceID);
        editor.putString("panadem.selectedDirectory",selectedDirectory);
        editor.putString("panadem.prefix",prefix);
        editor.putString("panadem.uid",uid+"");
        Log.i("selecteddirectory",selectedDirectory);
        //update db to set device_count +1
        update_device_prefix(uid,prefix);
        editor.commit();
    }
    public void loadData(){
        sharedPreferences=getSharedPreferences(Shared_prefs,MODE_PRIVATE);
        loginStatus=sharedPreferences.getBoolean("panadem.loginStatus",false);
        if(loginStatus) {
            if (sharedPreferences.contains("panadem.username") && !sharedPreferences.getString("panadem.username", "").equals("")) {
                username = sharedPreferences.getString("panadem.username", "");

                //folderArea.setText("login was successfull"+ sharedPreferences.getString(" panadem.deviceId", ""));
                startActivity(new Intent(this,MainActivity.class));
                finish();
            }
        }
        Log.e("saved_data", username+" "+loginStatus);
    }
    private void update_device_prefix(String id,String prefix)
    {
        //RequestQueue requestQueue =Volley.newRequestQueue(this);
        if(isInteger(id)) {


            Map<String, String> postParam = new HashMap<String, String>();
            postParam.put("id", id);
            postParam.put("prefix", prefix);
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    updateDeviceCountURL, new JSONObject(postParam),
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d("serverresponse", response.toString());

                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d("errorresponse", "Error: " + error.getMessage());
                    // hideProgressDialog();
                }
            }) {

                /**
                 * Passing some request headers
                 */
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    return headers;
                }


            };
            requestQueue.add(jsonObjReq);
        }
    }
    public static boolean isInteger(String s) {
        boolean isValidInteger = false;
        try
        {
            Integer.parseInt(s);
            isValidInteger = true;
        }
        catch (NumberFormatException ex)
        {
            // s is not an integer
        }

        return isValidInteger;
    }
    @Override
    protected void onResume() {
        super.onResume();
        Intent i = new Intent(loginAction.this, MainActivity.class);
        finish();
        overridePendingTransition(0, 0);
        startActivity(i);
        overridePendingTransition(0, 0);
    }
}
