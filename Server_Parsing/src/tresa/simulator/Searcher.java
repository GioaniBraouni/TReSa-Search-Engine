package tresa.simulator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.tartarus.snowball.ext.PorterStemmer;

public class Searcher {
    IndexSearcher indexSearcher;
    Directory indexDirectory;
    IndexReader indexReader;
    Analyzer analyzer;


    public Searcher() throws IOException
    {
        Path indexPath = Paths.get("Index");
        indexDirectory = FSDirectory.open(indexPath);
        indexReader = DirectoryReader.open(indexDirectory);
        indexSearcher = new IndexSearcher(indexReader);
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