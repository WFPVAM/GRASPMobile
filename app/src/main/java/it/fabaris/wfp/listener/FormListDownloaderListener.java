/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package it.fabaris.wfp.listener;

/**
 * Listener used to dismiss a task
 * 
 */

import it.fabaris.wfp.logic.FormDetails;

import java.util.HashMap;

public interface FormListDownloaderListener {
	void formListDownloadingComplete(HashMap<String, FormDetails> value);
}
