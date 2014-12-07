import java.awt.image.DataBufferByte;


public class GreenTintShader extends Shader {
	
	public void Apply(DataBufferByte db) {
		byte px[] = db.getData();
		
		int res;
		
		int i = px.length+1;
		while (i != 1) {
			i -= 3;
			
			res = (int) (px[i]*1.2);
			if (res > 127) {
				px[i] = 127;
			} else {
				px[i] = (byte) res;
			}
		}
	}

}
