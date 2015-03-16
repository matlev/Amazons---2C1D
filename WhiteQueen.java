
public class WhiteQueen extends Gamepiece{

	public final byte val;
	
	public WhiteQueen(byte x, byte y) {
		super(x, y);
		val = 1;
	}
	
	public WhiteQueen(char x, byte y) {
		super(x, y);
		val = 1;
	}
	
	public WhiteQueen(String c) {
		super(c);
		val = 1;
	}
	
	public String position() {
		return super.position();
	}
	
	public byte val() {
		return val;
	}
	
	public void move(byte x, byte y) {
		this.x = x;
		this.y = y;
	}
	
	public void move(char x, byte y) {
		this.x = (byte)(x > 96 ? x - 97 : x - 65);
		this.y = y;
	}
	
	public void move(String c) {
		int to_x = c.charAt(0);
		this.x = (byte)(to_x > 96 ? to_x - 97 : to_x - 65);
		this.y = (byte)(Integer.parseInt(c.substring(1)) - 1);
	}
}
