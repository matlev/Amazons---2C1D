
public class BlackQueen extends Gamepiece{
	
	public final int val;
	
	public BlackQueen(int x, int y) {
		super(x, y);
		val = 2;
	}
	
	public BlackQueen(char x, int y) {
		super(x, y);
		val = 2;
	}
	
	public BlackQueen(String c) {
		super(c);
		val = 2;
	}
	
	public String position() {
		return super.position();
	}
	
	public int val() {
		return val;
	}
	
	public void move(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void move(char x, int y) {
		this.x = (x > 96 ? x - 97 : x - 65);
		this.y = y;
	}
	
	public void move(String c) {
		int to_x = c.charAt(0);
		this.x = (to_x > 96 ? to_x - 97 : to_x - 65);
		this.y = Integer.parseInt(c.substring(1)) - 1;
	}
}
