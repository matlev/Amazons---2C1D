import java.util.ArrayList;
import java.util.Stack;


public class Gameboard {
		
		private static final double F_OMEGA_COEF = 1/(Math.E*45);
		private static final int WHITE = 1, BLACK = 2;
		private Stack<Object[]> moveHistory = new Stack();
		private WhiteQueen[] W_pieces;
		private BlackQueen[] B_pieces;
		private Gamepiece[][] board;
		
		// clockwise around position from top left;
		private final static byte[][] MATRIX = {{-1,-1},{-1,0},{-1,1},{0,1},{1,1},{1,0},{1,-1},{0,-1}};
		// check order to ensure maxiumum multiple fill rate.
		private final static byte[] ORDER = {4,5,3,6,2,7,1,0};
		// use with matrix to find common positions that need to be filled.
		private final static byte[][] COMMON = {{1,7},{0,2,7,3},{1,3},{1,2,5,4},{3,5},{3,4,6,7},{5,7},{0,1,5,6}};
		
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
					{new Blank("a3", 4),new Blank("b3", 7),new Blank("c3"),new Blank("d3"),new Blank("e3"),new Blank("f3"),new Blank("g3"),new Blank("h3"),new Blank("i3", 7),new Blank("j3", 4)},
					{W_pieces[2],new Blank("b4", 7),new Blank("c4"),new Blank("d4"),new Blank("e4"),new Blank("f4"),new Blank("g4"),new Blank("h4"),new Blank("i4", 7),W_pieces[3]},
					{new Blank("a5", 4),new Blank("b5", 7),new Blank("c5"),new Blank("d5"),new Blank("e5"),new Blank("f5"),new Blank("g5"),new Blank("h5"),new Blank("i5", 7),new Blank("j5", 4)},
					{new Blank("a6", 4),new Blank("b6", 7),new Blank("c6"),new Blank("d6"),new Blank("e6"),new Blank("f6"),new Blank("g6"),new Blank("h6"),new Blank("i6", 7),new Blank("j6", 4)},
					{B_pieces[0],new Blank("b7", 7),new Blank("c7"),new Blank("d7"),new Blank("e7"),new Blank("f7"),new Blank("g7"),new Blank("h7"),new Blank("i7", 7),B_pieces[1]},
					{new Blank("a8", 4),new Blank("b8", 7),new Blank("c8"),new Blank("d8"),new Blank("e8"),new Blank("f8"),new Blank("g8"),new Blank("h8"),new Blank("i8", 7),new Blank("j8", 4)},
					{new Blank("a9", 5),new Blank("b9"),new Blank("c9", 7),new Blank("d9", 7),new Blank("e9", 7),new Blank("f9", 7),new Blank("g9", 7),new Blank("h9", 7),new Blank("i9"),new Blank("j9", 5)},
					{new Blank("a10", 3),new Blank("b10", 5),new Blank("c10", 4),B_pieces[2],new Blank("e10", 4),new Blank("f10", 4),B_pieces[3],new Blank("h10", 4),new Blank("i10", 5),new Blank("j10", 3)}
			};
			
		}
		
		// Accepts the string coordinates for a queen's start and end position and where its arrow lands
		// Very slow and inefficient, use this for testing ONLY
		public boolean doMove(String start, String finish, String arrow) {
			int[] move = new int[6];
			int m = 0;
			
			move[5] = Integer.parseInt(arrow.substring(1)) - 1;
			move[4] = (arrow.charAt(0) > 96 ? arrow.charAt(0) - 97 : arrow.charAt(0) - 65);
			move[3] = Integer.parseInt(finish.substring(1)) - 1;
			move[2] = (finish.charAt(0) > 96 ? finish.charAt(0) - 97 : finish.charAt(0) - 65);
			move[1] = Integer.parseInt(start.substring(1)) - 1;
			move[0] = (start.charAt(0) > 96 ? start.charAt(0) - 97 : start.charAt(0) - 65);
			
			for(int i = 0; i < 6; i++) {
				m |= move[i] << (i * 4);
			}
			
			return doMove(m);
		}
		
		// Takes an encoded integer storing the positions of the move
		public boolean doMove(int move) {
			int[] coords = new int[6];
			int m = move;
			
			// Move stores positions in the order of fromX, fromY, toX, toY, arrowX, arrowY
			for(int i = 0; i < 6; i++) {
				coords[i] = move & 0xf;
				move = move >> 4;
			}
			
			if(movePiece(coords[0], coords[1], coords[2], coords[3])) {
				// Cast as a Gamepiece in case the arrow shot is illegal
				Gamepiece removed = null;
				if(coords[4] < 10 && coords[4] >= 0 && coords[5] < 10 && coords[5] >= 0) {
					removed = board[coords[5]][coords[4]];
				}
				
				// If the shot is legal, store the move on the stack
				if(shootArrow(coords[2], coords[3], coords[4], coords[5])) {
					Object[] movePlayed = {m,(Blank)removed};
					moveHistory.push(movePlayed);
					
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}	
		}
		
		// Undo the last move in reverse order.  Increment the blank neighbours around the arrow, set the arrow as a blank,
		// Move the queen from it's end position to it's start position and increment + decrement the empty neighbouring
		// squares, respectively.
		public void undoMove() {
			if(!moveHistory.isEmpty()) {
				// Grab the last move played form the top of the stack and extract the move and blank
				Object[] lastMovePlayed = moveHistory.pop();
				int move = (int)lastMovePlayed[0];
				Blank removedBlank = (Blank)lastMovePlayed[1];
				
				int[] coords = new int[6];
				
				// Move stores positions in the order of fromX, fromY, toX, toY, arrowX, arrowY
				for(int i = 0; i < 6; i++) {
					coords[i] = move & 0xf;
					move = move >> 4;
				}
				
				// Undo the arrow move neighbour updates
				for(int j = -1; j < 2; j++) {
					for(int i = -1; i < 2; i++) {
						if(!(((coords[5] + j) < 0) || ((coords[5] + j) > 9) || ((coords[4] + i) < 0) || ((coords[4] + i) > 9))) {
							if(this.board[coords[5] + j][coords[4] + i] instanceof Blank) {
								((Blank)this.board[coords[5] + j][coords[4] + i]).incrementEmptyNeighbours();
							}
						}
					}
				}
				
				// Set the arrow square to a blank square
				this.board[coords[5]][coords[4]] = removedBlank;
				
				// Move the queen the opposite direction without the legal move check (because the
				// queen couldn't have moved here in the first place if it was illegal)
				Gamepiece pieceToMove = this.board[coords[3]][coords[2]];
				
				boolean movedOneSpace = false;
				int blanks = 0;
				for(int j = -1; j < 2; j++) {
					for(int i = -1; i < 2; i++) {
						if(!(((coords[3] + j) < 0) || ((coords[3] + j) > 9) || ((coords[2] + i) < 0) || ((coords[2] + i) > 9))) {
							if(this.board[coords[3] + j][coords[2] + i] instanceof Blank) {
								blanks++;
								((Blank)this.board[coords[3] + j][coords[2] + i]).incrementEmptyNeighbours();
							}
						}
						
						if(!(((coords[1] + j) < 0) || ((coords[1] + j) > 9) || ((coords[0] + i) < 0) || ((coords[0] + i) > 9))) {
							if(this.board[coords[1] + j][coords[0] + i] instanceof Blank) {
								((Blank) this.board[coords[1] + j][coords[0] + i]).decrementEmptyNeighbours();
							}
							
							// A fix for blank squares not having a low enough empty neighbour score if the queen only moves one square away
							if((coords[1] + j) == coords[3] && (coords[0] + i) == coords[2]) {
								movedOneSpace = true;
							}
						}
					}
				}
				
				// Update the array of pieces so we can keep track of their location easier
				String pos = pieceToMove.stringPosition();
				if(pieceToMove instanceof WhiteQueen) {
					for(int i = 0; i < 4; i++) {
						if(W_pieces[i].stringPosition().equalsIgnoreCase(pos)) {
							W_pieces[i].move((byte)coords[0], (byte)coords[1]);
						}
					}
				} else {
					for(int i = 0; i < 4; i++) {
						if(B_pieces[i].stringPosition().equalsIgnoreCase(pos)) {
							B_pieces[i].move((byte)coords[0], (byte)coords[1]);
						}
					}
				}
				
				// Update the board locations with the appropriate game pieces
				this.board[coords[3]][coords[2]] = new Blank((byte)coords[2], (byte)coords[3], blanks);
				this.board[coords[1]][coords[0]] = pieceToMove;
				
				// If we only moved one space away, decrement the origin square neighbour count by 1
				if(movedOneSpace) {
					((Blank)this.board[coords[3]][coords[2]]).decrementEmptyNeighbours();
				}
				
			}
		}
		
		// Accepts the x and y coordinates of the piece to move and where to move it, 
		// and returns the new position.  Checks for a legal move (diagonal or orthogonal,
		// with no objects obstructing the path)
		public boolean movePiece(int p_x, int p_y, int to_x, int to_y) {
			boolean legal = checkLegalMove(p_x, p_y, to_x, to_y);
			
			if(legal) {
				Gamepiece pieceToMove = this.board[p_y][p_x];
				
				// Increment the empty neighbours count for all empty squares around the starting position, 
				// and count how many blanks there are.  Also decrement the empty squares around final position.
				boolean movedOneSpace = false;
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
							
							// A fix for blank squares not having a low enough empty neighbour score if the queen only moves one square away
							if((to_y + j) == p_y && (to_x + i) == p_x) {
								movedOneSpace = true;
							}
						}
					}
				}
				
				// Update the array of pieces so we can keep track of their location easier
				String pos = pieceToMove.stringPosition();
				if(pieceToMove instanceof WhiteQueen) {
					for(int i = 0; i < 4; i++) {
						if(W_pieces[i].stringPosition().equalsIgnoreCase(pos)) {
							W_pieces[i].move((byte)to_x, (byte)to_y);
						}
					}
				} else {
					for(int i = 0; i < 4; i++) {
						if(B_pieces[i].stringPosition().equalsIgnoreCase(pos)) {
							B_pieces[i].move((byte)to_x, (byte)to_y);
						}
					}
				}
				
				// Update the board locations with the appropriate game pieces
				this.board[p_y][p_x] = new Blank((byte)p_x, (byte)p_y, blanks);
				this.board[to_y][to_x] = pieceToMove;
				
				// If we only moved one space away, decrement the origin square neighbour count by 1
				if(movedOneSpace) {
					((Blank)this.board[p_y][p_x]).decrementEmptyNeighbours();
				}
				
				return true;
			} else {
				return false;
			}
			
		}
		
		// Overloaded method to handle a-j column names instead of #'s
		public boolean movePiece(char p_x, int p_y, char to_x, int to_y) {
			// Convert letters to numbers (uppercase letters have a value from 65 - 90)
			int px = (p_x > 96 ? p_x - 97 : p_x - 65);
			int tox = (to_x > 96 ? to_x - 97 : to_x - 65);
			
			return movePiece(px, --p_y, tox, --to_y);
		}
		
		// Overloaded method to handle coordinate input (ex. a4 to d1)
		public boolean movePiece(String from, String to) {
			char init = from.charAt(0);
			char dest = to.charAt(0);
			
			int p_x = ((int)init > 96 ? (int)init - 97 : (int)init - 65);
			int p_y = Integer.parseInt(from.substring(1)) - 1;
			int to_x = ((int)dest > 96 ? (int)dest - 97 : (int)dest - 65);
			int to_y = Integer.parseInt(to.substring(1)) - 1;
			
			return movePiece(p_x, p_y, to_x, to_y);
		}
		
		// Shoots an arrow to a square, blocking it off
		public boolean shootArrow(int p_x, int p_y, int to_x, int to_y) {
			boolean legal = checkLegalMove(p_x, p_y, to_x, to_y);
			
			if(legal) {
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
				
				return true;
			}
			
			return false;
		}
		
		// Overloaded function for shooting an arrow to allow "natural" coordinates (index starts at 1)
		public boolean shootArrow(String from, String to) {
			char init = from.charAt(0);
			char dest = to.charAt(0);
			
			int p_x = (init > 96 ? init - 97 : init - 65);
			int p_y = Integer.parseInt(from.substring(1)) - 1;
			int to_x = (dest > 96 ? dest - 97 : dest - 65);
			int to_y = Integer.parseInt(to.substring(1)) - 1;
			
			return shootArrow(p_x, p_y, to_x, to_y);
		}
		
		// Checks to see if the initial and destination squares are valid and a valid path exists between them
		private boolean checkLegalMove(int p_x, int p_y, int to_x, int to_y) {
			// Check for in bounds
			if(p_x < 0 || p_x > 9 || p_y < 0|| p_y > 9 || to_x < 0 || to_x > 9 || to_y < 0|| to_y > 9) {
				return false;
			}
			
			// Check for moving to same square
			if(p_x == to_x && p_y == to_y) {
				return false;
			}
			
			// Check for inappropriate movement (must be lateral or diagonal)
			int y_diff = Math.abs(to_y - p_y);
			int x_diff = Math.abs(to_x - p_x);
			if(!(p_x == to_x || p_y == to_y || x_diff == y_diff)) {
				return false;
			}
			
			// Check for objects in the path, including destination square
			if(y_diff == 0) { // Only need to check left and right
				for(int i = 1; i <= x_diff; i++) {
					if(to_x > p_x) {
						if(this.board[p_y][p_x + i].val() != 0) { return false; }
					} else if(this.board[p_y][p_x - i].val() != 0) { return false; }
				}
			} else if(x_diff == 0) { // Check up and down
				for(int i = 1; i <= y_diff; i++) {
					if(to_y > p_y) {
						if(this.board[p_y + i][p_x].val() != 0) { return false; }
					} else if(this.board[p_y - i][p_x].val() != 0) { return false; }
				}
			} else { // Check diagonally
				for(int i = 1; i <= y_diff; i++) {
					// Moving northeast
					if(to_y > p_y && to_x > p_x) {
						if(this.board[p_y + i][p_x + i].val() != 0) { return false; }
					}
					
					// Moving southeast
					if(to_y < p_y && to_x > p_x) {
						if(this.board[p_y - i][p_x + i].val() != 0) { return false; }
					}
					
					// Moving northwest
					if(to_y > p_y && to_x < p_x) {
						if(this.board[p_y + i][p_x - i].val() != 0) { return false; }
					}
					
					// Moving southwest
					if(to_y < p_y && to_x < p_x) {
						if(this.board[p_y - i][p_x - i].val() != 0) { return false; }
					}
				}
			}
			
			return true;
		}
		
		// Reset all the king and queen move counts for every blank square on the map.
		private void resetBlankMoveCounts() {
			for(int j = 0; j < 10; j++) {
				for(int i = 0; i < 10; i++) {
					if(board[j][i] instanceof Blank) {
						((Blank)board[j][i]).reset();
					}
				}
			}
		}
		
		// Generate a Queen-step map for every blank square on the board for each player.
		// The algorithm makes queen moves from each queen's location and labels any square it can reach as distance 1.
		// It saves these squares to a list and then tries to make moves from all of these squares and label and new
		// spaces as distance 2.  This continues on until our list is empty.
		private void generateQueenMoves() {
			// For each Queen, go through all of their possible steps, store each square as a 1, and save the squares to an array
			ArrayList<Blank> list = new ArrayList<Blank>();
			int[] updates = new int[1];
			updates[0] = 0;
			
			// Make steps for each white queen on the board
			for(WhiteQueen queen : W_pieces) {
				int[] pos = queen.position();
				int x = pos[0];
				int y = pos[1];
				
				// Append the Blank spaces generated from this queen's position to the new list
				list.addAll(genQueenMoves(x, y, WHITE));
			}
			
			if(!list.isEmpty()){
				genQueenMovesSecondPass(2, WHITE);
			}
			
			list.clear();
			updates[0] = 0;
			
			// Make steps for each black queen on the board
			for(BlackQueen queen : B_pieces) {
				int[] pos = queen.position();
				int x = pos[0];
				int y = pos[1];
				
				// Append the Blank spaces generated from this queen's position to the new list
				list.addAll(genQueenMoves(x, y, BLACK));
			}
				
			if(!list.isEmpty()){
				genQueenMovesSecondPass(2, BLACK);
			}
		}
		
		/*
		 * Generates a list of all squares that a queen can reach in 1 step
		 */
		private ArrayList<Blank> genQueenMoves(int x, int y, int player) {
			ArrayList<Blank> list = new ArrayList<>();
			int stepCount = 1;

			if(player == WHITE) {
				//up
				for(int up = y+1;up <= 9 && board[up][x] instanceof Blank; up++){
					if(((Blank)board[up][x]).wq > stepCount){
						((Blank)board[up][x]).setQueenMoves(stepCount, player);
						list.add((Blank) board[up][x]);
						//System.out.println("Adding up\t"+(Blank) board[up][x]);
					}
				}
				//down
				for(int down = y-1; down >= 0 && board[down][x] instanceof Blank; down--){
					if(((Blank)board[down][x]).wq > stepCount){
						((Blank)board[down][x]).setQueenMoves(stepCount, player);
						list.add((Blank) board[down][x]);
						//System.out.println("Adding down\t"+(Blank) board[down][x]);
					}
				}
				//right
				for(int right = x+1; right <= 9 && board[y][right] instanceof Blank; right++){
					if(((Blank)board[y][right]).wq > stepCount){
						((Blank)board[y][right]).setQueenMoves(stepCount, player);
						list.add((Blank) board[y][right]);
						//System.out.println("Adding right\t"+(Blank) board[y][right]);
					}
				}
				//left
				for(int left = x-1; left >= 0 && board[y][left] instanceof Blank; left--){
					if(((Blank)board[y][left]).wq > stepCount){
						((Blank)board[y][left]).setQueenMoves(stepCount, player);
						list.add((Blank) board[y][left]);
						//System.out.println("Adding left\t"+(Blank) board[y][left]);
					}
				}
				//upright
				for(int up = y+1, right = x+1; up <= 9 && right <= 9 && board[up][right] instanceof Blank; up++, right++){
					if(((Blank)board[up][right]).wq > stepCount){
						((Blank)board[up][right]).setQueenMoves(stepCount, player);
						list.add((Blank) board[up][right]);
						//System.out.println("Adding [up][right]\t"+(Blank) board[up][right]);
					}
				}
				//upleft
				for(int up = y+1, left = x-1; up <= 9 && left >= 0 && board[up][left] instanceof Blank; up++, left--){
					if(((Blank)board[up][left]).wq > stepCount){
						((Blank)board[up][left]).setQueenMoves(stepCount, player);
						list.add((Blank) board[up][left]);
						//System.out.println("Adding [up][left]\t"+(Blank) board[up][left]);
					}
				}
				//downright
				for(int down = y-1, right = x+1; down >= 0 && right <= 9 && board[down][right] instanceof Blank; down--, right++){
					if(((Blank)board[down][right]).wq > stepCount){
						((Blank)board[down][right]).setQueenMoves(stepCount, player);
						list.add((Blank) board[down][right]);
						//System.out.println("Adding [down][right]\t"+(Blank) board[down][right]);
					}
				}
				//downleft
				for(int down = y-1, left = x-1; down >= 0 && left >= 0 && board[down][left] instanceof Blank; down--, left--){
					if(((Blank)board[down][left]).wq > stepCount){
						((Blank)board[down][left]).setQueenMoves(stepCount, player);
						list.add((Blank) board[down][left]);
						//System.out.println("Adding [down][left]\t"+(Blank) board[down][left]);
					}
				}
			} else {
				//up
				for(int up = y+1; up <= 9 && board[up][x] instanceof Blank; up++){
					if(((Blank)board[up][x]).bq > stepCount){
						((Blank)board[up][x]).setQueenMoves(stepCount, player);
						list.add((Blank) board[up][x]);
					}
				}
				//down
				for(int down = y-1; down >= 0 && board[down][x] instanceof Blank; down--){
					if(((Blank)board[down][x]).bq > stepCount){
						((Blank)board[down][x]).setQueenMoves(stepCount, player);
						list.add((Blank) board[down][x]);
					}
				}
				//right
				for(int right = x+1; right <= 9 && board[y][right] instanceof Blank; right++){
					if(((Blank)board[y][right]).bq > stepCount){
						((Blank)board[y][right]).setQueenMoves(stepCount, player);
						list.add((Blank) board[y][right]);
					}
				}
				//left
				for(int left = x-1; left >= 0 && board[y][left] instanceof Blank; left--){
					if(((Blank)board[y][left]).bq > stepCount){
						((Blank)board[y][left]).setQueenMoves(stepCount, player);
						list.add((Blank) board[y][left]);
					}
				}
				//upright
				for(int up = y+1, right = x+1; up <= 9 && right <= 9 && board[up][right] instanceof Blank; up++, right++){
					if(((Blank)board[up][right]).bq > stepCount){
						((Blank)board[up][right]).setQueenMoves(stepCount, player);
						list.add((Blank) board[up][right]);
					}
				}
				//upleft
				for(int up = y+1, left = x-1; up <= 9 && left >= 0 && board[up][left] instanceof Blank; up++, left--){
					if(((Blank)board[up][left]).bq > stepCount){
						((Blank)board[up][left]).setQueenMoves(stepCount, player);
						list.add((Blank) board[up][left]);
					}
				}
				//downright
				for(int down = y-1, right = x+1; down >= 0 && right <= 9 && board[down][right] instanceof Blank; down--, right++){
					if(((Blank)board[down][right]).bq > stepCount){
						((Blank)board[down][right]).setQueenMoves(stepCount, player);
						list.add((Blank) board[down][right]);
					}
				}
				//downleft
				for(int down = y-1, left = x-1; down >= 0 && left >= 0 && board[down][left] instanceof Blank; down--, left--){
					if(((Blank)board[down][left]).bq > stepCount){
						((Blank)board[down][left]).setQueenMoves(stepCount, player);
						list.add((Blank) board[down][left]);
					}
				}
			}

			return list;
		}

		/*
		 * For each square that hasn't been updated, try to reach a square that has a stepcount of stepcount - 1
		 */
		private void genQueenMovesSecondPass(int stepCount, int player) {
			boolean gotOne = false;
			boolean oneLeft = false;

			for(int x = 0; x <= 9; x++){
				for(int y = 0; y <= 9; y++){
					if(board[y][x] instanceof Blank){
						boolean success;
						switch(player){
						case 1:
							if(((Blank)board[y][x]).wq == 127){
								success = genQueenMovesSecondPassCheck(x, y, (Blank)board[y][x], stepCount, player);
								if(success){
									gotOne = true;
								} else {
									oneLeft = true;
								}
							}
							break;
						case 2:
							if(((Blank)board[y][x]).bq == 127){
								success = genQueenMovesSecondPassCheck(x, y, (Blank)board[y][x], stepCount, player);
								if(success){
									gotOne = true;
								} else {
									oneLeft = true;
								}
							}
							break;
						}
					}
				}
			}

			if(!gotOne && oneLeft){
				//we gots a hole we can't get to
				return;
			}
			if(oneLeft){
				genQueenMovesSecondPass(++stepCount, player);
			}
		}

		/*
		 * Makes moves from Blank squares in queen directions and steps, attempting to find squares
		 * that have been updated already.
		 */
		private boolean genQueenMovesSecondPassCheck(int x, int y, Blank piece, int stepCount, int player) {
			if(player == WHITE) {
				//up
				for(int up = y+1;up <= 9; up++){
					if(board[up][x] instanceof Blank){
						if(((Blank)board[up][x]).wq == stepCount-1){
							piece.wq = (byte)stepCount;
							return true;
						}
					} else {
						break;
					}
				}
				//down
				for(int down = y-1;down >= 0 && down < y; down--){
					if(board[down][x] instanceof Blank){
						if(((Blank)board[down][x]).wq == stepCount-1){
							piece.wq = (byte)stepCount;
							return true;
						}
					} else {
						break;
					}
				}
				//right
				for(int right = x+1;right <= 9 && right > x; right++){
					if(board[y][right] instanceof Blank){
						if(((Blank)board[y][right]).wq == stepCount-1){
							piece.wq = (byte)stepCount;
							return true;
						}
					} else {
						break;
					}
				}
				//left
				for(int left = x-1;left >= 0 && left < x; left--){
					if(board[y][left] instanceof Blank){
						if(((Blank)board[y][left]).wq == stepCount-1){
							piece.wq = (byte)stepCount;
							return true;
						}
					} else {
						break;
					}
				}
				//upright
				for(int up = y+1, right = x+1; up <= 9 && up > y && right <= 9 && right > x; up++, right++){
					if(board[up][right] instanceof Blank){
						if(((Blank)board[up][right]).wq == stepCount-1){
							piece.wq = (byte)stepCount;
							return true;
						}
					} else {
						break;
					}
				}
				//upleft
				for(int up = y+1, left = x-1; up <= 9 && up > y && left >= 0 && left < x; up++, left--){
					if(board[up][left] instanceof Blank){
						if(((Blank)board[up][left]).wq == stepCount-1){
							piece.wq = (byte)stepCount;
							return true;
						}
					} else {
						break;
					}
				}
				//downright
				for(int down = y-1, right = x+1; down >= 0 && down < y && right <= 9 && right > x; down--, right++){
					if(board[down][right] instanceof Blank){
						if(((Blank)board[down][right]).wq == stepCount-1){
							piece.wq = (byte)stepCount;
							return true;
						}
					} else {
						break;
					}
				}
				//downleft
				for(int down = y-1, left = x-1; down >= 0 && down < y && left >= 0 && left < x; down--, left--){
					if(board[down][left] instanceof Blank){
						if(((Blank)board[down][left]).wq == stepCount-1){
							piece.wq = (byte)stepCount;
							return true;
						}
					} else {
						break;
					}
				}
			} else {
				//up
				for(int up = y+1;up <= 9; up++){
					if(board[up][x] instanceof Blank){
						if(((Blank)board[up][x]).bq == stepCount-1){
							piece.bq = (byte)stepCount;
							return true;
						}
					} else {
						break;
					}
				}
				//down
				for(int down = y-1;down >= 0 && down < y; down--){
					if(board[down][x] instanceof Blank){
						if(((Blank)board[down][x]).bq == stepCount-1){
							piece.bq = (byte)stepCount;
							return true;
						}
					} else {
						break;
					}
				}
				//right
				for(int right = x+1;right <= 9 && right > x; right++){
					if(board[y][right] instanceof Blank){
						if(((Blank)board[y][right]).bq == stepCount-1){
							piece.bq = (byte)stepCount;
							return true;
						}
					} else {
						break;
					}
				}
				//left
				for(int left = x-1;left >= 0 && left < x; left--){
					if(board[y][left] instanceof Blank){
						if(((Blank)board[y][left]).bq == stepCount-1){
							piece.bq = (byte)stepCount;
							return true;
						}
					} else {
						break;
					}
				}
				//upright
				for(int up = y+1, right = x+1; up <= 9 && up > y && right <= 9 && right > x; up++, right++){
					if(board[up][right] instanceof Blank){
						if(((Blank)board[up][right]).bq == stepCount-1){
							piece.bq = (byte)stepCount;
							return true;
						}
					} else {
						break;
					}
				}
				//upleft
				for(int up = y+1, left = x-1; up <= 9 && up > y && left >= 0 && left < x; up++, left--){
					if(board[up][left] instanceof Blank){
						if(((Blank)board[up][left]).bq == stepCount-1){
							piece.bq = (byte)stepCount;
							return true;
						}
					} else {
						break;
					}
				}
				//downright
				for(int down = y-1, right = x+1; down >= 0 && down < y && right <= 9 && right > x; down--, right++){
					if(board[down][right] instanceof Blank){
						if(((Blank)board[down][right]).bq == stepCount-1){
							piece.bq = (byte)stepCount;
							return true;
						}
					} else {
						break;
					}
				}
				//downleft
				for(int down = y-1, left = x-1; down >= 0 && down < y && left >= 0 && left < x; down--, left--){
					if(board[down][left] instanceof Blank){
						if(((Blank)board[down][left]).bq == stepCount-1){
							piece.bq = (byte)stepCount;
							return true;
						}
					} else {
						break;
					}
				}
			}

			return false;
		}
		
		// Generate a King-step map for every blank square on the board for each player.
		public void generateKingMoves() {
			
			for(WhiteQueen queen : W_pieces) {
				int[] pos = queen.position();
				genKingMovesFirstRun((byte)pos[0],(byte)pos[1],WHITE);
			}
			
			genKingMovesSecondPass(genLeft(WHITE), (byte)2, WHITE);

			for(BlackQueen queen : B_pieces) {
				int[] pos = queen.position();
				genKingMovesFirstRun((byte)pos[0],(byte)pos[1],BLACK);
			}
			
			genKingMovesSecondPass(genLeft(BLACK), (byte)2, BLACK);
		}

		// Finds all the squares one step away from the king
		private void genKingMovesFirstRun(byte x, byte y, int player){
			byte xOff, yOff;
			
			if(player == WHITE) {
				if(x > 0 && x < 9 && y > 0 && y < 9){
					for(byte[] pos : MATRIX){
						xOff = pos[1];
						yOff = pos[0];
						
						if(this.board[y+yOff][x+xOff] instanceof Blank && ((Blank)this.board[y+yOff][x+xOff]).wk == 127) {
							((Blank)this.board[y+yOff][x+xOff]).wk = 1;
						}
					}
				} else {
					for(byte[] pos : MATRIX){
						if(y+pos[0] >= 0 && y+pos[0] <= 9 && x+pos[1] >= 0 && x+pos[1] <= 9) {
							if(this.board[y+pos[0]][x+pos[1]] instanceof Blank && ((Blank)this.board[y+pos[0]][x+pos[1]]).wk == 127) {
								((Blank)this.board[y+pos[0]][x+pos[1]]).wk = 1;
							}
						}
					}
				}
			} else {
				if(x > 0 && x < 9 && y > 0 && y < 9){
					for(byte[] pos : MATRIX){
						if(this.board[y+pos[0]][x+pos[1]] instanceof Blank && ((Blank)this.board[y+pos[0]][x+pos[1]]).bk == 127) {
							((Blank)this.board[y+pos[0]][x+pos[1]]).bk = 1;
						}
					}
				} else {
					for(byte[] pos : MATRIX){
						if(y+pos[0] >= 0 && y+pos[0] <= 9 && x+pos[1] >= 0 && x+pos[1] <= 9) {
							if(this.board[y+pos[0]][x+pos[1]] instanceof Blank && ((Blank)this.board[y+pos[0]][x+pos[1]]).bk == 127) {
								((Blank)this.board[y+pos[0]][x+pos[1]]).bk = 1;
							}
						}
					}
				}
			}
		}
		
		// Works backwards from all squares that haven't been updated.  On each pass, if we iterate though
		// the squares and update a square with a distance if it finds a neighbouring square with a score already,
		// always preferring to set its own score to it's smallest neighbour + 1.  If we have a pass where we haven't
		// made and update or our list runs out, then we're done.
		private void genKingMovesSecondPass(ArrayList<Blank> list, byte stepCount, int player){
			boolean gotOne = false, oneLeft = false, success;
			ArrayList<Blank> Left = new ArrayList();
			
			for(Blank entry : list){
					success = genKingMovesHelperSecondCheck(entry, stepCount, player);
					if(success){
						gotOne = true;
					} else {
						Left.add(entry);
						oneLeft = true;
					}
			}
			
			if(!gotOne && oneLeft){
				// We have a hole we can't get to
				return;
			}
			
			if(oneLeft){
				genKingMovesSecondPass(Left,++stepCount, player);
			}
		}

		// Checks each passed square for a neighbour in a most-likely-to-find order.  
		private boolean genKingMovesHelperSecondCheck(Blank piece, byte stepCount, int player){
			byte x = piece.x;
			byte y = piece.y;
			
			if(player == WHITE) {
				if(x > 0 && x < 9 && y > 0 && y < 9){ //If not on an edge
					for(byte pos : ORDER){
						int xOff = MATRIX[pos][0];
						int yOff = MATRIX[pos][1];
						
						if(this.board[y+yOff][x+xOff] instanceof Blank && ((Blank)this.board[y+yOff][x+xOff]).wk == stepCount-1) {
							((Blank)this.board[y][x]).wk = stepCount;
							return true;
						}
					}
				} else {
					for(byte pos : ORDER){
						int xOff = MATRIX[pos][0];
						int yOff = MATRIX[pos][1];
						
						if(y+yOff >= 0 && y+yOff <= 9 && x+xOff >= 0 && x+xOff <= 9){
							if(this.board[y+yOff][x+xOff] instanceof Blank && ((Blank)this.board[y+yOff][x+xOff]).wk == stepCount-1) {
								((Blank)this.board[y][x]).wk = stepCount;
								return true;
							}
						}
					}
				}
			} else {
				if(x > 0 && x < 9 && y > 0 && y < 9){ //If not on an edge
					for(byte pos : ORDER){
						int xOff = MATRIX[pos][0];
						int yOff = MATRIX[pos][1];
						
						if(this.board[y+yOff][x+xOff] instanceof Blank && ((Blank)this.board[y+yOff][x+xOff]).bk == stepCount-1) {
							((Blank)this.board[y][x]).bk = stepCount;
							return true;
						}
					}
				} else {
					for(byte pos : ORDER){
						int xOff = MATRIX[pos][0];
						int yOff = MATRIX[pos][1];
						
						if(y+yOff >= 0 && y+yOff <= 9 && x+xOff >= 0 && x+xOff <= 9){
							if(this.board[y+yOff][x+xOff] instanceof Blank && ((Blank)this.board[y+yOff][x+xOff]).bk == stepCount-1) {
								((Blank)this.board[y][x]).bk = stepCount;
								return true;
							}
						}
					}
				}
			}
			
			return false;
		}
		
		// A function to find and store all the squares that haven't had their king distance calculated yet
		private ArrayList<Blank> genLeft(int player){
			ArrayList<Blank> list = new ArrayList();
			
			if(player == WHITE){
				for(int x = 0; x <= 9; x++){
					for(int y = 0; y <= 9; y++){
						if(board[y][x] instanceof Blank && ((Blank)board[y][x]).wk == 127){
							list.add((Blank)board[y][x]);
						}
					}
				}
			} else {
				for(int x = 0; x <= 9; x++){
					for(int y = 0; y <= 9; y++){
						if(board[y][x] instanceof Blank && ((Blank)board[y][x]).bk == 127){
							list.add((Blank)board[y][x]);
						}
					}
				}
			}
			
			return list;
		}
		
		// Calculate the Alpha score for every queen on the board
		// Resets the move distances for the board, recalculates them and then calculates the alphas using the updated values
		private void calculateAlphaScores() {
			resetBlankMoveCounts();
			generateQueenMoves();
			generateKingMoves();
			
			for(WhiteQueen queen : W_pieces) {
				queen.calculateAlpha(board);
			}
			
			for(BlackQueen queen : B_pieces) {
				queen.calculateAlpha(board);
			}
		}
		
		// Returns the PIECE VALUE (0 => Blank, 1 => WQ, 2 => BQ, -1 => Arrow)
		
		// Returns the PIECE VALUE (0 => Blank, 1 => WQ, 2 => BQ, -1 => Arrow)
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
		
		
		public float evaluate(int player) {
			// Value variables set up
			float val = 0f;
			float t1 = 0, t2 = 0, c1 = 0, c2 = 0, m = 0, m2 = 0, m1 = 0, w = 0, T = 0;
			
			// Calculate alphas.  Method resets the distance counts and recalculates them first
			calculateAlphaScores();
			
			// Calculate the global heuristic values for all blank squares
			for(int j = 0; j < 10; j++) {
				for(int i = 0; i < 10; i++) {
					if(board[j][i] instanceof Blank) {
						// Queen move score
						t1 += ((Blank)board[j][i]).getQueenDelta(player);
						
						// King move score
						t2 += ((Blank)board[j][i]).getKingDelta(player);
						
						// Local Queen move score
						c1 += ((Blank)board[j][i]).getC1Score();
						
						// Local King move score
						c2 += ((Blank)board[j][i]).getC2Score();
						
						// Measure of how partitioned the board is (goes to 0 as players get closer to having their own private territories)
						w += ((Blank)board[j][i]).getOmegaScore();
					}
				}
			}
			
			// If w == 0, then t2, t3 and t4 all return 0, so t1 is the only non-zero value
			if(Double.compare(w, 0) == 0) {
				val = t1;
			} else {
				// The weighted positional advantage score
				// f1(w)t1 + f2(w)c1 + f3(w)c2 + f4(w)t2 | 0 <= fi(w) <= 1; SUM(fi(w)) = 1
				T = (float)Math.pow(2, (-w)*F_OMEGA_COEF)*t1 + 0.18f*(float)(1 - Math.pow(2, (-w)*F_OMEGA_COEF))*c1 + 0.22f*(float)(1 - Math.pow(2, (-w)*F_OMEGA_COEF))*c2 + 0.6f*(float)(1 - Math.pow(2, (-w)*F_OMEGA_COEF))*t2;
				
				// Calculate Black's mobility score
				for(BlackQueen queen : B_pieces) {
					m2 += 1.5*Math.sqrt(w)*Math.pow(Math.E, -queen.getAlpha()*0.2);
				}
				
				// Calculate White's mobility score
				for(WhiteQueen queen : W_pieces) {
					m1 += 1.5*Math.sqrt(w)*Math.pow(Math.E, -queen.getAlpha()*0.2);
				}
				
				// The mobility penalty score
				m = m2 - m1;
				
				// The final heuristic value
				val = T + m;
			}
			
			//System.out.println("t1: " + t1 + "\tc1: " + c1 + "\tc2: " + c2 + "\tt2: " + t2 + "\nOmega: " + w + "\tT: " + T + "\nBA: " + m2 + "\tWA: " + m1 + "\tM: " + m);
			//System.out.println("Score: " + val + "\n");
			
			return val;
		}

		// A check for the Monte Carlo Tree Search algorithm to see if we're at the filling phase of the game or not.
		// Returns the score if we're fully partitioned, 0 otherwise.
		public float mctsEval(int player) {
			float w = 0, t1 = 0;
			
			resetBlankMoveCounts();
			generateQueenMoves();
			
			for(int j = 0; j < 10; j++) {
				for(int i = 0; i < 10; i++) {
					if(board[j][i] instanceof Blank) {
						w += ((Blank)board[j][i]).getOmegaScore();
						t1 += ((Blank)board[j][i]).getQueenDelta(player);
					}
				}
			}
			
			return (Float.compare(w, 0) == 0 ? 0 : t1);
		}

		///////////////////////////////////////////////////////////////////////////////////////////
		// Printing methods for testing purposes only, remove these when code is ready to launch //
		///////////////////////////////////////////////////////////////////////////////////////////
		
		// Print out the board
		public String toString() {
			String b = "\n   ___ ___ ___ ___ ___ ___ ___ ___ ___ ___\n";
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
		
		// Print out the empty neighbours map
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
		
		// Print out the queen moves map
		public String printQueenMovesCount() {
			// generateQueenMoves();
			
			String b = "White Queen Distance\n";
			b += "   ___ ___ ___ ___ ___ ___ ___ ___ ___ ___\n";
			for(int i = 9; i >= 0; i--){
				if(i < 9){b += (i + 1) + " |";}
				else{b += (i + 1) + "|";}
				
				for(int j = 0; j < 10; j++){
					int square = this.board[i][j].val();
					if(square == 0) {
						b += "_" + ((Blank)this.board[i][j]).wq + "_|";
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

			b += "Black Queen Distance\n";
			b += "   ___ ___ ___ ___ ___ ___ ___ ___ ___ ___\n";
			for(int i = 9; i >= 0; i--){
				if(i < 9){b += (i + 1) + " |";}
				else{b += (i + 1) + "|";}
				
				for(int j = 0; j < 10; j++){
					int square = this.board[i][j].val();
					if(square == 0) {
						b += "_" + ((Blank)this.board[i][j]).bq + "_|";
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
		
		// Print out the queen moves map
		public String printKingMovesCount() {
			//generateKingMoves();
			
			String b = "White King Distance\n";
			b += "   ___ ___ ___ ___ ___ ___ ___ ___ ___ ___\n";
			for(int i = 9; i >= 0; i--){
				if(i < 9){b += (i + 1) + " |";}
				else{b += (i + 1) + "|";}
				
				for(int j = 0; j < 10; j++){
					int square = this.board[i][j].val();
					if(square == 0) {
						b += "_" + ((Blank)this.board[i][j]).wk + "_|";
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

			b += "Black King Distance\n";
			b += "   ___ ___ ___ ___ ___ ___ ___ ___ ___ ___\n";
			for(int i = 9; i >= 0; i--){
				if(i < 9){b += (i + 1) + " |";}
				else{b += (i + 1) + "|";}
				
				for(int j = 0; j < 10; j++){
					int square = this.board[i][j].val();
					if(square == 0) {
						b += "_" + ((Blank)this.board[i][j]).bk + "_|";
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
		
		// Print out the alpha scores for each queen
		public String printAlphaScores() {
			calculateAlphaScores();
			
			String b = "   ___ ___ ___ ___ ___ ___ ___ ___ ___ ___\n";
			for(int i = 9; i >= 0; i--){
				if(i < 9){b += (i + 1) + " |";}
				else{b += (i + 1) + "|";}
				
				for(int j = 0; j < 10; j++){
					int square = this.board[i][j].val();

					if(square == 2) {
						b += "_" + ((BlackQueen)board[i][j]).getAlpha() + "_|";
					} else if(square == 1) {
						b += "_" + ((WhiteQueen)board[i][j]).getAlpha() + "_|";
					} else if(square == -1) {
						b += "_X_|";
					} else {
						b += "___|";
					}
				}
				b += "\n";
			}
			
			b += "    a   b   c   d   e   f   g   h   i   j\n";
			
			return b;
		}
		
		public String printBoardEval() {
			String response = "Eval if White to move: " + this.evaluate(WHITE);
			response +="\nEval if Black to move: " + this.evaluate(BLACK) + "\n";
			return response;
		}
		
		// Print the move history (encoded format)
		public String printMoveHistory() {
			String history = "";
			int player = 1;
			
			for(Object[] move : moveHistory) {
				if(player == 1) {
					history += "{" + move[0];
					player++;
				} else {
					history += ", " + move[0] + "},\n";
					player--;
				}		
			}
			
			return history;
		}
		
		// Print what the Q-Delta scores for each square would be depending on whose turn it is
		public String printQueenDelta() {
			this.resetBlankMoveCounts();
			this.generateQueenMoves();
			
			String b = "\nQ-Deltas if white to move";
			
			b += "\n   ___ ___ ___ ___ ___ ___ ___ ___ ___ ___\n";
			for(int i = 9; i >= 0; i--){
				if(i < 9){b += (i + 1) + " |";}
				else{b += (i + 1) + "|";}
				
				for(int j = 0; j < 10; j++){
					int square = this.board[i][j].val();

					if(square == 2) {
						b += "_B_|";
					} else if(square == 1) {
						b += "_W_|";
					} else if(square == -1) {
						b += "_X_|";
					} else {
						b += "_" + ((Blank)board[i][j]).getQueenDelta(1) + "_|";
					}
				}
				b += "\n";
			}
			
			b += "    a   b   c   d   e   f   g   h   i   j\n";
			
			b += "\nQ-Deltas if black to move";
			
			b += "\n   ___ ___ ___ ___ ___ ___ ___ ___ ___ ___\n";
			for(int i = 9; i >= 0; i--){
				if(i < 9){b += (i + 1) + " |";}
				else{b += (i + 1) + "|";}
				
				for(int j = 0; j < 10; j++){
					int square = this.board[i][j].val();

					if(square == 2) {
						b += "_B_|";
					} else if(square == 1) {
						b += "_W_|";
					} else if(square == -1) {
						b += "_X_|";
					} else {
						b += "_" + ((Blank)board[i][j]).getQueenDelta(2) + "_|";
					}
				}
				b += "\n";
			}
			
			b += "    a   b   c   d   e   f   g   h   i   j\n";
			
			return b;
		}
		
		// Print what the K-Delta scores for each square would be depending on whose turn it is
		public String printKingDelta() {
			this.resetBlankMoveCounts();
			this.generateKingMoves();
			
			String b = "\nK-Deltas if white to move";
			
			b += "\n   ___ ___ ___ ___ ___ ___ ___ ___ ___ ___\n";
			for(int i = 9; i >= 0; i--){
				if(i < 9){b += (i + 1) + " |";}
				else{b += (i + 1) + "|";}
				
				for(int j = 0; j < 10; j++){
					int square = this.board[i][j].val();

					if(square == 2) {
						b += "_B_|";
					} else if(square == 1) {
						b += "_W_|";
					} else if(square == -1) {
						b += "_X_|";
					} else {
						b += "_" + ((Blank)board[i][j]).getKingDelta(1) + "_|";
					}
				}
				b += "\n";
			}
			
			b += "    a   b   c   d   e   f   g   h   i   j\n";
			
			b += "\nK-Deltas if black to move";
			
			b += "\n   ___ ___ ___ ___ ___ ___ ___ ___ ___ ___\n";
			for(int i = 9; i >= 0; i--){
				if(i < 9){b += (i + 1) + " |";}
				else{b += (i + 1) + "|";}
				
				for(int j = 0; j < 10; j++){
					int square = this.board[i][j].val();

					if(square == 2) {
						b += "_B_|";
					} else if(square == 1) {
						b += "_W_|";
					} else if(square == -1) {
						b += "_X_|";
					} else {
						b += "_" + ((Blank)board[i][j]).getKingDelta(2) + "_|";
					}
				}
				b += "\n";
			}
			
			b += "    a   b   c   d   e   f   g   h   i   j\n";
			
			return b;
		}
}
