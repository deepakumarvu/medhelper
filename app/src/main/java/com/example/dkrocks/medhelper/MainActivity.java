package com.example.dkrocks.medhelper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;


public class MainActivity extends AppCompatActivity{

    Button start;
    View mProgressView;
    private SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start=(Button)findViewById(R.id.start);
        final Intent intent=new Intent(this,LoginActivity.class);
        final Intent intentaf=new Intent(this,Mainscreen.class);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(intent,1);
            }
        });
        mProgressView=(View)findViewById(R.id.progressBar2);
        final int[] ip = {0};

        mProgressView.animate().setDuration(0).alpha(1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ip[0]++;
                if(ip[0] >1)
                    startActivityForResult(intent,1);
            }
        });
        pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        Boolean b=pref.getBoolean("isAuth", false);
        if(!b)
        {
            startActivityForResult(intent,1);
        }
        else
        {
            if(!pref.getBoolean("basic", false))
                startActivityForResult(new Intent(this,basic_qrgen.class),2);
            else
                startActivity(intentaf);
        }

    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                String strEditText = data.getStringExtra("Value");
                if(!pref.getBoolean("basic", false)){
                    startActivityForResult(new Intent(this,basic_qrgen.class),2);
                    if(pref.getBoolean("basic", false))
                        startActivity(new Intent(this,Mainscreen.class));
                }
            }
        }
        if (requestCode == 2) {
            if(resultCode == RESULT_OK) {
                if(!pref.getBoolean("basic", false)){
                    startActivityForResult(new Intent(this,basic_qrgen.class),2);
                }
                if(pref.getBoolean("basic", false))
                    startActivity(new Intent(this,Mainscreen.class));
            }
        }
    }

}
