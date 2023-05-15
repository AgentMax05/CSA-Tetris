import java.time.*;
import java.awt.Color;

public class Board {
    private Color[][] board;

    private int score;
    private Piece currentPiece;
    private int pieceX, pieceY;
    public final int posX, posY;

    private Instant lastFall;

    private int level = 1;

    // frames per gridcell down
    private int gravityFrames = 43;
    
    private static final double secondsPerFrame = 1.0 / 60;

    public Board(int x, int y) {
        board = new Color[20][10];

        currentPiece = null;

        lastFall = Instant.now();

        posX = x;
        posY = y;
    }

    public void update() {
        if (Duration.between(lastFall, Instant.now()).toSeconds() >= (gravityFrames * secondsPerFrame)) {
            fallPiece();
            lastFall = Instant.now();
        }
    }

    public void moveLeft() {
        if (!pieceCollidingLeft()) {
            pieceX--;
        }
    }

    public void moveRight() {
        if (!pieceCollidingRight()) {
            pieceX++;
        }
    }

    public void rotateClockwise() {
        currentPiece.rotateClockwise();
        resetClip();
    }

    public void rotateCounterclockwise() {
        currentPiece.rotateCounterClockwise();
        resetClip();
    }

    // moves the piece back into the board
    // if it's clipping the wall
    private void resetClip() {
        while (pieceClippingLeft()) {
            pieceX++;
        }

        while (pieceClippingRight()) {
            pieceX--;
        }
    }

    // checks if a rotated piece would clip the wall on the left
    private boolean pieceClippingLeft() {
        if (pieceX >= 0) {
            return false;
        }
        
        int edgeCol = -1 * pieceX;

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < edgeCol; col++) {
                if (currentPiece.getShapeVal(row, col) > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    // checks if a rotated piece would clip the wall on the right
    private boolean pieceClippingRight() {
        if (pieceX + 4 <= numCols() - 1) {
            return false;
        } 

        int edgeCol = numCols() - 1 - pieceX;
        
        for (int row = 0; row < 4; row++) {
            for (int col = 3; col > edgeCol; col--) {
                if (currentPiece.getShapeVal(row, col) > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    // checks if currentPiece is next to the left wall
    private boolean pieceCollidingLeft() {
        if (pieceX > 0) {
            return false;
        }

        int col = -1 * pieceX;
        for (int row = 0; row < 4; row++) {
            if (currentPiece.getShapeVal(row, col) > 0) {
                return true;
            }
        }
        
        return false;
    }

    // checks if currentPiece is next to the right wall
    private boolean pieceCollidingRight() {
        if (pieceX + 5 <= numCols()) {
            return false;
        }
        
        int col = numCols() - 1 - pieceX;
        for (int row = 0; row < 4; row++) {
            if (currentPiece.getShapeVal(row, col) > 0) {
                return true;
            }
        }

        return false;
    }

    // checks if currentPiece is next to the bottom of the grid
    private boolean pieceCollidingBottom() {
        if (pieceY + 3 < numRows() - 1) {
            return false;
        }

        int edgeRow = numRows() - 1 - pieceY;

        for (int col = 0; col < 4; col++) {
            if (currentPiece.getShapeVal(edgeRow, col) > 0) {
                return true;
            }
        }

        return false;
    }

    private void fallPiece() {
        if (!pieceCollidingBottom()) {
            pieceY++;
        } else {
            placePiece();
            getNewPiece();
        }
    }   

    // places currentPiece in the board
    private void placePiece() {
        for (int col = pieceX; col < pieceX + 4; col++) {
            for (int row = pieceY; row < pieceY + 4; row++) {
                if (currentPiece.getShapeVal(row - pieceY, col - pieceX) > 0) {
                    board[row][col] = currentPiece.color;
                }
            }
        }
    }

    // progresses fall speed based on level
    public void nextLevel() {
        if (level < 8) {
            gravityFrames -= 5;
        } else {
            switch (level) {
                case 8:
                    gravityFrames -= 2;
                    break;
                case 9:
                    gravityFrames -= 1;
                    break;
                case 12:
                case 15:
                case 18:
                case 28:
                    gravityFrames -= 1;
                    break;
            }
        }
        level++;
    }

    public Color getSquare(int row, int col) {
        return board[row][col];
    }

    public boolean squareExists(int row, int col) {
        return getSquare(row, col) != null;
    }

    public int numRows() {
        return board.length;
    }

    public int numCols() {
        return board[0].length;
    }

    public Piece getPiece() {
        return currentPiece;
    }

    public int getPieceX() {
        return pieceX;
    }

    public int getPieceY() {
        return pieceY;
    }

    public void getNewPiece() {
        pieceX = 3;
        pieceY = 0;

        int randPiece = (int) (Math.random() * 7);

        switch (randPiece) {
            case 0:
                currentPiece = new IPiece();
                break;
            case 1:
                currentPiece = new JPiece();
                break;
            case 2:
                currentPiece = new LPiece();
                break;
            case 3:
                currentPiece = new OPiece();
                break;
            case 4:
                currentPiece = new SPiece();
                break;
            case 5:
                currentPiece = new TPiece();
                break;
            case 6:
                currentPiece = new ZPiece();
                break;
        }
    }

}
