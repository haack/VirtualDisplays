import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class RegionDetector {
	
	public ArrayList<Region> regions;
	
	int regionCount = 0;
	int targetEdgeCount = 4;
	
	public RegionDetector() {
		regions = new ArrayList<Region>();
	}
	
	public Mat detect(Mat input) {
		Mat img = new Mat();
		input.copyTo(img);
		
		preprocess(img);
		
		ArrayList<MatOfPoint> contours = findContours(img.clone());
		ArrayList<Point> contourCenter = filterContours(contours);

		track(contours, contourCenter);
		
		for (int i = 0; i < contours.size(); i++) {
        	for (int j = 0; j < contours.get(i).rows(); j++) {
        		Point vertex = new Point(contours.get(i).get(j, 0)[0], contours.get(i).get(j, 0)[1]);
        		Point nextVertex;
        		if (j == contours.get(i).rows() -1) {
            		nextVertex = new Point(contours.get(i).get(0, 0)[0], contours.get(i).get(0, 0)[1]);
        		} else {
            		nextVertex = new Point(contours.get(i).get(j+1, 0)[0], contours.get(i).get(j+1, 0)[1]);
        		}
                Core.circle(input, vertex, 5, new Scalar(100,100,100), 1);
                Core.line(input, vertex, nextVertex, new Scalar(1, 1, 1));
                Core.fillConvexPoly(input, contours.get(i), new Scalar(100, 100, 100, 0.7));
        	}
        }
		
		return input;
	}
	
	//already gray for efficiency
	private void preprocess(Mat img) {
		Size size = new Size(3, 3);
        
        Imgproc.GaussianBlur(img, img, size, 0);
        
        //already gray for speed purposes
//        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY);
        
        //size 15 seems better than 85
        Imgproc.adaptiveThreshold(img, img, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 10);

        Core.bitwise_not(img, img);
	}
	
	public ArrayList<MatOfPoint> findContours(Mat image) {
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat dump = new Mat();
        
        Mat thresh = new Mat();
        image.assignTo(thresh);
        Imgproc.findContours(thresh, contours, dump, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        
        //MOP2F is needed because of the stupid approxpolydp function. they definately need to overload it for double...
        MatOfPoint2f mMOP2f1 = new MatOfPoint2f();
        MatOfPoint2f approx = new MatOfPoint2f();
        
        for (int i = 0; i < contours.size(); i++) {
			contours.get(i).convertTo(mMOP2f1, CvType.CV_32FC2);
			Imgproc.approxPolyDP(mMOP2f1, approx, Imgproc.arcLength(mMOP2f1, true)*0.02, true);
            
            //Convert back to MatOfPoint and put the new values back into the contours list
            approx.convertTo(contours.get(i), CvType.CV_32S); //used to be CvType.CV_32S need F for warp
        }
        return contours;
	}
	
	public ArrayList<Point> filterContours(ArrayList<MatOfPoint> contours) {
		
		
		for (int i = 0; i < contours.size(); i++) {
            //iterate through contours and remove ones that dont have 4 vertices (i.e. not squares)
        	if (contours.get(i).rows() != targetEdgeCount) {
        		contours.remove(i);
        		i--;
        	}
        	else if (Math.abs(contours.get(i).get(0, 0)[0] - contours.get(i).get(2, 0)[0]) < 20) {
        		contours.remove(i);
        		i--;
        	}
        	else if (Math.abs(contours.get(i).get(0, 0)[1] - contours.get(i).get(2, 0)[1]) < 20) {
        		contours.remove(i);
        		i--;
        	}
        	
//        	for (int j = 0; j < contours.get(i).rows(); j++) {
//        		Point vertex = new Point(contours.get(i).get(j, 0)[0], contours.get(i).get(j, 0)[1]);
//        		Point nextVertex;
//        		Point prevVertex;
//        		if (j == contours.get(i).rows() -1) {
//            		nextVertex = new Point(contours.get(i).get(0, 0)[0], contours.get(i).get(0, 0)[1]);
//        		} else {
//            		nextVertex = new Point(contours.get(i).get(j+1, 0)[0], contours.get(i).get(j+1, 0)[1]);
//        		}
//        		
//        		if (j == 0) {
//        			prevVertex = new Point(contours.get(i).get(contours.get(i).rows()-1, 0)[0], contours.get(i).get(contours.get(i).rows()-1, 0)[1]); 	
//        		} else {
//            		prevVertex = new Point(contours.get(i).get(j-1, 0)[0], contours.get(i).get(j-1, 0)[1]);
//        		}
//        	}
        }
		
		ArrayList<Point> contourCenter = new ArrayList<Point>();
		//find average point in each contour
		for (int i = 0; i < contours.size(); i++) {
			Point total = new Point();
			int j;
	    	for (j = 0; j < contours.get(i).rows(); j++) {
	    		total.x += contours.get(i).get(j, 0)[0];
	    		total.y += contours.get(i).get(j, 0)[1];
	    	}
	    	total.x /= j-1;
    		total.y /= j-1;
//    		System.out.println("\nindex: " + i);
//    		System.out.println("x: " + total.x);
//    		System.out.println("y: " + total.y);
			contourCenter.add(i, total);
		}
		
		//remove contours with duplicate centre points
		for (int i = 0; i < contours.size(); i++) {
			for (int j = 0; j < contours.size(); j++) {
				if (i < contours.size()) {
					if (i != j) {
						if (Math.abs(contourCenter.get(i).x - contourCenter.get(j).x) < 10) {
							if (Math.abs(contourCenter.get(i).y - contourCenter.get(j).y) < 10) {
//								System.out.println("\n\nGOTCHA\n\n");
								//remove if smaller (if larger it will be removed on checking other node)
								if (Math.abs(contours.get(i).get(0, 0)[0] - contours.get(i).get(1, 0)[0]) < Math.abs(contours.get(j).get(0, 0)[0] - contours.get(j).get(1, 0)[0])) {
									contours.remove(j);
									contourCenter.remove(j);
					        		j--;
								}
							}
						}
					}
				}
			}
		}
		return contourCenter;
	}
	
	public void track(ArrayList<MatOfPoint> contours, ArrayList<Point> contourCenter) {
		for (int i = 0; i < contours.size(); i++) {
			boolean matched = false;
			outerloop:
			for (int j = 0; j < regions.size(); j++) {
				if (regions.get(j).isMatch(contours.get(i), contourCenter.get(i))) {
					//do matching stuff
					matched = true;
					break outerloop;
				}
			}
			if (!matched) {
				//add new region
				regions.add(new Region(regionCount, contourCenter.get(i)));
				regionCount++;
			}
		}
	}
}
