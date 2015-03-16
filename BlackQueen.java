
public class BlackQueen extends Gamepiece{
	
	public final byte val;
	
	public BlackQueen(byte x, byte y) {
		super(x, y);
		val = 2;
	}
	
	public BlackQueen(char x, byte y) {
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
