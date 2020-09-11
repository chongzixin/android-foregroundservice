package com.example.foregroundservicetutorial;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

class Utils {
    static String getCurrentDateTime() {
        SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS");
        Date now = new Date();
        return df.format(now);
    }

    static void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("timestamp.txt", Context.MODE_APPEND));
            outputStreamWriter.write(data + "\n");
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    static String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("timestamp.txt");

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

                // only display in reverse order if it's above Android 8.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Collections.reverse(tmp);
                    ret = String.join("\n", tmp);
                } else {
                    ret = stringBuilder.toString();
                }
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
