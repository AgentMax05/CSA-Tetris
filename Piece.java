import java.awt.Color;

public class Piece {
    private int[][] shapes;
    
    public int currentShape;
    public final Color color;
    
    // array that contains possible transformations for each
    // wall kick when a piece is rotated
    // the I piece has a different set of wall kicks
    // which is overriden in its class

    // wall kicks found here: https://tetris.fandom.com/wiki/SRS

    // wall kicks are ordered in counterclockwise first, clockwise second
    // Y-transformations are assuming positive Y is upwards (which is false in this case)
    public int[][][][] wallKicks = {
        // wall kicks going from 0
        {
            // 0 >> 3
            {{1, 0}, {1, 1}, {0, -2}, {1, -2}},
            // 0 >> 1
            {{-1, 0}, {-1, 1}, {0, -2}, {-1, -2}}
        },
        // wall kicks going from 1
        {
            // 1 >> 0
            {{1, 0}, {1, -1}, {0, 2}, {1, 2}},
            // 1 >> 2
            {{1, 0}, {1, -1}, {0, 2}, {1, 2}}
        },
        // wall kicks going from 2
        {
            // 2 >> 1
            {{-1, 0}, {-1, 1}, {0, -2}, {-1, -2}},
            // 2 >> 3
            {{1, 0}, {1, 1}, {0, -2}, {1, -2}}
        },
        // wall kicks going from 3
        {
            // 3 >> 2
            {{-1, 0}, {-1, -1}, {0, 2}, {-1, 2}},
            // 3 >> 0
            {{-1, 0}, {-1, -1}, {0, 2}, {-1, 2}}
        }
    };

    public int[][] getRelevantWallKick(int initialRotation, boolean counterClockwise) {
        return wallKicks[initialRotation][counterClockwise ? 0 : 1];
    }

    public Piece(int[][] newShapes, Color newColor){
        currentShape = 0;
        shapes = newShapes;
        color = newColor;
    }

    public void rotateClockwise() {
        currentShape = (currentShape+1) % shapes.length;
    }

    public void rotateCounterClockwise() {
        currentShape = currentShape > 0 ? currentShape - 1 : shapes.length - 1;
    }

    public int getShapeVal(int row, int col) {
        return shapes[currentShape][row * 4 + col];
    }

    public void resetRotation() {
        currentShape = 0;
    }
}
