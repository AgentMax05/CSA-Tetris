import java.time.*;
import java.awt.Color;
import java.util.Random;

public class Board {
    private Color[][] board;

    private int score;
    private Piece currentPiece;
    private int pieceX, pieceY;
    public final int posX, posY;

    private Instant lastFall;

    private int level = 1;

    private Random randGen;

    // frames per gridcell down
    private int gravityFrames = 43;
    
    private final int fastFallGravityFrames = 5;
    public boolean fastFall = false;
    
    private static final double secondsPerFrame = 1.0 / 60;

    public Board(int x, int y) {
        board = new Color[20][10];

        currentPiece = null;

        lastFall = Instant.now();

        posX = x;
        posY = y;

        randGen = new Random();
    }

    public void update() {  
        if (Duration.between(lastFall, Instant.now()).toMillis() / 1000.0 >= ((fastFall ? fastFallGravityFrames : gravityFrames) * secondsPerFrame)) {
            lastFall = Instant.now();
            fallPiece();
        }
    }

    // clears filled rows
    private void clearFilledRows() {
        // for (int row = numRows() - 1; row >= 0; row--) {
        for (int row = 0; row < numRows(); row++) {
            boolean rowFilled = true;
            for (int col = 0; col < numCols(); col++) {
                if (!squareExists(row, col)) {
                    rowFilled = false;
                    break;
                }
            }
            if (rowFilled) {
                for (int r = row; r > 0; r--) {
                    // board[r] = board[r-1];
                    for (int col = 0; col < numCols(); col++) {
                        board[r][col] = board[r-1][col];
                    }
                }
            }
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
        int lastRotationIndex = currentPiece.currentShape;
        currentPiece.rotateClockwise();
        if (!testWallKicks(lastRotationIndex, false)) {
            currentPiece.rotateCounterClockwise();
        }
    }

    public void rotateCounterclockwise() {
        int lastRotationIndex = currentPiece.currentShape;
        currentPiece.rotateCounterClockwise();
        if (!testWallKicks(lastRotationIndex, true)) {
            currentPiece.rotateClockwise();
        }
    }

    // attempts to relocate the piece based on wall kicks
    public boolean testWallKicks(int lastRotation, boolean counterClockwise) {
        int kickIndex = 0;
        int[][] relevantKicks = currentPiece.getRelevantWallKick(lastRotation, counterClockwise);

        int ogPieceX = pieceX;
        int ogPieceY = pieceY;

        while (kickIndex < relevantKicks.length && (pieceClippingLeft() || pieceClippingRight() || pieceClippingBottom())) {
            pieceX = ogPieceX + relevantKicks[kickIndex][0];
            pieceY = ogPieceY - relevantKicks[kickIndex][1];

            kickIndex++;
        }

        // returns true if a valid kick is found,
        // otherwise moves piece back and returns false
        if (kickIndex < relevantKicks.length) {
            return true;
        } else {
            pieceX = ogPieceX;
            pieceY = ogPieceY;
            return false;
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

    // checks if currentPiece is clipping the bottom
    private boolean pieceClippingBottom() {
        // int edgeRow = pieceY - 4 - numRows();
        int edgeRow = numRows() - 1 - pieceY;

        // check clipping the wall
        if (edgeRow >= 0 && edgeRow <= 3) {
            for (int col = 0; col < 4; col++) {
                for (int row = edgeRow + 1; row < 4; row++) {
                    if (currentPiece.getShapeVal(row, col) > 0) {
                        return true;
                    }
                }
            }
        }

        // check clipping other pieces
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                if (currentPiece.getShapeVal(row, col) > 0) {
                    if (squareExists(pieceY + row, pieceX + col)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // checks if currentPiece is next to the left wall
    private boolean pieceCollidingLeft() {
        int edgeCol = -1 * pieceX;

        if (edgeCol >= 0 && edgeCol <= 3) {
            for (int row = 0; row < 4; row++) {
                if (currentPiece.getShapeVal(row, edgeCol) > 0) {
                    return true;
                }
            }
        }

        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 4; row++) {
                if (currentPiece.getShapeVal(row, col) > 0) {
                    if (squareExists(pieceY + row, pieceX + col - 1)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    // checks if currentPiece is next to the right wall
    private boolean pieceCollidingRight() {
        int edgeCol = numCols() - 1 - pieceX;

        if (edgeCol >= 0 && edgeCol <= 3) {
            for (int row = 0; row < 4; row++) {
                if (currentPiece.getShapeVal(row, edgeCol) > 0) {
                    return true;
                }
            }
        }

        for (int col = 3; col >= 0; col--) {
            for (int row = 0; row < 4; row++) {
                if (currentPiece.getShapeVal(row, col) > 0) {
                    if (squareExists(pieceY + row, pieceX + col + 1)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // checks if currentPiece is next to the bottom of the grid
    private boolean pieceCollidingBottom() {
        // checks if the piece is bordering the bottom of the grid
        int edgeRow = numRows() - 1 - pieceY;

        if (edgeRow >= 0 && edgeRow <= 3) {
            for (int col = 0; col < 4; col++) {
                if (currentPiece.getShapeVal(edgeRow, col) > 0) {
                    return true;
                }
            }
        }

        // checks if the piece is bordering another piece
        for (int row = 3; row >= 0; row--) {
            for (int col = 3; col >= 0; col--) {
                if (currentPiece.getShapeVal(row, col) > 0) {
                    if (squareExists(pieceY + row + 1, pieceX + col)) {
                        return true;
                    }
                }
            }    
        }

        return false;   
    }

    public void instantFall() {
        while (!pieceCollidingBottom()) {
            pieceY++;
        }

        placePiece();
        getNewPiece();
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
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 4; row++) {
                if (currentPiece.getShapeVal(row, col) > 0) {
                    board[row + pieceY][col + pieceX] = currentPiece.color;
                }
            }
        }
        clearFilledRows();
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
        currentPiece = null;

        pieceX = 3;
        pieceY = 0;

        // int randPiece = (int) (Math.random() * 7);
        int randPiece = randGen.nextInt(7);
        // int randPiece = 0;

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
