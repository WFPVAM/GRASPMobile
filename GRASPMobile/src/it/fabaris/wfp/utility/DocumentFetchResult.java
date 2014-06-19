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

import org.kxml2.kdom.Document;

/**
 * Class not used in GRASP solution
 *
 */

public class DocumentFetchResult {
	public final String errorMessage;
	public final int responseCode;
	public final Document doc;
	public final boolean isOpenRosaResponse;
	
	public DocumentFetchResult(String msg, int response){
		responseCode = response;
		errorMessage = msg;
		doc = null;
		isOpenRosaResponse = false;
	}
	public DocumentFetchResult(Document doc, boolean isOpenRosaResponse){
		responseCode = 0;
		errorMessage = null;
		this.doc = doc;
		this.isOpenRosaResponse = isOpenRosaResponse;
	}
}
