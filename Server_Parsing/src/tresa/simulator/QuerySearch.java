package tresa.simulator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

public class QuerySearch {
    IndexSearcher indexSearcher;
    Directory indexDirectory;
    IndexReader indexReader;
    Analyzer analyzer;


    public QuerySearch() throws IOException
    {

        Path indexPath = Paths.get("Index");
        indexDirectory = FSDirectory.open(indexPath);
        indexReader = DirectoryReader.open(indexDirectory);
        indexSearcher = new IndexSearcher(indexReader);
        indexSearcher.setSimilarity(new ClassicSimilarity());
    }

    //public BooleanQueryParser(String )

    public ScoreDoc[] search(String query) throws IOException,
            ParseException
    {
        TopScoreDocCollector docCollector =  TopScoreDocCollector.create(10000, 20000);
        ScoreDoc[] searchResults = null;

        analyzer = new StandardAnalyzer();
        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(new String[] { "places", "people","title","body" },
                analyzer);

        Preprocessor prep = new Preprocessor(query);
        System.out.println("Searching for '" + prep.toString() + "' using QueryParser");
        Query searchQuery = queryParser.parse(prep.toString());
        TReSaMain.query = searchQuery;
        indexSearcher.search(searchQuery, docCollector);
        searchResults = docCollector.topDocs().scoreDocs;

        return searchResults;
    }


    public ScoreDoc[] testSearch(String input,String input1) throws IOException,
            ParseException
    //TODO more work to be done. Seems to work
    {
        TopScoreDocCollector docCollector =  TopScoreDocCollector.create(10000, 20000);
        ScoreDoc[] searchResults = null;


        analyzer = new StandardAnalyzer();
        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(new String[] { "places", "people","title","body" },
                analyzer);
        queryParser.setDefaultOperator(QueryParser.OR_OPERATOR);
        TermQuery query1 = new TermQuery(new Term(TReSaFields.BODY,input));
        TermQuery query2 = new TermQuery(new Term(TReSaFields.BODY,input1));
        Query searx = queryParser.parse("body:" + input + " NOT body:" +  input1);

        // prep = new Preprocessor(query);
        //System.out.println("Searching for '" + prep.toString() + "' using QueryParser");
        //Query searchQuery = queryParser.parse(prep.toString());

        indexSearcher.search(searx, docCollector);
        searchResults = docCollector.topDocs().scoreDocs;

        return searchResults;
    }

    public void Test(String input, String input2) throws IOException, ParseException {

        IndexReader r =DirectoryReader.open(FSDirectory.open(Paths.get("Index")));
        IndexSearcher searcher = new IndexSearcher(indexReader);

        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(new String[] { "places", "people","title","body" },
                analyzer);
        queryParser.setDefaultOperator(QueryParser.OR_OPERATOR);
        TermQuery query1 = new TermQuery(new Term(TReSaFields.BODY,input));
        TermQuery query2 = new TermQuery(new Term(TReSaFields.BODY,input2));
        Query searx = queryParser.parse(query1 + "NOT" + query2);
        BooleanQuery matchingQuery = new BooleanQuery.Builder()
                .add(query1,BooleanClause.Occur.SHOULD)
                .add(query2,BooleanClause.Occur.MUST_NOT)
                .build();
        //TotalHits hits = this.search(matchingQuery.toString());

    }

    public IndexSearcher getIndexSearcher()
    {
        return this.indexSearcher;
    }
    public void closeReader() throws IOException
    {
        this.indexReader.close();
    }
}
