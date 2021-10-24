package ie.tcd.yshukla;

public class Main {
	public static void main (String[] args) throws Exception {
        CreateIndex indexing = new CreateIndex();
        SearchQuery search = new SearchQuery();

        indexing.index();
        search.srch();
    }
}
