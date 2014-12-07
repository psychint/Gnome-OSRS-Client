package java.awt;

public class GraphicsDB {
	
	public static Runnable OnRender;

	public static Graphics G;
	public static Graphics GS[];//maybe used later for multiple canvases?
	static {
		GS = new Graphics[0];
	}
	
}
