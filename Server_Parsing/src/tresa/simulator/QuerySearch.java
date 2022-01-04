package tresa.simulator;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.index.*;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.document.*;

import javax.print.Doc;

public class QuerySearch {
    IndexSearcher indexSearcher;
    Directory indexDirectory;
    IndexReader indexReader;
    Analyzer analyzer;
    TReSaIndex index;



    public QuerySearch() throws IOException
    {

        Path indexPath = Paths.get("Index");
        indexDirectory = FSDirectory.open(indexPath);
        indexReader = DirectoryReader.open(indexDirectory);
        indexSearcher = new IndexSearcher(indexReader);
        indexSearcher.setSimilarity(new ClassicSimilarity());
        index = new TReSaIndex();
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


    protected ScoreDoc[] searchFile(String input,int top_k) throws IOException, NoSuchAlgorithmException, ParseException {

        List<String> stopWords = List.of("places","people","title","body","reuter");

        CharArraySet stopSet = new CharArraySet(stopWords,true);

        CharArraySet enStopSet = EnglishAnalyzer.ENGLISH_STOP_WORDS_SET;

        stopSet.addAll(enStopSet);
        File file1 = new File(input);
        Document doc = index.getDocument(file1);
        TopScoreDocCollector docCollector =  TopScoreDocCollector.create(10000, 20000);
        ScoreDoc[] searchResults = null;
        MoreLikeThis moreLikeThis = new MoreLikeThis(indexReader);
        moreLikeThis.setFieldNames(new String[]{TReSaFields.TITLE,TReSaFields.PEOPLE,TReSaFields.PLACES,TReSaFields.BODY});

        Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
        analyzerMap.put(TReSaFields.PEOPLE, new StandardAnalyzer(stopSet));
        analyzerMap.put(TReSaFields.TITLE, new StandardAnalyzer(stopSet));
        analyzerMap.put(TReSaFields.PLACES, new StandardAnalyzer(stopSet));
        PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(new EnglishAnalyzer(stopSet), analyzerMap);
        moreLikeThis.setAnalyzer(wrapper);

        BooleanQuery.setMaxClauseCount(10000);

        StringBuilder sb = new StringBuilder();
        sb.append(doc.get("title")).append(" ").append(doc.get("places")).append(" ").append(doc.get("people")).append(" ").append(doc.get("body"));
        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(new String[] { "places", "people","title","body" },
                wrapper);

        String st = sb.toString();

        Preprocessor preprocessor = new Preprocessor(st);
        //System.out.println(preprocessor);
        String el = preprocessor.toString();

        Query myquery= queryParser.parse(el);

        TopDocs similarDocs = indexSearcher.search(myquery, top_k);
        searchResults=similarDocs.scoreDocs;
        int i=0;
        for (ScoreDoc sd: searchResults){
            Document document = indexSearcher.doc(sd.doc);
            System.out.println(document.get("fileName") + " " + searchResults[i].score);
            i++;
        }

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
