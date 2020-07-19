package com.sakurajiang.asmsample;

import android.util.Log;

/**
 * Created by JDK on 2020/7/7.
 */
public class LoggerHelper {
    public static void log(boolean b){
        if(b) {
            System.out.println("print to console");
        }else {
            System.out.println("write 2 file");
        }
    }
}
