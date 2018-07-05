package com.example.dkrocks.medhelper;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Prev_history extends AppCompatActivity implements View.OnClickListener {
    public static final String DATABASE_NAME = "medhelper";
    SQLiteDatabase mDatabase;
    EditText what,who,des;
    Spinner sever;
    DatePicker datePicker;
    List<String> list = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prev_history);

        sever = (Spinner) findViewById(R.id.sever);
        list.add("Low");
        list.add("Medium");
        list.add("High");

        what = (EditText) findViewById(R.id.problem);
        who= (EditText) findViewById(R.id.who);
        des=(EditText)findViewById(R.id.des);
        datePicker=(DatePicker)findViewById(R.id.datePicker);
        datePicker.setMaxDate(new Date().getTime());
        findViewById(R.id.btnadd).setOnClickListener(this);
        mDatabase=openOrCreateDatabase(DATABASE_NAME,MODE_PRIVATE,null);
        createTable();
    }
    private void createTable()
    {
        mDatabase.execSQL("CREATE TABLE IF NOT EXISTS Medhelp ( what varchar(200) NOT NULL, who varchar(200) NOT NULL,medi varchar(200) NOT NULL,des varchar(200),date varchar(200) NOT NULL,id integer PRIMARY KEY AUTOINCREMENT)");

    }

    private void addEmp()
    {
        String twhat = what.getText().toString().trim();
        String twho= who.getText().toString().trim();
        String tsever = sever.getSelectedItem().toString().trim();
        String tdes = des.getText().toString().trim();
        String date;
        if(twhat.isEmpty()){
            what.setError("Problem cant be empty");
            what.requestFocus();
            return;
        }
        if(twho.isEmpty()){
            who.setError("Disease cant be empty");
            who.requestFocus();
            return;
        }
        date=""+datePicker.getYear()+"-"+datePicker.getMonth()+"-"+datePicker.getDayOfMonth();
        Log.d("what",twhat);
        Log.d("who",twho);
        Log.d("sever",tsever);

        String insertsql = "INSERT INTO Medhelp \n" + "(what,who,medi,des,date)\n" + "VALUES \n" + "(?, ?, ?, ?, ?)";
        mDatabase.execSQL(insertsql, new String[]{twhat, twho, tsever,tdes,date});
        Toast.makeText(this,"Data Added", Toast.LENGTH_LONG).show();
    }
    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.btnadd:
                addEmp();
                break;
        }
    }
}
