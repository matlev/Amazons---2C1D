import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.*;
import java.net.URL;
import javax.imageio.ImageIO;

public class GameboardGUI {

    private final JPanel gui = new JPanel(new BorderLayout(3, 3));
    private JButton[][] chessBoardSquares = new JButton[10][10];
    private Image[][] chessPieceImages = new Image[2][6];
    private JPanel chessBoard;
    private static final String COLS = "ABCDEFGHIJ";
    public static final int QUEEN = 1;
    public static final int BLACK = 0, WHITE = 1;

    GameboardGUI() {
        initializeGui();
    }

    public final void initializeGui() {
        // create the images for the chess pieces
        createImages();

        // set up the main GUI
        gui.setBorder(new EmptyBorder(5, 5, 5, 5));
      //  gui.add(BorderLayout.PAGE_START);



        gui.add(new JLabel("?"), BorderLayout.LINE_START);
        
        chessBoard = new JPanel(new GridLayout(0, 11)) {
        	

            /**
             * Override the preferred size to return the largest it can, in
             * a square shape.  Must (must, must) be added to a GridBagLayout
             * as the only component (it uses the parent as a guide to size)
             * with no GridBagConstaint (so it is centered).
             */
            @Override
            public final Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                Dimension prefSize = null;
                Component c = getParent();
                if (c == null) {
                    prefSize = new Dimension(
                            (int)d.getWidth(),(int)d.getHeight());
                } else if (c!=null &&
                        c.getWidth()>d.getWidth() &&
                        c.getHeight()>d.getHeight()) {
                    prefSize = c.getSize();
                } else {
                    prefSize = d;
                }
                int w = (int) prefSize.getWidth();
                int h = (int) prefSize.getHeight() - 160;
                // the smaller of the two sizes
                int s = (w>h ? h : w);
                return new Dimension(s,s);
            }
        };
        chessBoard.setBorder(new CompoundBorder(
                new EmptyBorder(5,5,5,5),
                new LineBorder(Color.BLACK)
                ));
        // Set the BG to be ochre
        Color ochre = new Color(204,119,34);
        chessBoard.setBackground(ochre);
        JPanel boardConstrain = new JPanel(new GridBagLayout());
        boardConstrain.setBackground(ochre);
        boardConstrain.add(chessBoard);
        gui.add(boardConstrain);

        // create the chess board squares
        Insets buttonMargin = new Insets(0, 0, 0, 0);
        for (int ii = 0; ii < chessBoardSquares.length; ii++) {
            for (int jj = 0; jj < chessBoardSquares[ii].length; jj++) {
                JButton b = new JButton();
                b.setMargin(buttonMargin);
                // our chess pieces are 64x64 px in size, so we'll
                // 'fill this in' using a transparent icon..
                ImageIcon icon = new ImageIcon(
                        new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB));
                b.setIcon(icon);
                if ((jj % 2 == 1 && ii % 2 == 1)
                        //) {
                        || (jj % 2 == 0 && ii % 2 == 0)) {
                    b.setBackground(Color.WHITE);
                } else {
                    b.setBackground(Color.BLACK);
                }
                chessBoardSquares[jj][ii] = b;
            }
        }

        /*
         * fill the chess board
         */
        chessBoard.add(new JLabel(""));
        // fill the top row
        for (int ii = 0; ii < 10; ii++) {
            chessBoard.add(
                    new JLabel(COLS.substring(ii, ii + 1),
                    SwingConstants.CENTER));
        }
        // fill the black non-pawn piece row
        for (int ii = 0; ii < 10; ii++) {
            for (int jj = 0; jj < 10; jj++) {
                switch (jj) {
                    case 0:
                        chessBoard.add(new JLabel("" + (11-(ii + 1)),
                                SwingConstants.CENTER));
                    default:
                        chessBoard.add(chessBoardSquares[jj][ii]);
                }
            }
        }
        
        // Fill squares with the black queen pieces
        chessBoardSquares[0][3].setIcon(new ImageIcon(chessPieceImages[BLACK][QUEEN]));
        chessBoardSquares[3][0].setIcon(new ImageIcon(chessPieceImages[BLACK][QUEEN]));
        chessBoardSquares[6][0].setIcon(new ImageIcon(chessPieceImages[BLACK][QUEEN]));
        chessBoardSquares[9][3].setIcon(new ImageIcon(chessPieceImages[BLACK][QUEEN]));
        
        // Fill squares with the white queen pieces
        chessBoardSquares[0][6].setIcon(new ImageIcon(chessPieceImages[WHITE][QUEEN]));
        chessBoardSquares[3][9].setIcon(new ImageIcon(chessPieceImages[WHITE][QUEEN]));
        chessBoardSquares[6][9].setIcon(new ImageIcon(chessPieceImages[WHITE][QUEEN]));
        chessBoardSquares[9][6].setIcon(new ImageIcon(chessPieceImages[WHITE][QUEEN]));
    }

    public final JComponent getGui() {
        return gui;
    }

    private final void createImages() {
        try {
            URL url = new URL("http://i.stack.imgur.com/memI0.png");
            BufferedImage bi = ImageIO.read(url);
            for (int ii = 0; ii < 2; ii++) {
                for (int jj = 0; jj < 6; jj++) {
                    chessPieceImages[ii][jj] = bi.getSubimage(
                            jj * 64, ii * 64, 64, 64);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                GameboardGUI cg = new GameboardGUI();

                JFrame f = new JFrame("AmazonsChamp");
                f.add(cg.getGui());
                
                f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                f.setLocationByPlatform(true);

                // ensures the frame is the minimum size it needs to be
                // in order display the components within it
                f.pack();
                // ensures the minimum size is enforced.
                f.setMinimumSize(f.getSize());
                f.setVisible(true);
            }
        };
        SwingUtilities.invokeLater(r);
    }
}