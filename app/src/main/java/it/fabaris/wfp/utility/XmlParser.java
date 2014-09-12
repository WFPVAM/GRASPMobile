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

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

/**
 * Class that defines the parser of the xml
 *
 * @author Fabaris Srl: Leonardo Luciani
 * 	www.fabaris.it
 *
 */

public class XmlParser {
    Logger logger;
    String dataStore;
    String uiselsect;

    public int getNumGroup(File file) throws SAXException, IOException,XPathExpressionException, ParserConfigurationException {
        int groupcount = 0;
        try {
            File fXmlFile = file;
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression inputcount = xpath.compile("/html/body/group[1]/input");
            NodeList nlinput = (NodeList) inputcount.evaluate(doc,XPathConstants.NODESET);
            int ninput = nlinput.getLength();
            XPathExpression selectcount = xpath.compile("/html/body/group[1]/select");
            NodeList nlselect = (NodeList) selectcount.evaluate(doc,XPathConstants.NODESET);
            int nselect = nlselect.getLength();
            XPathExpression select1count = xpath.compile("/html/body/group[1]/select1");
            NodeList nlselect1 = (NodeList) select1count.evaluate(doc,XPathConstants.NODESET);
            int nselect1 = nlselect1.getLength();
            groupcount = ninput+nselect+nselect1;
        }
        catch (DOMException e) {
            e.printStackTrace();
        }
        return groupcount;
    }
    public String getID(File file) throws SAXException, IOException,XPathExpressionException, ParserConfigurationException {
        String id = null;
        try{
            File fXmlFile = file;
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile("/html/head/model[1]/instance[1]/data[1]/id[1]");
            id = expr.evaluate(doc);
        }
        catch(DOMException e){
            e.printStackTrace();
        }
        return id;
    }
    public String getResponse(File file) throws SAXException, IOException,XPathExpressionException, ParserConfigurationException {
        String resp = null;
        try{
            File fXmlFile = file;
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile("/response/data[1]");
            resp = expr.evaluate(doc);
        }
        catch(DOMException e){
            e.printStackTrace();
        }
        return resp;
    }
    public String getName(File file) throws SAXException, IOException,XPathExpressionException, ParserConfigurationException{
        String name = null;
        try{
            File fXmlFile = file;
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile("/html/head/title[1]");
            name = expr.evaluate(doc);
        }
        catch(DOMException e){
            e.printStackTrace();
        }
        return name;

    }
}
