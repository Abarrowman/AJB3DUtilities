package ajb.core;

import java.util.Vector;
import org.lwjgl.opengl.GL11;

public class Object3D implements Object3DContainer  {
	//default transformations
	/*
	public float x=0;
	public float y=0;
	public float z=0;
	public float xRotation=0;
	public float yRotation=0;
	public float zRotation=0;
	public float xScale=1;
	public float yScale=1;
	public float zScale=1;
	*/
	
	public float[][] matrix;
	public boolean visible=true;
	public boolean isHud=false;
	public boolean isOpaque=true;
	
	//display list
	public int displayList=-1;
	public int textureId=-1;
	
	
	public Vector<Object3D> children;
	public Object3DContainer parent;
	
	
	public Bounds3D bounds=null;	
	
	public Object3D(){
		children=new Vector<Object3D>();
		parent=null;
		matrix=MatrixMath.identityMatrix(4, 4);
	}
	

	public void draw(){
		if(visible){
			preRender();
			render();
			postRender();
		}
	}
	
	protected void render() {
		if(displayList!=-1){
			GL11.glCallList(displayList);
		}
		for(int n=0;n<children.size();n++){
			children.get(n).draw();
		}
	}
	
	public final void applyMatrix(float[][] trans){
		matrix=MatrixMath.multiplyMatracies(matrix, trans);
	}
	
	/*
	 * Incrementing Y Rotation (property 6) in the proximity of pi/2 or -pi/2 things rotation doesn't ensue.
	 */
	
	public final float getProperty(int property){
		if(property>=0&&property<9){
			float[] properties=getProperties();
			return properties[property];
		}else{
			return Float.NaN;
		}
	}
	
	public final void setProperty(int property, float value){
		if(property>=0&&property<9){
			float[] properties=getProperties();
			properties[property]=value;
			setMatrixFromProperties(properties);
		}
	}
	
	public final void incrementProperty(int property, float value){
		if(property>=0&&property<9){
			float[] properties=getProperties();
			properties[property]+=value;
			setMatrixFromProperties(properties);
		}
	}
	/**
	 * 
	 * @return An array of values representing one way of constructing this transformation matrix.
	 * 0 -> X Translation  1 -> Y Translation  2 -> Z Translation
	 * 3 -> X Rotation  4 -> Y Rotation  5 -> Z Rotation
	 *  6 -> X Scale  7 -> Y Scale  8 -> Z Scale
	 */
	public final float[] getProperties(){
		float[] properties=new float[9];
		float[] origin=VectorMath.homogenizeVector(VectorMath.vectorFromColumnVectorMatrix(MatrixMath.multiplyMatracies(matrix,
				MatrixMath.matrixFromColumnVector(new float[]{0,0,0,1}))));
		
		float[] xAxis=VectorMath.subtractVectors(VectorMath.homogenizeVector(VectorMath.vectorFromColumnVectorMatrix(
				MatrixMath.multiplyMatracies(matrix,MatrixMath.matrixFromColumnVector(new float[]{1,0,0,1})))),origin);
		
		float[] yAxis=VectorMath.subtractVectors(VectorMath.homogenizeVector(VectorMath.vectorFromColumnVectorMatrix(
				MatrixMath.multiplyMatracies(matrix,MatrixMath.matrixFromColumnVector(new float[]{0,1,0,1})))),origin);
		
		float[] zAxis=VectorMath.subtractVectors(VectorMath.homogenizeVector(VectorMath.vectorFromColumnVectorMatrix(
				MatrixMath.multiplyMatracies(matrix,MatrixMath.matrixFromColumnVector(new float[]{0,0,1,1})))),origin);
		
		//x
		properties[0]=origin[0];
		//y
		properties[1]=origin[1];
		//z
		properties[2]=origin[2];
		//scale
		properties[6]=VectorMath.getMagnitude(xAxis);
		properties[7]=VectorMath.getMagnitude(yAxis);
		properties[8]=VectorMath.getMagnitude(zAxis);
		
		//facing
		float[] angles=VectorMath.anglesOf(zAxis);
		float xRot=angles[0];
		float yRot=angles[1];	
				
		properties[3]=xRot;
		properties[4]=yRot;
		
		
		
		float[][] matx=MatrixMath.multiplyMatracies(MatrixMath.multiplyMatracies(
				MatrixMath.translation3D(properties[0], properties[1], properties[2]),
				MatrixMath.rotation3D(properties[3], properties[4], 0)),
				MatrixMath.scale3D(properties[6], properties[7], properties[8]));
		
		yAxis=VectorMath.subtractVectors(VectorMath.homogenizeVector(VectorMath.vectorFromColumnVectorMatrix(
				MatrixMath.multiplyMatracies(matx,MatrixMath.matrixFromColumnVector(new float[]{0,1,0,1})))),origin);
				
		float[] perpXZ=VectorMath.crossProduct(xAxis, zAxis);
		
		float zRot;
		
		float anglePerpXZToY=VectorMath.angleBetween(yAxis, perpXZ);
		
		if(Float.isNaN(anglePerpXZToY)){
			if(VectorMath.absVectorDifference(yAxis, perpXZ)<VectorMath.absVectorDifference(yAxis, VectorMath.multiplyVector(perpXZ,-1f))){
				zRot=(float)Math.PI;
			}else{
				zRot=0f;
			}
		}else{
			if(perpXZ[0]>0){
				zRot=(float)(Math.PI-(double)anglePerpXZToY);
			}else{
				zRot=(float)(Math.PI+(double)anglePerpXZToY);
			}
		}
		
		float pow=(float)Math.pow(10,6);
		zRot=(float)Math.round(zRot*pow)/pow;
		
		properties[5]=zRot;
				
		return properties;
	}
	
	public final void setMatrixFromProperties(float[] properties){
		if(properties.length==9){
			float x=properties[0];
			float y=properties[1];
			float z=properties[2];
			float xRotation=properties[3];
			float yRotation=properties[4];
			float zRotation=properties[5];
			float xScale=properties[6];
			float yScale=properties[7];
			float zScale=properties[8];
			matrix=MatrixMath.multiplyMatracies(MatrixMath.multiplyMatracies(
					MatrixMath.translation3D(x, y, z),
					MatrixMath.rotation3D(xRotation, yRotation, zRotation)),
					MatrixMath.scale3D(xScale, yScale, zScale));
		}
	}
	
	public final void rotateAround(float[] axis, float radians){
		rotateAround(axis, new float[]{0,0,0}, radians);
	}
	
	public final void rotateAround(float[] axis, float[] location, float radians){
		if(axis.length==3&&location.length==3){
			float[] properties=getProperties();
									
			axis=VectorMath.normalize(axis);			
			float[] angs;
			angs=VectorMath.anglesOf(axis);	
			
			
			
			float[][] yrot= MatrixMath.rotation3D(0, angs[1], 0);
			float[][] xrot= MatrixMath.rotation3D(angs[0], 0, 0);
			float[][] xrote = MatrixMath.rotation3D(-angs[0], 0, 0);
			float[][] yrote = MatrixMath.rotation3D(0, -angs[1], 0);
			
			float[][] trans=MatrixMath.translation3D(location[0], location[1], location[2]);
			float[][] transe=MatrixMath.translation3D(-location[0], -location[1], -location[2]);
			
			
			float[][] matx=MatrixMath.identityMatrix(4,4);
			matx=MatrixMath.multiplyMatracies(matx, xrot);
			matx=MatrixMath.multiplyMatracies(matx, yrot);
			//System.out.println(VectorMath.vectorToString(VectorMath.vectorFromColumnVectorMatrix(
			//		MatrixMath.multiplyMatracies(matx, MatrixMath.matrixFromColumnVector(new float[]{axis[0],axis[1],axis[2],1})))));
			matx=MatrixMath.multiplyMatracies(matx, MatrixMath.rotation3D(0, 0, radians));
			
			matx=MatrixMath.multiplyMatracies(matx, yrote);
			matx=MatrixMath.multiplyMatracies(matx, xrote);
			
			
			
			float[][] mat=MatrixMath.rotation3D(properties[3], properties[4], properties[5]);
			float[][] tranu=MatrixMath.translation3D(properties[0], properties[1], properties[2]);
			matrix=MatrixMath.multiplyMatracies(matrix, trans);
			matrix=MatrixMath.multiplyMatracies(MatrixMath.invertMatrix(tranu), matrix);
			applyMatrix(MatrixMath.invertMatrix(mat));
			applyMatrix(matx);
			applyMatrix(mat);
			matrix=MatrixMath.multiplyMatracies(tranu, matrix);
			matrix=MatrixMath.multiplyMatracies(matrix, transe);
		}
	}
	
	protected void preRender(){
		//transformation
		GL11.glPushMatrix();
		/*GL11.glTranslatef(x, y, z);
		GL11.glRotatef(xRotation, 1, 0, 0);
		GL11.glRotatef(yRotation, 0, 1, 0);
		GL11.glRotatef(zRotation, 0, 0, 1);
		GL11.glScalef(xScale, yScale, zScale);*/
		GL11.glMultMatrix(MatrixMath.matrixToFloatBuffer(matrix));
		//texture
		if(textureId!=-1){
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		}else{
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
	}
	
	protected void postRender(){
		//transformation
		GL11.glPopMatrix();
		//texture
		if(textureId!=-1){
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
	}
	
	public boolean contains(float[] point){
		if(bounds!=null){
			return bounds.contains(point);
		}else{
			return false;
		}
	}
	
	public void addChild(Object3D object) {
		if(!hasChild(object)){
			children.add(object);
			object.parent=this;
		}
	}

	public void removeChild(Object3D object) {
		if(hasChild(object)){
			children.remove(object);
			object.parent=null;
		}
	}

	public boolean hasChild(Object3D object) {
		return children.contains(object);
	}

	public void removeAllChildren() {
		for(int n=children.size()-1;n>=0;n--){
			removeChild(children.get(n));
		}
	}


	public float[][] getTransformationMatrix() {
		if(parent==null){
			return matrix;
		}else{
			//System.out.println(MatrixMath.matrixToString(matrix)+"\n-");
			return MatrixMath.multiplyMatracies(parent.getTransformationMatrix(), matrix);
		}
	}
	
	public float[] localToGlobal(float[] point){
		float[][] transformation=getTransformationMatrix();
		float[][] product=MatrixMath.multiplyMatracies(transformation,
			MatrixMath.matrixFromColumnVector(new float[]{point[0],point[1],point[2],1}));
		return VectorMath.homogenizeVector(VectorMath.vectorFromColumnVectorMatrix(product));
	}
	
	public float[] globalToLocal(float[] point){
		float[][] transformation=MatrixMath.invertMatrix(getTransformationMatrix());
		float[][] product=MatrixMath.multiplyMatracies(transformation,
			MatrixMath.matrixFromColumnVector(new float[]{point[0],point[1],point[2],1}));
		return VectorMath.homogenizeVector(VectorMath.vectorFromColumnVectorMatrix(product));
	}
	
}
