/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package it.fabaris.wfp.logic;

import java.io.Serializable;

/**
 *
 *	Class that implements the details of an xform 
 *
 */

public class FormDetails implements Serializable {
    private static final long serialVersionUID = 1L;
    public final String errorStr;
    public final String formName;
    public final String downloadUrl;
    public final String manifestUrl;
    public String formID;

    public String getFormID() {
        return formID;
    }

    public void setFormID(String formID) {
        this.formID = formID;
    }

    public FormDetails(){
        manifestUrl = null;
        downloadUrl = null;
        formName = null;
        formID = null;
        errorStr = null;
    }

    public FormDetails(String error) {
        manifestUrl = null;
        downloadUrl = null;
        formName = null;
        formID = null;
        errorStr = error;
    }

    public FormDetails(String name, String url, String manifest, String id) {
        manifestUrl = manifest;
        downloadUrl = url;
        formName = name;
        formID = id;
        errorStr = null;
    }

}
