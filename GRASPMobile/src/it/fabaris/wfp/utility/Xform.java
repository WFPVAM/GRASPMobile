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

/**
 * Class that define the xform object
 * 
 * @author Fabaris Srl: Leonardo Luciani
 * 	www.fabaris.it
 *
 */

public class Xform {
	 public String nome = null;;
	 public String version = null;
	 
	 public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}

	public Xform(String nome, String version) {
		super();
		this.nome = nome;
		this.version = version;
	}
}
