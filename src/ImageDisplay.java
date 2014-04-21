import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;


public class ImageDisplay extends JPanel{
	
	private Image img;
	Dimension size;
	
	public ImageDisplay() {
		changeSize(400, 200);
	}
	 
    public void paintComponent(Graphics g) {
    	g.drawImage(img, 0, 0, this.size.width, this.size.height, null);
    }
    
    public void changeSize(int width, int height) {
    	size = new Dimension(width, height);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);
    }
    
    public void changeSize(Dimension newSize) {
    	size = newSize;
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);
    }

	public void setImage(Image img) {
    	this.img = img;
    }
}
