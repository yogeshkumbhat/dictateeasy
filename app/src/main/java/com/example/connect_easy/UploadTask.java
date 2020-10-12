/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package panadem_new;

/**
 *
 * @author Yogesh
 */
package com.example.connect_easy;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static com.example.connect_easy.uploadActivity.failed_file_count;
import static com.example.connect_easy.uploadActivity.file_number;
import static com.example.connect_easy.uploadActivity.isConnected;
import static com.example.connect_easy.uploadActivity.is_button_enabled;
import static com.example.connect_easy.uploadActivity.is_upload_started;
import static com.example.connect_easy.uploadActivity.present_file;
import static com.example.connect_easy.uploadActivity.supported_file_extensions;

//import javax.swing.JOptionPane;
//import javax.swing.SwingWorker;
//import com.example.connect_easy.MainActivity.getFileSize();

/**
 * Executes the file upload in a background thread and updates progress to
 * listeners that implement the java.beans.PropertyChangeListener interface.
 * @author www.codejava.net
 *
 */
public class UploadTask extends AsyncTask<Void,Integer,Void> {
    public uploadActivity.AsyncResponse delegate=null;
    private String uploadURL,generatedFileName;
    private File uploadFile;
    private int file_count;
    private int relevant_file;
    private int attempt_counter;
    private int bhasad_var;
    private int mc_var;
    private String folder_path;
    private TextView message_box;
    // private int uid;
    public UploadTask(String uploadURL, File uploadFile, int file_count, int relevant_file, int attempt_counter, String generatedFileName, int bhasad_var, String folder_path, uploadActivity.AsyncResponse asyncResponse) {
        delegate=asyncResponse;
        this.uploadURL = uploadURL;
        this.uploadFile = uploadFile;
        this.relevant_file=relevant_file;
        this.file_count=file_count;
        this.attempt_counter=attempt_counter;
        this.generatedFileName=generatedFileName;
        this.bhasad_var=bhasad_var;
        this.mc_var=0;
        this.folder_path=folder_path;
       // message_box=(TextView) findViewById(R.id.message_box);
        //System.out.println("yo he url: "+uploadURL+" file_count"+bhasad_var+" "+uploadFile.getName());

        // this.uid=Integer.parseInt(uid);
    }


    /**
     * Executed in background thread
     * @return
     */
    @Override
    protected void onPreExecute()
    {
        uploadActivity.progerssText.setText("0");
    }
    @Override
    protected Void doInBackground(Void... voids) {
        // int current_file_count=bhasad_var;
        ////System.out.println("background chala");
        try {
            if(bhasad_var!=9999)
            {
                //Panadem.jLabel8.setText("<html><div style='color:#971b1c;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>File upload is in progress. Please do not remove the hardware.</DIV></HTML>");
                //Panadem.jLabel8.paintImmediately(Panadem.jLabel8.getVisibleRect());
            }

            ////System.out.println("try chala");
            if(!isConnected)
            {

                cancel(true);
            }
            ////System.out.println("connect to he");
            ////System.out.println(uploadURL);
            MultipartUploadUtility util = new MultipartUploadUtility(uploadURL,"UTF-8");

            if(!uploadFile.exists())
            {
                if(uploadFile.getName().substring(uploadFile.getName().lastIndexOf(".") + 1).toLowerCase().equals("xml"))
                {

                    return null;
                }
                else
                {
                    //System.out.print("file exist hi nai kri");
                    failed_file_count++;
                    return null;
                }
            }
            util.addFilePart("uploadFile", uploadFile);
            FileInputStream inputStream = new FileInputStream(uploadFile);
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            long totalBytesRead = 0;
            int percentCompleted = 0;
            long fileSize = uploadFile.length();
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                util.writeFileBytes(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                percentCompleted = (int) (totalBytesRead * 100 / fileSize);
                //System.out.println(percentCompleted+" "+uploadFile.getName());

            }


            inputStream.close();
            util.finish();
             
            /*if(file_number==relevant_file)
               {
                    jLabel4.setText("<html><div style='color:#FFFFFF;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>All files uploaded successfully.</DIV></HTML>");
                    jLabel4.paintImmediately(Panadem.jLabel4.getVisibleRect());
                    Panadem.is_button_enabled=true;Panadem.is_upload_started=0;

               }*/
        }

        catch (IOException ex) {

            ex.printStackTrace();
            ////System.out.println("hi1");
            is_button_enabled=true;
            uploadActivity.is_upload_started=0;
            //setProgress(0);
            cancel(true);
        }
        mc_var=1;
        return null;
    }

    /**
     * Executed in Swing's event dispatching thread
     */
    @Override
    protected void onPostExecute(Void voids) {
        //System.out.println(mc_var+"mc value");
        if (!isCancelled() && !uploadFile.getName().substring(uploadFile.getName().lastIndexOf(".") + 1).toLowerCase().equals("xml"))
        {
            //System.out.println("calling from done"+uploadFile.getName());
            if(new File(uploadFile.getAbsolutePath()).exists() && isConnected)
            {
                ////System.out.println("upload file name:"+uploadFile.getName());
                if(bhasad_var==9999)
                {

                    // Panadem.jLabel4.setText("<html><div style='color:#FFFFFF;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>Updating data files.</DIV></HTML>");
                    //Panadem.jLabel4.paintImmediately(Panadem.jLabel4.getVisibleRect());
                }
                else
                {
                    Log.e("message box","<html><div style='color:#FFFFFF;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>Uploading "+file_number+" of "+relevant_file+" files.</DIV></HTML>");
                    int status=(int)((file_number/relevant_file)*100);
                    Log.e("status","ramu kaki"+ status);
                    //Panadem.jLabel4.paintImmediately(Panadem.jLabel4.getVisibleRect());
                }
                if(relevant_file<=file_number && file_count==0 && bhasad_var!=9999)
                {
                    //Panadem.jLabel4.setText("<html><div style='color:#FFFFFF;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>All the files uploaded successfully.</DIV></HTML>");
                    //Panadem.jLabel4.paintImmediately(Panadem.jLabel4.getVisibleRect());
                    //////System.out.println("hi2");
                    //Panadem.is_button_enabled=true;Panadem.is_upload_started=0;
                }
                ////System.out.println(file_number+"  "+relevant_file);
                MainActivity.get_String(uploadFile.getAbsolutePath(),generatedFileName,bhasad_var,new MainActivity.VolleyCallback(){
                    @Override
                    public void onSuccess(String result){

                        Long server_file_size=Long.parseLong(result);
                        if(server_file_size>=uploadFile.length())
                        {
                            //attempt_counter=1;
                            ////System.out.print("delete hua ");
                            //Panadem.jp_progress.setVisible(true);
                            int status=(int)((file_number/relevant_file)*100);
                            Log.e("status","ramu kaka"+ status);
                          //  System.out.println(status);
                            //uploadActivity.progressBar.setProgress(status);

                            if(status==100)
                            {
                                uploadActivity.dialog.dismiss();

                            }
                            Log.e("message box",(int)(file_number*100/relevant_file)+"");

                            // Panadem.jp_progress.repaint();
                            //Panadem.jp_progress.paintImmediately(Panadem.jp_progress.getVisibleRect());
                            ////System.out.print(uploadFile.getAbsolutePath());
                            uploadFile.delete();
                            System.out.println(uploadFile.getName()+".xml  xmlfilename");
                            if(new File(uploadFile.getAbsolutePath()+"-attachment.zip").exists() )
                            {
                                ////System.out.println("delete hua "+uploadFile.getAbsolutePath()+"-attachment.zip");
                                new File(uploadFile.getAbsolutePath()+"-attachment.zip").delete();
                            }
                            if(new File(uploadFile.getAbsolutePath().replaceAll(".xml","-attachment.zip")).exists())
                            {
                                new File(uploadFile.getAbsolutePath().replaceAll(".xml","-attachment.zip")).delete();
                            }

                            if(new File(uploadFile.getAbsolutePath().replace("-0.jpeg","-0-small.jpeg")).exists())
                            {
                                new File(uploadFile.getAbsolutePath().replaceAll("-0.jpeg","-0-small.jpeg")).delete();
                            }
                            if(new File(uploadFile.getAbsolutePath()+".xml").exists())
                            {
                                new File(uploadFile.getAbsolutePath()+".xml").delete();
                            }
                            if(new File(uploadFile.getAbsolutePath()+".XML").exists())
                            {
                                new File(uploadFile.getAbsolutePath()+".XML").delete();
                            }
                            if(supported_file_extensions.contains(uploadFile.getName().substring(uploadFile.getName().lastIndexOf(".") + 1)))
                            {
                                if(bhasad_var!=9999)
                                {

                                    file_number++;
                                    Log.e("file_number increament", file_number+"");
                                    uploadActivity.progressBar.setProgress((int)(file_number*100/relevant_file));
                                    uploadActivity.progerssText.setText(String.valueOf((int)(file_number*100/relevant_file)));
                                   // delegate.processFinish(100);
                                    if(file_number==relevant_file)
                                    {
                                        uploadActivity.dialog.dismiss();
                                      //  startActivity(new Intent(UploadTask.this,uploadActivity.class));
                                      //  finish();

                                        //this.finish();
                                    }
                                    Log.e("file number increament",file_number+"");
                                }
                            }




                        }

                        else if(server_file_size<uploadFile.length() && attempt_counter<4)
                        {
                            //System.out.println("reattempt block server: "+server_file_size+"client file size:" + uploadFile.length());
                            //System.out.println(uploadFile.getAbsolutePath()+" "+generatedFileName+" call from reattempt block");
                            ////System.out.println("attempt"+attempt_counter+"bkl file:"+uploadFile.getName());

                            UploadTask reAttempt=new UploadTask(uploadURL,uploadFile,file_number,relevant_file,++attempt_counter,generatedFileName,bhasad_var,folder_path,delegate);
                            reAttempt.execute();
                            attempt_counter++;
                        }
                        else if(attempt_counter==4)
                        {
                            failed_file_count++;
                            //System.out.println("attempt failed:"+failed_file_count);
                        }
                    }
                });
                //Long server_file_size=MainActivity.getFileSize(uploadFile.getAbsolutePath(),generatedFileName,bhasad_var);


            }
            else if(!new File(uploadFile.getAbsolutePath()).exists())
            {
                ////System.out.println(Panadem.drive_selected+File.separator+"Panadem_do_not_delete.txt");
                if(bhasad_var!=9999)
                {
                    if(!uploadFile.getName().substring(uploadFile.getName().lastIndexOf(".") + 1).toLowerCase().equals("xml"))
                    {
                        failed_file_count++;
                    }
                    //failed_file_count++;
                    //System.out.println("this is called");
                }
            }
            //file deleted before upload starts and after click
            if(present_file<=file_number && bhasad_var!=9999)
            {
                //System.out.println("block1");
                if(uploadActivity.listf(folder_path,0,supported_file_extensions)==0)
                {
                    ///Panadem.jp_progress.UpdateProgress(0);
                    //////System.out.println("hi6 file number:"+file_number+"relevant file:"+relevant_file);
                    is_button_enabled=true;is_upload_started=0;
                    //hmap.clear();
                    //jLabel5.setText(jLabel5.getText().replaceAll(" \\d+ ", " 0 "));
                    //jLabel5.paintImmediately(Panadem.jLabel5.getVisibleRect());
                    Log.e("message box","<html><div style='color:#FFFFFF;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>All files uploaded successfully.</DIV></HTML>");
                    //jLabel4.paintImmediately(Panadem.jLabel4.getVisibleRect());
                    Log.e("message box","<html><div style='color:#1c971b;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>File upload is completed. Safe to remove hardware.</DIV></HTML>");
                   //Panadem.jLabel8.paintImmediately(Panadem.jLabel8.getVisibleRect());
                }
            }
            else if((file_number+failed_file_count)<relevant_file && bhasad_var!=9999)
            {
                // //////System.out.println("hi6");
                is_button_enabled=false;
                Log.e("message box","<html><div style='color:#971b1c;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>File upload is in progress. Please do not remove the hardware.</DIV></HTML>");
               // message_boxjLapaintImmediately(Panadem.jLabel8.getVisibleRect());
            }
            else if(file_number>=relevant_file && failed_file_count<=0 && bhasad_var!=9999)
            {
               // Panadem.jp_progress.UpdateProgress(0);
                //System.out.println("block2");
                //////System.out.println("hi6 file number:"+file_number+"relevant file:"+relevant_file);
                //hmap.clear();
                is_button_enabled=true;is_upload_started=0;

                //jLabel5.setText(jLabel5.getText().replaceAll(" \\d+ ", " 0 "));
                //jLabel5.paintImmediately(Panadem.jLabel5.getVisibleRect());
                //jLabel4.setText("<html><div style='color:#FFFFFF;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>All files uploaded successfully.</DIV></HTML>");
                //jLabel4.paintImmediately(Panadem.jLabel4.getVisibleRect());
                //Panadem.jLabel8.setText("<html><div style='color:#1c971b;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>File upload is completed. Safe to remove hardware.</DIV></HTML>");
                //Panadem.jLabel8.paintImmediately(Panadem.jLabel8.getVisibleRect());
                uploadActivity.dialog.dismiss();
            }
            else if(failed_file_count>0 && (failed_file_count+file_number)>=relevant_file && bhasad_var!=9999 )
            {
                //Panadem.jp_progress.UpdateProgress(0);
                //Panadem.jp_progress.repaint();
                //////System.out.println("hi7");
                is_button_enabled=true;is_upload_started=0;
                // jLabel5.setText(jLabel5.getText().replaceAll(" \\d+ ", " 0 "));
                //jLabel5.paintImmediately(Panadem.jLabel5.getVisibleRect());
                //jLabel4.setText("<html><div style='color:#FFFFFF;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>Connection error.</DIV></HTML>");
                //jLabel4.paintImmediately(Panadem.jLabel4.getVisibleRect());
                //Panadem.jLabel8.setText("<html><div style='color:white;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>"+(relevant_file-failed_file_count)+" files uploaded successfully. Please try again for remaining files. </DIV></HTML>");
                //Panadem.jLabel8.paintImmediately(Panadem.jLabel8.getVisibleRect());
            }
        }
    }




}