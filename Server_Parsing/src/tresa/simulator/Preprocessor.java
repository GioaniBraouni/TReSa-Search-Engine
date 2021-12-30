package tresa.simulator;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.DateTools;
import org.tartarus.snowball.ext.PorterStemmer;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

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
        if (checkIfEmpty(currentLine)){
            return null;
        }

        tokens = new StringTokenizer(currentLine);

        int len = tokens.countTokens();


        for (int i = 0; i < len; i++) {
            stemmer.setCurrent(tokens.nextToken());

            String string = stemmer.getCurrent().replaceAll("-"," ");
            string = string.replace("("," ");
            string = string.replace(")"," ");
            string = string.replaceAll("<title>", "");
            string = string.replaceAll("</title>", "");
            string = string.replaceAll("<places>", "");
            string = string.replaceAll("</places>", "");
            string = string.replaceAll("<people>", "");
            string = string.replaceAll("</people>", "");
            string = string.replaceAll("<body>", "");
            string = string.replaceAll("</body>", "");
            string = string.replaceAll("and","");
            string = string.replaceAll("or","");
            string = string.replaceAll("not","");
            string = string.replaceAll("\\+","");
            string = string.replaceAll("-","");
            string = string.replaceAll("\"","");

           string = string.replaceAll("the","");


            //System.out.println(string);
            if (string.matches(".*\\d.*")){
                if (string.matches(".*\\d.*") && (string.endsWith(",")
                        || string.endsWith(".")) && !string.contains("/")){
                    String n = string.replaceAll(",","");
                    n = string.replaceAll(".","");
                    stringBuilder.append(n + " ");
                }else if (string.length()-string.replaceAll("/","").length() == 2){
                    if (string.contains(".")){
                        string = string.replace(".","");
                    }
                    if (string.contains(",")){
                        string = string.replaceAll(",","");
                    }

                    String ai = string.replaceAll("/","-");

                    stringBuilder.append(string + " ");
                }else if(string.length()-string.replaceAll("/","").length() == 1){
                    if (string.contains(".")){
                        string = string.replace(".","");
                    }
                    if (string.contains(",")){
                        string = string.replaceAll(",","");
                    }
                    String doted = string.replaceAll("/","-");
                    stringBuilder.append(doted + " ");
                }else if (string.contains(".") && string.contains(",")){
                    String s = string.replace(",","");
                    stringBuilder.append(s + " ");
                }
                else {
                    stringBuilder.append(string + " ");
                }
                //String stemmed = stemmer.getCurrent().replaceAll("/","");

            }
            else {
//                String punp = stemmer.getCurrent();\
                String punp = string;

                punp = punp.replaceAll("and","");
                punp = punp.replaceAll("or","");
                punp = punp.replaceAll("not","");
                punp = punp.replaceAll("reuter","");
                punp = punp.replaceAll("[^A-Za-z]", "");
                stemmer.setCurrent(punp);
                stemmer.stem();
                stringBuilder.append(stemmer.getCurrent() + " ");
            }

        }
        return stringBuilder.toString();
    }

    public String toString() {
        return currentLinePrep() + "";
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private boolean checkIfEmpty(String string){
        String altered = string;
        altered = altered.replaceAll("<title>", "");
        altered = altered.replaceAll("</title>", "");
        altered = altered.replaceAll("<places>", "");
        altered = altered.replaceAll("</places>", "");
        altered = altered.replaceAll("<people>", "");
        altered = altered.replaceAll("</people>", "");
        altered = altered.replaceAll("<body>", "");
        altered = altered.replaceAll("</body>", "");

        return altered.trim().length() < 1;
    }
}