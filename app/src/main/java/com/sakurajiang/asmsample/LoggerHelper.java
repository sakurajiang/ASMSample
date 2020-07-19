package com.sakurajiang.asmsample;

import android.util.Log;

/**
 * Created by JDK on 2020/7/7.
 */
public class LoggerHelper {
    //this test visitFrame
    public static void log(boolean b){
        if(b) {
            System.out.println("this is insert log by ASM");
        }else {
            System.out.println("insert null");
        }
    }
}
