package aditya.emu;
public abstract class CPU
{
    public Bus bus;
    public abstract void update();
    public abstract void reset();
}
