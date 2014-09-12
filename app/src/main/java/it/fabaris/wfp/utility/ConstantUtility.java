package it.fabaris.wfp.utility;

import android.content.Context;
import android.telephony.TelephonyManager;

public class ConstantUtility {


    private static String flagCalculated ="no";
    private static String flagSpinner = "no";
    private static String flagWdigetHasAnswer = "no";
    public static boolean widgetHasASavedAnswer = false;
    public static boolean prova = false;
    public static String protocol = new String();

    public static String getProtocol(){
        return protocol;
    }

    public static void setProtocol(String valore){
        if(valore.equals("no") || valore.equals("si")){
            protocol = valore;
        }
    }

    public static String getFlagCalculated(){
        return flagCalculated;
    }

    public static void setFlagCalculated(String valore){
        if(valore.equals("no") || valore.equals("si")){
            flagCalculated = valore;
        }
    }

    public static String getFlagSpinner(){
        return flagSpinner;
    }

    public static void setFlagSpinner(String valore){
        if(valore.equals("no") || valore.equals("si")){
            flagSpinner = valore;
        }
    }

    public static String getFlagWdigetHasAnswer(){
        return flagWdigetHasAnswer;
    }

    public static void setFlagWdigetHasAnswer(String valore){
        if(valore.equals("no") || valore.equals("si")){
            flagWdigetHasAnswer = valore;
        }
    }


}
