import java.awt.image.DataBufferByte;


public class RedTintShader extends Shader {
	
	

	@Override
	public void Apply(DataBufferByte db) {
		byte px[] = db.getData();
		
		int res;
		
		int i = px.length+2;
		while (i != 2) {
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
