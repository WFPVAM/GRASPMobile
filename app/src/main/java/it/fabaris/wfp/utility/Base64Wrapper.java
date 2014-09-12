/*******************************************************************************
 * Copyright (c) 2012 Fabaris SRL.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 *     Fabaris SRL - initial API and implementation
 ******************************************************************************/
package it.fabaris.wfp.utility;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Wrapper class for accessing Base64 functionality.
 * This allows API Level 7 deployment of ODK Collect while
 * enabling API Level 8 and higher phone to support encryption.
 *
 * Class not used in GRASP solution
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class Base64Wrapper {

    private static final int FLAGS = 2;// NO_WRAP
    private Class<?> base64 = null;

    public Base64Wrapper() throws ClassNotFoundException {
        base64 = this.getClass().getClassLoader()
                .loadClass("android.util.Base64");
    }

    public String encodeToString(byte[] ba) {
        Class<?>[] argClassList = new Class[]{byte[].class, int.class};
        try {
            Method m = base64.getDeclaredMethod("encode", argClassList);
            Object[] argList = new Object[]{ ba, FLAGS };
            Object o = m.invoke(null, argList);
            byte[] outArray = (byte[]) o;
            String s = new String(outArray, "UTF-8");
            return s;
        } catch (SecurityException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.toString());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.toString());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.toString());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.toString());
        }
    }

    public byte[] decode(String base64String) {
        Class<?>[] argClassList = new Class[]{String.class, int.class};
        Object o;
        try {
            Method m = base64.getDeclaredMethod("decode", argClassList);
            Object[] argList = new Object[]{ base64String, FLAGS };
            o = m.invoke(null, argList);
        } catch (SecurityException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.toString());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.toString());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.toString());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.toString());
        }
        return (byte[]) o;
    }
}
