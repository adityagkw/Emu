package aditya.emu.emu6502;
import aditya.emu.*;
import org.lwjgl.opengl.*;
import org.lwjgl.input.*;
import org.lwjgl.openal.*;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.*;
import java.text.*;
import javax.swing.text.*;
public class EmuSystem6502Comp extends EmuSystem6502
{
    RAM6502 ram;
    Monitor6502 monitor;
    Keyboard6502 keyboard;
    Mouse6502 mouse;
    static JFrame f;
    static EmuSystem6502Comp emu;
    static boolean play=true, single_step = false;
    //static String input_buffer;
    //static JFrame inf;
    static boolean mem_live_update = false;
    public EmuSystem6502Comp()
    {
        super();
        ram=new RAM6502();
        ram.parent=this;
        monitor=new Monitor6502();
        monitor.parent=this;
        keyboard=new Keyboard6502();
        keyboard.parent=this;
        mouse=new Mouse6502();
        mouse.parent=this;
        CPU6502 c=(CPU6502)cpu;
        c.parent=this;
        components.add(ram);
        components.add(monitor);
        components.add(keyboard);
        components.add(mouse);
    }
    public void update()
    {
        CPU6502 c=(CPU6502)cpu;
        c.parent=this;
        c.update();
    }
    public static void main(String[] args)
    {
        int width=800,height=600;
        f = new JFrame("Emu 6502");
        f.setSize(width,height);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setResizable(false);
        f.setVisible(true);
        f.setLayout(new BorderLayout());
        Canvas canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(width,height));
        f.add(canvas,BorderLayout.CENTER);
        
        JMenuBar menubar = new JMenuBar();
        JMenu file_menu = new JMenu("File");
        JMenuItem open_item = new JMenuItem("Open");
        open_item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                open();
            }
        });
        file_menu.add(open_item);
        menubar.add(file_menu);
        JMenu tools_menu = new JMenu("Tools");
        JMenuItem control_item = new JMenuItem("Control");
        tools_menu.add(control_item);
        JMenuItem disassembly_item = new JMenuItem("Disassembly");
        tools_menu.add(disassembly_item);
        JMenuItem memory_viewer_item = new JMenuItem("Memory Viewer");
        tools_menu.add(memory_viewer_item);
        JMenuItem input_buffer_item = new JMenuItem("Input Buffer");
        tools_menu.add(input_buffer_item);
        menubar.add(tools_menu);
        f.add(menubar,BorderLayout.PAGE_START);
        
        JFrame inf = new JFrame("Input Buffer");
        inf.setSize(400,400);
        inf.setLayout(new BorderLayout());
        inf.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        JTextArea input_buffer_area = new JTextArea();
        input_buffer_area.setEditable(false);
        inf.add(input_buffer_area,BorderLayout.CENTER);
        Thread input_buffer_thread = new Thread()
        {
            public void run()
            {
                String buffer = "";
                while (true)
                {
                    try
                    {
                        if (!emu.keyboard.buffer.equals(buffer))
                        {
                            buffer = emu.keyboard.buffer;
                            input_buffer_area.setText("Buffer:\n"+buffer);
                        }
                        Thread.sleep(100);
                    }catch(Exception e){}
                }
            }
        };
        input_buffer_thread.start();
        inf.setVisible(false);
        input_buffer_item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                inf.setVisible(true);
            }
        });
        
        JFrame memf = new JFrame("Memory Viewer");
        memf.setSize(600,500);
        memf.setLayout(new BorderLayout());
        memf.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        JPanel mem_top = new JPanel();
        JLabel mem_al = new JLabel("Page:");
        mem_top.add(mem_al);
        MaskFormatter address_format=null; 
        try
        {
            address_format = new MaskFormatter("HH");
        }catch(Exception e){}
        JFormattedTextField mem_at= new JFormattedTextField(address_format);
        mem_at.setPreferredSize(new Dimension(100,20));
        mem_at.setText("00");
        mem_top.add(mem_at);
        memf.add(mem_top,BorderLayout.PAGE_START);
        JPanel mem_main = new JPanel();
        mem_main.setLayout(new BoxLayout(mem_main,BoxLayout.LINE_AXIS));
        JTextArea mem_hex = new JTextArea();
        mem_hex.setEditable(false);
        mem_hex.setPreferredSize(new Dimension(400,350));
        //mem_hex.setContentType("text/html");
        mem_hex.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        mem_main.add(mem_hex);
        JTextArea mem_ascii = new JTextArea();
        mem_ascii.setEditable(false);
        mem_ascii.setPreferredSize(new Dimension(200,350));
        mem_ascii.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        mem_main.add(mem_ascii);
        memf.add(mem_main,BorderLayout.CENTER);
        JCheckBox mem_live = new JCheckBox("Live update");
        mem_live.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                mem_live_update = e.getStateChange()==1;
            }
        });
        memf.add(mem_live,BorderLayout.PAGE_END);
        Thread memt = new Thread()
        {
            public void run()
            {
                byte[] cd = new byte[256];
                int ca = 0;
                String mem_str = "";
                String mem_astr = "";
                while (true)
                {
                    try
                    {
                        int a = Integer.parseInt(mem_at.getText(),16)*256;
                        boolean u = mem_live_update;
                        if (ca!=a)
                        {
                            //System.out.println("Address: "+a);
                            ca=a;
                            u = true;
                        }
                        if (u)
                        {
                            mem_str = "     00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F";
                            mem_astr = "";
                            for (int i=0;i<256;i++)
                            {
                                if (i%16==0)
                                {
                                    String d = ""+Integer.toHexString((a+i)/16)+"0";
                                    while (d.length()<4) d = "0"+d;
                                    mem_str+="\n"+d+" ";
                                    mem_astr+="\n";
                                }
                                if (a+i<0x10000)
                                {
                                    cd[i] = emu.ram.data[a+i];//((CPU6502)emu.cpu).readInstruction(a+i);
                                    String d = Integer.toHexString(0xff&cd[i]);
                                    if (d.length()<2) d = "0"+d;
                                    mem_str += d + " ";
                                    char c=(char)cd[i];
                                    if (c<32)
                                    {
                                        c = '.';
                                    }
                                    mem_astr+=c;
                                }
                                else
                                {
                                    cd[i] = 0;
                                }
                            }
                            //mem_str+="</body></html>";
                            mem_hex.setText(mem_str);
                            mem_ascii.setText(mem_astr);
                        }
                        Thread.sleep(1000);
                    }catch(Exception e){}
                }
            }
        };
        memt.start();
        memf.setVisible(false);
        memory_viewer_item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                memf.setVisible(true);
            }
        });
        
        JFrame conf = new JFrame("Control");
        conf.setSize(600,500);
        conf.setLayout(new BorderLayout());
        conf.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        conf.setVisible(false);
        JPanel con_top = new JPanel();
        //con_top.setLayout(new BoxLayout(con_top,BoxLayout.LINE_AXIS));
        JButton playb = new JButton("Play");
        playb.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                play = true;
            }
        });
        con_top.add(playb);
        JButton sstepb = new JButton("Step");
        sstepb.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                single_step = true;
            }
        });
        con_top.add(sstepb);
        JButton pauseb = new JButton("Pause");
        pauseb.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                play = false;
            }
        });
        con_top.add(pauseb);
        JButton resetb = new JButton("Reset");
        resetb.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                reset_comp();
                play = false;
            }
        });
        con_top.add(resetb);
        conf.add(con_top,BorderLayout.PAGE_START);
        JPanel con_main = new JPanel();
        JTextArea con_text = new JTextArea();
        con_text.setEditable(false);
        con_text.setPreferredSize(new Dimension(500,500));
        Thread cont = new Thread()
        {
            public void run()
            {
                while (true)
                {
                    try
                    {
                        CPU6502 c=(CPU6502)emu.cpu;
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
                        String cstr = "";
                        cstr+="A: \t"+A+"\n";
                        cstr+="X: \t"+X+"\n";
                        cstr+="Y: \t"+Y+"\n";
                        cstr+="PC: \t"+PC+"\n";
                        cstr+="SP: \t"+SP+"\n";
                        cstr+="Flags:\tNV-BDIZC"+"\n";
                        cstr+="SR: \t"+SR+"\n";
                        cstr+="\nOpcode: "+Integer.toHexString(c.opcode).toUpperCase()+"\t";
                        cstr+=c.opcode_names[c.opcode]+"\t";
                        cstr+=c.opcode_mode[c.opcode]+"\n";
                        cstr+="Cycles:"+c.cycles;
                        con_text.setText(cstr);
                        Thread.sleep(100);
                    }catch(Exception e){}
                }
            }
        };
        cont.start();
        con_main.add(con_text);
        conf.add(con_main,BorderLayout.CENTER);
        control_item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                conf.setVisible(true);
            }
        });
        
        JFrame disf = new JFrame("Disassembly");
        disf.setSize(600,500);
        disf.setLayout(new BorderLayout());
        disf.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        JPanel dis_top = new JPanel();
        JLabel dis_al = new JLabel("Address:");
        dis_top.add(dis_al);
        MaskFormatter dis_format=null; 
        try
        {
            dis_format = new MaskFormatter("HHHH");
        }catch(Exception e){}
        JFormattedTextField dis_at= new JFormattedTextField(dis_format);
        dis_at.setPreferredSize(new Dimension(100,20));
        dis_at.setText("0000");
        dis_top.add(dis_at);
        disf.add(dis_top,BorderLayout.PAGE_START);
        JTextArea dis_main = new JTextArea();
        dis_main.setEditable(false);
        dis_main.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        dis_main.setPreferredSize(new Dimension(500,500));
        Thread dist = new Thread()
        {
            public void run()
            {
                int p = -1;
                while (true)
                {
                    try
                    {
                        int a = 0xffff & Integer.parseInt(dis_at.getText(),16);
                        if (a!=p)
                        {
                            String diss = "";
                            int ind = 0;
                            CPU6502 c = (CPU6502) emu.cpu;
                            for (int i=0;i<32;i++)
                            {
                                diss += disasseble(a+ind)+"\n";
                                ind += modeSize(c.opcode_mode[0xff & emu.ram.data[a+ind]]);
                            }
                            dis_main.setText(diss);
                        }
                        p = a;
                        Thread.sleep(100);
                    }catch(Exception e){}
                }
            }
        };
        dist.start();
        disf.add(dis_main,BorderLayout.CENTER);
        disf.setVisible(false);
        disassembly_item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                disf.setVisible(true);
            }
        });
        
        
        try
        {
            AL.create();
            Display.setDisplayMode(new DisplayMode(width-100,height-100));
            Display.create();
            Display.setTitle("6502 Emulator Monitor");
            Display.setParent(canvas);
            //System.out.println(Display.getWidth()+" "+Display.getHeight());
            Mouse.create();
            Keyboard.create();
        }catch(Exception e){System.out.println(""+e);}
        
        f.revalidate();
        f.repaint();
        
        emu=new EmuSystem6502Comp();
        //emu.ram.storeFileBin("pong.6502",0);
        emu.ram.storeFileBin("hello.6502",0);
        //emu.ram.storeFileBin("input.6502",0);
        emu.cpu.reset();
        /*Thread t=new Thread()
        {
            public void run()
            {
                while (true)
                {
                    try
                    {
                        emu.cpu.update();
                        Thread.sleep(1);
                    }catch(Exception e){e.printStackTrace();}
                }
            }
        };
        //t.start();*/
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glOrtho(0,width,height,0,-0.01,100);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        while(!Display.isCloseRequested())
        {
            for (int i=0;i<20000;i++)
            {
                if (play || single_step)
                {
                     if (single_step)
                     {
                         CPU6502 c = (CPU6502)emu.cpu;
                         //int p = c.cycles;
                         while (c.cycles!=0)
                         {
                            //System.out.println(c.cycles);
                            emu.cpu.update();
                         }
                     }
                     emu.cpu.update();
                     single_step = false;
                }
            }
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            GL11.glOrtho(0,width,height,0,-0.01,100);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glClearColor(0,0,0,0);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glLoadIdentity();
            emu.monitor.load();
            emu.monitor.render(width,height);
            //System.out.println(emu.ram.data[0x0300]);
            Display.update();
            Display.sync(60);
        }
        Display.destroy();
        Mouse.destroy();
        Keyboard.destroy();
        AL.destroy();
        System.exit(0);
    }
    
    static void open()
    {
        JFileChooser fc = new JFileChooser(".");
        int i = fc.showOpenDialog(f);
        if (i==JFileChooser.APPROVE_OPTION)
        {
            play = false;
            emu.ram.storeFileBin(fc.getSelectedFile().getPath(),0);
            reset_comp();
            //emu.cpu.reset();
            //emu.monitor.reset();
            //emu.keyboard.reset();
            //play = true;
        }
    }
    static String disasseble(int a)
    {
        String str = "";
        CPU6502 c = (CPU6502) emu.cpu;
        int opcode = 0xff & emu.ram.data[a];
        String instruction = c.opcode_names[opcode];
        String mode = c.opcode_mode[opcode];
        //System.out.println(a+" "+opcode+" "+instruction+" "+mode);
        String astr = Integer.toHexString(a & 0xffff);
        for (int l=astr.length();l<4;astr="0"+astr,l++);
        str=astr+" : ";
        for (int i=0;i<modeSize(mode);i++)
        {
            String dstr = Integer.toHexString(0xff & emu.ram.data[a+i]);
            for (int l=dstr.length();l<2;dstr="0"+dstr,l++);
            str +=dstr+" ";
        }
        for (int l = str.length();l<18;str+=" ",l++);
        str+=": "+instruction;
        //System.out.println(str);
        switch (mode)
        {
            case "impl ":
            break;
            case "A    ":
            str+=" A";
            break;
            case "#    ":
            str+=" #$"+Integer.toHexString(emu.ram.data[a+1]);
            break;
            case "abs  ":
            str+=" $"+Integer.toHexString((0xff & emu.ram.data[a+1])+(0xff00 & (emu.ram.data[a+2]<<8)));
            break;
            case "rel  ":
            str+=" "+emu.ram.data[a+1]+" ; abs = "+Integer.toHexString(a+2+emu.ram.data[a+1]);
            break;
            case "zpg  ":
            str+= " $"+Integer.toHexString(0xff & emu.ram.data[a+1]);
            break;
            case "ind  ":
            str+=" ($"+Integer.toHexString((0xff & emu.ram.data[a+1])+(0xff00 & (emu.ram.data[a+2]<<8)))+")";
            break;
            case "abs,X":
            str+=" $"+Integer.toHexString((0xff & emu.ram.data[a+1])+(0xff00 & (emu.ram.data[a+2]<<8)))+",X";
            break;
            case "abs,Y":
            str+=" $"+Integer.toHexString((0xff & emu.ram.data[a+1])+(0xff00 & (emu.ram.data[a+2]<<8)))+",Y";
            break;
            case "X,ind":
            str+= " ($"+Integer.toHexString(0xff & emu.ram.data[a+1])+",X)";
            break;
            case "ind,Y":
            str+= " ($"+Integer.toHexString(0xff & emu.ram.data[a+1])+"),Y";
            break;
            case "zpg,X":
            str+= " $"+Integer.toHexString(0xff & emu.ram.data[a+1])+",X";
            break;
            case "zpg,Y":
            str+= " $"+Integer.toHexString(0xff & emu.ram.data[a+1])+",Y";
            break;
        }
        return str;
    }
    static int modeSize(String mode)
    {
        switch(mode)
        {
            case "impl ":
            return 1;
            case "A    ":
            return 1;
            case "#    ":
            return 2;
            case "abs  ":
            return 3;
            case "rel  ":
            return 2;
            case "zpg  ":
            return 2;
            case "ind  ":
            return 3;
            case "abs,X":
            return 3;
            case "abs,Y":
            return 3;
            case "X,ind":
            return 2;
            case "ind,Y":
            return 2;
            case "zpg,X":
            return 2;
            case "zpg,Y":
            return 2;
            
        }
        return 1;
    }
    static void reset_comp()
    {
        emu.cpu.reset();
        emu.monitor.reset();
        emu.keyboard.reset();
    }
}
