import java.awt.Color;


public class RayVector {
	
	public float x;
	public float y;
	public float z;
	
	public RayVector()
	{
		x = 0.f;
		y = 0.f;
		z = 0.f;
	}
	
	public RayVector(RayVector v)
	{
		x=v.x;
		y=v.y;
		z=v.z;
	}
	
	public RayVector(float a, float b, float c)
	{
		x = a;
		y = b;
		z = c;
	}

	 public RayVector add(RayVector r) 
	 {
		 return new RayVector(x+r.x,y+r.y,z+r.z);
	 } 
     
	 public RayVector pow(final RayVector r) // cross product
     {	 
		 return new RayVector(y*r.z-z*r.y,z*r.x-x*r.z,x*r.y-y*r.x);
     } 
    

	 public float  dot(final RayVector r)  // dot product
	 {
		 return x*r.x+y*r.y+z*r.z;
	 } 
	 
     public RayVector multiply(float r) // scalar multiplication
     {
    	 return new RayVector(x*r,y*r,z*r);
     }      
     
     public RayVector norm()
     {
    	 return multiply((float)(1.f/Math.sqrt(dot(this))));
     } 

}

