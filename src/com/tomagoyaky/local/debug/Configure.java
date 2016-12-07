package com.tomagoyaky.local.debug;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Configure {

	private static String xmlCfg = "env.cfg";

	public static String getItemValue(String name) throws ParserConfigurationException, SAXException, IOException {
		File file = new File(xmlCfg);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(file);
		Node root = doc.getElementsByTagName("root").item(0);
		NodeList nodeList = root.getChildNodes();
		
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE){
				NamedNodeMap namedNodeMap = node.getAttributes();
				for (int j = 0; j < namedNodeMap.getLength(); j++) {
					if(namedNodeMap.item(j).getNodeValue().equals(name)){
						return namedNodeMap.getNamedItem("path").getNodeValue();
					}
				}
			}
		}
		return null;
	}

}
