
public class Amazons {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Gameboard B = new Gameboard();
		
		//System.out.print(B);
		
		int move = 0;
		int[] m = new int[6];
		m[0] = 3; m[1] = 0; m[2] = 3; m[3] = 6; m[4] = 3; m[5] = 7;
		
		for (int i = 0; i < 6; i++) {
			move |= m[i] << (i*4); 
		}
		
//		//B.doMove("d1", "d2", "d8");
		B.doMove(move);
		System.out.print(B.emptySquaresCount());
		B.undoMove();
//		B.doMove("a7", "c9", "c8");
//		B.doMove("j4", "e4", "e8");
//	    B.doMove("j7", "h9", "g8");
//		B.doMove("g1", "f2", "f8");
//		B.doMove("h9", "g9", "h8");
//		B.doMove("e4", "i8", "h9");
//		B.doMove("g10", "f10", "h10");
//		B.doMove("a4", "a8", "c10");
//		B.doMove("c9", "b8", "b9");
//		B.doMove("a8", "b7", "c7");
		
//		System.out.print(B.printQueenMovesCount());
//		System.out.print(B.printKingMovesCount());
		System.out.print(B.emptySquaresCount());
		//System.out.print(B.printAlphaScores());
		//System.out.print(B.printBoardEval());
//		System.out.print(B);
	}

}
