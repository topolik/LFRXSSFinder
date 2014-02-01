package cz.topolik.xssfinder.v2.animal.fish;

import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.water.Droplet;
import cz.topolik.xssfinder.v2.water.Water;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class BigRareRainbowFish implements RainbowFish {
    private static final String SAFE_EXPRESSION = "\"\"";
    private static final ThreadLocal<Boolean> EXECUTED = new ThreadLocal<Boolean>();
    Pattern PATT = Pattern.compile("^([\\w]*|\"[^\"]*\")$");

    @Override
    public Water swallow(Droplet droplet) {
        // execute only once
        if (EXECUTED.get() != null) {
            return Water.UNKNOWN_WATER;
        }

        try {
            EXECUTED.set(Boolean.TRUE);

            return swallowCarefully(droplet);

        } catch (Throwable e) {
            World.announce("Exception while processing: " + droplet.getExpression() + "\n\t" + droplet.getTreeRoot() + ':' + droplet.getRingNum() + "\n\t" + droplet.getRing(), e);
            return Water.UNKNOWN_WATER;
        } finally {
            EXECUTED.set(null);
        }
    }

    private Water swallowCarefully(Droplet droplet) {
        String expression = droplet.getExpression();

        if (!isValid(expression)) {
            return Water.UNKNOWN_WATER;
        }

        boolean executed = false;
        Water result = new Water();

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
                        Water subExpressionResult = droplet.droppy(subExpression).dryUp();
                        if (subExpressionResult.equals(Water.CLEAN_WATER)) {
                            subExpression = SAFE_EXPRESSION;
                        } else {
                            result.add(subExpression);
                            result.add(subExpressionResult);
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
                    Water subExpressionResult = null;

                    if (subExpression.length() == 0 || subExpression.equals(SAFE_EXPRESSION)) {
                        subExpressionResult = Water.CLEAN_WATER;
                    } else {
                        subExpressionResult = droplet.droppy(subExpression).dryUp();
                    }

                    if (subExpressionResult.equals(Water.CLEAN_WATER)) {
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
                        result.add(subExpressionResult);
                    }

                    break;
                }
            }

            stack.get(0).append(ch);
        }

        if (executed) {
            String simplifiedExpression = stack.get(0).toString();
            Water seResult = droplet.droppy(simplifiedExpression).dryUp();
            if (seResult.equals(Water.CLEAN_WATER)) {
                return Water.CLEAN_WATER;
            }

            result.add(simplifiedExpression);
            result.add(seResult);
            return result;
        }

        return Water.UNKNOWN_WATER;
    }

    protected boolean processMethodParameter(List<StringBuffer> stack, List<InsideMethodState> insideMethodStack, Water result, Droplet droplet) {
        boolean executed = false;

        StringBuffer buffer = stack.get(0);
        int startPos = insideMethodStack.get(0).sentenceStart;
        insideMethodStack.get(0).sentenceStart = buffer.length() + 1;

        String actualParam = buffer.toString().substring(startPos);

        if (!needsProcessing(actualParam)) {
            return false;
        }

        Water subExpressionResult = droplet.droppy(actualParam).dryUp();
        if (subExpressionResult.equals(Water.CLEAN_WATER)) {
            buffer.setLength(startPos);
            buffer.append(SAFE_EXPRESSION);
            insideMethodStack.get(0).sentenceStart = buffer.length() + 1;

            executed = true;
        } else {
            result.add(actualParam);
            result.add(subExpressionResult);
        }

        return executed;
    }

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

