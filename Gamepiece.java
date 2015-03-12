
public abstract class Gamepiece {

		protected int x, y;
		
		// Stores the x and y coordinates of a piece
		public Gamepiece(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		// Converts the letter coordinate into a 0-index number and stores the x and y values
		public Gamepiece(char x, int y) {
			this.x = (int)(x > 96 ? x - 97 : x - 65); // (a = 0 (65 or 97), b = 1 (66 or 98), ...
			this.y = y;
		}
		
		// Converts a string into 0-index values and stores them into x and y
		public Gamepiece(String coord) {
			int in_x = coord.charAt(0);
			x = (in_x > 96 ? in_x - 97 : in_x - 65);
			y = Integer.parseInt(coord.substring(1)) - 1;
		}
		
		// Returns the 1-index value of the piece's position in algebraic format (ex. a4; g3; b7; etc.)
		public String position() {
			char x_pos = (char)(x + 65);
			
			return x_pos + "" + (y + 1);
		}
		
		public abstract int val();
}
