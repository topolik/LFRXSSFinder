package cz.topolik.xssfinder.scan.advanced.parser;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;
import cz.topolik.xssfinder.scan.Logger;
import cz.topolik.xssfinder.scan.advanced.XSSEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class ParenthesesCEP implements ComplexExpressionParser {
    private XSSEnvironment environment;
    private static final String SAFE_EXPRESSION = "\"\"";
    private static final ThreadLocal<Boolean> EXECUTED = new ThreadLocal<Boolean>();

    public ParenthesesCEP(XSSEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public List<String> execute(String expression, int lineNum, String line, FileContent f, FileLoader loader) {
        // execute only once
        if(EXECUTED.get() != null) {
            return RESULT_DONT_KNOW;
        }

        try {
            EXECUTED.set(Boolean.TRUE);
            return execute2(expression, lineNum, line, f, loader);
        } catch (Throwable e) {
            Logger.log("Exception while processing: "+expression+"\n\t"+f.getFile()+':'+lineNum+"\n\t"+line);
            e.printStackTrace();
            return RESULT_DONT_KNOW;
        } finally {
            EXECUTED.set(null);
        }
    }

    private List<String> execute2(String expression, int lineNum, String line, FileContent f, FileLoader loader) {
        if(!isValid(expression)){
            return RESULT_DONT_KNOW;
        }

        boolean executed = false;

        List<StringBuffer> stack = new ArrayList<StringBuffer>();
        List<InsideMethodState> insideMethodStack = new ArrayList<InsideMethodState>();
        boolean insideString = false;
        int insideArray = 0;

        stack.add(new StringBuffer());
        insideMethodStack.add(InsideMethodState.FALSE);

        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);
            switch(ch) {
                case '"': {
                    if(!insideString) {
                        insideString = true;
                    }
                    else if(i > 0 && expression.charAt(i-1)!= '\\') {
                        insideString = false;
                    }
                    break;
                }
                case '(': {
                    if(insideString) {
                        break;
                    }


                    if (i > 0 && Character.isLetterOrDigit(expression.charAt(i - 1))) {
                        insideMethodStack.add(0, new InsideMethodState(true));
                        stack.get(0).append(ch);
                        stack.add(0, new StringBuffer());
                        continue;
                    } else {
                        insideMethodStack.add(0, InsideMethodState.FALSE);
                    }

                    stack.add(0, new StringBuffer());
                    continue;
                }
                case ')': {
                    if(insideString) {
                        break;
                    }

                    if (insideMethodStack.get(0).isIn()) {
                        executed |= processMethodParameter(stack, insideMethodStack, lineNum, line, f, loader);

                        stack.get(1).append(stack.remove(0));
                        insideMethodStack.remove(0);
                        break;
                    }

                    insideMethodStack.remove(0);
                    StringBuffer sb = stack.remove(0);

                    String subExpression = sb.toString();

                    // did we already processed the content?
                    if(needsProcessing(subExpression)){
                        List<String> subExpressionResult = environment.getXSSLogicProcessorHelperUtilThingie().isCallArgumentSuspected(subExpression, lineNum, line, f, loader);
                        if (subExpressionResult == RESULT_SAFE) {
                            subExpression = SAFE_EXPRESSION;
                        }

                        executed = true;
                    }

                    stack.get(0).append(subExpression);
                    continue;
                }
                /*
                 Evaluate method parameter / expression as early as possible
                 - safe parameter replace with "1"
                 - safe expression remove
                 */
                case '[':
                case '{': {
                    if (insideString) {
                        break;
                    }

                    insideArray++;
                    break;
                }
                case ']':
                case '}': {
                    if (insideString) {
                        break;
                    }

                    insideArray--;
                    break;
                }
                case ',': {
                    if (insideString) {
                        break;
                    }

                    if (insideArray != 0) {
                        break;
                    }

                    if (!insideMethodStack.get(0).isIn()) {
                        break;
                    }

                    executed |= processMethodParameter(stack, insideMethodStack, lineNum, line, f, loader);
                    break;
                }
                case '|':
                case '&':
                case '+':
                case '-':
                case '*':
                case '/': {
                    if(insideString) {
                        break;
                    }

                    if((ch == '|' || ch == '&') && i > 0 && expression.charAt(i - 1) == ch) {
                        break;
                    }

                    int startPos = 0;

                    if (insideMethodStack.get(0).isIn()) {
                        startPos = insideMethodStack.get(0).actualParameterStartPos;
                    }

                    String subExpression = stack.get(0).toString().substring(startPos);
                    if(needsProcessing(subExpression)){
                        List<String> subExpressionResult = environment.getXSSLogicProcessorHelperUtilThingie().isCallArgumentSuspected(subExpression, lineNum, line, f, loader);
                        if (subExpressionResult == RESULT_SAFE) {
                            // expression is safe - we can cut it out
                            executed = true;
                            stack.get(0).setLength(startPos);
                            // skip && and || when we removed the expression
                            if (ch == '|' || ch == '&') {
                                i++;
                            }
                            continue;
                        }
                    }

                    break;
                }
            }

            stack.get(0).append(ch);
        }

        if (executed) {
            String simplifiedExpression = stack.get(0).toString();
            return environment.getXSSLogicProcessorHelperUtilThingie().isCallArgumentSuspected(simplifiedExpression, lineNum, line, f, loader);
        }

        return RESULT_DONT_KNOW;
    }


    protected boolean processMethodParameter(List<StringBuffer> stack, List<InsideMethodState> insideMethodStack, int lineNum, String line, FileContent f, FileLoader loader){
        boolean executed = false;

        StringBuffer buffer = stack.get(0);
        int startPos = insideMethodStack.get(0).actualParameterStartPos;
        insideMethodStack.get(0).actualParameterStartPos = buffer.length() + 1;

        String actualParam = buffer.toString().substring(startPos);

        if(!needsProcessing(actualParam)){
            return false;
        }

        List<String> subExpressionResult = environment.getXSSLogicProcessorHelperUtilThingie().isCallArgumentSuspected(actualParam, lineNum, line, f, loader);
        if (subExpressionResult == RESULT_SAFE) {
            buffer.setLength(startPos);
            buffer.append(SAFE_EXPRESSION);
            insideMethodStack.get(0).actualParameterStartPos = buffer.length() + 1;

            executed = true;
        }

        return executed;
    }


    Pattern PATT = Pattern.compile("^([\\w]*|\"[^\"]*\")$");
    protected boolean needsProcessing(String arg) {
        arg = arg.trim();
        return arg.length() > 0 && !PATT.matcher(arg).matches();
    }

    protected boolean isValid(String expression) {
        boolean hasParenthesis = false;
        int check = 0;
        boolean insideString = false;

        for (int i = 0; i < expression.length(); i++) {
            if(check < 0) {
                return false;
            }

            char ch = expression.charAt(i);
            switch (ch) {
                case '"': {
                    if(!insideString) {
                        insideString = true;
                    }
                    else if(i > 0 && expression.charAt(i-1)!= '\\') {
                        insideString = false;
                    }
                    break;
                }
                case '(': {
                    if (insideString) {
                        break;
                    }

                    check++;

//                    if (i > 0 && expression.charAt(i - 1) >= 97 && expression.charAt(i - 1) <= 122) {
//                        break;
//                    }

                    hasParenthesis = true;
                    break;
                }
                case ')': if(!insideString) check--; break;
            }
        }

        return hasParenthesis && check == 0 && !insideString;
    }
}

class InsideMethodState {
    public static final InsideMethodState FALSE = new InsideMethodState(false);
    boolean in = false;
    int actualParameterStartPos = 0;

    public InsideMethodState(boolean insideMethod) {
        this.in = insideMethod;
    }

    public boolean isIn() {
        return in;
    }

    @Override
    public String toString() {
        return "InsideMethodState{" +
                "in=" + in +
                ", actualParameterStartPos=" + actualParameterStartPos +
                '}';
    }
}

