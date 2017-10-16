package Graphics;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by joseph on 9-2-2017.
 */
public class Images {
    private Image road;
    private Image wallSoft;
    private Image wallHard;
    private Image bomb;
    private Image player1;
    private Image player2;
    private Image player3;
    private Image player4;

    Images(){
        try {
            BufferedImage buffer = ImageIO.read(getClass().getResource("resources/wallBreak.jpeg"));
            wallSoft = buffer.getScaledInstance(50, 50, wallSoft.SCALE_DEFAULT);
            buffer = ImageIO.read(getClass().getResource("resources/concrete.jpg"));
            wallHard = buffer.getScaledInstance(50, 50, wallSoft.SCALE_DEFAULT);
            buffer = ImageIO.read(getClass().getResource("resources/bomb.png"));
            bomb = buffer.getScaledInstance(30, 30, wallSoft.SCALE_DEFAULT);
            buffer = ImageIO.read(getClass().getResource("resources/green.png"));
            road = buffer.getScaledInstance(50, 50, wallSoft.SCALE_DEFAULT);
            buffer = ImageIO.read(getClass().getResource("resources/bomberman1.png"));
            player1 = buffer.getScaledInstance(40, 40, wallSoft.SCALE_DEFAULT);
            buffer = ImageIO.read(getClass().getResource("resources/bomberman2.png"));
            player2 = buffer.getScaledInstance(40, 40, wallSoft.SCALE_DEFAULT);
            buffer = ImageIO.read(getClass().getResource("resources/bomberman3.png"));
            player3 = buffer.getScaledInstance(40, 40, wallSoft.SCALE_DEFAULT);
            buffer = ImageIO.read(getClass().getResource("resources/bomberman4.png"));
            player4 = buffer.getScaledInstance(40, 40, wallSoft.SCALE_DEFAULT);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Image getRoad() {
        return road;
    }

    public Image getWallSoft() {
        return wallSoft;
    }

    public Image getWallHard() {
        return wallHard;
    }

    public Image getBomb() {
        return bomb;
    }

    public Image getPlayer1() {
        return player1;
    }

    public Image getPlayer2() {
        return player2;
    }

    public Image getPlayer3() {
        return player3;
    }

    public Image getPlayer4() {
        return player4;
    }
}
