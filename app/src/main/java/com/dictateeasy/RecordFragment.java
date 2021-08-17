package com.dictateeasy;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment implements View.OnClickListener {

    private NavController navController;

    private ImageButton listBtn;
    private ImageButton recordBtn;
    private ImageButton play_pause_button;
    private TextView filenameText;

    private boolean isRecording = false;
    private boolean isInProgress=false;
    private String currentAudioPath="";

    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21;

    private MediaRecorder mediaRecorder;
    private String recordFile;

    private Chronometer timer;
    private long pauseOffset;
    private boolean running=false;

    private SeekBar recorderSeekbar;
    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;
    private Handler seekbarHandler;
    private Runnable updateSeekbar;
    private Handler seekbarTimeHandler;

    private String fileToAppendWith="";
    private String mergedFileName;
    private Audio_util audioUtil;

    //name of the file which needs to be edited
    String existing_file_name;
    int exsiting_file_duration=0;
    public RecordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
       existing_file_name = RecordFragmentArgs.fromBundle(getArguments()).getFilename();
        Log.e("edit_filename",existing_file_name);
        super.onViewCreated(view, savedInstanceState);

        //Intitialize Variables
        pauseOffset=0;
        navController = Navigation.findNavController(view);
        listBtn = view.findViewById(R.id.record_list_button);
        recordBtn = view.findViewById(R.id.record_btn);
        timer = view.findViewById(R.id.record_timer);
        filenameText = view.findViewById(R.id.record_filename);
        play_pause_button=view.findViewById(R.id.play_pause_button);
        recorderSeekbar = view.findViewById(R.id.recorder_seekbar);
        recorderSeekbar.setEnabled(false);
        /* Setting up on click listener
           - Class must implement 'View.OnClickListener' and override 'onClick' method
         */
        listBtn.setOnClickListener(this);
        recordBtn.setOnClickListener(this);
        play_pause_button.setOnClickListener(this);
        Log.e("file_name_test","jjj"+existing_file_name+"kkkk");
        if(!existing_file_name.equals("blank"))
        {
            Log.e("file_name","jjj"+existing_file_name+"kkkk");
            if(checkPermissions()) {

                //Start Recording
               // startRecording(0);
                play_pause_button.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.recorder_play_btn, null));
                // Change button image and set Recording state to false
                recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_recording, null));
                //isRecording = true;
                isInProgress=true;
                //isPlaying=true;
                running=false;
                filenameText.setText("Recording, File Name : " + (new File(existing_file_name)).getName());

            }
            fileToAppendWith=existing_file_name;
           // String mediaPath = Uri.parse("android.resource://<your-package-name>/raw/filename").getPath();

            exsiting_file_duration=getAudioDuration(new File(existing_file_name));
            timer.setBase(SystemClock.elapsedRealtime()- exsiting_file_duration);
            Log.e("seekbar_length", exsiting_file_duration+"");

            recorderSeekbar.setMax( exsiting_file_duration);
            recorderSeekbar.setProgress( exsiting_file_duration);
           Log.e("seekbar_progress", recorderSeekbar.getProgress()+"");
            recorderSeekbar.setEnabled(true);
           // timer.setBase();
          //  playAudio();

        }
        recorderSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                playAudio();
                }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.e("position","stoptracking");
                int progress = seekBar.getProgress();
                mediaPlayer.seekTo(progress);
                mediaPlayer.start();
                updateRunnable(true);
                seekbarHandler.postDelayed(updateSeekbar, 0);
                //updateRunnable(true);
             //   seekbarHandler.postDelayed(updateSeekbar, 0);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        /*  Check, which button is pressed and do the task accordingly
         */android.util.Log.e("click hua",v.getId()+"");

        switch (v.getId()) {

            case R.id.record_list_button:
                /*
                Navigation Controller
                Part of Android Jetpack, used for navigation between both fragments
                 */
                if(isPlaying)
                {
                    mediaPlayer.stop();
                }
                if(isRecording){
                    startActivity(new Intent(getActivity(),MyDialog.class));
                    //dialog.show();
                   /* AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                    alertDialog.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            navController.navigate(R.id.action_recordFragment_to_audioListFragment);
                            isRecording = false;
                        }
                    });
                    alertDialog.setNegativeButton("CANCEL", null);
                    alertDialog.setTitle("Audio Still recording");
                    alertDialog.setMessage("Are you sure, you want to stop the recording?");
                    alertDialog.create().show();*/
                } else {
                    navController.navigate(R.id.action_recordFragment_to_audioListFragment);
                }
                break;

            case R.id.record_btn:

                if(isInProgress) {
                    //Stop Recording
                    isInProgress=false;

                        stopRecording();

                    // Change button image and set Recording state to false

                    recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_stopped, null));
                    isRecording = false;
                    existing_file_name="blank";

                } else {
                    //Check permission to record audio
                    if(checkPermissions()) {
                        //Start Recording
                        startRecording(0);

                        // Change button image and set Recording state to false
                        recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_recording, null));
                        isRecording = true;
                        isInProgress=true;

                    }
                }
                break;


            case R.id.play_pause_button:
                Log.e("ye_hai",running+"");
                if(running)
                {
                    pauseRecording();
                }
                else
                {
                    Log.e("recording_trigger","resume recording");
                    resumeRecording();
                }
                break;
        }
    }
    private int getAudioDuration(File audioFile)
    {
        // load data file
        if(audioFile.exists()) {
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(audioFile.getAbsolutePath());

            String out = "";
            // get mp3 info

            // convert duration to minute:seconds
            String duration =
                    metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            // exsiting_file_duration=duration;
            Log.v("time", duration);
            long dur = Long.parseLong(duration);
            String seconds = String.valueOf((dur % 60000) / 1000);

            // close object
            metaRetriever.release();
            return Integer.parseInt(duration);
        }
        else
        {
            return 0;
        }
    }
    private void playAudio(){
        //pauseAudio();
        if(isRecording) {

            stopRecording();

        }
        Log.e("isplaying value",isPlaying+"");
        if (!isPlaying) {
            if (fileToAppendWith != "") {
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(fileToAppendWith);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        stopAudio();

                    }
                });


                recorderSeekbar.setMax(mediaPlayer.getDuration());
                recorderSeekbar.setProgress(mediaPlayer.getDuration());

                seekbarHandler = new Handler();



                isPlaying=true;
                play_pause_button.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.recorder_play_btn, null));

                //playAudio(new File(currentAudioPath));
            }
        }
        else
        {
            //pause audia
            Log.e("position","else");
            mediaPlayer.pause();
            seekbarHandler.removeCallbacks(updateSeekbar);
        }
    }
    private void stopAudio() {
        //Stop The Audio
        //playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_play_btn, null));
        // playerHeader.setText("Stopped");
        //isPlaying = false;
       // mediaPlayer.stop();
        seekbarHandler.removeCallbacks(updateSeekbar);
    }
    private void stopRecording() {

        //stop seekbar update
        if (!isInProgress) {
            filenameText.setText("Recording Stopped, File Saved : " + fileToAppendWith.substring(fileToAppendWith.lastIndexOf("/"), fileToAppendWith.length()));
        }
        if(isRecording) {
            seekbarHandler.removeCallbacks(updateSeekbar);
            //Stop Timer, very obvious\
            Log.e("fileappendbeforeif", currentAudioPath + "_" + fileToAppendWith);

            timer.stop();
            running = false;
            //Change text on page to file saved
            Log.e("filename", fileToAppendWith + " " + currentAudioPath);

            //Stop media recorder and set it to null for further use to record new audio
            pauseOffset = 0;
            isRecording = false;

            if (!isPlaying) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;

                if (fileToAppendWith != "" && fileToAppendWith != currentAudioPath) {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.CANADA);
                    Date now = new Date();

                    //initialize filename variable with date and time at the end to ensure the new file wont overwrite previous file
                    mergedFileName = getActivity().getExternalFilesDir("/").getAbsolutePath() + "/" + "Recording_" + formatter.format(now) + ".mp3";

                    audioUtil = new Audio_util(fileToAppendWith, mergedFileName);
                    String result = audioUtil.ConcatAudios(new File(fileToAppendWith), new File(currentAudioPath));
                    Log.e("mergeresult", result + "");
                }
            } else {
                mediaPlayer.pause();
                play_pause_button.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.recorder_pause_btn_disabled, null));

            }
        }
        //isRecording = false;
        running=false;
        //mediaPlayer = null;
       // isPlaying = false;
        play_pause_button.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.recorder_pause_btn_disabled, null));
      //  recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_stopped, null));


    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void pauseRecording() {

        if (running && isRecording) {
            timer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - timer.getBase();
            running = false;
            mediaRecorder.pause();
            play_pause_button.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.recorder_play_btn, null));

        }
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void resumeRecording() {
        Log.e("resume_call",isInProgress +" "+ isPlaying);
        if(!existing_file_name.equals("blank"))
        {
           startRecording(getAudioDuration(new File(existing_file_name)));
        }
       else if(isInProgress && isPlaying)
       {

               stopAudio();
               mediaPlayer.stop();
               startRecording(mediaPlayer.getDuration());
       }
       else if (!running && isRecording) {

            timer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            timer.start();
            running = true;
            mediaRecorder.resume();
            play_pause_button.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.recorder_pause_btn, null));
        }
        existing_file_name="blank";
    }

    private void startRecording(int startOffset) {
        //Start timer from 0
        if(isPlaying)
        {
            stopAudio();
            mediaPlayer.stop();
        }
        isPlaying=false;
        isRecording=true;

        pauseOffset=0;
        timer.setBase(SystemClock.elapsedRealtime()-startOffset);
        timer.start();
        //seekbar timing to max

        recorderSeekbar.setEnabled(true);
        seekbarHandler = new Handler();
        updateRunnable(false);
        seekbarHandler.postDelayed(updateSeekbar, 0);

        running=true;
        //Get app external directory path
        String recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();

        //Get current date and time
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.CANADA);
        Date now = new Date();

        //initialize filename variable with date and time at the end to ensure the new file wont overwrite previous file
        recordFile = "Recording_" + formatter.format(now) + ".mp3";
        currentAudioPath=recordPath + "/" + recordFile;
        //IF ITS STARTING AFTER play button clicked
        if(!isInProgress) {
            filenameText.setText("Recording, File Name : " + recordFile);
            fileToAppendWith=currentAudioPath;
        }
        //Setup Media Recorder for recording

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(currentAudioPath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Start Recording
        mediaRecorder.start();
        play_pause_button.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.recorder_pause_btn, null));
    }

    private boolean checkPermissions() {
        //Check permission
        if (ActivityCompat.checkSelfPermission(getContext(), recordPermission) == PackageManager.PERMISSION_GRANTED) {
            //Permission Granted
            return true;
        } else {
            //Permission not granted, ask for permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isRecording){
            stopRecording();

        }
        if(isPlaying)
        {
            stopAudio();
            mediaPlayer.stop();
        }
    }

    private void updateRunnable(final boolean play_time) {
        updateSeekbar = new Runnable() {
            @Override
            public void run() {
                Log.e("play_time",play_time+"");
                if(!play_time) {

                    long recordingDuration = SystemClock.elapsedRealtime() - timer.getBase();
                    recorderSeekbar.setMax((int) recordingDuration);
                    recorderSeekbar.setProgress((int)recordingDuration);
                    seekbarHandler.postDelayed(this, 500);
                }
                else {
                    recorderSeekbar.setProgress(mediaPlayer.getCurrentPosition());
                    seekbarHandler.postDelayed(this, 500);
                }
            }
        };
    }



}
