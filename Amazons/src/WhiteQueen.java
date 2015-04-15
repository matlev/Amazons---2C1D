/****************************************************
 * 
 * @author Mathew Levasseur
 *
 ****************************************************/

public class WhiteQueen extends Gamepiece{

	public final byte val;
	private float alpha;
	
	public WhiteQueen(byte x, byte y) {
		super(x, y);
		val = 1;
		alpha = -1;
	}
	
	public WhiteQueen(char x, byte y) {
		super(x, y);
		val = 1;
		alpha = -1;
	}
	
	public WhiteQueen(String c) {
		super(c);
		val = 1;
		alpha = -1;
	}
	
	public String stringPosition() {
		return super.stringPosition();
	}
	
	public int[] position() {
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
	
	public float getAlpha() {
		return alpha;
	}
	
	public void calculateAlpha(Gamepiece[][] board) {
        int xOff, yOff;
        float val = 0f;
        int stepSize = 1;
        for(int[] offset : MATRIX){
            for(yOff = y+offset[0], xOff = x+offset[1]; (yOff > -1 && yOff < 10) && (xOff > -1 && xOff < 10); yOff += offset[0], xOff += offset[1]){
                if(board[yOff][xOff] instanceof Blank){
                    if(((Blank)board[yOff][xOff]).bq < 127) {
                        val += Math.pow(2, -(stepSize - 1)) * (int)((Blank)board[yOff][xOff]).emptyNeighbours;
                    }
                    stepSize++;
                } else {
                    break;
                }
            }
            stepSize = 1;
        }
		alpha = val;
   }
	
}
