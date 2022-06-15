package aditya.emu.emu6502;

import java.io.*;
import java.util.*;

public class Assembler
{
    HashMap<String,Integer> labels = new HashMap<String,Integer>();
    
    public String[] opcode_names=new String[]{
    //0     1     2     3     4     5     6     7     8     9     A     B     C     D     E     F
    "BRK","ORA","???","???","???","ORA","ASL","???","PHP","ORA","ASL","???","???","ORA","ASL","???",//0
    "BPL","ORA","???","???","???","ORA","ASL","???","CLC","ORA","???","???","???","ORA","ASL","???",//1
    "JSR","AND","???","???","BIT","AND","ROL","???","PLP","AND","ROL","???","BIT","AND","ROL","???",//2
    "BMI","AND","???","???","???","AND","ROL","???","SEC","AND","???","???","???","AND","ROL","???",//3
    "RTI","EOR","???","???","???","EOR","LSR","???","PHA","EOR","LSR","???","JMP","EOR","LSR","???",//4
    "BVC","EOR","???","???","???","EOR","LSR","???","CLI","EOR","???","???","???","EOR","LSR","???",//5
    "RTS","ADC","???","???","???","ADC","ROR","???","PLA","ADC","ROR","???","JMP","ADC","ROR","???",//6
    "BVS","ADC","???","???","???","ADC","ROR","???","SEI","ADC","???","???","???","ADC","ROR","???",//7
    "???","STA","???","???","STY","STA","STX","???","DEY","???","TXA","???","STY","STA","STX","???",//8
    "BCC","STA","???","???","STY","STA","STX","???","TYA","STA","TXS","???","???","STA","???","???",//9
    "LDY","LDA","LDX","???","LDY","LDA","LDX","???","TAY","LDA","TAX","???","LDY","LDA","LDX","???",//A
    "BCS","LDA","???","???","LDY","LDA","LDX","???","CLV","LDA","TSX","???","LDY","LDA","LDX","???",//B
    "CPY","CMP","???","???","CPY","CMP","DEC","???","INY","CMP","DEX","???","CPY","CMP","DEC","???",//C
    "BNE","CMP","???","???","???","CMP","DEC","???","CLD","CMP","???","???","???","CMP","DEC","???",//D
    "CPX","SBC","???","???","CPX","SBC","INC","???","INX","SBC","NOP","???","CPX","SBC","INC","???",//E
    "BEQ","SBC","???","???","???","SBC","INC","???","SED","SBC","???","???","???","SBC","INC","???" //F
    };                            //C5 CPM
    
    public String[] opcode_mode=new String[]{
    // 0       1       2       3       4       5       6       7       8       9       A       B       C       D       E       F
    "impl ","X,ind","?????","?????","?????","zpg  ","zpg  ","?????","impl ","#    ","A    ","?????","?????","abs  ","abs  ","?????",//0
    "rel  ","ind,Y","?????","?????","?????","zpg,X","zpg,X","?????","impl ","abs,Y","?????","?????","?????","abs,X","abs,X","?????",//1
    "abs  ","X,ind","?????","?????","zpg  ","zpg  ","zpg  ","?????","impl ","#    ","A    ","?????","abs  ","abs  ","abs  ","?????",//2
    "rel  ","ind,Y","?????","?????","?????","zpg,X","zpg,X","?????","impl ","abs,Y","?????","?????","?????","abs,X","abs,X","?????",//3
    "impl ","X,ind","?????","?????","?????","zpg  ","zpg  ","?????","impl ","#    ","A    ","?????","abs  ","abs  ","abs  ","?????",//4
    "rel  ","ind,Y","?????","?????","?????","zpg,X","zpg,X","?????","impl ","abs,Y","?????","?????","?????","abs,X","abs,X","?????",//5
    "impl ","X,ind","?????","?????","?????","zpg  ","zpg  ","?????","impl ","#    ","A    ","?????","ind  ","abs  ","abs  ","?????",//6
    "rel  ","ind,Y","?????","?????","?????","zpg,X","zpg,X","?????","impl ","abs,Y","?????","?????","?????","abs,X","abs,X","?????",//7
    "?????","ind  ","?????","?????","zpg  ","zpg  ","zpg  ","?????","impl ","?????","impl ","?????","abs  ","abs  ","abs  ","?????",//8
    "rel  ","ind,Y","?????","?????","zpg,X","zpg,X","zpg,Y","?????","impl ","abs,Y","impl ","?????","?????","abs,X","?????","?????",//9
    "#    ","X,ind","#    ","?????","zpg  ","zpg  ","zpg  ","?????","impl ","#    ","impl ","?????","abs  ","abs  ","abs  ","?????",//A
    "rel  ","ind,Y","?????","?????","zpg,X","zpg,X","zpg,Y","?????","impl ","abs,Y","impl ","?????","abs,X","abs,X","abs,Y","?????",//B
    "#    ","X,ind","?????","?????","zpg  ","zpg  ","zpg  ","?????","impl ","#    ","impl ","?????","abs  ","abs  ","abs  ","?????",//C
    "rel  ","ind,Y","?????","?????","?????","zpg,X","zpg,X","?????","impl ","abs,Y","?????","?????","?????","abs,X","abs,X","?????",//D
    "#    ","X,ind","?????","?????","zpg  ","zpg  ","zpg  ","?????","impl ","#    ","impl ","?????","abs  ","abs  ","abs  ","?????",//E
    "rel  ","ind,Y","?????","?????","?????","zpg,X","zpg,X","?????","impl ","abs,Y","?????","?????","?????","abs,X","abs,X","?????" //F
    };
    
    byte[] machineCode = new byte[0x10000];
    int pointer;
    
    public void open(String fn,String ofn) //Opens and reads a file
    {
        String contents = "";
        try
        {
            FileInputStream fis = new FileInputStream(fn);
            Scanner scanner = new Scanner(fis);
            while (scanner.hasNextLine())
            {
                contents += scanner.nextLine()+"\n";
            }
            fis.close();
        }catch(Exception e){}
        assemble(contents,ofn);
    }
    public boolean isAddressingMode(String[] operands,String mode) //Returns if operands are in the specified addressing mode
    {
        switch(mode)
        {
            case "impl ":
            return operands.length==0;
            case "A    ":
            return  operands.length==0 || (operands.length==1 && operands[0].equalsIgnoreCase("A"));
            case "#    ":
            return operands.length==1 && operands[0].startsWith("#");
            case "abs  ":
            return operands.length==1 && !operands[0].startsWith("#") && !operands[0].startsWith(".b") && !operands[0].startsWith("(") && !operands[0].endsWith(")");
            case "rel  ":
            return operands.length==1 && !operands[0].startsWith("#");
            case "zpg  ":
            return operands.length==1 && !operands[0].startsWith("#") && operands[0].startsWith(".b");
            case "ind  ":
            return operands.length==1 && operands[0].startsWith("(") && operands[0].endsWith(")");
            case "abs,X":
            return operands.length==2 && operands[1].equalsIgnoreCase("X") && !operands[0].startsWith(".b");
            case "abs,Y":
            return operands.length==2 && operands[1].equalsIgnoreCase("Y") && !operands[0].startsWith(".b");
            case "X,ind":
            return operands.length==2 && operands[0].startsWith("(") && operands[1].equalsIgnoreCase("X)");
            case "ind,Y":
            return operands.length==2 && operands[0].startsWith("(") && operands[0].endsWith(")") && operands[1].equalsIgnoreCase("Y");
            case "zpg,X":
            return operands.length==2 && operands[1].equalsIgnoreCase("X") && operands[0].startsWith(".b");
            case "zpg,Y":
            return operands.length==2 && operands[1].equalsIgnoreCase("Y") && operands[0].startsWith(".b");
            
        }
        return false;
    }
    public int getNumeric(String operand) //Read get numeric value from operand
    {
        if (operand.startsWith("("))
        {
            operand = operand.substring(1);
        }
        if (operand.endsWith(")"))
        {
            operand = operand.substring(0,operand.length()-1);
        }
        if (operand.startsWith("#"))
        {
            operand = operand.substring(1);
        }
        if (operand.startsWith(".b"))
        {
            operand = operand.substring(2);
        }
        else if (operand.startsWith(".w"))
        {
            operand = operand.substring(2);
        }
        
        
        if (operand.startsWith("$"))
        {
            return Integer.parseInt(operand.substring(1),16);
        }
        else if (operand.startsWith("%"))
        {
            return Integer.parseInt(operand.substring(1),2);
        }
        else if (operand.startsWith("0x"))
        {
            return Integer.parseInt(operand.substring(2),16);
        }
        else if (operand.startsWith("0o"))
        {
            return Integer.parseInt(operand.substring(2),8);
        }
        else if (operand.startsWith("0b"))
        {
            return Integer.parseInt(operand.substring(2),2);
        }
        else if (operand.startsWith("0") && operand.length()>1)
        {
            return Integer.parseInt(operand.substring(1),8);
        }
        else if (Character.isDigit(operand.charAt(0)))
        {
            return Integer.parseInt(operand);
        }
        else if (operand.startsWith("'") && operand.endsWith("'"))
        {
            return operand.charAt(1);
        }
        else if(labels.containsKey(operand))
        {
            return labels.get(operand);
        }
        else if(operand.contains("[") && operand.contains("]"))
        {
            int li = operand.indexOf("[");
            int ri = operand.indexOf("]");
            //System.out.println(operand);
            int val = labels.get(operand.substring(0,li));
            val += Integer.parseInt(operand.substring(li+1,ri));
            return val;
        }
        return -1;
    }
    public byte[] getMultipleNumeric(String operand) //Read all data as an array
    {
        boolean insideString=false;
        String result="";
        String data = "";
        for (int i=0;i<operand.length();i++)
        {
            char c = operand.charAt(i);
            if (c=='"')
            {
                insideString = !insideString;
            }
            else if(insideString)
            {
                result+=c;
            }
            else if(c==',')
            {
                if (data.length()>0)
                {
                    result+=(char)getNumeric(data);
                }
                data="";
            }
            else
            {
                data+=c;
            }
        }
        if (data.length()>0)
        {
            result+=(char)getNumeric(data);
        }
        return result.getBytes();
    }
    public int getAddressingModeSize(String mode) //Get number of bytes required by instruction
    {
        switch(mode)
        {
            case "impl ":
            return 1;
            case "A    ":
            return 1;
            case "#    ":
            return 2;
            case "abs  ":
            return 3;
            case "rel  ":
            return 2;
            case "zpg  ":
            return 2;
            case "ind  ":
            return 3;
            case "abs,X":
            return 3;
            case "abs,Y":
            return 3;
            case "X,ind":
            return 2;
            case "ind,Y":
            return 2;
            case "zpg,X":
            return 2;
            case "zpg,Y":
            return 2;
            
        }
        return 0;
    }
    public void insertInstruction(int opcode,String[] operands,String mode) //Insert the instruction into memory
    {
        machineCode[pointer] = (byte)opcode;
        pointer++;
        switch (mode)
        {
            case "#    ":
            case "X,ind":
            case "ind,Y":
            case "zpg  ":
            case "zpg,X":
            case "zpg,Y":
            machineCode[pointer] = (byte)getNumeric(operands[0]);
            pointer++;
            break;
            
            case "abs  ":
            case "abs,X":
            case "abs,Y":
            case "ind":
            int i = getNumeric(operands[0]);
            //System.out.println(i);
            machineCode[pointer] = (byte)i;
            pointer++;
            i>>=8; //Left shift 8 times
            machineCode[pointer] = (byte)i;
            pointer++;
            break;
            
            case "rel  ":
            int j = getNumeric(operands[0]);
            pointer++;
            machineCode[pointer-1] = (byte)(j - (pointer));
        }
    }
    public void assemble(String code,String ofn) //Assemble to memory
    {
        //System.out.println(code);
        String[] lines = code.split("\n");
        firstPass(lines);
        secondPass(lines);
        save(ofn);
    }
    public void firstPass(String[] lines) //Reads through the entire file and sets all the adressess for labels
    {
        for (int ln=0;ln<lines.length;ln++)
        {
            String line=lines[ln].trim().replace("\t"," ");
            System.out.println(Integer.toHexString(pointer)+" "+line);
            if (line.startsWith(";"))
            {
                
            }
            else if (line.startsWith("*"))
            {
                String line2 = line.replace(" ","");
                if (line2.startsWith("*="))
                {
                    pointer = getNumeric(line2.substring(2));
                }
                else
                {
                    byte[] data = getMultipleNumeric(line.substring(1));
                    pointer+=data.length;
                    //for (int i=0;i<data.length;pointer++,i++);
                }
            }
            else if(line.endsWith(":"))
            {
                String label = line.replace(" ","").substring(0,line.length()-1);
                labels.put(label,pointer);
            }
            else if (line.contains("="))
            {
                line = line.replace(" ","");
                int ind = line.indexOf("=");
                String label = line.substring(0,ind);
                int val = 0;
                if (line.charAt(ind+1)=='*')
                {
                    val = pointer;
                    //pointer++;
                }
                else
                {
                    val = getNumeric(line.substring(ind+1));
                }
                labels.put(label,val);
            }
            else if(line.equals(""))
            {
                
            }
            else
            {
                //line = line.replace("\t"," ");
                int ind = line.indexOf(" ");
                String name = line.toUpperCase();
                String operand = "";
                String[] operands = {};
                if (ind!=-1)
                {
                    name = line.substring(0,ind).toUpperCase();
                    operand = line.substring(ind+1);
                    operand = operand.replace(" ","");
                    operands = operand.split(",");
                }
                int opcode = -1;
                for (int i=0;i<opcode_names.length;i++)
                {
                    String opcodeName = opcode_names[i];
                    if (opcodeName.equals(name) && isAddressingMode(operands,opcode_mode[i]))
                    {
                        opcode = i;
                        break;
                    }
                }
                if (opcode==-1)
                {
                    System.out.println("Error in Line: "+line);
                }
                pointer+=getAddressingModeSize(opcode_mode[opcode]);
            }
        }
        
    }
    public void secondPass(String[] lines) //Reads through the entire file and writes instructions to memory
    {
        for (int ln=0;ln<lines.length;ln++)
        {
            String line=lines[ln].trim().replace("\t"," ");
            //System.out.println(Integer.toHexString(pointer)+" "+line);
            if (line.startsWith(";"))
            {
                
            }
            else if (line.startsWith("*"))
            {
                String line2 = line.replace(" ","");
                if (line2.startsWith("*="))
                {
                    pointer = getNumeric(line2.substring(2));
                    int i = pointer;
                    machineCode[0xfffc] = (byte)i;
                    i>>=8;
                    machineCode[0xfffd] = (byte)i;
                }
                else
                {
                    byte[] data = getMultipleNumeric(line.substring(1));
                    for (int i=0;i<data.length;pointer++,i++)
                    {
                        machineCode[pointer] = data[i];
                    }
                }
            }
            else if(line.endsWith(":") || line.contains("=") || line.equals(""))
            {
                
            }
            else
            {
                int ind = line.indexOf(" ");
                String name = line.toUpperCase();
                String operand = "";
                String[] operands = {};
                if (ind!=-1)
                {
                    name = line.substring(0,ind).toUpperCase();
                    operand = line.substring(ind+1);
                    operand = operand.replace(" ","");
                    operands = operand.split(",");
                }
                int opcode = -1;
                for (int i=0;i<opcode_names.length;i++)
                {
                    String opcodeName = opcode_names[i];
                    if (opcodeName.equals(name) && isAddressingMode(operands,opcode_mode[i]))
                    {
                        opcode = i;
                        break;
                    }
                }
                insertInstruction(opcode,operands,opcode_mode[opcode]);
                //pointer+=getAddressingModeSize(cpu.opcode_mode[opcode]);
            }
        }
    }
    public void save(String ofn) //Saves the machine code to file
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(ofn);
            fos.write(machineCode);
            fos.flush();
            fos.close();
        }catch(Exception e){}
    }
    public static void main(String[] args) 
    {
        Assembler asm = new Assembler();
        //asm.open("input.txt","input.6502");
        asm.open(args[0],args[1]);
    }
}
