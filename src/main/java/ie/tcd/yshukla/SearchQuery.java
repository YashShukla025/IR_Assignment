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
public class SearchQuery {

    public void srch() throws Exception {

        String indexEnglish = "Index/English_analyzer";
        String indexStandard = "Index/standard_analyzer";
        String indexSimple = "Index/simple_analyzer";
        String indexEnglish2 = "Index/English_analyzer_bm25";        
        String indexStandard2 = "Index/standard_analyzer_bm25";
        String indexSimple2 = "Index/simple_analyzer_bm25";
        String queryString = "";
        
        //Reader for Classic Similarity
        IndexReader reader1 = DirectoryReader.open(FSDirectory.open(Paths.get(indexEnglish)));
        IndexReader reader2 = DirectoryReader.open(FSDirectory.open(Paths.get(indexStandard)));
        IndexReader reader3 = DirectoryReader.open(FSDirectory.open(Paths.get(indexSimple)));
        //Reader for BM25 Similarity
        IndexReader reader11 = DirectoryReader.open(FSDirectory.open(Paths.get(indexEnglish2)));
        IndexReader reader22 = DirectoryReader.open(FSDirectory.open(Paths.get(indexStandard2)));
        IndexReader reader33 = DirectoryReader.open(FSDirectory.open(Paths.get(indexSimple2)));
        
        IndexSearcher search_english = new IndexSearcher(reader1);
        IndexSearcher search_stndrd = new IndexSearcher(reader2);
        IndexSearcher search_simple = new IndexSearcher(reader2);
        IndexSearcher search_english2 = new IndexSearcher(reader11);
        IndexSearcher search_stndrd2 = new IndexSearcher(reader22);
        IndexSearcher search_simple2 = new IndexSearcher(reader33);

        //Analyzer analyzer = new WhitespaceAnalyzer();
        Analyzer sAnalyzer = new StandardAnalyzer();
        Analyzer eAnalyser = new EnglishAnalyzer();
        Analyzer simpleAnalyzer = new SimpleAnalyzer();

        String path_english = "results_english.txt";
        String path_stndrd = "results_standard.txt";
        String path_simple = "results_simple.txt";
        String path_english2 = "results_english_BM25.txt";
        String path_stndrd2 = "results_standard_BM25.txt";
        String path_simple2 = "results_simple_BM25.txt";
        
        PrintWriter pwenglish = new PrintWriter(path_english, "UTF-8");
        PrintWriter pwstndrd = new PrintWriter(path_stndrd, "UTF-8");
        PrintWriter pwsimple = new PrintWriter(path_simple, "UTF-8");
        PrintWriter pwenglish2 = new PrintWriter(path_english2, "UTF-8");
        PrintWriter pwstndrd2 = new PrintWriter(path_stndrd2, "UTF-8");
        PrintWriter pwsimple2 = new PrintWriter(path_simple2, "UTF-8");

        //BM25 Similarity
        //searcher.setSimilarity(new BM25Similarity());

        //Classic Similarity
        //searcher.setSimilarity(new ClassicSimilarity());

        //LMDirichletSimilarity
        //searcher.setSimilarity(new LMDirichletSimilarity());

        //Trying a multi similarity model
        search_english.setSimilarity(new ClassicSimilarity());
        search_stndrd.setSimilarity(new ClassicSimilarity());
        search_simple.setSimilarity(new ClassicSimilarity());
        search_english2.setSimilarity(new BM25Similarity());
        search_stndrd2.setSimilarity(new BM25Similarity());
        search_simple2.setSimilarity(new BM25Similarity());

//        search_english.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new ClassicSimilarity()}));
//        search_stndrd.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new ClassicSimilarity()}));
//        search_simple.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new ClassicSimilarity()}));

        //Trying another multi similarity model
        //searcher.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new LMDirichletSimilarity()}));

        //Trying another multi similarity model
        //searcher.setSimilarity(new MultiSimilarity(new Similarity[]{new ClassicSimilarity(),new LMDirichletSimilarity()}));

        String queriesPath = "cran/cran.qry";
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(queriesPath), StandardCharsets.UTF_8);
        MultiFieldQueryParser englishParser = new MultiFieldQueryParser(new String[]{"Title", "Words"}, eAnalyser);
        MultiFieldQueryParser standardParser = new MultiFieldQueryParser(new String[]{"Title", "Words"}, sAnalyzer);
        MultiFieldQueryParser simpleParser = new MultiFieldQueryParser(new String[]{"Title", "Words"}, simpleAnalyzer);
        MultiFieldQueryParser englishParser2 = new MultiFieldQueryParser(new String[]{"Title", "Words"}, eAnalyser);
        MultiFieldQueryParser standardParser2 = new MultiFieldQueryParser(new String[]{"Title", "Words"}, sAnalyzer);
        MultiFieldQueryParser simpleParser2 = new MultiFieldQueryParser(new String[]{"Title", "Words"}, simpleAnalyzer);

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
            Query query3 = simpleParser.parse(QueryParser.escape(queryString));
            Query query11 = englishParser2.parse(QueryParser.escape(queryString));
            Query query22 = standardParser2.parse(QueryParser.escape(queryString));
            Query query33 = simpleParser2.parse(QueryParser.escape(queryString));
            queryString = "";
            performSearch(search_english, pwenglish, Integer.parseInt(id), query1);
            performSearch(search_stndrd, pwstndrd, Integer.parseInt(id), query2);
            performSearch(search_simple, pwsimple, Integer.parseInt(id), query3);
            performSearch(search_english2, pwenglish2, Integer.parseInt(id), query11);
            performSearch(search_stndrd2, pwstndrd2, Integer.parseInt(id), query22);
            performSearch(search_simple2, pwsimple2, Integer.parseInt(id), query33);
        }

        System.out.println("Results have been written to the 'results_english.txt' and 'results_standard.txt' file.");
        pwenglish.close();
        pwstndrd.close();
        pwsimple.close();
        pwenglish2.close();
        pwstndrd2.close();
        pwsimple2.close();
        reader1.close();
        reader2.close();
        reader3.close();
        reader11.close();
        reader22.close();
        reader33.close();
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