package Algoritmo;


import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Stemmer {

	private String value;
	private String indexS;
	
	public void setValue(String v)
	{
		value=v;
	}
	public String getIndexS()
	{
		return indexS;
	}
	
	public Stemmer(String v) throws IOException, ParserConfigurationException, SAXException{	
	
		setValue(v);
		URL url = new URL("http://192.168.1.4:8983/solr/thesaurus/analysis/field?analysis.fieldtype=text_it&analysis.fieldvalue="+URLEncoder.encode(value,"UTF-8"));
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(url.openStream());
	
		NodeList nList = doc.getElementsByTagName("arr");
	
		String terms="";
	 
	 for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				
				if(eElement.getAttribute("name").equals("org.apache.lucene.analysis.it.ItalianLightStemFilter"))
				{ 
					boolean fine=false;
					int n=0;
					while(!fine){
							try{
							if(eElement.getElementsByTagName("str").item(n).getTextContent()!=null)
							{
								terms+=eElement.getElementsByTagName("str").item(n).getTextContent()+" ";
								n=n+3;
							}	
							}
							catch(Exception exc)
							{
								fine= true;
								
							}

					}
					
					indexS=terms.substring(0,terms.length()-1);
				}	 									
			}
		}	 
	 

    
}
	

	/*	
	public static void main(String[] args) throws ParserConfigurationException, SAXException {
	    
	    long inizio= System.nanoTime();
	        try {
				analizzatore exp = new analizzatore("legge sugli alloggi");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    long fine= System.nanoTime();
	    System.out.println(fine-inizio);
	    }*/
	
	
	
}
