package com.omi.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.omi.bean.DC;

public class ReadXmlConfig {
	/*
	 * public static void main(String argv[]) {
	 * 
	 * ReadXmlConfig config = new ReadXmlConfig();
	 * 
	 * System.out.println(config.method("DC"));
	 * System.err.println(config.method("PATCHGROUP"));
	 * 
	 * }
	 */

	public static Document getDocument() {
		try {
			File fXmlFile = new File("engage_deployment_config.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;

			dBuilder = dbFactory.newDocumentBuilder();

			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			return doc;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public List<String> getAllPatchGroupID() {
		List<String> patchGroupIdList = new ArrayList<String>();
		Document doc = getDocument();
		NodeList nList = doc.getElementsByTagName("PATCHGROUP");
		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);
			// System.out.println("\nCurrent Element :" + nNode.getNodeName());
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				patchGroupIdList.add(eElement.getAttribute("PATCHGROUPID"));
			}
		}

		return patchGroupIdList;
	}

	public List<String> getPackageNames() {
		List<String> packages = new LinkedList<String>();
		Document doc = getDocument();
		NodeList nList = doc.getElementsByTagName("ENGAGEPACKAGE");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			// System.out.println(nList.getLength());
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				for (int serverIndex = 0; serverIndex < eElement.getElementsByTagName("PACKAGE")
						.getLength(); serverIndex++) {
					// System.out.println(serverIndex);
					packages.add(eElement.getElementsByTagName("PACKAGE").item(serverIndex).getTextContent());

				}
			}
		}

		return packages;
	}

	public List<DC> method(List<String> selectedPatchGroup) {
		return method(selectedPatchGroup, 0);
	}

	public List<DC> method(List<String> selectedPatchGroup, int targeted) {
		List<DC> DCList = new ArrayList<DC>();
		try {

			Document doc = getDocument();
			NodeList nList = doc.getElementsByTagName("PATCHGROUP");
			// System.out.println("----------------------------");

			DC objDC;
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				List<DC> eachPatchGroup = new ArrayList<DC>();

				// System.out.println("\nCurrent Element :" + nNode.getNodeName());
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					// System.out.println("Patchgroup ID : " +
					// eElement.getAttribute("PATCHGROUPID"));

					String patchGroupId = eElement.getAttribute("PATCHGROUPID");

					if (selectedPatchGroup.contains(eElement.getAttribute("PATCHGROUPID").trim())) {
						eachPatchGroup = new ArrayList<DC>();
						NodeList childNode = eElement.getChildNodes();
						for (int i = 0; i < childNode.getLength(); i++) {
							objDC = new DC();
							Node currentDC = childNode.item(i);
							if (currentDC.getNodeType() == Node.ELEMENT_NODE) {
								Element elementDC = (Element) currentDC;

								if (targeted == 2 && elementDC.getElementsByTagName("ServerName").getLength() == 1) {

									objDC.setPatchGroupId(patchGroupId);

									// System.out.println("DCID : " + elementDC.getAttribute("DCID"));
									objDC.setDcid(Integer.parseInt(elementDC.getAttribute("DCID")));

									// System.out.println("ServerName : "
									// + elementDC.getElementsByTagName("ServerName").item(0).getTextContent());

									String serverName = "";

									for (int serverIndex = 0; serverIndex < elementDC.getElementsByTagName("ServerName")
											.getLength(); serverIndex++) {
										// System.out.println(serverIndex);
										serverName += elementDC.getElementsByTagName("ServerName").item(serverIndex)
												.getTextContent();
										if (serverIndex != elementDC.getElementsByTagName("ServerName").getLength()
												- 1) {
											serverName += ",";
										}

									}

									// System.out.println(serverName);
									objDC.setServerName(serverName);

									// System.out.println("SiteName : "
									// + elementDC.getElementsByTagName("SiteName").item(0).getTextContent());
									objDC.setSiteName(
											elementDC.getElementsByTagName("SiteName").item(0).getTextContent());
									eachPatchGroup.add(objDC);
								}

								else if (elementDC.getElementsByTagName("ServerName").getLength() > targeted) {

									objDC.setPatchGroupId(patchGroupId);

									// System.out.println("DCID : " + elementDC.getAttribute("DCID"));
									objDC.setDcid(Integer.parseInt(elementDC.getAttribute("DCID")));

									// System.out.println("ServerName : "
									// + elementDC.getElementsByTagName("ServerName").item(0).getTextContent());

									String serverName = "";

									for (int serverIndex = 0; serverIndex < elementDC.getElementsByTagName("ServerName")
											.getLength(); serverIndex++) {
										// System.out.println(serverIndex);
										serverName += elementDC.getElementsByTagName("ServerName").item(serverIndex)
												.getTextContent();
										if (serverIndex != elementDC.getElementsByTagName("ServerName").getLength()
												- 1) {
											serverName += ",";
										}

									}

									// System.out.println(serverName);
									objDC.setServerName(serverName);

									// System.out.println("SiteName : "
									// + elementDC.getElementsByTagName("SiteName").item(0).getTextContent());
									objDC.setSiteName(
											elementDC.getElementsByTagName("SiteName").item(0).getTextContent());
									eachPatchGroup.add(objDC);
								}
							}

						}
					}
					Collections.sort(eachPatchGroup);
					DCList.addAll(eachPatchGroup);
				}

			}

		} catch (

		Exception e) {
			e.printStackTrace();
		}
		return DCList;
	}
}
