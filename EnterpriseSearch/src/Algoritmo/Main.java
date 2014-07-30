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
    	
    	    /*
    	     * Setting dei parametri di connessione al core "corpus" per una query con
    	     * il clustering attivato.
    		*/
    		SolrServer server = new HttpSolrServer("http://192.168.1.4:8983/solr/corpus");
    	    ModifiableSolrParams params = new ModifiableSolrParams();
    	    params.set("qt", "/clustering");
    	    params.set("q", "*:*");


    	    /*
    	     * Effettuiamo la query sul server e recuperiamo tutti i cluster generati in output.
    		*/
    	    QueryResponse response = server.query(params);  
    	    ArrayList cluster = (ArrayList) response.getResponse().get("clusters"); 
    	    // inizzializziamo l'arrayList di cluster
    	    ArrayList<Cluster> clusters = new ArrayList<Cluster>();
    	    // prepariamo una stringa per costruire la query da eseguire sul thesaurus
    	    String queryThes="";
    	    // ciclo sui cluster
    	    for (int j=0; j<cluster.size();j++)
    	    {
    	    	SimpleOrderedMap<Object> map = (SimpleOrderedMap<Object>) cluster.get(j);
    	    	// recupero la label del cluster
    	    	String l = map.get("labels").toString();
    	    	// elimino il primo e l'ultimo carattere della label
    	    	l = l.substring(1,l.length()-1);
    	    	// effettuo lo stemming della label 
    	    	Stemmer stem = new Stemmer(l);
    	    	String stemLabel = stem.getIndexS();
    	    	// recupero gli id dei documenti presenti nel cluster
    	    	String docs = map.get("docs").toString().substring(1,map.get("docs").toString().length()-1);    	    	
    	    	// creo un arrayList con gli id dei documenti
    	    	String[] s = docs.split(",");
    	    	ArrayList<String> docList = new ArrayList<String>(Arrays.asList(s));
    	    	// creo un oggetto della classe Cluster settando i parametri "label" "stemLabel" "docs"
    	    	Cluster aux = new Cluster(l, stemLabel,docList);     	    	
    	    	// aggiungo alla lista il cluster appena creato in maniera ordinata
    	    	clusters.add(getSortedIndex(stemLabel,clusters), aux);
    	    	
    	    	// Costruzione della stringa per la query sul thesaurus. Si elimina il cluster "other topics"
    	    	// da tale lista poichè non deve essere ricercato nel thesaurus.
    	    	if(!stemLabel.equals("other topics"))
    	    	{	
    	    			if(j==0)
		    	    		queryThes+="descrittore:"+stemLabel;
		    	    	else
		    	    		queryThes+=" OR descrittore:"+stemLabel;
    	    	}
    	    }
    	    
    	    
    	    
    	    /*
    	     * Setting dei parametri di connessione al core "thesaurus" per una query con
    	     * il componente di Facet attivato sul field "hierarchy". Richiediamo inoltre che
    	     * i risultati siano ordinati per "descrittore".
    		*/
    	    SolrServer server2 = new HttpSolrServer("http://192.168.1.4:8983/solr/thesaurus");
    	    SolrQuery query = new SolrQuery();
    	    
    	    query.setQuery(queryThes);
    	    query.setFacet(true);
    	    query.setFacetMinCount(1);
    	    query.addFacetField("hierarchy");
    	    query.addSort("descrittore", ORDER.asc);
    	    
    	    /*
    	     * Effettuiamo la query sul server e recuperiamo tutti i documenti ottenuti in output.
    		*/
    	    QueryResponse response2 = server2.query(query);
    	    SolrDocumentList results = response2.getResults();
    	    // inizializziamo l'arrayList di descrittori
    	    ArrayList<Desc> descrittori = new ArrayList<Desc>();
    	    // ciclo sui documenti
    	    for(int i=0; i<results.size(); i++)
    	    {
    	    	// per ogni documento creo un oggetto della classe "Desc"
    	    	Desc aux = new Desc();
    	    	// recupero il descrittore e setto tale valore nell'oggetto creato
    	    	aux.setName(results.get(i).getFieldValue("descrittore").toString());
    	    	// recupero le gerarchie e setto tale valore nell'oggetto creato
    	    	aux.setGerarchia((ArrayList)(results.get(i).getFieldValues("hierarchy")));
    	    	// aggiunto l'oggetto alla lista dei descrittori
    	    	descrittori.add(aux);
    	    } 
    	    
    	    /*
    	     * Richiamo della funzione "merge" che effettuerà il confronto tra la lista
    	     * dei cluster e la lista dei descrittori generando in output un'unica lista
    	     * contenente tutti i termini ognuno dei quali avrà associato i propri documenti.
    		*/
    	    ArrayList<Desc> out = merge (clusters,descrittori);
    	    
    	    
    	    
    	    /*
    	     * Recupero dei facet restituiti dalla query
    	     */
    	    int size = response2.getFacetField("hierarchy").getValueCount();
    	    
    	    for(int i=0;i<size;i++)
    	    {
    	    	response2.getFacetField("hierarchy").getValues().get(i).getName();
    	    } 
    	    
    	    
    	    
    	    
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
	  // finchè non scandisco tutti i descrittori eseguo:
	  while(indexD < descrittori.size())
	  {
		  /* 
		   * se il confronto va a buon fine, cioè se la label del descrittore 
		   * è uguale a quella del cluster.
		   */
		  if(clusters.get(indexC).getStemLabel().equals(descrittori.get(indexD).getName()) )
		  {
			  // prendo i documenti associati al cluster e li setto al descrittore.
			  descrittori.get(indexD).setDocs(clusters.get(indexC).getDocs());
			  // recupero inoltre la label non stemmata del cluster
			  descrittori.get(indexD).setName(clusters.get(indexC).getlabel());
			  // aggiungo all'output il descrittore
			  output.add(descrittori.get(indexD));
			  // avanzo entrambi gli indici delle due liste
			  indexC++;
			  indexD++;
		  }
		  /* 
		   * se il confronto non va a buon fine, cioè se la label del descrittore 
		   * è diversa da quella del cluster, allora tale cluster non è stato trovato
		   * nel thesaurus e quindi posso aggiungerlo direttamente all'output
		   */
		  else
		  {
			  
			  // creo un nuovo oggetto descrittore e gli setto i documenti, la label e la gerarchia.
			  Desc aux = new Desc();
			  aux.setDocs(clusters.get(indexC).getDocs());
			  aux.setName(clusters.get(indexC).getLabel());
			  aux.setGerarchia(new ArrayList<String>(Arrays.asList(clusters.get(indexC).getLabel())));
			  // aggiungo l'oggetto all'output
			  output.add(aux);
			  // incremento l'indice della lista di cluster
			  indexC++;
		  }
		  
	  }
	  
	  // terminata la scansione dei descrittori rimangono dei cluster da inserire nell'output
	  while(indexC < clusters.size())
	  {
		  Desc aux = new Desc();
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