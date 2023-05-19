import java.time.*;
import java.awt.Color;
import java.security.SecureRandom;

public class Board {
    private Color[][] board;

    private int score;
    private int linesCleared = 0;

    private Piece currentPiece;
    private Piece[] nextPieces;

    private Piece heldPiece = null;

    private boolean pieceHeld = false;

    private int pieceX, pieceY;
    public final int posX, posY;

    private Instant lastFall;

    private int level = 1;

    private SecureRandom randGen;

    // frames per gridcell down
    private int gravityFrames = 43;
    
    private final int fastFallGravityFrames = 5;
    public boolean fastFall = false;
    
    private static final double secondsPerFrame = 1.0 / 60;

    private boolean gameOver = false;

    public Board(int x, int y) {
        board = new Color[20][10];

        currentPiece = null;

        lastFall = Instant.now();

        posX = x;
        posY = y;

        nextPieces = new Piece[3];

        randGen = new SecureRandom();

        getNewPiece();
    }

    public int getScore() {
        return score;
    }

    public void update() {  
        if (!gameOver && Duration.between(lastFall, Instant.now()).toMillis() / 1000.0 >= ((fastFall ? fastFallGravityFrames : gravityFrames) * secondsPerFrame)) {
            lastFall = Instant.now();
            fallPiece();
        }
    }

    public boolean isHeldPiece() {
        return heldPiece != null;
    }

    public Piece getHeldPiece() {
        return heldPiece;
    }

    public Piece[] getNextPieces() {
        return nextPieces;
    }

    public int getLevel() {
        return level;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    // holds a piece
    public void holdPiece() {
        if (gameOver) return;

        if (!pieceHeld) {
            pieceX = 3;
            pieceY = 0;
            currentPiece.resetRotation();
            if (heldPiece != null) {
                Piece temp = heldPiece;
                heldPiece = currentPiece;
                currentPiece = temp;
            } else {
                heldPiece = currentPiece;
                getNewPiece();
            }
            pieceHeld = true;
        }
    }

    public int getLinesCleared() {
        return linesCleared;
    }

    // clears filled rows
    private void clearFilledRows() {
        int numRowsCleared = 0;
        for (int row = 0; row < numRows(); row++) {
            boolean rowFilled = true;
            for (int col = 0; col < numCols(); col++) {
                if (!squareExists(row, col)) {
                    rowFilled = false;
                    break;
                }
            }
            if (rowFilled) {
                numRowsCleared++;
                for (int r = row; r > 0; r--) {
                    // board[r] = board[r-1];
                    for (int col = 0; col < numCols(); col++) {
                        board[r][col] = board[r-1][col];
                    }
                }
            }
        }

        // TODO: add T-spin scoring
        if (numRowsCleared == 1) {
            score += 100 * level;
        } else if (numRowsCleared == 2) {
            score += 300 * level;
        } else if (numRowsCleared == 3) {
            score += 500 * level;
        } else if (numRowsCleared == 4) {
            score += 800 * level;
        }

        linesCleared += numRowsCleared;

        if (linesCleared > level * 10) {
            nextLevel();
        }
    }

    public void moveLeft() {
        if (gameOver) return;

        if (!pieceCollidingLeft()) {
            pieceX--;
        }
    }

    public void moveRight() {
        if (gameOver) return;

        if (!pieceCollidingRight()) {
            pieceX++;
        }
    }

    public void rotateClockwise() {
        if (gameOver) return;

        int lastRotationIndex = currentPiece.currentShape;
        currentPiece.rotateClockwise();
        if (!testWallKicks(lastRotationIndex, false)) {
            currentPiece.rotateCounterClockwise();
        }
    }

    public void rotateCounterclockwise() {
        if (gameOver) return;

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
        if (gameOver) return;

        while (!pieceCollidingBottom()) {
            pieceY++;
            score += 2 * level;
        }

        placePiece();
        getNewPiece();
    }

    private void fallPiece() {
        if (!pieceCollidingBottom()) {
            pieceY++;
            score += fastFall ? level : 0;
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

        pieceHeld = false;
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

    private Piece pieceFromNum(int pieceNum) {
        switch (pieceNum) {
            case 0:
                return new IPiece();
            case 1:
                return new JPiece();
            case 2:
                return new LPiece();
            case 3:
                return new OPiece();
            case 4:
                return new SPiece();
            case 5:
                return new TPiece();
            case 6:
                return new ZPiece();
        }
        return null;
    }

    public void getNewPiece() {
        pieceX = 3;
        pieceY = 0;

        int randPiece = randGen.nextInt(7);

        if (nextPieces[0] != null) {
            currentPiece = nextPieces[0];
            // nextPieces[ = pieceFromNum(randPiece);
            nextPieces[0] = nextPieces[1];
            nextPieces[1] = nextPieces[2];
            nextPieces[2] = pieceFromNum(randPiece);
        } else {
            currentPiece = pieceFromNum(randPiece);
            for (int i = 0; i < 3; i++) {
                nextPieces[i] = pieceFromNum(randGen.nextInt(7));
            }
        }

        if (pieceClippingRight() || pieceClippingLeft() || pieceClippingBottom()) {
            gameOver = true;
            currentPiece = null;
        }
    }
}
