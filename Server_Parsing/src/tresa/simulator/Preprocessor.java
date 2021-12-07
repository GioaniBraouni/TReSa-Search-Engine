package tresa.simulator;

import org.tartarus.snowball.ext.PorterStemmer;

import java.util.Locale;
import java.util.StringTokenizer;

public class Preprocessor {
    private PorterStemmer stemmer = new PorterStemmer();
    private StringBuilder stringBuilder = new StringBuilder();
    private StringTokenizer tokens = null;
    private String currentLine;

    public Preprocessor(String currentLine) {
        this.currentLine = currentLine;
    }

    private String currentLinePrep() {
        String result = this.currentLine.toLowerCase(Locale.ROOT);

        tokens = new StringTokenizer(currentLine);

        int len = tokens.countTokens();

        for (int i = 0; i < len; i++) {
            stemmer.setCurrent(tokens.nextToken());
            stemmer.stem();
            stringBuilder.append(stemmer.getCurrent() + " ");
        }
        return stringBuilder.toString();
    }

    public String toString() {
        return currentLinePrep() + "";
    }
}