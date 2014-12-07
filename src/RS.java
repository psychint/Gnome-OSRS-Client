import java.applet.Applet;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDB;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import sun.awt.image.BufImgSurfaceData;
import sun.java2d.*;
import sun.java2d.windows.GDIWindowSurfaceData;


public class RS extends JPanel implements Runnable, MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {
	
	private Hashtable<String,Class<?>> RSClasses;
	private Canvas clientCanvas;
	private float screenScaleX,screenScaleY;
	private int gameRightX,gameRightY,gameWidth,gameHeight;
	private int invyTop;
	private int motionBlurAmount;
	
	private boolean invyEnabled,chatEnabled;
	private Object aaVal;
	
	private Shader shader;
	
	private Color btnColor;
	
	private Rectangle invyRect,chatRect,showInvyRect,showChatRect;
	
	private AlphaComposite gameComp,uiComp;
	
	private GraphicsDevice gd;
	
	BufferedImage buf,gameBuf,screenBuf;
	Applet RSClient;
	
	public RS() {
		try {
			RSClasses = Loader.LoadClasses();
		} catch (Exception e1) {
			e1.printStackTrace();
			try {
				JOptionPane.showMessageDialog(this, "Updating please wait...");
				Main.G.window.getContentPane().setEnabled(false);
				Main.G.DownloadJar();
				Main.G.window.getContentPane().setEnabled(true);
				JOptionPane.showMessageDialog(null, "Gamepack.jar updated, please restart the client.");
				System.exit(0);
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
		}
		
		//check for any .class files in the working directory and if so try and load as a Shader
		ClassLoader cLoader = getClass().getClassLoader();
		try {
			GenericExtFilter filt = new GenericExtFilter(".class");
			String list[] = new File(System.getProperty("user.dir")).list(filt);
			if (list.length > 0) {
				Class<?> c = cLoader.loadClass(list[0].substring(0,list[0].indexOf('.')));
				shader = (Shader) c.newInstance();
			} else {
				shader = null;
			}
		} catch (Exception e2) {
			e2.printStackTrace();
			shader = null;
		}
		
		btnColor = new Color(70,70,70);
		
		invyEnabled = true;
		chatEnabled = true;
		aaVal = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
		
		buf = new BufferedImage(765,503,BufferedImage.TYPE_3BYTE_BGR);
		gameBuf = new BufferedImage(510,335,BufferedImage.TYPE_3BYTE_BGR);
		screenBuf = new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);

		uiComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
		gameComp = AlphaComposite.getInstance(AlphaComposite.SRC, 1f);
		
		invyRect = new Rectangle();
		chatRect = new Rectangle();
		showInvyRect = new Rectangle();
		showChatRect = new Rectangle();
				
		try {
			RSClient = ((Applet) RSClasses.get("client").newInstance());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Failed to create a new instance of client.class.", "Damnnnn, err I mean darn.", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		RSClient.setStub(new Stub(RSClient));
		
		try {
			RSClient.init();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				JOptionPane.showMessageDialog(this, "Updating please wait...");
				Main.G.window.getContentPane().setEnabled(false);
				Main.G.DownloadJar();
				Main.G.window.getContentPane().setEnabled(true);
				JOptionPane.showMessageDialog(null, "Gamepack.jar updated, please restart the client.");
				System.exit(0);
			} catch (InterruptedException | IOException e1) {
				e1.printStackTrace();
			}
		}
		
		while (RSClient.getComponentCount() == 0) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {}
		}
		
		clientCanvas = (Canvas)RSClient.getComponent(0);
		
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		this.addMouseWheelListener(this);
				
		GraphicsDB.OnRender = this;
	}
	
	public void run() {//render game screen to offscreen buffer, apply pixel shader if using one
		Graphics2D g = (Graphics2D)gameBuf.getGraphics();
		g.drawImage(buf, 0, 0, 510, 335, 5, 5, 515, 340, null);
		g.dispose();
		
		if (shader != null) {
			shader.Apply((DataBufferByte)gameBuf.getRaster().getDataBuffer());
		}
		
		
		
		g = (Graphics2D)screenBuf.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, aaVal);
		g.setComposite(gameComp);
		g.drawImage(gameBuf, 0, 0, gameWidth, gameHeight, null);
		
		g.setComposite(uiComp);
		if (chatEnabled) {
			g.drawImage(buf, 0, gameRightY, 520, gameHeight, 0, 340, 520, 503, null);
		}
		if (invyEnabled) {
			g.drawImage(buf, gameRightX, invyTop, gameWidth, gameHeight, 520, 0, 765, 503, null);
		}
		
		g.setColor(btnColor);
		g.fillRect(showInvyRect.x,showInvyRect.y,showInvyRect.width,showInvyRect.height);
		g.fillRect(showChatRect.x,showChatRect.y,showChatRect.width,showChatRect.height);
		
		//display frames per second
		/*
		nowTime = System.currentTimeMillis();
		deltaTime = (nowTime-lastTime)/1000.0f;
		lastTime = nowTime;
		
		nFrames++;
		nTime += deltaTime;
		if (nTime >= 1.0) {
			nTime = 0;
			avgFps = nFrames;
			nFrames = 0;
		}
		
		g.setColor(Color.cyan);
		g.drawString("FPS: "+avgFps, 20, 80);
		*/
		
		
		g.dispose();
		
		paintComponent(getGraphics());
		
		GraphicsDB.G = buf.getGraphics();
	}

	/*
	//for checking frames per second
	long lastTime,nowTime;
	float deltaTime;
	int nFrames,avgFps;
	float nTime;
	*/
	
	public void paintComponent(Graphics g) {//paint
		g.drawImage(screenBuf, 0, 0, null);
		g.dispose();
		Toolkit.getDefaultToolkit().sync();
	}
	
	
	public void onResize() {
		Dimension d = Main.G.window.getContentPane().getSize();
		setBounds(0,0,d.width,d.height);
		
		screenBuf = new BufferedImage(d.width,d.height,BufferedImage.TYPE_INT_ARGB);
		
				
		gameRightX = d.width-255;
		gameRightY = d.height-165;
		gameWidth = d.width;
		gameHeight = d.height;
		
		invyTop = gameHeight-503;
		
		
		chatRect.setBounds(0, gameRightY, 515, 165);
		invyRect.setBounds(gameRightX, invyTop, 255, 503);
		
		showChatRect.setBounds(520, gameHeight-20, 20, 20);
		showInvyRect.setBounds(gameRightX-20,gameHeight-20, 20, 20);
		
		screenScaleX = 510f/d.width;
		screenScaleY = 345f/d.height;
	}
	
	
	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		if (x < invyRect.x && y < chatRect.y) {
			clientCanvas.dispatchEvent(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), (int)(e.getX()*screenScaleX), (int)(e.getY()*screenScaleY), e.getClickCount(), e.isPopupTrigger()));
		} else {
			if (showChatRect.intersects(x, y, 1, 1)) {
				chatEnabled = !chatEnabled;
				return;
			}
			if (showInvyRect.intersects(x, y, 1, 1)) {
				invyEnabled = !invyEnabled;
				return;
			}
			if (chatEnabled) {
				if (chatRect.intersects(x, y, 1, 1)) {
					clientCanvas.dispatchEvent(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), x, y-chatRect.y+340, e.getClickCount(), e.isPopupTrigger()));
					return;
				}
			}
			if (invyEnabled) {
				if (invyRect.intersects(x,y,1,1)) {
					clientCanvas.dispatchEvent(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), x-invyRect.x+510, y-invyRect.y-5, e.getClickCount(), e.isPopupTrigger()));
					return;
				}
			}
			clientCanvas.dispatchEvent(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), (int)(e.getX()*screenScaleX), (int)(e.getY()*screenScaleY), e.getClickCount(), e.isPopupTrigger()));
		}
	}

	public void mouseReleased(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		if (x < invyRect.x && y < chatRect.y) {
			clientCanvas.dispatchEvent(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), (int)(e.getX()*screenScaleX), (int)(e.getY()*screenScaleY), e.getClickCount(), e.isPopupTrigger()));
		} else {
			if (showChatRect.intersects(x, y, 1, 1)) {
				return;
			}
			if (showInvyRect.intersects(x, y, 1, 1)) {
				return;
			}
			if (chatEnabled) {
				if (chatRect.intersects(x, y, 1, 1)) {
					clientCanvas.dispatchEvent(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), x, y-chatRect.y+340, e.getClickCount(), e.isPopupTrigger()));
					return;
				}
			}
			if (invyEnabled) {
				if (invyRect.intersects(x,y,1,1)) {
					clientCanvas.dispatchEvent(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), x-invyRect.x+510, y-invyRect.y+5, e.getClickCount(), e.isPopupTrigger()));
					return;
				}
			}
			clientCanvas.dispatchEvent(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), (int)(e.getX()*screenScaleX), (int)(e.getY()*screenScaleY), e.getClickCount(), e.isPopupTrigger()));
		}
	}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	
	
	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		if (x < invyRect.x && y < chatRect.y) {
			clientCanvas.dispatchEvent(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), (int)(e.getX()*screenScaleX), (int)(e.getY()*screenScaleY), e.getClickCount(), e.isPopupTrigger()));
		} else {
			if (chatEnabled) {
				if (chatRect.intersects(x, y, 1, 1)) {
					clientCanvas.dispatchEvent(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), x, y-chatRect.y+340, e.getClickCount(), e.isPopupTrigger()));
					return;
				}
			}
			if (invyEnabled) {
				if (invyRect.intersects(x,y,1,1)) {
					clientCanvas.dispatchEvent(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), x-invyRect.x+510, y-invyRect.y+5, e.getClickCount(), e.isPopupTrigger()));
					return;
				}
			}
			clientCanvas.dispatchEvent(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), (int)(e.getX()*screenScaleX), (int)(e.getY()*screenScaleY), e.getClickCount(), e.isPopupTrigger()));
		}
	}
	public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		if (x < invyRect.x && y < chatRect.y) {
			clientCanvas.dispatchEvent(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), (int)(e.getX()*screenScaleX), (int)(e.getY()*screenScaleY), e.getClickCount(), e.isPopupTrigger()));
		} else {
			if (chatEnabled) {
				if (chatRect.intersects(x, y, 1, 1)) {
					clientCanvas.dispatchEvent(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), x, y-chatRect.y+340, e.getClickCount(), e.isPopupTrigger()));
					return;
				}
			}
			if (invyEnabled) {
				if (invyRect.intersects(x,y,1,1)) {
					clientCanvas.dispatchEvent(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), x-invyRect.x+510, y-invyRect.y+5, e.getClickCount(), e.isPopupTrigger()));
					return;
				}
			}
			clientCanvas.dispatchEvent(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), (int)(e.getX()*screenScaleX), (int)(e.getY()*screenScaleY), e.getClickCount(), e.isPopupTrigger()));
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		clientCanvas.dispatchEvent(e);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_F10) {
			motionBlurAmount++;
			if (motionBlurAmount == 6) {
				motionBlurAmount = 0;
				gameComp = AlphaComposite.getInstance(AlphaComposite.SRC, 1f);
			} else {
				gameComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f-(motionBlurAmount*0.15f));
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_F11) {
			if (aaVal == RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR) {
				aaVal = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
			} else {
				aaVal = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
			}
			return;
		}
		if (e.getKeyCode() == KeyEvent.VK_F12) {
			Main.G.window.dispose();
			Main.G.initWindow(!Main.G.window.isUndecorated());
			if (Main.G.window.isUndecorated()) {
				if (gd == null) {
					gd = Main.G.window.getGraphicsConfiguration().getDevice();
				}
				
				gd.setFullScreenWindow(Main.G.window);
			}
			return;
		}
		
		clientCanvas.dispatchEvent(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		clientCanvas.dispatchEvent(e);

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		clientCanvas.dispatchEvent(e);
	}
}


class GenericExtFilter implements FilenameFilter {
	 
	private String ext;

	public GenericExtFilter(String ext) {
		this.ext = ext;
	}

	public boolean accept(File dir, String name) {
		return (name.endsWith(ext));
	}
}
