package com.dictateeasy;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.dictateeasy.uploadActivity.hmap;


public class MainActivity extends AppCompatActivity {

    Button submitButton,dirButton;
    EditText deviceID;
    EditText prefix;
    EditText password;
    String login_url,passwordVal="",deviceIdVal="",selectedDirectory="";
    TextView folderArea;
    private static final int SDCARD_PERMISSION = 1,
            FOLDER_PICKER_CODE = 2,
            FILE_PICKER_CODE = 3;
    static RequestQueue requestQueue;
    static String version="1.1.6";
    //RequestQueue requestQueue;
    String server_url="http://54.190.247.51:3000/test1.html";
    public static final String Shared_prefs="sharedPrefs";
    public static final String isLoggedIn="isLoggedIn";
    public static final String registeredDriectories="registeredDirs";
    public static final Set<String> directorySet= new HashSet<String>();
    //constant for folder picker
    private static final int PICKFILE_REQUEST_CODE=100;
    private Boolean loginStatus=false;
    String username="";
    JSONObject objres;
    JSONObject jsonBodyObj ;
    Intent myFileIntent;
    static String uid="DC-22001";
    private static SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {



        uploadActivity instanceToChekcversion=new uploadActivity();
        instanceToChekcversion.checkSoftwareUpdate(this);
        Log.e("devicename",getDeviceName());
        requestQueue =Volley.newRequestQueue(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        submitButton=(Button)findViewById(R.id.submitButton);
        deviceID=(EditText) findViewById(R.id.deviceID);
        prefix=(EditText) findViewById(R.id.prefix);
        password=(EditText) findViewById(R.id.password);
        checkStoragePermission();
        login_url="http://54.190.247.51:3000/api/users/login";
        folderArea=(TextView)findViewById(R.id.folderArea);
        jsonBodyObj = new JSONObject();
        dirButton=(Button)findViewById(R.id.dirButton);
        // setting up data in pripor just to test
        // this needs to be come through api


        Log.e("loaddata","data loaded");
        loadData();
        submitButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                passwordVal=password.getText().toString();
                deviceIdVal=deviceID.getText().toString();

                submit();


            }
        });
        dirButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Intent intent = new Intent(getApplicationContext(), FolderPicker.class);
                //intent.putExtra("location", Environment.getDataDirectory());
                //startActivityForResult(intent,  FOLDER_PICKER_CODE);

                try {
                    Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    i.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivityForResult(Intent.createChooser(i, "Choose directory"), FOLDER_PICKER_CODE);
                }
                catch (Exception e)
                {
                    selectedDirectory="";
                }

            }
        });

        //loadData();
        //updateViews();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



    }
    void checkStoragePermission() {

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

    }





    public String getPath(MainActivity mainActivity, Uri uri)
    {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index =             cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {

        if (requestCode == FOLDER_PICKER_CODE) {

            // Log.i("Test", "Result URI " + intent.getData());
            try {
                Uri uri = intent.getData();
                Uri treeUri = intent.getData();
                String folderLocation = FileUtil.getFullPathFromTreeUri(treeUri, this);
                //File file=FileUtils.getFile(getApplicationContext(),uri);
                // String folderLocation =  intent.getExtras().getString("data");
                selectedDirectory=folderLocation;
                folderArea.setText(folderLocation);
            }
            catch (Exception e)
            {
                selectedDirectory="";
            }

            //folderArea.setText(intent.getData());
               /* if (resultCode == Activity.RESULT_OK && intent.hasExtra("data")) {
                    String folderLocation =  intent.getExtras().getString("data");
                    selectedDirectory=folderLocation;
                    folderArea.setText(folderLocation);
                    Log.i("selectedDir",selectedDirectory);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    selectedDirectory="";

                }*/

        }


        super.onActivityResult(requestCode, resultCode, intent);


    }
    public void openFile(String mimeType) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(mimeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        // if you want any file type, you can skip next line
        sIntent.putExtra("CONTENT_TYPE", mimeType);
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent chooserIntent;
        if (getPackageManager().resolveActivity(sIntent, 0) != null){
            // it is device with Samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Open file");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { intent});
        } else {
            chooserIntent = Intent.createChooser(intent, "Open file");
        }

        try {
            startActivityForResult(chooserIntent, PICKFILE_REQUEST_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "No suitable File Manager was found.", Toast.LENGTH_SHORT).show();
        }
    }

    //login_type 1 for manual login and 2 for qr code scan login
    private void submit() {
        //RequestQueue requestQueue =Volley.newRequestQueue(this);
        //  Log.e("deviceid",deviceIdVal.toString());
        //Log.e("bakchodi","bakchodi");
        if(selectedDirectory.trim()=="")
        {
            String recordPath = this.getExternalFilesDir("/").getAbsolutePath();
            selectedDirectory=recordPath;
        }
        else
        {
            String recordPath = this.getExternalFilesDir("/").getAbsolutePath();
            selectedDirectory=selectedDirectory+","+recordPath;
        }
        Map<String, String> postParam = new HashMap<String, String>();

        postParam.put("id", deviceIdVal);
        postParam.put("password", passwordVal);

        if(deviceIdVal.toString().equals("") || (deviceIdVal.toString().trim()==null)) {
            Toast.makeText(getApplicationContext(), "Device ID can't be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(prefix.getText().toString().equals("") || (prefix.getText().toString().trim()==null))
        {
            Toast.makeText(getApplicationContext(),"Please enter a prefix.",Toast.LENGTH_SHORT).show();
            return;
        }
        if(passwordVal.toString().equals("") || (passwordVal.toString().trim()==null))
        {
            //do nothing
            Toast.makeText(getApplicationContext(),"Password field can't be empty.",Toast.LENGTH_SHORT).show();
            return;
        }

        if(selectedDirectory.trim()=="")
        {

            Toast.makeText(getApplicationContext(),"Please select an upload directory.",Toast.LENGTH_SHORT).show();
            return;
        }
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                login_url, new JSONObject(postParam),
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        parseData(response.toString(),selectedDirectory);
                        Log.d("serverresponse", response.toString());
                        //  msgResponse.setText(response.toString());
                        //hideProgressDialog();
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
    public void parseData(String response,String selectedFolder) {
        try {

            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.getString("success").equals("1")) {

                JSONObject dataobj = jsonObject.getJSONObject("data");
                //JSONArray dataArray = jsonObject.getJSONArray("data");
                //folderArea.setText(dataobj.getString("username"));

                Log.i("success_val",dataobj.getString("id")+"ramu kaka");
                if(!selectedFolder.trim().equals(""))
                {
                    saveData(true,dataobj.getString("username"),selectedDirectory,dataobj.getString("user_id"),prefix.getText().toString(),dataobj.getString("id"));
                    loadData();
                    Toast.makeText(getApplicationContext(),"Login Successfull",Toast.LENGTH_SHORT).show();

                }



                //Intent intent = new Intent(MainActivity.this,HobbyActivity.class);
                //startActivity(intent);
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Please enter correct username and password.", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //set data in shared reference and make them private
    public void saveData(Boolean loginStatus,String username,String selectedDirectory, String deviceID,String prefix,String uid)
    {
        sharedPreferences=getSharedPreferences(Shared_prefs,MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean("panadem.loginStatus", loginStatus);
        editor.putString("panadem.username",username);
        editor.putString(" panadem.deviceId",deviceID);
        editor.putString("panadem.selectedDirectory",selectedDirectory);
        editor.putString("panadem.prefix",prefix);
        editor.putString("panadem.uid",uid+"");
        Log.i("selecteddirectory",selectedDirectory);
        editor.commit();
    }
    public void loadData(){
        sharedPreferences=getSharedPreferences(Shared_prefs,MODE_PRIVATE);
        loginStatus=sharedPreferences.getBoolean("panadem.loginStatus",false);
        if(loginStatus) {
            if (sharedPreferences.contains("panadem.username") && !sharedPreferences.getString("panadem.username", "").equals("")) {
                username = sharedPreferences.getString("panadem.username", "");

                //folderArea.setText("login was successfull"+ sharedPreferences.getString(" panadem.deviceId", ""));
                startActivity(new Intent(this, AppAction.class));
                Log.e("appactionActivityintent","mainsctivity");
                finish();
            }
        }
        Log.e("saved_data", username+" "+loginStatus);
    }


    public void updateViews(){
        deviceID.setText(loginStatus.toString());
    }






    String result="";
    public static void get_String(String absolutePath,String generatedFileName,int bhasad_var,final VolleyCallback callback) {
        String server_file_size="-1";
        String url_string="";
        File file=new File(absolutePath);


        String user_id=uid=sharedPreferences.getString("panadem.uid","");
        String tag_string_req = "string_raq";
        if(bhasad_var==9999)
        {
            url_string="http://upload.panadem.com/testapp/fileSize?uid="+user_id+"&fileName="+"1-"+file.getName()+"&last_modified="+file.lastModified()+"&generatedFileName="+generatedFileName;

        }
        else
        {
            if(generatedFileName.trim().equals(""))
            {
                url_string="http://upload.panadem.com/testapp/fileSize?uid="+user_id+"&fileName="+hmap.get(file.getName())+"-"+file.getName()+"&last_modified="+file.lastModified()+"&generatedFileName="+generatedFileName;
            }
            else
            {
                url_string="http://upload.panadem.com/testapp/fileSize?uid="+user_id+"&fileName="+hmap.get(file.getName())+"-"+file.getName()+"&last_modified="+file.lastModified()+"&generatedFileName="+hmap.get(file.getName())+"-"+generatedFileName;
            }
        }
        Log.e("size url",url_string);

        StringRequest strReq = new StringRequest(Request.Method.GET, url_string, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //result=response;
                Log.e("response",response);
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println(volleyError.getMessage());
            }
        });
        requestQueue.add(strReq);
        //return result;
    }
    public interface VolleyCallback{
        void onSuccess(String result);
    }
    public static long getFileSize(String absolutePath,String generatedFileName,int bhasad_var) throws MalformedURLException, IOException
    {
        //System.out.println("file count"+bhasad_var);
        // creates a unique boundary based on time stamp
        //System.out.println(absolutePath);
        String server_file_size="-1";
        String url_string="";





        HttpURLConnection httpConn;
        OutputStream outputStream;
        PrintWriter writer;
        // URL url = new URL("http://upload.panadem.com/testappfileSize?uid="+uid);
        File file=new File(absolutePath);
        if(bhasad_var==9999)
        {
            url_string="http://upload.panadem.com/testapp/fileSize?uid="+uid+"&fileName="+"1-"+file.getName()+"&last_modified="+file.lastModified()+"&generatedFileName="+generatedFileName;

        }
        else
        {
            if(generatedFileName.trim().equals(""))
            {
                url_string="http://upload.panadem.com/testapp/fileSize?uid="+uid+"&fileName="+hmap.get(file.getName())+"-"+file.getName()+"&last_modified="+file.lastModified()+"&generatedFileName="+generatedFileName;
            }
            else
            {
                url_string="http://upload.panadem.com/testapp/fileSize?uid="+uid+"&fileName="+hmap.get(file.getName())+"-"+file.getName()+"&last_modified="+file.lastModified()+"&generatedFileName="+hmap.get(file.getName())+"-"+generatedFileName;
            }
        }
        Log.e("url",url_string);
        String boundary = "===" + System.currentTimeMillis() + "===";

        URL url = new URL(url_string);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setDoOutput(true); // indicates POST method
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
        int status = httpConn.getResponseCode();


        if (status == HttpURLConnection.HTTP_OK) { // success
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpConn.getInputStream()));
            while (reader.readLine() != null) {
                server_file_size = reader.readLine();

            }
            reader.close();
            httpConn.disconnect();
            //System.out.println(server_file_size+"bkl file size");
            try {
                if (server_file_size == null || server_file_size.equals("null")) {
                    reader.close();
                    httpConn.disconnect();
                    return Long.parseLong("-1");

                }


            } catch (NullPointerException e) {

                // Panadem.jp_progress.UpdateProgress(0);
                //Panadem.jp_progress.repaint();
                //System.out.println("hi7");
                // e.printStackTrace();
                uploadActivity.is_button_enabled = true;
                uploadActivity.is_upload_started = 0;
                //jLabel5.setText(jLabel5.getText().replaceAll(" \\d+ ", " 0 "));
                //jLabel5.paintImmediately(Panadem.jLabel5.getVisibleRect());
                Log.e("connection error", "<html><div style='color:#FFFFFF;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>Connection error.</DIV></HTML>");
                //jLabel4.paintImmediately(Panadem.jLabel4.getVisibleRect());
                Log.e("connection error", "<html><div style='color:white;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>" + " Please try again for remaining files. </DIV></HTML>");
                //Panadem.jLabel8.paintImmediately(Panadem.jLabel8.getVisibleRect());


                return Long.parseLong("-1");
            }
        }



        return  Long.parseLong(server_file_size);
    }
    /* public static void setUserId(String uid) throws MalformedURLException, IOException
            {

        // creates a unique boundary based on time stamp
       //System.out.println("setting userid");
        HttpURLConnection httpConn;
        OutputStream outputStream;
        PrintWriter writer;
        URL url = new URL("https://upload.panadem.com/testapp/testapp//set_user");
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setDoOutput(true); // indicates POST method
        httpConn.setDoInput(true);
       outputStream = httpConn.getOutputStream();
        OutputStreamWriter output_writer = new OutputStreamWriter(outputStream);
        output_writer.write("uid="+uid);
        output_writer.flush();
        BufferedReader br = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
        //System.out.println("yha he output "+br.readLine());

    }*/
    public static String getVersion() throws NoSuchAlgorithmException, KeyManagementException
    {

        // creates a unique boundary based on time stamp
        try
        {


            // //System.out.println("getting version");
            HttpURLConnection httpConn;
            OutputStream outputStream;
            PrintWriter writer;
            URL url = new URL("http://upload.panadem.com/testapp/androidVersion");
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setDoOutput(true); // indicates POST method
            httpConn.setDoInput(true);
            outputStream = httpConn.getOutputStream();
            OutputStreamWriter output_writer = new OutputStreamWriter(outputStream);
            output_writer.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));

            return  br.readLine();

        }
        catch(java.net.NoRouteToHostException ex)
        {
            //System.out.println("hi35");
            uploadActivity.is_button_enabled=true;
            uploadActivity.is_upload_started=0;
            return("not_connected");
        }
        catch(java.net.ConnectException ex)
        {
            //System.out.println("hi36");
            uploadActivity.is_button_enabled=true;
            uploadActivity.is_upload_started=0;
            return("not_connected");
        }
        catch(java.io.FileNotFoundException ex)
        {
            //System.out.println("hi37");
            uploadActivity.is_button_enabled=true;
            uploadActivity.is_upload_started=0;
            return("not_found");
        }
        catch(java.net.UnknownHostException ex)
        {
            //System.out.println("hi38");
            uploadActivity.is_button_enabled=true;
            uploadActivity.is_upload_started=0;
            return("not_connected");
        }
        catch(java.net.SocketTimeoutException ex)
        {
            return("not_connected");
        }
        catch(IOException e)
        {
            //System.out.println("hi39");
            uploadActivity.is_button_enabled=true;
            uploadActivity.is_upload_started=0;
            e.printStackTrace();
        }

        return "hi";
    }
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        }
        return manufacturer + " " + model;
    }
}

