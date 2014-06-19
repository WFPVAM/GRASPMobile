package it.fabaris.wfp.activities;

import android.app.Activity;
import android.os.Bundle;

/**
 * Class that defines the help area.
 * 
 */

public class HelpActivity extends Activity{
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.help));
	}
}
