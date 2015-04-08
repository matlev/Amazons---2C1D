import java.util.ArrayList;
import java.util.Random;

public class AI {
	// MCTS variables
	Random rand = new Random();
	private ArrayList<Node> firstMoves;
	private long count = 0;
	private final int[][] generateFirstMapList = new int[4][100];
	private int[] shotMapGlob = new int[100];
	private int[] mctsBoard = null;
	private int simulations = 0;
	private static final int GRID = 10;
	private final static int[][] MATRIX = {{-1,-1},{-1,0},{-1,1},{0,1},{1,1},{1,0},{1,-1},{0,-1}};
	
	// Minimax alpha-beta variables
	public int leaves = 0, max_depth = 2;// Tinker with depth
	private static double alpha, beta;
	
	// Shared
	private boolean mcts = false;
	private static final int ARROW = -1, BLANK = 0, WHITE = 1, BLACK = 2; 
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
	
	public AI(Gameboard g_board, int player, boolean MCTS) {
		if(MCTS) {
			this.g_board = g_board;
			this.player = player;
//			mctsBoard = createBoard(g_board.getBoard());
			root = new Node();
			firstMoves = new ArrayList<Node>();
			mcts = true;
		} else {
			new AI(g_board, player);
		}
	}
	
/*
 * **************************
 * MINIMAX ALPHA-BETA METHODS
 * **************************	
 */
	public float getScore() {
		return root.getScore();
	}
	
	// Runs a a search for the best move in the current position of the g_board.  Initially uses an alpha-beta minimax search
	// and switches to a heuristic-influenced Monte Carlo simulation once the g_board is fully partitioned.  Returns a string
	// array containing the queen move and the square the arrow is shot to.
	public String[] search() {
		
		root.setScore(0);
		leaves = 0;
		
		/*
		 * Interrupt the think() thread so we can have the updated game tree
		 */
		
		String[] move = new String[2];
		
		// Find the max or min value for the white or black player respectively
		float val = alphaBeta(root, max_depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, player);
		
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
				move[0] = ((char)(m[0] + 97)) + "" + (m[1]) + "-" + ((char)(m[2] + 97)) + "" + (m[3]);
				move[1] = ((char)(m[4] + 97)) + "" + (m[5]);
				
				// Play the move on the AI's g_board, set the root to this node and break out of the loop
				g_board.doMove(n.move);
				
				// Reset the root rather than set it to a branch since we can currently only achieve a depth of 2-ply
				root.getChildren().clear();
				
				break;
			}
		}
		
//		if(leaves <= 3500) {
//			max_depth++;
//		}
		
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
	private float alphaBeta(Node n, int depth, double A, double B, int player) {
		
		// Only do a depth check for termination, as this implementation passes nodes that initially have empty
		// successor moves (so we can't test for terminal node at this step)
		if(depth == 0) {
			leaves++;
			
			float score = g_board.evaluate(player);
			n.setScore(score);
			
			return score;
		}
		
		if(n.getChildren() == null || n.getChildren().isEmpty()) {
			// Send the parent information to the move generator, which will recursively call A-B with the child information
			return generateLegalMoves(n, depth, A, B, player);
		} else {
			// If we have a pre-constructed tree already, we can run normal alphaBeta
			ArrayList<Node> moves = n.getChildren();
			
			// White
			if(player == WHITE) {
				double val = Double.NEGATIVE_INFINITY;
				
				for(Node child : moves) {
					val = Math.max(val, alphaBeta(child, depth - 1, A, B, (BLACK)));
					A = Math.max(val, A);
					if(A >= B) {
						break;
					}
				}
				
				return (float)val;
			// Black
			} else {
				double val = Double.POSITIVE_INFINITY;
				
				for(Node child : moves) {
					val = Math.min(val, alphaBeta(child, depth - 1, A, B, (WHITE)));
					B = Math.min(val, B);
					if(A >= B) {
						break;
					}
				}
				
				return (float)val;
			}
		}	
	}
	
	// Generates legal moves and runs alphaBeta on each one.  At any point, if A >= B then 
	// the method stops generating moves for the current parent and returns a value.
	private float generateLegalMoves(Node n, int depth, double A, double B, int player) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					// Undo the move made at this level
					g_board.undoMove();
					
					// If we've reached a point where A >= B, break out of the entire move generation process
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
					break;
				}
			}
			
			if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					// Undo the move made at this level
					g_board.undoMove();
					
					// If we've reached a point where A >= B, break out of the entire move generation process
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
					break;
				}
			}
			
			if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					// Undo the move made at this level
					g_board.undoMove();
					
					// If we've reached a point where A >= B, break out of the entire move generation process
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
					break;
				}
			}
			
			if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					// Undo the move made at this level
					g_board.undoMove();
					
					// If we've reached a point where A >= B, break out of the entire move generation process
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
					break;
				}
			}
			
			if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					// Undo the move made at this level
					g_board.undoMove();
					
					// If we've reached a point where A >= B, break out of the entire move generation process
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
					break;
				}
			}
			
			if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					// Undo the move made at this level
					g_board.undoMove();
					
					// If we've reached a point where A >= B, break out of the entire move generation process
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
					break;
				}
			}
			
			if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					// Undo the move made at this level
					g_board.undoMove();
					
					// If we've reached a point where A >= B, break out of the entire move generation process
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
					break;
				}
			}
			
			if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					// Undo the move made at this level
					g_board.undoMove();
					
					// If we've reached a point where A >= B, break out of the entire move generation process
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
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
						val = Math.max(val, alphaBeta(child, depth - 1, A, B, 2));
						A = Math.max(val, A);
					} else {
						val = Math.min(val, alphaBeta(child, depth - 1, A, B, 1));
						B = Math.min(val, B);
					}
					
					g_board.undoMove();
					
					if(A >= B) {
						break;
					}
				}
				
				if(A >= B) {
					break;
				}
			}
			
			if(A >= B) {
				break;
			}
		}
		
		// If we found no successor moves for this child then this player can't move and therefore loses.
		if(n.getChildren() == null || n.getChildren().isEmpty()) {
//			val = (float)(player == 1 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
//			leaves++;
			val = alphaBeta(n, 0, A, B, player);
		}
		
		n.setScore((float)val);
		
		return (float)val;
	}
	
	// A function to encode moves
	private static int encodeMove(int p_x, int p_y, int to_x, int to_y, int a_x, int a_y) {
		return p_x + (p_y << 4) + (to_x << 8) + (to_y << 12) + (a_x << 16) + (a_y << 20);
	}
	
	public void playMove(int move) {
		g_board.doMove(move);
	}
	
	public void playMoveServer(String from, String to, String arrow) {
		char qfromXChar = from.charAt(0);
		char qtoXChar = to.charAt(0);
		char arrowXChar = arrow.charAt(0);
		
		int qfromX = (qfromXChar > 96 ? qfromXChar - 97 : qfromXChar - 65);
		int qfromY = Integer.parseInt(from.substring(1));
		int qtoX = (qtoXChar > 96 ? qtoXChar - 97 : qtoXChar - 65);
		int qtoY = Integer.parseInt(to.substring(1));
		int arrowX = (arrowXChar > 96 ? arrowXChar - 97 : arrowXChar - 65);
		int arrowY = Integer.parseInt(arrow.substring(1));
		
		int move = qfromX + (qfromY << 4) + (qtoX << 8) + (qtoY << 12) + (arrowX << 16) + (arrowY << 20);
		g_board.doMove(move);
	}
	
	// A method for the AI to play the opponent's move when we receive it
	public void playMove(String from, String to, String arrow) {
		g_board.doMove(from, to, arrow);
	}
	
/*
 * *******************************
 * MONTE CARLO TREE SEARCH METHODS
 * *******************************
 */

	// Runs an UCB MCTS given a starting position.  First generates a list of moves and randomly selects
	// one as its initial simulation point.  It then updates and maintains a "confidence" score for each
	// node and selects the next node to simulate with the highest score. Returns the node with the highest
	// win/loss ratio.
	public int[] mcts() {
		// Get first set of children
		if(this.firstMoves.isEmpty()) {
			generateFirstMap(root, mctsBoard, player);
		}
		
		// Initialize simulation variables
		long timer = System.currentTimeMillis();
		Node maxNode;
		int[] gboard;
		
		// Run simulations for 28 seconds.  On the first simulation randomly select a first move to explore,
		// otherwise calculate the UCB scores for each first child, select the highest scoring node and simulate it.
		do {
			maxNode = null;
			gboard = mctsBoard.clone();
			
			if(simulations == 0) {
				Random rand = new Random();
				maxNode = (Node)firstMoves.get(rand.nextInt(firstMoves.size()));
			} else {
				float maxScore = 0;
				
				for(Node child : firstMoves) {
					calculatePickMeScore(child);
					
					if(child.value > maxScore) {
						maxScore = child.value;
						maxNode = child;
					}
				}
			}
			
			maxNode.simulations++;
			
			simulate(maxNode, gboard, player);
			simulations++;
		} while((System.currentTimeMillis() - timer) < 28);
		
		// ****************************************************************************************************************************
		// Recalculates the score for each node as a percentage of the highest simulation count.  So the node
		// that has the highest simulation count will have an unchanged win/loss ratio, while other nodes with a lower 
		// sim count will have a reduced score to reflect the confidence of that win %age.  In this way, a node that had
		// slightly less simulations than the highest simulated node - but a higher win ratio - has the possibility of being
		// the successor move rather than just naively picking the most simulated node.
		// ****************************************************************************************************************************
		int maxSims = 0, numWins = 0;
		Node winningMove = null;
		double highScore = 0, temp = 0;
		
		for(Node child : firstMoves) {
			maxSims = Math.max(maxSims, child.simulations);
		}
		
		for(Node child : firstMoves) {
			numWins = (player == WHITE ? child.whiteWin : child.blackWin);
			
			temp = (double)numWins/(double)maxSims;
			if(temp > highScore) {
				highScore = temp;
				winningMove = child;
			}
		}
		
		// Play the move on the AI's respective boards and return it
		int move = winningMove.move;
		
		mctsDoMove(move, mctsBoard, player);
		g_board.doMove(move);
		
		int[] response = new int[]{move, (int)highScore*100, winningMove.simulations, simulations};
		simulations = 0;
		firstMoves = null;
		
		return response;
	}
	
	// Dives down the tree to a terminating node, evaluates it as a win or a loss, and trickles back up.
	private void simulate(Node maxNode, int[] gboard, int player) {
		mctsDoMove(maxNode.move, gboard, player);
		
		generateFirstMap(maxNode, gboard, 3 - player);
		
		if(maxNode.getChildren() != null && !maxNode.getChildren().isEmpty()) {
			ArrayList<Node> children = maxNode.getChildren();
			simulate(children.get(rand.nextInt(children.size())), gboard, 3 - player);
		} else {
			maxNode.trickleWinUp(player);
		}
	}
	
	// Calculate the UCT value for a node.  We only call this function if simulations > 0,
	// but we still need to check if the node's personal simulation count is 0
	private void calculatePickMeScore(Node n) {
		// Force a simulation count of 1 if this node has never been simulated
		int numSims = (n.simulations == 0 ? 1 : n.simulations);
		int winCount = (player == WHITE ? n.whiteWin : n.blackWin);
		n.value = (float)(winCount/numSims + 1.4*Math.sqrt(Math.log(simulations)/numSims));
	}
	
	// Generates all legal moves for the given board position and attaches them as children to the given node
	private void generateFirstMap(Node parent, int[] board, int player){
		int qCount = 0, i = -1;
		try{
			int[][] queenPos = new int[4][2];
			for(i = 0; i < board.length; i++){
				if(board[i] == player){
					queenPos[qCount][0] = i%10; // X
					queenPos[qCount][1] = i/10; // Y
					genQueenMovesMapBRETT(generateFirstMapList[qCount++], board, i);
					if(qCount > 3){break;}
				}
			}
			int count = 0;
			for(int[] movemap : generateFirstMapList){
				board[queenPos[count][1]*GRID+queenPos[count][0]] = 0;
				for(int x = 0; x <= 9; x++){
					for(int y = 0; y <= 9; y++){
						if(movemap[y*GRID+x] != 0){
							genShotMap(shotMapGlob, board, x, y);
							multiply_shotmap(parent,movemap[y*GRID+x],shotMapGlob);
						}
					}
				}
				board[queenPos[count][1]*GRID+queenPos[count][0]] = player;
				count++;
			}
		} catch (Exception e){
			System.out.println("q "+qCount+" i"+i);
			//System.out.println(printMap(board));
		}
	}

	// Creates a 10x10 map with the encoded queen+move values.
	private void genQueenMovesMapBRETT(int[] list, int[] board, int queen) {
		for(int i = 0; i < list.length; i++){
			list[i] = 0;
		}

		int x = queen%10;
		int y = queen/10;
		int xOff, yOff;
		for(int[] offset : MATRIX){
			for(yOff = y+offset[0], xOff = x+offset[1]; (yOff > -1 && yOff < 10) && (xOff > -1 && xOff < 10); yOff += offset[0], xOff += offset[1]){
				if(board[yOff*GRID+xOff] == 0){
					list[yOff*GRID+xOff] = (((yOff&0xf) << 12) | ((xOff&0xf) << 8) | ((y&0xf) << 4) | x&0xf);
				} else {
					break;
				}
			}
		}
	}

	// Create a 10x10 shot map with the encoded arrow values.
	private void genShotMap(int[] shotMapGlob, int[] board, int xShot, int yShot){
		int i, xOff, yOff;
		for(i = 0; i < shotMapGlob.length; i++){
			shotMapGlob[i] = -1;
		}

		shotMapGlob[yShot*GRID+xShot] = -99999;
		for(int[] offset : MATRIX){
			for(yOff = yShot+offset[0], xOff = xShot+offset[1]; (yOff > -1 && yOff < 10) && (xOff > -1 && xOff < 10); yOff += offset[0], xOff += offset[1]){
				if(board[yOff*GRID+xOff] == 0){
					shotMapGlob[yOff*GRID+xOff] = (((yOff&0xf) << 4) | xOff&0xf); // Rather than calling encode, doing it inline
				} else {
					break;
				}
			}
		}
	}

	// Multiple the 10x10 maps together to get an array of ints for the move and shoots.
	private void multiply_shotmap(Node parent, int valueMove, int[] shotmap){
		//Account for shooting back at the starting position
		for(int x = 0; x <= 9; x++){
			for(int y = 0; y <= 9; y++){
				if(shotmap[y*GRID+x] >= 0){ // need 0 because orign point is 0*65536 = 0
					int move = (((shotmap[y*GRID+x]&0xffff) << 16) | valueMove);
					parent.addChild(new Node(move,parent));
					count++;
				}
			}
		}
	}

	// Create a 1 dimensional int-array representation of the gameboard
	public static int[] createBoard(Gamepiece[][] gBoard){
		int[] ret = new int[100];
		for(int x = 0; x < GRID; x++){
			for(int y = 0; y < GRID; y++){
				ret[y*GRID+x] = gBoard[y][x].val();
			}
		}

		return ret;
	}
	
	// Play a move on the int[] board
	public static void mctsDoMove(int move, int[] board, int curPlayer){
        int xq = move & 0xf;
        int yq = (move & 0xf0) >> 4;
        int xm = (move & 0xf00) >> 8;
        int ym = (move & 0xf000) >> 12;
        int xs = (move & 0xf0000) >> 16;
        int ys = (move & 0xf00000) >> 20;
        
        board[yq*GRID+xq] = BLANK;
        board[ym*GRID+xm] = curPlayer;
        board[ys*GRID+xs] = ARROW;
    }
	
	// The Node class storing the evaluation, move made and successor moves from the node's position
	private class Node {
		private Node parent;
		private ArrayList<Node> children;
		private float value;
		private int whiteWin;
		private int blackWin;
		private int move;
		private int simulations;
		
		public Node() {
			parent = null;
			children = null;
			value = 0;
			move = 0;
			simulations = 0;
			whiteWin = 0;
			blackWin = 0;
		}
		
		public Node(int move) {
			parent = null;
			children = null;
			value = 0;
			simulations = 0;
			whiteWin = 0;
			blackWin = 0;
			this.move = move;
		}
		
		public Node(int move, Node parent) {
			this.parent = parent;
			children = null;
			value = 0;
			simulations = 0;
			whiteWin = 0;
			blackWin = 0;
			this.move = move;
		}
		
		public void addChild(Node n) {
			if(this.children == null) {
				this.children = new ArrayList<Node>();
			}
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
		
		public float getScore() {
			return value;
		}
		
		public int getMove() {
			return move;
		}
		
		public void trickleWinUp(int player) {
			if(player == WHITE) {
				this.whiteWin++;
			} else {
				this.blackWin++;
			}
			
			if(this.getParent() != null) {
				this.getParent().trickleWinUp(player);
			}
		}
	}
}
