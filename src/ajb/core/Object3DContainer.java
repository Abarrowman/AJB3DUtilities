package ajb.core;

public interface Object3DContainer {
	public abstract void addChild(Object3D object);
	public abstract void removeChild(Object3D object);
	public abstract boolean hasChild(Object3D object);
	public abstract void removeAllChildren();
	public abstract float[][] getTransformationMatrix();
}
