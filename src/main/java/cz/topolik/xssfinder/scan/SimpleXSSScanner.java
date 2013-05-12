/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.topolik.xssfinder.scan;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;
import cz.topolik.xssfinder.PossibleXSSLine;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Tomas Polesovsky
 */
public class SimpleXSSScanner implements XSSScanner {
    static final String OUT_PRINT = "out.print(";
    static final String STRING_DECLARATION = "String ";
    static final String DECLARATION_POSTFIX = " = ";
    static final List<String> safeXSSExpressions = Arrays.asList(new String[]{
                "request.getAttribute(",
                "(String)request.getAttribute(",
                "(java.lang.String)request.getAttribute(",
                "PortalUtil.generateRandomKey(",
                "SessionMessages.get(",
                "LanguageUtil.getLanguageId(",
                "PortalUtil.getStaticResourceURL(",
                "PortletURLUtil.getRefreshURL(",
                "HtmlUtil.escape(",
                "HtmlUtil.escapeJS(",
                "PortalUtil.getPortalURL(",
                "BrowserSnifferUtil.is"
            });

    public Set<PossibleXSSLine> scan(FileLoader loader) {
        loader.load(FileLoader.DIR_JSPPRECOMPILED);

        Logger.log("Starting scanning ...");
        Set<PossibleXSSLine> result = new HashSet<PossibleXSSLine>();
        int pos = 0;
        Set<FileContent> files = loader.getFiles(FileLoader.DIR_JSPPRECOMPILED);
        for(FileContent f : files){
            pos ++;
            if(pos % 10 == 0){
                Logger.log("Scanned " + pos + " of " + files.size() + " files");
            }
            result.addAll(scan(f, loader));
        }
        Logger.log("Scanning finished.");
        return result;
    }

    protected Set<PossibleXSSLine> scan(FileContent f, FileLoader loader) {
        Set<PossibleXSSLine> result = new HashSet<PossibleXSSLine>();
        List<String> lines = f.getContent();
        for (int lineNum = 0; lineNum < lines.size(); lineNum++) {
            String[] suspectedLineStacktrace = isLineSuspected(lineNum, lines.get(lineNum), f, loader);
            if (suspectedLineStacktrace != null) {
                result.add(new PossibleXSSLine(f, lineNum + 1, lines.get(lineNum), suspectedLineStacktrace));
            }
        }
        return result;
    }

    protected String[] isLineSuspected(int lineNum, String line, FileContent f, FileLoader loader) {
        String trimmed = line.trim();
        if (!trimmed.startsWith(OUT_PRINT)) {
            return null;
        }

        String functionArgument = trimmed.substring(OUT_PRINT.length(), trimmed.length() - 2).trim();

        for (String safeExpression : safeXSSExpressions) {
            if (functionArgument.startsWith(safeExpression)) {
                return null;
            }
        }

        // we don't take function calls that doesn't contain request
        if (functionArgument.contains("(")) {
            if (functionArgument.contains("request")) {
                // cannot semantically understand manipulation with request, so we leave it as suspected :0
                return new String[0];
            }
            return null;
        }

        // now we have only variable or constant
        String variableDefinition = STRING_DECLARATION + functionArgument + DECLARATION_POSTFIX;
        String declarationLine = null;
        for(int i = lineNum; i >= 0; i--){
            String fileLine = f.getContent().get(i);
            if (fileLine.contains(variableDefinition)) {
                declarationLine = fileLine;
            }
        }
        if(declarationLine == null){
            for(int i = lineNum; i < f.getContent().size(); i++){
                String fileLine = f.getContent().get(i);
                if (fileLine.contains(variableDefinition)) {
                    declarationLine = fileLine;
                }
            }
        }
        if (declarationLine == null || !declarationLine.contains("request")) {
            return null;
        }

        for (String safeExpression : safeXSSExpressions) {
            if (declarationLine.contains(safeExpression)) {
                return null;
            }
        }

        return new String[]{declarationLine};
    }

}
