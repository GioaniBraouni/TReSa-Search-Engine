package tresa.simulator;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Testing {

/*
 Testing English Stemmer. Packet found in Analyzer/Common
 */



    public static void main(String[] args) throws IOException {

        EnglishStemmer stemmer = new EnglishStemmer();
        List<Character> chars = Arrays.asList('.', '/', ',', '!', '-');
        final CharArraySet stopWords = new CharArraySet(chars,true);
//        Set<Character> charSet = new HashSet<>(chars);
        StopAnalyzer stop = new StopAnalyzer(stopWords);
        StandardAnalyzer analyzer = new StandardAnalyzer(); // TODO Me auto kati prepei na paixtei pou den exw katalavei akoma
        String test = "testing";
        Tokenizer token = new StandardTokenizer();

        Pattern pattern = Pattern.compile("(?<=\\<title>)(.*?)(?=\\</title>)|(?<=\\<places>)(.*?)(?=\\</places>)|(?<=\\<people>)(.*?)(?=\\</people>)");
        Matcher matcher;
        //stemmer.setCurrent(test);
        //stemmer.stem();
        //String res = stemmer.getCurrent();
        //System.out.println(res);


        Scanner sc2 = null;
        try {
            sc2 = new Scanner(new File("Reuters/Article0.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (sc2.hasNextLine()) {
            Scanner s2 = new Scanner(sc2.nextLine());
            while (s2.hasNext()) {

                String s = s2.next();
                stemmer.setCurrent(s);
                stemmer.stem();
                String st = stemmer.getCurrent().toLowerCase(Locale.ROOT);
                System.out.println(st);
            }

        }

    }


}
