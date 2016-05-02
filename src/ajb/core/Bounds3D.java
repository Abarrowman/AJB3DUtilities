package ajb.core;

public abstract class Bounds3D {
	public float x=0;
	public float y=0;
	public float z=0;
	
	public abstract boolean contains(float[] point);
}
