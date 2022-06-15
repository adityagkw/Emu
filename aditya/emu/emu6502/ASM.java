package aditya.emu.emu6502;
import java.io.*;
import java.util.*;
public class ASM
{
    byte[] b=new byte[0x10000];
    HashMap<String,String> v=new HashMap<String,String>();
    HashMap<String,Integer> f=new HashMap<String,Integer>();
    int p=0,ps=-1,pe=-1;
    boolean eo=true;
    public static void test()
    {
        ASM.main(new String[]{"asm test.txt","-f"});
        //ASM.main(new String[]{"cps.txt","-f"});
        //ASM.main(new String[]{"key test.txt","-f"});
        //ASM.main(new String[]{"pong.txt"});
    }
    public static void main(String[] args)
    {
        if (args.length<1)
        {
            System.err.println("ASM: No file provided");
            return;
        }
        String tf=args[0].substring(0,args[0].lastIndexOf('.'))+".6502";
        boolean full=true;
        for (int i=0;i<args.length;i++)
        {
            args[i]=args[i].trim();
            /*if (args[i].equalsIgnoreCase("-f"))
            {
                full=true;
            }
            else*/ if (args[i].equalsIgnoreCase("-o"))
            {
                if (i==args.length-1)
                {
                    System.err.println("ASM: No output file provided after -o");
                    return;
                }
                tf=args[i+1];
                i++;
            }
        }
        new ASM().run(args[0],tf,full);
    }
    public void run(String fn,String tf,boolean full)
    {
        CPU6502 cpu=new CPU6502();
        try
        {
            Scanner s=new Scanner(new FileInputStream(fn));
            String l="";
            int ln=0;
            while (s.hasNextLine())
            {
                ln++;
                l=s.nextLine();
                l=l.replace(" ","").replace("\t","");
                if (l.equals(""))
                {
                    continue;
                }
                if (l.startsWith("*="))
                {
                    if (l.length()<3)
                    {
                        //error("Address required after *= in line "+ln);
                        error("Address required after *=");
                    }
                    if (l.substring(2,l.length()).equals("*"))
                    {
                        l="*="+p;
                    }
                    int i=loadNum(l.substring(2,l.length()));
                    p=i;
                    b[0xfffc]=(byte)i;
                    i>>=8;
                    b[0xfffd]=(byte)i;
                    if (ps==-1)
                    {
                        ps=p;
                    }
                }
                else if (l.startsWith("org "))
                {
                    if (l.length()<5)
                    {
                        //error("Address required after *= in line "+ln);
                        error("Address required after ORG ");
                    }
                    if (l.substring(4,l.length()).equals("*"))
                    {
                        l="org "+p;
                    }
                    int i=loadNum(l.substring(4,l.length()));
                    p=i;
                    b[0xfffc]=(byte)i;
                    i>>=8;
                    b[0xfffd]=(byte)i;
                    if (ps==-1)
                    {
                        ps=p;
                    }
                }
                else if (l.startsWith("section "))
                {
                    if (l.length()<9)
                    {
                        //error("Address required after *= in line "+ln);
                        error("Address required after SECTION ");
                    }
                    String sn=l.substring(8,l.length());
                    if (sn.equals(".TEXT"))
                    {
                        p=0x0400;
                    }
                    else if (sn.equals(".DATA"))
                    {
                        p=0x0010;
                    }
                    int i=p;
                    b[0xfffc]=(byte)i;
                    i>>=8;
                    b[0xfffd]=(byte)i;
                    if (ps==-1)
                    {
                        ps=p;
                    }
                }
                else if (l.startsWith("*"))
                {
                    p++;
                }
                else if(l.contains("="))
                {
                    String[] s2=l.split("=");
                    if (s2.length!=2)
                    {
                        //error("= needs 2 operands only on line "+ln);
                        error("= needs 2 operands only");
                    }
                    if (s2[1].trim().equals("*"))
                    {
                        s2[1]=""+p;
                    }
                    v.put(s2[0],s2[1]);
                }
                else if (l.endsWith(":"))
                {
                    f.put(l.substring(0,l.length()-1).trim(),p);
                    //System.out.println(l.substring(0,l.length()-1)+": "+Integer.toHexString(p));
                }
                else
                {
                    //System.out.println(l);
                    String op=l.substring(0,3);
                    l=l.substring(3,l.length());
                    String[] ar=l.split(",");
                    l="";
                    for (int i=0;i<ar.length;i++)
                    {
                        ar[i]=ar[i].trim();
                        if (ar[i].contains("["))
                        {
                            ar[i]=ar[i].substring(0,ar[i].indexOf("[")).trim();
                        }
                        if (v.containsKey(ar[i]))
                        {
                            ar[i]=v.get(ar[i]);
                        }
                        eo=false;
                        if (!ar[i].equals("") && !ar[i].equalsIgnoreCase("A") && !ar[i].equalsIgnoreCase("X") && !ar[i].equalsIgnoreCase("Y") && loadNum(ar[i])==-1)
                        {
                            ar[i]="$00";
                        }
                        eo=true;
                        if (i>0)
                        {
                            l+=",";
                        }
                        l+=ar[i];
                    }
                    String ind=getIndexing(l);
                    if (ind.equals("rel  ") && !(op.startsWith("b")||op.startsWith("B")))
                    {
                        ind="abs  ";
                    }
                    l=l.replace("(","").replace(")","").replace("*","").replace("#","").replace("A","");
                    String arg[]=l.split(",");
                    for (int i=0;i<cpu.opcode_names.length;i++)
                    {
                        if (op.equalsIgnoreCase(cpu.opcode_names[i]) && ind.equals(cpu.opcode_mode[i]))
                        {
                            //System.out.println(op+"->"+ind);
                            p++;
                            if (ind.contains("imd")||ind.contains("zpg")||ind.contains("rel")||ind.contains("#"))
                            {
                                p++;
                            }
                            else if (ind.contains("abs"))
                            {
                                p++;
                                p++;
                            }
                            else if (ind.equals("ind  "))
                            {
                                p++;
                                p++;
                            }
                            else if (ind.contains("ind"))
                            {
                                p++;
                            }
                        }
                    }
                }
                if (p>pe)
                {
                    pe=p;
                }
            }
            s.close();
            ln=0;
            s=new Scanner(new FileInputStream(fn));
            while (s.hasNextLine())
            {
                ln++;
                l=s.nextLine();
                l=l.replace(" ","").replace("\t","");
                if (l.equals(""))
                {
                    continue;
                }
                if (l.startsWith("*="))
                {
                    if (l.length()<3)
                    {
                        //error("Address required after *= in line "+ln);
                        error("Address required after *=");
                    }
                    if (l.substring(2,l.length()).equals("*"))
                    {
                        l="*="+p;
                    }
                    int i=loadNum(l.substring(2,l.length()));
                    p=i;
                    b[0xfffc]=(byte)i;
                    i>>=8;
                    b[0xfffd]=(byte)i;
                }
                else if (l.startsWith("org "))
                {
                    if (l.length()<5)
                    {
                        //error("Address required after *= in line "+ln);
                        error("Address required after ORG ");
                    }
                    if (l.substring(4,l.length()).equals("*"))
                    {
                        l="org "+p;
                    }
                    int i=loadNum(l.substring(4,l.length()));
                    p=i;
                    b[0xfffc]=(byte)i;
                    i>>=8;
                    b[0xfffd]=(byte)i;
                    if (ps==-1)
                    {
                        ps=p;
                    }
                }
                else if (l.startsWith("section "))
                {
                    if (l.length()<9)
                    {
                        //error("Address required after *= in line "+ln);
                        error("Address required after SECTION ");
                    }
                    String sn=l.substring(8,l.length());
                    if (sn.equals(".TEXT"))
                    {
                        p=0x0400;
                    }
                    else if (sn.equals(".DATA"))
                    {
                        p=0x0010;
                    }
                    int i=p;
                    b[0xfffc]=(byte)i;
                    i>>=8;
                    b[0xfffd]=(byte)i;
                    if (ps==-1)
                    {
                        ps=p;
                    }
                }
                else if (l.startsWith("*"))
                {
                    l=l.substring(1,l.length());
                    b[p]=(byte)loadNum(l);
                    p++;
                }
                else if(l.contains(":"))
                {
                    
                }
                else if(l.contains("="))
                {
                    String[] s2=l.split("=");
                    if (s2.length!=2)
                    {
                        //error("= needs 2 operands only on line "+ln);
                        error("= needs 2 operands only");
                    }
                    if (s2[1].trim().equals("*"))
                    {
                        s2[1]=""+p;
                    }
                    v.put(s2[0],s2[1]);
                }
                else
                {
                    String op=l.substring(0,3);
                    //System.out.println(op);
                    l=l.substring(3,l.length());
                    String[] ar=l.split(",");
                    l="";
                    for (int i=0;i<ar.length;i++)
                    {
                        //System.out.print(ar[i]+" "+f);
                        ar[i]=ar[i].trim();
                        int ia=0;
                        if (ar[i].contains("["))
                        {
                            //System.out.println(ar[i]+" "+ar[i].substring(ar[i].indexOf("[")+1,ar[i].indexOf("]")));
                            ia=loadNum(ar[i].substring(ar[i].indexOf("[")+1,ar[i].indexOf("]")).trim());
                            ar[i]=ar[i].substring(0,ar[i].indexOf("[")).trim();
                            //System.out.println(ar[i]+" "+ia);
                        }
                        if (v.containsKey(ar[i]))
                        {
                            //System.out.println(ar[i]+" "+v);
                            ar[i]=v.get(ar[i]);
                            if (ia!=0)
                            {
                                ar[i]=(""+(loadNum(ar[i])+ia)).trim();
                                //System.out.println(ar[i]+" "+ia);
                            }
                        }
                        else if (f.containsKey(ar[i]))
                        {
                            if (op.equalsIgnoreCase("JMP")||op.equalsIgnoreCase("JSR"))
                            {
                                ar[i]=""+f.get(ar[i]);
                            }
                            else
                            {
                                //System.out.println(f.get(ar[i])-p);
                                ar[i]=""+Byte.toUnsignedInt((byte)(f.get(ar[i])-p-2));
                            }
                            //System.out.println(ar[i]);
                        }
                        //System.out.println(ar[i]+" ");
                        if (i>0)
                        {
                            l+=",";
                        }
                        l+=ar[i];
                    }
                    String ind=getIndexing(l);
                    if (ind.equals("rel  ") && !(op.startsWith("b")||op.startsWith("B")))
                    {
                        ind="abs  ";
                    }
                    l=l.replace("(","").replace(")","").replace("*","").replace("#","");
                    if ((l.contains("A")||l.contains("a")) && !l.contains("$"))
                    {
                        l.replace("A","");
                    }
                    String arg[]=l.split(",");
                    for (int i=0;i<cpu.opcode_names.length;i++)
                    {
                        if (op.equalsIgnoreCase(cpu.opcode_names[i]) && ind.equals(cpu.opcode_mode[i]))
                        {
                            b[p]=(byte)i;
                            p++;
                            //System.out.println(op+" "+cpu.opcode_names[i]+" "+ind);
                            if (ind.contains("imd")||ind.contains("zpg")||ind.contains("rel")||ind.contains("#"))
                            {
                                b[p]=(byte)loadNum(arg[0].trim());
                                p++;
                            }
                            else if (ind.contains("abs"))
                            {
                                int n=loadNum(arg[0].trim());
                                b[p]=(byte)n;
                                p++;
                                n>>=8;
                                b[p]=(byte)n;
                                p++;
                            }
                            else if (ind.equals("ind  "))
                            {
                                int n=loadNum(arg[0].trim());
                                b[p]=(byte)n;
                                p++;
                                n>>=8;
                                b[p]=(byte)n;
                                p++;
                            }
                            else if (ind.contains("ind"))
                            {
                                int n=loadNum(arg[0].trim());
                                b[p]=(byte)n;
                                p++;
                            }
                        }
                    }
                }
            }
            s.close();
            FileOutputStream fos=new FileOutputStream(tf);
            if (full)
            {
                fos.write(b);
            }
            else
            {
                byte[] b2=new byte[pe-ps+1];
                for (int i=0;i<b2.length;i++)
                {
                    b2[i+ps]=b[i];
                }
                fos.write(b2);
            }
            fos.flush();
            fos.close();
            String ipcl=Integer.toHexString(Byte.toUnsignedInt(b[0xfffc]));
            String ipch=Integer.toHexString(Byte.toUnsignedInt(b[0xfffd]));
            for (int i=ipcl.length();i<2;ipcl="0"+ipcl,i++);
            for (int i=ipch.length();i<2;ipch="0"+ipch,i++);
            System.out.println("PC: "+ipch+ipcl);
        }catch(Exception e){e.printStackTrace();}
    }
    private int loadNum(String s)
    {
        int i=-1;
        try
        {
        String s2=s;
        if (s2.startsWith("#"))
        {
            s2=s.substring(1,s.length());
        }
        if (s2.startsWith("$"))
        {
            i=Integer.parseInt(s2.substring(1,s2.length()),16);
            //System.out.println(s2+" "+i);
        }
        else if (s2.startsWith("%"))
        {
            i=Integer.parseInt(s2.substring(1,s2.length()),2);
        }
        else if (s2.startsWith("0"))
        {
            if (s2.length()==1)
            {
                i=0;
            }
            else
            {
                i=Integer.parseInt(s2.substring(1,s2.length()),8);
            }
        }
        else if (s2.startsWith("\'"))
        {
            //System.out.println(s2);
            s2=s2.replace("\\n","\n").replace("\\t","\t").replace("\\\'","\'").replace("\\\"","\"").replace("\\0","\0");
            i=(int)s2.charAt(1);
        }
        else
        {
            i=Integer.parseInt(s2.substring(0,s2.length()));
            //System.out.println("e "+s2+" "+i);
        }
        i=Short.toUnsignedInt((short)i);
        }catch(Exception e){if (eo){e.printStackTrace();}}
        return i;
    }
    private String getIndexing(String l)
    {
        String op="?????";
        boolean impl=l.length()==0;
        boolean ind=l.contains("(");
        boolean zpg=l.contains("*");
        boolean imd=l.contains("#");
        boolean a=l.equalsIgnoreCase("A");
        boolean abs=false;
        boolean rel=false;
        if (impl)
        {
            op="impl";
        }
        else if (ind)
        {
            op="ind";
        }
        else if (zpg)
        {
            op="zpg";
        }
        else if(imd)
        {
            op="#";
        }
        else if(a)
        {
            op="A";
        }
        else
        {
            int i=loadNum(l.split(",")[0]);
            if (i>0xff)
            {
                abs=true;
                op="abs";
            }
            else
            {
                rel=true;
                op="rel";
            }
        }
        
        l=l.replace("(","").replace(")","").replace("*","").replace("#","").replace("A","");
        String arg[]=l.split(",");
        
        if (arg.length==2)
        {
            if (arg[1].equalsIgnoreCase("X"))
            {
                if (ind)
                {
                    op="X,"+op;
                }
                else
                {
                    op+=",X";
                }
            }
            if (arg[1].equalsIgnoreCase("Y"))
            {
                op+=",Y";
            }
        }
        for (int i=op.length();i<5;op+=" ",i++);
        return op;
    }
    private void error(String s)
    {
        System.err.println(s);
    }
}
