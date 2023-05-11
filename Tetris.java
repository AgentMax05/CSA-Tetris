package tetris;

import javax.swing.*;
// import java.awt.*;

public class Tetris extends JFrame {
    public Tetris() {
        super("Tetris");

        setContentPane(new DrawPane());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 400);
        setVisible(true);
    }

    class DrawPane extends JPanel {
        public void paintComponent(java.awt.Graphics g) {
            for (int i = 0; i < 1; i++) {
                i *= 100;
                g.fill3DRect(i, i, i, i, true);
            }
        }
    }

    public static void main(String[] args) {
        new Tetris();
        
    }
}

// public class Window {
//     private JFrame frame;

//     public Window (String windowName, int sizeX, int sizeY) {
//         frame = new JFrame(windowName);
//         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//         frame.setSize(sizeX, sizeY);
//         frame.setVisible(true);
//     }
// }