
import java.awt.Rectangle;


public class Platform {
	
	private Rectangle top;
	private int y;
	
	public Platform(int x, int y, int width){
		top = new Rectangle(x,y,width,10);
		this.y = y;
	}
	
	public Platform(int x, int y, int width, int height){
		top = new Rectangle(x,y - 4,width,9);
		
	}
	//retourne le dessus de la plateforme
	public Rectangle getTop(){ return top; }
	public void setTop(Rectangle top){ this.top = top;} 
	//retourne la coordonnée en y de la plateforme
	public int getTopY(){return y;}
	
}
