package com.example.foregroundservicetutorial;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

class Utils {
    static String getCurrentDateTime() {
        return DateFormat.getDateTimeInstance().format(new Date());
    }

    static void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("location.txt", Context.MODE_APPEND));
            outputStreamWriter.write(data + "\n");
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    static String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("location.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                List<String> tmp = new ArrayList<String>();

                StringBuilder stringBuilder = new StringBuilder();
                // read the file in reverse order and put into arraylist.
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    tmp.add(receiveString);
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();

                // reverse the order of the String
                Collections.reverse(tmp);
                ret = String.join("\n", tmp);
            }
        }
        catch (FileNotFoundException e) {
            Log.e("location activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("location activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
}
