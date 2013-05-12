package cz.topolik.xssfinder;

import cz.topolik.xssfinder.scan.Logger;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 * @author Tomas Polesovsky
 */
public class FileLoader {
    public static final String DIR_JSPPRECOMPILED = "jsp-precompile";
    public static final String DIR_PORTALIMPL = "portal-impl/src";
    public static final String DIR_PORTALSERVICE = "portal-service/src";
    public static final String DIR_UTILBRIDGES = "util-bridges";
    public static final String DIR_UTILJAVA = "util-java/src";
    public static final String DIR_UTILTAGLIB = "util-taglib/src";

    private static final Pattern JAVA_PATTERN = Pattern.compile("^.*java$");
    private File portalSourcesDir;
    private Map<String, Set<FileContent>> allFiles = new HashMap<String, Set<FileContent>>();

    public FileLoader(File portalSourcesDir) {
        this.portalSourcesDir = portalSourcesDir;
    }

    public void load(String directoryName){
        load(directoryName, JAVA_PATTERN);
    }
    
    public void load(String directoryName, Pattern fileNamePattern){
        if(!allFiles.containsKey(directoryName)){
			File dir = new File(portalSourcesDir, directoryName);
            Logger.log("Loading " + directoryName + " ... from " + dir.getAbsolutePath());
			Set<FileContent> loadedFiles = loadAllFiles(dir, fileNamePattern);
            allFiles.put(directoryName, loadedFiles);
            Logger.log(" ... finished, loaded files: " + loadedFiles.size());
        }
    }
    
    public void load(){
        load(DIR_JSPPRECOMPILED);
        load(DIR_PORTALIMPL);
        load(DIR_PORTALSERVICE);
        load(DIR_UTILBRIDGES);
        load(DIR_UTILJAVA);
        load(DIR_UTILTAGLIB);
    }

    public Set<FileContent> getAllFiles(){
        HashSet<FileContent> result = new HashSet<FileContent>();
        for(Set<FileContent> files : allFiles.values()){
            result.addAll(files);
        }
        return result;
    }

    public Set<FileContent> getFiles(String directoryName){
        if(allFiles.containsKey(directoryName)){
            return allFiles.get(directoryName);
        } else {
            throw new IllegalStateException("Directory is not loaded: " + directoryName);
        }
    }

    public File getPortalSourcesDir() {
        return portalSourcesDir;
    }

    protected void loadFileContent(FileContent f) {
        try {
            List<String> lines = f.getContent();
            Scanner s = new Scanner(f.getFile());
            while (s.hasNextLine()) {
                lines.add(s.nextLine());
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("Cannot read from file! " + ex.getMessage(), ex);
        }
    }

    protected Set<FileContent> loadAllFiles(File entry, Pattern fileNameRE) {
        Set<FileContent> result = new HashSet<FileContent>();
        if (entry.isDirectory()) {
            String[] children = entry.list();
            for (int i=0; i<children.length; i++) {
                result.addAll(loadAllFiles(new File(entry, children[i]), fileNameRE));
            }
        } else {
            if(fileNameRE.matcher(entry.getAbsolutePath()).matches()){
                FileContent f = new FileContent(entry);
                loadFileContent(f);
                result.add(f);
            }
        }
        return result;
    }
}
