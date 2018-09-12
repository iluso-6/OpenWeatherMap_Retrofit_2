package shay.example.com.openweatherretrofit2.Util;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Shay de Barra on 03,May,2018
 * Email:  x16115864@student.ncirl.ie
 */
public class Utilities {

    public static String getHumanReadable(long convert_me){

        // the format of the required date
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
        // set the timezone reference for formatting
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC+1"));

        long value = convert_me * 1000L;
        // get the date in milliseconds

        Date date_value = new java.util.Date(value);

        return sdf.format(date_value);

    }
}
