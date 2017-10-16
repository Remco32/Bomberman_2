package Graphics;

import GameWorld.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by joseph on 9-2-2017.
 */
public class GameWindow {
    private JFrame frame;
    private GameWorld world;
    private GameCanvas gameCanvas;
    public GameWindow(GameWorld world){

        this.world = world;
        frame = new JFrame("Bomberman");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameCanvas = new GameCanvas(this.world);

        frame.add(gameCanvas,BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    public void repaint(){
        gameCanvas.repaint();
    }

    public void setWorld(GameWorld world) {
        this.world = world;
    }

    public void close(){
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }



}
