package utils;

import it.fabaris.wfp.activities.FormListActivity;
import it.fabaris.wfp.activities.R;
import it.fabaris.wfp.activities.SplashScreenActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import database.DbAdapterGrasp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class ApplicationExt extends Application
{
	// NOTE: the content of this path will be deleted 
    //       when the application is uninstalled (Android 2.2 and higher) 
    protected File extStorageAppBasePath; 
    public String basepath; 
    protected File extStorageAppCachePath; 
    
    private String LOG_TAG = "GRASP Application";
    private static ApplicationExt  mInstance;
    public static DbAdapterGrasp mDb;
    
    public static DbAdapterGrasp getDatabaseAdapter()
    {
        return mDb;
    }
    
    @Override
    public void onCreate() 
    { 
        super.onCreate(); 
        
        Log.w(LOG_TAG, "Application::onCreate");
          
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString(); 
        basepath = extStorageDirectory + "/GRASP/"; 
        
        mInstance = this;
  
        // Controlla se il salvataggio su SD è utilizzabile 
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) 
        { 
            // Retrieve the base path for the application in the external storage 
            File externalStorageDir = Environment.getExternalStorageDirectory(); 
                  
            if (externalStorageDir != null) 
            { 
                // {SD_PATH}/GRASP 
                extStorageAppBasePath = new File(externalStorageDir.getAbsolutePath() + File.separator + "GRASP"); 
            } 
      
            if (extStorageAppBasePath != null) 
            { 
                // {SD_PATH}/GRASP/database 
                extStorageAppCachePath = new File(extStorageAppBasePath.getAbsolutePath() + File.separator + "database"); 
      
                boolean isCachePathAvailable = true; 
                  
                if (!extStorageAppCachePath.exists()) 
                { 
                    // Create the cache path on the external storage 
                    isCachePathAvailable = extStorageAppCachePath.mkdirs(); 
                } 
                  
                if (!isCachePathAvailable) 
                { 
                    // Unable to create the cache path 
                    extStorageAppCachePath = null; 
                } 
            } 
        } 
        
        //--------------------------
        
        //TASK ASINCRONO DI COPIA DEI CONTENUTI DEL DATABASE
        //new CopyTask().execute(); 
          
        //--------------------------
        
       
        //VERIFICA SE L'APPLICAZIONE STA FACENDO IL PRIMO AVVIO 
        try
        { 
            FirstRun(); 
        }
        catch (IOException e) { 
            // TODO Auto-generated catch block 
            e.printStackTrace(); 
        } 
    } 
      
    public static Context getInstance() {
        return mInstance;
    }

    
    @Override
    public File getCacheDir() 
    { 
        // NOTE: this method is used in Android 2.2 and higher 
          
        if (extStorageAppCachePath != null) 
        { 
            // Use the external storage for the cache 
            return extStorageAppCachePath; 
        } 
        else
        { 
            // /cache 
            return super.getCacheDir(); 
        } 
    } 
      
    private void FirstRun() throws IOException  
    { 
        SharedPreferences settings = this.getSharedPreferences("GRASP", 0); 
        boolean firstrun = settings.getBoolean("firstrun", true); 
        if (firstrun)  
        {  
            // Checks to see if we've ran the application  
            SharedPreferences.Editor e = settings.edit(); 
            e.putBoolean("firstrun", false); 
            e.commit(); 
            // If not, run these methods: 
            //SetDirectory(); 
            mDb = new DbAdapterGrasp(mInstance);//copyGraspDatabaseToSD(""); // Then run the method to copy the file. 
            
            //lancio la prima activity di servizio 
            //Intent home = new Intent(ApplicationExt.this, SplashScreenActivity.class); 
            //startActivity(home); 
        }  
        else 
        {  
            // Otherwise start the application here: 
        	mDb = new DbAdapterGrasp(mInstance);
            //Intent home = new Intent(ApplicationExt.this, SplashScreenActivity.class); 
            //startActivity(home); 
        } 
    } 
      
    private void SetDirectory() throws IOException  
    { 
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))  
        { 
            File grasp = new File(basepath + "grasp"); 
            if (!grasp.exists()) 
            { 
                grasp.mkdirs();     
            } 
            copyGraspDatabaseToSD(""); // Then run the method to copy the file. 
  
        }  
        else if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED_READ_ONLY))  
        { 
            Toast.makeText(getApplicationContext(), getString(R.string.update_toast), Toast.LENGTH_SHORT).show(); 
        } 
  
    } 
      
    private void copyGraspDatabaseToSD(String path) throws IOException  
    { 
    	mDb = new DbAdapterGrasp(mInstance);
    	/*
    	//CORRETTO
        AssetManager assetManager = this.getAssets(); 
        String assets[] = null; 
        try 
        { 
            Log.i("tag", "copyGraspToSD() " + path); 
            assets = assetManager.list(path); 
            if (assets.length == 0) 
            { 
                copyFile(path); 
            }  
            else
            { 
                String fullPath =  basepath +  path; 
                Log.i("tag", "path= "+ fullPath); 
              
                File dir = new File(fullPath); 
                if (!dir.exists() && !path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit")) 
                    if (!dir.mkdirs()); 
                        Log.i("tag", "Non può creare la cartella "+ fullPath); 
                for (int i = 0; i < assets.length; ++i) 
                { 
                    String p; 
                    if (path.equals("")) 
                        p = ""; 
                    else 
                        p = path + "/"; 
  
                    if (!path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit")) 
                    	copyGraspToSD( p + assets[i]); 
                } 
            } 
        }  
        catch (IOException ex)  
        { 
            Log.e("tag", "I/O Exception", ex); 
        } 
      
        /* 
        for(int i=0; i<files.length; i++) 
        { 
            InputStream in = null; 
            OutputStream out = null; 
            try 
            { 
                in = assetManager.open(files[i]); 
                out = new FileOutputStream(basepath + "/" + files[i]); 
                copyFile(in, out); 
                in.close(); 
                in = null; 
                out.flush(); 
                out.close(); 
                out = null; 
            } 
            catch (Exception e)  
            { 
                // TODO: handle exception 
                Log.e(TAG, e.getMessage()); 
            } 
        } 
        */
    } 
    
    /*
    public void onTerminate() {
        // Close the internal db
        getDatabaseAdapter().close(DatabaseAdapter.INTERNAL);

        Log.e(LOG_TAG, "::onTerminate::");
        super.onTerminate();
    }
    */
    
    public void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
  
    private void copyFile(String filename) throws IOException 
    { 
        AssetManager assetManager = this.getAssets(); 
  
        InputStream in = null; 
        OutputStream out = null; 
        String newFileName = null; 
        try 
        { 
            Log.i("tag", "copyFile() " + filename); 
            in = assetManager.open(filename); 
            if (filename.endsWith(".jpg")) // extension was added to avoid compression on APK file 
                newFileName = basepath + filename.substring(0, filename.length()-4); 
            else
                newFileName = basepath + filename; 
            out = new FileOutputStream(newFileName); 
  
            byte[] buffer = new byte[1024]; 
            int read; 
            while ((read = in.read(buffer)) != -1) 
            { 
                out.write(buffer, 0, read); 
            } 
            in.close(); 
            in = null; 
            out.close(); 
            out.flush(); 
            out = null; 
        }  
        catch (Exception e) 
        { 
            Log.e("tag", "Exception in copyFile() of "+newFileName); 
            Log.e("tag", "Exception in copyFile() "+e.toString()); 
        } 
        /* 
        byte[] buffer = new byte[1024]; 
        int read; 
        while((read = in.read(buffer)) != -1) 
        { 
          out.write(buffer, 0, read); 
        } 
        */
    } 
    
    //TASK DI COPIA DEI CONTENUTI
    private class CopyTask extends AsyncTask<Void, Void, Void>
    {   
        @Override
        protected Void doInBackground(Void... params) 
        {
            Looper.prepare();
            // TODO Auto-generated method stub
            try
            {
                FirstRun();
                //copyDB();
            } 
            catch (IOException e1) 
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            return null;
        }
        protected void onProgressUpdate(Void... unsued) 
        {
            Toast messaggio = Toast.makeText(getApplicationContext(), "E' in corso la copia dei contenuti...", Toast.LENGTH_LONG);  
            messaggio.show();   
        }
    }
} 