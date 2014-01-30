package cz.topolik.xssfinder.v2.water;

import cz.topolik.xssfinder.scan.Logger;
import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.wood.Tree;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class Rain {
    /**
     * normalized (lowercase) method names - we can then check static methods and "normal" methods together
     */
    private Set<String> safeMethodsString = new HashSet<String>();
    Map<String, ParsedClass> parsedClasses = new HashMap<String, ParsedClass>();

    private static final Pattern CLASS_DECLARATION = Pattern.compile("^public (abstract class|class|interface) ([^\\s\\{]+).*$");
    private static final Pattern METHOD_DECLARATION = Pattern.compile("^\\s?public (static )?([a-zA-Z.]+\\.)?([^.\\s]+|[a-zA-Z]+<[^\\s]+>) ([\\w]+)\\(.*$");

    private static final Pattern SAFE_RETURN_TYPES = Pattern.compile(River.SAFE_VARIABLE_DECLARATION_START.trim());

    public void fallDown() {
        initRegistry();

        Logger.log("Wiring " + parsedClasses.size() + " classes together by inheritance ....");
        wireClasses();
        Logger.log(" ... finished");

        Logger.log("Creating list of all safe methods ....");
        createSafeMethodsList();
        Logger.log(" ... finished");
    }

    protected void initRegistry() {
        List<Tree> trees = World.see().forest().birches();
        Logger.log("PortalAPICallsProcessor is registering " + trees.size() + " files ....");
        for (Tree tree : trees) {
            water(tree);
        }
        Logger.log(" ... finished");
    }

    protected void wireClasses() {
        LinkedList<ParsedClass> unwiredClasses = new LinkedList<ParsedClass>(parsedClasses.values());
        while (unwiredClasses.size() > 0) {
            // remove & process first class from the list
            ParsedClass parsedClass = unwiredClasses.poll();
            boolean parentsWired = true;
            for (Iterator<String> it = parsedClass.getInheritanceClasses().iterator(); it.hasNext() && parentsWired; ) {
                String parent = it.next();
                parentsWired &= !parsedClasses.containsKey(parent) || parsedClasses.get(parent).isInheritanceWired();
            }
            // all parents are complete, we can inherit safe methods
            if (parentsWired) {
                for (String parent : parsedClass.getInheritanceClasses()) {
                    if (parsedClasses.containsKey(parent)) {
                        parsedClass.getMethods().addAll(parsedClasses.get(parent).getMethods());
                    }
                }
                parsedClass.setInheritanceWired(true);
            } else {
                // add back to the end of the list
                unwiredClasses.offer(parsedClass);
            }
        }
    }

    protected void createSafeMethodsList() {
        String DOT = ".";
        String BR = "(";
        for (ParsedClass parsedClass : parsedClasses.values()) {
            for (String method : parsedClass.getMethods()) {
                safeMethodsString.add(parsedClass.getClassName() + DOT + method + BR);
            }
        }
    }

    protected void water(Tree tree) {
        ParsedClass result = null;
        List<String> vulnerableMethods = new ArrayList<String>();

        for (int i = 0; i < tree.getGrowthRings().size(); i++) {
            String line = tree.getGrowthRings().get(i);
            if (result == null) {
                Matcher m = CLASS_DECLARATION.matcher(line);
                if (m.matches()) {
                    String className = normalize(m.group(2));
                    if (!parsedClasses.containsKey(className)) {
                        parsedClasses.put(className, new ParsedClass(className));
                    }
                    result = parsedClasses.get(className);
                    result.setClassName(className);

                    // join lines to have all declarations together
                    String classDeclarationLine = line;
                    int j = i + 1;
                    while (!classDeclarationLine.contains("{")) {
                        classDeclarationLine += " " + tree.getGrowthRings().get(j++).replaceAll("\t", " ") + " ";
                    }

                    if (classDeclarationLine.contains(" extends ")) {
                        int startIndex = classDeclarationLine.indexOf(" extends ") + " extends ".length();
                        int endIndex = classDeclarationLine.contains(" implements ") ? classDeclarationLine.indexOf("implements", startIndex) : classDeclarationLine.indexOf("{", startIndex);
                        String[] extendsClasses = normalize(classDeclarationLine.substring(startIndex, endIndex).split(","));
                        for (String clas_ : extendsClasses) {
                            result.getInheritanceClasses().add(clas_);
                        }
                    }
                }
            } else {
                Matcher m = METHOD_DECLARATION.matcher(line);
                if (m.matches()) {
                    String returnType = m.group(3);
                    String methodName = normalize(m.group(4));

                    if (SAFE_RETURN_TYPES.matcher(returnType).matches()) {
                        result.getMethods().add(methodName);
                    } else {
                        vulnerableMethods.add(methodName);
                    }
                }
            }
        }

        // there may be a safe method added which has String return type
        for (String vulnerableMethod : vulnerableMethods) {
            if (result.getMethods().contains(vulnerableMethod)) {
                result.getMethods().remove(vulnerableMethod);
            }
        }
    }

    public boolean isExpressionSafe(String expression) {
        String normalizedExpression = normalize(expression);
        for (String apiCall : safeMethodsString) {
            if (normalizedExpression.startsWith(apiCall)) {
                return true;
            }
        }
        return false;
    }

    protected String normalize(String s) {
        return s.trim().toLowerCase().replaceAll("<[^>]+>", "").replaceAll("<[^>]+>", "");
    }

    protected String[] normalize(String[] sArray) {
        String[] result = new String[sArray.length];
        for (int i = 0; i < sArray.length; i++) {
            result[i] = normalize(sArray[i]);
        }
        return result;
    }

    class ParsedClass {
        String className;
        Set<String> methods = new HashSet<String>();
        Set<String> inheritanceClasses = new HashSet<String>();
        boolean inheritanceWired;

        public ParsedClass(String className) {
            this.className = className;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public Set<String> getInheritanceClasses() {
            return inheritanceClasses;
        }

        public void setInheritanceClasses(Set<String> inheritanceClasses) {
            this.inheritanceClasses = inheritanceClasses;
        }

        public Set<String> getMethods() {
            return methods;
        }

        public void setMethods(Set<String> methods) {
            this.methods = methods;
        }

        public boolean isInheritanceWired() {
            return inheritanceWired;
        }

        public void setInheritanceWired(boolean inheritanceWired) {
            this.inheritanceWired = inheritanceWired;
        }

    }
}
