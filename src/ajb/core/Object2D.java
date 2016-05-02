package ajb.core;

import java.awt.Rectangle;

import org.lwjgl.opengl.GL11;

public class Object2D extends Object3D {
	
	private int width;
	private int height;
	private Rectangle mask=null;
	
	
	public Object2D(int wide, int high, int texture){
		//usually can be overriden
		isHud=true;
		width=wide;
		height=high;
		textureId=texture;
		makeList();
	}
	
	public void draw(){
		if(textureId!=-1){
			super.draw();
		}
	}
	
	public void setWidth(int wide){
		width=wide;
		GL11.glDeleteLists(displayList, 1);
		makeList();
	}
	
	public void setHeight(int high){
		height=high;
		GL11.glDeleteLists(displayList, 1);
		makeList();
	}
	
	public void setMask(Rectangle displayMask){
		mask=displayMask;
		GL11.glDeleteLists(displayList, 1);
		makeList();
	}
	
	public int getWidth(){
		return width;
	}
	
	public int getHeight(){
		return height;
	}
	
	/**
	 * Returns a copy of the mask rectangle.
	 * @return A copy of the mask rectangle.
	 */
	public Rectangle getMask(){
		return new Rectangle(mask.x,mask.y,mask.width,mask.height);
	}
	
	public void makeList(){
		displayList=GL11.glGenLists(1);
		GL11.glNewList(displayList, GL11.GL_COMPILE);
		if(mask==null){
			RenderUtils.renderImage(new Rectangle(0,0,width,height), width, height);
		}else{
			RenderUtils.renderImage(mask, width, height);
		}
		GL11.glEndList();
	}
	
}
