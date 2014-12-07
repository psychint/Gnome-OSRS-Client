import java.applet.Applet;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.SystemColor;

import javax.swing.JButton;
import javax.swing.ImageIcon;

import java.awt.Insets;
import java.awt.FlowLayout;

import javax.swing.JLabel;

import java.awt.Component;

import javax.swing.Box;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.JTabbedPane;


public class Gnome implements WindowListener, ComponentListener {
		
	private JPanel clientWrap,panel;
	private JTabbedPane tabbedPane;
	
	RS rs;
	JFrame window;
	
	
	static String jarPath;
	
	public Gnome() {
		initWindow(false);
	}
	
	public void initWindow(boolean undec) {
		window = new JFrame("Gnome Oldschool Client");
		window.setSize(765,503);
		window.setResizable(true);
		window.setUndecorated(undec);
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.setVisible(true);
		
		Container pane = window.getContentPane();
		pane.setLayout(null);
		pane.setFocusable(true);
		pane.requestFocus();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (rs == null) {
					rs = new RS();
				}
				
				window.getContentPane().add(rs);
				rs.onResize();
				
				window.getContentPane().addKeyListener(rs);
				window.addWindowListener(Main.G);
				window.addComponentListener(Main.G);
			}
		});
	}
	
	static void DownloadJar() throws InterruptedException, IOException {
		URL website = new URL("http://oldschool1a.runescape.com/"+jarPath);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream("gamepack.jar");
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
	}
	
	public void windowOpened(WindowEvent e) {}
	public void windowClosing(WindowEvent e) {
		int res = JOptionPane.showConfirmDialog(window, "Are you sure?", "Do you really want to...", JOptionPane.YES_NO_OPTION);
		if (res == JOptionPane.YES_OPTION) {
			System.exit(0);
		}
	}
	public void windowClosed(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void componentResized(ComponentEvent e) {
		rs.onResize();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {}
}
