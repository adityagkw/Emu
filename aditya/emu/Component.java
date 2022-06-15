package aditya.emu;
public abstract class Component
{
    public Bus bus;
    public EmuSystem parent;
    public abstract void update();
}
