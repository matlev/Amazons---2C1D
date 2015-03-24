import java.util.Scanner;

public class Amazons {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Gameboard B = new Gameboard();
		Gameboard AIBoard = new Gameboard();
		
		double score = 0;
		String queen = "", to = "", arrow = "";
		
		Scanner scan = new Scanner(System.in);
		AI ai = new AI(AIBoard, 2);
		
		do {
		
			System.out.print(B);
			
			do {
				System.out.print("\nChoose a white queen: ");
				queen = scan.nextLine();
				
				if(queen.equalsIgnoreCase("stop")) {
					break;
				}
				
				System.out.print("Choose a square to move to: ");
				to = scan.nextLine();
				System.out.print("Choose where to shoot: ");
				arrow = scan.nextLine();
			} while(!B.doMove(queen, to, arrow));
				
			if(queen.equalsIgnoreCase("stop")) {
				break;
			}
			
			ai.playMove(queen, to, arrow);
			
			System.out.print(B);
			
			Long start = System.currentTimeMillis();
			String[] ai_move = ai.search();
			Long finish = System.currentTimeMillis();
			String[] qMoves = ai_move[0].split("-");
			
			B.doMove(qMoves[0], qMoves[1], ai_move[1]);
			ai.playMove(qMoves[0], qMoves[1], ai_move[1]);
			
			score = (double)ai.getScore();
			
			//System.out.println("Computer plays " + ai_move[0] + " and fires an arrow to " + ai_move[1]);
			System.out.println("Computer took " + ((finish - start)/1000) + " seconds to reply.");
			System.out.println("Leaf nodes evaluated: " + ai.leaves);
			System.out.println("Position score: " + score);
	
			
	//		System.out.print(B.printQueenMovesCount());
	//		System.out.print(B.printKingMovesCount());
	//		System.out.print(B.emptySquaresCount());
	//		System.out.print(B.printAlphaScores());
	//		System.out.print(B.printAlphaScores());
	//		System.out.print(B.printBoardEval());
			
		} while(Double.isFinite(score));
		
		scan.close();
		System.out.print(B);
		B.printMoveHistory();
	}

}
