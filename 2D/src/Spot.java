import java.awt.Point;
import java.awt.Rectangle;


public class Spot {
	
	private Point spawn, nextXY;
	private Rectangle area;
	private int nextMap;
	public boolean invisible = false;
	
	public Spot(Point spot, Point spawn, int nextMap, Point nextXY){
		this.area = new Rectangle(spot.x,spot.y,100,200);
		this.spawn = spawn;
		this.nextMap = nextMap;
		this.nextXY = nextXY;
	}
	public Spot(Point spot, Point spawn, int nextMap, Point nextXY, boolean invisible){
		this(spot,spawn,nextMap,nextXY);
		this.invisible = invisible;
	}
	
	//retourne le spawn du personnage s'il entre dans le téléporteur
	public Point getSpawn(){
		return spawn;
	}
	//retourne la zone du téléporteur
	public Rectangle getArea(){
		return area;
	}
	//retourne la prochaine map
	public int getNextMap(){
		return nextMap;
	}
	//retourne les coordonnées de la "caméra" de la prochaine map si le personnage entre dans ce téléporteur
	public Point getNextXY(){
		return nextXY;
	}
	
}
