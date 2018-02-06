package net.medhand.dyna;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DynaXmlToManifest {
	static ArrayList<String> filesList = new ArrayList<String>();
	static int row=1;
	
	static TreeSet<String> diff = new TreeSet<String>();
	static TreeSet<String> disease = new TreeSet<String>();
	static TreeSet<String> drug = new TreeSet<String>();
	static TreeSet<String> syn = new TreeSet<String>();
	static TreeSet<String> misc = new TreeSet<String>();
	static TreeSet<String> redir = new TreeSet<String>();
	
	static String outputXmlFile = "";
	static String errorXMLPath = "";
	
	static ArrayList<String> errorFiles = new ArrayList<String>();
	
	private final static Logger LOGGER = Logger.getLogger(DynaXmlToManifest.class.getName());
	
	public static void main(String[] args) {
				
		if(args.length!=2) {
			System.out.println("Usage: java -jar DynaManifestGen.jar <path to xml files> <output_file_path>");
			System.exit(0);
		}
	
		outputXmlFile = args[1];
		File path = new File(args[0]);

		//checkMissingLinks(path);
		
		errorXMLPath = new File(new File(new File(outputXmlFile).getParent()).getParent()).getParent() 
				+ File.separator + new SimpleDateFormat("ddMMyy").format(new Date()) 
				+ File.separator + "ERROR_" + new SimpleDateFormat("HHMMSS").format(new Date());
		
		//Create Error Dir
		new File(errorXMLPath).mkdirs();
		
		String logFile = errorXMLPath + File.separator + "LogFile.log";
		try {
			FileHandler fileHander;
			fileHander = new FileHandler(logFile);
			fileHander.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(fileHander);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("\n\n*** Starting Manifest Generation ****");
		LOGGER.info("*** Starting Manifest Generation ****");
		
		System.out.println("\n Error Files Path:"+errorXMLPath);		
		LOGGER.info("Error Files Path:"+errorXMLPath);
		
		readXmlManifestGen(path);
		
		checkMissingFileLinks(path);
		
		System.out.println("Manifest Generated at:"+outputXmlFile);
		LOGGER.info("Manifest Generated at:"+outputXmlFile);
		System.out.println("*** Manifest Generation Completed ****\n\n");
		LOGGER.info("*** Manifest Generation Completed ****");
//		checkStatus(new File("C:/Goks/mh/dyna/output/ihtml/html/0"));
	}
	
	public static void checkStatus(File folder) {
		HashSet<String> files = new HashSet<String>();
		int count=0;
		for(String fileName:folder.list()) {
			if(fileName.contains("-"))
				files.add(fileName.split("-")[0]);
			else
				files.add(fileName.split(".html")[0]);
			count++;
		}
		System.out.println(count + "-- total");
		LOGGER.info(count + "-- total");
		System.out.println(files.size() + "-- files processed");
		LOGGER.info(files.size() + "-- files processed");
		System.out.println(new Date());
		for(String str:files){
			//System.out.println(str);
		}
	}
		
	
	public static void readXmlManifestGen(File path){
		
		for(File file:path.listFiles()) {
			//Validate & Remove invalid files-- Using Java below
			//if(XMLCleaner.isXMLValid(file,errorXMLPath)) {}
				//Generate Manifest
			//Validate XML while reading file for generation of manifest
			if(!cleanDOCTYPE(file) || !generateManifest(file)) {
				String fileName = file.getName();
//				file.renameTo(new File(errorXMLPath + File.separator + fileName));
				try {
					Files.copy(file.toPath(), new File(errorXMLPath + File.separator + fileName).toPath());
					try {
						Files.delete(file.toPath());
					} catch (IOException e) {
						LOGGER.severe("File Delete failed for " + file);
						LOGGER.severe(e.toString());
						e.printStackTrace();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LOGGER.severe(e.toString());
				}
				//Replace the old file if available in the older folder
				
			}
		}
		
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;	
        Document doc=null;
        try {
			docBuilder = dbfac.newDocumentBuilder();
	        doc = docBuilder.newDocument();
	        Element root = doc.createElement("manifest");
	        root.setAttribute("title", "Dynamed");
	        root.setAttribute("dropDate", new SimpleDateFormat("dd-MMM-yy").format(new Date()));
	        doc.appendChild(root);
	        
	        
	        for(String str : diff){
				Element manifestFile = null;
				String title = str.split(";")[0];
				String file = str.split(";")[1];
				manifestFile=doc.createElement("manifest-file");
				manifestFile.setAttribute("id", file.split(".xml")[0]);
				manifestFile.setAttribute("src", "xml\\"+file);
				manifestFile.setAttribute("sub-title", cleanString(title));
				manifestFile.setAttribute("target", "");
				manifestFile.setAttribute("title", "Main-Index");
				manifestFile.setAttribute("type", "DiffDiagnosis");
				root.appendChild(manifestFile);
			}
	        
			for(String str : disease){
				Element manifestFile = null;
				String title = str.split(";")[0];
				String file = str.split(";")[1];
				manifestFile=doc.createElement("manifest-file");
				manifestFile.setAttribute("id", file.split(".xml")[0]);
				manifestFile.setAttribute("src", "xml\\"+file);
				manifestFile.setAttribute("sub-title", cleanString(title));
				manifestFile.setAttribute("target", "");
				manifestFile.setAttribute("title", "Main-Index");
				manifestFile.setAttribute("type", "Disease");
				root.appendChild(manifestFile);
			}
			
			for(String str : drug){
				Element manifestFile = null;
				String title = str.split(";")[0];
				String file = str.split(";")[1];
				manifestFile=doc.createElement("manifest-file");
				manifestFile.setAttribute("id", file.split(".xml")[0]);
				manifestFile.setAttribute("src", "xml\\"+file);
				manifestFile.setAttribute("sub-title", cleanString(title));
				manifestFile.setAttribute("target", "");
				manifestFile.setAttribute("title", "Main-Index");
				manifestFile.setAttribute("type", "Drug");
				root.appendChild(manifestFile);
			}
			
			for(String str : misc){
				Element manifestFile = null;
				String title = str.split(";")[0];
				String file = str.split(";")[1];
				manifestFile=doc.createElement("manifest-file");
				manifestFile.setAttribute("id", file.split(".xml")[0]);
				manifestFile.setAttribute("src", "xml\\"+file);
				manifestFile.setAttribute("sub-title", cleanString(title));
				manifestFile.setAttribute("target", "");
				manifestFile.setAttribute("title", "Main-Index");
				manifestFile.setAttribute("type", "Misc");
				root.appendChild(manifestFile);
			}
			
			for(String str : redir){
				Element manifestFile = null;
				String title = str.split(";")[0];
				String file = str.split(";")[1];
				manifestFile=doc.createElement("manifest-file");
				manifestFile.setAttribute("id", file.split(".xml")[0]);
				manifestFile.setAttribute("src", "xml\\"+file);
				manifestFile.setAttribute("sub-title", cleanString(title));
				manifestFile.setAttribute("target", "");
				manifestFile.setAttribute("title", "Main-Index");
				manifestFile.setAttribute("type", "Redirector");
				root.appendChild(manifestFile);
			}
			
			for(String str : syn){
				Element manifestFile = null;
				String title = str.split(";")[0];
				String file = str.split(";")[1];
				manifestFile=doc.createElement("manifest-file");
				manifestFile.setAttribute("id", file.split(".xml")[0]);
				manifestFile.setAttribute("src", "xml\\"+file);
				manifestFile.setAttribute("sub-title", cleanString(title));
				manifestFile.setAttribute("target", "");
				manifestFile.setAttribute("title", "Main-Index");
				manifestFile.setAttribute("type", "Synonym");
				root.appendChild(manifestFile);
			}
			
			readXML(doc);
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			LOGGER.severe(e.toString());
			System.exit(-1);
		}
		
	}
	
	
	public static String cleanString(String str) {
		if(str.contains("–")) {
			str = str.replace("–","-");
		}
		
		if(str.contains("’")) {
			str = str.replace("’","'");
		}
		
		String wierdSpace = " ";
		if(str.contains(wierdSpace)){
			str = str.replace(wierdSpace, " ");
		}
		
		str = str.trim();
		
		return str;
	}
	
	public static boolean generateManifest(File fXmlFile){
		
		FileInputStream stream = null;
		
		try {
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			stream = new FileInputStream(fXmlFile);
			Document doc = dBuilder.parse(stream);
			
			String text = doc.getElementsByTagName("article-title").item(0).getTextContent()+";" + fXmlFile.getName();
			String articleType = doc.getDocumentElement().getAttribute("article-type");
			
			// Check for title and article type
			if(text==null || text.trim().length()==0 || articleType==null || 
					articleType.trim().length()==0) {
				return false;
			}
			
			//1. Check for orphan text under sec
			NodeList secList = doc.getElementsByTagName("sec");
			
			for(int i=0;i<secList.getLength();i++){
				Node sec = secList.item(i);
				NodeList secChildren = sec.getChildNodes();
				for(int j=0;j<secChildren.getLength();j++){
					Node secChild = secChildren.item(j);
					if(secChild.getNodeType()==Node.TEXT_NODE){
						String secText = secChild.getTextContent().trim();
						if(secText!=null && secText.trim().length()!=0)	{
							System.out.println("Invalid Text found: "+ secText 
									+ " in " + fXmlFile.getName());
							LOGGER.severe("Invalid Text found: "+ secText 
									+ " in " + fXmlFile.getName());
							return false;
						}
						
					} /*else if(secChild.getNodeName()!=null && secChild.getNodeName().contains("xref") ){
						System.out.println("Element " + secChild.getNodeName() + 
								" not allowed under " + sec.getNodeName()
 								+ " in " + fXmlFile.getName());
						LOGGER.severe("Element " + secChild.getNodeName() + 
								" not allowed under " + sec.getNodeName()
 								+ " in " + fXmlFile.getName() + " in line \n" + sec  .getTextContent());
						return false;
					}*/
				}
			}
			
			//2. Check for orphan text under list
			NodeList listList = doc.getElementsByTagName("list");
			for(int i=0;i<listList.getLength();i++){
				Node list = listList.item(i);
				NodeList listChildren = list.getChildNodes();
				for(int j=0;j<listChildren.getLength();j++){
					Node listChild = listChildren.item(j);
					if(listChild.getNodeType()==Node.TEXT_NODE){
						String listText = listChild.getTextContent().trim();
						if(listText!=null && listText.trim().length()!=0)	{
							System.out.println("Invalid Text found: "+ listText 
									+ " in " + fXmlFile.getName());
							LOGGER.severe("Invalid Text found: "+ listText 
									+ " in " + fXmlFile.getName());
							return false;
						} else if(listChild.getNodeName()!=null && listChild.getNodeName().contains("xref") ){
							System.out.println("Element " + listChild.getNodeName() + 
									" not allowed under " + list.getNodeName()
	 								+ " in " + fXmlFile.getName());
							LOGGER.severe("Element " + listChild.getNodeName() + 
									" not allowed under " + list.getNodeName()
	 								+ " in " + fXmlFile.getName());
							return false;
						}
						
					}
				}
			}
			
			//3. Check for orphan text under listitem
			NodeList listItemList = doc.getElementsByTagName("list-item");
			for(int i=0;i<listItemList.getLength();i++){
				Node listItem = listItemList.item(i);
				NodeList listItemChildren = listItem.getChildNodes();
				for(int j=0;j<listItemChildren.getLength();j++){
					Node listItemChild = listItemChildren.item(j);
					if(listItemChild.getNodeType()==Node.TEXT_NODE){
						String listItemText = listItemChild.getTextContent().trim();
						if(listItemText!=null && listItemText.trim().length()!=0)	{
							System.out.println("Invalid Text found: "+ listItemText 
									+ " in " + fXmlFile.getName());
							LOGGER.severe("Invalid Text found: "+ listItemText 
									+ " in " + fXmlFile.getName());
							return false;
						} else if(listItemChild.getNodeName()!=null && listItemChild.getNodeName().contains("xref") ){
							System.out.println("Element " + listItemChild.getNodeName() + 
									" not allowed under " + listItem.getNodeName()
	 								+ " in " + fXmlFile.getName());
							LOGGER.severe("Element " + listItemChild.getNodeName() + 
									" not allowed under " + listItem.getNodeName()
	 								+ " in " + fXmlFile.getName());
							return false;
						}
					}
				}
			}
			
			if(articleType.contains("Synonym")){
				syn.add(text);
			} else if(articleType.contains("Drug")){
				drug.add(text);
			} else if(articleType.contains("Redirector")){
				redir.add(text);
			} else if(articleType.contains("Disease")){
				disease.add(text);
			} else if(articleType.contains("DiffDiagnosis")){
				diff.add(text);
			} else if(articleType.contains("Misc")){
				misc.add(text);
			} else {
				misc.add(text);
				System.out.println(articleType + "--- "+text +"-- Undefined so put under misc");
				LOGGER.warning(articleType + "--- "+text +"-- Undefined so put under misc");
			}

			return true;
		} catch(Exception e) {
			e.printStackTrace();
			
			try {
				stream.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				LOGGER.severe(e.toString());
			}
			
			LOGGER.severe(e.toString() + " in " + fXmlFile);
			System.out.println("ERROR in file: "+fXmlFile);
			e.printStackTrace();
			return false;
		}
		
	}
	
	
	public static void readXML(Document doc){
		try{
	           TransformerFactory transfac = TransformerFactory.newInstance();
	            Transformer trans = transfac.newTransformer();
	            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	            trans.setOutputProperty(OutputKeys.INDENT, "yes");
	            //create string from xml tree
	            StringWriter sw = new StringWriter();
	            StreamResult result = new StreamResult(sw);
	            DOMSource source = new DOMSource(doc);
	            trans.transform(source, result);
	            String xmlString = sw.toString();
	            //print xml
	            writeFile(xmlString);

		} catch(Exception e){
			e.printStackTrace();
			LOGGER.severe(e.toString());
			System.exit(-1);
		}
		
	}
	
	public static void checkMissingFileLinks(File path){
		
		HashMap<String, String> missingFileLinks = new HashMap<String, String>();
		FileInputStream stream = null;
		
		for(File fXmlFile:path.listFiles()) {
			//Each file
			String missingFiles = "";
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				stream = new FileInputStream(fXmlFile);
				Document doc = dBuilder.parse(stream);
				//Check for Missing Links
				NodeList extLinkList = doc.getElementsByTagName("ext-link");
				for(int i=0;i<extLinkList.getLength();i++){
					Node extLinkItem = extLinkList.item(i);
					if(extLinkItem!=null) {
						NamedNodeMap attributes = extLinkItem.getAttributes();
						if(attributes!=null) {
							Node attribute = attributes.getNamedItem("ext-link-type");
							if(attribute!=null) {
								String txt = attribute.getTextContent();
								if(txt!=null && txt.contains("ArticleLink")) {
									Node fileAtt = attributes.getNamedItem("xlink:href");
									if(fileAtt!=null){
										String fileNa = fileAtt.getTextContent();
										if(fileNa!=null){
											fileNa = fileNa+".xml";
											String refFilePath = fXmlFile.getParent() + File.separator + fileNa;
											if(!new File(refFilePath).exists()) {
												missingFiles = missingFiles + fileNa +";";
											}
											
										}
									}
								}
							}
						}
					}
					
				}
				if(missingFiles!=null && missingFiles.trim().length()!=0){
					missingFileLinks.put(fXmlFile.getName(), missingFiles);
				}
			} catch (Exception e) {
				e.printStackTrace();
				
				try {
					stream.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					LOGGER.severe(e.toString());
				}
				
				LOGGER.severe(e.toString());
			}
			
		}
		
		String missingFilesText="";
		boolean missingFiles = false;
		for (String key : missingFileLinks.keySet()) {
			missingFilesText = missingFilesText + key + " has the missing file links for " + missingFileLinks.get(key) + "\n";
			missingFiles = true;
		}
		
		if(missingFiles) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(errorXMLPath+File.separator+"MissingFiles.txt"));
				out.write(missingFilesText);
				out.close();
			
				} catch (Exception e) { 
				e.printStackTrace(); 
				LOGGER.severe(e.toString());
				System.exit(-1);
				}
		}
		
	}
	
	public static void writeFile(String file){
		//System.out.println(file);
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(outputXmlFile));
			out.write(file);
			out.close();
			} catch (Exception e) { 
				e.printStackTrace(); 
				LOGGER.severe(e.toString());
				System.exit(-1);
				}	
	}
	
	public static boolean cleanDOCTYPE(File file) {

		BufferedReader br = null;
		boolean writeFile = false;
		StringBuilder sb = new StringBuilder();
		try {
			br = new BufferedReader(new FileReader(file.getAbsolutePath()));
			
		    String line = br.readLine();
		    
		    while (line != null) {
		    	if(line.contains("<!DOCTYPE")) {
		    		writeFile = true;
		    		line = line.replaceAll("<!DOCTYPE((.|\n|\r)*?)\">", "");
		    	}
		    	if(line.contains("xlink:href=\"t")) {
		    		writeFile = true;
		    		line = line.replaceAll("xlink:href=\"t","xlink:href=\"T");
		    	}
		    	sb.append(line);
		    	sb.append("\n");
		    	line = br.readLine();
		        }
		    sb.toString();
		    } catch (Exception e) {
		    	LOGGER.severe(e.toString());
		    	return false;
		    } 
			finally {
		        try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LOGGER.severe(e.toString());
					return false;
				}
		    }
		
		if(writeFile) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(file));
				out.write(sb.toString());
				out.close();
			} 
			catch (Exception e) {
				e.printStackTrace(); 
				LOGGER.severe(e.toString());
				return false;
			}	
		}
		
		return true;
		
	}

}
