/**
 * Based on http://www.cs.utah.edu/~aek/code/card.cpp
 */

import java.io.BufferedOutputStream; import java.io.FileOutputStream; import java.io.IOException;
import java.util.Random; import java.util.Vector;

public final class Raytracer {
	
		private final static RayVector TILE_WHITE  = new RayVector( 3,  3,  3); //floor pattern color 
		private final static RayVector TILE_BLACK  = new RayVector( 0,  0,  0); //floor pattern color
    	private static float t;
    	private static RayVector n;
		
		final static char[][] scene = {
	        	"                           ".toCharArray(),
		        "     1     1     1     1   ".toCharArray(),
		        "                           ".toCharArray(),
		        "                           ".toCharArray(),
		        "     1     1     1     1   ".toCharArray(),
		        "                           ".toCharArray(),
		        "                           ".toCharArray(),
		        "     1     1     1     1   ".toCharArray(),
		        "                           ".toCharArray(),
		        "                           ".toCharArray(),
		        "     1     1     1     1   ".toCharArray()
		};

    	static RayVector[] objects; // keeps track of the spheres in the scene
    	static BufferedOutputStream image; // the image printer
    	static byte[] bytes = new byte[3*512*512]; // byte array that gets sent to the image printer
      
        public static void main(String[] args) throws Exception {
        	
        	buildWorld();
        	image = new BufferedOutputStream(new FileOutputStream("image2.ppm"));
        	image.write("".format("P6 %d %d 255 ", 512, 512).getBytes()); // header for .ppm format
        	raytrace(new Random());
        	image.write(bytes);
        	image.flush();
        	image.close();

        }
        static void buildWorld() { // adds the 1's as objects to the world
        	final Vector<RayVector> tmp = new Vector<RayVector>(scene.length * scene[0].length);
                for (int k = scene[0].length - 1; k >= 0; k--) {
                        for (int j = scene.length - 1; j >= 0; j--) {
                                if (scene[j][scene[0].length - 1 - k] != ' ') {
                                	tmp.add(new RayVector(-k, 0, -(scene.length - 1 - j)));
                                }}}
                objects = tmp.toArray(new RayVector[0]);        
        }
        
        static int Tracer(RayVector origin, RayVector direction) { //traces a single ray, returns m=0 if day doesn't hit anything and goes up, m=1 if it goes down and m=2 if it hits
                t = Float.MAX_VALUE;
                n = new RayVector();
                int m = 0;
                float p = -origin.z / direction.z;
                if (0.02f < p) {
                        t = p; 
                        n = new RayVector(0, 0, 1); 
                        m = 1;
                }
                origin = origin.add(new RayVector(0, 3, -4));
                for(int i = 0; i < objects.length; i++) {
                        RayVector p1 = origin.add(objects[i]);
                        float b = p1.dot(direction);
                        float c = p1.dot(p1) - 1;
                        float b2 = b * b;
                        
                        if (b2 > c) {
                                float q = b2 - c;
                                float s = (float) (-b - Math.sqrt(q)); //distance from camera to sphere
                                if (s < t && s > 0.02f) {
                                        t = s; 
                                        n = (p1.add(direction.multiply(t))).norm(); 
                                        m = 2;
                }}}
                return m;
        }
        static RayVector Sampler(RayVector origin, RayVector direction, Random rand) { // builds the color of the pixel
                if (Tracer(origin, direction) == 0) //the ray didn't hit anything and is going up, so return the sky color
                	return new RayVector(0.7f, 0.6f, 1.0f).multiply((float) Math.pow(1-direction.z,4));

                RayVector h = origin.add(direction.multiply(t)); // coordinate of intersection point
                RayVector l = (new RayVector( 9+rand.nextFloat(), 9+rand.nextFloat(), 16).add(h.multiply(-1.f))).norm(); // light direction
                
                float b = l.dot(n); //lambertian factor
                if (b < 0 || Tracer(h, l) != 0) { // illumination factor
                        b = 0;
                }
                if (Tracer(origin, direction) == 1) { // the ray didn't hit anything and is going down, so return the floor color
                        h = h.multiply(0.2f); // controls size of tiles
                        return ((((int) (Math.ceil(h.x) + Math.ceil(h.y))) & 1) == 1 ? TILE_WHITE : TILE_BLACK).multiply(b * 0.2f + 0.1f); // this generates the grid pattern
                }
                RayVector on = new RayVector(n);
                RayVector r = direction.add(on.multiply(on.dot(direction.multiply(-2.f)))); //reflection vector from whatever was hit
                float p = (float)Math.pow(l.dot(r.multiply(b > 0 ? 1.f : 0.f)), 99.0); // calculate the color of the pixel
                return new RayVector(p, p, p).add(Sampler(h, r, rand)); // hit a sphere, call again with h as the origin and r as the reflection vector and add to current color
        }
        static final RayVector camera = (new RayVector(-10, -40, 0)).norm(); // The direction the camera is facing, x-y-z coords
        static final RayVector a = ((new RayVector(0, 0, 1)).pow(camera)).norm().multiply(.002f); // camera up vector
        static final RayVector b = (camera.pow(a)).norm().multiply(.002f); // camera right vector
        static final RayVector c = (a.add(b)).multiply(-256).add(camera); // offset from eye point

        public static void raytrace(Random rand) throws IOException {
        	for(int i = 512 ; i > 0; i--)
        	{
                int k = (512 - i) * 512 * 3;
        		for(int j = 512 ; j > 0; j--)
        		{ 
        			RayVector p = new RayVector(13, 13, 13); //color of pixel at (i,j) in the image
        			for (int r = 64; r > 0; r--) { // number of rays per pixel
                        RayVector t = a.multiply(rand.nextFloat()-.5f).multiply(99).add(b.multiply(rand.nextFloat()-.5f).multiply(99)); //adding the random values causes a slight blur effect
                        p = Sampler(new RayVector(17, 16, 8).add(t), t.multiply(-1).add((a.multiply(rand.nextFloat() + j).add(b.multiply(i + rand.nextFloat())).add(c)).multiply(16)).norm() , rand).multiply(3.5f).add(p); 
                   }
                    bytes[k++] = (byte) p.x;
                    bytes[k++] = (byte) p.y;
                    bytes[k++] = (byte) p.z;
        		}
        	}
        }
}
