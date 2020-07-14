/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package panadem_new;

/**
 *
 * @author Yogesh
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import static panadem_new.Panadem.drive_selected;
import static panadem_new.Panadem.failed_file_count;
import static panadem_new.Panadem.file_number;
import static panadem_new.Panadem.isConnected;
import static panadem_new.Panadem.is_button_enabled;
import static panadem_new.Panadem.jLabel4;
import static panadem_new.Panadem.jLabel5;
import static panadem_new.Panadem.listf;
import static panadem_new.Panadem.present_file;

/**
 * Executes the file upload in a background thread and updates progress to
 * listeners that implement the java.beans.PropertyChangeListener interface.
 * @author www.codejava.net
 *
 */
public class UploadTask extends SwingWorker<Void, Integer> {
    private String uploadURL,generatedFileName;
    private File uploadFile;
    private int file_count;
    private int relevant_file;
    private int attempt_counter;

    // private int uid;
    public UploadTask(String uploadURL, File uploadFile,int file_count,int relevant_file,int attempt_counter,String generatedFileName) {
        this.uploadURL = uploadURL;
        this.uploadFile = uploadFile;
        this.relevant_file=relevant_file;
        this.file_count=file_count;
        this.attempt_counter=attempt_counter;
        this.generatedFileName=generatedFileName;
        System.out.println("yo he url: "+uploadURL+" "+uploadFile.getName());

        // this.uid=Integer.parseInt(uid);
    }


    /**
     * Executed in background thread
     */
    @Override
    protected Void doInBackground() throws Exception {

        System.out.println("background chala");
        try {
            Panadem.jLabel8.setText("<html><div style='color:#971b1c;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>File upload is in progress. Please do not remove the hardware.</DIV></HTML>");
            Panadem.jLabel8.paintImmediately(Panadem.jLabel8.getVisibleRect());
            System.out.println("try chala");
            if(!isConnected)
            {

                cancel(true);
            }
            System.out.println("connect to he");
            System.out.println(uploadURL);
            MultipartUploadUtility util = new MultipartUploadUtility(uploadURL,"UTF-8");

            if(!uploadFile.exists())
            {
                System.out.println("file exist hi nai kri");
                failed_file_count++;
                return null;
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
                // System.out.println(percentCompleted+"");
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
            System.out.println("hi1");
            is_button_enabled=true;
            Panadem.is_upload_started=0;
            setProgress(0);
            cancel(true);
        }

        return null;
    }

    /**
     * Executed in Swing's event dispatching thread
     */
    @Override
    protected void done() {
        if(new File(uploadFile.getAbsolutePath()).exists() && isConnected)
        {
            System.out.println("upload file name:"+uploadFile.getName());
            try {

                Panadem.jLabel4.setText("<html><div style='color:#FFFFFF;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>Uploading "+file_number+" of "+relevant_file+" files.</DIV></HTML>");
                Panadem.jLabel4.paintImmediately(Panadem.jLabel4.getVisibleRect());
                if(relevant_file<=file_number && file_count==0)
                {
                    Panadem.jLabel4.setText("<html><div style='color:#FFFFFF;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>All the files uploaded successfully.</DIV></HTML>");
                    Panadem.jLabel4.paintImmediately(Panadem.jLabel4.getVisibleRect());
                    //System.out.println("hi2");
                    Panadem.is_button_enabled=true;Panadem.is_upload_started=0;
                }
                System.out.println(file_number+"  "+relevant_file);

                if(Panadem.getFileSize(uploadFile.getAbsolutePath(),generatedFileName)>=uploadFile.length())
                {
                    //attempt_counter=1;
                    System.out.println("delete hua ");
                    Panadem.jp_progress.setVisible(true);
                    Panadem.jp_progress.UpdateProgress((int)(file_number*100/relevant_file));
                    Panadem.jp_progress.repaint();
                    Panadem.jp_progress.paintImmediately(Panadem.jp_progress.getVisibleRect());
                    System.out.println(uploadFile.getAbsolutePath());
                    uploadFile.delete();

                    if(new File(uploadFile.getAbsolutePath()+"-attachment.zip").exists())
                    {
                        System.out.println("delete hua "+uploadFile.getAbsolutePath()+"-attachment.zip");
                        new File(uploadFile.getAbsolutePath()+"-attachment.zip").delete();
                    }
                    if(new File(uploadFile.getAbsolutePath().replaceAll("0-small","0")).exists())
                    {
                        new File(uploadFile.getAbsolutePath().replaceAll("0-small","0")).delete();
                    }
                    if(!uploadFile.getName().substring(uploadFile.getName().lastIndexOf(".") + 1).equals("xml") || !uploadFile.getName().substring(uploadFile.getName().lastIndexOf(".") + 1).equals("jpeg"))
                    {
                        file_number++;
                    }

                }
                else if(Panadem.getFileSize(uploadFile.getAbsolutePath(),generatedFileName)<uploadFile.length() && attempt_counter<4)
                {

                    System.out.println("attempt"+attempt_counter+"bkl file:"+uploadFile.getName());
                    UploadTask reAttempt=new UploadTask(uploadURL,uploadFile,file_number,relevant_file,++attempt_counter,generatedFileName);
                    reAttempt.execute();
                    //  attempt_counter++;
                }
                else if(attempt_counter==4)
                {
                    failed_file_count++;
                    System.out.println("attempt failed:"+failed_file_count);
                }
            } catch (IOException ex) {
                //System.out.println("hi3");
                Panadem.is_button_enabled=true;Panadem.is_upload_started=0;
                Logger.getLogger(UploadTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else if(!new File(uploadFile.getAbsolutePath()).exists())
        {
            System.out.println(Panadem.drive_selected+File.separator+"Panadem_do_not_delete.txt");
            if(!new File(Panadem.drive_selected+File.separator+"Panadem_do_not_delete.txt").exists())
            {
                System.out.println("this works");
                //System.out.println("hi4");
                Panadem.is_button_enabled=true;Panadem.is_upload_started=0;
                System.out.println(uploadFile.getAbsolutePath()+"");
                Panadem.jLabel4.setText("<html><div style='color:red;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>Please connect the device properly and try again.</DIV></HTML>");
                Panadem.jLabel4.paintImmediately(Panadem.jLabel4.getVisibleRect());
                Panadem.jp_progress.UpdateProgress(0);
                jLabel5.setText("<html><div style='color:#FFFFFF;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>No registered device is connected to the system.</div></html>");
                Panadem.jp_progress.repaint();
                setProgress(0);
                //System.out.println("hi5");
                Panadem.is_button_enabled=true;Panadem.is_upload_started=0;
                cancel(true);
            }
            else
            {
                failed_file_count++;
                System.out.println("this is called");
            }
        }
        //file deleted before upload starts and after click
        if(present_file<=file_number)
        {
            if(listf(drive_selected)==0)
            {
                Panadem.jp_progress.UpdateProgress(0);
                //System.out.println("hi6 file number:"+file_number+"relevant file:"+relevant_file);
                Panadem.is_button_enabled=true;Panadem.is_upload_started=0;
                jLabel5.setText(jLabel5.getText().replaceAll(" \\d+ ", " 0 "));
                jLabel5.paintImmediately(Panadem.jLabel5.getVisibleRect());
                jLabel4.setText("<html><div style='color:#FFFFFF;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>All files uploaded successfully.</DIV></HTML>");
                jLabel4.paintImmediately(Panadem.jLabel4.getVisibleRect());
                Panadem.jLabel8.setText("<html><div style='color:#1c971b;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>File upload is completed. Safe to remove hardware.</DIV></HTML>");
                Panadem.jLabel8.paintImmediately(Panadem.jLabel8.getVisibleRect());
            }
        }
        if((file_number+failed_file_count)<relevant_file)
        {
            // //System.out.println("hi6");
            Panadem.is_button_enabled=false;
            Panadem.jLabel8.setText("<html><div style='color:#971b1c;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>File upload is in progress. Please do not remove the hardware.</DIV></HTML>");
            Panadem.jLabel8.paintImmediately(Panadem.jLabel8.getVisibleRect());
        }
        else if(file_number>=relevant_file && failed_file_count<=0)
        {
            Panadem.jp_progress.UpdateProgress(0);
            //System.out.println("hi6 file number:"+file_number+"relevant file:"+relevant_file);
            Panadem.is_button_enabled=true;Panadem.is_upload_started=0;
            jLabel5.setText(jLabel5.getText().replaceAll(" \\d+ ", " 0 "));
            jLabel5.paintImmediately(Panadem.jLabel5.getVisibleRect());
            jLabel4.setText("<html><div style='color:#FFFFFF;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>All files uploaded successfully.</DIV></HTML>");
            jLabel4.paintImmediately(Panadem.jLabel4.getVisibleRect());
            Panadem.jLabel8.setText("<html><div style='color:#1c971b;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>File upload is completed. Safe to remove hardware.</DIV></HTML>");
            Panadem.jLabel8.paintImmediately(Panadem.jLabel8.getVisibleRect());
        }
        else if(failed_file_count>0 && (failed_file_count+file_number)>=relevant_file )
        {
            Panadem.jp_progress.UpdateProgress(0);
            Panadem.jp_progress.repaint();
            //System.out.println("hi7");
            Panadem.is_button_enabled=true;Panadem.is_upload_started=0;
            // jLabel5.setText(jLabel5.getText().replaceAll(" \\d+ ", " 0 "));
            jLabel5.paintImmediately(Panadem.jLabel5.getVisibleRect());
            jLabel4.setText("<html><div style='color:#FFFFFF;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>Connection error.</DIV></HTML>");
            jLabel4.paintImmediately(Panadem.jLabel4.getVisibleRect());
            Panadem.jLabel8.setText("<html><div style='color:white;font-weight:bold;text-align:center;font-size:12px;font-family:calibri'>"+(relevant_file-failed_file_count)+" files uploaded successfully. Please try again for remaining files. </DIV></HTML>");
            Panadem.jLabel8.paintImmediately(Panadem.jLabel8.getVisibleRect());
        }
    }
}