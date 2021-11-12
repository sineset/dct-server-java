package dct_server_main;
import java.io.IOException;
import java.io.StringReader;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class BarcodeParser {

	DocumentBuilderFactory dbf;
	DocumentBuilder db;

	public BarcodeParser() {
		dbf = DocumentBuilderFactory.newInstance();
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.out.println("DocumentBuilder creating error " + e.getMessage());
			db = null;
			return;
		}
	}

	public Document parse(String xml_data) {
		if (db == null) {
			System.out.println("DocumentBuilder doesnt created");
			return null;
		}
		Document out = null;
		try {
			InputSource is = new InputSource(new StringReader(xml_data));
			out = db.parse(is);
		} catch (SAXException e) {
			System.out.println("SAX parser error " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO error " + e.getMessage());
		}
		return out;
	}

	public ArrayList<ArrayList<String>> sql_formatter_acceptance(Document parsedDocument) {
		String terminal_id = "0";
		ArrayList<ArrayList<String>> lout = new ArrayList<ArrayList<String>>();
		Element root = parsedDocument.getDocumentElement();
		NodeList root_subnodes = root.getChildNodes();
		Node node;
		NodeList sub_nodes;
		NodeList terminal_ids = root.getElementsByTagName("terminal-id");
		for (int i = 0; i < terminal_ids.getLength(); i++) {
			terminal_id = terminal_ids.item(i).getTextContent();
		}
		for (int i = 0; i < root_subnodes.getLength(); i++) {
			node = root_subnodes.item(i);
			if (node.getNodeName() == "acceptance") {
				sub_nodes = node.getChildNodes();
				for (int ia = 0; ia < sub_nodes.getLength(); ia++) {
					ArrayList<String> lnode = new ArrayList<String>();
					lnode.add(terminal_id);
					lnode.add("2");
					lnode.add(sub_nodes.item(ia).getTextContent());
					lout.add(lnode);
//					out += "(" + terminal_id + ",2,'" + sub_nodes.item(ia).getTextContent() + "')";
//					if (ia < sub_nodes.getLength() - 1) {
//						out += ",";
//					}
				}
			} else if (node.getNodeName() == "shipment") {
				sub_nodes = node.getChildNodes();
				for (int ia = 0; ia < sub_nodes.getLength(); ia++) {
					ArrayList<String> lnode = new ArrayList<String>();
					lnode.add(terminal_id);
					lnode.add("3");
					lnode.add(sub_nodes.item(ia).getTextContent());
					lout.add(lnode);
				}
			}
		}
		return lout;
	}
}