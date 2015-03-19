import java.util.ArrayList;


public class Gameboard {
		
		private static final int WHITE = 1, BLACK = 2;
		private int numBlanks;
		private WhiteQueen[] W_pieces;
		private BlackQueen[] B_pieces;
		private Gamepiece[][] board;
		
		// Set up the initial position of the pieces
		public Gameboard() {
			// Start with 92 blank spaces
			numBlanks = 92;
			
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
		
		// Allows the creation of a new board with a predefined position
		public Gameboard(Gamepiece[][] board, WhiteQueen[] wqs, BlackQueen[] bqs) {
			this.board = board;
			
			W_pieces = wqs;
			B_pieces = bqs;	
		}
		
		// Accepts the string coordinates for a queen's start and end position and where its arrow lands
		public void doMove(String start, String finish, String arrow) {
			boolean legal = movePiece(start, finish);
			if(legal) {
				shootArrow(finish, arrow);
			}
		}
		
		// Takes an encoded integer storing the positions of the move
		public void doMove(int move) {
			int[] coords = new int[6];
			
			// Move stores positions in the order of fromX, fromY, toX, toY, arrowX, arrowY
			for(int i = 0; i < 6; i++) {
				coords[i] = move & 0xf;
				move = move >> 4;
			}
			
			if(movePiece(coords[0], coords[1], coords[2], coords[3])) {
				shootArrow(coords[2], coords[3], coords[4], coords[5]);
			}
		}
		
		// Undo the move in reverse order.  Increment the blank neighbours around the arrow, set the arrow as a blank,
		// Move the queen from it's end position to it's start position and increment + decrement the empty neighbouring
		// squares, respectively.
		public void undoMove(int move) {
			int[] coords = new int[6];
			
			// Move stores positions in the order of fromX, fromY, toX, toY, arrowX, arrowY
			for(int i = 0; i < 6; i++) {
				coords[i] = move & 0xf;
				move = move >> 4;
			}
			
			// Undo the arrow move neighbour updates
			int blanks = 0;
			for(int j = -1; j < 2; j++) {
				for(int i = -1; i < 2; i++) {
					if(!(((coords[5] + j) < 0) || ((coords[5] + j) > 9) || ((coords[4] + i) < 0) || ((coords[4] + i) > 9))) {
						if(this.board[coords[5] + j][coords[4] + i] instanceof Blank) {
							blanks++;
							((Blank)this.board[coords[5] + j][coords[4] + i]).incrementEmptyNeighbours();
						}
					}
				}
			}
			
			// Set the arrow square to a blank square
			this.board[coords[5]][coords[4]] = new Blank((byte)coords[5], (byte)coords[4], (byte)blanks);
			
			// Move the queen the opposite direction
			movePiece(coords[2], coords[3], coords[0], coords[1]);
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
				
				numBlanks--;
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
				list.addAll(genQueenMovesHelper(x, y, 1, WHITE, updates, numBlanks));
			}
			
			if(!list.isEmpty()){
				generateQueenMoves(list, 2, WHITE, updates, numBlanks);
			}
			
			list.clear();
			updates[0] = 0;
			
			// Make steps for each black queen on the board
			for(BlackQueen queen : B_pieces) {
				int[] pos = queen.position();
				int x = pos[0];
				int y = pos[1];
				
				// Append the Blank spaces generated from this queen's position to the new list
				list.addAll(genQueenMovesHelper(x, y, 1, BLACK, updates, numBlanks));
			}
				
			if(!list.isEmpty()){
				generateQueenMoves(list, 2, BLACK, updates, numBlanks);
			}
		}
		
		// A helper function to assist in calculating the smallest distance to every square for a player.
		// It accepts a list of Blanks to move from next, a step counter to determine how many steps we're
		// taking at this point, and a flag for which player we're counting steps for.  This functions calls
		// itself recursively for as long as list isn't blank.
		private void generateQueenMoves(ArrayList<Blank> list, int stepCount, int player, int[] updates, int numBlanks) {
			ArrayList<Blank> newList = new ArrayList<Blank>();
			
			for(Blank square : list) {
				int[] pos = square.position();
				int x = pos[0];
				int y = pos[1];
				
				newList.addAll(genQueenMovesHelper(x, y, stepCount, player, updates, numBlanks));
				
				// Stop iterating through squares if we've updates all possible Blanks
				if(updates[0] == numBlanks) {
					newList.clear();
					break;
				}
			}
			
			// If our new list isn't empty, re-run the function with the new squares and an incremented step count
			if(!newList.isEmpty()) {
				generateQueenMoves(newList, ++stepCount, player, updates, numBlanks);
			}
		}
		
		// The meat and potatoes of the queen move generator.  Handles out of bounds checking, only writing
		// a value if it's smaller than the square's current distance, and stops checking a direction if we reach an obstacle.
		private ArrayList<Blank> genQueenMovesHelper(int x, int y, int stepCount, int player, int[] updates, int numBlanks) {
			ArrayList<Blank> list = new ArrayList<Blank>();
			
			// Set up some variables for the iteration
			boolean done = false, U = false, D = false, L = false, R = false, UR = false, DL = false, UL = false, DR = false;
			int stepSize = 1;
			
			do {
				// Break out if we've updated all the blank squares already
				if(updates[0] == numBlanks) {
					break;
				}
				
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
				
				if(player == WHITE) {
					// Need to look up
					if(!U) {
						if(board[up][x].val() != 0) {
							U = true;
						} else {
							if(((Blank)board[up][x]).wq > stepCount) {
								// We don't want to add Blanks to the list that have already been added
								((Blank)board[up][x]).setQueenMoves(stepCount, player);
								list.add((Blank) board[up][x]);
								updates[0]++;
							}	
						}
					}
					
					// Need to look down
					if(!D) {
						if(board[down][x].val() != 0) {
							D = true;
						} else if(((Blank)board[down][x]).wq > stepCount) {
							((Blank)board[down][x]).setQueenMoves(stepCount, player);
							list.add((Blank) board[down][x]);
							updates[0]++;
						}
					}
					
					// Need to look left
					if(!L) {
						if(board[y][left].val() != 0) {
							L = true;
						} else if(((Blank)board[y][left]).wq > stepCount) {
							((Blank)board[y][left]).setQueenMoves(stepCount, player);
							list.add((Blank) board[y][left]);
							updates[0]++;
						}
					}
					
					// Need to look right
					if(!R) {
						if(board[y][right].val() != 0) {
							R = true;
						} else if(((Blank)board[y][right]).wq > stepCount) {
							((Blank)board[y][right]).setQueenMoves(stepCount, player);
							list.add((Blank) board[y][right]);
							updates[0]++;
						}
					}
					
					// Need to look up-right
					if(!UR) {
						if(board[up][right].val() != 0) {
							UR = true;
						} else if(((Blank)board[up][right]).wq > stepCount) {
							((Blank)board[up][right]).setQueenMoves(stepCount, player);
							list.add((Blank) board[up][right]);
							updates[0]++;
						}
					}
					
					// Need to look up-left
					if(!UL) {
						if(board[up][left].val() != 0) {
							UL = true;
						} else if(((Blank)board[up][left]).wq > stepCount) {
							((Blank)board[up][left]).setQueenMoves(stepCount, player);
							list.add((Blank) board[up][left]);
							updates[0]++;
						}
					}
					
					// Need to look down-left
					if(!DL) {
						if(board[down][left].val() != 0) {
							DL = true;
						} else if(((Blank)board[down][left]).wq > stepCount) {
							((Blank)board[down][left]).setQueenMoves(stepCount, player);
							list.add((Blank) board[down][left]);
							updates[0]++;
						}
					}
					
					// Need to look down-right
					if(!DR) {
						if(board[down][right].val() != 0) {
							DR = true;
						} else if(((Blank)board[down][right]).wq > stepCount) {
							((Blank)board[down][right]).setQueenMoves(stepCount, player);
							list.add((Blank) board[down][right]);
							updates[0]++;
						}
					}
				} else {
					// Need to look up
					if(!U) {
						if(board[up][x].val() != 0) {
							U = true;
						} else {
							if(((Blank)board[up][x]).bq > stepCount) {
								((Blank)board[up][x]).setQueenMoves(stepCount, player);
								list.add((Blank) board[up][x]);
								updates[0]++;
							}	
						}
					}
					
					// Need to look down
					if(!D) {
						if(board[down][x].val() != 0) {
							D = true;
						} else if(((Blank)board[down][x]).bq > stepCount) {
							((Blank)board[down][x]).setQueenMoves(stepCount, player);
							list.add((Blank) board[down][x]);
							updates[0]++;
						}
					}
					
					// Need to look left
					if(!L) {
						if(board[y][left].val() != 0) {
							L = true;
						} else if(((Blank)board[y][left]).bq > stepCount) {
							((Blank)board[y][left]).setQueenMoves(stepCount, player);
							list.add((Blank) board[y][left]);
							updates[0]++;
						}
					}
					
					// Need to look right
					if(!R) {
						if(board[y][right].val() != 0) {
							R = true;
						} else if(((Blank)board[y][right]).bq > stepCount) {
							((Blank)board[y][right]).setQueenMoves(stepCount, player);
							list.add((Blank) board[y][right]);
							updates[0]++;
						}
					}
					
					// Need to look up-right
					if(!UR) {
						if(board[up][right].val() != 0) {
							UR = true;
						} else if(((Blank)board[up][right]).bq > stepCount) {
							((Blank)board[up][right]).setQueenMoves(stepCount, player);
							list.add((Blank) board[up][right]);
							updates[0]++;
						}
					}
					
					// Need to look up-left
					if(!UL) {
						if(board[up][left].val() != 0) {
							UL = true;
						} else if(((Blank)board[up][left]).bq > stepCount) {
							((Blank)board[up][left]).setQueenMoves(stepCount, player);
							list.add((Blank) board[up][left]);
							updates[0]++;
						}
					}
					
					// Need to look down-left
					if(!DL) {
						if(board[down][left].val() != 0) {
							DL = true;
						} else if(((Blank)board[down][left]).bq > stepCount) {
							((Blank)board[down][left]).setQueenMoves(stepCount, player);
							list.add((Blank) board[down][left]);
							updates[0]++;
						}
					}
					
					// Need to look down-right
					if(!DR) {
						if(board[down][right].val() != 0) {
							DR = true;
						} else if(((Blank)board[down][right]).bq > stepCount) {
							((Blank)board[down][right]).setQueenMoves(stepCount, player);
							list.add((Blank) board[down][right]);
							updates[0]++;
						}
					}
				}
				
				stepSize++;
			} while(!done);
			
			return list;
		}
		
		// Generate a King-step map for every blank square on the board for each player.
		private void generateKingMoves() {
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
				list.addAll(genKingMovesHelper(x, y, 1, WHITE, updates));
			}
			
			if(!list.isEmpty()) {
				generateKingMoves(list, 2, WHITE, updates, numBlanks);
			}
			
			list.clear();
			updates[0] = 0;
			
			// Make steps for each black queen on the board
			for(BlackQueen queen : B_pieces) {
				int[] pos = queen.position();
				int x = pos[0];
				int y = pos[1];
				
				// Append the Blank spaces generated from this queen's position to the new list
				list.addAll(genKingMovesHelper(x, y, 1, BLACK, updates));
			}
			
			if(!list.isEmpty()) {
				generateKingMoves(list, 2, BLACK, updates, numBlanks);
			}
		}
		
		private void generateKingMoves(ArrayList<Blank> list, int stepCount, int player, int[] updates, int numBlanks) {
			ArrayList<Blank> newList = new ArrayList<Blank>();
			
			for(Blank square : list) {
				int[] pos = square.position();
				int x = pos[0];
				int y = pos[1];
				
				newList.addAll(genKingMovesHelper(x, y, stepCount, player, updates));
				
				if(updates[0] == numBlanks) {
					newList.clear();
					break;
				}
			}
			
			// If our new list isn't empty, re-run the function with the new squares and an incremented step count
			if(!newList.isEmpty()) {
				generateKingMoves(newList, ++stepCount, player, updates, numBlanks);
			}
		}
		
		private ArrayList<Blank> genKingMovesHelper(int x, int y, int stepCount, int player, int[] updates) {
			ArrayList<Blank> list = new ArrayList<Blank>();
			
			// Convenience booleans for bounds checking
			boolean U = false, D = false, L = false, R = false, UR = false, DL = false, UL = false, DR = false;
			
			// Only make a step of size one for measuring king moves
			int up = y + 1;
			int down = y - 1;
			int right = x + 1;
			int left = x - 1;
			
			// Check boundaries
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
			
			if(player == WHITE) {
				// Check up
				if(!U) {
					if(board[up][x].val() == 0) {
						if(((Blank)board[up][x]).wk > stepCount) {
							((Blank)board[up][x]).setKingMoves(stepCount, player);
							list.add((Blank)board[up][x]);
							updates[0]++;
						}
					}
				}
				
				// Check down
				if(!D) {
					if(board[down][x].val() == 0) {
						if(((Blank)board[down][x]).wk > stepCount) {
							((Blank)board[down][x]).setKingMoves(stepCount, player);
							list.add((Blank)board[down][x]);
							updates[0]++;
						}
					}
				}
				
				// Check left
				if(!L) {
					if(board[y][left].val() == 0) {
						if(((Blank)board[y][left]).wk > stepCount) {
							((Blank)board[y][left]).setKingMoves(stepCount, player);
							list.add((Blank)board[y][left]);
							updates[0]++;
						}
					}
				}
				
				// Check right
				if(!R) {
					if(board[y][right].val() == 0) {
						if(((Blank)board[y][right]).wk > stepCount) {
							((Blank)board[y][right]).setKingMoves(stepCount, player);
							list.add((Blank)board[y][right]);
							updates[0]++;
						}
					}
				}
				
				// Check up-right
				if(!UR) {
					if(board[up][right].val() == 0) {
						if(((Blank)board[up][right]).wk > stepCount) {
							((Blank)board[up][right]).setKingMoves(stepCount, player);
							list.add((Blank)board[up][right]);
							updates[0]++;
						}
					}
				}
				
				// Check up-left
				if(!UL) {
					if(board[up][left].val() == 0) {
						if(((Blank)board[up][left]).wk > stepCount) {
							((Blank)board[up][left]).setKingMoves(stepCount, player);
							list.add((Blank)board[up][left]);
							updates[0]++;
						}
					}
				}
				
				// Check down-left
				if(!DL) {
					if(board[down][left].val() == 0) {
						if(((Blank)board[down][left]).wk > stepCount) {
							((Blank)board[down][left]).setKingMoves(stepCount, player);
							list.add((Blank)board[down][left]);
							updates[0]++;
						}
					}
				}
				
				// Check down-right
				if(!DR) {
					if(board[down][right].val() == 0) {
						if(((Blank)board[down][right]).wk > stepCount) {
							((Blank)board[down][right]).setKingMoves(stepCount, player);
							list.add((Blank)board[down][right]);
							updates[0]++;
						}
					}
				}
			} else {
				// Check up
				if(!U) {
					if(board[up][x].val() == 0) {
						if(((Blank)board[up][x]).bk > stepCount) {
							((Blank)board[up][x]).setKingMoves(stepCount, player);
							list.add((Blank)board[up][x]);
							updates[0]++;
						}
					}
				}
				
				// Check down
				if(!D) {
					if(board[down][x].val() == 0) {
						if(((Blank)board[down][x]).bk > stepCount) {
							((Blank)board[down][x]).setKingMoves(stepCount, player);
							list.add((Blank)board[down][x]);
							updates[0]++;
						}
					}
				}
				
				// Check left
				if(!L) {
					if(board[y][left].val() == 0) {
						if(((Blank)board[y][left]).bk > stepCount) {
							((Blank)board[y][left]).setKingMoves(stepCount, player);
							list.add((Blank)board[y][left]);
							updates[0]++;
						}
					}
				}
				
				// Check right
				if(!R) {
					if(board[y][right].val() == 0) {
						if(((Blank)board[y][right]).bk > stepCount) {
							((Blank)board[y][right]).setKingMoves(stepCount, player);
							list.add((Blank)board[y][right]);
							updates[0]++;
						}
					}
				}
				
				// Check up-right
				if(!UR) {
					if(board[up][right].val() == 0) {
						if(((Blank)board[up][right]).bk > stepCount) {
							((Blank)board[up][right]).setKingMoves(stepCount, player);
							list.add((Blank)board[up][right]);
							updates[0]++;
						}
					}
				}
				
				// Check up-left
				if(!UL) {
					if(board[up][left].val() == 0) {
						if(((Blank)board[up][left]).bk > stepCount) {
							((Blank)board[up][left]).setKingMoves(stepCount, player);
							list.add((Blank)board[up][left]);
							updates[0]++;
						}
					}
				}
				
				// Check down-left
				if(!DL) {
					if(board[down][left].val() == 0) {
						if(((Blank)board[down][left]).bk > stepCount) {
							((Blank)board[down][left]).setKingMoves(stepCount, player);
							list.add((Blank)board[down][left]);
							updates[0]++;
						}
					}
				}
				
				// Check down-right
				if(!DR) {
					if(board[down][right].val() == 0) {
						if(((Blank)board[down][right]).bk > stepCount) {
							((Blank)board[down][right]).setKingMoves(stepCount, player);
							list.add((Blank)board[down][right]);
							updates[0]++;
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
			
			// The weighted positional advantage score
			// f1(w)t1 + f2(w)c1 + f3(w)c2 + f4(w)t2 | 0 <= fi(w) <= 1; SUM(fi(w)) = 1
			T = (float)Math.pow(1.2, (-w)*0.25)*t1 + (1/7)*(float)(1 - Math.pow(1.2, (-w)*0.25))*c1 + (2/7)*(float)(1 - Math.pow(1.2, (-w)*0.25))*c2 + (4/7)*(float)(1 - Math.pow(1.2, (-w)*0.25))*t2;
			
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
			
			System.out.println("t1: " + t1 + "\tc1: " + c1 + "\tc2: " + c2 + "\tt2: " + t2 + "\nOmega: " + w + "\tT: " + T + "\nBA: " + m2 + "\tWA: " + m1 + "\tM: " + m);
			
			return val;
		}
		
        ///////////////////////////////////////////////////////////////////////////////////////////
		// Printing methods for testing purposes only, remove these when code is ready to launch //
		///////////////////////////////////////////////////////////////////////////////////////////
		
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
			generateQueenMoves();
			
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
			generateKingMoves();
			
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
}
