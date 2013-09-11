package cz.topolik.xssfinder.scan.advanced.parser;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;

import java.util.List;

/**
 * @author Tomas Polesovsky
 */
public class FileContentCEP implements ComplexExpressionParser {
    private String fileName;
    private String content;

    public FileContentCEP(String fileName, String content) {
        this.fileName = fileName;
        this.content = content;
    }

    @Override
    public List<String> execute(String expression, int lineNum, String line, FileContent f, FileLoader loader) {

        if(f.getFile().toString().endsWith(fileName) && expression.equalsIgnoreCase(content)){
            return RESULT_SAFE;
        }

        return RESULT_DONT_KNOW;
    }
}
