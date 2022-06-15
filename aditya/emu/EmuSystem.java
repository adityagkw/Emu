package aditya.emu;
import java.util.*;
public abstract class EmuSystem
{
    public long clock_speed;
    public CPU cpu;
    public Bus bus;
    public ArrayList<Component> components=new ArrayList<Component>();
    public abstract void update();
}
