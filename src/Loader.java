import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;


public class Loader extends JFrame {
	private JProgressBar progress;
	public Loader() {
		super("...");
		setSize(300,70);
		setResizable(false);
		Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((ss.width/2)-150,(ss.height/2)-35);
		
		progress = new JProgressBar();
		add(progress);
		
		setVisible(true);
		progress.setValue(0);
		
		
		new Thread(new Runnable() {
			public void run() {
				progress.setValue(6);
				LoadRSClasses();
			}
		}).start();
	}
	
	
	private final String OLDSCHOOL_SITE = "http://oldschool21a.runescape.com/";
	private void LoadRSClasses() {
		try {
			String rsPage = GetWebpage(OLDSCHOOL_SITE+"j1");
			progress.setValue(16);
			
			Stub.ExtractParams(rsPage);
			progress.setValue(18);
			
			int jarInd = rsPage.indexOf("document.write('archive=");
			rsPage = rsPage.substring(jarInd+24);
			jarInd = rsPage.indexOf(".jar");
			
			Gnome.jarPath = rsPage.substring(0, jarInd)+".jar";
		} catch (Exception e) {e.printStackTrace();}
		
		Hashtable<String,Class<?>> rsClasses = new Hashtable<String,Class<?>>();
		
		this.dispose();
		Main.G = new Gnome();
	}
	
	static Hashtable<String,Class<?>> LoadClasses() throws Exception {
		Hashtable<String,Class<?>> classes = new Hashtable<String,Class<?>>();
		
		JarFile jf = new JarFile("gamepack.jar");
		Enumeration e = jf.entries();
		URLClassLoader cl = URLClassLoader.newInstance(new URL[]{new URL("jar:file:"+"gamepack.jar"+"!/")});
		
		JarEntry je;
		String name;
		while (e.hasMoreElements()) {
			je = (JarEntry) e.nextElement();
			name = je.getName();
			if (je.isDirectory() || !name.endsWith(".class")) {
				continue;
			}
			
			name = name.substring(0, name.length()-6).replace('/', '.');
			classes.put(name, cl.loadClass(name));
		}
		
		return classes;
	}

	private String GetWebpage(String path) throws Exception {
		URL url = new URL(path);
		URLConnection con = url.openConnection();
		Pattern p = Pattern.compile("text/html;\\s+charset=([^\\s]+)\\s*");
		Matcher m = p.matcher(con.getContentType());

		
		String charset = m.matches() ? m.group(1) : "ISO-8859-1";
		Reader r = new InputStreamReader(con.getInputStream(), charset);
		StringBuilder buf = new StringBuilder();
		while (true) {
		  int ch = r.read();
		  if (ch < 0) {
		    break;
		  }
		  buf.append((char) ch);
		}
		return buf.toString();
	}
}
