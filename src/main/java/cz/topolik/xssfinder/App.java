package cz.topolik.xssfinder;

import cz.topolik.xssfinder.scan.XSSScanner;
import cz.topolik.xssfinder.scan.advanced.AdvancedXSSScanner;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class App {

    public static void main(String[] args) throws IllegalAccessException {
        if(args.length < 1){
            printSyntax();
            args = new String[]{"/opt/liferay.git/portal"};
        }

        FileLoader loader = new FileLoader(new File(args[0]));

        XSSScanner scanner = new AdvancedXSSScanner();

        Set<PossibleXSSLine> xsss = new TreeSet<PossibleXSSLine>(scanner.scan(loader));

        List<Occurence> occurences = new ArrayList<Occurence>();
        int i = 0;
        for(PossibleXSSLine line : xsss){
            String relevantLines = Arrays.asList(line.getStackTrace()).toString();
            System.out.println("Problem " + ++i + ":");
            System.out.print(line.getSourceFile().getFile().getAbsolutePath() + " ");
            System.out.print(line.getLineNum()+": ");
            System.out.println(line.getLineContent().trim());
            System.out.println("Relevant lines:");
            System.out.println(relevantLines);
            if(relevantLines.contains("ParamUtil.getString(req")){
                System.out.println("@@@ POSSIBLE XSS @@@");
            }
            if (relevantLines.contains("BeanParamUtil.getString(")) {
                System.out.println("@@@ POSSIBLE XSS @@@");
            }
            System.out.println("---------------------------------------------");

            boolean found = false;
            String lineContent = line.getLineContent().trim();
            for(Occurence o1 : occurences){
                if(o1.getLine().equals(lineContent)){
                    found = true;
                    o1.addOccurence();
                }
            }
            if(!found){
                occurences.add(new Occurence(lineContent));
            }
        }

        int possibleXSS = xsss.size();
        System.out.println("Total possible XSS: " + possibleXSS);

        int counter = 0;
        Collections.sort(occurences);
        for(Iterator<Occurence> it2 = occurences.iterator(); it2.hasNext() && counter < 10; counter++){
            Occurence o = it2.next();
            System.out.println("Occurence: ["+o.getOccured() + ", " + o.getLine()+"]");
        }

    }

    private static void printSyntax(){
        System.out.println("First parameter missing. Please specify directory with java-precompiled jsps to perform XSS scan!");
    }
}

class Occurence implements Comparable<Occurence>{
    int occured = 1;
    String line;

    public Occurence(String line) {
        this.line = line;
    }

    public void addOccurence(){
        occured++;
    }

    public int getOccured() {
        return occured;
    }

    public String getLine() {
        return line;
    }

    public int compareTo(Occurence o) {
        if(o == null){
            return 1;
        }
        return this.occured<o.occured ? 1 : (this.occured==o.occured ? 0 : -1);
    }

}
