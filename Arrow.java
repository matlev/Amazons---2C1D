
public class Arrow extends Gamepiece{

	public final byte val;
	
	public Arrow(byte x, byte y) {
		super(x, y);
		val = -1;
	}
	
	public Arrow(char x, byte y) {
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
	
	public byte val() {
		return val;
	}
	
}
