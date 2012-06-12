import java.awt.Rectangle;


public class Ladder extends Rectangle {
	

	private static final long serialVersionUID = 5996287707438685996L;
	Rectangle top;
	boolean fixedX = false;
	
	public Ladder(int x, int y, int width, int height){
		super(x,y,width,height);
		top = new Rectangle(x,y-5,width,5);
		if(width == 0) {
			this.x -= 3;
			this.width = 6;
			fixedX = true;
		}
	}
	
	public Ladder(int x, Platform plat, int width, int height){
		this(x,plat.getTop().y+plat.getTop().height-1,width,height);
		if(width == 0) {
			this.x -= 3;
			this.width = 6;
			fixedX = true;
		}
	}
	
	public Ladder(int x, int y, int width, Platform plat2){
		this(x,y,width,plat2.getTop().y-y-5);
		if(width == 0) {
			this.x -= 3;
			this.width = 6;
			fixedX = true;
		}
	}
	
	public Ladder(int x, Platform plat1, int width, Platform plat2){
		this(x,plat1.getTop().y+plat1.getTop().height-1,width,plat2.getTop().y-plat1.getTop().y+plat1.getTop().height-7);
		if(width == 0) {
			this.x -= 3;
			this.width = 6;
			fixedX = true;
		}
	}
	
	
	public Ladder(int x, int y, int width, int height, boolean fixedX){
		super(x,y,width,height);
		top = new Rectangle(x,y-5,width,5);
		this.fixedX = fixedX;
	}
	
	public Ladder(int x, Platform plat, int width, int height, boolean fixedX){
		this(x,plat,width,height);
		this.fixedX = fixedX;
	}
	
	public Ladder(int x, int y, int width, Platform plat2, boolean fixedX){
		this(x,y,width,plat2);
		this.fixedX = fixedX;
	}
	
	public Ladder(int x, Platform plat1, int width, Platform plat2, boolean fixedX){
		this(x,plat1,width,plat2);
		this.fixedX = fixedX;
	}
	
	
	
	public Rectangle getTop() { return top;}

}
