package cz.topolik.xssfinder;

import cz.topolik.xssfinder.scan.XSSScanner;
import cz.topolik.xssfinder.scan.advanced.AdvancedXSSScanner;
import cz.topolik.xssfinder.scan.threaded.ThreadedXSSScanner;
import cz.topolik.xssfinder.v2.World;

import java.io.File;
import java.util.*;

public class App {

    public static void main(String[] args) throws IllegalAccessException {
        if (args.length < 1) {
            printSyntax();
            args = new String[]{"/opt/liferay.git/portal", "8"};
        }
        Set<PossibleXSSLine> xsss = null;

        boolean version1 = true;
        if (version1) {
            FileLoader loader = new FileLoader(new File(args[0]));

            XSSScanner scanner = new ThreadedXSSScanner(Integer.parseInt(args[1]));

            xsss = new TreeSet<PossibleXSSLine>(scanner.scan(loader));

            scanner.destroy();
        }
        else {
            File[] continents = new File[]{
                    new File(args[0], "jsp-precompile"),
                    new File(args[0], "portal-impl/src"),
                    new File(args[0], "portal-service/src"),
                    new File(args[0], "util-bridges"),
                    new File(args[0], "util-java/src"),
                    new File(args[0], "util-taglib/src")
            };

            World.see().explore(continents);
            xsss = World.see().rotate(Integer.parseInt(args[1]));
            World.see().jDay();
        }

        List<Occurence> occurences = new ArrayList<Occurence>();
        int i = 0;
        for (PossibleXSSLine line : xsss) {
            String relevantLines = Arrays.asList(line.getStackTrace()).toString();
//            System.out.println("Problem " + ++i + ":");
            System.out.print(line.getSourceFile().getFile().getAbsolutePath() + " ");
            System.out.print(line.getLineNum() + ": ");
            System.out.println(line.getLineContent().trim());
            System.out.println("Relevant lines:");
            System.out.println(relevantLines);
            if (relevantLines.contains("ParamUtil.getString(req") ||
                    relevantLines.contains("BeanParamUtil.getString(") ||
                    relevantLines.contains("PrefsParamUtil.getString(")) {

                System.out.println("@@@ POSSIBLE XSS @@@");
            }
            System.out.println("Format for whitelist:");
            System.out.print("file=");
            int pos = line.getSourceFile().getFile().toString().indexOf("src/org/apache/jsp/") + 19;
            System.out.print(line.getSourceFile().getFile().toString().substring(pos));
            System.out.print(",");
            System.out.print(line.getLineNum());
            System.out.print(",");
            String vuln = line.getLineContent().trim();
            if (vuln.startsWith(AdvancedXSSScanner.ROW_ADDTEXT)) {
                String arg[] = AdvancedXSSScanner.parseSearchContainerRowExpression((int) line.getLineNum(), vuln, line.getSourceFile());
                System.out.print(arg[0]);
                if (arg.length > 1) {
                    System.out.print(" || <- OR -> || ");
                    System.out.print(arg[1]);
                }
                System.out.println();
            } else {
                System.out.println(vuln.substring(10, vuln.length() - 2).trim());
            }
            System.out.println("---------------------------------------------");

            boolean found = false;
            String lineContent = line.getLineContent().trim();
            for (Occurence o1 : occurences) {
                if (o1.getLine().equals(lineContent)) {
                    found = true;
                    o1.addOccurence();
                }
            }
            if (!found) {
                occurences.add(new Occurence(lineContent));
            }
        }

        int possibleXSS = xsss.size();
        System.out.println("Total possible XSS: " + possibleXSS);

        int counter = 0;
        Collections.sort(occurences);
        for (Iterator<Occurence> it2 = occurences.iterator(); it2.hasNext() && counter < 10; counter++) {
            Occurence o = it2.next();
            System.out.println("Occurence: [" + o.getOccured() + ", " + o.getLine() + "]");
        }

        System.out.println("==============================================================");
        for (PossibleXSSLine line : xsss) {
            System.out.print("file=");
            int pos = line.getSourceFile().getFile().toString().indexOf("src/org/apache/jsp/") + 19;
            System.out.print(line.getSourceFile().getFile().toString().substring(pos));
            System.out.print(",");
            System.out.print(line.getLineNum());
            System.out.print(",");
            String vuln = line.getLineContent().trim();
            if (vuln.startsWith(AdvancedXSSScanner.ROW_ADDTEXT)) {
                String arg[] = AdvancedXSSScanner.parseSearchContainerRowExpression((int) line.getLineNum(), vuln, line.getSourceFile());
                System.out.print(arg[0]);
                if (arg.length > 1) {
                    System.out.print(" || <- OR -> || ");
                    System.out.print(arg[1]);
                }
                System.out.println();
            } else {
                System.out.println(vuln.substring(10, vuln.length() - 2).trim());
            }
        }


    }

    private static void printSyntax() {
        System.out.println("Usage: \n\t\tdirectory with java-precompiled jsps to perform XSS scan\n\t\tthread pool size");
    }
}

class Occurence implements Comparable<Occurence> {
    int occured = 1;
    String line;

    public Occurence(String line) {
        this.line = line;
    }

    public void addOccurence() {
        occured++;
    }

    public int getOccured() {
        return occured;
    }

    public String getLine() {
        return line;
    }

    public int compareTo(Occurence o) {
        if (o == null) {
            return 1;
        }
        return this.occured < o.occured ? 1 : (this.occured == o.occured ? 0 : -1);
    }

}
