package ie.tcd.yshukla;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.PrintWriter;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.FSDirectory;

/** Simple command-line based search demo. */
public class Querysearcher {

    public void srch() throws Exception {

    	String indexEnglish = "Index/English_analyzer";
        String indexStandard = "Index/standard_analyzer";
        String queryString = "";

        IndexReader reader1 = DirectoryReader.open(FSDirectory.open(Paths.get(indexEnglish)));
        IndexReader reader2 = DirectoryReader.open(FSDirectory.open(Paths.get(indexStandard)));
        
        IndexSearcher search_english = new IndexSearcher(reader1);
        IndexSearcher search_stndrd = new IndexSearcher(reader2);

        //Analyzer analyzer = new SimpleAnalyzer();
        //Analyzer analyzer = new WhitespaceAnalyzer();
        Analyzer sAnalyzer = new StandardAnalyzer();
        Analyzer eAnalyser = new EnglishAnalyzer();

        String path_english = "results_english.txt";
        String path_stndrd = "results_standard.txt";
        
        PrintWriter pwenglish = new PrintWriter(path_english, "UTF-8");
        PrintWriter pwstndrd = new PrintWriter(path_stndrd, "UTF-8");

        //BM25 Similarity
        //searcher.setSimilarity(new BM25Similarity());

        //Classic Similarity
        //searcher.setSimilarity(new ClassicSimilarity());

        //LMDirichletSimilarity
        //searcher.setSimilarity(new LMDirichletSimilarity());

        //Trying a multi similarity model
        search_english.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new ClassicSimilarity()}));
        search_stndrd.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new ClassicSimilarity()}));

        //Trying another multi similarity model
        //searcher.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new LMDirichletSimilarity()}));

        //Trying another multi similarity model
        //searcher.setSimilarity(new MultiSimilarity(new Similarity[]{new ClassicSimilarity(),new LMDirichletSimilarity()}));

        String queriesPath = "cran/cran.qry";
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(queriesPath), StandardCharsets.UTF_8);
        MultiFieldQueryParser englishParser = new MultiFieldQueryParser(new String[]{"title", "words"}, eAnalyser);
        MultiFieldQueryParser standardParser = new MultiFieldQueryParser(new String[]{"title", "words"}, sAnalyzer);

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
            Query query1 = englishParser.parse(QueryParser.escape(queryString));
            Query query2 = standardParser.parse(QueryParser.escape(queryString));
            queryString = "";
            performSearch(search_english, pwenglish, Integer.parseInt(id), query1);
            performSearch(search_stndrd, pwstndrd, Integer.parseInt(id), query2);
        }

        System.out.println("Results have been written to the 'results_english.txt' and 'results_standard.txt' file.");
        pwenglish.close();
        pwstndrd.close();
        reader1.close();
        reader2.close();
    }


    // Performs search and writes results to the writer
    public static void performSearch(IndexSearcher searcher, PrintWriter writer, Integer queryNumber, Query query) throws IOException {
        TopDocs results = searcher.search(query, 999);
        ScoreDoc[] hits = results.scoreDocs;

        // To write the results for each hit in the format expected by the trec_eval tool.
        for (int i = 0; i < hits.length; i++) {
            Document doc = searcher.doc(hits[i].doc);
            writer.println(queryNumber + " Q0 " + doc.get("ID") + " " + i + " " + hits[i].score + " ANALYSIS");
        }
    }
}