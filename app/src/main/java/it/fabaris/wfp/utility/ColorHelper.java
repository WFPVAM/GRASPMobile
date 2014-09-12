package it.fabaris.wfp.utility;

import it.fabaris.wfp.activities.R;
import it.fabaris.wfp.widget.QuestionWidget;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.PreferenceManager;

public class ColorHelper {

    private int defaultBackgroundColor;
    private int defaultForeColor;
    private int errorBackgroundColor;
    private int errorForeColor;
    private int mandatoryBackgroundColor;
    private int mandatoryForeColor;
    private int readOnlyBackgroundColor;
    private int readOnlyForeColor;
    private int readOnlyErrorBackgroundColor;
    private int readOnlyErrorForeColor;

    private int readOnlyBackgroundInvisible;
    private int readOnlyForegroundInvisible;

    public ColorHelper(Context context, Resources res) {
        //SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        defaultBackgroundColor=res.getColor(R.color.bg_std);
        defaultForeColor=res.getColor(R.color.fg_std);

        mandatoryBackgroundColor=res.getColor(R.color.bg_mand);
        mandatoryForeColor=res.getColor(R.color.fg_mand);

        errorBackgroundColor=res.getColor(R.color.bg_err);
        errorForeColor=res.getColor(R.color.fg_err);

        readOnlyBackgroundColor=res.getColor(R.color.bg_readonly);
        readOnlyForeColor=res.getColor(R.color.fg_readonly);

        readOnlyBackgroundInvisible=res.getColor(R.color.bg_readonly_inv);
        readOnlyForegroundInvisible=res.getColor(R.color.fg_readonly_inv);

        readOnlyErrorBackgroundColor=res.getColor(R.color.bg_readonly_err);
        readOnlyErrorForeColor=res.getColor(R.color.fg_readonly_err);
		/*
		defaultBackgroundColor=loadColor(settings, PreferencesActivity.TEXT_BACKGROUND_COLOR, Collect.DEFAULT_TEXT_BACKGROUNDCOLOR);
        defaultForeColor=loadColor(settings,PreferencesActivity.TEXT_FOREGROUND_COLOR, Collect.DEFAULT_TEXT_FORECOLOR);
        mandatoryBackgroundColor=loadColor(settings, PreferencesActivity.TEXT_MANDATORY_BACKGROUND_COLOR, Collect.DEFAULT_TEXT_MANDATORY_BACKGROUNDCOLOR);
        mandatoryForeColor=loadColor(settings,PreferencesActivity.TEXT_MANDATORY_FOREGROUND_COLOR, Collect.DEFAULT_TEXT_MANDATORY_FORECOLOR);
        errorBackgroundColor=loadColor(settings, PreferencesActivity.TEXT_ERROR_BACKGROUND_COLOR, Collect.DEFAULT_TEXT_ERROR_BACKGROUNDCOLOR);
        errorForeColor=loadColor(settings,PreferencesActivity.TEXT_ERROR_FOREGROUND_COLOR, Collect.DEFAULT_TEXT_ERROR_FORECOLOR);
        */
    }


    private int loadColor(SharedPreferences settings, String key, String defaultvalue) {
        String colorasstring;
        colorasstring=settings.getString(key, defaultvalue);
        return parseIntMultiBase(colorasstring);
    }

    private int parseIntMultiBase(String colorasstring) {
        int parsed=0;
        parsed=Color.parseColor(colorasstring);
        return parsed;
    }


    public int getDefaultBackgroundColor() {
        return defaultBackgroundColor;
    }
    public int getDefaultForeColor() {
        return defaultForeColor;
    }
    public int getMandatoryBackgroundColor() {
        return mandatoryBackgroundColor;
    }
    public int getMandatoryForeColor() {
        return mandatoryForeColor;
    }
    public int getErrorBackgroundColor() {
        return errorBackgroundColor;
    }
    public int getErrorForeColor() {
        return errorForeColor;
    }

    public int getReadOnlyBackgroundColor() {
        return readOnlyBackgroundColor;
    }

    public int getReadOnlyForeColor() {
        return readOnlyForeColor;
    }

    public int getErrorReadOnlyBackgroundColor() {
        return readOnlyErrorBackgroundColor;
    }

    public int getErrorReadOnlyForeColor() {
        return readOnlyErrorForeColor;
    }


    public int getReadOnlyBackgroundInvisible() {
        return readOnlyBackgroundInvisible;
    }


    public void setReadOnlyBackgroundInvisible(int readOnlyBackgroundInvisible) {
        this.readOnlyBackgroundInvisible = readOnlyBackgroundInvisible;
    }


    public int getReadOnlyForegroundInvisible() {
        return readOnlyForegroundInvisible;
    }


    public void setReadOnlyForegroundInvisible(int readOnlyForegroundInvisible) {
        this.readOnlyForegroundInvisible = readOnlyForegroundInvisible;
    }



}
