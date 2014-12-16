package it.fabaris.wfp.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Class that defines the database that hold the new form
 * 
 *
 */

public final class MessageProviderAPI {
	public final static String AUTHORITY = "mnt.sdcard.fabarisODK.message";
    public MessageProviderAPI() {}
    
    public static final class MessageColumns implements BaseColumns {
        private MessageColumns() {}
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/message");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.message";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.odk.message";
        
        public static final String FORM_ID = "formId";
        public static final String FORM_NAME = "formName";
        public static final String FORM_IMPORTED = "formImported";
        public static final String FORM_ENCODED_TEXT = "formEncodedText";
        public static final String FORM_TEXT = "formText";
        public static final String DATE = "date";
    }
}
