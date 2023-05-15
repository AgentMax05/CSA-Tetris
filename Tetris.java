import javax.swing.*;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.time.*;

// public class Tetris extends JFrame {
public class Tetris implements KeyListener {
    Board board;
    int i = 0;
    JFrame frame;
    private Thread gameLoop;

    boolean leftDown = false, rightDown = false, upDown = false, downDown = false;
    boolean lastLeft = false, lastRight = false;
    Instant lastLeftPress;
    Instant lastRightPress;
    double moveDelay = moveSecondDelay;

    static final int gridCellSize = 30;

    static final double moveSecondDelay = 0.075;
    static final double initialMoveSecondDelay = 0.12;

    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 700;

    private DrawPane pane;

    public Tetris() {
        lastLeftPress = Instant.now();
        lastRightPress = Instant.now();

        frame = new JFrame("Tetris");

        board = new Board((WINDOW_WIDTH - 10 * gridCellSize) / 2, (WINDOW_HEIGHT - 20 * gridCellSize) / 2);

        board.getNewPiece();

        pane = new DrawPane();
        pane.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setResizable(false);

        frame.add(pane);
        frame.pack();
        frame.setVisible(true);

        frame.addKeyListener(this);

        frame.setLocationRelativeTo(null);

        // setup keybindings
        // pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), )

        gameLoop = new Thread(() -> {
            while (true) {
                board.update();

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
                if (!downDown) {
                    board.rotateCounterclockwise();
                    downDown = true;
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
                downDown = false;
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

            
            // draw active piece
            Piece currentPiece = board.getPiece();
            if (currentPiece != null) {
                g.setColor(currentPiece.color);
                for (int row = 0; row < 4; row++) {
                    for (int col = 0; col < 4; col++) {
                        if (currentPiece.shapes[currentPiece.currentShape][row * 4 + col] == 1) {
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
            
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
        }
    }

    public static void main(String[] args) {
        // Tetris game = new Tetris();
        SwingUtilities.invokeLater(Tetris::new);
    }
}
