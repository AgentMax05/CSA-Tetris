import java.awt.Color;

public class Piece {
    int[][] shapes;
    int currentShape;
    Color color;
    
    
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
}
