package com.indra.procesos.usb;
import android.util.Log;


public class Logger {
	public static void d(Object o){
		Log.d(">=< USB Controller >=<", String.valueOf(o));
	}
	
	public static void e(Object o){
		Log.e(">=< USB Controller >=<", String.valueOf(o));
	}
}
