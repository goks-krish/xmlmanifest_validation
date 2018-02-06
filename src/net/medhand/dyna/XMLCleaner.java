package net.medhand.dyna;


import java.io.File;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class XMLCleaner {
	
	static String xsdOutputPath="DynamedXSD.xsd";
	
	public static boolean isXMLValid(File file, String errorPath){
		//validate the xml file. if errors found move it out
		if(validateAgainstXSD(file.getAbsolutePath())) {
			return true;
		} else {
			//MOVE error files
			
//			Path destinationPath = Paths.get(errorPath + File.separator + file.getName());
//			Path sourcePath  = Paths.get(file.getAbsolutePath());
//			try {
//			    Files.move(sourcePath, destinationPath,
//			            StandardCopyOption.REPLACE_EXISTING);
//			} catch (Exception e) {
//			    //moving file failed.
//			    e.printStackTrace();
//			}

			file.renameTo(new File(errorPath + File.separator + file.getName()));

			return false;
		}
	}
	
	public static void main(String[] arg){
		//TODO: To test within class
	    if(validateAgainstXSD("C:/Goks/mh/DynamedManifestGenerator/xml/T113645.xml"))
	    	System.out.println("VALID");
		
//		String file1="C:\\Goks\\mh\\DynamedManifestGenerator\\xml\\T908389.xml";
//		String file2="C:\\Goks\\mh\\DynamedManifestGenerator\\ERROR_12-05-2016_23-44-06\\T908389.xml";
//		new File(file1).renameTo(new File(file2));
		
	}
	
	public static boolean validateAgainstXSD(String outputFilePath) {
        
		boolean status=false;
		
		SchemaFactory schemaFactory = null;
		Schema schema = null;
		Validator validator = null;
		StreamSource streamSource = null;
		File xml = null;
		InputStream sIn = null;
	    try {
	    	
	        schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	        schema = schemaFactory.newSchema(new StreamSource(xsdOutputPath));
	        validator = schema.newValidator();
	        xml = new File(outputFilePath);
	        sIn = xml.toURL().openStream();
	        streamSource = new StreamSource(sIn);
	        
	        validator.validate(streamSource);
	        status = true;
	        
	    } catch(SAXParseException spex) {
	    	System.out.println("\t Output XML Validation Exception \n \t File Name : " + spex.getSystemId()
	    			
	    			+ " \n \t Line Number " +  spex.getLineNumber() 
	    			+ "\n \t Column number: "+spex.getColumnNumber()
	    			+  " \n \t Message: "+spex.getMessage()
	    			+ "\n\t SAXParseException : " + spex.fillInStackTrace());

	    } catch (SAXException se) {
	    	System.out.println("SAXException : " + se.fillInStackTrace());
		} catch (Exception e) {
			System.out.println("Exception : " + e.fillInStackTrace());
		} finally {
			streamSource = null;
			validator.reset();
			validator=null;
			xml = null;
			try {
				sIn.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
     
	    return status;
	}	
}
