import java.awt.image.DataBufferByte;

//For custom shaders
public abstract class Shader {
	public abstract void Apply(DataBufferByte db);
}
