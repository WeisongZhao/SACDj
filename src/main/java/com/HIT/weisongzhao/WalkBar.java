package com.HIT.weisongzhao;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

public class WalkBar
  extends JToolBar
  implements ActionListener
{
  private JProgressBar progress = new JProgressBar();
  private JButton bnHelp = new JButton("Help");
  private JButton bnAbout = new JButton("About");
  private JButton bnClose = new JButton("Close");
  private String[] about = { "About", "Version", "Description", "Author", "Harbin Institute of Technology", "2020", "https://weisongzhao.github.io" };
  private String help;
  private double chrono;
  private int xSizeAbout = 400;
  private int ySizeAbout = 400;
  private int xSizeHelp = 400;
  private int ySizeHelp = 400;
  
  private static class SetValue
    implements Runnable
  {
    private int value;
    private JProgressBar progress;
    
    public SetValue(JProgressBar progress, int value)
    {
      this.progress = progress;
      this.value = value;
    }
    
    @Override
	public void run()
    {
      this.progress.setValue(this.value);
    }
  }
  
  private static class IncValue
    implements Runnable
  {
    private double inc;
    private JProgressBar progress;
    
    public IncValue(JProgressBar progress, double inc)
    {
      this.progress = progress;
      this.inc = inc;
    }
    
    @Override
	public void run()
    {
      this.progress.setValue((int)Math.round(this.progress.getValue() + this.inc));
    }
  }
  
  private static class SetMessage
    implements Runnable
  {
    private String msg;
    private JProgressBar progress;
    
    public SetMessage(JProgressBar progress, String msg)
    {
      this.progress = progress;
      this.msg = msg;
    }
    
    @Override
	public void run()
    {
      this.progress.setString(this.msg);
    }
  }
  
  public WalkBar()
  {
    super("Walk Bar");
    build("", false, false, false, 100);
  }
  
  public WalkBar(String initialMessage, boolean isAbout, boolean isHelp, boolean isClose)
  {
    super("Walk Bar");
    build(initialMessage, isAbout, isHelp, isClose, 100);
  }
  
  public WalkBar(String initialMessage, boolean isAbout, boolean isHelp, boolean isClose, int size)
  {
    super("Walk Bar");
    build(initialMessage, isAbout, isHelp, isClose, size);
  }
  
  private void build(String initialMessage, boolean isAbout, boolean isHelp, boolean isClose, int size)
  {
    if (isAbout) {
      add(this.bnAbout);
    }
    if (isHelp) {
      add(this.bnHelp);
    }
    addSeparator();
    add(this.progress);
    addSeparator();
    if (isClose) {
      add(this.bnClose);
    }
    this.progress.setStringPainted(true);
    this.progress.setString(initialMessage);
    
    this.progress.setMinimum(0);
    this.progress.setMaximum(100);
    this.progress.setPreferredSize(new Dimension(size, 20));
    this.bnAbout.addActionListener(this);
    this.bnHelp.addActionListener(this);
    
    setFloatable(false);
    setRollover(true);
    setBorderPainted(false);
    this.chrono = System.currentTimeMillis();
  }
  
  @Override
public synchronized void actionPerformed(ActionEvent e)
  {
    if (e.getSource() == this.bnHelp) {
      showHelp();
    } else if (e.getSource() == this.bnAbout) {
      showAbout();
    } else {
      e.getSource();
    }
  }
  
  public JButton getButtonClose()
  {
    return this.bnClose;
  }
  
  public void progress(String msg, int value)
  {
    double elapsedTime = System.currentTimeMillis() - this.chrono;
    String t = " [" + (elapsedTime > 3000.0D ? Math.round(elapsedTime / 10.0D) / 100.0D + "s." : new StringBuilder(String.valueOf(elapsedTime)).append("ms").toString()) + "]";
    SwingUtilities.invokeLater(new SetValue(this.progress, value));
    SwingUtilities.invokeLater(new SetMessage(this.progress, msg + t));
  }
  
  public void increment(double inc)
  {
    SwingUtilities.invokeLater(new IncValue(this.progress, inc));
  }
  
  public void setValue(int value)
  {
    SwingUtilities.invokeLater(new SetValue(this.progress, value));
  }
  
  public void setMessage(String msg)
  {
    SwingUtilities.invokeLater(new SetMessage(this.progress, msg));
  }
  
  public void progress(String msg, double value)
  {
    progress(msg, (int)Math.round(value));
  }
  
  public void reset()
  {
    this.chrono = System.currentTimeMillis();
    progress("Start", 0);
  }
  
  public void finish()
  {
    progress("End", 100);
  }
  
  public void finish(String msg)
  {
    progress(msg, 100);
  }
  
  public void fillAbout(String name, String version, String description, String author, String organisation, String date, String info)
  {
    this.about[0] = name;
    this.about[1] = version;
    this.about[2] = description;
    this.about[3] = author;
    this.about[4] = organisation;
    this.about[5] = date;
    this.about[6] = info;
  }
  
  public void fillHelp(String help)
  {
    this.help = help;
  }
  
  public void showAbout()
  {
    final JFrame frame = new JFrame("SACD analyze " + this.about[0]);
    JEditorPane pane = new JEditorPane();
    pane.setEditable(false);
    pane.setContentType("text/html; charset=ISO-8859-1");
    pane.setText("<html><head><title>" + this.about[0] + "</title>" + getStyle() + "</head><body>" + (
      this.about[0] == "" ? "" : new StringBuilder("<p class=\"name\">").append(this.about[0]).append("</p>").toString()) + (
      this.about[1] == "" ? "" : new StringBuilder("<p class=\"vers\">").append(this.about[1]).append("</p>").toString()) + (
      this.about[2] == "" ? "" : new StringBuilder("<p class=\"desc\">").append(this.about[2]).append("</p><hr>").toString()) + (
      this.about[3] == "" ? "" : new StringBuilder("<p class=\"auth\">").append(this.about[3]).append("</p>").toString()) + (
      this.about[4] == "" ? "" : new StringBuilder("<p class=\"orga\">").append(this.about[4]).append("</p>").toString()) + (
      this.about[5] == "" ? "" : new StringBuilder("<p class=\"date\">").append(this.about[5]).append("</p>").toString()) + (
      this.about[6] == "" ? "" : new StringBuilder("<p class=\"more\">").append(this.about[6]).append("</p>").toString()) + 
      "</html>");
    
    JButton bnClose = new JButton("Close");
    bnClose.addActionListener(new ActionListener()
    {
      @Override
	public void actionPerformed(ActionEvent e)
      {
        frame.dispose();
      }
    });
    pane.setCaret(new DefaultCaret());
    JScrollPane scrollPane = new JScrollPane(pane);
    
    scrollPane.setPreferredSize(new Dimension(this.xSizeAbout, this.ySizeAbout));
    frame.getContentPane().add(scrollPane, "North");
    frame.getContentPane().add(bnClose, "Center");
    
    frame.pack();
    frame.setResizable(false);
    frame.setVisible(true);
    center(frame);
  }
  
  
  public void showAbout_SACD()
  {
    final JFrame frame = new JFrame("About " + this.about[0]);
    JEditorPane pane = new JEditorPane();
    pane.setEditable(false);
    pane.setContentType("text/html; charset=ISO-8859-1");
    pane.setText("<html><head><title>" + this.about[0] + "</title>" + getStyle() + "</head><body>" + (
      this.about[0] == "" ? "" : new StringBuilder("<p class=\"name\">").append(this.about[0]).append("</p>").toString()) + (
      this.about[1] == "" ? "" : new StringBuilder("<p class=\"vers\">").append(this.about[1]).append("</p>").toString()) + (
      this.about[2] == "" ? "" : new StringBuilder("<p class=\"desc\">").append(this.about[2]).append("</p><hr>").toString()) + (
      this.about[3] == "" ? "" : new StringBuilder("<p class=\"auth\">").append(this.about[3]).append("</p>").toString()) + (
      this.about[4] == "" ? "" : new StringBuilder("<p class=\"orga\">").append(this.about[4]).append("</p>").toString()) + (
      this.about[5] == "" ? "" : new StringBuilder("<p class=\"date\">").append(this.about[5]).append("</p>").toString()) + (
      this.about[6] == "" ? "" : new StringBuilder("<p class=\"more\">").append(this.about[6]).append("</p>").toString()) + 
      "</html>");
    
    JButton bnClose = new JButton("Close");
    bnClose.addActionListener(new ActionListener()
    {
      @Override
	public void actionPerformed(ActionEvent e)
      {
        frame.dispose();
      }
    });
    pane.setCaret(new DefaultCaret());
    JScrollPane scrollPane = new JScrollPane(pane);
    
    scrollPane.setPreferredSize(new Dimension(this.xSizeAbout, this.ySizeAbout));
    frame.getContentPane().add(scrollPane, "North");
    frame.getContentPane().add(bnClose, "Center");
    
    frame.pack();
    frame.setResizable(false);
    frame.setVisible(true);
    center(frame);
  }
  
  
  
  public void showHelp()
  {
    final JFrame frame = new JFrame("Help " + this.about[0]);
    JEditorPane pane = new JEditorPane();
    pane.setEditable(false);
    pane.setContentType("text/html; charset=ISO-8859-1");
    pane.setText("<html><head><title>" + this.about[0] + "</title>" + getStyle() + "</head><body>" + (
      this.about[0] == "" ? "" : new StringBuilder("<p class=\"name\">").append(this.about[0]).append("</p>").toString()) + (
      this.about[1] == "" ? "" : new StringBuilder("<p class=\"vers\">").append(this.about[1]).append("</p>").toString()) + (
      this.about[2] == "" ? "" : new StringBuilder("<p class=\"desc\">").append(this.about[2]).append("</p>").toString()) + 
      "<hr><p class=\"help\">" + this.help + "</p>" + 
      "</html>");
    
    JButton bnClose = new JButton("Close");
    bnClose.addActionListener(new ActionListener()
    {
      @Override
	public void actionPerformed(ActionEvent e)
      {
        frame.dispose();
      }
    });
    pane.setCaret(new DefaultCaret());
    JScrollPane scrollPane = new JScrollPane(pane);
    scrollPane.setVerticalScrollBarPolicy(22);
    scrollPane.setPreferredSize(new Dimension(this.xSizeHelp, this.ySizeHelp));
    frame.setPreferredSize(new Dimension(this.xSizeHelp, this.ySizeHelp));
    frame.getContentPane().add(scrollPane, "Center");
    frame.getContentPane().add(bnClose, "South");
    frame.setVisible(true);
    frame.pack();
    center(frame);
  }
  
  private void center(Window w)
  {
    Dimension screenSize = new Dimension(0, 0);
    boolean isWin = System.getProperty("os.name").startsWith("Windows");
    if (isWin) {
      screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    }
    if (GraphicsEnvironment.isHeadless())
    {
      screenSize = new Dimension(0, 0);
    }
    else
    {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice[] gd = ge.getScreenDevices();
      GraphicsConfiguration[] gc = gd[0].getConfigurations();
      Rectangle bounds = gc[0].getBounds();
      if ((bounds.x == 0) && (bounds.y == 0)) {
        screenSize = new Dimension(bounds.width, bounds.height);
      } else {
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      }
    }
    Dimension window = w.getSize();
    if (window.width == 0) {
      return;
    }
    int left = screenSize.width / 2 - window.width / 2;
    int top = (screenSize.height - window.height) / 4;
    if (top < 0) {
      top = 0;
    }
    w.setLocation(left, top);
  }
  
  private String getStyle()
  {
    return 
      "<style type=text/css>body {backgroud-color:#222277}hr {width:80% color:#333366; padding-top:7px }p, li {margin-left:10px;margin-right:10px; color:#000000; font-size:1em; font-family:Verdana,Helvetica,Arial,Geneva,Swiss,SunSans-Regular,sans-serif}p.name {color:#ffffff; font-size:1.2em; font-weight: bold; background-color: #333366; text-align:center;}p.vers {color:#333333; text-align:center;}p.desc {color:#333333; font-weight: bold; text-align:center;}p.auth {color:#333333; font-style: italic; text-align:center;}p.orga {color:#333333; text-align:center;}p.date {color:#333333; text-align:center;}p.more {color:#333333; text-align:center;}p.help {color:#000000; text-align:left;}</style>";
  }
}
