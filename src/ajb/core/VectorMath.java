package ajb.core;

import java.util.Arrays;
import java.util.Vector;

public class VectorMath {
	
	public static final float[] upVector3D = new float[]{0,1,0};
	
	public static float[] cloneVector(float[] vector){
		float[] clone=new float[vector.length];
		for(int n=0;n<vector.length;n++){
			clone[n]=vector[n];
		}
		return clone;
	}
	
	public static String hashVector3D(float[] vector){
		if(vector.length==3){
			float x=Math.round(vector[0]*1000f)/1000f;
			float y=Math.round(vector[1]*1000f)/1000f;
			float z=Math.round(vector[2]*1000f)/1000f;
			return "x:"+x+"y:"+y+"z:"+z;
		}else{
			return "null";
		}
	}
	
	public static float[] homogenizeVector(float[] vector){
		if(vector.length>=2){
			int len=vector.length-1;
			float[] homo=new float[len];
			float hetero=vector[len];
			for(int n=0;n<homo.length;n++){
				homo[n]=vector[n]/hetero;
			}
			return homo;
		}else{
			return null;
		}
	}
	
	public static float[] anglesOf(float[] vector){
		if(vector.length==3){
			return new float[]{(float)-Math.atan2(vector[1],vector[2]),
					(float)Math.atan2(vector[0],Math.sqrt(vector[2]*vector[2]+vector[1]*vector[1]))};
		}else{
			return null;
		}
	}
	
	public static float angleBetween(float[] vector1, float[] vector2){
		if(vector1.length==3&&vector2.length==3){
			return (float)Math.acos((double)(dotProduct(vector1, vector2)/(getMagnitude(vector1)*getMagnitude(vector2))));
		}else{
			return Float.NaN;
		}
	}
	
	public static float[] subVector(float[] vector, int start, int length){
		float len=vector.length;
		if(start+length<=len&&start>=0){
			float[] sub=new float[length];
			for(int n=0;n<length;n++){
				sub[n]=vector[n+start];
			}
			return sub;
		}else{
			return null;
		}
	}
	
	public static int indexOfVector(Vector<float[]> list, float[] vector) {
		for (int n = 0; n < list.size(); n++) {
			if (Arrays.equals(list.get(n), vector)) {
				return n;
			}
		}
		return -1;
	}

	public static String vectorToString(float[] vector) {
		String string = "";
		for (int n = 0; n < vector.length; n++) {
			string += vector[n];
			if (n < vector.length - 1) {
				string += ",";
			}
		}
		return string;
	}

	public static float distance(float[] vector1, float[] vector2) {
		float distance = 0;
		for (int n = 0; n < vector1.length && n < vector2.length; n++) {
			distance += Math.pow(vector1[n] - vector2[n], 2);
		}
		return (float) Math.sqrt(distance);
	}

	public static float[] addVectors(float[] vector1, float[] vector2) {
		float[] sum = new float[Math.max(vector1.length, vector2.length)];
		for (int n = 0; n < sum.length; n++) {
			sum[n] = 0;
			if (n < vector1.length) {
				sum[n] += vector1[n];
			}
			if (n < vector2.length) {
				sum[n] += vector2[n];
			}
		}
		return sum;
	}

	/**
	 * Finds the difference between two vectors.
	 * @param vector1 The vector being subtracted from.
	 * @param vector2 The vector being subtracted.
	 * @return A vector equal to the difference between vector1 and vector2.
	 */
	public static float[] subtractVectors(float[] vector1, float[] vector2) {
		float[] difference = new float[Math.max(vector1.length, vector2.length)];
		for (int n = 0; n < difference.length; n++) {
			difference[n] = 0;
			if (n < vector1.length) {
				difference[n] += vector1[n];
			}
			if (n < vector2.length) {
				difference[n] -= vector2[n];
			}
		}
		return difference;
	}

	public static float absVectorDifference(float[] vector1, float[] vector2) {
		float difference = 0;
		int len = Math.min(vector1.length, vector2.length);
		for (int n = 0; n < len; n++) {
			difference += Math.abs(vector1[n] - vector2[n]);
		}
		if (vector1.length > vector2.length) {
			for (int n = len + 1; n < vector1.length; n++) {
				difference += Math.abs(vector1[n]);
			}
		} else {
			for (int n = len + 1; n < vector2.length; n++) {
				difference += Math.abs(vector2[n]);
			}
		}
		return difference;
	}
	
	public static float[] absVector(float[] vector) {
		float[] abs = new float[vector.length];
		for(int n=0;n<abs.length;n++){
			abs[n]=(float)Math.abs(vector[n]);
		}
		return abs;
	}

	public static float[] multiplyVector(float[] vector, float amount) {
		float[] product = new float[vector.length];
		for (int n = 0; n < product.length; n++) {
			product[n] = vector[n] * amount;
		}
		return product;
	}

	public static float[] divideVectors(float[] vector, float denomenator) {
		float[] quotient = new float[vector.length];
		for (int n = 0; n < quotient.length; n++) {
			quotient[n] = vector[n] / denomenator;
		}
		return quotient;
	}

	public static float[] divideVectors(float[] vector1, float[] vector2) {
		float[] quotient = new float[Math.max(vector1.length, vector2.length)];
		for (int n = 0; n < quotient.length; n++) {
			quotient[n] = 0;
			if (n < vector1.length) {
				quotient[n] += vector1[n];
			}
			if (n < vector2.length) {
				quotient[n] /= vector2[n];
			}
		}
		return quotient;
	}

	public static float[] normalize(float[] vector) {
		// find the vector magnitude
		float magnitude = getMagnitude(vector);
		// normalize the vector
		for (int n = 0; n < vector.length; n++) {
			vector[n] = vector[n] / magnitude;
		}
		// return the normalized vector
		return vector;
	}

	public static float getMagnitudeSquared(float[] vector) {
		float magnitudeSquared = 0;
		for (int n = 0; n < vector.length; n++) {
			magnitudeSquared += (float) Math.pow(vector[n], 2);
		}
		return magnitudeSquared;
	}

	public static float getMagnitude(float[] vector) {
		return (float) Math.sqrt(getMagnitudeSquared(vector));
	}
	
	public static float[] crossProduct(float[] vector1, float[] vector2){
		if(vector1.length==3&&vector2.length==3){
			float[] product=new float[3];
			product[0]=(vector1[1]*vector2[2])-(vector1[2]*vector2[1]);
			product[1]=(vector1[2]*vector2[0])-(vector1[0]*vector2[2]);
			product[2]=(vector1[0]*vector2[1])-(vector1[1]*vector2[0]);
			return product;
		}else{
			return null;
		}
	}
	
	public static float dotProduct(float[] vector1, float[] vector2){
		if(vector1.length==vector2.length){
			float sum=0f;
			for(int n=0;n<vector1.length;n++){
				sum+=vector1[n]*vector2[n];
			}
			return sum;
		}else{
			return Float.NaN;
		}
	}

	public static float[] reverse(float[] vector) {
		// reverse the vector
		float[] reverse = new float[vector.length];
		for (int n = 0; n < vector.length; n++) {
			reverse[n] = -vector[n];
		}
		// return the reversed vector
		return reverse;
	}
	
	public static float[] vectorFromColumnVectorMatrix(float[][] matrix){
		float[] vector=new float[matrix.length];
		for(int n=0;n<matrix.length;n++){
			vector[n]=matrix[n][0];
		}
		return vector;
	}

	public static float[] roundVector(float[] vector, int decimals) {
		float pow=(float)Math.pow(10,decimals);
		float[] rounded = new float[vector.length];
		for (int n = 0; n < rounded.length; n++) {
			rounded[n] = Math.round(vector[n]*pow)/pow;
		}
		return rounded;
	}
}
