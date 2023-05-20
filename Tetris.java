import javax.swing.*;
// import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.event.*;
import java.time.*;
import java.io.File;
import java.io.IOException;

// public class Tetris extends JFrame {
public class Tetris implements KeyListener {
    Board board;
    int i = 0;
    JFrame frame;
    private Thread gameLoop;

    // 0: score
    // 1: lines
    // 2: level
    // 3: game over
    JLabel[] gameLabels;
    String[] labelStarters = {
        "Score:",
        "Lines:",
        "Level:"
    };

    private static Font gameFont;

    boolean leftDown = false, rightDown = false, upDown = false, downDown = false;
    boolean lastLeft = false, lastRight = false;
    boolean cDown = false;

    boolean spaceDown = false;

    Instant lastLeftPress;
    Instant lastRightPress;
    double moveDelay = moveSecondDelay;

    static final int gridCellSize = 30;

    static final double moveSecondDelay = 0.075;
    static final double initialMoveSecondDelay = 0.12;

    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 700;

    private static int boardX = (WINDOW_WIDTH - 10 * gridCellSize) / 2;
    private static int boardY = (WINDOW_HEIGHT - 20 * gridCellSize) / 2;

    private DrawPane pane;

    private SoundPlayer player;

    public Tetris() {
        player = new SoundPlayer("./tetris_music_wav.wav");
        player.setLoopStartEnd(6000, 3408931);

        try {
            gameFont = Font.createFont(Font.TRUETYPE_FONT, new File("./PublicPixel-z84yD.ttf"));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(gameFont);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FontFormatException e) {
            e.printStackTrace();
        }

        lastLeftPress = Instant.now();
        lastRightPress = Instant.now();

        frame = new JFrame("Tetris");
        frame.setBackground(Color.BLACK);
        frame.getContentPane().setBackground(Color.BLACK);

        board = new Board(boardX, boardY);

        gameLabels = new JLabel[4];

        pane = new DrawPane();
        pane.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        pane.setLayout(null);

        
        for (int i = 0; i < gameLabels.length; i++) {
            JLabel newLabel = new JLabel();
            newLabel.setForeground(Color.WHITE);
            newLabel.setFont(gameFont.deriveFont(16f));
            newLabel.setAlignmentX(0f);
            newLabel.setBounds(30, 0, board.posX, board.posY + 350 + 150 * i);
            gameLabels[i] = newLabel;
            pane.add(newLabel);
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setResizable(false);
        
        frame.add(pane);

        frame.pack();
        frame.setVisible(true);

        frame.addKeyListener(this);

        frame.setLocationRelativeTo(null);

        gameLoop = new Thread(() -> {
            while (true) {
                board.update();

                if (board.isGameOver() && gameLabels[3].getText().length() == 0) {
                    gameLabels[3].setText("<html>GAME OVER<p><br>[ENTER]</p><p><br>for new game</p></html>");
                } else if (!board.isGameOver() && gameLabels[3].getText().length() > 0) {
                    gameLabels[3].setText("");
                }

                if (board.isGameOver() && player.isPlaying()) {
                    player.pause();
                }

                int[] data = {board.getScore(), board.getLinesCleared(), board.getLevel()};
                for (int i = 0; i < data.length; i++) {
                    gameLabels[i].setText(labelStarters[i] + " " + data[i]);
                }

                this.pane.repaint();

                // keyboard input
                if (leftDown && !rightDown) {
                    if (!lastLeft) {
                        board.moveLeft();
                        lastLeft = true;
                        moveDelay = initialMoveSecondDelay;
                        lastLeftPress = Instant.now();
                    } else if ((Duration.between(lastLeftPress, Instant.now()).toMillis() / 1000.0) >= moveDelay) {
                        lastLeftPress = Instant.now();
                        board.moveLeft();
                        moveDelay = moveSecondDelay;
                    }
                    
                } else if (rightDown && !leftDown) {
                    if (!lastRight) {
                        board.moveRight();
                        lastRight = true;
                        moveDelay = initialMoveSecondDelay;
                        lastRightPress = Instant.now();
                    } else if ((Duration.between(lastRightPress, Instant.now()).toMillis() / 1000.0) >= moveDelay) {
                        lastRightPress = Instant.now();
                        board.moveRight();
                        moveDelay = moveSecondDelay;
                    }
                }

                try {
                    Thread.sleep(1000/60, (int)(((1000.0/60) % 1) * 1000000));
                } catch (InterruptedException e) {}
            }
        });

        gameLoop.start();
        player.startLoop(true);
    }

    // handle keyboard input
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            // left arrow
            case 37:
                leftDown = true;
                break;

            // right arrow
            case 39:
                rightDown = true;
                break;

            // up arrow
            case 38:
                if (!upDown) {
                    board.rotateClockwise();
                    upDown = true;
                }
                break;

            // down arrow
            case 40:
                board.fastFall = true;
                break;

            // Z key
            case 90:
                if (!downDown) {
                    board.rotateCounterclockwise();
                    downDown = true;
                }
                break;

            // space key
            case 32:
                if (!spaceDown) {
                    board.instantFall();
                    spaceDown = true;
                }
                break;

            // c key
            case 67:
                if (!cDown) {
                    board.holdPiece();
                    cDown = true;
                }
                break;

            // enter key
            case 10:
                if (board.isGameOver()) {
                    board = new Board(boardX, boardY);
                    player.reset();
                    player.resume();
                }
                break;
        }
    }

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            // left arrow
            case 37:
                leftDown = false;
                lastLeft = false;
                break;

            // right arrow
            case 39:
                rightDown = false;
                lastRight = false;
                break;

            // up arrow
            case 38:
                upDown = false;
                break;

            // down arrow
            case 40:
                board.fastFall = false;
                break;

            // Z key
            case 90:
                downDown = false;
                break;

            // space key
            case 32:
                spaceDown = false;
                break;

            // c key
            case 67:
                cDown = false;
                break;
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    class DrawPane extends JPanel {
        public DrawPane() {
            this.setIgnoreRepaint(true);
        }

        @Override
        public void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);

            g.setColor(new Color(0, 0, 0, 255));
            g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
            
            // draw active and ghost piece
            Piece currentPiece = board.getPiece();
            if (currentPiece != null) {
                Color pieceColor = currentPiece.color;
                Color ghostColor = pieceColor.darker().darker();
                for (int row = 0; row < 4; row++) {
                    for (int col = 0; col < 4; col++) {
                        if (currentPiece.getShapeVal(row, col) > 0) {
                            // draw ghost piece
                            g.setColor(ghostColor);
                            g.fillRect(board.posX + (board.getPieceX() + col) * 30, board.posY + (board.getGhostY() + row) * 30, 30, 30);
                            // draw active piece
                            g.setColor(pieceColor);
                            g.fillRect(board.posX + (board.getPieceX() + col) * 30, board.posY + (board.getPieceY() + row) * 30, 30, 30);
                        }
                    }
                }
            }
            
            // draw grid
            g.setColor(new Color(255, 255, 255, 255));
            for (int row = 0; row < board.numRows(); row++) {
                for (int col = 0; col < board.numCols(); col++) {
                    if (board.squareExists(row, col)) {
                        g.setColor(board.getSquare(row, col));
                        g.fillRect(board.posX + col * 30, board.posY + row * 30, gridCellSize, gridCellSize);
                        g.setColor(new Color(255, 255, 255, 255));
                    }
                    g.drawRect(board.posX + col * 30, board.posY + row * 30, 30, 30);
                }
            }

            // draw held piece
            g.setColor(Color.WHITE);
            g.drawRect(board.posX + board.numCols() * 30 + 90, board.posY + board.numRows() * 30 - 140, 140, 140);
            
            if (board.isHeldPiece()) {
                Piece heldPiece = board.getHeldPiece();
                g.setColor(heldPiece.color);
                for (int row = 0; row < 4; row++) {
                    for (int col = 0; col < 4; col++) {
                        if (heldPiece.getShapeVal(row, col) > 0) {
                            g.fillRect(board.posX + board.numCols() * 30 + 100 + col * 30, board.posY - 120 + board.numRows() * 30 + row * 30, 30, 30);
                        }
                    }
                }
            }

            // draw next pieces
            g.setColor(Color.WHITE);
            g.drawRect(board.posX + board.numCols() * 30 + 90, board.posY, 140, 420);
            Piece[] nextPieces = board.getNextPieces();
            
            for (int piece = 0; piece < nextPieces.length; piece++) {
                Piece nextPiece = nextPieces[piece];
                g.setColor(nextPiece.color);
                for (int row = 0; row < 4; row++) {
                    for (int col = 0; col < 4; col++) {
                        if (nextPiece.getShapeVal(row, col) > 0) {
                            g.fillRect(board.posX + board.numCols() * 30 + 100 + col * 30, board.posY + 20 + row * 30 + 150 * piece, 30, 30);
                        }
                    }
                }
            }
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Tetris::new);
    }
}
