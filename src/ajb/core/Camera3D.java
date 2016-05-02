package ajb.core;

import org.lwjgl.opengl.GL11;

public class Camera3D {
	public float x=0;
	public float y=0;
	public float z=0;
	public float xRotation=0;
	public float yRotation=0;
	public float zRotation=0;
	public Camera3D(){
	}
	
	public void createView(){
		GL11.glRotatef(xRotation, 1, 0, 0);
		GL11.glRotatef(yRotation, 0, 1, 0);
		GL11.glRotatef(zRotation, 0, 0, 1);
		GL11.glTranslatef(x, y, z);
	}
}
