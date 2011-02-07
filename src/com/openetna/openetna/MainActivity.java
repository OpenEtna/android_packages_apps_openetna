
package com.openetna.openetna;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.app.Dialog;
import android.app.AlertDialog;

public class MainActivity extends PreferenceActivity {

    private Preference mInstallGapps;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        mInstallGapps = findPreference("install_gapps");
        updateScreen();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mInstallGapps) {
            String fn = getGappsPath();
            if (fn == null) {
               Toast.makeText(this,"Could not open /etc/init.d/15checkgapps! Reflash system.img!", Toast.LENGTH_LONG).show(); 
               return true;
            }
            File f = new File(fn);
            if (!f.exists()) {
                showDialog(0);
                return true;
            }
            if (!remountSystem(true)) {
                Toast.makeText(this,"Remounting system as rw failed!", Toast.LENGTH_LONG).show();
                return true;
            }
            if (!installGapps(fn)) {
                Toast.makeText(this,"Unzipping failed!", Toast.LENGTH_LONG).show();
                updateScreen();
                return true;
            }
            if (!remountSystem(false)) {
                Toast.makeText(this,"Remounting system as ro failed!", Toast.LENGTH_LONG).show();
            }
            updateScreen();
        }
        return true;
    }

    @Override
    protected Dialog onCreateDialog (int id, Bundle args) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Could not find the gapps file "+ getGappsFilename() + " on your sdcard! (I have looked at " + getGappsFilename() + ")")
               .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                   }
                   });
               return builder.create();
    }

    private void updateScreen() {
        if( isGappsInstalled() )
            mInstallGapps.setSummary("Already installed");
        else
            mInstallGapps.setSummary("Not yet installed");
    }

    private String getGappsFilename() {
        String p = getGappsPath();
        if ( p == null )
            return null;
        return p.substring(p.lastIndexOf('/')+1);
    }

    private String getGappsPath() {
        try { // catches IOException below
            FileInputStream fIn = new FileInputStream("/etc/init.d/15checkgapps");
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader in = new BufferedReader(isr);
            String CurLine = "";
            while (!(CurLine.startsWith("ARC="))){

                CurLine = in.readLine();
            }
            if (!(CurLine.startsWith("ARC=")))
                return null;

            Log.i("File Reading stuff", "success = " + CurLine.substring(4));
            return CurLine.substring(4);
            // WOHOO lets Celebrate =)
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    public boolean isGappsInstalled() {
        File f = new File("/system/framework/com.google.android.maps.jar");
        return f.exists();
    }

    private boolean remountSystem(boolean rw) {
        if (rw)
            return (exec("/system/xbin/mount -o remount,rw /system") == 0);
        else
            return (exec("/system/xbin/mount -o remount,ro /system") == 0);
    }
    
    public boolean installGapps(String gappsPath) {
        return (exec("/system/xbin/unzip -o " + gappsPath +" -x 'META-INF*' -d /") == 0);
    }

    public int exec(String executable) {
        try {
            // Executes the command.
            Process process = Runtime.getRuntime().exec(executable);
    
            // Reads stdout.
            // NOTE: You can write to stdin of the command using
            //       process.getOutputStream().
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
        
            // Waits for the command to finish.
            process.waitFor();
   
            Log.i("GappsInstaller", "Running " + executable);
            Log.i("GappsInstaller", "Stdout got " + output.toString());
            Log.i("GappsInstaller", "Exit value is " + process.exitValue());
            return process.exitValue();
        } catch (IOException e) {
            return 100;
        } catch (InterruptedException e) {
            return 101;
        }
    }
}
