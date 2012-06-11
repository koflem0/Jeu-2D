import java.awt.Point;


public class Effect extends Sprite{

	private long totalTime;
	private boolean active;
	
	public Effect(Point p, Animation a, long totalTime){
		super();
		
		super.setX((float) p.getX());
		super.setY((float) p.getY());
		super.setAnimation(a);
		this.totalTime = totalTime;
		active = true;
		
	}
	
	public void update(long timePassed){
		super.update(timePassed);
		totalTime -= timePassed;
		if(totalTime <= 0) active = false;
	}
	
	public boolean isActive(){return active;}
	
}
