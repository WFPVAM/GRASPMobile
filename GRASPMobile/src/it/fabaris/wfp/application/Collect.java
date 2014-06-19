/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package it.fabaris.wfp.application;


import java.io.File;

import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;

import it.fabaris.wfp.activities.R;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.preference.PreferenceManager;

/**
 * Class that defines folder and some options
 *
 */

public class Collect extends Application {

    /**
     *  Storage paths
     */
	public static final String FABARISODK_ROOT = Environment.getExternalStorageDirectory() + "/GRASP";
    public static final String FORMS_PATH = FABARISODK_ROOT + "/forms";//cartella che contiene gli xml con i template delle form
    public static final String INSTANCES_PATH = FABARISODK_ROOT + "/instances";//cartella che contiene gli xml con le risposte alle domande
    public static final String CACHE_PATH = FABARISODK_ROOT + "/.cache";
    public static final String METADATA_PATH = FABARISODK_ROOT + "/metadata/";//cartella che contiene il forms.db e il message.db
    public static final String TMPFILE_PATH = CACHE_PATH + "/tmp.jpg";
    public static final String DEFAULT_FONTSIZE = "18";
    public static final String DEFAULT_TEXT_FORECOLOR = "#000066";
    public static final String DEFAULT_TEXT_BACKGROUNDCOLOR = "#00FFFF";
    public static final String DEFAULT_TEXT_MANDATORY_FORECOLOR = "#007CF9";
    public static final String DEFAULT_TEXT_MANDATORY_BACKGROUNDCOLOR = "#00FFFF";
    public static final String DEFAULT_TEXT_ERROR_FORECOLOR = "#660000";
    public static final String DEFAULT_TEXT_ERROR_BACKGROUNDCOLOR = "#00FFFF";
    
    private HttpContext localContext = null;
    private static Collect singleton = null;

    public static Collect getInstance() {
        return singleton;
    }

    public String getVersionedAppName() {
        String versionDetail = "";
		try {
	        PackageInfo pinfo;
			pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
	        int versionNumber = pinfo.versionCode;
	        String versionName = pinfo.versionName;
	        versionDetail = " " + versionName + "(" + versionNumber + ")";
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return getString(R.string.app_name) + versionDetail;
    }
    /**
     * Creates required directories on the SDCard (or other external storage)
     * @throws RuntimeException if there is no SDCard or the directory exists as a non directory
     */
    public static void createODKDirs() throws RuntimeException {
        String cardstatus = Environment.getExternalStorageState();
        if (cardstatus.equals(Environment.MEDIA_REMOVED)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTED)
                || cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)
                || cardstatus.equals(Environment.MEDIA_SHARED)) {
            RuntimeException e =
                new RuntimeException("ODK reports :: SDCard error: "+ Environment.getExternalStorageState());
            throw e;
        }

        String[] dirs = {FABARISODK_ROOT, FORMS_PATH, INSTANCES_PATH, CACHE_PATH, METADATA_PATH};

        for (String dirName : dirs) {
            File dir = new File(dirName);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    RuntimeException e =
                        new RuntimeException("ODK reports :: Cannot create directory: " + dirName);
                    throw e;
                }
            } else {
                if (!dir.isDirectory()) {
                    RuntimeException e =
                        new RuntimeException("ODK reports :: " + dirName
                                + " exists, but is not a directory");
                    throw e;
                }
            }
        }
    }

    /**
     * Shared HttpContext so a user doesn't have to re-enter login information
     * @return
     */
    public synchronized HttpContext getHttpContext() {
        if (localContext == null) {
            /**
             *  set up one context for all HTTP requests so that authentication
             *  and cookies can be retained.
             */
            localContext = new SyncBasicHttpContext(new BasicHttpContext());

            /**
             *  establish a local cookie store for this attempt at downloading...
             */
            CookieStore cookieStore = new BasicCookieStore();
            localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

            /**
             *  and establish a credentials provider.  Default is 7 minutes.
             *  CredentialsProvider credsProvider = new AgingCredentialsProvider(7 * 60 * 1000);
             *  localContext.setAttribute(ClientContext.CREDS_PROVIDER, credsProvider);
             */
        }
        return localContext;
    }

    @Override
    public void onCreate() {
        singleton = this;
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        super.onCreate();
    }

}
