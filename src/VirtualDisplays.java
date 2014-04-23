import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamLockException;


public class VirtualDisplays implements Runnable, MouseListener {

	JFrame window;
	ImageDisplay panel;
	Webcam webcam;
	
	RegionDetector rd;
	
	Dimension windowSize = new Dimension(600, 400);
	
	public static final int WEBCAM = 0;
	public static final int FILE = 1;
	int inputMode = WEBCAM;
	
	boolean running = true;
	
	public VirtualDisplays() {
		//load opencv
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		initDisplay();
		initInput();
		
		//add mouselistener to panel
		panel.addMouseListener(this);
	}
	
	public void initDisplay() {
		window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLayout(new BorderLayout());
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		
		panel = new ImageDisplay();
		window.add(panel, BorderLayout.PAGE_START);
	}
	
	public void initInput() {
		if (inputMode == WEBCAM) {
			webcam = Webcam.getDefault();
		} else {
			System.out.println("non webcam mode unimplemented.");
		}
	}
	
	public void start() {
		new Thread(this).start();
	}
	
	public void init() {
		rd = new RegionDetector();
		
		if (inputMode == WEBCAM) {
			while (!webcam.isOpen()) {
				try {
					webcam.open();
				} catch (WebcamLockException e) {
					System.out.println("Waiting for webcam to be freee...");
					webcam.close();
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e2) {
						e2.printStackTrace();
					}
				}
			}
			panel.changeSize(webcam.getImage().getWidth()*2, webcam.getImage().getHeight()*2);
		}
		
		window.setSize(windowSize);
	}
	
	public void run() {
		init();
		
		long lastTime = System.currentTimeMillis();
		double delta = 0;
		double frameCount = 0;
		
		while (running) {
			long now = System.currentTimeMillis();
			delta += now - lastTime;
			frameCount++;
			lastTime = now;
			
			//getFrame
			BufferedImage img = getFrame();
			
			Mat mat = imageConvert(img);

	        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
	        
			//regionDetect
			mat = rd.detect(mat);
			
			//regionOverlay
			Mat zone = new Mat();
			
			if (rd.regions.size() >= 1) {
				Mat pers = new Mat(3, 3, CvType.CV_32FC2);
				
//				Mat src = rd.regions.get(0).contour;
//				src.convertTo(src, CvType.CV_32F);
				
//				Mat src = new Mat();
//				rd.regions.get(0).contour.copyTo(src); //hard to create a matofpoint from scratch so steal it
//				src.get(0, 0)[0] = 0;  //point 1, x
//				src.get(0, 0)[1] = 0;  //point 1, y
//				src.get(1, 0)[0] = panel.getWidth();
//				src.get(1, 0)[1] = 0;
//				src.get(2, 0)[0] = 0;
//				src.get(2, 0)[1] = panel.getHeight();
//				src.get(3, 0)[0] = panel.getWidth();
//				src.get(3, 0)[1] = panel.getHeight();
//				src.convertTo(src, CvType.CV_32F);
				Mat dst = rd.regions.get(0).contour;
				dst.convertTo(dst, CvType.CV_32F);
				
				Rect bound = rd.regions.get(0).getBoundingRect();
				Point tl = new Point(bound.x, bound.y);
				Point br = new Point(bound.width, bound.height);
				MatOfPoint src = new MatOfPoint(tl, new Point(br.x,tl.y), br, new Point(tl.x,br.y));
				src.convertTo(src, CvType.CV_32F);

		        pers = Imgproc.getPerspectiveTransform(src, dst);
		        
		        zone.create(bound.size(), CvType.CV_32F);
		        
		        Imgproc.warpPerspective(imageConvert(webcam.getImage()), zone, pers, mat.size());
		        zone.convertTo(zone, CvType.CV_32F);
	        }
			
			//render
			render(imageConvert(mat));
//			render(img);
		}
	}
	
	public BufferedImage getFrame() {
		if (inputMode == WEBCAM) {
			return webcam.getImage();
		} else {
			System.out.println("not implemented");
			return null;
		}
	}
	
	public synchronized void stop() {
		running = false;
	}
	
	
	public void render(BufferedImage img) {
		panel.setImage(img);		
		panel.update(window.getGraphics());
	}
	
	public static void main(String[] args) {
		new VirtualDisplays().start();
	}

	public void mouseClicked(MouseEvent arg0) {
		System.out.println("click");
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent arg0) {
	}

	public void mouseReleased(MouseEvent arg0) {
	}
	
	public BufferedImage imageConvert(Mat src) {
		Mat image_tmp = src;
		
	    MatOfByte matOfByte = new MatOfByte();

	    MatOfInt params = new MatOfInt(Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGB);

	    Highgui.imencode(".jpg", image_tmp, matOfByte, params);

	    byte[] byteArray = matOfByte.toArray();
	    
	    try {
	        InputStream in = new ByteArrayInputStream(byteArray);
	        return ImageIO.read(in);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
//	public BufferedImage imageConvert(Mat matBGR){  
//	      int width = matBGR.width(), height = matBGR.height(), channels = matBGR.channels() ;  
//	      byte[] sourcePixels = new byte[width * height * channels];  
//	      matBGR.get(0, 0, sourcePixels);  
//	      // create new image and get reference to backing data  
//	      BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);  
//	      final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();  
//	      System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);  
//	      return image;  
//	}  
	
	/*
	 * 
	 * long startTime = System.nanoTime();
	 * stuff here
      long endTime = System.nanoTime();  
      System.out.println(String.format("Elapsed time: %.2f ms", (float)(endTime - startTime)/1000000));  
	 * 
	 */
	
	public Mat imageConvert(BufferedImage src) {
		byte[] data = ((DataBufferByte) src.getRaster().getDataBuffer()).getData();
		Mat mat = new Mat(src.getHeight(), src.getWidth(), CvType.CV_8UC3);
		mat.put(0, 0, data);
		return mat;
	}
	
}
