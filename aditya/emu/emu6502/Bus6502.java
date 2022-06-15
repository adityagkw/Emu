package aditya.emu.emu6502;
import aditya.emu.*;
public class Bus6502 extends Bus
{
    public volatile int address=0;//uint16
    public volatile byte data=0;
    public volatile boolean write=true;
    public volatile boolean readonly=false;
}
