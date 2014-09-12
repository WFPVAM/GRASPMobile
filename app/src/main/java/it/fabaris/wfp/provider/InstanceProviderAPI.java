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
package it.fabaris.wfp.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Class not used in GRASP solution
 *
 */

public final class InstanceProviderAPI {
    public final static String AUTHORITY = "mnt.sdcard.fabarisODK";
    public InstanceProviderAPI() {}

    //status for instances
    public static final String STATUS_SAVED = "saved";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_SUBMITTED = "submitted";
    public static final String STATUS_FINALIZED = "finalized";
    public static final String STATUS_SUBMISSION_FAILED = "submissionFailed";

    public static final class InstanceColumns implements BaseColumns {
        // This class cannot be instantiated
        private InstanceColumns() {}
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/instances");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.instances";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.odk.instance";
        // These are the only things needed for an insert
        public static final String DISPLAY_NAME = "displayName";
        public static final String SUBMISSION_URI = "submissionUri";
        public static final String INSTANCE_FILE_PATH = "instanceFilePath";
        public static final String JR_FORM_ID = "jrFormId";

        // these are generated for you (but you can insert something else if you want)
        public static final String STATUS = "status";
        public static final String CAN_EDIT_WHEN_COMPLETE = "canEditWhenComplete";
        public static final String LAST_STATUS_CHANGE_DATE = "date";
        public static final String DISPLAY_SUBTEXT = "displaySubtext";

//        public static final String DEFAULT_SORT_ORDER = "modified DESC";
//        public static final String TITLE = "title";
//        public static final String NOTE = "note";
//        public static final String CREATED_DATE = "created";
//        public static final String MODIFIED_DATE = "modified";
    }
}
