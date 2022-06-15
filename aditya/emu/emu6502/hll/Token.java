package aditya.emu.emu6502.hll;


public class Token
{
    
    public static final int INT         =   1;
    public static final int CHAR        =   2;
    public static final int OPERATOR    =   3;
    public static final int FUNCTION    =   4;
    
    String data;
    int type;
    
    static int getCharType(char c)
    {
        switch (c)
        {
            case ' ':
                return 1;
            case '+':
            case '-':
            case '*':
            case '/':
            case '|':
            case '&':
            case '^':
            case '~':
            case '<':
            case '>':
            case '=':
            case ',':
                return 2;
            case '(':
            case ')':
                return 3;
        }
        return 0;
    }
    
    
    
    static int priority(String op)
    {
        switch (op)
        {
            case ">":
            case "<":
            case "==":
            case ">=":
            case "<=":
                return 1;
            case "+":
            case "-":
                return 10;
            case "*":
                return 20;
            case "/":
                return 21;
            case "|":
            case "&":
            case "^":
                return 30;
            case "~":
                return 31;
        }
        return 0;
    }
    
    
    static boolean isOperator(String token)
    {
        switch(token)
        {
            case "+":
            case "-":
            case "*":
            case "/":
            case "|":
            case "&":
            case "^":
            case "~":
            case "<<":
            case ">>":
            case ">":
            case "<":
            case "==":
            case ">=":
            case "<=":
            case ",":
                return true;
        }
        return false;
    }
    
}
