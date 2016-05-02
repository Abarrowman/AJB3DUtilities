package ajb.core;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

public class MatrixMath {
	
	public static FloatBuffer matrixToFloatBuffer(float[][] matrix){
		int width=matrix[0].length;
		int height=matrix.length;
		float[] vec=new float[width*height];
		for (int yn=0;yn<height;yn++){
			for(int xn=0;xn<width;xn++){
				vec[yn+xn*height]=matrix[yn][xn];
			}
		}
		FloatBuffer buf=BufferUtils.createFloatBuffer(width*height).put(vec);
		buf.flip();
		return buf;
	}
	
	public static String matrixToString(float[][] matrix) {
		int width=matrix[0].length;
		int height=matrix.length;
		String string = "";
		for (int yn=0;yn<height;yn++){
			for(int xn=0;xn<width;xn++){
				string+=matrix[yn][xn];
				if(xn!=width-1||yn!=height-1){
					string+=",";
				}
			}
			if(yn!=height-1){
				string+="\n";
			}
		}
		return string;
	}
	
	public static float[][] addMatracies(float[][] matrix1, float[][] matrix2){
		int width=matrix1[0].length;
		int height=matrix1.length;
		if(height==matrix2.length&&width==matrix2[0].length){
			float[][] sum=new float[height][];
			for(int yn=0;yn<height;yn++){
				sum[yn]=new float[width];
				for(int xn=0;xn<width;xn++){
					sum[yn][xn]=matrix1[yn][xn]+matrix2[yn][xn];
				}
			}
			return sum;
		}else{
			return null;
		}
	}
	
	public static float[][] subtractMatracies(float[][] matrix1, float[][] matrix2){
		int width=matrix1[0].length;
		int height=matrix1.length;
		if(height==matrix2.length&&width==matrix2[0].length){
			float[][] difference=new float[height][];
			for(int yn=0;yn<height;yn++){
				difference[yn]=new float[width];
				for(int xn=0;xn<width;xn++){
					difference[yn][xn]=matrix1[yn][xn]-matrix2[yn][xn];
				}
			}
			return difference;
		}else{
			return null;
		}
	}
	
	public static float[][] multiplyMatrix(float[][] matrix, float scalar){
		int width=matrix[0].length;
		int height=matrix.length;
		float[][] product=new float[height][];
		for(int yn=0;yn<height;yn++){
			product[yn]=new float[width];
			for(int xn=0;xn<width;xn++){
				product[yn][xn]=matrix[yn][xn]*scalar;
			}
		}
		return product;
	}
	
	public static float[][] multiplyMatracies(float[][] matrix1, float[][] matrix2){
		int width1=matrix1[0].length;
		int height1=matrix1.length;
		int width2=matrix2[0].length;
		int height2=matrix2.length;
		
		if(width1==height2){
			float[][] product=new float[height1][];
			for(int yn=0;yn<height1;yn++){
				product[yn]=new float[width2];
				for(int xn=0;xn<width2;xn++){
					product[yn][xn]=0;
					for(int zn=0;zn<height2;zn++){
						//multiply rows by columns
						product[yn][xn]+=matrix1[yn][zn]*matrix2[zn][xn];
					}
				}
			}
			return product;
		}else{
			return null;
		}
	}
	
	public static float[][] transposeMatrix(float[][] matrix){
		int width=matrix[0].length;
		int height=matrix.length;
		float[][] transposed=new float[width][];
		for(int yn=0;yn<width;yn++){
			transposed[yn]=new float[height];
			for(int xn=0;xn<height;xn++){
				transposed[yn][xn]=matrix[xn][yn];
			}
		}
		return transposed;
	}
	
	public static float[][] roundMatrix(float[][] matrix, int decimalPlace){
		int width=matrix[0].length;
		int height=matrix.length;
		float[][] rounded=new float[height][];
		float place=(float)Math.pow(10, decimalPlace-1);
		for(int yn=0;yn<height;yn++){
			rounded[yn]=new float[width];
			for(int xn=0;xn<width;xn++){
				rounded[yn][xn]=Math.round(matrix[yn][xn]*place)/place;
			}
		}
		return rounded;
	}
	
	public static float calculateMatrixDeterminant(float[][] matrix){
		int width=matrix[0].length;
		int height=matrix.length;
		if(width==height){
			float[][] rowEchelon=convertMatrixToRowEchelonForm(matrix);
			float product=1;
			for(int n=0;n<height;n++){
				product*=rowEchelon[n][n];
			}
			return product;
		}else{
			return Float.NaN;
		}
	}
	
	public static float[][] identityMatrix(int height, int width){
		float[][] identity = new float[height][];
		for(int yn=0;yn<height;yn++){
			identity[yn]=new float[width];
			for(int xn=0;xn<width;xn++){
				if(xn==yn){
					identity[yn][xn]=1;
				}else{
					identity[yn][xn]=0;
				}
			}
		}
		return identity;
	}
	
	public static float[][] zeroMatrix(int height, int width){
		float[][] empty = new float[height][];
		for(int yn=0;yn<height;yn++){
			empty[yn]=new float[width];
			for(int xn=0;xn<width;xn++){
				empty[yn][xn]=0;
			}
		}
		return empty;
	}
	
	public static float[][] translation3D(float x, float y, float z){
		float[][] matrix=identityMatrix(4,4);
		matrix[0][3]=x;
		matrix[1][3]=y;
		matrix[2][3]=z;
		return matrix;
	}
	
	public static float[][] rotation3D(float xRot, float yRot, float zRot){
		float[][] xRotMatrix=identityMatrix(4,4);
		xRotMatrix[1][1]=(float)(Math.cos(xRot));
		xRotMatrix[1][2]=(float)(-Math.sin(xRot));
		xRotMatrix[2][1]=(float)(Math.sin(xRot));
		xRotMatrix[2][2]=(float)(Math.cos(xRot));
		
		float[][] yRotMatrix=identityMatrix(4,4);
		yRotMatrix[0][0]=(float)(Math.cos(yRot));
		yRotMatrix[2][0]=(float)(-Math.sin(yRot));
		yRotMatrix[0][2]=(float)(Math.sin(yRot));
		yRotMatrix[2][2]=(float)(Math.cos(yRot));
		
		float[][] zRotMatrix=identityMatrix(4,4);
		zRotMatrix[0][0]=(float)(Math.cos(zRot));
		zRotMatrix[0][1]=(float)(-Math.sin(zRot));
		zRotMatrix[1][0]=(float)(Math.sin(zRot));
		zRotMatrix[1][1]=(float)(Math.cos(zRot));
		
		return multiplyMatracies(multiplyMatracies(xRotMatrix, yRotMatrix), zRotMatrix);
	}
	
	public static float[][] scale3D(float xScale, float yScale, float zScale){
		
		//return multiplyMatracies(identityMatrix(4,4),matrixFromColumnVector(new float[]{xScale,yScale,zScale,1}));
		float[][] scale = new float[4][];
		for(int yn=0;yn<4;yn++){
			scale[yn]=new float[4];
			for(int xn=0;xn<4;xn++){
				if(xn==yn){
					if(yn==0){
						scale[yn][xn]=xScale;
					}else if(yn==1){
						scale[yn][xn]=yScale;
					}else if(yn==2){
						scale[yn][xn]=zScale;
					}else if(yn==3){
						scale[yn][xn]=1;
					}
				}else{
					scale[yn][xn]=0;
				}
			}
		}
		return scale;
	}
	
	
	public static float[][] invertMatrix(float[][] matrix){
		int width=matrix[0].length;
		int height=matrix.length;
		if(Math.abs(calculateMatrixDeterminant(matrix))>Math.pow(10, -6)){
			float[][] augmented=new float[height][];
			for(int yn=0;yn<height;yn++){
				augmented[yn]=new float[width*2];
				for(int xn=0;xn<width*2;xn++){
					if(xn<width){
						augmented[yn][xn]=matrix[yn][xn];
					}else if(xn-width==yn){
						augmented[yn][xn]=1;
					}else{
						augmented[yn][xn]=0;
					}
				}
			}
			
			augmented=convertMatrixToReducedRowEchelonForm(augmented);
			//System.out.println("Augmented:\n"+matrixToString(augmented));
			
			float[][] inverse=new float[height][];
			for(int yn=0;yn<height;yn++){
				inverse[yn]=new float[width];
				for(int xn=0;xn<width;xn++){
					inverse[yn][xn]=augmented[yn][xn+width];
				}
			}
			return inverse;
		}else{
			return null;
		}
	}
	
	public static float[][] addRows(float[][] matrix, float coefficient, int from, int to){
		int width=matrix[0].length;
		int height=matrix.length;
		float[][] sum=new float[height][];
		for(int yn=0;yn<height;yn++){
			sum[yn]=new float[width];
			for(int xn=0;xn<width;xn++){
				sum[yn][xn]=matrix[yn][xn];
				if(yn==to){
					sum[yn][xn]+=coefficient*matrix[from][xn];
				}
			}
		}
		return sum;
	}
	
	public static float[][] multiplyRow(float[][] matrix, float coefficient, int row){
		int width=matrix[0].length;
		int height=matrix.length;
		float[][] product=new float[height][];
		for(int yn=0;yn<height;yn++){
			product[yn]=new float[width];
			for(int xn=0;xn<width;xn++){
				product[yn][xn]=matrix[yn][xn];
				if(yn==row){
					product[yn][xn]*=coefficient;
				}
			}
		}
		return product;
	}
	
	public static float[][] swapRows(float[][] matrix, int from, int to){
		int width=matrix[0].length;
		int height=matrix.length;
		float[][] swapped=new float[height][];
		for(int yn=0;yn<height;yn++){
			swapped[yn]=new float[width];
			for(int xn=0;xn<width;xn++){
				if(yn==from){
					swapped[yn][xn]=matrix[to][xn];
				}else if(yn==to){
					swapped[yn][xn]=matrix[from][xn];
				}else{
					swapped[yn][xn]=matrix[yn][xn];
				}
				
			}
		}
		return swapped;
	}
	
	public static float[][] convertMatrixToRowEchelonForm(float[][] matrix){
		int width=matrix[0].length;
		int height=matrix.length;
		float[][] rowEchelon=new float[height][];
		for(int yn=0;yn<height;yn++){
			rowEchelon[yn]=new float[width];
			for(int xn=0;xn<width;xn++){
				rowEchelon[yn][xn]=matrix[yn][xn];
			}
		}
		//swap zero rows to the bottom
		int lastNonZeroRow=height-1;
		for(int yn=lastNonZeroRow;yn>=0;yn--){
			boolean isZero=true;
			for(int xn=0;xn<width;xn++){
				if(rowEchelon[yn][xn]!=0){
					isZero=false;
					break;
				}
				
			}
			if(isZero){
				if(yn!=lastNonZeroRow){
					//System.out.println("Swap row "+yn+" and row "+lastNonZeroRow);
					rowEchelon=swapRows(rowEchelon, yn, lastNonZeroRow);
				}
				lastNonZeroRow--;
			}
		}
		
		if(lastNonZeroRow!=-1){
			int minY=0;
			for(int xn=0;xn<width;xn++){
				for(int yn=minY;yn<height;yn++){
					if(rowEchelon[yn][xn]!=0){
						//ensure row is at minimum y coordinate
						if(yn!=minY){
							//System.out.println("Swap row "+minY+" and row "+yn);
							rowEchelon=swapRows(rowEchelon, yn, minY);
						}
						//make all entries below the pivot in the same column 0 
						for(int ny=minY+1;ny<height;ny++){
							if(rowEchelon[ny][xn]!=0){
								rowEchelon=addRows(rowEchelon,-rowEchelon[ny][xn]/rowEchelon[minY][xn], minY, ny);
								//System.out.println("Add "+(-rowEchelon[ny][xn]/rowEchelon[minY][xn])+" times row "+minY+" to row "+ny);
								//System.out.println(MatrixMath.matrixToString(rowEchelon));
							}
						}
						//set minY of next pivot
						minY=minY+1;
						break;
					}
				}
			}
			//System.out.println("Now it's in row echelon form");
			//System.out.println(MatrixMath.matrixToString(rowEchelon));
			return rowEchelon;
		}else{
			//zero matrix
			return rowEchelon;
		}
	}
	
	public static float[][] convertMatrixToReducedRowEchelonForm(float[][] matrix){
		float[][] reducedRowEchelon=convertMatrixToRowEchelonForm(matrix);
		if(reducedRowEchelon!=null){
			int width=reducedRowEchelon[0].length;
			int height=reducedRowEchelon.length;
			//re find pivots
			int minX=0;
			for(int yn=0;yn<height;yn++){
				for(int xn=minX;xn<width;xn++){
					if(reducedRowEchelon[yn][xn]!=0){
						//make element 1
						//System.out.println("Multiply row "+yn+" by "+(1/reducedRowEchelon[yn][xn]));
						reducedRowEchelon=multiplyRow(reducedRowEchelon,1/reducedRowEchelon[yn][xn], yn);
						//make all entries in the same column 0
						for(int ny=0;ny<height;ny++){
							if(ny!=yn){
								if(reducedRowEchelon[ny][xn]!=0){
									//System.out.println("Add "+(-reducedRowEchelon[ny][xn]/reducedRowEchelon[yn][xn])+" times row "+yn+" to row "+ny);
									reducedRowEchelon=addRows(reducedRowEchelon,
									-reducedRowEchelon[ny][xn]/reducedRowEchelon[yn][xn],yn, ny);
								}
							}
						}
						//set minX of next pivot
						minX=xn+1;
						//break out of inner for loop
						break;
					}
				}
			}
			return reducedRowEchelon;
		}else{
			return null;
		}
	}
	
	public static float[][] matrixFromColumnVector(float[] vector){
		float[][] matrix=new float[vector.length][];
		for(int n=0;n<vector.length;n++){
			matrix[n]=new float[]{vector[n]};
		}
		return matrix;
	}
}
