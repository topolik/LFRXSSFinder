package cz.topolik.xssfinder.scan.threaded;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;
import cz.topolik.xssfinder.PossibleXSSLine;
import cz.topolik.xssfinder.scan.Logger;
import cz.topolik.xssfinder.scan.advanced.AdvancedXSSScanner;

import java.sql.Time;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Tomas Polesovsky
 */
public class ThreadedXSSScanner extends AdvancedXSSScanner {
    private static final Set<PossibleXSSLine> EMPTY_RESPONSE = new HashSet<PossibleXSSLine>();
    private ExecutorService pool;
    private Vector<Set<PossibleXSSLine>> scanResults = new Vector<Set<PossibleXSSLine>>();
    private Vector<String> remainingFilesToProcess = new Vector<String>();

    public ThreadedXSSScanner(int poolSize) {
        Logger.log("Initializing ThreadedXSSScanner with pool size: " + poolSize);
        pool = Executors.newFixedThreadPool(poolSize);
    }

    @Override
    public Set<PossibleXSSLine> scan(FileLoader loader) {
        super.scan(loader);

        long time = System.currentTimeMillis();
        pool.shutdown();
        try {
            Logger.log("Waiting for threads to terminate... 10s");
            while(!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                if(remainingFilesToProcess.size() < 10){
                    StringBuffer sb = new StringBuffer();
                    for(String file : remainingFilesToProcess) {
                        sb. append("......");
                        sb.append(file);
                        sb.append("\n");
                    }
                    Logger.log(sb.toString());
                }

                long soFar = (System.currentTimeMillis() - time) / 1000;
                Logger.log("... remaining files: " + remainingFilesToProcess.size() + "... waiting another 10s, total time: " + soFar + "s");
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Logger.log("... not all threads finished successfully!");
        }
        Logger.log("... finished");

        HashSet<PossibleXSSLine> result = new HashSet<PossibleXSSLine>();
        for(Set<PossibleXSSLine> lines : scanResults) {
            result.addAll(lines);
        }
        return result;
    }

    @Override
    protected Set<PossibleXSSLine> scan(final FileContent f, final FileLoader loader) {
        remainingFilesToProcess.add(f.getFile().getAbsolutePath());
        pool.execute(new Runnable() {
            @Override
            public void run() {
                Set<PossibleXSSLine> result = ThreadedXSSScanner.super.scan(f, loader);
                ThreadedXSSScanner.this.scanResults.add(result);
                remainingFilesToProcess.remove(f.getFile().getAbsolutePath());
            }
        });

        return EMPTY_RESPONSE;
    }
}
