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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Class not used in GRASP solution
 *
 */

public class UrlUtils {

    public static boolean isValidUrl(String url) {

        try {
            new URL(URLDecoder.decode(url, "utf-8"));
            return true;
        } catch (MalformedURLException e) {
            return false;
        } catch (UnsupportedEncodingException e) {
            return false;
        }

    }

}
