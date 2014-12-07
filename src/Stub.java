import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;


//
public class Stub implements AppletStub {
	
	static String ParamNames[] = new String[16];
	static String ParamValues[] = new String[16];
	
	private Applet app;

	public Stub(Applet app) {
		super();
		
		this.app = app;
		
		for (int i = 0; i < 16; i++) {
			addParameter(ParamNames[i],ParamValues[i]);
			System.out.println("Setting rs client applet param "+ParamNames[i]+" to "+ParamValues[i]);
		}
	}

	static void ExtractParams(String rsPage) {
		for (int i = 0; i < 16; i++) {
			int paramInd = rsPage.indexOf("<param name=")+13;
			rsPage = rsPage.substring(paramInd);
			int paramEndInd = rsPage.indexOf('"');
			ParamNames[i] = rsPage.substring(0, paramEndInd);
			
			paramInd = rsPage.indexOf("value=")+7;
			rsPage = rsPage.substring(paramInd);
			paramEndInd = rsPage.indexOf('"');
			ParamValues[i] = rsPage.substring(0, paramEndInd);
			if (ParamValues[i].length() == 3 && ParamValues[i].startsWith("3")) {
				ParamValues[i] = "301";
			}
		}
	}
	
    HashMap<String,String> params = new HashMap<String,String>();

    public void appletResize(int width, int height) {}
    public AppletContext getAppletContext() {
        return null;
    }

    public Applet getApplet() {
    	return app;
    }
    
    public URL getDocumentBase() {
        try {
			return new URL("http://oldschool1a.runescape.com/");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
    }

    public URL getCodeBase() {
        return getDocumentBase();
    }

    public boolean isActive() {
        return false;
    }
    
    public String getParameter(String name) {
        return params.get(name);
    }

    public void addParameter(String name, String value) {
        params.put(name, value);
    }
}
