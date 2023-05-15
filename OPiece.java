import java.awt.Color;

public class OPiece extends Piece {
    private static int[][] shapes = {
        {
            0, 1, 1, 0,
            0, 1, 1, 0,
            0, 0, 0, 0,
            0, 0, 0, 0
        }
    };

    // private static Color color = new Color(255, 255, 0);
    private static Color color = new Color(247, 211, 8);

    public OPiece() {
        super(shapes, color);
    }
}