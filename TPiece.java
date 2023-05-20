import java.awt.Color;

public class TPiece extends Piece {
    private static int[][] shapes = {
        {
            0, 1, 0, 0,
            1, 1, 1, 0,
            0, 0, 0, 0,
            0, 0, 0, 0
        },
        {
            0, 1, 0, 0,
            0, 1, 1, 0,
            0, 1, 0, 0,
            0, 0, 0, 0
        },
        {
            0, 0, 0, 0,
            1, 1, 1, 0,
            0, 1, 0, 0,
            0, 0, 0, 0
        },
        {
            0, 1, 0, 0,
            1, 1, 0, 0,
            0, 1, 0, 0, 
            0, 0, 0, 0
        }
    };

    private static Color color = new Color(128, 0, 128);

    public TPiece() {
        super(shapes, color);
    }
}
