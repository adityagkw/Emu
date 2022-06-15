package aditya.emu.emu6502;
import aditya.emu.*;
import aditya.emu.emunes.*;
import org.lwjgl.opengl.*;
import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.*;
public class Monitor6502 extends Component
{
    int[][] p=new int[256][256];
    byte[][] pb=new byte[256][256];
    char [][] text = new char[16][16];
    byte[][] tcb = new byte[16][16];
    int[][] tc = new int[16][16];
    byte[][] tbb = new byte[16][16];
    int[][] tb = new int[16][16];
    byte text_settings = 0;
    byte ctb=0xd,ctc=0x30;
    int x=0,y=0,tx=0,ty=0;
    public int[][] color=new int[][]{
       //      0             1             2             3             4             5             6             7             8             9             A             B             C             D             E             F     
         { 84, 84, 84},{  0, 30,116},{  8, 16,144},{ 48,  0,136},{ 68,  0,100},{ 92,  0, 48},{ 84,  4,  0},{ 60, 24,  0},{ 32, 42,  0},{  8, 58,  0},{  0, 64,  0},{  0, 60,  0},{  0, 50, 60},{  0,  0,  0},{  0,  0,  0},{  0,  0,  0},// A
         {152,150,152},{  8, 76,196},{ 48, 50,236},{ 92, 30,228},{136, 20,176},{160, 20,100},{152, 34, 32},{120, 60,  0},{ 84, 90,  0},{ 40,114,  0},{  8,124,  0},{  0,118, 40},{  0,102,120},{  0,  0,  0},{  0,  0,  0},{  0,  0,  0},// B
         {236,238,236},{ 76,154,236},{120,124,236},{176, 98,236},{228, 84,236},{236, 88,180},{236,106,100},{212,136, 32},{160,170,  0},{116,196,  0},{ 76,208, 32},{ 56,204,108},{ 56,180,204},{ 60, 60, 60},{  0,  0,  0},{  0,  0,  0},// C
         {236,238,236},{168,204,236},{188,188,236},{212,178,236},{236,174,236},{236,174,212},{236,180,176},{228,196,144},{204,210,120},{180,222,120},{168,226,144},{152,226,180},{160,214,228},{160,162,160},{  0,  0,  0},{  0,  0,  0} // D
    };
    Texture t=new Texture();
    Font font = new Font("Times New Roman",Font.PLAIN,16);
    public Monitor6502()
    {
        for (int i=0;i<pb.length;i++)
        {
            for (int j=0;j<pb[0].length;j++)
            {
                pb[i][j] = 0xd;
            }
        }
        for (int i=0;i<tbb.length;i++)
        {
            for (int j=0;j<tbb[0].length;j++)
            {
                tbb[i][j] = 0xd;
            }
        }
        
    }
    public void reset()
    {
        for (int i=0;i<pb.length;i++)
        {
            for (int j=0;j<pb[0].length;j++)
            {
                pb[i][j] = 0xd;
                p[i][j] = getRGB(pb[i][j]);
            }
        }
        for (int i=0;i<tbb.length;i++)
        {
            for (int j=0;j<tbb[0].length;j++)
            {
                tcb[i][j] = 0;
                tc[i][j] = getRGB(tcb[i][j]);
                tbb[i][j] = 0xd;
                tb[i][j] = getRGB(tbb[i][j]);
                text[i][j] = (char)0;
            }
        }
        x=0;
        y=0;
        tx=0;
        ty=0;
        ctb=0xd;
        ctc=0x30;
        text_settings = 0;
    }
    public void update()
    {
        Bus6502 b=(Bus6502)bus;
        if (b.address==0x0230)
        {
            if (!b.write)
            {
                x=Byte.toUnsignedInt(b.data);
            }
            else
            {
                b.data=(byte)x;
            }
        }
        else if (b.address==0x0231)
        {
            if (!b.write)
            {
                y=Byte.toUnsignedInt(b.data);
            }
            else
            {
                b.data=(byte)y;
            }
        }
        else if (b.address==0x0232)
        {
            if (!b.write)
            {
                pb[y][x]=b.data;
                p[y][x]=getRGB(b.data);
                //System.out.println(Integer.toHexString(Byte.toUnsignedInt(b.data)));
            }
            else
            {
                //b.data=getC(p[y][x]);
                b.data=pb[y][x];
            }
        }
        else if (b.address==0x0233)
        {
            if (!b.write)
            {
                tx=Byte.toUnsignedInt(b.data);
            }
            else
            {
                b.data=(byte)tx;
            }
        }
        else if (b.address==0x0234)
        {
            if (!b.write)
            {
                ty=Byte.toUnsignedInt(b.data);
            }
            else
            {
                b.data=(byte)ty;
            }
        }
        else if (b.address==0x0235)
        {
            if (!b.write)
            {
                //System.out.println((char)b.data);
                if ((char)b.data == '\n')
                {
                    tx = 0;
                    if (ty!=text.length-1)
                    {
                        ty++;
                    }
                    return;
                }
                text[ty][tx] = (char)b.data;
                loadCharacter(tx,ty,ctc,ctb);
                int mtx = text[0].length;
                int mty = text.length;
                if ((text_settings&1) == 0)
                {
                    //System.out.println("inc");
                    tx++;
                    if (tx>=mtx)
                    {
                        tx = 0;
                        ty++;
                        if (ty>=mty)
                        {
                            for (int i=0;i<mty-1;i++)
                            {
                                for (int j=0;j<mtx;j++)
                                {
                                    text[i][j] = text[i+1][j];
                                    tcb[i][j] = tcb[i+1][j];
                                    tc[i][j] = tc[i][j];
                                }
                            }
                            ty=15;
                        }
                    }
                }
            }
            else
            {
                b.data=(byte)text[y][x];
            }
        }
        else if (b.address==0x0236)
        {
            if (!b.write)
            {
                ctc=b.data;
                //System.out.println(Integer.toHexString(Byte.toUnsignedInt(b.data)));
            }
            else
            {
                //b.data=getC(p[y][x]);
                b.data=ctc;
            }
        }
        else if (b.address==0x0237)
        {
            if (!b.write)
            {
                ctb=b.data;
                //System.out.println(Integer.toHexString(Byte.toUnsignedInt(b.data)));
            }
            else
            {
                //b.data=getC(p[y][x]);
                b.data=ctb;
            }
        }
        else if (b.address==0x0238)
        {
            if (!b.write)
            {
                text_settings=b.data;
                //System.out.println(Integer.toHexString(Byte.toUnsignedInt(b.data)));
            }
            else
            {
                //b.data=getC(p[y][x]);
                b.data=text_settings;
            }
        }
        else if (b.address==0x0239)
        {
            if (!b.write)
            {
                tcb[ty][tx]=b.data;
                tc[ty][tx]=getRGB(b.data);
                //System.out.println(Integer.toHexString(Byte.toUnsignedInt(b.data)));
            }
            else
            {
                //b.data=getC(p[y][x]);
                b.data=tcb[ty][tx];
            }
        }
        else if (b.address==0x023a)
        {
            if (!b.write)
            {
                tbb[ty][tx]=b.data;
                tb[ty][tx]=getRGB(b.data);
                //System.out.println(Integer.toHexString(Byte.toUnsignedInt(b.data)));
            }
            else
            {
                //b.data=getC(p[y][x]);
                b.data=tbb[ty][tx];
            }
        }
    }
    public int getRGB(byte c)
    {
        int c2=Byte.toUnsignedInt(c);
        c2%=color.length;
        int col=0;
        col=(color[c2][0]<<16)+(color[c2][1]<<8)+color[c2][2];
        return col;
    }
    /*public byte getC(int c)
    {
        byte col=0;
        int b=c&0xff;
        int g=(c>>8)&0xff;
        int r=(c>>16)&0xff;
        for (int i=0;i<color.length;i++)
        {
            if (Math.abs(r-color[i][0])<10 && Math.abs(g-color[i][1])<10 && Math.abs(b-color[i][2])<10)
            {
                col=(byte)i;
                break;
            }
        }
        return col;
    }*/
    public void load()
    {
        t.loadRGBA(p);
    }
    public void loadCharacter(int x, int y,byte tc,byte tb)
    {
        char t = text[y][x];
        int width = 256/text[0].length;
        int height = 256/text.length;
        int px = x*width;
        int py = y*height;
        //System.out.println(x+","+y+","+px+","+py);
        BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.setFont(font);
        g2d.setColor(new Color(getRGB(tc)));
        g2d.drawString(""+t,0,height-4);
        g2d.dispose();
        for (int i=0;i<height;i++)
        {
            for (int j=0;j<width;j++)
            {
                pb[py+i][px+j] = bi.getRGB(j,i)!=0?tc:tb;
                p[py+i][px+j] = getRGB(pb[py+i][px+j]);
            }
        }
    }
    public void render(int width,int height)
    {
        
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        t.bind(GL11.GL_TEXTURE_2D);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0,0);
        GL11.glVertex2f(0,0);
        GL11.glTexCoord2f(0,1);
        GL11.glVertex2f(0,height);
        GL11.glTexCoord2f(1,1);
        GL11.glVertex2f(width,height);
        GL11.glTexCoord2f(1,0);
        GL11.glVertex2f(width,0);
        GL11.glEnd();
        t.unbind();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }
}
