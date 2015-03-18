
public class Amazons {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Gameboard B = new Gameboard();
		
		//System.out.print(B);
		
		//String newLoc = B.movePiece("d1", "d7");
		//String ArrowLoc = B.shootArrow(newLoc, "F9");
		B.moveAndShoot("d1", "d7", "d8");
		B.moveAndShoot("a7", "c9", "c8");
		B.moveAndShoot("j4", "e4", "e8");
	    B.moveAndShoot("j7", "h9", "g8");
		B.moveAndShoot("g1", "f2", "f8");
		B.moveAndShoot("h9", "g9", "h8");
		B.moveAndShoot("e4", "i8", "h9");
		B.moveAndShoot("g10", "f10", "h10");
		B.moveAndShoot("a4", "a8", "c10");
		B.moveAndShoot("c9", "d9", "c9");
		
		//System.out.print(B.printQueenMovesCount());
		//System.out.print(B.printKingMovesCount());
		//System.out.print(B.emptySquaresCount());
		//System.out.print(B.printAlphaScores());
		System.out.print(B.printBoardEval());
		System.out.print(B);
	}

}
