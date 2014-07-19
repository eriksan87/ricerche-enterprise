package Algoritmo;

import org.xml.sax.SAXException;
import org.apache.log4j.Level;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.SimpleOrderedMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;





import javax.xml.parsers.ParserConfigurationException;

public class Main {

	
    public Main() throws SolrServerException, IOException, ParserConfigurationException, SAXException  {
    	  SolrServer server = new HttpSolrServer("http://192.168.1.4:8983/solr/corpus");
    	  ModifiableSolrParams params = new ModifiableSolrParams();
    	    params.set("qt", "/clustering");
    	    params.set("q", "*:*");
//    	    params.set("carrot.title", "myTitle");
//    	    params.set("clustering", "true");

    	    QueryResponse response = server.query(params);
    	   
    	   
    	    ArrayList cluster= (ArrayList) response.getResponse().get("clusters");   	    
    	   
    	    ArrayList<Cluster> clusters =new ArrayList<Cluster>();
    	    
    	    String queryThes="";
    	    
    	    for (int j=0; j<cluster.size();j++)
    	    {
    	    	SimpleOrderedMap<Object> map = (SimpleOrderedMap<Object>) cluster.get(j);
    	    	String l=map.get("labels").toString();
    	    	l=l.substring(1,l.length()-1);
    	    	Stemmer a=new Stemmer(l);
    	    	String stemLabel=a.getIndexS();
    	    	String docs=map.get("docs").toString().substring(1,map.get("docs").toString().length()-1);    	    	
    	    	//lista di documenti
    	    	String[] s=docs.split(",");
    	    	ArrayList<String> docList= new ArrayList<String>(Arrays.asList(s));
    	    	
    	    	Cluster aux=new Cluster(l, stemLabel,docList);     	    	
    	    	clusters.add(getSortedIndex(stemLabel,clusters), aux);
    	    //costruisco la stringa per la query
    	    	
    	    	if(!stemLabel.equals("other topics"))
    	    	{	
    	    			if(j==0)
		    	    		queryThes+="descrittore:"+stemLabel;
		    	    	else
		    	    		queryThes+=" OR descrittore:"+stemLabel;
    	    	}
    	    }
    	        	    
    	    SolrServer server2 = new HttpSolrServer("http://192.168.1.4:8983/solr/thesaurus");
    	    SolrQuery query = new SolrQuery();
    	    
    	    query.setQuery(queryThes);
    	    query.setFacet(true);
    	    query.setFacetMinCount(1);
    	    query.addFacetField("hierarchy");
    	    query.addSort("descrittore",ORDER.asc);
    	    
    	    QueryResponse response2 = server2.query(query);
    	    
    	    SolrDocumentList results=response2.getResults();
    	    System.out.println(query.toString());
    	    
    	    ArrayList<Desc> descrittori=new ArrayList<Desc>();
    	    
    	    for(int i=0;i<results.size();i++)
    	    {
    	    	Desc aux=new Desc();
    	    	aux.setName(results.get(i).getFieldValue("descrittore").toString());
    	    	aux.setGerarchia((ArrayList)(results.get(i).getFieldValues("hierarchy")));
    	    	descrittori.add(aux);
    	    } 
    	    
    	    ArrayList<Desc> out=merge (clusters,descrittori);
    	    
    	    for(int i=0; i<out.size();i++)
    	    {
    	    	System.out.println(out.get(i).getName());
    	    	System.out.println(out.get(i).getGerarchia().get(0));
    	    	System.out.println("\t");
    	    	for(int j=0; j<out.get(i).getDocs().size();j++)
    	    	{
    	    		System.out.println(out.get(i).getDocs().get(j));
    	    	}
    	    	
    	    }
    }
     
  private static int getSortedIndex (String name, ArrayList<Cluster> list) {
        for (int i=0; i < list.size(); i++) {
            if (name.compareTo(list.get(i).getStemLabel()) < 0) {
                return i;
            }
        }       
        // name should be inserted at end
        return list.size();
    }
  
  private static ArrayList<Desc> merge (ArrayList<Cluster> clusters,ArrayList<Desc> descrittori) {
  
	  ArrayList<Desc> output=new ArrayList<Desc>();
	  
	  int indexC=0;
	  int indexD=0;
	  
	  while(indexD<descrittori.size())
	  {
		  if(clusters.get(indexC).getStemLabel().equals(descrittori.get(indexD).getName()) )
		  {
			  descrittori.get(indexD).setDocs(clusters.get(indexC).getDocs());
			  descrittori.get(indexD).setName(clusters.get(indexC).getlabel());
			  output.add(descrittori.get(indexD));
			  indexC++;
			  indexD++;
		  }
		  else{
			  
			  Desc aux= new Desc();
			  aux.setDocs(clusters.get(indexC).getDocs());
			  aux.setName(clusters.get(indexC).getLabel());
			  aux.setGerarchia(new ArrayList<String>(Arrays.asList(clusters.get(indexC).getLabel())));
			  output.add(aux);
			  indexC++;
		  }
		  
	  }
	  
	  while(indexC<clusters.size())
	  {
		  Desc aux= new Desc();
		  aux.setDocs(clusters.get(indexC).getDocs());
		  aux.setName(clusters.get(indexC).getLabel());
		  aux.setGerarchia(new ArrayList<String>(Arrays.asList(clusters.get(indexC).getLabel())));
		  output.add(aux);
		  indexC++;
	  }
	  
	  return output;
	  
  }
  
  

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
    	org.apache.log4j.Logger.getRootLogger().setLevel(Level.OFF);
    	
    	long inizio=System.nanoTime();
    	try {
			Main c=new Main();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println(System.nanoTime()-inizio);
    
    }
}