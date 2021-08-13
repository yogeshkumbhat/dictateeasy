package com.dictateeasy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AppAction extends AppCompatActivity {
    Button upload_file_button;
    Button record_new_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_action);
       upload_file_button=(Button)findViewById(R.id.upload_file_button);
        upload_file_button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {

               startActivity(new Intent(AppAction.this,uploadActivity.class));

            }
        });
       record_new_button=(Button)findViewById(R.id.record_new_button);
        record_new_button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {

                startActivity(new Intent(AppAction.this,AudioRecorder.class));

            }
        });
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}