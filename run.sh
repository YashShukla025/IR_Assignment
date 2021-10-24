mvn clean package

java -jar target/Lucene-Example-0.0.1-SNAPSHOT.jar

./trec_eval/trec_eval -m runid -m map -m P.5 -m gm_map cran/cranqrell results_standard.txt
./trec_eval/trec_eval -m runid -m map -m P.5 -m gm_map cran/cranqrell results_english.txt
./trec_eval/trec_eval -m runid -m map -m P.5 -m gm_map cran/cranqrell results_simple.txt
./trec_eval/trec_eval -m runid -m map -m P.5 -m gm_map cran/cranqrell results_standard_BM25.txt
./trec_eval/trec_eval -m runid -m map -m P.5 -m gm_map cran/cranqrell results_english_BM25.txt
./trec_eval/trec_eval -m runid -m map -m P.5 -m gm_map cran/cranqrell results_simple_BM25.txt

