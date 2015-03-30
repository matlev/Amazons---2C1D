import java.util.Scanner;

public class Amazons {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Gameboard B = new Gameboard();
		Gameboard AIBoard = new Gameboard();
		
		int move = 1;
		double score = 0;
		String queen = "", to = "", arrow = "";
		
		Scanner scan = new Scanner(System.in);
		AI ai_white = new AI(B, 1);
		AI ai_black = new AI(AIBoard, 2);
		
//		int moves = 0;
//		long time = System.currentTimeMillis();
//		for(int i = 0; i < 2176; i++) {
//			ai_white.mcts();
//		}
//		time = System.currentTimeMillis() - time;
//		System.out.print(moves + " moves found in " + time + " milliseconds.");
		
//		int[][] gamePlayed = new int[][]{
//			{6710019, 3225193},
//			{9708550, 7500691},
//			{4679011, 4268915},
//			{4395056, 4818326},
//			{8533817, 1119798},
//			{5656592, 7698053},
//			{1204837, 5445398},
//			{2500663, 3683107},
//			{6644578, 8750966},
//			{5263696, 4350560},
//			{5337425, 4617058},
//			{8615011, 8675955},
//			{5399412, 4749447},
//			{2627624, 4465203},
//			{1574680, 4527383},
//			{1443335, 2302242},
//			{3290404, 140309},
//			{2229254, 333092},
//			{2364420, 7894904},
//			{472885, 3548437},
//			{7430513, 5600098},
//			{5530723, 3478821},
//			{6579044, 5867127},
//			{3610423, 197653},
//			{1377815, 9803398},
//			{6513251, 5740438},
//			{2561798, 267012},
//			{591895, 7566451},
//			{3741960, 8225},
//			{8413281, 69664},
//			{6447458, 1060880},
//			{6385761, 2113584},
//			{9535856, 2175040},
//			{9671297, 3162160},
//			{9601426, 10057623},
//			{8491137, 9013111},
//			{6320224, 10000263},
//			{1320212, 9934487},
//			{1640473, 8947606},
//			{530184, 9864839},
//			{1508887, 7956614},
//			{3355685, 8881768},
//			{3417396, 6908038},
//		};
//		
//		for(int i = 0; i < 13; i++) {
//			for(int j = 0; j < 2; j++) {
//				B.doMove(gamePlayed[i][j]);
//			}
//		}
//		
//		//B.doMove(gamePlayed[23][0]);
//		
//		
//		System.out.print(B);
//		System.out.print(B.printBoardEval());
//		System.out.println();
//		System.out.print(B.printKingMovesCount());
//		System.out.print(B.printQueenMovesCount());
//		System.out.print(B.printQueenDelta());
//		System.out.print(B.printKingDelta());
		
		
		do {
		
			System.out.println(move);
			System.out.print(B);
			
			Long start = System.currentTimeMillis();
			String[] ai_move = ai_white.search();
			Long finish = System.currentTimeMillis();
			
			if(ai_move[0] == null) {
				score = (double)ai_white.getScore();
				break;
			}
			
			String[] qMoves = ai_move[0].split("-");
			
			ai_black.playMove(qMoves[0], qMoves[1], ai_move[1]);
//			B.doMove(qMoves[0], qMoves[1], ai_move[1]);
			
			score = (double)ai_white.getScore();
			
			//System.out.println("Computer plays " + ai_move[0] + " and fires an arrow to " + ai_move[1]);
			System.out.println("Computer took " + ((finish - start)/1000) + " seconds to reply.");
			System.out.println("Leaf nodes evaluated: " + ai_white.leaves + " at depth " + ai_white.max_depth + ".");
			System.out.println("Position score: " + score);
			
			System.out.print(B);
			
//			do {
//				System.out.print("\nChoose a white queen: ");
//				queen = scan.nextLine();
//				
//				if(queen.equalsIgnoreCase("stop")) {
//					break;
//				}
//				
//				System.out.print("Choose a square to move to: ");
//				to = scan.nextLine();
//				System.out.print("Choose where to shoot: ");
//				arrow = scan.nextLine();
//			} while(!B.doMove(queen, to, arrow));
//				
//			if(queen.equalsIgnoreCase("stop")) {
//				break;
//			}
//			
//			ai_white.playMove(queen, to, arrow);
			
			start = System.currentTimeMillis();
			ai_move = ai_black.search();
			finish = System.currentTimeMillis();
			
			if(ai_move[0] == null) {
				score = (double)ai_black.getScore();
				break;
			}
			
			qMoves = ai_move[0].split("-");
			
			ai_white.playMove(qMoves[0], qMoves[1], ai_move[1]);
			
			score = (double)ai_black.getScore();
			
			//System.out.println("Computer plays " + ai_move[0] + " and fires an arrow to " + ai_move[1]);
			System.out.println("Computer took " + ((finish - start)/1000) + " seconds to reply.");
			System.out.println("Leaf nodes evaluated: " + ai_black.leaves + " at depth " + ai_black.max_depth + ".");
			System.out.println("Position score: " + score);
			
			move++;
			
	//		System.out.print(B.printQueenMovesCount());
	//		System.out.print(B.printKingMovesCount());
	//		System.out.print(B.emptySquaresCount());
	//		System.out.print(B.printAlphaScores());
	//		System.out.print(B.printAlphaScores());
	//		System.out.print(B.printBoardEval());
			
		} while(true);
		
		if(score > 0) {
			System.out.println("WHITE WINS!");
		} else {
			System.out.println("BLACK WINS!");
		}
		
		scan.close();
		System.out.print(B);
		System.out.print(B.printMoveHistory());
	}

}
