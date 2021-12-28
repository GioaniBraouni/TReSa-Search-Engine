package tresa.simulator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.spell.SpellChecker;
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


    public ScoreDoc[] search(String query) throws IOException,
            ParseException
    {
        TopScoreDocCollector docCollector =  TopScoreDocCollector.create(10000, 20000);
        ScoreDoc[] searchResults = null;
        Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
        analyzerMap.put(TReSaFields.PEOPLE, new StandardAnalyzer());
        analyzerMap.put(TReSaFields.TITLE, new StandardAnalyzer());
        analyzerMap.put(TReSaFields.PLACES, new StandardAnalyzer());
        PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(new EnglishAnalyzer(), analyzerMap);
//        analyzer = new StandardAnalyzer();

        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(new String[] { "places", "people","title","body" },
                wrapper);

        //Preprocessor prep = new Preprocessor(query);
        String prep = query.toString();
        System.out.println("Searching for '" + prep.toString() + "' using QueryParser");
        Query searchQuery = queryParser.parse(prep.toString());
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
