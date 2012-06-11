import java.awt.Rectangle;



public class Projectile extends Sprite{
	
	private boolean active;
	private int timer = 0;
	
	public Projectile(Animation a){
		setAnimation(a);
		active = true;
	}

	public Rectangle getArea(){
		return new Rectangle(getX(), getY(), getWidth(), getHeight());
	}
	public boolean isActive(){return active;}
	public void delete(){active = false;}
	public void activate(){active = true;}
	public void update(long timePassed){
		super.update(timePassed);
		timer+=timePassed;
		if(timer >= 400) delete();
		
	}
	
}
