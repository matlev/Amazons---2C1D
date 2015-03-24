import java.util.ArrayList;

public class AI {
	
	public static int leaves = 0;
	private final int MAX_DEPTH = 2, WHITE = 1, BLACK = 2; // Tinker with depth
	private int player;
	private Node root;
	Gameboard g_board;
	Gamepiece[][] board;
	
	// Set the AI to be a certain player (white or black) and give it its own instance of the g_board to make moves on
	public AI(Gameboard g_board, int player) {
		this.g_board = g_board;
		board = g_board.getBoard();
		this.player = player;
		root = new Node();
	}
	
	// Runs a a search for the best move in the current position of the g_board.  Initially uses an alpha-beta minimax search
	// and switches to a heuristic-influenced Monte Carlo simulation once the g_board is fully partitioned.  Returns a string
	// array containing the queen move and the square the arrow is shot to.
	public String[] search() {
		
		/*
		 * Interrupt the think() thread so we can have the updated game tree
		 */
		
		String[] move = new String[2];
		
		// Find the max or min value for the white or black player respectively
		float val = alphaBeta(root, MAX_DEPTH, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, player);
		
		// Retrieve the move that has the min/max vlaue
		ArrayList<Node> moves = root.getChildren();
		for(Node n : moves) {
			if(Float.compare(n.value, val) == 0) {
				int[] m = new int[6];
				int encodedM = n.move;
				
				// Decode the move
				for(int i = 0; i < 6; i++) {
					m[i] = encodedM & 0xf;
					encodedM >>= 4;
				}
				
				// Convert the array into a string array with the format [0] => "a7-b7", [1] => "b5"
				move[0] = ((char)(m[0] + 97)) + "" + (m[1] + 1) + "-" + ((char)(m[2] + 97)) + "" + (m[3] + 1);
				move[1] = ((char)(m[4] + 97)) + "" + (m[5] + 1);
				
				// Play the move on the AI's g_board, set the root to this node and break out of the loop
				g_board.doMove(n.move);
				root = n;
				break;
			}
		}
		
		/*
		 * Start the think() thread
		 */
		
		return move;
	}
	
	// Runs an alpha-beta minimax simulation on a game tree populated on the fly.  Rather than finding
	// all legal moves for a position and storing them in a node's children list, the moves are generated,
	// added and explored one at a time.  This way when A >= B and alpha-beta breaks out of the child node
	// search, the program can just stop generating new legal moves and move back up the tree.  This saves
	// memory and time.
	private float alphaBeta(Node n, int depth, double alpha, double beta, int player) {
		
		// Only do a depth check for termination, as this implementation passes nodes that initially have empty
		// successor moves (so we can't test for terminal node at this step)
		if(depth == 0) {
			leaves++;
			
			float score = g_board.evaluate(player);
			n.setScore(score);
			
			return score;
		}
		
		if(n.getChildren() == null || n.getChildren().isEmpty()) {
			// Send the parent information to the move generator, which will recursively call alpha-beta with the child information
			return generateLegalMoves(n, depth, alpha, beta, player);
		} else {
			// If we have a pre-constructed tree already, we can run normal alphaBeta
			ArrayList<Node> moves = n.getChildren();
			
			// White
			if(player == 1) {
				double val = Double.NEGATIVE_INFINITY;
				
				for(Node child : moves) {
					val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, (3 - player)));
					alpha = Math.max(val, alpha);
					if(alpha >= beta) {
						break;
					}
				}
				
				return (float)val;
			// Black
			} else {
				double val = Double.POSITIVE_INFINITY;
				
				for(Node child : moves) {
					val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, (3 - player)));
					beta = Math.min(val, beta);
					if(alpha >= beta) {
						break;
					}
				}
				
				return (float)val;
			}
		}	
	}
	
	// Generates legal moves and runs alphaBeta on each one.  At any point, if alpha >= beta then 
	// the method stops generating moves for the current parent and returns a value.
	private float generateLegalMoves(Node n, int depth, double alpha, double beta, int player) {
		double val = (player == 1 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
		
		Gamepiece[] playerPieces = (player == 1 ? g_board.getWhiteQueens() : g_board.getBlackQueens());
		
		for(Gamepiece Queen : playerPieces) {
			int[] pos = Queen.position();
			
			int x = pos[0];
			int y = pos[1];
			int to_x = 0, to_y = 0, a_x = 0, a_y = 0;
			
			// Try to move up
			for(int i = 1; i < (10 - y) && board[y + i][x] instanceof Blank; i++) {
				to_x = x;
				to_y = (y + i);
				
				// Try to shoot up
				for(int j = 1; j < (10 - to_y) && board[to_y + j][to_x] instanceof Blank; j++) {
					// Finish constructing the move
					a_x = to_x;
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					// Create a new node with the move and this node as its parent
					Node child = new Node(move, n);
					n.addChild(child);
					
					// Play the move on the AI board
					g_board.doMove(move);
					
					// Recursive call back to alphaBeta to search deeper
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					// Undo the move made at this level
					g_board.undoMove();
					
					// If we've reached a point where alpha >= beta, break out of the entire move generation process
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot right
				for(int j = 1; j < (10 - to_x) && board[to_y][to_x + j] instanceof Blank; j++) {
					a_x = to_x + j;
					a_y = to_y;
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down (with special case => can shoot to queen's origin square)
				for(int j = 1; j <= to_y && (board[to_y - j][to_x] instanceof Blank || (to_y - j == y)); j++) {
					a_x = to_x;
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot left
				for(int j = 1; j <= to_x && board[to_y][to_x - j] instanceof Blank; j++) {
					a_x = (to_x - j);
					a_y = to_y;
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot up-right
				for(int j = 1; j < (10 - to_y) && j < (10 - to_x) && board[to_y + j][to_x + j] instanceof Blank; j++) {
					a_x = (to_x + j);
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down-right
				for(int j = 1; j <= to_y && j < (10 - to_x) && board[to_y - j][to_x + j] instanceof Blank; j++) {
					a_x = (to_x + j);
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot up-left
				for(int j = 1; j < (10 - to_y) && j <= to_x && board[to_y + j][to_x - j] instanceof Blank; j++) {
					a_x = (to_x - j);
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down-left
				for(int j = 1; j <= to_y && j <= to_x && board[to_y - j][to_x - j] instanceof Blank; j++) {
					a_x = (to_x - j);
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
			}
			
			if(alpha >= beta) {
				break;
			}
			
			// Try to move right
			for(int i = 1; i < (10 - x) && board[y][x + i] instanceof Blank; i++) {
				to_x = x + i;
				to_y = y;
				
				// Try to shoot up
				for(int j = 1; j < (10 - to_y) && board[to_y + j][to_x] instanceof Blank; j++) {
					// Finish constructing the move
					a_x = to_x;
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					// Create a new node with the move and this node as its parent
					Node child = new Node(move, n);
					n.addChild(child);
					
					// Play the move on the AI board
					g_board.doMove(move);
					
					// Recursive call back to alphaBeta to search deeper
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					// Undo the move made at this level
					g_board.undoMove();
					
					// If we've reached a point where alpha >= beta, break out of the entire move generation process
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot right
				for(int j = 1; j < (10 - to_x) && board[to_y][to_x + j] instanceof Blank; j++) {
					a_x = to_x + j;
					a_y = to_y;
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down 
				for(int j = 1; j <= to_y && board[to_y - j][to_x] instanceof Blank; j++) {
					a_x = to_x;
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot left (with special case => can shoot to queen's origin square)
				for(int j = 1; j <= to_x && (board[to_y][to_x - j] instanceof Blank || (to_x - j) == x); j++) {
					a_x = (to_x - j);
					a_y = to_y;
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot up-right
				for(int j = 1; j < (10 - to_y) && j < (10 - to_x) && board[to_y + j][to_x + j] instanceof Blank; j++) {
					a_x = (to_x + j);
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down-right
				for(int j = 1; j <= to_y && j < (10 - to_x) && board[to_y - j][to_x + j] instanceof Blank; j++) {
					a_x = (to_x + j);
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot up-left
				for(int j = 1; j < (10 - to_y) && j <= to_x && board[to_y + j][to_x - j] instanceof Blank; j++) {
					a_x = (to_x - j);
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down-left
				for(int j = 1; j <= to_y && j <= to_x && board[to_y - j][to_x - j] instanceof Blank; j++) {
					a_x = (to_x - j);
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
			}
			
			if(alpha >= beta) {
				break;
			}
			
			// Try to move down
			for(int i = 1; i <= y && board[y - i][x] instanceof Blank; i++) {
				to_x = x;
				to_y = y - i;
				
				// Try to shoot up (With special case arrow loc = queen start)
				for(int j = 1; j < (10 - to_y) && (board[to_y + j][to_x] instanceof Blank || to_y + j == y); j++) {
					// Finish constructing the move
					a_x = to_x;
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					// Create a new node with the move and this node as its parent
					Node child = new Node(move, n);
					n.addChild(child);
					
					// Play the move on the AI board
					g_board.doMove(move);
					
					// Recursive call back to alphaBeta to search deeper
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					// Undo the move made at this level
					g_board.undoMove();
					
					// If we've reached a point where alpha >= beta, break out of the entire move generation process
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot right
				for(int j = 1; j < (10 - to_x) && board[to_y][to_x + j] instanceof Blank; j++) {
					a_x = to_x + j;
					a_y = to_y;
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down 
				for(int j = 1; j <= to_y && board[to_y - j][to_x] instanceof Blank; j++) {
					a_x = to_x;
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot left (with special case => can shoot to queen's origin square)
				for(int j = 1; j <= to_x && (board[to_y][to_x - j] instanceof Blank || (to_x - j) == x); j++) {
					a_x = (to_x - j);
					a_y = to_y;
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot up-right
				for(int j = 1; j < (10 - to_y) && j < (10 - to_x) && board[to_y + j][to_x + j] instanceof Blank; j++) {
					a_x = (to_x + j);
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down-right
				for(int j = 1; j <= to_y && j < (10 - to_x) && board[to_y - j][to_x + j] instanceof Blank; j++) {
					a_x = (to_x + j);
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot up-left
				for(int j = 1; j < (10 - to_y) && j <= to_x && board[to_y + j][to_x - j] instanceof Blank; j++) {
					a_x = (to_x - j);
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down-left
				for(int j = 1; j <= to_y && j <= to_x && board[to_y - j][to_x - j] instanceof Blank; j++) {
					a_x = (to_x - j);
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
			}
			
			if(alpha >= beta) {
				break;
			}
			
			// Try to move left
			for(int i = 1; i <= x && board[y][x - i] instanceof Blank; i++) {
				to_x = x - i;
				to_y = y;
				
				// Try to shoot up
				for(int j = 1; j < (10 - to_y) && board[to_y + j][to_x] instanceof Blank; j++) {
					// Finish constructing the move
					a_x = to_x;
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					// Create a new node with the move and this node as its parent
					Node child = new Node(move, n);
					n.addChild(child);
					
					// Play the move on the AI board
					g_board.doMove(move);
					
					// Recursive call back to alphaBeta to search deeper
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					// Undo the move made at this level
					g_board.undoMove();
					
					// If we've reached a point where alpha >= beta, break out of the entire move generation process
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot right (with special case)
				for(int j = 1; j < (10 - to_x) && (board[to_y][to_x + j] instanceof Blank || (to_x + j == x)); j++) {
					a_x = to_x + j;
					a_y = to_y;
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down 
				for(int j = 1; j <= to_y && board[to_y - j][to_x] instanceof Blank; j++) {
					a_x = to_x;
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot left (with special case => can shoot to queen's origin square)
				for(int j = 1; j <= to_x && (board[to_y][to_x - j] instanceof Blank || (to_x - j) == x); j++) {
					a_x = (to_x - j);
					a_y = to_y;
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot up-right
				for(int j = 1; j < (10 - to_y) && j < (10 - to_x) && board[to_y + j][to_x + j] instanceof Blank; j++) {
					a_x = (to_x + j);
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down-right
				for(int j = 1; j <= to_y && j < (10 - to_x) && board[to_y - j][to_x + j] instanceof Blank; j++) {
					a_x = (to_x + j);
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot up-left
				for(int j = 1; j < (10 - to_y) && j <= to_x && board[to_y + j][to_x - j] instanceof Blank; j++) {
					a_x = (to_x - j);
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down-left
				for(int j = 1; j <= to_y && j <= to_x && board[to_y - j][to_x - j] instanceof Blank; j++) {
					a_x = (to_x - j);
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
			}
			
			if(alpha >= beta) {
				break;
			}
			
			// Try to move up-right
			for(int i = 1; i < (10 - y) && i < (10 - x) && board[y + i][x + i] instanceof Blank; i++) {
				to_x = x + i;
				to_y = y + i;
				
				// Try to shoot up
				for(int j = 1; j < (10 - to_y) && board[to_y + j][to_x] instanceof Blank; j++) {
					// Finish constructing the move
					a_x = to_x;
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					// Create a new node with the move and this node as its parent
					Node child = new Node(move, n);
					n.addChild(child);
					
					// Play the move on the AI board
					g_board.doMove(move);
					
					// Recursive call back to alphaBeta to search deeper
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					// Undo the move made at this level
					g_board.undoMove();
					
					// If we've reached a point where alpha >= beta, break out of the entire move generation process
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot right
				for(int j = 1; j < (10 - to_x) && board[to_y][to_x + j] instanceof Blank; j++) {
					a_x = to_x + j;
					a_y = to_y;
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down 
				for(int j = 1; j <= to_y && board[to_y - j][to_x] instanceof Blank; j++) {
					a_x = to_x;
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot left
				for(int j = 1; j <= to_x && board[to_y][to_x - j] instanceof Blank; j++) {
					a_x = (to_x - j);
					a_y = to_y;
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot up-right
				for(int j = 1; j < (10 - to_y) && j < (10 - to_x) && board[to_y + j][to_x + j] instanceof Blank; j++) {
					a_x = (to_x + j);
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down-right
				for(int j = 1; j <= to_y && j < (10 - to_x) && board[to_y - j][to_x + j] instanceof Blank; j++) {
					a_x = (to_x + j);
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot up-left
				for(int j = 1; j < (10 - to_y) && j <= to_x && board[to_y + j][to_x - j] instanceof Blank; j++) {
					a_x = (to_x - j);
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down-left (with special case)
				for(int j = 1; j <= to_y && j <= to_x && (board[to_y - j][to_x - j] instanceof Blank || ((to_y - j) == y && (to_x - j) == x)); j++) {
					a_x = (to_x - j);
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
			}
			
			if(alpha >= beta) {
				break;
			}
			
			// Try to move down-right
			for(int i = 1; i <= y && i < (10 - x) && board[y - i][x + i] instanceof Blank; i++) {
				to_x = x + i;
				to_y = y - i;
				
				// Try to shoot up
				for(int j = 1; j < (10 - to_y) && board[to_y + j][to_x] instanceof Blank; j++) {
					// Finish constructing the move
					a_x = to_x;
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					// Create a new node with the move and this node as its parent
					Node child = new Node(move, n);
					n.addChild(child);
					
					// Play the move on the AI board
					g_board.doMove(move);
					
					// Recursive call back to alphaBeta to search deeper
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					// Undo the move made at this level
					g_board.undoMove();
					
					// If we've reached a point where alpha >= beta, break out of the entire move generation process
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot right
				for(int j = 1; j < (10 - to_x) && board[to_y][to_x + j] instanceof Blank; j++) {
					a_x = to_x + j;
					a_y = to_y;
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down 
				for(int j = 1; j <= to_y && board[to_y - j][to_x] instanceof Blank; j++) {
					a_x = to_x;
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot left
				for(int j = 1; j <= to_x && board[to_y][to_x - j] instanceof Blank; j++) {
					a_x = (to_x - j);
					a_y = to_y;
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot up-right
				for(int j = 1; j < (10 - to_y) && j < (10 - to_x) && board[to_y + j][to_x + j] instanceof Blank; j++) {
					a_x = (to_x + j);
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down-right
				for(int j = 1; j <= to_y && j < (10 - to_x) && board[to_y - j][to_x + j] instanceof Blank; j++) {
					a_x = (to_x + j);
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot up-left (with special case)
				for(int j = 1; j < (10 - to_y) && j <= to_x && (board[to_y + j][to_x - j] instanceof Blank || ((to_y + j) == y && (to_x - j) == x)); j++) {
					a_x = (to_x - j);
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down-left
				for(int j = 1; j <= to_y && j <= to_x && board[to_y - j][to_x - j] instanceof Blank; j++) {
					a_x = (to_x - j);
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
			}
			
			if(alpha >= beta) {
				break;
			}
			
			// Try to move up-left
			for(int i = 1; i < (10 - y) && i <= x && board[y + i][x - i] instanceof Blank; i++) {
				to_x = x - i;
				to_y = y + i;
				
				// Try to shoot up
				for(int j = 1; j < (10 - to_y) && board[to_y + j][to_x] instanceof Blank; j++) {
					// Finish constructing the move
					a_x = to_x;
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					// Create a new node with the move and this node as its parent
					Node child = new Node(move, n);
					n.addChild(child);
					
					// Play the move on the AI board
					g_board.doMove(move);
					
					// Recursive call back to alphaBeta to search deeper
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					// Undo the move made at this level
					g_board.undoMove();
					
					// If we've reached a point where alpha >= beta, break out of the entire move generation process
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot right
				for(int j = 1; j < (10 - to_x) && board[to_y][to_x + j] instanceof Blank; j++) {
					a_x = to_x + j;
					a_y = to_y;
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down 
				for(int j = 1; j <= to_y && board[to_y - j][to_x] instanceof Blank; j++) {
					a_x = to_x;
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot left
				for(int j = 1; j <= to_x && board[to_y][to_x - j] instanceof Blank; j++) {
					a_x = (to_x - j);
					a_y = to_y;
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot up-right
				for(int j = 1; j < (10 - to_y) && j < (10 - to_x) && board[to_y + j][to_x + j] instanceof Blank; j++) {
					a_x = (to_x + j);
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down-right (with special case)
				for(int j = 1; j <= to_y && j < (10 - to_x) && (board[to_y - j][to_x + j] instanceof Blank || ((to_y - j) == y && (to_x + j) == x)); j++) {
					a_x = (to_x + j);
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot up-left
				for(int j = 1; j < (10 - to_y) && j <= to_x && board[to_y + j][to_x - j] instanceof Blank; j++) {
					a_x = (to_x - j);
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down-left
				for(int j = 1; j <= to_y && j <= to_x && board[to_y - j][to_x - j] instanceof Blank; j++) {
					a_x = (to_x - j);
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
			}
			
			if(alpha >= beta) {
				break;
			}
			
			// Try to move down-left
			for(int i = 1; i <= y && i <= x && board[y - i][x - i] instanceof Blank; i++) {
				to_x = x - i;
				to_y = y - i;
				
				// Try to shoot up
				for(int j = 1; j < (10 - to_y) && board[to_y + j][to_x] instanceof Blank; j++) {
					// Finish constructing the move
					a_x = to_x;
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					// Create a new node with the move and this node as its parent
					Node child = new Node(move, n);
					n.addChild(child);
					
					// Play the move on the AI board
					g_board.doMove(move);
					
					// Recursive call back to alphaBeta to search deeper
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					// Undo the move made at this level
					g_board.undoMove();
					
					// If we've reached a point where alpha >= beta, break out of the entire move generation process
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot right
				for(int j = 1; j < (10 - to_x) && board[to_y][to_x + j] instanceof Blank; j++) {
					a_x = to_x + j;
					a_y = to_y;
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down 
				for(int j = 1; j <= to_y && board[to_y - j][to_x] instanceof Blank; j++) {
					a_x = to_x;
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot left
				for(int j = 1; j <= to_x && board[to_y][to_x - j] instanceof Blank; j++) {
					a_x = (to_x - j);
					a_y = to_y;
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot up-right (with special case)
				for(int j = 1; j < (10 - to_y) && j < (10 - to_x) && (board[to_y + j][to_x + j] instanceof Blank || ((to_y + j) == y && (to_x + j) == x)); j++) {
					a_x = (to_x + j);
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down-right
				for(int j = 1; j <= to_y && j < (10 - to_x) && board[to_y - j][to_x + j] instanceof Blank; j++) {
					a_x = (to_x + j);
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot up-left
				for(int j = 1; j < (10 - to_y) && j <= to_x && board[to_y + j][to_x - j] instanceof Blank; j++) {
					a_x = (to_x - j);
					a_y = (to_y + j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
				
				// Try to shoot down-left
				for(int j = 1; j <= to_y && j <= to_x && board[to_y - j][to_x - j] instanceof Blank; j++) {
					a_x = (to_x - j);
					a_y = (to_y - j);
					int move = encodeMove(x, y, to_x, to_y, a_x, a_y);
					
					Node child = new Node(move, n);
					n.addChild(child);
					
					g_board.doMove(move);
					
					if(player == 1) {
						val = Math.max(val, alphaBeta(child, depth - 1, alpha, beta, 2));
						alpha = Math.max(val, alpha);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, alpha, beta, 1));
						beta = Math.min(val, beta);
					}
					
					g_board.undoMove();
					
					if(alpha >= beta) {
						break;
					}
				}
				
				if(alpha >= beta) {
					break;
				}
			}
			
			if(alpha >= beta) {
				break;
			}
		}
		
		// If we found no successor moves for this child then this player can't move and therefore loses.
		if(n.getChildren().isEmpty()) {
			val = (float)(player == 1 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
		}
		
		n.setScore((float)val);
		
		return (float)val;
	}
	
	// A function to encode moves
	private static int encodeMove(int p_x, int p_y, int to_x, int to_y, int a_x, int a_y) {
		return p_x + (p_y << 4) + (to_x << 8) + (to_y << 12) + (a_x << 16) + (a_y << 20);
	}
	
	// A method for the AI to play the opponent's move when we receive it
	public void playMove(String from, String to, String arrow) {
		g_board.doMove(from, to, arrow);
	}
	
	// The Node class storing the evaluation, move made and successor moves from the node's position
	private class Node {
		private Node parent;
		private ArrayList<Node> children;
		private float value;
		private int move;
		
		public Node() {
			parent = null;
			children = new ArrayList<Node>();
			value = 0;
			move = 0;
		}
		
		public Node(int move) {
			parent = null;
			children = new ArrayList<Node>();
			value = 0;
			this.move = move;
		}
		
		public Node(int move, Node parent) {
			this.parent = parent;
			children = new ArrayList<Node>();
			value = 0;
			this.move = move;
		}
		
		public void addChild(Node n) {
			this.children.add(n);
		}
		
		public ArrayList<Node> getChildren() {
			return this.children;
		}
		
		public Node getParent() {
			return this.parent;
		}
		
		public void setParent(Node node) {
			this.parent = node;
		}
		
		public void setScore(float val) {
			value = val;
		}
		
		public void trickleScoreUp() {
			this.getParent().setScore(this.value);
		}
	}
}
