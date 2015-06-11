package it.fabaris.wfp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/**
 * This class is used for input and check the password, 
 * in case it is correct the user can access the Preferences Activity 
 * where he can sets his preferences
 */

public class ControlActivity extends Activity
{
    private EditText password;
    private Button login;
    private String language;
    private Button updateAppBtn;

   ProgressDialog mProgressDialog;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_setting);


            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());


        language = Locale.getDefault().toString();

        password = (EditText) findViewById(R.id.password_edit);
        login = (Button) findViewById(R.id.button_in);


        //set the Progress dialog
        mProgressDialog = new ProgressDialog(ControlActivity.this);
        mProgressDialog.setMessage("Download...");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //added to check the last version of the app and install it
        updateAppBtn= (Button) findViewById(R.id.button_check);

        /**
         * on Click we check if the psw inserted is correct
         */

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        login.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                String pass = password.getText().toString();
                if(!pass.equals("brains"))//if the psw is not correct
                {
                    if(language.equals("it_IT"))
                        Toast.makeText(getBaseContext(), getString(R.string.password_error),Toast.LENGTH_SHORT).show();
                    else if(language.equals("es_ES"))
                        Toast.makeText(getBaseContext(), getString(R.string.password_error),Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getBaseContext(), getString(R.string.password_error),Toast.LENGTH_SHORT).show();
                }
                else//if the psw is correct
                {
                    Intent myIntent = new Intent(v.getContext(), PreferencesActivity.class);
                    startActivity(myIntent);
                    finish();
                    System.exit(0);
                }
            }
        });


/**Added By Claudia
 * this method updates the app to the latest version on the server
 **/

        updateAppBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
               //TODO Implement the function

                //get the current app version
                try {
                  String versionName =getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                  String ver= versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                //connect to the serve
                //request version number
                //compare the current version with the one on the server
                //if the same display the latest version already installed

                //else download the apk
                DownloadFile downloadFile = new DownloadFile();
//                downloadFile.execute(getString(R.string.new_app_url));
                String apkURL= new String();
//                InetAddress actualIP,actualIP1;
//                String address="";
//                String address1="";

                String ip = settings.getString(PreferencesActivity.KEY_IP,null);
//                apkURL =ip +":80/graspreporting/Public/GraspMobile.apk";


            if(Patterns.WEB_URL.matcher(ip).matches()){


                apkURL ="http://"+ ip +":80/graspreporting/Public/GraspMobile.apk";
                downloadFile.execute(apkURL);
            }
               else{
                Toast.makeText(getBaseContext(),"Server Unavailable!",Toast.LENGTH_LONG).show();
            }




            }

        });
    }
    private class DownloadFile extends AsyncTask<String, Integer, String> {
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... sUrl) {
            try {
                URL url = new URL(sUrl[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                /**
                 *  download the file
                 */
                InputStream input = new BufferedInputStream(url.openStream());

                /**
                 * CREATE A TEMPORARY FOLDER
                 */
                File temporaryDirectory = new File(Environment.getExternalStorageDirectory().getPath() + "/temporary/");
                temporaryDirectory.mkdirs();


                File output = new File(Environment.getExternalStorageDirectory().getPath() + "/GRASPMobile.apk");
                //File output = new File(Environment.getExternalStorageDirectory().getPath() + "/GRASP/grasp.apk");
                //File output = new File(PreferencesActivity.this.getCacheDir() + "/GRASP/grasp.apk");

                FileOutputStream fileOutput = new FileOutputStream(output);

                /**
                 *  this will be useful so that you can show a typical 0-100% progress bar
                 */
                int fileLength = connection.getContentLength();

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    /**
                     *  publishing the progress....
                     */
                    publishProgress((int) (total * 100 / fileLength));
                    Log.i("downloaded", "scaricati " + total);
                    fileOutput.write(data, 0, count);
                }
                fileOutput.flush();
                fileOutput.close();
                input.close();
                mProgressDialog.dismiss();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }
        protected void onPostExecute(String sResponse) {
            mProgressDialog.dismiss();
            if (sResponse == null) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                Intent updateApp = new Intent(Intent.ACTION_VIEW);
                                updateApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                updateApp.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/temporary/" + "grasp.apk")), "application/vnd.android.package-archive");
                                startActivity(updateApp);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked

                                finish();

                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ControlActivity.this);
                builder.setMessage(getString(R.string.update))
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show();
            }
        }
}

}
