package ie.tcd.yshukla;

public class Main {
	public static void main (String[] args) throws Exception {
        Indexing indexing = new Indexing();
        Querysearcher search = new Querysearcher();

        indexing.index();
        search.srch();
    }
}
