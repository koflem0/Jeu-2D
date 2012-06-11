import java.awt.Rectangle;


public class Ladder extends Rectangle {
	

	private static final long serialVersionUID = 5996287707438685996L;
	Rectangle top;
	
	public Ladder(int x, int y, int width, int height){
		super(x,y,width,height);
		top = new Rectangle(x,y-5,width,5);
	}
	
	public Ladder(int x, Platform plat, int width, int height){
		this(x,plat.getTop().y+plat.getTop().height-1,width,height);
	}
	
	public Ladder(int x, int y, int width, Platform plat2){
		this(x,y,width,plat2.getTop().y-y-5);
	}
	
	public Ladder(int x, Platform plat1, int width, Platform plat2){
		this(x,plat1.getTop().y+plat1.getTop().height-1,width,plat2.getTop().y-plat1.getTop().y+plat1.getTop().height-7);
	}
	
	
	
	public Rectangle getTop() { return top;}

}
