
public abstract class Gamepiece {

		protected byte x, y;
		
		// Stores the x and y coordinates of a piece
		public Gamepiece(byte x, byte y) {
			this.x = x;
			this.y = y;
		}
		
		// Converts the letter coordinate into a 0-index number and stores the x and y values
		public Gamepiece(char x, byte y) {
			this.x = (byte)(x > 96 ? x - 97 : x - 65); // (a = 0 (65 or 97), b = 1 (66 or 98), ...
			this.y = y;
		}
		
		// Converts a string into 0-index values and stores them into x and y
		public Gamepiece(String coord) {
			int in_x = coord.charAt(0);
			x = (byte)(in_x > 96 ? in_x - 97 : in_x - 65);
			y = (byte)(Integer.parseInt(coord.substring(1)) - 1);
		}
		
		// Returns the 1-index value of the piece's position in algebraic format (ex. a4; g3; b7; etc.)
		public String stringPosition() {
			char x_pos = (char)(x + 65);
			
			return x_pos + "" + (y + 1);
		}
		
		public int[] position() {
			int[] pos = new int[2];
			pos[0] = x;
			pos[1] = y;
			return pos;
		}
		
		public abstract byte val();
}
