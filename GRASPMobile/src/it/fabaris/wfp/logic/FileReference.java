/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 ******************************************************************************/
package it.fabaris.wfp.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.javarosa.core.reference.Reference;

/**
 * Class not used in GRASP solution
 * 
 */
public class FileReference implements Reference {
    String localPart;
    String referencePart;


    public FileReference(String localPart, String referencePart) {
        this.localPart = localPart;
        this.referencePart = referencePart;
    }


    private String getInternalURI() {
        return "/" + localPart + referencePart;
    }


    @Override
    public boolean doesBinaryExist() {
        return new File(getInternalURI()).exists();
    }


    @Override
    public InputStream getStream() throws IOException {
        return new FileInputStream(getInternalURI());
    }


    @Override
    public String getURI() {
        return "jr://file" + referencePart;
    }


    @Override
    public boolean isReadOnly() {
        return false;
    }


    @Override
    public OutputStream getOutputStream() throws IOException {
        return new FileOutputStream(getInternalURI());
    }


    @Override
    public void remove() {
        // TODO bad practice to ignore return values
        new File(getInternalURI()).delete();
    }


    @Override
    public String getLocalURI() {
        return getInternalURI();
    }


//	@Override
//	public Reference[] probeAlternativeReferences() {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
