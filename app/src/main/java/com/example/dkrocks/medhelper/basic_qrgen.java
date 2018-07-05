package com.example.dkrocks.medhelper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import com.google.firebase.auth.PhoneAuthProvider;

public class basic_qrgen extends AppCompatActivity {

    EditText name,addr,con1,con2,otp;
    Spinner bg;
    Context mCtx=this;
    Button gen_btn,save,ok,resend;
    ImageView image;
    String txt2qr,phone;
    Bitmap bitmap;
    int z=1;
    private FirebaseAuth mAuth;
    LayoutInflater inflater = null;
    boolean cont1=false,cont2=false,con=false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    SharedPreferences.Editor editor;
    AlertDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_qrgen);
        final AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
        mAuth = FirebaseAuth.getInstance();
        inflater = (LayoutInflater) mCtx.getSystemService(mCtx.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.otp, null);
        builder.setView(view);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        ok=view.findViewById(R.id.okk);
        resend=view.findViewById(R.id.res);
        otp=view.findViewById(R.id.otps);
        view.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String o=otp.getText().toString().trim();
                if(o.isEmpty()||!(o.matches("[0-9]*"))||(o.length()!=6)) {
                    otp.setError("Number Invalid");
                    return;
                }
                verifyPhoneNumberWithCode(mVerificationId,o);
            }
        });
        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendVerificationCode(phone,mResendToken);
            }
        });
        name=(EditText) findViewById(R.id.name);
        addr=(EditText) findViewById(R.id.addr);
        con1=(EditText) findViewById(R.id.contact1);
        bg=(Spinner) findViewById(R.id.bg);
        con2=(EditText) findViewById(R.id.contact2);
        gen_btn=(Button) findViewById(R.id.gen_btn);
        image=(ImageView) findViewById(R.id.image);
        save=(Button) findViewById(R.id.sav_btn);
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();
        name.setText(pref.getString("name",null));
        addr.setText(pref.getString("addr",null));
        save.setEnabled(false);
        bg.setSelection(Integer.parseInt(pref.getString("bg", "0")));
        con1.setText(pref.getString("con1",null));
        con2.setText(pref.getString("con2", null));
        gen_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                name.setError(null);
                addr.setError(null);
                con2.setError(null);
                con1.setError(null);
                String con11=con1.getText().toString().trim(),con22=con2.getText().toString().trim();
                if(name.getText().toString().trim().isEmpty()||!(name.getText().toString().matches("[a-z A-Z]*"))) {
                    name.setError("The Name is invalid");
                    return;
                }
                if(addr.getText().toString().trim().isEmpty()) {
                    addr.setError("The Address is invalid");
                    return;
                }
                if(con11.isEmpty()||!(con11.matches("[0-9]*"))||(con11.length()>11)) {
                    con1.setError("Number Invalid");
                    return;
                }
                if(!(con22.matches("[0-9]*"))) {
                    con2.setError("Number Invalid");
                    return;
                }
                txt2qr = "Name: "+name.getText().toString().trim()+"\nAddress: "+addr.getText().toString().trim()+"\nBloodGroup: "+bg.getSelectedItem().toString().trim()+"\nContact-1: "+con1.getText().toString().trim();
                if(con11.equals(con22))
                {
                    con1.setError("Duplicate Numbers");
                    con2.setError("Duplicate Numbers");
                }
                else if(!con11.equals(pref.getString("con1",null)))
                {
                    con1.setError("To Be Verified");
                    z=1;
                    cont1=false;
                    phone=con11;
                    startPhoneNumberVerification(con1.getText().toString().trim());
                    otp.setText("");
                    dialog.show();
                }
                else
                {
                    cont1=true;
                    if(!con22.isEmpty()) {
                        txt2qr += "\nContact-2: " + con2.getText().toString().trim();
                        if(!con22.equals(pref.getString("con2",null)))
                        {
                            con2.setError("To Be Verified");
                            z=2;
                            phone=con22;
                            cont2=false;
                            startPhoneNumberVerification(con2.getText().toString().trim());
                            otp.setText("");
                            dialog.show();
                        }
                        else
                        {
                            cont2=true;
                        }
                    }
                    else
                    {
                        cont2=true;
                    }
                }
                if(cont1==true)
                {
                    editor.putString("con1",con1.getText().toString());
                    editor.commit();
                }
                if(cont1==true && cont2==true)
                {
                    editor.putString("con1",con1.getText().toString());
                    if(!con2.getText().toString().trim().isEmpty())
                        editor.putString("con2",con2.getText().toString());
                    editor.putBoolean("basic",true);
                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            storeImage(bitmap);
                        }
                    });
                    save.setEnabled(true);
                    editor.commit();
                }
                MultiFormatWriter multiFormatWriter= new MultiFormatWriter();
                try
                {
                    BitMatrix bitMatrix= multiFormatWriter.encode(txt2qr, BarcodeFormat.QR_CODE,700,700);
                    BarcodeEncoder barcodeEncoder= new BarcodeEncoder();
                    bitmap= barcodeEncoder.createBitmap(bitMatrix);
                    image.setImageBitmap(bitmap);
                    editor.putString("name",name.getText().toString().trim());
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user!=null) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name.getText().toString())
                                .build();
                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("", "User profile updated.");
                                        }
                                    }
                                });
                    }
                    editor.putString("addr",addr.getText().toString().trim());
                    if(cont1==true)
                    editor.putString("bg",""+bg.getSelectedItemPosition());
                    editor.commit();

                }
                catch(WriterException e)
                {
                    e.printStackTrace();
                }
            }
        });
        if(!con1.getText().toString().isEmpty())
        {
            gen_btn.callOnClick();
        }
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user action.
                Log.d("ewr", "onVerificationCompleted:" + credential);
                Toast.makeText(basic_qrgen.this,"Verification Complete",Toast.LENGTH_SHORT).show();
                if(z==1) {
                    cont1 = true;
                    editor.putString("con1", con1.getText().toString());
                    editor.commit();
                    con1.setError(null);
                }
                else if(z==2) {
                    cont2 = true;
                    editor.putString("con2", con2.getText().toString());
                    editor.commit();
                    con2.setError(null);
                }
                dialog.dismiss();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w("", "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    final AlertDialog dialog = builder.create();
                    dialog.show();
                    Toast.makeText(basic_qrgen.this,"Invalid Code",Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                    Toast.makeText(basic_qrgen.this,"SMS quota Exceeded",Toast.LENGTH_SHORT).show();
                }
                if(z==1)
                {
                    cont1=false;
                }
                else if(z==2)
                {
                    cont2=false;
                }
                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("", "onCodeSent:" + verificationId);
                Toast.makeText(basic_qrgen.this,"Code Sent",Toast.LENGTH_SHORT).show();
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // ...
            }
        };
    }
    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        con=false;
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

    }
    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }
    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("otp", "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            user.delete();
                            mAuth.signOut();
                            Toast.makeText(basic_qrgen.this,"Verification Complete",Toast.LENGTH_SHORT).show();
                            if(z==1) {
                                cont1 = true;
                                editor.putString("con1", con1.getText().toString());
                                editor.commit();
                                con1.setError(null);
                            }
                            else if(z==2) {
                                cont2 = true;
                                editor.putString("con2", con2.getText().toString());
                                editor.commit();
                                con2.setError(null);
                            }
                            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(basic_qrgen.this);
                            mAuth.signInWithEmailAndPassword(pref.getString("email",""),pref.getString("pass",""));
                            dialog.dismiss();
                            // [START_EXCLUDE]
                            // [END_EXCLUDE]
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("otp", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                // [START_EXCLUDE silent]
                                otp.setError("Invalid code.");
                                // [END_EXCLUDE]
                            }
                            // [END_EXCLUDE]
                        }
                    }
                });
    }
    // [END sign_in_with_phone]

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }
    private void storeImage(Bitmap image) {
        final int j=0;
        File pictureFile;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, j);
            if(checkSelfPermission(WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                return;
        }
        pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
                Log.i("Image : ","Error creating media file, check storage permissions: ");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(checkSelfPermission(WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, j);
                if(checkSelfPermission(WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                    return;
            }
        }
        pictureFile = getOutputMediaFile();
        if(pictureFile==null)
            return;
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
            MediaScannerConnection.scanFile(this,
                    new String[]{pictureFile.getAbsolutePath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });
            Toast.makeText(this,"Successful",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.putExtra("editTextValue", "Basic Successful");
            setResult(RESULT_OK, intent);
            finish();
        } catch (FileNotFoundException e) {
            Log.d("", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("", "Error accessing file: " + e.getMessage());
        }
    }

     File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        String folderPath = Environment.getExternalStorageDirectory().getAbsolutePath()+ java.io.File.separator + "my_data";
        File mediaStorageDir  = new File(folderPath);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.i("Image : ","dir ");
                return null;
            }
        }
        // Create a media file name
        File mediaFile;
        String mImageName="MH_basic.png";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }
}
