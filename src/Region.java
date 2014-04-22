import org.opencv.core.Core;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;


public class Region {

	int id;
	boolean active;
	Point center; //store multiple?
	Point expected;
	MatOfPoint contour;
	double circumference;
	
	public Region(int id, MatOfPoint contour, Point center) {
		this.id = id;
		this.center = center;
		this.contour = contour;
		
		//calculate circumference
    	for (int j = 0; j < contour.rows(); j++) {
    		Point vertex = new Point(contour.get(j, 0)[0], contour.get(j, 0)[1]);
    		Point nextVertex;
    		if (j == contour.rows() -1) {
        		nextVertex = new Point(contour.get(0, 0)[0], contour.get(0, 0)[1]);
    		} else {
        		nextVertex = new Point(contour.get(j+1, 0)[0], contour.get(j+1, 0)[1]);
    		}
    		double xDelta = Math.abs(vertex.x - nextVertex.x);
    		double yDelta = Math.abs(vertex.y - nextVertex.y);
    		circumference = Math.pow(xDelta + yDelta, 2);
        }

		active = true;
		System.out.println("Region added ID:" + id);
		System.out.println("Circumference:" + circumference);
	}
	
	public boolean isMatch(MatOfPoint test, Point testCenter) {
		if (Math.abs(center.x - testCenter.x) < 50) {
			if (Math.abs(center.y - testCenter.y) < 50) {
				center = testCenter;
				contour = test;
	     		return true;
			}
     	}
		return false;
	}
	
	public double getCircumference() {
		return circumference;
	}
}
