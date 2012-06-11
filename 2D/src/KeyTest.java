import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;




public class KeyTest extends Core implements KeyListener{
	
	public static void main(String[] args){
		new KeyTest().run();
	}
	
	private String mess = "";
	
	//call init from superclass & init more stuff
	public void init(){ 
		super.init();
		Window w = S.getFSWindow();
		w.setFocusTraversalKeysEnabled(false);
		w.addKeyListener(this);
		mess = "press escape to exit";
	}
	
	
	//draw
	public synchronized void draw(Graphics2D g) {
		Window w = S.getFSWindow();
		g.setColor(w.getBackground());
		g.fillRect(0,0,S.getWidth(),S.getHeight());
		g.setColor(w.getForeground());
		g.drawString(mess,350,250);
	}
	
	@Override
	public void update(long timePassed){
		
	}

	//key pressed
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		
		if(key == KeyEvent.VK_ESCAPE){
			stop();
		}else{
			mess = "Pressed : " + KeyEvent.getKeyText(key);
			e.consume();
		}
	}

	//key released
	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		
		mess = "Released : " + KeyEvent.getKeyText(key);
		e.consume();
	}

	//useless trollolololo
	public void keyTyped(KeyEvent e) {e.consume();}

}
