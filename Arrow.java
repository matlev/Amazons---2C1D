
public class Arrow extends Gamepiece{

	public final int val;
	
	public Arrow(int x, int y) {
		super(x, y);
		val = -1;
	}
	
	public Arrow(char x, int y) {
		super(x, y);
		val = -1;
	}
	
	public Arrow(String c) {
		super(c);
		val = -1;
	}
	
	public String position() {
		return super.position();
	}
	
	public int val() {
		return val;
	}
	
}
