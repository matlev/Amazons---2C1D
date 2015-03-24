
public class Amazons {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Gameboard B = new Gameboard();
		Gameboard AIBoard = new Gameboard();
		
		AI ai = new AI(AIBoard, 2);
		
		//System.out.print(B);
		
		int move = 0;
		int[] m = new int[6];
		m[0] = 3; m[1] = 0; m[2] = 3; m[3] = 7; m[4] = 1; m[5] = 5;
		
		for (int i = 0; i < 6; i++) {
			move |= m[i] << (i*4); 
		}
		
//		B.doMove("d1", "d2", "d8");
		B.doMove(move);
		ai.playMove("d1", "d8", "b6");
		
		Long start = System.currentTimeMillis();
		String[] ai_move = ai.search();
		Long finish = System.currentTimeMillis();
		
		System.out.println("Computer plays " + ai_move[0] + " and fires an arrow to " + ai_move[1]);
		System.out.println("Computer took " + ((finish - start)/1000) + " seconds to reply.");
		System.out.println("Leaf nodes evaluated: " + ai.leaves);
		B.doMove("a7", "e7", "e1");
//		B.doMove("a7", "c9", "c8");
//		ai.playMove("a7", "c9", "c8");
//		B.doMove("j4", "e4", "e8");
//		ai.playMove("j4", "e4", "e8");
//	    B.doMove("j7", "h9", "g8");
//	    ai.playMove("j7", "h9", "g8");
//		B.doMove("g1", "f2", "f8");
//		ai.playMove("g1", "f2", "f8");
//		B.doMove("h9", "g9", "h8");
//		ai.playMove("h9", "g9", "h8");
//		B.doMove("e4", "i8", "h9");
//		ai.playMove("e4", "i8", "h9");
//		B.doMove("g10", "f10", "h10");
//		ai.playMove("g10", "f10", "h10");
//		B.doMove("a4", "a8", "c10");
//		ai.playMove("a4", "a8", "c10");
//		B.doMove("c9", "b8", "b9");
//		ai.playMove("c9", "b8", "b9");
//		B.doMove("a8", "b7", "c7");
//		ai.playMove("a8", "b7", "c7");
//		
//		String[] ai_move = ai.search();
//		
//		System.out.println("Computer plays " + ai_move[0] + " and fires an arrow to " + ai_move[1]);
		
//		System.out.print(B.printQueenMovesCount());
//		System.out.print(B.printKingMovesCount());
//		System.out.print(B.emptySquaresCount());
		//System.out.print(B.printAlphaScores());
		System.out.print(B.printBoardEval());
		System.out.print(B);
	}

}
