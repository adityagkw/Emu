package aditya.emu.emu6502;
import aditya.emu.*;
import org.lwjgl.input.*;
public class Mouse6502 extends Component
{
    byte dx=0,dy=0,dw=0;
    byte mb=0;
    public void update()
    {
        Bus6502 b=(Bus6502)bus;
        Mouse.poll();
        setBit(0,Mouse.isButtonDown(0));
        setBit(1,Mouse.isButtonDown(1));
        setBit(2,Mouse.isButtonDown(2));
        setBit(3,Mouse.hasWheel());
        dx=(byte)Mouse.getDX();
        dy=(byte)Mouse.getDY();
        if (Mouse.hasWheel())
        {
            dw=(byte)Mouse.getDWheel();
        }
        if (b.address==0x0220)
        {
            if (!b.write)
            {
                dx=b.data;
            }
            else
            {
                b.data=dx;
            }
        }
        else if (b.address==0x0221)
        {
            if (!b.write)
            {
                dy=b.data;
            }
            else
            {
                b.data=dy;
            }
        }
        else if (b.address==0x0222)
        {
            if (!b.write)
            {
                dw=b.data;
            }
            else
            {
                b.data=dw;
            }
        }
        else if (b.address==0x0223)
        {
            if (!b.write)
            {
                mb=b.data;
            }
            else
            {
                b.data=mb;
            }
        }
        else if (b.address==0x0224)
        {
            if (!b.write)
            {
                
            }
            else
            {
                b.data=(byte)Math.round(Math.random()*255);
            }
        }
    }
    private void setBit(int i,boolean b)
    {
        mb=(byte)(mb|(1<<i));
        if (!b)
        {
            //mb=(byte)(mb-(1<<i));
            mb=(byte)(mb&(~(1<<i)));
        }
    }
}
