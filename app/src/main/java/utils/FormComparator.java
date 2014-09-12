package utils;

import java.util.Comparator;

import object.FormInnerListProxy;

public class FormComparator implements Comparator<FormInnerListProxy>
{
    public int compare(FormInnerListProxy lhs, FormInnerListProxy rhs) {
        float a = Float.valueOf(lhs.getIdDataBase());
        float b = Float.valueOf(rhs.getIdDataBase());
        if (a < b) return 1;
        if (a > b) return -1;
        return 0;
    }
} 