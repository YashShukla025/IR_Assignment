package ie.tcd.yshukla;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import java.nio.file.Paths;
import java.util.HashMap;
import java.nio.file.Files;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

import org.apache.lucene.document.Document;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.index.DirectoryReader;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;

import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.*;

public class Querysearcher {
	// the location of the search index
	private static String INDEX_DIRECTORY = "index";
	
	// Limit the number of search results we get
	private static int MAX_RESULTS = 100;

	public void searchQuery() throws IOException
	{
		// Open the folder that contains our search index
		Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
		
		// create objects to read and search across the index
		DirectoryReader ireader = DirectoryReader.open(directory);
		IndexSearcher isearcher = new IndexSearcher(ireader);

		// builder class for creating our query
		BooleanQuery.Builder query = new BooleanQuery.Builder();

		// Some words that we want to find and the field in which we expect
		// to find them
		Query term1 = new TermQuery(new Term("Document_Content", "transition"));

//		// construct our query using basic boolean operations.
		query.add(new BooleanClause(term1, BooleanClause.Occur.SHOULD));   // AND
//		query.add(new BooleanClause(term2, BooleanClause.Occur.MUST));     // OR
//		query.add(new BooleanClause(term3, BooleanClause.Occur.MUST_NOT)); // NOT

		// Get the set of results from the searcher
		ScoreDoc[] hits = isearcher.search(query.build(), MAX_RESULTS).scoreDocs;
		
		// Print the results
		System.out.println("Documents: " + hits.length);
		for (int i = 0; i < hits.length; i++)
		{
			Document hitDoc = isearcher.doc(hits[i].doc);
			System.out.println(i + ") " + hitDoc.get("Document_Title") + " " + hits[i].score);
		}

		// close everything we used
		ireader.close();
		directory.close();
	}
	
	public void searchQueryFromFile() throws Exception
	{
		String index = "index";
        String queryString = "";
        
        Analyzer analyzer = new EnglishAnalyzer();
        
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new MultiSimilarity(new Similarity[]{new ClassicSimilarity(),new LMDirichletSimilarity(),new BM25Similarity()}));
		
        String results_path = "results.txt";
        PrintWriter writer = new PrintWriter(results_path, "UTF-8");
        
		String queriesPath = "cran/cran.qry";
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(queriesPath), StandardCharsets.UTF_8);
        MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"Document_ID", "Document_Title", "Document_Authour", "Document_Biblo","Document_Content"}, analyzer);
        String currentLine = bufferedReader.readLine();

        System.out.println("Reading in queries and creating search results.");

        String id = "";
        int i=0;

        while (currentLine != null) {
            i++;
            if (currentLine.startsWith(".I")) {
                id = Integer.toString(i);
                currentLine = bufferedReader.readLine();
            }
            if (currentLine.startsWith(".W")) {
                currentLine = bufferedReader.readLine();
                while (currentLine != null && !currentLine.startsWith(".I")) {
                    queryString += currentLine + " ";
                    currentLine = bufferedReader.readLine();
                }
            }
            queryString = queryString.trim();
            Query query = parser.parse(QueryParser.escape(queryString));
            queryString = "";
            StringBuffer results = searchIndex(searcher, Integer.parseInt(id), query);
            writer.print(results.toString());
        }

        System.out.println("Results have been written to the 'results.txt' file.");
        writer.close();
        reader.close();
	}
	
	// Performs search and writes results to the writer
    public static StringBuffer searchIndex(IndexSearcher searcher, Integer queryNumber, Query query) throws IOException {
    	StringBuffer result = new StringBuffer();
        TopDocs results = searcher.search(query, 1400);
        ScoreDoc[] hits = results.scoreDocs;

        // To write the results for each hit in the format expected by the trec_eval tool.
        for (int i = 0; i < hits.length; i++) {
            Document doc = searcher.doc(hits[i].doc);
            result.append(queryNumber+" Q0 "+ doc.get("Document_ID") + " " + i + " " + hits[i].score + " STANDARD\n");
        }
        
        return result;
    }
}