package com.sakurajiang.asmsample;

import android.util.Log;

/**
 * Created by JDK on 2020/7/8.
 */
public abstract class T {
    public String mPath;
    public T(String path){
        this.mPath = path;
    }

    public void test(boolean b){
        if(b){
            int t = 50;
            Log.e("sakurajiang","b ="+b);
        }else {
            Log.e("sakurajiang","false b= "+b);
        }
    }
}
