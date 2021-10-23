package ie.tcd.yshukla;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;
import java.nio.file.Files;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.search.similarities.TFIDFSimilarity;

import org.apache.commons.lang3.StringUtils;

 
public class Indexing
{
	
	// Directory where the search index will be saved
	private static String INDEX_DIRECTORY = "index";
	

	public static void main(String[] args) throws IOException
	{
		// Make sure we were given something to index
		if (args.length <= 0)
		{
            System.out.println("Expected crandata as input");
            System.exit(1);            
        }

		// Analyzer that is used to process TextField
		//Analyzer analyzer = new StandardAnalyzer();
		Analyzer englishAnalyzer = new EnglishAnalyzer();
		
		
		// ArrayList of documents in the corpus
		ArrayList<Document> documents = new ArrayList<Document>();

		// Open the directory that contains the search index
		Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

		// Set up an index writer to add process and save documents to the index
		IndexWriterConfig config = new IndexWriterConfig(englishAnalyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		
		config.setSimilarity(new MultiSimilarity(new Similarity[]{new ClassicSimilarity(),new LMDirichletSimilarity(),new BM25Similarity()}));

		IndexWriter iwriter = new IndexWriter(directory, config);
		
		String queriesPath = "cran/cran.";
		BufferedReader reader = new BufferedReader(new FileReader(args[0]));
		String contentLine = reader.readLine();
		
		StringBuffer dataset = new StringBuffer();
		
		while (contentLine != null) {
			dataset.append(contentLine+"\n");
		    contentLine = reader.readLine();
		}
	    reader.close();
	    
	    HashMap<String,String> map = new HashMap<String,String>();
	    for(int i=1;i<1401;i++) {
	    	System.out.println(String.format("Indexing the %d document", i));
	    	String result = StringUtils.substringBetween(dataset.toString(), ".I "+String.valueOf(i), ".I "+String.valueOf(i+1));
		    String title = StringUtils.substringBetween(result, ".T", ".A");
		    String authour = StringUtils.substringBetween(result, ".A", ".B");
		    String biblo = StringUtils.substringBetween(result, ".B", ".W");
		    String content = result.substring(result.indexOf(".W\n")+3);
		    
		    //Create a new document
		    Document doc = new Document();
		    doc.add(new StringField("Document_ID",String.valueOf(i),Field.Store.YES));
			doc.add(new TextField("Document_Title", title, Field.Store.YES));
			doc.add(new TextField("Document_Authour", authour, Field.Store.YES));
			doc.add(new TextField("Document_Biblo", biblo, Field.Store.YES));
			doc.add(new TextField("Document_Content", content, Field.Store.YES));
			
			documents.add(doc);
	    }
	   
		// Write all the documents in the linked list to the search index
		iwriter.addDocuments(documents);
		
		//Using writer.forceMerge to maximise search performance.
		iwriter.forceMerge(1);

		// Commit everything and close
		iwriter.close();
		directory.close();
		
//		try{
//			Querysearcher querySearcher = new Querysearcher();
//			//querySearcher.searchQuery();
//			querySearcher.searchQueryFromFile();
//		}catch(Exception e) {
//			e.printStackTrace();
//		}
	}
}