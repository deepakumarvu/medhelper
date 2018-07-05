package com.example.dkrocks.medhelper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.List;

public class docinfo extends AppCompatActivity {
    List<medhelp1> MedList;
    SQLiteDatabase mDatabase;
    ListView listView;
    medAdapter adapter;
    ImageView image;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docinfo);
        image=(ImageView) findViewById(R.id.image);
        listView = (ListView) findViewById(R.id.listViewEmployees);
        MedList = new ArrayList<>();
        mDatabase = openOrCreateDatabase(Prev_history.DATABASE_NAME, MODE_PRIVATE, null);
        createTable();
        showdatafromdb();
    }
    private void createTable()
    {
        mDatabase.execSQL("CREATE TABLE IF NOT EXISTS Medhelp ( what varchar(200) NOT NULL, who varchar(200) NOT NULL,medi varchar(200) NOT NULL,des varchar(200),date varchar(200) NOT NULL,id integer PRIMARY KEY AUTOINCREMENT)");

    }
    private void showdatafromdb() {
            Cursor cursorEmployees = mDatabase.rawQuery("SELECT * FROM Medhelp", null);
        String txt2QR="His ";
            if (cursorEmployees.moveToFirst()) {
                //looping through all the records
                do {
                    //pushing each record in the employee list
                    txt2QR+="\nWhat = "+cursorEmployees.getString(0)+"\nWho and Where = "+cursorEmployees.getString(1)+
                            "\nSeverity = "+cursorEmployees.getString(2)+"\nDescription = "+cursorEmployees.getString(3)+
                            "\nDate = "+cursorEmployees.getString(4)+"\n---------------------------";
                    MedList.add(new medhelp1(
                            cursorEmployees.getString(0),
                            cursorEmployees.getString(1),
                            cursorEmployees.getString(2),
                            cursorEmployees.getString(3),
                            cursorEmployees.getString(4),
                            cursorEmployees.getInt(5)
                    ));
                } while (cursorEmployees.moveToNext());
            }
            //closing the cursor
            MultiFormatWriter multiFormatWriter= new MultiFormatWriter();
        BitMatrix bitMatrix= null;
        try {
            bitMatrix = multiFormatWriter.encode(txt2QR, BarcodeFormat.QR_CODE,700,700);
        } catch (WriterException e) {
            e.printStackTrace();
        }
            BarcodeEncoder barcodeEncoder= new BarcodeEncoder();
            final Bitmap bitmap= barcodeEncoder.createBitmap(bitMatrix);
            image.setImageBitmap(bitmap);
            cursorEmployees.close();
            adapter = new medAdapter(this, R.layout.list_layout_employee, MedList, mDatabase);
            listView.setAdapter(adapter);
    }
}
