mvn clean package

java -jar target/Lucene-Example-0.0.1-SNAPSHOT.jar

./trec_eval/trec_eval -m runid -m map -m P.5 -m gm_map cran/cranqrell results_standard.txt
./trec_eval/trec_eval -m runid -m map -m P.5 -m gm_map cran/cranqrell results_english.txt