package aditya.emu.emu6502;
import aditya.emu.*;
import org.lwjgl.input.*;
public class Keyboard6502 extends Component
{
    byte[] k=new byte[32];
    byte flags = 0;
    String buffer="";
    public void reset()
    {
        flags = 0;
        buffer = "";
    }
    public void update()
    {
        Bus6502 b=(Bus6502)bus;
        Keyboard.poll();
        getKey();
        if (b.address>=0x0200 && b.address<0x0200+k.length)
        {
            int a=b.address-0x0200;
            if (!b.write)
            {
                k[a]=b.data;
            }
            else
            {
                b.data=k[a];
            }
        }
        else if(b.address==0x0240)
        {
            if (!b.write)
            {
                buffer+=b.data;
            }
            else
            {
                if (buffer.length()>0)
                {
                    b.data = (byte)buffer.charAt(0);
                    buffer = buffer.substring(1);
                }
                else
                {
                    b.data = 0;
                }
            }
        }
        else if(b.address==0x0241)
        {
            if (!b.write)
            {
                int length = Byte.toUnsignedInt(b.data);
                int len = buffer.length();
                if (length<len)
                {
                    buffer.substring(len-length);
                }
                else if(length>len)
                {
                    
                }
            }
            else
            {
                b.data = (byte)buffer.length();
            }
        }
        else if(b.address==0x0242)
        {
            if (!b.write)
            {
                flags = b.data;
            }
            else
            {
                b.data = flags;
                //flags = (byte)(flags&(~1));
            }
        }
    }
    private void setBit(int i,boolean b)
    {
        int j=i%8;
        i=i/8;
        k[i]=(byte)(k[i]|(1<<j));
        if (!b)
        {
            //k[i]=(byte)(k[i]-(1<<j));
            k[i]=(byte)(k[i]&(~(1<<j)));
        }
    }
    private void getKey()
    {
        setBit('A',Keyboard.isKeyDown(Keyboard.KEY_A));
        setBit('B',Keyboard.isKeyDown(Keyboard.KEY_B));
        setBit('C',Keyboard.isKeyDown(Keyboard.KEY_C));
        setBit('D',Keyboard.isKeyDown(Keyboard.KEY_D));
        setBit('E',Keyboard.isKeyDown(Keyboard.KEY_E));
        setBit('F',Keyboard.isKeyDown(Keyboard.KEY_F));
        setBit('G',Keyboard.isKeyDown(Keyboard.KEY_G));
        setBit('H',Keyboard.isKeyDown(Keyboard.KEY_H));
        setBit('I',Keyboard.isKeyDown(Keyboard.KEY_I));
        setBit('J',Keyboard.isKeyDown(Keyboard.KEY_J));
        setBit('K',Keyboard.isKeyDown(Keyboard.KEY_K));
        setBit('L',Keyboard.isKeyDown(Keyboard.KEY_L));
        setBit('M',Keyboard.isKeyDown(Keyboard.KEY_M));
        setBit('N',Keyboard.isKeyDown(Keyboard.KEY_N));
        setBit('O',Keyboard.isKeyDown(Keyboard.KEY_O));
        setBit('P',Keyboard.isKeyDown(Keyboard.KEY_P));
        setBit('Q',Keyboard.isKeyDown(Keyboard.KEY_Q));
        setBit('R',Keyboard.isKeyDown(Keyboard.KEY_R));
        setBit('S',Keyboard.isKeyDown(Keyboard.KEY_S));
        setBit('T',Keyboard.isKeyDown(Keyboard.KEY_T));
        setBit('U',Keyboard.isKeyDown(Keyboard.KEY_U));
        setBit('V',Keyboard.isKeyDown(Keyboard.KEY_V));
        setBit('W',Keyboard.isKeyDown(Keyboard.KEY_W));
        setBit('X',Keyboard.isKeyDown(Keyboard.KEY_X));
        setBit('Y',Keyboard.isKeyDown(Keyboard.KEY_Y));
        setBit('Z',Keyboard.isKeyDown(Keyboard.KEY_Z));
        setBit('0',Keyboard.isKeyDown(Keyboard.KEY_0));
        setBit('1',Keyboard.isKeyDown(Keyboard.KEY_1));
        setBit('2',Keyboard.isKeyDown(Keyboard.KEY_2));
        setBit('3',Keyboard.isKeyDown(Keyboard.KEY_3));
        setBit('4',Keyboard.isKeyDown(Keyboard.KEY_4));
        setBit('5',Keyboard.isKeyDown(Keyboard.KEY_5));
        setBit('6',Keyboard.isKeyDown(Keyboard.KEY_6));
        setBit('7',Keyboard.isKeyDown(Keyboard.KEY_7));
        setBit('8',Keyboard.isKeyDown(Keyboard.KEY_8));
        setBit('9',Keyboard.isKeyDown(Keyboard.KEY_9));
        boolean kn = Keyboard.next();
        int k = Keyboard.getEventKey();
        if (k!=0 && kn)
        {
            char kc = Keyboard.getEventCharacter();
            if(Keyboard.getEventKeyState() && (flags&128)!=0)
            {
                //System.out.println("Key: "+kc+":"+(int)kc);
                if (kc!=0)
                {
                    if (kc==8)
                    {
                        if (buffer.length()>0)
                        {
                            buffer = buffer.substring(0,buffer.length()-1);
                        }
                        else
                        {
                            buffer += (char)8;
                        }
                    }
                    else
                    {
                        buffer+=kc;
                        if (kc==10 || kc==13)
                        {
                            flags = (byte)(flags|1);
                        }
                    }
                    //if ((flags&2) != 0)
                        //System.out.println("Input: "+buffer);
                }
            }
        }
    }
    /*
    A 8 1
    B 8 2
    C 8 3
    D 8 4
    E 8 5
    F 8 6
    G 8 7
    H 9 0
    I 9 1
    J 9 2
    K 9 3
    L 9 4
    M 9 5
    N 9 6
    O 9 7
    P 10 0
    Q 10 1
    R 10 2
    S 10 3
    T 10 4
    U 10 5
    V 10 6
    W 10 7
    X 11 0
    Y 11 1
    Z 11 2
     */
}
