package it.fabaris.wfp.activities;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Class is responsible for displaying credits area
 * 
 *
 */

public class CreditsActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.credits);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.credits));
        
        try {
			String version = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0 ).versionName;
			TextView textView = (TextView)findViewById(R.id.textView5);
			textView.setText("Version "+version);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
