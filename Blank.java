
public class Blank extends Gamepiece{

		private static final float KAPPA = 0.2f;
		public final byte val;
		public byte wq, bq, wk, bk, emptyNeighbours;
	
		public Blank(byte x, byte y) {
			super(x, y);
			wq = bq = wk = bk = 127; // Maximum positive value for a byte -> substitute for positive infinity
			val = 0;
			emptyNeighbours = 8;
		}
		
		public Blank(byte x, byte y, int nb) {
			super(x, y);
			wq = bq = wk = bk = 127; // Maximum positive value for a byte -> substitute for positive infinity
			val = 0;
			emptyNeighbours = (byte)nb;
		}
		
		public Blank(String c) {
			super(c);
			wq = bq = wk = bk = 127;
			val = 0;
			emptyNeighbours = 8;
		}
		
		public Blank(String c, int nb) {
			super(c);
			wq = bq = wk = bk = 127;
			val = 0;
			emptyNeighbours = (byte)nb;
		}
		
		public byte val() {
			return val;
		}
		
		public void setEmptyNeighbours(byte spaces) {
			emptyNeighbours = spaces;
		}
		
		public void incrementEmptyNeighbours() {
			int temp = emptyNeighbours;
			temp++;
			emptyNeighbours = (byte)temp;
		}
		
		public void decrementEmptyNeighbours() {
			int temp = emptyNeighbours;
			temp--;
			emptyNeighbours = (byte)temp;
		}
		
		// The minimum number of "Queen" moves it takes for a player to reach this square
		public void setQueenMoves(byte player, byte moves) {
			if(player == 1) {
				this.wq = moves;
			} else {
				this.bq = moves;
			}
		}
		
		// The minimum number of "King" moves it takes for a player to reach this square
		public void setKingMoves(byte player, byte moves) {
			if(player == 1) {
				this.wk = moves;
			} else {
				this.bk = moves;
			}
		}
		
		// Resets the king and queen move scores to infinity
		public void reset() {
			wq = bq = wk = bk = 127;
		}
		
		// Returns the local "Queen Move" score for this square.  This evaluation becomes more
		// and more important during the main game and gives very good estimates of the expected 
		// territory shortly before the filling phase.
		public float getQueenDelta(byte player) {
			float delta = 0;
			
			// Neither player can reach this square
			if(wq == 127 && bq == 127) {
				delta = 0f;
			}
			
			// White can reach the square first
			if(wq < bq) {
				delta = 1f;
			}
			
			// Black can reach the square first
			if(wq > bq) {
				delta = -1f;
			}
			
			// Black and white can reach the square at the same time, score in favor of whose turn it is right now
			if(Double.compare(wq, bq) == 0) {
				delta = (float)(Math.pow(-1.0d, (double)player)*KAPPA); // (-1^player)(1/5) | player = {1: white, 2: black}
			}
			
			return delta;
		}
		
		// Returns the local "King Move" score for this square.  This evaluation rewards balanced 
		// distributions of the player's amazons on the board or helps to hinder the opponent from 
		// reaching such a distribution. This is most important at the beginning of the game.
		public float getKingDelta(byte player) {
			float delta = 0;
			
			if(wk == 127 && bk == 127) {
				delta = 0f;
			}
			
			if(wk < bk) {
				delta = 1f;
			}
			
			if(wk > bk) {
				delta = -1f;
			}
			
			if(wk == bk) {
				delta = (float)(Math.pow((-1.0d), (double)player)*KAPPA); // (-1^player)(1/5) | player = {1: white, 2: black}
			}
			
			return delta;
		}
		
		// Evaluation function that rewards moves in earlier phases of the game that replace
		// clear local disadvantages by small disadvantages and small advantages by clear advantages.
		public float getLocalizedQueenScore() {
			return (float)(Math.pow(2.0, ((double)-wq)) - Math.pow(2, ((double)-bq))); // 2^(-wq) - 2^(-bk)
		}
		
		public float getLocalizedKingScore() {
			return Math.min(1, (float)(Math.max(-1, ((int)bk - (int)wk))/6)); // min(1, max(-1, (bk - wk)/6))
		}
		
		// Used to estimate how many moves are left until the filling stage of the game.  When
		// each player has their own territories, the sum of all of squares will be 0.
		public float getPartitionedScore() {
			if(wq == 127 || bq == 127) {
				return 0f;
			} else {
				return (float)Math.pow(2, (-1)*Math.abs((int)wq - (int)bq)); // 2^(-|wq - bq|)
			}
		}
}
