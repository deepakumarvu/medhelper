package com.example.dkrocks.medhelper;

/**
 * Created by dkrocks on 8/10/17.
 */

public class medhelp1 {
    String da,dis,medi,des,who;
    int id;
    public medhelp1(String dis,String who,String medi,String des,String da,int id)
    {
        this.da=da;
        this.dis=dis;
        this.medi=medi;
        this.des=des;
        this.who=who;
        this.id=id;

    }
    public  String getda()
    {
        return da;
    }
    public String getdis()
    {
        return dis;
    }
    public String getmedi()
    {
        return medi;
    }
    public String getwho()
    {
        return who;
    }
    public String getdes() {return des;}
    public int getid(){return id;}

}
