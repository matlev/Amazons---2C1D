import java.math.*;

public class Gameboard {

		public final int BQ = 0x1; // A black queen
		public final int WQ = 0x2; // A white queen
		public final int ARROW = 0x3;      // An arrow
		
		private int[][] board;
		
		public Gameboard() {
			board = new int[][]{
				{0,0,0,WQ,0,0,WQ,0,0,0},
				{0,0,0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0,0,0},
				{WQ,0,0,0,0,0,0,0,0,WQ},
				{0,0,0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0,0,0},
				{BQ,0,0,0,0,0,0,0,0,BQ},
				{0,0,0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0,0,0},
				{0,0,0,BQ,0,0,BQ,0,0,0}
			};
		}
		
		// Accepts the x and y coordinates of the piece to move and where to move it, 
		// and returns the new position.  Checks for a legal move (diagonal or orthogonal,
		// with no objects obstructing the path)
		public String movePiece(int p_x, int p_y, int to_x, int to_y) {
			// Decremented to allow for more "natural" move input (coordinates start at 1 instead of 0)
			p_x--;
			p_y--;
			to_x--;
			to_y--;
			String move = checkLegalMove(p_x, p_y, to_x, to_y);
			
			if(move.equals("valid")) {
				int pieceToMove = this.board[p_x][p_y];
				this.board[p_y][p_x] = 0;
				this.board[to_y][to_x] = pieceToMove;
				
				return "" + (char)(to_x + 65) + "" + (to_y + 1);
			} else {
				return "";
			}
			
		}
		
		// Overloaded method to handle a-j column names instead of #'s
		public String movePiece(char p_x, int p_y, char to_x, int to_y) {
			int px = (int)p_x;
			int tox = (int)to_x;
			
			// Convert letters to numbers (uppercase letters have a value from 65 - 90)
			px = (px > 96 ? px - 96 : px - 64);
			tox = (tox > 96 ? tox - 96 : tox - 64);
			
			return movePiece(px, p_y, tox, to_y);
		}
		
		// Overloaded method to handle coordinate input (ex. a4 to d1)
		public String movePiece(String from, String to) {
			char init = from.charAt(0);
			char dest = to.charAt(0);
			
			int p_x = ((int)init > 96 ? (int)init - 96 : (int)init - 64);
			int p_y = Integer.parseInt(from.substring(1));
			int to_x = ((int)dest > 96 ? (int)dest - 96 : (int)dest - 64);
			int to_y = Integer.parseInt(to.substring(1));
			
			return movePiece(p_x, p_y, to_x, to_y);
		}
		
		// Shoots an arrow to a square, blocking it off
		public String shootArrow(int p_x, int p_y, int to_x, int to_y) {
			// Decremented to allow for more "natural" move input (coordinates start at 1 instead of 0)
			p_x--;
			p_y--;
			to_x--;
			to_y--;
			String move = checkLegalMove(p_x, p_y, to_x, to_y);
			
			if(move.equals("valid")) {
				this.board[to_y][to_x] = ARROW;
				
				return "" + (char)(to_x + 65) + "" + (to_y + 1);
			}
			
			return "";
		}
		
		// Overloaded function for shooting an arrow to allow "natural" coordinates (index starts at 1)
		public String shootArrow(String from, String to) {
			char init = from.charAt(0);
			char dest = to.charAt(0);
			
			int p_x = ((int)init > 96 ? (int)init - 96 : (int)init - 64);
			int p_y = Integer.parseInt(from.substring(1));
			int to_x = ((int)dest > 96 ? (int)dest - 96 : (int)dest - 64);
			int to_y = Integer.parseInt(to.substring(1));
			
			return shootArrow(p_x, p_y, to_x, to_y);
		}
		
		// Checks to see if the initial and destination squares are valid and a valid path exists between them
		private String checkLegalMove(int p_x, int p_y, int to_x, int to_y) {
			// Check for in bounds
			if(p_x < 0 || p_x > 9 || p_y < 0|| p_y > 9 || to_x < 0 || to_x > 9 || to_y < 0|| to_y > 9) {
				return "";
			}
			
			// Check for moving to same square
			if(p_x == to_x && p_y == to_y) {
				return "";
			}
			
			// Check for inappropriate movement (must be lateral or diagonal)
			int y_diff = Math.abs(to_y - p_y);
			int x_diff = Math.abs(to_x - p_x);
			if(!(p_x == to_x || p_y == to_y || x_diff == y_diff)) {
				return "";
			}
			
			// Check for objects in the path, including destination square
			if(y_diff == 0) { // Only need to check left and right
				for(int i = 1; i <= x_diff; i++) {
					if(to_x > p_x) {
						if(this.board[p_y][p_x + i] != 0) { return ""; }
					} else if(this.board[p_y][p_x - i] != 0) { return ""; }
				}
			} else if(x_diff == 0) { // Check up and down
				for(int i = 1; i <= y_diff; i++) {
					if(to_y > p_y) {
						if(this.board[p_y + i][p_x] != 0) { return ""; }
					} else if(this.board[p_y - i][p_x] != 0) { return ""; }
				}
			} else { // Check diagonally
				for(int i = 1; i <= y_diff; i++) {
					// Moving northeast
					if(to_y > p_y && to_x > p_x) {
						if(this.board[p_y + i][p_x + i] != 0) { return ""; }
					}
					
					// Moving southeast
					if(to_y < p_y && to_x > p_x) {
						if(this.board[p_y - i][p_x + i] != 0) { return ""; }
					}
					
					// Moving northwest
					if(to_y > p_y && to_x < p_x) {
						if(this.board[p_y + i][p_x - i] != 0) { return ""; }
					}
					
					// Moving southwest
					if(to_y < p_y && to_x < p_x) {
						if(this.board[p_y - i][p_x - i] != 0) { return ""; }
					}
				}
			}
			
			return "valid";
		}
		
		public int getSquare(int x, int y) {
			if(x < 0 || x > 9 || y < 0 || y > 9) {
				return 0;
			} else {
				return this.board[y][x];
			}
		}
		
		public int getSquare(String c) {
			int x = c.charAt(0);
			x = (x > 96 ? x - 97 : x - 65);
			int y = Integer.parseInt(c.substring(1)) - 1;
			return getSquare(x, y);
		}
		
		// Get the board instance
		public int[][] getBoard() {
			return this.board;
		}
		
		// Print out the board
		public void print() {
			System.out.println("   ___ ___ ___ ___ ___ ___ ___ ___ ___ ___ ");
			for(int i = 9; i >= 0; i--){
				if(i < 9){System.out.print((i + 1) + " |");}
				else{System.out.print((i + 1) + "|");}
				
				for(int j = 0; j < 10; j++){
					int square = this.board[i][j];
					if(square == 0) {
						System.out.print("___|");
					} else if(square == 0x1) {
						System.out.print("_B_|");
					} else if(square == 0x2) {
						System.out.print("_W_|");
					} else {
						System.out.print("_X_|");
					}
				}
				System.out.println();
			}
			
			System.out.println("    a   b   c   d   e   f   g   h   i   j");
		
		}
}
