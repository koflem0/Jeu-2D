import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.*;


public class MouseInput extends Core implements KeyListener, MouseMotionListener, MouseListener, MouseWheelListener {

	
	public static String mess = "";
	
	public static void main(String[] args){
		new MouseInput().run();
	}
	
	public void init(){
		super.init();
		Window w = S.getFSWindow();
		w.addMouseListener(this);
		w.addMouseMotionListener(this);
		w.addMouseWheelListener(this);
		w.addKeyListener(this);
	}
	
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	
	public void mouseWheelMoved(MouseWheelEvent e) {
		mess = "you moved the mouse wheel";
	}
	
	public void mouseDragged(MouseEvent e) {
		mess = "you are dragging the mouse";
	}
	public void mouseMoved(MouseEvent e) {
		mess = "you moved the mouse";
	}
	
	public void mousePressed(MouseEvent e) {
		mess = "you pressed down the mouse button";
	}

	public void mouseReleased(MouseEvent e) {
		mess = "you released the mouse button";
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

	public synchronized void draw(Graphics2D g) {
		Window w = S.getFSWindow();
		g.setColor(w.getBackground());
		g.fillRect(0, 0, S.getWidth(), S.getHeight());
		g.setColor(w.getForeground());
		g.drawString(mess, 100, 100);
		
	}
	
	public void update(long timePassed){
		
	}
	

}
