
public class Gameboard {
		
		private WhiteQueen[] W_pieces;
		private BlackQueen[] B_pieces;
		private Gamepiece[][] board;
		
		// Set up the initial position of the pieces
		public Gameboard() {
			// White queens
			W_pieces = new WhiteQueen[4];
			W_pieces[0] = new WhiteQueen("d1");
			W_pieces[1] = new WhiteQueen("g1");
			W_pieces[2] = new WhiteQueen("a4");
			W_pieces[3] = new WhiteQueen("j4");
			
			// Black queens
			B_pieces = new BlackQueen[4];
			B_pieces[0] = new BlackQueen("a7");
			B_pieces[1] = new BlackQueen("j7");
			B_pieces[2] = new BlackQueen("d10");
			B_pieces[3] = new BlackQueen("g10");
			
			board = new Gamepiece[][]{
					{new Blank("a1", 3),new Blank("b1", 5),new Blank("c1", 4),W_pieces[0],new Blank("e1", 4),new Blank("f1", 4),W_pieces[1],new Blank("h1", 4),new Blank("i1", 5),new Blank("j1", 3)},
					{new Blank("a2", 5),new Blank("b2"),new Blank("c2", 7),new Blank("d2", 7),new Blank("e2", 7),new Blank("f2", 7),new Blank("g2", 7),new Blank("h2", 7),new Blank("i2"),new Blank("j2", 5)},
					{new Blank("a3", 4),new Blank("b3", 7),new Blank("c3"),new Blank("d3"),new Blank("e3"),new Blank("f3"),new Blank("g3"),new Blank("h3"),new Blank("i3", 7),new Blank("j3", 7)},
					{W_pieces[2],new Blank("b4", 7),new Blank("c4"),new Blank("d4"),new Blank("e4"),new Blank("f4"),new Blank("g4"),new Blank("h4"),new Blank("i4", 7),W_pieces[3]},
					{new Blank("a5", 4),new Blank("b5", 7),new Blank("c5"),new Blank("d5"),new Blank("e5"),new Blank("f5"),new Blank("g5"),new Blank("h5"),new Blank("i5", 7),new Blank("j5", 7)},
					{new Blank("a6", 4),new Blank("b6", 7),new Blank("c6"),new Blank("d6"),new Blank("e6"),new Blank("f6"),new Blank("g6"),new Blank("h6"),new Blank("i6", 7),new Blank("j6", 7)},
					{B_pieces[0],new Blank("b7", 7),new Blank("c7"),new Blank("d7"),new Blank("e7"),new Blank("f7"),new Blank("g7"),new Blank("h7"),new Blank("i7", 7),B_pieces[1]},
					{new Blank("a8", 4),new Blank("b8", 7),new Blank("c8"),new Blank("d8"),new Blank("e8"),new Blank("f8"),new Blank("g8"),new Blank("h8"),new Blank("i8", 7),new Blank("j8", 7)},
					{new Blank("a9", 5),new Blank("b9"),new Blank("c9", 7),new Blank("d9", 7),new Blank("e9", 7),new Blank("f9", 7),new Blank("g9", 7),new Blank("h9", 7),new Blank("i9"),new Blank("j9", 5)},
					{new Blank("a10", 3),new Blank("b10", 5),new Blank("c10", 4),B_pieces[2],new Blank("e10", 4),new Blank("f10", 4),B_pieces[3],new Blank("h10", 4),new Blank("i10", 5),new Blank("j10", 3)}
			};
			
		}
		
		// Allows the creation of a new board with a predefined position
		public Gameboard(Gamepiece[][] board, WhiteQueen[] wqs, BlackQueen[] bqs) {
			this.board = board;
			
			W_pieces = wqs;
			B_pieces = bqs;	
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
				Gamepiece pieceToMove = this.board[p_y][p_x];
				
				// Increment the empty neighbours count for all empty squares around the starting position, 
				// and count how many blanks there are.  Also decrement the empty squares around final position.
				int blanks = 0;
				for(int j = -1; j < 2; j++) {
					for(int i = -1; i < 2; i++) {
						if(!(((p_y + j) < 0) || ((p_y + j) > 9) || ((p_x + i) < 0) || ((p_x + i) > 9))) {
							if(this.board[p_y + j][p_x + i] instanceof Blank) {
								blanks++;
								((Blank)this.board[p_y + j][p_x + i]).incrementEmptyNeighbours();
							}
						}
						
						if(!(((to_y + j) < 0) || ((to_y + j) > 9) || ((to_x + i) < 0) || ((to_x + i) > 9))) {
							if(this.board[to_y + j][to_x + i] instanceof Blank) {
								((Blank) this.board[to_y + j][to_x + i]).decrementEmptyNeighbours();
							}
						}
					}
				}
				 System.out.println("Blanks: " + blanks);
				
				// Update the array of pieces so we can keep track of their location easier
				String pos = pieceToMove.position();
				if(pieceToMove instanceof WhiteQueen) {
					for(int i = 0; i < 4; i++) {
						if(W_pieces[0].position().equalsIgnoreCase(pos)) {
							W_pieces[0].move((byte)to_x, (byte)to_y);
						}
					}
				} else {
					for(int i = 0; i < 4; i++) {
						if(B_pieces[0].position().equalsIgnoreCase(pos)) {
							B_pieces[0].move((byte)to_x, (byte)to_y);
						}
					}
				}
				
				this.board[p_y][p_x] = new Blank((byte)p_x, (byte)p_y, blanks);
				this.board[to_y][to_x] = pieceToMove;
				
				return "" + (char)(to_x + 65) + "" + (to_y + 1);
			} else {
				return "";
			}
			
		}
		
		// Overloaded method to handle a-j column names instead of #'s
		public String movePiece(char p_x, byte p_y, char to_x, byte to_y) {
			// Convert letters to numbers (uppercase letters have a value from 65 - 90)
			byte px = (byte)(p_x > 96 ? p_x - 96 : p_x - 64);
			byte tox = (byte)(to_x > 96 ? to_x - 96 : to_x - 64);
			
			return movePiece(px, p_y, tox, to_y);
		}
		
		// Overloaded method to handle coordinate input (ex. a4 to d1)
		public String movePiece(String from, String to) {
			char init = from.charAt(0);
			char dest = to.charAt(0);
			
			byte p_x = (byte)((int)init > 96 ? (int)init - 96 : (int)init - 64);
			byte p_y = (byte)Integer.parseInt(from.substring(1));
			byte to_x = (byte)((int)dest > 96 ? (int)dest - 96 : (int)dest - 64);
			byte to_y = (byte)Integer.parseInt(to.substring(1));
			
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
				this.board[to_y][to_x] = new Arrow((byte)to_x, (byte)to_y);
				
				// Decrement the empty neighbours count for all empty spaces around the arrow.
				for(int j = -1; j < 2; j++) {
					for(int i = -1; i < 2; i++) {
						if(!(((to_y + j) < 0) || ((to_y + j) > 9) || ((to_x + i) < 0) || ((to_x + i) > 9))) {
							if(this.board[to_y + j][to_x + i] instanceof Blank) {
								((Blank) this.board[to_y + j][to_x + i]).decrementEmptyNeighbours();
							}
						}
					}
				}
				
				return "" + (char)(to_x + 65) + "" + (to_y + 1);
			}
			
			return "";
		}
		
		// Overloaded function for shooting an arrow to allow "natural" coordinates (index starts at 1)
		public String shootArrow(String from, String to) {
			char init = from.charAt(0);
			char dest = to.charAt(0);
			
			byte p_x = (byte)(init > 96 ? init - 96 : init - 64);
			byte p_y = (byte)Integer.parseInt(from.substring(1));
			byte to_x = (byte)(dest > 96 ? dest - 96 : dest - 64);
			byte to_y = (byte)Integer.parseInt(to.substring(1));
			
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
						if(this.board[p_y][p_x + i].val() != 0) { return ""; }
					} else if(this.board[p_y][p_x - i].val() != 0) { return ""; }
				}
			} else if(x_diff == 0) { // Check up and down
				for(int i = 1; i <= y_diff; i++) {
					if(to_y > p_y) {
						if(this.board[p_y + i][p_x].val() != 0) { return ""; }
					} else if(this.board[p_y - i][p_x].val() != 0) { return ""; }
				}
			} else { // Check diagonally
				for(int i = 1; i <= y_diff; i++) {
					// Moving northeast
					if(to_y > p_y && to_x > p_x) {
						if(this.board[p_y + i][p_x + i].val() != 0) { return ""; }
					}
					
					// Moving southeast
					if(to_y < p_y && to_x > p_x) {
						if(this.board[p_y - i][p_x + i].val() != 0) { return ""; }
					}
					
					// Moving northwest
					if(to_y > p_y && to_x < p_x) {
						if(this.board[p_y + i][p_x - i].val() != 0) { return ""; }
					}
					
					// Moving southwest
					if(to_y < p_y && to_x < p_x) {
						if(this.board[p_y - i][p_x - i].val() != 0) { return ""; }
					}
				}
			}
			
			return "valid";
		}
		
		public int getSquare(int x, int y) {
			if(x < 0 || x > 9 || y < 0 || y > 9) {
				return 0;
			} else {
				return this.board[y][x].val();
			}
		}
		
		public int getSquare(String c) {
			int x = c.charAt(0);
			x = (x > 96 ? x - 97 : x - 65);
			int y = Integer.parseInt(c.substring(1)) - 1;
			return getSquare(x, y);
		}
		
		// Get the board instance
		public Gamepiece[][] getBoard() {
			return this.board;
		}
		
		public WhiteQueen[] getWhiteQueens() {
			return this.W_pieces;
		}
		
		public BlackQueen[] getBlackQueens() {
			return this.B_pieces;
		}
		
		// Print out the board
		public String toString() {
			String b = "   ___ ___ ___ ___ ___ ___ ___ ___ ___ ___\n";
			for(int i = 9; i >= 0; i--){
				if(i < 9){b += (i + 1) + " |";}
				else{b += (i + 1) + "|";}
				
				for(int j = 0; j < 10; j++){
					int square = this.board[i][j].val();
					if(square == 0) {
						b += "___|";
					} else if(square == 2) {
						b += "_B_|";
					} else if(square == 1) {
						b += "_W_|";
					} else {
						b += "_X_|";
					}
				}
				b += "\n";
			}
			
			b += "    a   b   c   d   e   f   g   h   i   j\n";
			
			return b;
		
		}
		
		public String emptySquaresCount() {
			String b = "   ___ ___ ___ ___ ___ ___ ___ ___ ___ ___\n";
			for(int i = 9; i >= 0; i--){
				if(i < 9){b += (i + 1) + " |";}
				else{b += (i + 1) + "|";}
				
				for(int j = 0; j < 10; j++){
					int square = this.board[i][j].val();
					if(square == 0) {
						b += "_" + ((Blank)this.board[i][j]).emptyNeighbours + "_|";
					} else if(square == 2) {
						b += "_B_|";
					} else if(square == 1) {
						b += "_W_|";
					} else if(square == -1) {
						b += "_X_|";
					} 
				}
				b += "\n";
			}
			
			b += "    a   b   c   d   e   f   g   h   i   j\n";
			
			return b;
		}
}
