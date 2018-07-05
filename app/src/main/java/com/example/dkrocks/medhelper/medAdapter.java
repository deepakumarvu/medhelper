/**
 * Created by root on 10/4/17.
 */
package com.example.dkrocks.medhelper;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class medAdapter extends ArrayAdapter<medhelp1> {
    Context mCtx;
    int listLayoutRes;
    List<medhelp1> MedList;
    static SQLiteDatabase mDatabase;
    LayoutInflater inflater = null;

    public medAdapter(Context mCtx, int listLayoutRes, List<medhelp1> MedList, SQLiteDatabase mDatabase) {
        super(mCtx, listLayoutRes, MedList);
        this.MedList = MedList;
        this.mCtx = mCtx;
        this.listLayoutRes = listLayoutRes;
        this.mDatabase = mDatabase;
        inflater = LayoutInflater.from(mCtx);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        inflater = (LayoutInflater) mCtx.getSystemService(mCtx.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.list_layout_employee, parent, false);
        final medhelp1 MedHis = MedList.get(position);

        TextView what = (TextView) view.findViewById(R.id.what);
        TextView who = (TextView) view.findViewById(R.id.who);
        TextView sever = (TextView) view.findViewById(R.id.sever);
        TextView des = (TextView) view.findViewById(R.id.des);
        TextView date = (TextView) view.findViewById(R.id.date);
        String st=MedHis.getdis();
        what.setText(String.valueOf(MedHis.getdis()));
        who.setText(String.valueOf(MedHis.getwho()));
        sever.setText(String.valueOf(MedHis.getmedi()));
        des.setText(String.valueOf(MedHis.getdes()));
        date.setText(String.valueOf(MedHis.getda()));
        Button buttonEdit = (Button) view.findViewById(R.id.buttonEditmedHis);
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateEmployee(MedHis);
            }
        });

        return view;
    }

    private void updateEmployee(final medhelp1 MedHis) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);


        inflater = (LayoutInflater) mCtx.getSystemService(mCtx.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.dialog_update_employee, null);
        builder.setView(view);


        final EditText what = (EditText) view.findViewById(R.id.problem);
        final EditText who = (EditText) view.findViewById(R.id.who);
        final Spinner sever = (Spinner) view.findViewById(R.id.sever);
        List<String> list = new ArrayList<String>();
        list.add("Low");
        list.add("Medium");
        list.add("High");
        final EditText des = (EditText) view.findViewById(R.id.des);
        final DatePicker date = (DatePicker) view.findViewById(R.id.datePicker);
        String s = MedHis.getda();
        String a[] = s.split("-");
        date.updateDate(Integer.parseInt(a[0]), Integer.parseInt(a[1]), Integer.parseInt(a[1]));
        String st=MedHis.getdis();
        what.setText(String.valueOf(MedHis.getdis()));
        who.setText(String.valueOf(MedHis.getwho()));
        sever.setSelection(list.indexOf(String.valueOf(MedHis.getmedi())));
        des.setText(String.valueOf(MedHis.getdes()));

        final AlertDialog dialog = builder.create();
        dialog.show();
        date.setMaxDate(new Date().getTime());
        view.findViewById(R.id.btnupd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String twhat = what.getText().toString().trim();
                String twho = who.getText().toString().trim();
                String tsever = sever.getSelectedItem().toString().trim();
                String tdes = des.getText().toString().trim();
                String tdate;
                if (twhat.isEmpty()) {
                    what.setError("Problem cant be empty");
                    what.requestFocus();
                    return;
                }
                if (twho.isEmpty()) {
                    who.setError("Disease cant be empty");
                    who.requestFocus();
                    return;
                }
                tdate = "" + date.getYear() + "-" + date.getMonth() + "-" + date.getDayOfMonth();
                String sql = "UPDATE Medhelp " +
                        "SET what = ?, " +
                        "who = ?," +
                        "medi = ?, " +
                        "des = ? ," +
                        "date = ? " +
                        "WHERE id = ?;";

                mDatabase.execSQL(sql, new String[]{twhat, twho, tsever, tdes, tdate, String.valueOf(MedHis.getid())});
                Toast.makeText(mCtx, " Updated ", Toast.LENGTH_SHORT).show();
                reloadEmployeesFromDatabase();

                dialog.dismiss();
            }
        });


    }

    private void reloadEmployeesFromDatabase() {
        Cursor cursorEmployees = mDatabase.rawQuery("SELECT * FROM Medhelp", null);
        if (cursorEmployees.moveToFirst()) {
            MedList.clear();
            do {
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
        cursorEmployees.close();
        notifyDataSetChanged();
    }
}