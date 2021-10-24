package ie.tcd.yshukla;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class CreateIndex {

    /** Index all text files under a directory. */
    public void index() {
        String indexEnglish = "Index/English_analyzer";
        String indexStandard = "Index/standard_analyzer";
        String indexSimple = "Index/simple_analyzer";
        String indexEnglish2 = "Index/English_analyzer_bm25";        
        String indexStandard2 = "Index/standard_analyzer_bm25";
        String indexSimple2 = "Index/simple_analyzer_bm25";
        
        
        String cran_path = "cran/cran.all.1400";

        final Path cranPath = Paths.get(cran_path);

        if (!Files.isReadable(cranPath)) {
            System.out.println("Document directory '" + cranPath.toAbsolutePath() + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        Date start = new Date();
        try {
        	
            Directory indexEnglishDirectory = FSDirectory.open(Paths.get(indexEnglish));
            Directory indexStandardDirectory = FSDirectory.open(Paths.get(indexStandard));
            Directory indexSimpleDirectory = FSDirectory.open(Paths.get(indexSimple));
            Directory indexEnglishDirectory2 = FSDirectory.open(Paths.get(indexEnglish2));
            Directory indexStandardDirectory2 = FSDirectory.open(Paths.get(indexStandard2));
            Directory indexSimpleDirectory2 = FSDirectory.open(Paths.get(indexSimple2));
            
            

           
            Analyzer sAnalyzer = new StandardAnalyzer();
            Analyzer eAnalyser = new EnglishAnalyzer();
            Analyzer simpleAnalyzer = new SimpleAnalyzer();


			IndexWriterConfig iWCEnglish = new IndexWriterConfig(eAnalyser);
            IndexWriterConfig iWCStandard = new IndexWriterConfig(sAnalyzer);
            IndexWriterConfig iWCSimple = new IndexWriterConfig(simpleAnalyzer);
            IndexWriterConfig iWCEnglish2 = new IndexWriterConfig(eAnalyser);
            IndexWriterConfig iWCStandard2 = new IndexWriterConfig(sAnalyzer);
            IndexWriterConfig iWCSimple2 = new IndexWriterConfig(simpleAnalyzer);


            iWCEnglish.setSimilarity(new ClassicSimilarity());
            iWCStandard.setSimilarity(new ClassicSimilarity());
            iWCSimple.setSimilarity(new ClassicSimilarity());
            
            iWCEnglish2.setSimilarity(new BM25Similarity());
            iWCStandard2.setSimilarity(new BM25Similarity());
            iWCSimple2.setSimilarity(new BM25Similarity());
            //Trying a multi similarity model
            
//            iWCEnglish.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new ClassicSimilarity()}));
//            iWCStandard.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new ClassicSimilarity()}));
//            iWCSimple.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new ClassicSimilarity()}));

            //Trying another multi similarity model
            //iwc.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new LMDirichletSimilarity()}));

            //Trying another multi similarity model
            //iwc.setSimilarity(new MultiSimilarity(new Similarity[]{new ClassicSimilarity(),new LMDirichletSimilarity()}));

            iWCEnglish.setOpenMode(OpenMode.CREATE);
            iWCStandard.setOpenMode(OpenMode.CREATE);
            iWCSimple.setOpenMode(OpenMode.CREATE);
            iWCEnglish2.setOpenMode(OpenMode.CREATE);
            iWCStandard2.setOpenMode(OpenMode.CREATE);
            iWCSimple2.setOpenMode(OpenMode.CREATE);

            IndexWriter indexWriter1 = new IndexWriter(indexEnglishDirectory, iWCEnglish);
            IndexWriter indexWriter2 = new IndexWriter(indexStandardDirectory, iWCStandard);
            IndexWriter indexWriter3 = new IndexWriter(indexSimpleDirectory, iWCSimple);
            IndexWriter indexWriter11 = new IndexWriter(indexEnglishDirectory2, iWCEnglish2);
            IndexWriter indexWriter22 = new IndexWriter(indexStandardDirectory2, iWCStandard2);
            IndexWriter indexWriter33 = new IndexWriter(indexSimpleDirectory2, iWCSimple2);
            
            indexDoc(indexWriter1, cranPath);
            indexDoc(indexWriter2, cranPath);
            indexDoc(indexWriter3, cranPath);
            indexDoc(indexWriter11, cranPath);
            indexDoc(indexWriter22, cranPath);
            indexDoc(indexWriter33, cranPath);

            //Using writer.forceMerge to maximise search performance.
            indexWriter1.forceMerge(1);
            indexWriter2.forceMerge(1);
            indexWriter3.forceMerge(1);
            indexWriter11.forceMerge(1);
            indexWriter22.forceMerge(1);
            indexWriter33.forceMerge(1);

            indexWriter1.close();
            indexWriter2.close();
            indexWriter3.close();
            indexWriter11.close();
            indexWriter22.close();
            indexWriter33.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        } catch (IOException e) {
            System.out.println(" Exception: " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }

    /** Indexes the 'cran.all.1400' file */
    static void indexDoc(IndexWriter writer, Path file) throws IOException {
        try (InputStream is = Files.newInputStream(file)) {

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            Boolean first = true;
            System.out.println("Indexing documents....");

            String line = bufferedReader.readLine();
            String contnt = "";
            while(line != null){
                Document doc = new Document();
                if(line.startsWith(".I")){
                    /*
                     * I think the ID of the document does not make sense to be analysed,
                     * hence it is just directly stored without any analysis.
                     */
                    doc.add(new StringField("ID", line.substring(3), Field.Store.YES));
                    line = bufferedReader.readLine();
                }
                if (line.startsWith(".T")){
                	line = bufferedReader.readLine();
                    while(!line.startsWith(".A")){
                        contnt += line + " ";
                        line = bufferedReader.readLine();
                    }
                    doc.add(new TextField("Title", contnt, Field.Store.YES));
                    contnt = "";
                }
                if (line.startsWith(".A")){
                	line = bufferedReader.readLine();
                    while(!line.startsWith(".B")){
                        contnt += line + " ";
                        line = bufferedReader.readLine();
                    }
                    doc.add(new TextField("Author", contnt, Field.Store.YES));
                    contnt = "";
                }
                if (line.startsWith(".B")){
                	line = bufferedReader.readLine();
                    while(!line.startsWith(".W")){
                        contnt += line + " ";
                        line = bufferedReader.readLine();
                    }
                    /*
                     * After a bit of analysis, I found that for this dataset, analysing
                     * and storing bibliography details proved to be slightly inefficient.
                     */
                    doc.add(new StringField("Biblography", contnt, Field.Store.YES));
                    contnt = "";
                }
                if (line.startsWith(".W")){
                	line = bufferedReader.readLine();
                    while(line != null && !line.startsWith(".I")){
                        contnt += line + " ";
                        line = bufferedReader.readLine();
                    }
                    
                    doc.add(new TextField("Words", contnt, Field.Store.YES));
                    contnt = "";
                }
                writer.addDocument(doc);
            }
        }
    }
}