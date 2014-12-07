import java.awt.image.DataBufferByte;


public class GrayScaleShader extends Shader {

	public void Apply(DataBufferByte db) {
		byte data[] = db.getData();
		
		int avg;
		int i = data.length;
		while (i != 0) {
			i -= 3;
			
			avg = data[i]+data[i+1]+data[i+2];
			data[i] = data[i+1] = data[i+2] = (byte) (avg/3f);
		}
	}
	
}
