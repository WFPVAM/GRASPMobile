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

import org.apache.http.auth.AuthSchemeRegistry;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

/**
 * Class not used in GRASP solution
 *
 */

public class EnhancedHttpClient extends DefaultHttpClient {
    public EnhancedHttpClient(HttpParams params) {
        super(params);
    }

    @Override
    protected AuthSchemeRegistry createAuthSchemeRegistry() {
        AuthSchemeRegistry registry = new AuthSchemeRegistry();
        registry.register(
                AuthPolicy.BASIC,
                new BasicSchemeFactory());
        registry.register(
                AuthPolicy.DIGEST,
                new EnhancedDigestSchemeFactory());
        return registry;
    }
}

