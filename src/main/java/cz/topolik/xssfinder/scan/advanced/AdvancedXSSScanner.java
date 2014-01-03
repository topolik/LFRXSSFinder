package cz.topolik.xssfinder.scan.advanced;

import cz.topolik.xssfinder.scan.Logger;
import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;
import cz.topolik.xssfinder.PossibleXSSLine;
import cz.topolik.xssfinder.scan.SimpleXSSScanner;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cz.topolik.xssfinder.scan.advanced.Constants.*;

/**
 *
 * @author Tomas Polesovsky
 */
public class AdvancedXSSScanner extends SimpleXSSScanner {
    static final String OUT_WRITE = "out.write(";
    public static final String ROW_ADDTEXT = "row.addText(";
    static final Pattern ROW_URL_PATTERN =  Pattern.compile("^(.*), (row[A-Z]+)$");
    private XSSEnvironment environment;


    @Override
    public Set<PossibleXSSLine> scan(FileLoader loader) {
        Logger.log("Initializing environment");
        environment = new XSSEnvironment();
        environment.init(loader);
        Logger.log("Running advanced scan");
        Set<PossibleXSSLine> result = super.scan(loader);
        Logger.log("Finished");
        return result;
    }

    public void destroy(){
        environment.destroy();
    }

    public static String[] parseSearchContainerRowExpression(int lineNum, String line, FileContent f){
        String argument = line.trim();
        int i = lineNum + 1;
        while(!argument.endsWith(";")){
            argument += f.getContent().get(i++).trim();
        }

        argument = argument.substring(ROW_ADDTEXT.length(), argument.length() - 2);
        Matcher m = ROW_URL_PATTERN.matcher(argument);
        if (m.matches()) {
            return new String[] {m.group(1), m.group(2)};
        }
        return new String[]{argument};
    }

    @Override
    protected String[] isLineSuspected(int lineNum, String line, FileContent f, FileLoader loader) {
        String trimmed = line.trim();

        // we have taglibs already processed
		// TODO: we have it processed but only to find vulnerable params, we don't save all threats
        if (environment.getTaglibProcessor().isTagLibJSP(f)) {
            return null;
        }

        // just string constants (Jasper convention)
        if(trimmed.startsWith(OUT_WRITE)){
            return null;
        }

        if (trimmed.startsWith(OUT_PRINT)) {
            String argument = trimmed.substring(OUT_PRINT.length(), trimmed.length() - 2);
            List<String> result = environment.getXSSLogicProcessorHelperUtilThingie().isCallArgumentSuspected(argument, lineNum, trimmed, f, loader);
            return result == XSSLogicProcessorHelperUtilThingie.RESULT_SAFE ? null : result.toArray(new String[0]);
        }

        // vulnerable search container
        if (trimmed.startsWith(ROW_ADDTEXT)) {
            String[] argument = parseSearchContainerRowExpression(lineNum, trimmed, f);
            List<String> result = environment.getXSSLogicProcessorHelperUtilThingie().isCallArgumentSuspected(argument[0], lineNum, trimmed, f, loader);
            if(argument.length > 1) {
                List<String> result1 = environment.getXSSLogicProcessorHelperUtilThingie().isCallArgumentSuspected(argument[1], lineNum, trimmed, f, loader);
                if(result == XSSLogicProcessorHelperUtilThingie.RESULT_SAFE){
                    result = result1;
                }
                else if(result1 != XSSLogicProcessorHelperUtilThingie.RESULT_SAFE){
                    result.addAll(result1);
                }
            }

            return result == XSSLogicProcessorHelperUtilThingie.RESULT_SAFE ? null : result.toArray(new String[0]);
        }

        /*
        TODO: Search container Row Checker (and co.) + ResultRow attrs
         */

        // so we know there is no direct XSS
        // but there can be vulnerable taglib call

        String taglibResult[] = environment.getTaglibProcessor().isLineVulnerableTaglib(lineNum, trimmed, f, loader);
        if(taglibResult == null){
            // it's safe
            return null;
        }
        String argument = taglibResult[0].trim();
        List<String> callArgumentResult = environment.getXSSLogicProcessorHelperUtilThingie().isCallArgumentSuspected(argument, lineNum, line, f, loader);
        if(callArgumentResult == XSSLogicProcessorHelperUtilThingie.RESULT_SAFE){
            // it's safe
            return null;
        }
        String[] result = new String[taglibResult.length + callArgumentResult.size() - 1];
        System.arraycopy(callArgumentResult.toArray(new String[0]), 0, result, 0, callArgumentResult.size());
        System.arraycopy(taglibResult, 1, result, callArgumentResult.size(), taglibResult.length - 1);
        return result;
    }

}
