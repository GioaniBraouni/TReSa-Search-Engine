package tresa.simulator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public ScoreDoc[] search(String query,String userInput) throws IOException,
            ParseException
    {
        String maxHits;
        if(userInput.equals("null"))
            maxHits= "100";
        else
            maxHits=userInput;
        TopScoreDocCollector docCollector =  TopScoreDocCollector.create(Integer.parseInt(maxHits), 20000);
        ScoreDoc[] searchResults = null;

        CharArraySet enStopSet = EnglishAnalyzer.ENGLISH_STOP_WORDS_SET;

        analyzer = new StandardAnalyzer(enStopSet);

        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(new String[] { "places", "people","title","body" },
                analyzer);

        Preprocessor prep = new Preprocessor(query);
        Query searchQuery = queryParser.parse(prep.toString());
        System.out.println(searchQuery);
        TReSaMain.query = searchQuery;
        indexSearcher.search(searchQuery, docCollector);
        searchResults = docCollector.topDocs().scoreDocs;

        return searchResults;
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
