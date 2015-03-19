
public class Amazons {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Gameboard B = new Gameboard();
		
		//System.out.print(B);
		
		int move = 0;
		move = move | 0x7;
		move = move << 4;
		move = move | 0x3;
		move = move << 4;
		move = move | 0x6;
		move = move << 4;
		move = move | 0x3;
		move = move << 4;
		move = move | 0x0;
		move = move << 4;
		move = move | 0x3;
		
		
		//B.doMove("d1", "d7", "d8");
		B.doMove(move);
		//B.undoMove(move);
		B.doMove("a7", "c9", "c8");
		B.doMove("j4", "e4", "e8");
	    B.doMove("j7", "h9", "g8");
		B.doMove("g1", "f2", "f8");
		B.doMove("h9", "g9", "h8");
		B.doMove("e4", "i8", "h9");
		B.doMove("g10", "f10", "h10");
		B.doMove("a4", "a8", "c10");
		//B.doMove("c9", "d9", "c9");
		B.doMove("c9", "b8", "b9");
		B.doMove("a8", "b7", "c7");
		
		//System.out.print(B.printQueenMovesCount());
		//System.out.print(B.printKingMovesCount());
		//System.out.print(B.emptySquaresCount());
		//System.out.print(B.printAlphaScores());
		System.out.print(B.printBoardEval());
		System.out.print(B);
	}

}
