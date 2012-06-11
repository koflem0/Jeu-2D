import java.awt.Rectangle;
import java.io.Serializable;


public class Stash implements Serializable{
	
	private static final long serialVersionUID = -2259455351989619924L;
	ItemSlot[][] stashSlots = new ItemSlot[8][8];
	Item[][][] items = new Item[4][8][8];
	PageButton[] pageButtons = new PageButton[4];
	private int currentPage = 0;
	private boolean open = false;
	public static final int x=50,y=38,width=500,height=600;
	
	
	public Stash(){
		for(int i = 0; i < pageButtons.length; i++){
			pageButtons[i] = new PageButton(i);
		}
		for(int i =0; i < 8; i++)
			for(int j = 0; j < 8; j++){
				stashSlots[i][j] = new ItemSlot(i,j,this);
			}
	}
	
	public void setPage(int i){currentPage = i;}
	
	public Item getItem(int i, int j){return items[currentPage][i][j];}
	public Item getItem(int page, int i, int j){return items[page][i][j];}
	
	public void delete(int i, int j){items[currentPage][i][j] = null;}
	public void delete(int page,int i, int j){items[page][i][j] = null;}
	
	public Rectangle getArea() {return new Rectangle(x,y,width,height);}
	
	public void setItem(Item item, int i, int j){
		items[currentPage][i][j]=item;
	}
	public void setItem(Item item, int page, int i, int j){
		items[page][i][j]=item;
	}
	public boolean add(Item item){
		
		for(int i = 0; i < 8; i++){
			for(int j = 0; j < 8; j++){
				
				if(items[currentPage][j][i]==null){items[currentPage][j][i]=item;return true;}
			}
		}

		return false;
	}
	
	public void toggle(){open = !open;}
	public boolean isOpen(){return open;}
	
	public int getPage(){return currentPage;}
	public void switchPage(int page){currentPage = page;}
	
	public class PageButton implements Serializable{
		
		private static final long serialVersionUID = 7418345511274381736L;
		int page;
		Rectangle area;
		
		public PageButton(int i){
			page = i;
			area = new Rectangle(x+20+110*i,y+20, 100, 50);
		}
		
		public void activate(){
			currentPage = page;
		}
	}
}
