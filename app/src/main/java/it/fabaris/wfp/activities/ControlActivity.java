package it.fabaris.wfp.activities;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_setting);

        language = Locale.getDefault().toString();

        password = (EditText) findViewById(R.id.password_edit);
        login = (Button) findViewById(R.id.button_in);

        /**
         * on Click we check if the psw inserted is correct
         */
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

    }
}
