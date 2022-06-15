package aditya.emu.emu6502.hll;

import java.util.*;

public class HLL
{
    int parseExpression(String exp,int i,int len)
    {
        Stack<String> out = new Stack<String>();
        Stack<String> op = new Stack<String>();
        String t = "";
        int tt = 0;
        for (;i<len;i++)
        {
            char c = exp.charAt(i);
            if (Token.getCharType(c)!=tt)
            {
                if (t.trim().length()!=0)
                {
                    if (Token.isOperator(t))
                    {
                        while (!op.isEmpty() && (Token.priority(op.peek())>Token.priority(t) || t.equals(")")))
                        {
                            System.out.println("p: "+op.peek());
                            if (op.peek().equals("("))
                            {
                                op.pop();
                                break;
                            }
                            out.push(op.pop());
                        }
                        if (!t.equals(")"))
                            op.push(t);
                    }
                    else
                    {
                        out.push(t);
                    }
                }
                t = ""+c;
                tt = Token.getCharType(c);
            }
            else
            {
                t+=c;
            }
        }
        out.push(t);
        while (!op.isEmpty())
        {
            out.push(op.pop());
        }
        
        for (int j=0;j<out.size();j++)
        {
            System.out.println(out.get(j));
        }
        
        return i;
    }
    
    public static void test()
    {
        String exp = "1*(2+3)";
        HLL h = new HLL();
        h.parseExpression(exp,0,exp.length());
    }
}
