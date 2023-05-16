import java.awt.Color;

public class IPiece extends Piece {
    private static int[][] shapes = {
        {
            0, 0, 0, 0,
            1, 1, 1, 1,
            0, 0, 0, 0,
            0, 0, 0, 0
        },
        {
            0, 0, 1, 0,
            0, 0, 1, 0,
            0, 0, 1, 0,
            0, 0, 1, 0
        },
        {
            0, 0, 0, 0,
            0, 0, 0, 0,
            1, 1, 1, 1,
            0, 0, 0, 0
        }, 
        {
            0, 1, 0, 0,
            0, 1, 0, 0,
            0, 1, 0, 0,
            0, 1, 0, 0
        }
    };

    // unique wall kick data for the I Piece
    public int[][][][] wallKicksI = {
        // wall kicks from 0
        {
            // 0 >> 3
            {{-1, 0}, {2, 0}, {-1, 2}, {2, -1}},
            // 0 >> 1
            {{-2, 0}, {1, 0}, {-2, -1}, {1, 2}}
        },
        // wall kicks from 1
        {
            // 1 >> 0
            {{2, 0}, {-1, 0}, {2, 1}, {-1, -2}},
            // 1 >> 2
            {{-1, 0}, {2, 0}, {-1, 2}, {2, -1}} 
        },
        // wall kicks from 2
        {
            // 2 >> 1
            {{1, 0}, {-2, 0}, {1, -2}, {-2, 1}},
            // 2 >> 3
            {{2, 0}, {-1, 0}, {2, 1}, {-1, -2}}
        },
        // wall kicks from 3
        {
            // 3 >> 2
            {{-2, 0}, {1, 0}, {-2, -1}, {1, 2}},
            // 3 >> 0
            {{1, 0}, {-2, 0}, {1, -2}, {-2, 1}}
        }
    };

    public int[][] getRelevantWallKick(int initialRotation, boolean counterClockwise) {
        return wallKicksI[initialRotation][counterClockwise ? 0 : 1];
    }

    private static Color color = new Color(0, 255, 255);

    public IPiece() {
        super(shapes, color);
    }
}
