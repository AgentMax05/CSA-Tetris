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
    private int ghostY;

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

    private int[] pieceBag;
    private int randPieceIndex;

    public Board(int x, int y) {
        board = new Color[20][10];
        currentPiece = null;
        lastFall = Instant.now();

        posX = x;
        posY = y;

        nextPieces = new Piece[3];
        randGen = new SecureRandom();

        pieceBag = new int[]{0, 1, 2, 3, 4, 5, 6};
        shuffleBag();

        getNewPiece();
    }

    // shuffles the piece bag
    private void shuffleBag() {
        for (int i = 0; i < pieceBag.length; i++) {
            int randIndex = randGen.nextInt(pieceBag.length);
            int temp = pieceBag[randIndex];
            pieceBag[randIndex] = pieceBag[i];
            pieceBag[i] = temp;
        }
        randPieceIndex = pieceBag.length - 1;
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
        calcGhostY();
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

        pieceX--;
        if (pieceClipping()) {
            pieceX++;
        } else {
            calcGhostY();
        }
    }

    public void moveRight() {
        if (gameOver) return;

        pieceX++;
        if (pieceClipping()) {
            pieceX--;
        } else {
            calcGhostY();
        }
    }

    public void rotateClockwise() {
        if (gameOver) return;

        int lastRotationIndex = currentPiece.currentShape;
        currentPiece.rotateClockwise();
        if (!testWallKicks(lastRotationIndex, false)) {
            currentPiece.rotateCounterClockwise();
        } else {
            calcGhostY();
        }
    }

    public void rotateCounterclockwise() {
        if (gameOver) return;

        int lastRotationIndex = currentPiece.currentShape;
        currentPiece.rotateCounterClockwise();
        if (!testWallKicks(lastRotationIndex, true)) {
            currentPiece.rotateClockwise();
        } else {
            calcGhostY();
        }
    }

    // attempts to relocate the piece based on wall kicks
    public boolean testWallKicks(int lastRotation, boolean counterClockwise) {
        int kickIndex = 0;
        int[][] relevantKicks = currentPiece.getRelevantWallKick(lastRotation, counterClockwise);

        int ogPieceX = pieceX;
        int ogPieceY = pieceY;

        while (kickIndex < relevantKicks.length && pieceClipping()) {
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

    // checks if currentPiece is clipping another piece
    private boolean pieceClipping() {
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                if (currentPiece.getShapeVal(row, col) > 0) {
                    // make sure x position is in bounds
                    if (pieceX + col < 0 || pieceX + col >= numCols()) {
                        return true;
                    } 
                    // make sure y position is in bounds
                    if (pieceY + row < 0 || pieceY + row >= numRows()) {
                        return true;
                    }
                    // check if piece is clipping
                    if (squareExists(pieceY + row, pieceX + col)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // calculates the y-coordinate of the ghost piece
    private void calcGhostY() {
        if (currentPiece == null) return;

        int ogY = pieceY;
        while (!pieceClipping()) {
            pieceY++;
        }
        ghostY = pieceY - 1;
        pieceY = ogY;
    }

    public int getGhostY() {
        return ghostY;
    }

    // puts the piece down
    public void instantFall() {
        if (gameOver) return;

        while (!pieceClipping()) {
            pieceY++;
            score += 2 * level;
        }
        pieceY--;
        score -= 2 * level;

        placePiece();
        getNewPiece();
    }

    // moves the piece down one grid or
    // places the piece down
    private void fallPiece() {
        pieceY++;
        if (!pieceClipping()) {
            score += fastFall ? level : 0;
        } else {
            pieceY--;
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

    // returns a new piece based on a number
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

    // returns a random piece
    private Piece getRandPiece() {
        if (randPieceIndex < 0) {
            shuffleBag();
        }
        Piece randPiece = pieceFromNum(pieceBag[randPieceIndex]);
        randPieceIndex--;
        return randPiece;
    }

    // generates a new piece
    private void getNewPiece() {
        pieceX = 3;
        pieceY = 0;

        if (nextPieces[0] != null) {
            currentPiece = nextPieces[0];
            nextPieces[0] = nextPieces[1];
            nextPieces[1] = nextPieces[2];
            nextPieces[2] = getRandPiece();
        } else {
            currentPiece = getRandPiece();
            for (int i = 0; i < 3; i++) {
                nextPieces[i] = getRandPiece();
            }
        }

        if (pieceClipping()) {
            gameOver = true;
            currentPiece = null;
        }
        
        calcGhostY();
    }
}
