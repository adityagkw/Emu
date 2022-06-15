package aditya.emu.emu6502;
import aditya.emu.*;
public class EmuSystem6502 extends EmuSystem
{
    public EmuSystem6502()
    {
        cpu=new CPU6502();
        bus=new Bus6502();
        cpu.bus=bus;
        ((CPU6502)cpu).parent=this;
    }
    public void update()
    {
        CPU6502 c=(CPU6502)cpu;
        c.parent=this;
        c.update();
    }
    public void updateComponents()
    {
        for (Component c:components)
        {
            c.bus=bus;
            c.update();
        }
        ((Bus6502)bus).write=true;
    }
    public void reset()
    {
        cpu.reset();
    }
    public static void main(String[] args)
    {
        EmuSystem6502 sys=new EmuSystem6502();
        RAM6502 ram=new RAM6502();
        sys.components.add(ram);
        //ram.storeFile("test.txt",0);
        //ram.storeFileHex("test2.txt",0);
        ram.storeFileBin("asm test.6502",0);
        sys.reset();
        CPU6502 c=(CPU6502)sys.cpu;
        while(true)
        {
            System.out.print("\u000c");
            c.update();
            String A=Integer.toHexString(Byte.toUnsignedInt(c.AC)).toUpperCase();
            String X=Integer.toHexString(Byte.toUnsignedInt(c.X)).toUpperCase();
            String Y=Integer.toHexString(Byte.toUnsignedInt(c.Y)).toUpperCase();
            String PC=Integer.toHexString(c.PC).toUpperCase();
            String SP=Integer.toHexString(Byte.toUnsignedInt(c.SP)).toUpperCase();
            String SR=Integer.toBinaryString(Byte.toUnsignedInt(c.SR));
            if (A.length()==1) A="0"+A;
            if (X.length()==1) X="0"+X;
            if (Y.length()==1) Y="0"+Y;
            for (int i=PC.length();i<4;i++){PC="0"+PC;}
            if (SP.length()==1) SP="0"+SP;
            for (int i=SR.length();i<8;i++){SR="0"+SR;}
            
            System.out.println("A: \t"+A);
            System.out.println("X: \t"+X);
            System.out.println("Y: \t"+Y);
            System.out.println("PC: \t"+PC);
            System.out.println("SP: \t"+SP);
            System.out.println("Flags:\tNV-BDIZC");
            System.out.println("SR: \t"+SR);
            System.out.print("\nOpcode: "+Integer.toHexString(c.opcode).toUpperCase()+"\t");
            System.out.print(c.opcode_names[c.opcode]+"\t");
            System.out.println(c.opcode_mode[c.opcode]);
            
            try
            {
                Thread.sleep(100*c.cycles);
            }catch(Exception e){}
        }
    }
}
