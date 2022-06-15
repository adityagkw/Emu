package aditya.emu.emu6502;
import aditya.emu.*;
public class CPU6502 extends CPU
{
    public EmuSystem6502 parent;
    
    public int PC=0; //Program counter
    public byte AC=0; //Accumulator
    public byte X=0; //X register
    public byte Y=0; //Y register
    public byte SR=0; //Status register
    public byte SP=0; //Stack pointer
    
    public boolean carry_flag;
    public boolean zero_flag;
    public boolean interrupt_flag;
    public boolean decimal_mode_flag;
    public boolean break_flag;
    public boolean unused_flag;
    public boolean overflow_flag;
    public boolean negative_flag;
    
    public int opcode;
    public int cycles;
    public int arg;
    public int address;
    public boolean extra_cycle_address,extra_cycle_op;
    public String args="";
    
    public byte[] stack=new byte[0xff];
    
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
    
    public int[] opcode_cycles=new int[]{
    //0 1 2 3 4 5 6 7 8 9 A B C D E F
      7,6,0,0,0,3,5,0,3,2,2,0,0,4,6,0,//0
      2,5,0,0,0,4,6,0,2,4,0,0,0,4,7,0,//1
      6,6,0,0,3,3,5,0,4,2,2,0,4,4,6,0,//2
      2,5,0,0,0,4,6,0,2,4,0,0,0,4,7,0,//3
      6,6,0,0,0,3,5,0,3,2,2,0,3,4,6,0,//4
      2,5,0,0,0,4,6,0,2,4,0,0,0,4,7,0,//5
      6,6,0,0,0,3,5,0,4,2,2,0,5,4,6,0,//6
      2,5,0,0,0,4,6,0,2,4,0,0,0,4,7,0,//7
      0,6,0,0,3,3,3,0,2,0,2,0,4,4,4,0,//8
      2,6,0,0,4,4,4,0,2,5,2,0,0,5,0,0,//9
      2,6,2,0,3,3,3,0,2,2,2,0,4,4,4,0,//A
      2,5,0,0,4,4,4,0,2,4,2,0,4,4,4,0,//B
      2,6,0,0,3,3,5,0,2,2,2,0,4,4,3,0,//C
      2,5,0,0,0,4,6,0,2,4,0,0,0,4,7,0,//D
      2,6,0,0,3,3,5,0,2,2,2,0,4,4,6,0,//E
      2,5,0,0,0,4,6,0,2,4,0,0,0,4,7,0//F
    };
    
    public void update()
    {
        if (cycles<=0)
        {
            //System.out.println("Update");
            getStatus();
            Bus6502 b=(Bus6502)bus;
            
            opcode=Byte.toUnsignedInt(readInstruction(PC));
            //System.out.println(opcode);
            //opcode=((opcode&0x0f)<<8)+((opcode&0xf0)>>8);
            //System.out.print(Integer.toHexString(PC)+" : "+opcode_names[opcode]+" ");
            cycles=opcode_cycles[opcode];
            PC++;
            unused_flag=true;
            setStatus();
            addressMode(opcode_mode[opcode]);
            //System.out.println(args);
            runOpcode();
            /*if (extra_cycle_address)
                cycles++;
            if (extra_cycle_op)
                cycles++;*/
            if (extra_cycle_address && extra_cycle_op)
                cycles++;
            PC&=0xffff;
            AC&=0xff;
            X&=0xff;
            Y&=0xff;
            //System.out.println(PC+" "+AC+" "+X+" "+Y);
            setStatus();
        }
        cycles--;
    }
    
    public void reset()
    {
        AC=0;
        X=0;
        Y=0;
        SP=(byte)0xFF;//FD
        carry_flag=false;
        zero_flag=false;
        interrupt_flag=false;
        decimal_mode_flag=false;
        break_flag=false;
        //unused_flag=true;//
        overflow_flag=false;
        negative_flag=false;
        address=0xFFFC;
        int lb=Byte.toUnsignedInt(readInstruction(address));
        int hb=Byte.toUnsignedInt(readInstruction(address+1));
        PC=(hb<<8)+lb;
        System.out.println("PC: "+Integer.toHexString(PC));
        address=0;
        arg=0;
        cycles=8;
        setStatus();
    }
    
    public void interrupt()
    {
        getStatus();
        if (!interrupt_flag)
        {
            //System.out.println("Interrupt");
            writeInstruction(0x0100+Byte.toUnsignedInt(SP),(byte)((PC>>8)&0xff));
            SP--;
            writeInstruction(0x0100+Byte.toUnsignedInt(SP),(byte)(PC&0xff));
            SP--;
            break_flag=false;
            unused_flag=true;
            interrupt_flag=true;
            setStatus();
            writeInstruction(0x0100+Byte.toUnsignedInt(SP),(byte)(SR&0xff));
            SP--;
            address=0xFFFE;
            int lb=Byte.toUnsignedInt(readInstruction(address));
            int hb=Byte.toUnsignedInt(readInstruction(address+1));
            PC=(hb<<8)+lb;
            cycles=7;
            setStatus();
        }
    }
    
    public void unmaskableInterrupt()
    {
        //System.out.println("Unmaskable Interrupt");
        writeInstruction(0x0100+Byte.toUnsignedInt(SP),(byte)((PC>>8)&0xff));
        SP--;
        writeInstruction(0x0100+Byte.toUnsignedInt(SP),(byte)(PC&0xff));
        SP--;
        break_flag=false;
        unused_flag=true;
        interrupt_flag=true;
        setStatus();
        writeInstruction(0x0100+Byte.toUnsignedInt(SP),(byte)(SR&0xff));
        SP--;
        address=0xFFFA;
        int lb=Byte.toUnsignedInt(readInstruction(address));
        int hb=Byte.toUnsignedInt(readInstruction(address+1));
        PC=(hb<<8)|lb;
        cycles=8;
        setStatus();
    }
    
    public void returnFromInterrupt()
    {
        SP++;
        SR=readInstruction(0x0100+Byte.toUnsignedInt(SP));
        getStatus();
        break_flag=false;
        unused_flag=false;
        SP++;
        PC=Byte.toUnsignedInt(readInstruction(0x0100+Byte.toUnsignedInt(SP)));
        SP++;
        PC+=Byte.toUnsignedInt(readInstruction(0x0100+Byte.toUnsignedInt(SP)))<<8;
        setStatus();
    }
    
    public void addressMode(String mode)
    {
        arg=0;
        address=0xffffffff;
        extra_cycle_address=false;
        if (mode.equals("A    "))     //Accumulator
        {
            arg=Byte.toUnsignedInt(AC);
            args="$"+Integer.toHexString(arg)+" [A]";
        }
        else if (mode.equals("abs  "))//Absolute
        {
            int lb=Byte.toUnsignedInt(readInstruction(PC));
            PC++;
            int hb=Byte.toUnsignedInt(readInstruction(PC));
            PC++;
            address=(hb<<8)|lb;
            args="[$"+Integer.toHexString(address)+"] [ABS]";
        }
        else if (mode.equals("abs,X"))//Absolute, X-indexed
        {
            int lb=Byte.toUnsignedInt(readInstruction(PC));
            PC++;
            int hb=Byte.toUnsignedInt(readInstruction(PC));
            PC++;
            address=(hb<<8)+lb+Byte.toUnsignedInt(X);
            if ((address&0xff00)!=(hb<<8))
                extra_cycle_address=true;
            args="[$"+Integer.toHexString(address)+"] ["+Integer.toHexString((hb<<8)+lb)+"],X";
        }
        else if (mode.equals("abs,Y"))//Absolute, Y-indexed
        {
            int lb=Byte.toUnsignedInt(readInstruction(PC));
            PC++;
            int hb=Byte.toUnsignedInt(readInstruction(PC));
            PC++;
            address=(hb<<8)+lb+Byte.toUnsignedInt(Y);
            if ((address&0xff00)!=(hb<<8))
                extra_cycle_address=true;;
            args="[$"+Integer.toHexString(address)+"] ["+Integer.toHexString((hb<<8)+lb)+"],Y";
        }
        else if (mode.equals("#    "))//Immediate
        {
            address=PC;
            //arg=Byte.toUnsignedInt(readInstruction(PC));
            PC++;
            args="$["+Integer.toHexString(address)+"] [#]";
        }
        else if (mode.equals("impl "))//Implied
        {
            arg=Byte.toUnsignedInt(AC);
            args="A: "+Byte.toUnsignedInt(AC)+" [IMPL]";
        }
        else if (mode.equals("ind  "))//Indirect
        {
            int lb1=Byte.toUnsignedInt(readInstruction(PC));
            PC++;
            int hb1=Byte.toUnsignedInt(readInstruction(PC));
            PC++;
            
            int ptr=(hb1<<8)+lb1;
            if (lb1==0xff)//Bug
            {
                int lb2=Byte.toUnsignedInt(readInstruction(ptr));
                int hb2=Byte.toUnsignedInt(readInstruction(ptr&0xff00));
                address=(hb2<<8)+lb2;
            }
            else
            {
                int lb2=Byte.toUnsignedInt(readInstruction(ptr));
                int hb2=Byte.toUnsignedInt(readInstruction(ptr+1));
                address=(hb2<<8)+lb2;
            }
            args="$["+Integer.toHexString(address)+"] "+" [$"+Integer.toHexString((hb1<<8)+lb1)+"] [IND]";
        }
        else if (mode.equals("X,ind"))//X-indexed, indirect
        {
            int lb1=Byte.toUnsignedInt(readInstruction(PC));
            PC++;
            int ptr=lb1+Byte.toUnsignedInt(X);
            ptr&=0xff;
            int lb=Byte.toUnsignedInt(readInstruction(ptr&0xff));
            int hb=Byte.toUnsignedInt(readInstruction((ptr+1)&0xff));
            address=(hb<<8)|lb;
            args="$["+Integer.toHexString(address)+"]"+" X,[$"+Integer.toHexString(lb1)+"]";
        }
        else if (mode.equals("ind,Y"))//Indirect, Y-indexed
        {
            int ptr=Byte.toUnsignedInt(readInstruction(PC));
            PC++;
            int lb=Byte.toUnsignedInt(readInstruction(ptr&0xff));
            int hb=Byte.toUnsignedInt(readInstruction((ptr+1)&0xff));
            address=(hb<<8)|lb+Byte.toUnsignedInt(Y);
            if ((address&0xff00)!=(hb<<8))
                extra_cycle_address=true;
            args="$["+Integer.toHexString(address)+"]"+" [$"+Integer.toHexString(ptr)+"],Y";
        }
        else if (mode.equals("rel  "))//Relative
        {
            //short offset=(short)Byte.toUnsignedInt(readInstruction(PC));
            byte offset=readInstruction(PC);
            PC++;
            //if ((offset & 0x80)==1)
                //offset|=0xff00;
            address=PC+offset;
            //arg=readInstruction((short)(PC+offset));
            args="$["+Integer.toHexString(address)+"]"+" ["+((byte)offset)+"] [REL]";
        }
        else if (mode.equals("zpg  "))//Zeropage
        {
            address=Byte.toUnsignedInt(readInstruction(PC));
            PC++;
            address&=0xff;
            //arg=readInstruction(loc);
            args="$["+Integer.toHexString(address)+"] [ZPG]";
        }
        else if (mode.equals("zpg,X"))//Zeropage, X-indexed
        {
            address=Byte.toUnsignedInt(readInstruction(PC))+Byte.toUnsignedInt(X);
            PC++;
            address&=0xff;
            args="$["+Integer.toHexString(address)+"] [ZPG,X]";
        }
        else if (mode.equals("zpg,Y"))//Zeropage, Y-indexed
        {
            address=Byte.toUnsignedInt(readInstruction(PC))+Byte.toUnsignedInt(Y);
            PC++;
            address&=0xff;
            args="$["+Integer.toHexString(address)+"] [ZPG,Y]";
        }
        if (address!=0xffffffff)
            arg=Byte.toUnsignedInt(readInstruction(address));
        //System.out.println(Integer.toHexString(arg));
    }
    public void runOpcode()
    {
        String op=opcode_names[opcode];
        extra_cycle_op=false;
        if (op.equals("NOP"))
        {
            if (opcode==0xfc)
            {
                extra_cycle_op=true;
            }
        }
        else if(op.equals("ADC"))
        {
            int t=Byte.toUnsignedInt(AC) + arg + (carry_flag?1:0);
            overflow_flag=((~(Byte.toUnsignedInt(AC)^Byte.toUnsignedInt((byte)arg))&(Byte.toUnsignedInt(AC)^Byte.toUnsignedInt((byte)t)))&0x0080)==128;
            AC=(byte)t;
            negative_flag=AC<0;
            zero_flag=Byte.toUnsignedInt(AC)==0;
            carry_flag=(t>>8)==1;
            extra_cycle_op=true;
        }
        else if(op.equals("AND"))
        {
            AC=(byte)(AC & arg);
            negative_flag=AC<0;
            zero_flag=Byte.toUnsignedInt(AC)==0;
            extra_cycle_op=true;
        }
        else if(op.equals("ASL"))
        {
            int t=arg<<1;
            if (opcode_mode[opcode].equals("imp  "))
            {
              AC=(byte)t;
            }
            else
            {
                writeInstruction(address,(byte)(t));
            }
            negative_flag=(byte)t<0;
            zero_flag=Byte.toUnsignedInt((byte)t)==0;
            carry_flag=(t>>8)==1;
        }
        else if(op.equals("BCC"))
        {
            if (!carry_flag)
            {
                cycles++;
                if ((address&0xff00)!=(PC&0xff00))
                    cycles++;
                PC=address;
            }
        }
        else if(op.equals("BCS"))
        {
            if (carry_flag)
            {
                cycles++;
                if ((address&0xff00)!=(PC&0xff00))
                    cycles++;
                PC=address;
            }
        }
        else if(op.equals("BEQ"))
        {
            if (zero_flag)
            {
                cycles++;
                if ((address&0xff00)!=(PC&0xff00))
                    cycles++;
                PC=address;
            }
        }
        else if(op.equals("BIT"))
        {
            negative_flag=((arg>>7)&1)==1;
            overflow_flag=((arg>>6)&1)==1;
            byte t=(byte)(AC&arg);
            zero_flag=Byte.toUnsignedInt(t)==0;
        }
        else if(op.equals("BMI"))
        {
            if (negative_flag)
            {
                cycles++;
                if ((address&0xff00)!=(PC&0xff00))
                    cycles++;
                PC=address;
            }
        }
        else if(op.equals("BNE"))
        {
            if (!zero_flag)
            {
                cycles++;
                if ((address&0xff00)!=(PC&0xff00))
                    cycles++;
                PC=address;
            }
        }
        else if(op.equals("BPL"))
        {
            if (!negative_flag)
            {
                cycles++;
                if ((address&0xff00)!=(PC&0xff00))
                    cycles++;
                PC=address;
            }
        }
        else if(op.equals("BRK"))
        {
            //interrupt_flag=true;
            PC++;
            //unmaskableInterrupt();
            writeInstruction(0x0100+Byte.toUnsignedInt(SP),(byte)((PC>>8)&0xff));
            SP--;
            writeInstruction(0x0100+Byte.toUnsignedInt(SP),(byte)(PC&0xff));
            SP--;
            break_flag=true;
            interrupt_flag=true;
            setStatus();
            writeInstruction(0x0100+Byte.toUnsignedInt(SP),(byte)(SR&0xff));
            SP--;
            break_flag=false;
            address=0xFFFE;
            int lb=Byte.toUnsignedInt(readInstruction(address));
            int hb=Byte.toUnsignedInt(readInstruction(address+1));
            PC=(hb<<8)+lb;
        }
        else if(op.equals("BVC"))
        {
            if (!overflow_flag)
            {
                cycles++;
                if ((address&0xff00)!=(PC&0xff00))
                    cycles++;
                PC=address;
            }
        }
        else if(op.equals("BVS"))
        {
            if (overflow_flag)
            {
                cycles++;
                if ((address&0xff00)!=(PC&0xff00))
                    cycles++;
                PC=address;
            }
        }
        else if(op.equals("CLC"))
        {
            carry_flag=false;
        }
        else if(op.equals("CLD"))
        {
            decimal_mode_flag=false;
        }
        else if(op.equals("CLI"))
        {
            interrupt_flag=false;
        }
        else if(op.equals("CLV"))
        {
            overflow_flag=false;
        }
        else if(op.equals("CMP"))
        {
            int t=Byte.toUnsignedInt(AC) - Byte.toUnsignedInt((byte)arg);
            byte t2=(byte)t;
            negative_flag=t2<0;
            zero_flag=Byte.toUnsignedInt(t2)==0;
            carry_flag=Byte.toUnsignedInt(AC)>=Byte.toUnsignedInt((byte)arg);
            extra_cycle_op=true;
        }
        else if(op.equals("CPX"))
        {
            int t=Byte.toUnsignedInt(X) - Byte.toUnsignedInt((byte)arg);
            byte t2=(byte)t;
            negative_flag=t2<0;
            zero_flag=Byte.toUnsignedInt(t2)==0;
            carry_flag=Byte.toUnsignedInt(X)>=Byte.toUnsignedInt((byte)arg);
        }
        else if(op.equals("CPY"))
        {
            int t=Byte.toUnsignedInt(Y) - Byte.toUnsignedInt((byte)arg);
            byte t2=(byte)t;
            negative_flag=t2<0;
            zero_flag=Byte.toUnsignedInt(t2)==0;
            carry_flag=Byte.toUnsignedInt(Y)>=Byte.toUnsignedInt((byte)arg);
        }
        else if(op.equals("DEC"))
        {
            int temp=Byte.toUnsignedInt((byte)(arg-1));
            writeInstruction(address,(byte)(temp&0x00ff));
            zero_flag=(temp&0xff)==0;
            negative_flag=((byte)temp)<0;
        }
        else if(op.equals("DEX"))
        {
            X--;
            zero_flag=X==0;
            negative_flag=X<0;
        }
        else if(op.equals("DEY"))
        {
            Y--;
            zero_flag=Y==0;
            negative_flag=Y<0;
        }
        else if(op.equals("EOR"))
        {
            AC=(byte)(AC ^ arg);
            negative_flag=AC<0;
            zero_flag=Byte.toUnsignedInt(AC)==0;
            extra_cycle_op=true;
        }
        else if(op.equals("INC"))
        {
            int temp=Byte.toUnsignedInt((byte)(arg+1));
            writeInstruction(address,(byte)(temp&0x00ff));
            zero_flag=(temp&0xff)==0;
            negative_flag=((byte)temp)<0;
        }
        else if(op.equals("INX"))
        {
            X++;
            zero_flag=X==0;
            negative_flag=X<0;
        }
        else if(op.equals("INY"))
        {
            Y++;
            zero_flag=Y==0;
            negative_flag=Y<0;
        }
        else if(op.equals("JMP"))
        {
            PC=address;
        }
        else if(op.equals("JSR"))
        {
            PC--;
            writeInstruction(0x0100+Byte.toUnsignedInt(SP),(byte)((PC>>8)&0xff));
            SP--;
            writeInstruction(0x0100+Byte.toUnsignedInt(SP),(byte)(PC&0xff));
            SP--;
            PC=address;
        }
        else if(op.equals("LDA"))
        {
            AC=(byte)arg;
            negative_flag=AC<0;
            zero_flag=Byte.toUnsignedInt(AC)==0;
            extra_cycle_op=true;
        }
        else if(op.equals("LDX"))
        {
            X=(byte)arg;
            negative_flag=X<0;
            zero_flag=Byte.toUnsignedInt(X)==0;
            extra_cycle_op=true;
        }
        else if(op.equals("LDY"))
        {
            Y=(byte)arg;
            negative_flag=Y<0;
            zero_flag=Byte.toUnsignedInt(Y)==0;
            extra_cycle_op=true;
        }
        else if(op.equals("LSR"))
        {
            int t=arg>>1;
            if (opcode_mode[opcode].equals("imp  "))
            {
              AC=(byte)t;
            }
            else
            {
                writeInstruction(address,(byte)(t));
            }
            negative_flag=((byte)t)<0;
            zero_flag=Byte.toUnsignedInt((byte)t)==0;
            carry_flag=(arg&0x1)==1;
        }
        else if(op.equals("ORA"))
        {
            AC=(byte)(AC | arg);
            negative_flag=AC<0;
            zero_flag=Byte.toUnsignedInt(AC)==0;
            extra_cycle_op=true;
        }
        else if(op.equals("PHA"))
        {
            writeInstruction(0x0100+Byte.toUnsignedInt(SP),AC);
            SP--;
        }
        else if(op.equals("PHP"))
        {
            writeInstruction(0x0100+Byte.toUnsignedInt(SP),SR);
            SP--;
            break_flag=false;
            unused_flag=false;
        }
        else if(op.equals("PLA"))
        {
            SP++;
            AC=readInstruction(0x100+Byte.toUnsignedInt(SP));
            zero_flag=Byte.toUnsignedInt(AC)==0;
            negative_flag=AC<0;
        }
        else if(op.equals("PLP"))
        {
            SP++;
            SR=readInstruction(0x100+Byte.toUnsignedInt(SP));
            unused_flag=true;
            getStatus();
        }
        else if(op.equals("ROL"))
        {
            int t=arg<<1;
            t+=(carry_flag)?1:0;
            if (opcode_mode[opcode].equals("A    "))
            {
                AC=(byte)t;
            }
            else
            {
                writeInstruction(address,(byte)(t));
            }
            negative_flag=(byte)t<0;
            zero_flag=Byte.toUnsignedInt((byte)t)==0;
            carry_flag=(t>>8)==1;
        }
        else if(op.equals("ROR"))
        {
            int t=arg>>1;
            t+=(carry_flag?1:0)<<7;
            if (opcode_mode[opcode].equals("A    "))
            {
                AC=(byte)t;
            }
            else
            {
                writeInstruction(address,(byte)(t));
            }
            negative_flag=(byte)t<0;
            zero_flag=Byte.toUnsignedInt((byte)t)==0;
            carry_flag=(arg%2)==1;
        }
        else if(op.equals("RTI"))
        {
            returnFromInterrupt();
        }
        else if(op.equals("RTS"))
        {
            SP++;
            PC=Byte.toUnsignedInt(readInstruction(0x0100+Byte.toUnsignedInt(SP)));
            SP++;
            PC+=Byte.toUnsignedInt(readInstruction(0x0100+Byte.toUnsignedInt(SP)))<<8;
            PC++;
        }
        else if(op.equals("SBC"))
        {
            arg=arg^0xff;
            int t=Byte.toUnsignedInt(AC) + arg + (carry_flag?1:0);
            //overflow_flag=((~(Byte.toUnsignedInt(AC)^Byte.toUnsignedInt((byte)arg))&(Byte.toUnsignedInt(AC)^Byte.toUnsignedInt((byte)t)))&0x0080)==128;
            overflow_flag=(((Byte.toUnsignedInt(AC)^Byte.toUnsignedInt((byte)arg))&(arg^Byte.toUnsignedInt((byte)t)))&0x0080)==128;
            AC=(byte)t;
            negative_flag=AC<0;
            zero_flag=Byte.toUnsignedInt(AC)==0;
            carry_flag=(t>>8)==1;
            extra_cycle_op=true;
        }
        else if(op.equals("SEC"))
        {
            carry_flag=true;
        }
        else if(op.equals("SED"))
        {
            decimal_mode_flag=true;
        }
        else if(op.equals("SEI"))
        {
            interrupt_flag=true;
        }
        else if(op.equals("STA"))
        {
            writeInstruction(address,(byte)AC);
        }
        else if(op.equals("STX"))
        {
            writeInstruction(address,(byte)X);
        }
        else if(op.equals("STY"))
        {
            writeInstruction(address,(byte)Y);
        }
        else if(op.equals("TAX"))
        {
            X=AC;
            zero_flag=Byte.toUnsignedInt(X)==0;
            negative_flag=X<0;
        }
        else if(op.equals("TAY"))
        {
            Y=AC;
            zero_flag=Byte.toUnsignedInt(Y)==0;
            negative_flag=Y<0;
        }
        else if(op.equals("TSX"))
        {
            SP++;
            X=readInstruction(0x100+Byte.toUnsignedInt(SP));
            zero_flag=Byte.toUnsignedInt(X)==0;
            negative_flag=X<0;
        }
        else if(op.equals("TXA"))
        {
            AC=X;
            zero_flag=Byte.toUnsignedInt(AC)==0;
            negative_flag=AC<0;
        }
        else if(op.equals("TXS"))
        {
            writeInstruction(0x0100+Byte.toUnsignedInt(SP),X);
            SP--;
        }
        else if(op.equals("TYA"))
        {
            AC=Y;
            zero_flag=Byte.toUnsignedInt(AC)==0;
            negative_flag=AC<0;
        }
        else
        {
            
        }
        setStatus();
    }
    
    public byte readInstruction(int address)
    {
        address&=0xffff;
        Bus6502 b=(Bus6502)bus;
        b.data=0;
        b.address=address;
        b.write=true;
        parent.updateComponents();
        //System.out.println(b.data);
        //b.write=true;
        b.address=0;
        return b.data;
    }
    public void writeInstruction(int address,byte data)
    {
        address&=0xffff;
        Bus6502 b=(Bus6502)bus;
        b.data=data;
        b.address=address;
        b.write=false;
        parent.updateComponents();
        b.address=0;
        b.write=true;
    }
    public void setStatus()
    {
        SR=0;
        if (carry_flag){SR+=1<<0;}
        if (zero_flag){SR+=1<<1;}
        if (interrupt_flag){SR+=1<<2;}
        if (decimal_mode_flag){SR+=1<<3;}
        if (break_flag){SR+=1<<4;}
        if (unused_flag){SR+=1<<5;}
        if (overflow_flag){SR+=1<<6;}
        if (negative_flag){SR+=1<<7;}
    }
    public void getStatus()
    {
        carry_flag=(SR>>0 & 0b1)==1;
        zero_flag=(SR>>1 & 0b1)==1;
        interrupt_flag=(SR>>2 & 0b1)==1;
        decimal_mode_flag=(SR>>3 & 0b1)==1;
        break_flag=(SR>>4 & 0b1)==1;
        unused_flag=(SR>>5 & 0b1)==1;
        overflow_flag=(SR>>6 & 0b1)==1;
        negative_flag=(SR>>7 & 0b1)==1;
    }
}
