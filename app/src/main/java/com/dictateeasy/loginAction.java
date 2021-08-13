package com.dictateeasy;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import com.android.volley.toolbox.StringRequest;
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
    private String loginURL= "http://54.190.247.51:3000/api/users/UUID_login";
    private String updateDeviceCountURL="http://54.190.247.51:3000/api/users/update_device_prefix";
    static RequestQueue requestQueue;
    public static final String Shared_prefs="sharedPrefs";
    private static SharedPreferences sharedPreferences;
    private Boolean loginStatus=false;
    String username="";
    String deviceID="";
    String prefix="";
    String userID="";
    //code 100 for file picker
    final int FOLDER_PICKER_CODE=100;
    //qr code scanner object
    private IntentIntegrator qrScan;
    VideoView vid;
    MediaPlayer mMediaPlayer;
    int mcurrentvideoPosition;
    String deviceName="";
    protected void onCreate(Bundle savedInstanceState) {


        //allow storage permission
        Log.e("check_hua","yes");
        uploadActivity instanceForVersionUpdate=new uploadActivity();
        instanceForVersionUpdate.checkSoftwareUpdate(loginAction.this);


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
        deviceName= Build.MODEL;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        try {
            Log.e("folder_code",FOLDER_PICKER_CODE+"");
            if (requestCode == FOLDER_PICKER_CODE) {

               /* if (intent.hasExtra("username") && intent.hasExtra("deviceId") && intent.hasExtra("prefix") && intent.hasExtra("uid")) {
*/
                       Uri uri = intent.getData();
                       Uri treeUri = intent.getData();
                       String folderLocation = FileUtil.getFullPathFromTreeUri(treeUri, this);
                       String recordPath = this.getExternalFilesDir("/").getAbsolutePath();
                       if(folderLocation.trim()=="")
                       {
                           folderLocation=recordPath;
                       }
                       else
                       {
                           folderLocation=folderLocation+","+recordPath;
                       }
                Log.e("login_details",(!username.equals("") && !deviceID.equals("") && !prefix.equals("") && !userID.equals(""))+"");
                    if(!username.equals("") && !deviceID.equals("") && !prefix.equals("") && !userID.equals("") && !folderLocation.equals(""))
                    {

                        saveData(true, username, folderLocation, deviceID, prefix, userID);
                        loadData();
                        Toast.makeText(getApplicationContext(), "Login Successfull", Toast.LENGTH_SHORT).show();
                    }


               /* } else if (resultCode == Activity.RESULT_CANCELED) {
                    //String selectedDirectory="";

                    startActivity(new Intent(this, loginAction.class));
                }*/
                else
                {
                    Log.e("xxxx","else chala");
                }
                super.onActivityResult(requestCode, resultCode, intent);
            } else {

                Log.e("elseran","else"+intent.getExtras().getString("username"));
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
                if (result != null) {
                    //if qrcode has nothing in it
                    if (result.getContents() == null) {
                        Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
                    } else {
                        //if qr contains data
                        Log.e("scanner_data", result.getContents());
                        submit(result.getContents());

                    }
                } else {

                    super.onActivityResult(requestCode, resultCode, intent);
                }
            }
        }
        catch(Exception e)
        {
            SharedPreferences sharedPreferences = getSharedPreferences(Shared_prefs, MODE_PRIVATE);
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putBoolean("panadem.loginStatus", false);
        }
    }
    //login_type 1 for manual login and 2 for qr code scan login
    private void submit(String UUID) {
        //RequestQueue requestQueue =Volley.newRequestQueue(this);
        //update qr code on panadem
       // https://panadem.com/portal/users/qr_reset
        //updateQROnServer();
        Map<String, String> postParam = new HashMap<String, String>();

        postParam.put("UUID", UUID);

        Log.e("UUID_value",UUID);


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
                //Log.e("reponse_string",response.toString)
                Log.e("errrortype","login_error");
                //VolleyLog.d("errorresponse", error.toString());
                error.printStackTrace();
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
    public void  updateQROnServer(final String client_id)
    {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://panadem.com/portal/users/qr_reset",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("response_data",response);
                        Toast.makeText(loginAction.this,response,Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(loginAction.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("client_id",client_id);

                return params;
            }

        };
        requestQueue.add(stringRequest);
    }
    public void parseData(String response) {
        try {

            JSONObject jsonObject = new JSONObject(response);
               Log.e("device name",deviceName);
            if (jsonObject.getString("success").equals("1")) {
                JSONObject dataobj = jsonObject.getJSONObject("data");
                Log.i("success_val",dataobj.getString("id")+"ramu kaka");
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
                if(deviceName.equals("psp2100"))
                {



                    Log.e("generatedprefix",generatedPrefix);
                   // string path
                    //get android directory path to check if a speech app is present on the devidce
                    //getListFiles(Environment.getExternalStorageDirectory());

                   // File rootDataDir = getActivity().getFilesDir();
                    //Log.e("root directory",pathToSearch.substring(0,pathToSearch.lastIndexOf("com.dictateeasy")));
                   // String Path="/storage/A2AF-14EF/Android/data/com.speech/files/dictations";
                    String Path= Environment.getExternalStorageDirectory()+"/Android/data/com.speech/files/dictations";
                    Log.e("pathtosearch",Path);
                    if(new File(Path).exists())
                    {
                        String recordPath = this.getExternalFilesDir("/").getAbsolutePath();
                        Path=Path+","+recordPath;
                        Log.e("selecteddir","directory found");
                        saveData(true,dataobj.getString("username"),Path,dataobj.getString("user_id"),generatedPrefix,dataobj.getString("id"));
                        loadData();
                        Toast.makeText(getApplicationContext(),"Login Successfull",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        //Intent intent = new Intent(getApplicationContext(), FolderPicker.class);
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

                        Log.e("qrcodedata",dataobj.getString("username")+dataobj.getString("user_id"));
                        intent.putExtra("username",dataobj.getString("username"));
                        intent.putExtra("deviceId",dataobj.getString("user_id"));
                        intent.putExtra("prefix",generatedPrefix);
                        intent.putExtra("uid",dataobj.getString("id"));
                           username=dataobj.getString("username");
                           deviceID=dataobj.getString("user_id");
                           prefix=generatedPrefix;
                           userID=dataobj.getString("id");
                        startActivityForResult(intent, FOLDER_PICKER_CODE);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        // startActivityForResult(intent,  FOLDER_PICKER_CODE);
                    }
                    /*String selectedDirectory=
                    */
                }
                else
                {
                    //if device is not psp register directly and pick files from the app recordings
                    String recordPath = this.getExternalFilesDir("/").getAbsolutePath();
                    Log.e("selecteddir","directory found");
                    saveData(true,dataobj.getString("username"),recordPath,dataobj.getString("user_id"),generatedPrefix,dataobj.getString("id"));
                    loadData();
                    Toast.makeText(getApplicationContext(),"Login Successfull",Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(), "We are unable to fetch detials for login. Please try again.", Toast.LENGTH_SHORT).show();

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
        updateQROnServer(uid);
        editor.commit();
    }
    public void loadData(){
        sharedPreferences=getSharedPreferences(Shared_prefs,MODE_PRIVATE);
        Log.e("selected_directory",sharedPreferences.getString("panadem.selectedDirectory", ""));
        File selectedFolder = new File(sharedPreferences.getString("panadem.selectedDirectory", ""));
        Log.e("loaded_data",selectedFolder.exists()+"");
        loginStatus=sharedPreferences.getBoolean("panadem.loginStatus",false);
        if(loginStatus) {
            if (sharedPreferences.contains("panadem.username") && !sharedPreferences.getString("panadem.username", "").equals("")) {
                username = sharedPreferences.getString("panadem.username", "");

                //folderArea.setText("login was successfull"+ sharedPreferences.getString(" panadem.deviceId", ""));
                startActivity(new Intent(this, MainActivity.class));
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
            Log.e("update_prefix",id+prefix);
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
    //handler if device is not psp or unable to find path

}
