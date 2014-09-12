/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 ******************************************************************************/
package it.fabaris.wfp.logic;

import org.javarosa.core.reference.PrefixedRootFactory;
import org.javarosa.core.reference.Reference;

/**
 * Class not used in GRASP solution
 *
 */

public class FileReferenceFactory extends PrefixedRootFactory {

    String localRoot;

    public FileReferenceFactory(String localRoot) {
        super(new String[] {"file"});
        this.localRoot = localRoot;
    }

    @Override
    protected Reference factory(String terminal, String URI) {
        return new FileReference(localRoot, terminal);
    }

}
