/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package it.fabaris.wfp.provider;

import android.net.Uri;
import android.provider.BaseColumns;
/**
 * 
 * Class that manage the FormsDB
 *
 */
public final class FormProviderAPI  {
	public final static String AUTHORITY = "mnt.sdcard.fabarisODK.forms";
    public FormProviderAPI() {}
    
    /**
     * status for instances
     */
    public static final String STATUS_NEW = "new";
    public static final String STATUS_SAVED = "saved";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_SUBMITTED = "submitted";
    public static final String STATUS_FINALIZED = "finalized";
    public static final String STATUS_SUBMISSION_FAILED = "submissionFailed";
	
    public static final class FormsColumns implements BaseColumns {
        private FormsColumns() {}
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/forms");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.form";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.odk.form";
        
        /**
         *  These are the only things needed for an insert
         */
        public static final String STATUS = "status";
        public static final String DISPLAY_NAME = "displayName";
        public static final String DISPLAY_NAME_INSTANCE = "displayNameInstance";
        public static final String DESCRIPTION = "description";  // can be null
        public static final String JR_FORM_ID = "jrFormId";
        public static final String FORM_FILE_PATH = "formFilePath";
        public static final String SUBMISSION_URI = "submissionUri"; // can be null
        public static final String BASE64_RSA_PUBLIC_KEY = "base64RsaPublicKey"; // can be null
        public static final String INSTANCE_FILE_PATH = "instanceFilePath";

        /**
         *  these are generated for you (but you can insert something else if you want)
         */
        public static final String DISPLAY_SUBTEXT = "displaySubtext";
        public static final String MD5_HASH = "md5Hash";
        public static final String DATE = "date";          
        public static final String JRCACHE_FILE_PATH = "jrcacheFilePath";
        public static final String FORM_MEDIA_PATH = "formMediaPath";      
        
        /**
         *  these are null unless you enter something and aren't currently used
         */
        public static final String MODEL_VERSION = "modelVersion";
        public static final String UI_VERSION = "uiVersion";
        
        /**
         *  this is null on create, and can only be set on an update.
         */
        public static final String LANGUAGE = "language";
        public static final String CAN_EDIT_WHEN_COMPLETE = "canEditWhenComplete";
    }
}
