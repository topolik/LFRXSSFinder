package cz.topolik.xssfinder.scan.advanced;

import cz.topolik.xssfinder.scan.Logger;
import cz.topolik.xssfinder.FileLoader;

/**
 *
 * @author Tomas Polesovsky
 */
public class XSSEnvironment {
    private TaglibProcessor taglibProcessor;
    private XSSLogicProcessorHelperUtilThingie XSSLogicProcessorHelperUtilThingie;
    private PortalAPICallsProcessor portalAPICallsProcessor;
    private FileLoader fileLoader;

    public FileLoader getFileLoader() {
        return fileLoader;
    }

    public void setFileLoader(FileLoader fileLoader) {
        this.fileLoader = fileLoader;
    }

    public TaglibProcessor getTaglibProcessor() {
        return taglibProcessor;
    }

    public PortalAPICallsProcessor getPortalAPICallsProcessor() {
        return portalAPICallsProcessor;
    }

    public void setPortalAPICallsProcessor(PortalAPICallsProcessor portalAPICallsProcessor) {
        this.portalAPICallsProcessor = portalAPICallsProcessor;
    }

    public void setTaglibProcessor(TaglibProcessor taglibProcessor) {
        this.taglibProcessor = taglibProcessor;
    }

    public XSSLogicProcessorHelperUtilThingie getXSSLogicProcessorHelperUtilThingie() {
        return XSSLogicProcessorHelperUtilThingie;
    }

    public void setXSSLogicProcessorHelperUtilThingie(XSSLogicProcessorHelperUtilThingie XSSLogicProcessorHelperUtilThingie) {
        this.XSSLogicProcessorHelperUtilThingie = XSSLogicProcessorHelperUtilThingie;
    }


    public void init(FileLoader loader) {
        Logger.log("Creating TaglibProcessor");
        setTaglibProcessor(new TaglibProcessor(this));
        Logger.log("Creating XSSLogicProcessorHelperUtilThingie");
        setXSSLogicProcessorHelperUtilThingie(new XSSLogicProcessorHelperUtilThingie(this));
        Logger.log("Creating PortalAPICallsProcessor");
        setPortalAPICallsProcessor(new PortalAPICallsProcessor());

        Logger.log("Initializing portalAPICallsProcessor");
        portalAPICallsProcessor.init(loader);
		Logger.log("Initializing taglibProcessor");
		taglibProcessor.init(loader);
        Logger.log("Environment initialization finished");
    }

    public void destroy() {
        getXSSLogicProcessorHelperUtilThingie().destroy();
    }
}
