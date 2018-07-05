package com.example.dkrocks.medhelper;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ProviderQueryResult;

public class forget_password extends AppCompatActivity {
    EditText email ;
    Button reset;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        email=(EditText)findViewById(R.id.foremail);
        reset=(Button)findViewById(R.id.reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                final boolean[] b = new boolean[1];
                mAuth.fetchProvidersForEmail(email.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                        b[0] = !task.getResult().getProviders().isEmpty();
                        if(b[0])
                        {
                            FirebaseAuth.getInstance().sendPasswordResetEmail(email.getText().toString().trim())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("Reset : ", "Email sent.");
                                                Toast.makeText(forget_password.this,"Mail Sent",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                        else
                        {
                            Toast.makeText(forget_password.this,"User Doesn't Exist",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}