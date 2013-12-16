package cz.topolik.xssfinder.scan.advanced.parser;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;

import java.util.List;

/**
 * @author Tomas Polesovsky
 */
public class FileContentCEP implements WhitelistExpressionParser {
    private String fileName;
    private String content;
    private int lineNum = -1;

    public FileContentCEP(String fileName, String content, int lineNum) {
        this.fileName = fileName;
        this.content = content;
        this.lineNum = lineNum;
    }

    @Override
    public boolean isSafe(String expression, int lineNum, String line, FileContent f, FileLoader loader) {
        if((this.lineNum == -1 || this.lineNum == lineNum) &&
                f.getFile().toString().endsWith(fileName) &&
                expression.equalsIgnoreCase(content)){

            return true;
        }

        return false;
    }
}
