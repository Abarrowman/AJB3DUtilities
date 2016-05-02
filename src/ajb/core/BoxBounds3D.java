package ajb.core;

public class BoxBounds3D extends Bounds3D {

	public float width=1;
	public float height=1;
	public float length=1;
	
	@Override
	public boolean contains(float[] point) {
		if(point[0]>=x&&point[0]<=x+width&&point[1]>=y&&point[1]<=y+height&&point[2]>=z&&point[2]<=z+length){
			return true;
		}else{
			return false;
		}
	}

}
