package com.panadem;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class uploadActivity extends AppCompatActivity {
    protected static String supported_file_type;
    protected static SharedPreferences sharedPreferences;
    private static AsyncResponse asyncResponse;
   // public AsyncResponse asyncResponse;
    //check login status
    //variable for success file count
   String apk_fileName="app-debug.apk";
    String update_url;
    boolean loginStatus = false;
    static String userId;
    static String selectedDirectories = "";
    String username = "";
    File selectedFolder;
    static TextView fileCount,username_field;
    static int audioFileCount;
    static Button uploadButton,update_button;
    static connectionDetector mConnectionDetector;
    static boolean isConnected = false;
    String ext_url;
    protected static boolean is_button_enabled = true;
    static TextView progerssText;
    //java app variables
    //variable for success file count
    static Dialog dialog;
    static int file_number = 0;
    //variable for failed file count
    static int failed_file_count = 0;
    static String version = "Panadem sync Version 1.1.3";
    static int moved_file_count = 0;
    static final int BUFFER_SIZE = 16384;
    static int relevant_file = 0;
    static int present_file = 0;
    int flag;
    static String file_affix;
    static String file_name = ",";
    static String file_path = ",";
    protected static int is_upload_started = 0;
    //static String uid = "DC-22001";
    static String suffix = "hard_coded_suffix";
    static String supported_file_extensions;
    static String extensions = "";
    public static TextView message_box;
    static HashMap<String, Integer> hmap = new HashMap<String, Integer>();
    static HashMap<String, String> missMap = new HashMap<String, String>();
    private static String prefix;
    private static String uid;
   public static ProgressBar progressBar;
    //static int is_upload_started=0;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkSoftwareUpdate(this);
        Log.e("uploadactivitycall","upload activity oncreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        //asyncResponse=new AsyncResponse() ;
        //this url will give allowed extensions on post request
        update_url="http://staging.thornbills.com/app-debug.apk";
        ext_url = "http://54.190.247.51:3000/api/users/supported_extensions";
        sharedPreferences = getSharedPreferences(MainActivity.Shared_prefs, MODE_PRIVATE);
        fileCount = (TextView) findViewById(R.id.fileCount);
        username_field=(TextView)findViewById(R.id.username);
        username_field.setText("Welcome "+sharedPreferences.getString("panadem.username","").replaceAll("_"," "));
        //message_box=(TextView)findViewById(R.id.message_box);


        mConnectionDetector = new connectionDetector(getApplicationContext());
        isConnected = mConnectionDetector.isConnectingToInternet();
        supported_file_type = "mp3";
        audioFileCount = 0;
        uploadButton = (Button) findViewById(R.id.uploadButton);
        uploadButton.setEnabled(false);

        loginStatus = sharedPreferences.getBoolean("panadem.loginStatus", false);
        username = sharedPreferences.getString("panadem.username", "");
        selectedDirectories = sharedPreferences.getString("panadem.selectedDirectory", "");
        userId=sharedPreferences.getString(" panadem.userId", "");
        prefix=sharedPreferences.getString("panadem.prefix","");
        uid=sharedPreferences.getString("panadem.uid","");
        supported_file_extensions = "";

        Log.e("block 0", "selected dir block");
        if (!selectedDirectories.equals("")) {
            Log.e("block 1", "selected dir block");
            if (isConnected) {
                //no connection- do something
                getSupportedFileCount(1);

            } else {
                fileCount.setText("no connection");
                //there is connection
            }


        }
        //  if(loginStatus==true && !username.equals("") && !directory)
        //upload action on uplaod click
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //audio_file_count=0;

                getSupportedFileCount(1);
                // 1 means normal operaion to count and show
                is_button_enabled = false;
                is_upload_started = 1;
                Toast.makeText(getApplicationContext(), extensions, Toast.LENGTH_SHORT).show();

            }

        });


        //function to move files to http

        //end of on click of upload

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        // MainActivity mainActivity=new

    }
    String WEBSERVICE_URL="http://upload.panadem.com/testapp/Version";
    String latest_versionCode="";
    String TAG="update_software";
     void checkSoftwareUpdate(final Context context){
        try {
            Log.e("function_called",isNetworkAvailable(context)+"");
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            Context activityContext=context;
            if (isNetworkAvailable(context)) {
                Log.e("url",WEBSERVICE_URL);
                StringRequest stringRequest = new StringRequest(Request.Method.POST,
                        WEBSERVICE_URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                //Log.d(TAG, response);
                                JSONObject json = null;
                                try {
//                                    json = new JSONObject(response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1));
                                        String upadte_url="";
                                        if (version != null) {

                                            latest_versionCode = response;
                                            Log.e("current_version",MainActivity.version);
                                            if(!response.contains(MainActivity.version))
                                            {
                                                Log.e("controller","reached");
                                                Intent x = new Intent(context, downloadNewVersion.class);
                                                startActivity(x);
                                            }
                                           // Intent x = new Intent(uploadActivity.this, downloadNewVersion.class);
                                            //startActivity(x);


                                        }

                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing data " + e.toString());
                                }
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Error: " + error.getMessage());
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("action", "checkSoftwareUpdate");
                        return params;
                    }
                };
                requestQueue.add(stringRequest);
            }
        }catch (RuntimeException e) {
            e.printStackTrace();
        }

    }

    private void downloadUpdate(final Context context){
        try{
            final File apk_file_path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), apk_fileName);
            if (apk_file_path.exists()) apk_file_path.delete();

            Log.e("software_update",update_url);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(update_url));
            //request.setDescription(context.getString(R.string.update_version));
            request.setTitle(context.getString(R.string.app_name));
            //set destination
            final Uri uri = Uri.parse("file://" + apk_file_path);
            request.setDestinationUri(uri);

            // get download service and enqueue file
            final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            final long downloadId = manager.enqueue(request);

           // downloadAppProgess.setVisibility(View.VISIBLE);
           // update_now.setVisibility(View.GONE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean downloading = true;
                    while(downloading) {
                        DownloadManager.Query q = new DownloadManager.Query();
                        q.setFilterById(downloadId);
                        Cursor cursor = manager.query(q);
                        cursor.moveToFirst();

                        final int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                            downloading = false;
                        }
                        int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        if (bytes_total != 0) {
                            final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);
                             runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                  //  downloadAppProgess.setProgress((int) dl_progress);
                                    Log.e("download progress",(int) dl_progress+"");
                                }
                            });
                        }
                        cursor.close();

                    }
                }
            }).start();


            //set BroadcastReceiver to install app when .apk is downloaded
            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {
                    //BroadcastReceiver on Complete
                    if (apk_file_path.exists()) {
                        Log.e("apk_file_path",apk_file_path.toString());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                           /* Intent i = new Intent();
                            i.setAction(Intent.ACTION_VIEW);
                            i.setDataAndType(Uri.fromFile(new File(String.valueOf(apk_file_path))), "application/vnd.android.package-archive" );
                            Log.d("Lofting", "About to install new .apk");
                            context.startActivity(i);*/

                            /*Uri apkUri = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),BuildConfig.APPLICATION_ID + ".provider", new File(String.valueOf(apk_file_path)));
                            intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                            intent.setData(apkUri);
                            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);*/
                        } else {
                            Uri apkUri = Uri.fromFile(apk_file_path);
                            intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(apkUri, manager.getMimeTypeForDownloadedFile(downloadId));
                            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }

                    }else{
                      //  AppController.setAlert(context, "Something went wrong");
                        //downloadAppProgess.setVisibility(View.GONE);
                        //update_now.setVisibility(View.VISIBLE);
                        Log.e("error" ,"Something went wrong" );
                    }
                    context.unregisterReceiver(this);
                }
            };
            context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        }catch (Exception e){
            e.printStackTrace();
           // downloadAppProgess.setVisibility(View.GONE);
            //update_now.setVisibility(View.VISIBLE);
        }

    }

    //check internet connectivity
    public static boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
        if (activeNetworkInfo != null) { // connected to the internet
            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                return true;
            } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                return true;
            }
        }
        Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
        return false;
    }

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
               // Log.v(TAG,"Storage permission is granted");
                return true;
            } else {
                //Log.v(TAG,"Storage permission is revoked");
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }else {
            //permission is automatically granted on sdk<23 upon installation
           // Log.v(TAG,"Storage permission is granted");
            return true;
        }
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    public void getSupportedFileCount(final int Flag) {
        // Flag=1 means normal operaion to count and show
        Log.e("listf called", Flag + "");
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
// To dismiss the dialog

        RequestQueue requestQueue = Volley.newRequestQueue(this);


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                ext_url,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {


                            JSONObject dataobj = response.getJSONObject("extension");
                            //JSONArray dataArray = jsonObject.getJSONArray("data");
                            if (Flag == 1) {
                                String Folders[] = selectedDirectories.split(",");
                                audioFileCount = 0;
                                for (int folder_count = 0; folder_count < Folders.length; folder_count++) {
                                    File selectedFolder = new File(Folders[folder_count]);
                                    Log.e("uploading from",Folders[folder_count]);
                                    if (selectedFolder.exists()) {
                                        //no connection- do something
                                        supported_file_extensions = dataobj.getString("allowed_extensions");
                                        audioFileCount = audioFileCount + listf(Folders[folder_count], audioFileCount, dataobj.getString("allowed_extensions"));
                                    }
                                }
                                //TextView fileCounterView=findViewById(R.id.fileCount);

                                fileCount.setText("You Have " + audioFileCount + " new files to upload");
                                uploadButton.setEnabled(true);
                                uploadButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        String Folders[] = selectedDirectories.split(",");
                                        audioFileCount = 0;
                                        hmap.clear();

                                        for (int folder_count = 0; folder_count < Folders.length; folder_count++) {
                                            File selectedFolder = new File(Folders[folder_count]);

                                            if (selectedFolder.exists()) {
                                                //create hashmap
                                                audioFileCount = audioFileCount + listf(Folders[folder_count], audioFileCount, supported_file_extensions);
                                                Log.e("filecount",audioFileCount+"");
                                                //System.out.println("hash map button click");
                                            }
                                        }
                                        relevant_file=audioFileCount;
                                        if(audioFileCount==0)
                                        {
                                            Toast.makeText(getApplicationContext(), "You don't have any file to upload. "+userId, Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        Set set = hmap.entrySet();
                                        Iterator iterator = set.iterator();
                                        while (iterator.hasNext()) {
                                            Map.Entry mentry = (Map.Entry) iterator.next();
                                            Log.e("hash map print","key is: " + mentry.getKey() + " & Value is: "+mentry.getValue());
                                            //System.out.println();
                                        }
                                        //open update dialog on top of everything
                                        dialog = new Dialog(uploadActivity.this);
                                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                        dialog.setCancelable(false);
                                        dialog.setContentView(R.layout.dialog);
                                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialogInterface) {
                                                Log.e("listener call","dismiss listener");
                                                refreshActivity();
                                            }
                                        });
                                        progressBar=(ProgressBar) dialog.findViewById(R.id.progress_horizontal);;
                                        progerssText=dialog.findViewById(R.id.value123);
                                        dialog.show();
                                        Window window = dialog.getWindow();
                                        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

                                        for (int folder_count = 0; folder_count < Folders.length; folder_count++) {
                                            File selectedFolder = new File(Folders[folder_count]);
                                            if (selectedFolder.exists()) {
                                                //create hashmap
                                               // audioFileCount = audioFileCount + listf(Folders[folder_count], audioFileCount, supported_file_extensions);
                                                Log.e("hash map button click",userId);

                                                int x= 0;
                                                try {
                                                    x = move_files_to_http(Folders[folder_count],userId);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                present_file=x;
                                               // suffix=file_data.substring(file_data.indexOf("<suffix>")+8,file_data.indexOf("</suffix>"));
                                                if(x==0)
                                                {
                                                    //System.out.println("yha haga");
                                                   // Panadem.is_button_enabled=true;Panadem.is_upload_started=0;
                                                    //Panadem.jLabel4.setText("<html><div style='color:#FFFFFF;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>You have 0 files to upload.</div></html>");
                                                    //Panadem.jLabel4.paintImmediately(Panadem.jLabel4.getVisibleRect());
                                                }

                                            }
                                        }





                                        Toast.makeText(getApplicationContext(), "Initiating your file upload. "+userId, Toast.LENGTH_SHORT).show();

                                    }
                                });
                                progress.dismiss();

                            }
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                progress.dismiss();
                fileCount.setText("We couldn't fetch the data from Server. Please restart the application.");
                error.printStackTrace();
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


    public static int move_files_to_http(String directoryName,String DevId) throws IOException {
        Log.e("function call check","move function called");
        String uploadURL="http://upload.panadem.com/testapp/UploadServlet?uid="+uid;
        File directory = new File(directoryName);
        File[] fList = directory.listFiles();
        List<File> resultList = new ArrayList<File>();
        //int relevant_file=listf(directoryName);
        //System.out.println("mvoe function called");
        file_number=0;
        failed_file_count=0;
        // get all the files from a directory
        //connect FTP to serve
        for (File nfile : fList) {

            if (nfile.isFile()) {
                if(supported_file_type.indexOf(nfile.getName().substring(nfile.getName().lastIndexOf(".") + 1))>-1)
                {
                   // relevant_file++;
                }

            } else if (nfile.isDirectory() && nfile.getName().indexOf(".")!=0) {
                move_files_to_http(nfile.getAbsolutePath(),DevId);
            }
        }
        Log.e("relevent file",relevant_file+"");
        if(relevant_file>0)
        {
            Log.e("relevent file","inside if");
            //if(ftpClient.cwd(DevId+"")==550)
            //  jLabel4.setText("directory doesn't exist");
            //ftpClient.logout();
            //ftpClient.disconnect();
            try{

                int i=0;
                resultList.addAll(Arrays.asList(fList));
                /*set initial value to 1% to show that application is working*/
               // Panadem.jp_progress.setVisible(true);
                //Panadem.jp_progress.UpdateProgress((int)(1));
                // is_button_enabled=true;
                //Panadem.jp_progress.repaint();
                //Panadem.jp_progress.paintImmediately(Panadem.jp_progress.getVisibleRect());
                for (File file : fList) {
                   // Log.e("supported_type",supported_file_extensions);
                    if (file.isFile()) {
                        //System.out.println(file.getName()+"call from move function");
                        if(supported_file_extensions.indexOf(file.getName().substring(file.getName().lastIndexOf(".") + 1))>-1 && file.getName().indexOf(".")!=0)
                        {
                            String xmlFileName=file.getName()+".xml";
                            String jpegFileName=file.getName().split("[.]")[0]+"-0.jpeg";
                            File xmlFile=new File(directoryName+"/"+xmlFileName);
                            File jpegFile=new File(directoryName+"/"+jpegFileName);
                            String generatedFileName="";
                            if(xmlFile.exists())
                            {
                                //System.out.println("xmlfile found");
                                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                                try {
                                    Document doc = dBuilder.parse(xmlFile);
                                    doc.getDocumentElement().normalize();
                                    NodeList nList = doc.getElementsByTagName("DictationProperties");
                                    //System.out.println(nList.getLength());
                                    generatedFileName = "";
                                    for (int temp = 0; temp < nList.getLength(); temp++) {
                                        Node nNode = nList.item(temp);
                                        ////System.out.println("\nCurrent Element :" + nNode.getNodeName());

                                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                                            Element eElement = (Element) nNode;
                                            if (eElement.getElementsByTagName("PropertyIdentifier").item(0).getTextContent().contains("DPMBarcode")) {
                                                try {
                                                    generatedFileName = eElement.getElementsByTagName("patientId").item(0).getTextContent() + "_" + eElement.getElementsByTagName("patientName").item(0).getTextContent() + "_" + eElement.getElementsByTagName("patientSex").item(0).getTextContent() + "_" + eElement.getElementsByTagName("patientAge").item(0).getTextContent() + "_" + eElement.getElementsByTagName("patientDOB").item(0).getTextContent() + "_" + eElement.getElementsByTagName("patientDOI").item(0).getTextContent() + "_" + eElement.getElementsByTagName("patientDOS").item(0).getTextContent();
                                                    generatedFileName = generatedFileName.replaceAll("([^\\p{L}\\p{N}_])", "-");
                                                } catch (Exception e) {
                                                    generatedFileName = "";
                                                }
                                            }


                                        }
                                    }
                                }
                                //parser failed in parsing the file
                                catch (Exception e)
                                {
                                    if(xmlFile.length()>0)
                                    {
                                        String content=getFileContent(directoryName+"/"+xmlFileName);
                                        Log.e("file content",content );
                                    }
                                }

                            }
                            i++;
                            //System.out.println(file.getName()+" "+file.length());
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                            SimpleDateFormat file_affix=new SimpleDateFormat("yyyyMMddhhmmss");
                            Date date = new Date();
                            String generatedAudioName="";
                            if(!generatedFileName.trim().equals(""))
                            {
                                generatedAudioName= generatedFileName+"."+file.getName().substring(file.getName().lastIndexOf(".") + 1);
                            }
                            try {
                                try
                                {
                                    File uploadFile=file;
                                    long fileSize = uploadFile.length();
                                    //set file name according to xml
                                    uploadURL="http://upload.panadem.com/testapp/UploadServlet?counter="+hmap.get(uploadFile.getName())+"&uid="+uid+"&last_modified="+uploadFile.lastModified()+"&suffix="+prefix+"&generatedFileName="+ generatedAudioName;
                                    Log.e("upload url",uploadURL);
                                    //System.out.println(generatedFileName);

                                    UploadTask task = new UploadTask(uploadURL, uploadFile,i,relevant_file,1, generatedAudioName,moved_file_count,directoryName,asyncResponse);
                                    //System.out.println("upload url: "+uploadURL);
                                    //task.addPropertyChangeListener(this);

                                    task.execute();

                                } catch (Exception ex) {
                                    //System.out.println("hi32");
                                   // Panadem.is_button_enabled=true;is_upload_started=0;
                                    //jLabel4.setText("<html><div style='color:red;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>Oops! Something went wrong.</div></html>");
                                    //jLabel4.paintImmediately(jLabel4.getVisibleRect());

                                    //System.out.println("file transfer unsuccessfull");


                                }

                            }
                            catch (Exception ex)
                            {
                                //System.out.println("hi33");
                                is_button_enabled=true;is_upload_started=0;
                                //jLabel4.setText("<html><div style='color:red;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>Oops! Something went wrong.</div></html>");
                                //jLabel4.paintImmediately(jLabel4.getVisibleRect());

                                ex.printStackTrace();
                            }
                            moved_file_count++;

                            ////System.out.println(moved_file_count);
                            if(jpegFile.exists())
                            {
                                // i++;
                                //System.out.println(jpegFile.getName()+" "+file.length());
                                String generatedJPEGName="";
                                if(!generatedFileName.trim().equals(""))
                                {
                                    generatedJPEGName= generatedFileName+".jpeg";
                                }


                                try {
                                    try
                                    {
                                        File uploadFile=jpegFile;
                                        long fileSize = uploadFile.length();
                                        uploadURL="http://upload.panadem.com/testapp/UploadServlet?counter="+hmap.get(uploadFile.getName())+"&uid="+uid+"&last_modified="+uploadFile.lastModified()+"&suffix="+prefix+"&generatedFileName="+generatedJPEGName;
                                        UploadTask task = new UploadTask(uploadURL, uploadFile,i,relevant_file,1,generatedJPEGName,moved_file_count,supported_file_extensions,asyncResponse);
                                        //System.out.println("upload url: "+uploadURL);
                                        //task.addPropertyChangeListener(this);

                                        task.execute();

                                    } catch (Exception ex) {
                                        //System.out.println("hi32");
                                      //  Panadem.is_button_enabled=true;is_upload_started=0;
                                       // jLabel4.setText("<html><div style='color:red;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>Oops! Something went wrong.</div></html>");
                                        //jLabel4.paintImmediately(jLabel4.getVisibleRect());

                                        //System.out.println("file transfer unsuccessfull");


                                    }

                                }
                                catch (Exception ex)
                                {
                                    //System.out.println("hi33");
                                   // is_button_enabled=true;is_upload_started=0;
                                    //jLabel4.setText("<html><div style='color:red;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>Oops! Something went wrong.</div></html>");
                                    //jLabel4.paintImmediately(jLabel4.getVisibleRect());

                                    ex.printStackTrace();
                                }
                                moved_file_count++;
                            }

                            if(xmlFile.exists())
                            {
                                // i++;
                                //System.out.println("xml file block"+xmlFile.getName()+" "+file.length());
                                String generatedXMLName="";
                                if(!generatedFileName.trim().equals(""))
                                {
                                    generatedXMLName= generatedFileName+".xml";
                                    //System.out.println("generated name: "+generatedFileName);
                                }

                                try {
                                    try
                                    {
                                        File uploadFile=xmlFile;
                                        long fileSize = uploadFile.length();
                                        uploadURL="http://upload.panadem.com/testapp/UploadServlet?counter="+hmap.get(uploadFile.getName())+"&uid="+uid+"&last_modified="+uploadFile.lastModified()+"&suffix="+prefix+"&generatedFileName="+generatedXMLName;
                                        UploadTask task = new UploadTask(uploadURL, uploadFile,i,relevant_file,1,generatedXMLName,moved_file_count,supported_file_extensions,asyncResponse);
                                        //System.out.println("upload url: "+uploadURL);
                                        //task.addPropertyChangeListener(this);

                                        task.execute();

                                    } catch (Exception ex) {
                                        //System.out.println("hi32");
                                        //Panadem.is_button_enabled=true;is_upload_started=0;
                                        //jLabel4.setText("<html><div style='color:red;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>Oops! Something went wrong.</div></html>");
                                        //jLabel4.paintImmediately(jLabel4.getVisibleRect());

                                        //System.out.println("file transfer unsuccessfull");


                                    }

                                }
                                catch (Exception ex)
                                {
                                    //System.out.println("hi33");
                                    is_button_enabled=true;is_upload_started=0;
                                    //jLabel4.setText("<html><div style='color:red;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>Oops! Something went wrong.</div></html>");
                                    //jLabel4.paintImmediately(jLabel4.getVisibleRect());

                                    ex.printStackTrace();
                                }
                                moved_file_count++;
                            }
                        }

                    } /*else if (file.isDirectory()) {
                       move_files_to_http(file.getAbsolutePath(),DevId);
                   }*/
                }


            }
            catch (Exception ex)
            {
                //jLabel4.setText("<html><div style='color:red;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>Oops! Something went wrong.</div></html>");
                //jLabel4.paintImmediately(jLabel4.getVisibleRect());
                //System.out.println("hi34");
                is_button_enabled=true;is_upload_started=0;
                ex.printStackTrace();
            }

            ////System.out.println(fList);

            return moved_file_count;
        }
        return 0;
    }

    public static int listf(String directoryName, int count, String extensions) {
        int a_count = count;
        try {
            // //System.out.println("lidtf backgrouund call");
            String s = "";
            String f_name = "";
            Log.e("directory_name",directoryName);
            File directory = new File(directoryName);
            Log.e("directory_exist",directory.exists()+"");
            List<File> resultList = new ArrayList<>();
            // get all the files from a directory

            File[] fList = directory.listFiles();


            resultList.addAll(Arrays.asList(fList));
            for (File file : fList) {
                Log.e("File_path: ", file.getAbsolutePath());
                if (file.isFile()) {


                    if (extensions.contains(file.getName().substring(file.getName().lastIndexOf(".") + 1)) && file.getName().indexOf(".") != 0) {

                        a_count++;
                         hmap.put(file.getName(),a_count);
                        String xmlFileName = file.getName() + ".xml";
                        String jpegFileName = file.getName().split("[.]")[0] + "-0.jpeg";
                        //System.out.println(directoryName+jpegFileName);
                        File xmlFile = new File(directoryName + "/" + xmlFileName);
                        File jpegFile = new File(directoryName + "/" + jpegFileName);
                        if (jpegFile.exists()) {
                               hmap.put(jpegFile.getName(),a_count);
                            //count++;
                        }
                        if (xmlFile.exists()) {
                              hmap.put(xmlFile.getName(),a_count);
                            String OldFileContent = "";
                            BufferedReader xmlReader = new BufferedReader(new FileReader(xmlFile));
                            String line = xmlReader.readLine();
                            while (line != null) {
                                OldFileContent = OldFileContent + line + System.getProperty("line.separator");
                                line = xmlReader.readLine();
                            }

                            if (OldFileContent.contains("&gt;")) {
                                String newContent = OldFileContent.replaceAll("&lt;", "<");

                                newContent = newContent.replaceAll("&gt;", ">");
                                xmlReader.close();
                                FileWriter xmlWriter = new FileWriter(xmlFile);
                                xmlWriter.write(newContent);
                                xmlWriter.close();
                            } else {
                                xmlReader.close();
                            }
                            //System.out.println("xmlfile found");
                            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                            Document doc = dBuilder.parse(xmlFile);
                            doc.getDocumentElement().normalize();
                            NodeList nList = doc.getElementsByTagName("DictationProperties");
                            //System.out.println(nList.getLength());
                            String fileName = xmlFile.getName();
                            for (int temp = 0; temp < nList.getLength(); temp++) {
                                Node nNode = nList.item(temp);
                                ////System.out.println("\nCurrent Element :" + nNode.getNodeName());

                                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                                    Element eElement = (Element) nNode;
                                    if (eElement.getElementsByTagName("PropertyIdentifier").item(0).getTextContent().contains("DPMBarcode")) {
                                        try {
                                            fileName = eElement.getElementsByTagName("patientId").item(0).getTextContent() + "_" + eElement.getElementsByTagName("patientName").item(0).getTextContent().replaceAll(" ", "-") + "_" + eElement.getElementsByTagName("patientSex").item(0).getTextContent() + "_" + eElement.getElementsByTagName("patientAge").item(0).getTextContent() + "_" + eElement.getElementsByTagName("patientDOB").item(0).getTextContent() + "_" + eElement.getElementsByTagName("patientDOI").item(0).getTextContent() + "_" + eElement.getElementsByTagName("patientDOS").item(0).getTextContent();
                                            fileName = fileName.replaceAll("([^\\p{L}\\p{N}_])", "-");
                                        } catch (Exception e) {
                                            fileName = xmlFile.getName();
                                        }
                                    }


                                }
                            }
                            //System.out.println(fileName);
                            // count++;
                        }
                        //map_count++;
                    }
                } else if (file.isDirectory() && file.getName().indexOf(".") != 0) {
                    // //System.out.println(file.getAbsolutePath());

                    a_count = listf(file.getAbsolutePath(), a_count, extensions);

                }
            }

        } catch (Exception e) {
            //System.out.println("hi31");
            is_button_enabled = true;
            is_upload_started = 0;
            //jLabel4.setText("<html><div style='color:red;font-weight:bold;text-align:center;font-size:12px;font-family:segoe ui'>Oops! Something went wrong.</div></html>");
            //jLabel4.paintImmediately(jLabel4.getVisibleRect());
            // is_button_enabled=true;
            e.printStackTrace();
        }
        return a_count;

    }
    public interface AsyncResponse {
        void processFinish(Integer progress);
    }
    public void resetActivity()
    {
        startActivity(new Intent(uploadActivity.this, uploadActivity.class));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Intent i = new Intent(uploadActivity.this, MainActivity.class);
        finish();
        overridePendingTransition(0, 0);
        startActivity(i);
        overridePendingTransition(0, 0);
    }



    public void refreshActivity()
    {
        finish();
        startActivity(getIntent());
    }
    protected static String getFileContent(String xmlFilePath) throws IOException {
        String xmlFileContent="";

        InputStream is = new FileInputStream(xmlFilePath);
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));

        String line = buf.readLine();
        StringBuilder sb = new StringBuilder();

        while(line != null){
            sb.append(line);
            line = buf.readLine();
        }

        xmlFileContent = sb.toString();

        return xmlFileContent;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.titlebaroption, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.logout_button:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    void logout()
    {
        Log.e("logout","function call");
       // sharedPreferences=getSharedPreferences("sharedPrefs",MODE_PRIVATE);
        sharedPreferences.edit().clear().commit();
        startActivity(new Intent(this, loginAction.class));
    }
}

