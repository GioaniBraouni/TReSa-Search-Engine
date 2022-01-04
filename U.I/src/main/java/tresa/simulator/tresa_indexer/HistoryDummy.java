package tresa.simulator.tresa_indexer;

public class HistoryDummy {

    private String query;
    private String results;
    private String date;

    public HistoryDummy(String query, String results,String date) {
        this.query = query;
        this.results = results;
        this.date = date;
    }


    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getResults() {
        return results;
    }

    public void setResults(String results) {
        this.results = results;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return query + "\t" + results + "\t" + date;
    }
}