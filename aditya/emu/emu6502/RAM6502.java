package aditya.emu.emu6502;
import aditya.emu.*;
import java.io.*;
import java.util.*;
public class RAM6502 extends Component
{
    public byte[] data;
    public int start,end;
    public RAM6502(int start,int end)
    {
        super();
        this.start=start;
        this.end=end;
        data=new byte[end-start];
    }
    public RAM6502()
    {
        //this(0,64*1024);
        this(0,0x10000);
    }
    public void update()
    {
        Bus6502 b=(Bus6502)bus;
        if (b.address>=start && b.address<end)
        {
            if (b.write)
            {
                b.data=data[b.address];
            }
            else
            {
                data[b.address]=b.data;
            }
        }
    }
    public void store(byte[] data,int offset)
    {
        for (int i=0;i<data.length;i++)
        {
            this.data[i+offset]=data[i];
        }
    }
    public void storeHex(String data,int offset)
    {
        if (data.length()%2==1)
        {
            data="0"+data;
        }
        byte[] d=new byte[data.length()/2];
        for (int i=0;i<d.length;i++)
        {
            String hd=""+data.charAt(i*2)+data.charAt(i*2+1);
            System.out.println(hd);
            d[i]=(byte)(Integer.parseInt(hd,16));
        }
        store(d,offset);
    }
    public String storeFile(String fn,int offset)
    {
        try
        {
            Scanner s=new Scanner(new FileInputStream(fn));
            String fc="";
            while (s.hasNextLine())
            {
                fc+=s.nextLine().replace(" ","");
            }
            storeHex(fc,offset);
        }catch(Exception e){return ""+e;}
        return null;
    }
    public String storeFileBin(String fn,int offset)
    {
        byte[] b=new byte[0x10000];
        byte[] d=null;
        try
        {
            FileInputStream fis=new FileInputStream(fn);
            int i=0,j=0;
            while ((i=fis.read())!=-1)
            {
                b[j]=(byte)i;
                j++;
            }
            d=new byte[j];
            for (i=0;i<j;i++)
            {
                d[i]=b[i];
            }
        }catch(Exception e){return ""+e;}
        store(d,offset);
        return null;
    }
    public String storeFileHex(String fn,int offset)
    {
        try
        {
            String fc="";
            FileInputStream fis=new FileInputStream(new File(fn));
            int i=0;
            while((i=fis.read())!=-1)
            {
                String hex=Integer.toHexString(i);
                if (hex.length()<2){hex="0"+hex;}
                fc+=hex;
            }
            storeHex(fc,offset);
        }catch(Exception e){return ""+e;}
        return null;
    }
}
