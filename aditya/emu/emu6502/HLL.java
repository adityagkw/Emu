package aditya.emu.emu6502;
import java.io.*;
import java.util.*;
public class HLL
{
    public class HLLType
    {
        String name;
        int size;
        public HLLType(String name,int size)
        {
            this.name=name;
            this.size=size;
        }
    }
    public class HLLVar
    {
        String name;
        HLLType type;
        public HLLVar(String name,String type,int size)
        {
            this.name=name;
            this.type=new HLLType(type,size);
        }
        public HLLVar(String name,HLLType type)
        {
            this.name=name;
            this.type=type;
        }
    }
    public class HLLFunc
    {
        String name;
        HLLType returntype;
        HLLVar[] arguments;
        String code;
        String data;
        boolean extern;
        public HLLFunc(String name,HLLType returntype,HLLVar[] arguments)
        {
            this.name=name;
            this.returntype=returntype;
            this.arguments=arguments;
            this.code="";
            this.data="";
            this.extern=false;
        }
        public HLLFunc(String name,HLLType returntype,HLLVar[] arguments,boolean extern)
        {
            this.name=name;
            this.returntype=returntype;
            this.arguments=arguments;
            this.code="";
            this.data="";
            this.extern=extern;
        }
    }
    String[] raw=null;
    String code="";
    String data="";
    String name="";
    String ff="";
    ArrayList<HLLVar> var=new ArrayList<HLLVar>();
    ArrayList<HLLFunc> func=new ArrayList<HLLFunc>();
    Stack<HLLFunc> funcstack=new Stack<HLLFunc>();
    int symbols=3;
    String ns="_";
    
    HLLType HLL_void=new HLLType("void",0);
    HLLType HLL_byte=new HLLType("byte",1);
    HLLType HLL_char=new HLLType("char",1);
    
    
    public static void main(String[] args)
    {
        HLL h=new HLL();
        if (args==null || args.length==0)
        {
            h.error("No input file given");
        }
        try
        {
            String fn=args[0].substring(0,args[0].lastIndexOf('.'));
            Scanner s=new Scanner(new FileInputStream(args[0]));
            String d="";
            while (s.hasNextLine())
            {
                d+=s.nextLine()+"\n";
            }
            s.close();
            System.out.println(d);
            h.raw=d.split("\n");
            h.run();
            FileOutputStream fos=new FileOutputStream(fn+".asm");
            fos.write(h.ff.getBytes());
            //fos.write(h.code.getBytes());
            fos.flush();
            fos.close();
        }catch(Exception e){e.printStackTrace();}
    }
    public void run()
    {
        HLLFunc mainf=new HLLFunc("",getType("void"),new HLLVar[0]);
        funcstack.add(mainf);
        code+="section .TEXT\n";
        data+="section .DATA\n";
        int pp=0;
        for (int ln=0;ln<raw.length;ln++)
        {
            String l=raw[ln].trim();
            int p=checkPriority(raw[ln]);
            if (p<pp)
            {
                for (int i=0;i<pp-p;i++)
                {
                    code+="rts\n";
                    HLLFunc f1=funcstack.pop();
                    f1.code=code;
                    f1.data=data;
                    HLLFunc f2=funcstack.peek();
                    code=f2.code;
                    data=f2.data;
                    name=f2.name;
                }
            }
            if (l.startsWith("ASM "))
            {
                code+=l.substring(4)+"\n";
            }
            else if (l.startsWith("extern "))
            {
                if (l.startsWith("extern def "))
                {
                    String sig=l.substring(11).replace("("," ").replace(")"," ").replace(","," ");
                    String[] sigp=sig.split(" ");
                    HLLType rt=getType(sigp[0]);
                    String fn=sigp[1];
                    HLLVar[] sv=new HLLVar[sigp.length/2-1];
                    for (int i=0;i<sv.length;i++)
                    {
                        HLLType fst=getType(sigp[2+2*i]);
                        String fsn=ns+fn+ns+sigp[3+2*i];
                        sv[i]=new HLLVar(fsn,fst);
                        var.add(sv[i]);
                    }
                    HLLFunc f=new HLLFunc(ns+fn,rt,sv,true);
                    func.add(f);
                }
                else
                {
                    String[] es=l.substring(7).split(" ");
                    HLLType dt=getType(es[0]);
                    if (dt!=null)
                    {
                        var.add(new HLLVar(ns+es[1],dt));
                    }
                }
            }
            else if (l.startsWith("def "))
            {
                String sig=l.substring(4).replace("("," ").replace(")"," ").replace(","," ");
                String[] sigp=sig.split(" ");
                HLLType rt=getType(sigp[0]);
                String fn=name+ns+sigp[1];
                HLLVar[] sv=new HLLVar[sigp.length/2-1];
                for (int i=0;i<sv.length;i++)
                {
                    HLLType fst=getType(sigp[2+2*i]);
                    String fsn=fn+ns+sigp[3+2*i];
                    sv[i]=new HLLVar(fsn,fst);
                    var.add(sv[i]);
                }
                HLLFunc f=new HLLFunc(fn,rt,sv);
                func.add(f);
                HLLFunc f1=funcstack.peek();
                f1.code=code;
                f1.data=data;
                funcstack.push(f);
                name=fn;
                code=fn+":\n";
                data="";
                p++;
            }
            else
            {
                String[] ls=l.split(" ");
                HLLType dt=getType(ls[0]);
                System.out.println("Data type: "+ls[0]+" "+dt);
                if (dt!=null)
                {
                    if (l.contains("="))
                    {
                        System.out.println("Data type: = "+ls[0]+" "+dt);
                        String vn=name+ns+l.substring(ls[0].length()+1,l.indexOf("=")).trim();
                        data+=vn+"=*\n";
                        declareExp(l.substring(l.indexOf("=")+1),vn);
                        var.add(new HLLVar(vn,dt));
                    }
                    else
                    {
                        System.out.println("Data type: no= "+ls[0]+" "+dt);
                        String vn=name+ns+l.substring(ls[0].length()+1).trim();
                        data+=vn+"=*\n";
                        var.add(new HLLVar(vn,dt));
                    }
                }
            }
            pp=p;
        }
        for (int i=0;i<pp;i++)
        {
            code+="rts\n";
            HLLFunc f1=funcstack.pop();
            f1.code=code;
            f1.data=data;
            HLLFunc f2=funcstack.peek();
            code=f2.code;
            data=f2.data;
            name=f2.name;
        }
        code+="\n";
        data+="\n\n";
        ff+=data;
        ff+=code;
        for (int i=0;i<func.size();i++)
        {
            HLLFunc f=func.get(i);
            if (!f.name.equals(""))
            {
                ff+=f.data;
                ff+=f.code+"\n";
            }
        }
    }
    
    public void error(String s)
    {
        System.err.println(s);
    }
    public void declareExp(String s,String ve)
    {
        String a="";
        String[] se=splitExp(s);
        int[] pe=priorityExp(se);
        for (int i=0;i<se.length;i++)
        {
            System.out.print(se[i]+":");
            System.out.print(pe[i]+(i==pe.length-1?"":","));
        }
        System.out.println();
        /*for (int i=0;i<var.size();i++)
        {
            HLLVar v=(HLLVar)var.get(i);
            //System.out.println("var: "+v.name);
            if (v.name.equals(s.trim()))
            {
                data+="*0\n";
                code+="lda "+v.name+"\nsta "+ve+"\n";
                return;
            }
        }*/
        boolean parsed=false;
        String ss=s;
        String done="{done}";
        int t=0;
        while (!parsed)
        {
            int mp=0;
            for (int i=0;i<se.length;i++)
            {
                if (pe[i]>mp)
                    mp=pe[i];
            }
            for (int i=0;i<se.length;i++)
            {
                if(pe[i]==mp)
                {
                    if (isSpecial(se[i].charAt(0)))
                    {
                        if (se[i].equals("+"))
                        {
                            //System.out.println("+++");
                            if (i!=0)
                            {
                                String lhs=format(se[i-1]);
                                String rhs=format(se[i+1]);
                                if (lhs.startsWith(done))
                                {
                                    String lhs2=lhs.substring(6);
                                    for (int j=0;j<se.length;j++)
                                    {
                                        if (se[j].equals(lhs))
                                            se[j]=done+t;
                                    }
                                    lhs=lhs2;
                                }
                                if (rhs.startsWith(done))
                                {
                                    String rhs2=rhs.substring(6);
                                    for (int j=0;j<se.length;j++)
                                    {
                                        if (se[j].equals(rhs))
                                            se[j]=done+t;
                                    }
                                    rhs=rhs2;
                                }
                                code+="lda "+lhs+"\nadc "+rhs+"\nsta "+t+"\n";
                                se[i-1]=done+t;
                                se[i]=done+t;
                                se[i+1]=done+t;
                                
                                pe[i-1]=0;
                                pe[i]=0;
                                pe[i+1]=0;
                                t++;
                            }
                            code+="clc\nclv\n";
                        }
                        else if (se[i].equals("-"))
                        {
                            //System.out.println("---");
                            if (i!=0)
                            {
                                String lhs=format(se[i-1]);
                                String rhs=format(se[i+1]);
                                if (lhs.startsWith(done))
                                {
                                    String lhs2=lhs.substring(6);
                                    for (int j=0;j<se.length;j++)
                                    {
                                        if (se[j].equals(lhs))
                                            se[j]=done+t;
                                    }
                                    lhs=lhs2;
                                }
                                if (rhs.startsWith(done))
                                {
                                    String rhs2=rhs.substring(6);
                                    for (int j=0;j<se.length;j++)
                                    {
                                        if (se[j].equals(rhs))
                                            se[j]=done+t;
                                    }
                                    rhs=rhs2;
                                }
                                code+="lda "+lhs+"\nsbc "+rhs+"\nsta "+t+"\n";
                                se[i-1]=done+t;
                                se[i]=done+t;
                                se[i+1]=done+t;
                                pe[i-1]=0;
                                pe[i]=0;
                                pe[i+1]=0;
                                t++;
                            }
                            code+="clc\nclv\n";
                        }
                    }
                }
            }
            boolean left=false;
            for (int i=0;i<se.length;i++)
            {
                if (!se[i].startsWith(done))
                {
                    left=true;
                    break;
                }
            }
            /*for (int i=0;i<se.length;i++)
            {
                System.out.print(se[i]+" ");
            }
            System.out.println();*/
            if (!left)
            {
                parsed=true;
                data+="*0\n";
                code+="lda "+(t-1)+"\nsta "+ve+"\n";
                return;
            }
            if (se.length==1)
            {
                
                HLLVar v=getVar(s.trim());
                //System.out.println("len 1 "+s.trim()+" "+v);
                if (v!=null)
                {
                    data+="*0\n";
                    code+="lda "+v.name+"\nsta "+ve+"\n";
                    return;
                }
                /*for (int i=0;i<var.size();i++)
                {
                    HLLVar v=(HLLVar)var.get(i);
                    
                    //System.out.println("var: "+v.name);
                    if (v.name.equals(s.trim()))
                    {
                        data+="*0\n";
                        code+="lda "+v.name+"\nsta "+ve+"\n";
                        return;
                    }
                }*/
                parsed=true;
            }
        }
        //System.out.println("else: "+s);
        String fs=format(s);
        if (fs.startsWith("#"))
        {
            fs=fs.substring(1);
        }
        data+="*"+fs+"\n";
    }
    public String[] splitExp(String s)
    {
        ArrayList<String> al=new ArrayList<String>();
        String w="";
        boolean op=false;
        for (int i=0;i<s.length();i++)
        {
            char c=s.charAt(i);
            w+=c;
            if (i==s.length()-1)
            {
                al.add(w);
            }
            else
            {
                char c2=s.charAt(i+1);
                
                if (c=='(' || c==')' || c2=='(' || c2==')')
                {
                    al.add(w);
                    w="";
                    op=isSpecial(c2);
                    //i++;
                }
                else if (isSpecial(c2)!=op)
                {
                    op=!op;
                    al.add(w);
                    w="";
                }
            }
        }
        String[] ans=new String[al.size()];
        for (int i=0;i<ans.length;ans[i]=al.get(i),i++);
        return ans;
    }
    public int[] priorityExp(String[] exp)
    {
        int[] p=new int[exp.length];
        int pp=1;
        for (int i=0;i<p.length;i++)
        {
            String s=exp[i];
            int cp=pp;
            if (s.equals("("))
            {
                cp++;
            }
            else if (s.equals(")"))
            {
                cp--;
            }
            if (isSpecial(s.charAt(0)))
            {
                if (s.equals("+"))
                {
                    p[i]=symbols*cp+1;
                }
                else if (s.equals("-"))
                {
                    p[i]=symbols*cp+2;
                }
                else if (s.equals(","))
                {
                    p[i]=symbols*cp+3;
                }
            }
            else
            {
                p[i]=symbols*cp;
            }
            pp=cp;
        }
        return p;
    }
    public boolean isSpecial(char c)
    {
        return c=='+'||c=='-'||c==',';
    }
    public String format(String s)
    {
        if (Character.isDigit(s.charAt(0)))
        {
            return "#"+s;
        }
        else if (s.equals("true"))
        {
            return "#1";
        }
        else if (s.equals("false"))
        {
            return "#0";
        }
        //System.out.println("format "+s);
        HLLVar v=getVar(s);
        if (v!=null)
        {
            return v.name;
        }
        return s;
    }
    public HLLVar getVar(String name)
    {
        HLLVar v=null;
        /*for (int i=0;i<var.size();i++)//Local
        {
            HLLVar v2=(HLLVar)var.get(i);
            if ((this.name+ns+name).equals(v2.name))
            {
                return v2;
            }
        }*/
        
        //System.out.println("name: "+this.name);
        String[] nsp=(this.name).split(ns);
        /*for (int j=1;j<nsp.length;j++)
        {
            System.out.println("ns: "+nsp[j]);
        }*/
        for (int j=nsp.length;j>=0;j--)//Local
        {
            String n2="";
            for (int k=0;k<j;k++)
            {
                n2+=nsp[k]+ns;
            }
            n2+=name;
            //n2=n2.substring(1);
            //System.out.println("n2 "+n2);
            for (int i=0;i<var.size();i++)
            {
                HLLVar v2=(HLLVar)var.get(i);
                //System.out.println("var "+v2.name);
                if (n2.equals(v2.name))
                {
                    //System.out.println("return "+v2.name);
                    return v2;
                }
            }
        }
        String n2=ns+name;
        //n2=n2.substring(1);
        //System.out.println("n2 "+n2);
        for (int i=0;i<var.size();i++)//Global
        {
            HLLVar v2=(HLLVar)var.get(i);
            if (n2.equals(v2.name))
            {
                return v2;
            }
        }
        return v;
    }
    public HLLType getType(String t)
    {
        if (t.equals("byte"))
        {
            return HLL_byte;
        }
        else if (t.equals("char"))
        {
            return HLL_char;
        }
        else if (t.equals("void"))
        {
            return HLL_void;
        }
        return null;
    }
    public int checkPriority(String s)
    {
        int n=0;
        for (int i=0;i<s.length();i++)
        {
            if (s.charAt(i)!='\t')
                break;
            n++;
        }
        return n;
    }
    public static void test()
    {
        main(new String[]{"hll test.txt"});
    }
}
