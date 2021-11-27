package tresa.simulator;

import org.tartarus.snowball.ext.EnglishStemmer;

public class Testing {

/*
 Testing English Stemmer. Packet found in Analyzer/Common
 */

    public static void main(String[] args) {
        EnglishStemmer stemmer = new EnglishStemmer();
        String test = "testing";
        stemmer.setCurrent(test);
        stemmer.stem();
        String res = stemmer.getCurrent();
        System.out.println(res);
    }
}
