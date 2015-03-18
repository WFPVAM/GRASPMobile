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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.javarosa.xform.parse.XFormParser;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Class not used in GRASP solution
 *
 */

public class FileUtils {
    private final static String t = "FileUtils";

    // Used to validate and display valid form names.
    public static final String VALID_FILENAME = "[ _\\-A-Za-z0-9]*.x[ht]*ml";

    public static String FORMID = "formid";
    public static String UI = "uiversion";
    public static String MODEL = "modelversion";
    public static String TITLE = "title";
    public static String SUBMISSIONURI = "submission";
    public static String BASE64_RSA_PUBLIC_KEY = "base64RsaPublicKey";


    public static boolean createFolder(String path) {
        boolean made = true;
        File dir = new File(path);
        if (!dir.exists()) {
            made = dir.mkdirs();
        }
        return made;
    }



    public static byte[] getFileAsBytes(File file) {
        byte[] bytes = null;
        InputStream is = null;
        try {
            is = new FileInputStream(file);

            // Get the size of the file
            long length = file.length();
            if (length > Integer.MAX_VALUE) {
                Log.e(t, "File " + file.getName() + "is too large");
                return null;
            }

            // Create the byte array to hold the data
            bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int read = 0;
            try {
                while (offset < bytes.length && read >= 0) {
                    read = is.read(bytes, offset, bytes.length - offset);
                    offset += read;
                }
            } catch (IOException e) {
                Log.e(t, "Cannot read " + file.getName());
                e.printStackTrace();
                return null;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                try {
                    throw new IOException("Could not completely read file " + file.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            return bytes;

        } catch (FileNotFoundException e) {
            Log.e(t, "Cannot find " + file.getName());
            e.printStackTrace();
            return null;

        } finally {
            // Close the input stream
            try {
                is.close();
            } catch (IOException e) {
                Log.e(t, "Cannot close input stream for " + file.getName());
                e.printStackTrace();
                return null;
            }
        }
    }


    public static byte[] convertImageToByte(File file) throws Exception {
        byte[] convertedImageByte = null;
        FileInputStream fis = null;

        fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        for (int readNum; (readNum = fis.read(buf)) != -1; ) {
            bos.write(buf, 0, readNum);
            System.out.println("read " + readNum + " bytes,");
        }
        convertedImageByte = bos.toByteArray();

        return convertedImageByte;
    }


    public static String getMd5Hash(File file) {
        try {
            // CTS (6/15/2010) : stream file through digest instead of handing it the byte[]
            MessageDigest md = MessageDigest.getInstance("MD5");
            int chunkSize = 256;

            byte[] chunk = new byte[chunkSize];

            // Get the size of the file
            long lLength = file.length();

            if (lLength > Integer.MAX_VALUE) {
                Log.e(t, "File " + file.getName() + "is too large");
                return null;
            }

            int length = (int) lLength;

            InputStream is = null;
            is = new FileInputStream(file);

            int l = 0;
            for (l = 0; l + chunkSize < length; l += chunkSize) {
                is.read(chunk, 0, chunkSize);
                md.update(chunk, 0, chunkSize);
            }

            int remaining = length - l;
            if (remaining > 0) {
                is.read(chunk, 0, remaining);
                md.update(chunk, 0, remaining);
            }
            byte[] messageDigest = md.digest();

            BigInteger number = new BigInteger(1, messageDigest);
            String md5 = number.toString(16);
            while (md5.length() < 32)
                md5 = "0" + md5;
            is.close();
            return md5;

        } catch (NoSuchAlgorithmException e) {
            Log.e("MD5", e.getMessage());
            return null;

        } catch (FileNotFoundException e) {
            Log.e("No Cache File", e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e("Problem reading from file", e.getMessage());
            return null;
        }

    }


    public static Bitmap getBitmapScaledToDisplay(File f, int screenHeight, int screenWidth) {
        // Determine image size of f
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getAbsolutePath(), o);

        int heightScale = o.outHeight / screenHeight;
        int widthScale = o.outWidth / screenWidth;

        // Powers of 2 work faster, sometimes, according to the doc.
        // We're just doing closest size that still fills the screen.
        int scale = Math.max(widthScale, heightScale);

        // get bitmap with scale ( < 1 is the same as 1)
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = scale;
        Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
        if (b != null) {
            Log.i(t,
                    "Screen is " + screenHeight + "x" + screenWidth + ".  Image has been scaled down by "
                            + scale + " to " + b.getHeight() + "x" + b.getWidth());
        }
        return b;
    }


    public static void copyFile(File sourceFile, File destFile) {
        if (sourceFile.exists()) {
            FileChannel src;
            try {
                src = new FileInputStream(sourceFile).getChannel();
                FileChannel dst = new FileOutputStream(destFile).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            } catch (FileNotFoundException e) {
                Log.e(t, "FileNotFoundExeception while copying audio");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(t, "IOExeception while copying audio");
                e.printStackTrace();
            }
        } else {
            Log.e(t, "Source file does not exist: " + sourceFile.getAbsolutePath());
        }

    }



    public static HashMap<String, String> parseXML(File xmlFile) {
        HashMap<String, String> fields = new HashMap<String, String>();
        InputStream is;
        try {
            is = new FileInputStream(xmlFile);
        } catch (FileNotFoundException e1) {
            throw new IllegalStateException(e1);
        }

        InputStreamReader isr;
        try {
            isr = new InputStreamReader(is, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            Log.w(t, "UTF 8 encoding unavailable, trying default encoding");
            isr = new InputStreamReader(is);
        }

        if (isr != null) {

            Document doc;
            try {
                doc = XFormParser.getXMLDocument(isr);
            } finally {
                try {
                    isr.close();
                } catch (IOException e) {
                    Log.w(t, xmlFile.getAbsolutePath() + " Error closing form reader");
                    e.printStackTrace();
                }
            }

            String xforms = "http://www.w3.org/2002/xforms";
            String html = doc.getRootElement().getNamespace();

            Element head = doc.getRootElement().getElement(html, "head");
            Element title = head.getElement(html, "title");
            if (title != null) {
                fields.put(TITLE, XFormParser.getXMLText(title, true));
            }

            Element model = getChildElement(head, "model");
            Element cur = getChildElement(model,"instance");

            int idx = cur.getChildCount();
            int i;
            for (i = 0; i < idx; ++i) {
                if (cur.isText(i))
                    continue;
                if (cur.getType(i) == Node.ELEMENT) {
                    break;
                }
            }

            if (i < idx) {
                cur = cur.getElement(i); // this is the first data element
                String id = cur.getAttributeValue(null, "id");
                String xmlns = cur.getNamespace();
                String modelVersion = cur.getAttributeValue(null, "version");
                String uiVersion = cur.getAttributeValue(null, "uiVersion");

                fields.put(FORMID, (id == null) ? xmlns : id);
                fields.put(MODEL, (modelVersion == null) ? null : modelVersion);
                fields.put(UI, (uiVersion == null) ? null : uiVersion);
            } else {
                throw new IllegalStateException(xmlFile.getAbsolutePath() + " could not be parsed");
            }
            try {
                Element submission = model.getElement(xforms, "submission");
                String submissionUri = submission.getAttributeValue(null, "action");
                fields.put(SUBMISSIONURI, (submissionUri == null) ? null : submissionUri);
                String base64RsaPublicKey = submission.getAttributeValue(null, "base64RsaPublicKey");
                fields.put(BASE64_RSA_PUBLIC_KEY,
                        (base64RsaPublicKey == null || base64RsaPublicKey.trim().length() == 0)
                                ? null : base64RsaPublicKey.trim());
            } catch (Exception e) {
                Log.i(t, xmlFile.getAbsolutePath() + " does not have a submission element");
                // and that's totally fine.
            }

        }
        return fields;
    }

    // needed because element.getelement fails when there are attributes
    private static Element getChildElement(Element parent, String childName) {
        Element e = null;
        int c = parent.getChildCount();
        int i = 0;
        for (i = 0; i < c; i++) {
            if (parent.getType(i) == Node.ELEMENT) {
                if (parent.getElement(i).getName().equalsIgnoreCase(childName)) {
                    return parent.getElement(i);
                }
            }
        }
        return e;
    }
//****************************** added for the photo upload *********************************//
public static byte[] compressImage(String imagePath, int newWidth, int newHeight, int quality) {
    //parameter float rotateDegree;
    byte[] compresedImageBytes = null;
    ByteArrayOutputStream baos = null;
    try {
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inScaled = false;
        Bitmap imageFile = decodeFile(imagePath, newWidth, newHeight);

        //Compress Image with the given quality
        baos = new ByteArrayOutputStream();
        imageFile.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        compresedImageBytes = baos.toByteArray();
    } catch (Exception e) {
        e.printStackTrace();
    }finally
    {
        try
        {
            if (baos != null)
            {
                baos.flush();
                baos.close();
            }
        }catch(IOException e)
        {

        }
    }
    return compresedImageBytes;
}

    private static Bitmap decodeFile(String fileName, int newWidth, int newHeight){
        try {
            //Decode image size
            BitmapFactory.Options justDecodeBoundsOption = new BitmapFactory.Options();
            justDecodeBoundsOption.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(fileName),null,justDecodeBoundsOption);

            BitmapFactory.Options scaleOption = new BitmapFactory.Options();

            //The new size we want to scale to
            //final int REQUIRED_SIZE=80;
            if((newWidth != 0 && newHeight != 0)
                    && (justDecodeBoundsOption.outWidth > newWidth && justDecodeBoundsOption.outHeight > newHeight))
            {
                //Find the correct scale value. It should be the power of 2.
                int scale=1;

                //while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
                while(justDecodeBoundsOption.outWidth/scale/2>=newWidth && justDecodeBoundsOption.outHeight/scale/2>=newHeight)
                    scale*=2;

                //Decode with inSampleSize
                scaleOption.inSampleSize=scale;
            }

            //o2.inScaled = false;
            return BitmapFactory.decodeStream(new FileInputStream(fileName), null, scaleOption);
        } catch (FileNotFoundException e) {}
        return null;
    }
}
