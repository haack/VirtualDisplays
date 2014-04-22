import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;


public class Region {

	int id;
	boolean active;
	Point center; //store multiple?
	Point expected;
	MatOfPoint contour;
	double circumference;
	
	public Region(int id, Point center) {
		active = true;
		this.center = center;
		this.id = id;
		System.out.println("Region added ID:" + id);
	}
	
	public boolean isMatch(MatOfPoint test, Point testCenter) {
		//todo check y
		if (Math.abs(center.x - testCenter.x) < 20) {
			 System.out.println("MATCH id: "+ id);
			 center = testCenter;
     		return true;
     	}
		System.out.println("NOMATCH");
		return false;
	}
}
