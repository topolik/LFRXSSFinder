package cz.topolik.xssfinder.v2.butterfly;

import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.water.Droplet;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class BigRareColoredButterfly implements ColoredButterfly {
    private static final String SAFE_EXPRESSION = "\"\"";
    private static final ThreadLocal<Boolean> EXECUTED = new ThreadLocal<Boolean>();

    @Override
    public List<String> execute(Droplet droplet) {
        // execute only once
        if (EXECUTED.get() != null) {
            return RESULT_DONT_KNOW;
        }

        try {
            EXECUTED.set(Boolean.TRUE);
            return execute2(droplet);
        } catch (Throwable e) {
            World.announce("Exception while processing: " + droplet.getExpression() + "\n\t" + droplet.getTree().getRoot() + ':' + droplet.getGrowthRingNum() + "\n\t" + droplet.getGrowthRing(), e);
            return RESULT_DONT_KNOW;
        } finally {
            EXECUTED.set(null);
        }
    }

    private List<String> execute2(Droplet droplet) {
        String expression = droplet.getExpression();

        if (!isValid(expression)) {
            return RESULT_DONT_KNOW;
        }

        boolean executed = false;
        List<String> result = new ArrayList<String>();

        List<StringBuffer> stack = new ArrayList<StringBuffer>();
        List<InsideMethodState> insideMethodStack = new ArrayList<InsideMethodState>();
        boolean insideString = false;
        int insideArray = 0;

        stack.add(new StringBuffer());
        insideMethodStack.add(new InsideMethodState(false));

        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);
            switch (ch) {
                case '"': {
                    if (!insideString) {
                        insideString = true;
                    } else if (i > 0 && expression.charAt(i - 1) != '\\') {
                        insideString = false;
                    }
                    break;
                }
                case '(': {
                    if (insideString) {
                        break;
                    }


                    if (i > 0 && Character.isLetterOrDigit(expression.charAt(i - 1))) {
                        insideMethodStack.add(0, new InsideMethodState(true));
                        stack.get(0).append(ch);
                        stack.add(0, new StringBuffer());
                        continue;
                    } else {
                        insideMethodStack.add(0, new InsideMethodState(false));
                    }

                    stack.add(0, new StringBuffer());
                    continue;
                }
                case ')': {
                    if (insideString) {
                        break;
                    }

                    if (insideMethodStack.get(0).isIn()) {
                        executed |= processMethodParameter(stack, insideMethodStack, result, droplet);

                        stack.get(1).append(stack.remove(0));
                        insideMethodStack.remove(0);
                        break;
                    }

                    insideMethodStack.remove(0);
                    StringBuffer sb = stack.remove(0);

                    String subExpression = sb.toString();

                    // did we already processed the content?
                    if (needsProcessing(subExpression)) {
                        List<String> subExpressionResult = World.see().river().isCallArgumentSuspected(droplet.droppy(subExpression));
                        if (subExpressionResult == RESULT_SAFE) {
                            subExpression = SAFE_EXPRESSION;
                        } else {
                            result.add(subExpression);
                            result.addAll(subExpressionResult);
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

                    executed |= processMethodParameter(stack, insideMethodStack, result, droplet);
                    break;
                }
                case '|':
                case '&':
                case '+':
                case '-':
                case '<':
                case '>':
                case '*':
                case '/': {
                    if (insideString) {
                        break;
                    }

                    if ((ch == '|' || ch == '&') && i > 0 && expression.charAt(i - 1) == ch) {
                        break;
                    }

                    int startPos = insideMethodStack.get(0).sentenceStart;
                    insideMethodStack.get(0).sentenceStart = stack.get(0).toString().length() + 1;

                    String subExpression = stack.get(0).toString().substring(startPos).trim();
                    List<String> subExpressionResult = null;

                    if (subExpression.length() == 0 || subExpression.equals(SAFE_EXPRESSION)) {
                        subExpressionResult = RESULT_SAFE;
                    } else {
                        subExpressionResult = World.see().river().isCallArgumentSuspected(droplet.droppy(subExpression));
                    }

                    if (subExpressionResult == RESULT_SAFE) {
                        // expression is safe - we can cut it out
                        executed = true;
                        stack.get(0).setLength(startPos);
                        insideMethodStack.get(0).sentenceStart = startPos;

                        // skip && and || when we removed the expression
                        if (ch == '|' || ch == '&') {
                            i++;
                        }

                        continue;
                    } else if (subExpressionResult != null) {
                        result.add(subExpression);
                        result.addAll(subExpressionResult);
                    }

                    break;
                }
            }

            stack.get(0).append(ch);
        }

        if (executed) {
            String simplifiedExpression = stack.get(0).toString();
            List<String> seResult = World.see().river().isCallArgumentSuspected(droplet.droppy(simplifiedExpression));
            if (seResult == RESULT_SAFE) {
                return RESULT_SAFE;
            }

            result.add(simplifiedExpression);
            result.addAll(seResult);
            return result;
        }

        return RESULT_DONT_KNOW;
    }


    protected boolean processMethodParameter(List<StringBuffer> stack, List<InsideMethodState> insideMethodStack, List<String> result, Droplet droplet) {
        boolean executed = false;

        StringBuffer buffer = stack.get(0);
        int startPos = insideMethodStack.get(0).sentenceStart;
        insideMethodStack.get(0).sentenceStart = buffer.length() + 1;

        String actualParam = buffer.toString().substring(startPos);

        if (!needsProcessing(actualParam)) {
            return false;
        }

        List<String> subExpressionResult = World.see().river().isCallArgumentSuspected(droplet.droppy(actualParam));
        if (subExpressionResult == RESULT_SAFE) {
            buffer.setLength(startPos);
            buffer.append(SAFE_EXPRESSION);
            insideMethodStack.get(0).sentenceStart = buffer.length() + 1;

            executed = true;
        } else {
            result.add(actualParam);
            result.addAll(subExpressionResult);
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
        boolean hasSpecialChar = false;
        int check = 0;
        boolean insideString = false;

        for (int i = 0; i < expression.length(); i++) {
            if (check < 0) {
                return false;
            }

            char ch = expression.charAt(i);
            switch (ch) {
                case '"': {
                    if (!insideString) {
                        insideString = true;
                    } else if (i > 0 && expression.charAt(i - 1) != '\\') {
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
                case ')':
                    if (!insideString) check--;
                    break;
                case '-':
                case '+':
                case '<':
                case '>':
                case '*':
                case '/':
                case '|':
                case '&':
                    if (!insideString) hasSpecialChar = true;
                    break;

            }
        }

        return (hasParenthesis || hasSpecialChar) && check == 0 && !insideString;
    }
}

class InsideMethodState {
    boolean in = false;
    int sentenceStart = 0;

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
                ", sentenceStart=" + sentenceStart +
                '}';
    }
}

