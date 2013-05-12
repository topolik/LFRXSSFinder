package cz.topolik.xssfinder.scan;

import cz.topolik.xssfinder.FileLoader;
import cz.topolik.xssfinder.PossibleXSSLine;
import java.util.Set;

/**
 *
 * @author Tomas Polesovsky
 */
public interface XSSScanner {
    Set<PossibleXSSLine> scan(FileLoader loader);
}
