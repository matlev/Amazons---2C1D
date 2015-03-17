
public class BlackQueen extends Gamepiece{
	
	public final byte val;
	private float alpha;
	
	public BlackQueen(byte x, byte y) {
		super(x, y);
		val = 2;
		alpha = -1;
	}
	
	public BlackQueen(char x, byte y) {
		super(x, y);
		val = 2;
		alpha = -1;
	}
	
	public BlackQueen(String c) {
		super(c);
		val = 2;
		alpha = -1;
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
	
	public float getAlpha() {
		return alpha;
	}
	
	public void calculateAlpha(Gamepiece[][] board) {
		// Get this queen's x and y coordinates
		int x = super.x;
		int y = super.y;
		
		// Convenience booleans to check bounds and obstacles
		boolean done = false, U = false, D = false, L = false, R = false, UR = false, DL = false, UL = false, DR = false;
		int stepSize = 1;
		
		// Temp variable to store the sum of the scores returned from each square
		float val = 0f;
		
		// Check each direction from this queen's location and for each square b add (2^-(this queen's king moves to b - 1))*N(b) IFF b.bq < 127
		do {
			// Check that every direction from this queen has been checked as far as possible
			int up = y + stepSize;
			int down = y - stepSize;
			int right = x + stepSize;
			int left = x - stepSize;
			
			if(up > 9) {
				U = true;
				UR = true;
				UL = true;
			}
			
			if(down < 0) {
				D = true;
				DL = true;
				DR = true;
			}
			
			if(left < 0) {
				L = true;
				UL = true;
				DL = true;
			}
			
			if(right > 9) {
				R = true;
				UR = true;
				DR = true;
			}
			
			if(U && D && L && R && UR && UL && DR && DL) {
				done = true;
			}
			
			
			// Need to look up
			if(!U) {
				if(board[up][x].val() != 0) {
					U = true;
				} else if(((Blank)board[up][x]).wq < 127) {
					// stepSize serves as this queen's King Move Distance to this square
					val += Math.pow(2, -(stepSize - 1)) * (int)((Blank)board[up][x]).emptyNeighbours;
				}
			}
			
			// Need to look down
			if(!D) {
				if(board[down][x].val() != 0) {
					D = true;
				} else if(((Blank)board[down][x]).wq < 127) {
					// stepSize serves as this queen's King Move Distance to this square
					val += Math.pow(2, -(stepSize - 1)) * (int)((Blank)board[down][x]).emptyNeighbours;
				}
			}
			
			// Need to look left
			if(!L) {
				if(board[y][left].val() != 0) {
					L = true;
				} else if(((Blank)board[y][left]).wq < 127) {
					// stepSize serves as this queen's King Move Distance to this square
					val += Math.pow(2, -(stepSize - 1)) * (int)((Blank)board[y][left]).emptyNeighbours;
				}
			}
			
			// Need to look right
			if(!R) {
				if(board[y][right].val() != 0) {
					R = true;
				} else if(((Blank)board[y][right]).wq < 127) {
					// stepSize serves as this queen's King Move Distance to this square
					val += Math.pow(2, -(stepSize - 1)) * (int)((Blank)board[y][right]).emptyNeighbours;
				}
			}
			
			// Need to look up-right
			if(!UR) {
				if(board[up][right].val() != 0) {
					UR = true;
				} else if(((Blank)board[up][right]).wq < 127) {
					// stepSize serves as this queen's King Move Distance to this square
					val += Math.pow(2, -(stepSize - 1)) * (int)((Blank)board[up][right]).emptyNeighbours;
				}
			}
			
			// Need to look up-left
			if(!UL) {
				if(board[up][left].val() != 0) {
					UL = true;
				} else if(((Blank)board[up][left]).wq < 127) {
					// stepSize serves as this queen's King Move Distance to this square
					val += Math.pow(2, -(stepSize - 1)) * (int)((Blank)board[up][left]).emptyNeighbours;
				}
			}
			
			// Need to look down-left
			if(!DL) {
				if(board[down][left].val() != 0) {
					DL = true;
				} else if(((Blank)board[down][left]).wq < 127) {
					// stepSize serves as this queen's King Move Distance to this square
					val += Math.pow(2, -(stepSize - 1)) * (int)((Blank)board[down][left]).emptyNeighbours;
				}
			}
			
			// Need to look down-right
			if(!DR) {
				if(board[down][right].val() != 0) {
					DR = true;
				} else if(((Blank)board[down][right]).wq < 127) {
					// stepSize serves as this queen's King Move Distance to this square
					val += Math.pow(2, -(stepSize - 1)) * (int)((Blank)board[down][right]).emptyNeighbours;
				}
			}
			
			stepSize++;
		} while(!done);
		
		alpha = val;
	}
}
