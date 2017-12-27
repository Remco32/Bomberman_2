package Graphics;

import GameWorld.BomberMan;
import GameWorld.GameWorld;
import GameWorld.WorldPosition;

import javax.swing.*;
import java.awt.*;
/**
 * Created by joseph on 9-2-2017.
 */
public class GameCanvas extends JPanel {
    private GameWorld world;
    private Images image;

    GameCanvas(GameWorld world) {
        this.world = world;
        image = new Images();
        setPreferredSize(new Dimension(world.getGridSize() * 50, world.getGridSize() * 50));
    }

    public void paint(Graphics g) {
        super.paint(g);
        WorldPosition position;
        for (int x = 0; x < world.getGridSize(); x++) {
            for (int y = 0; y < world.getGridSize(); y++) {
                position = world.getPositions(x,y);
                if (position.getType() == 0) paintWallHard(g, x * 50, y * 50);
                if (position.getType()== 1) paintWallSoft(g, x * 50, y * 50);
                if (position.getType() == 2) paintRoad(g, x * 50, y * 50);
                if (!position.getBombermanList().isEmpty())
                    paintBomberMan(g, x * 50 + 5, y * 50 + 5, position.getBombermanList().get(0));
                if (position.getBomb() != null) paintBomb(g, x * 50 + 10, y * 50 + 10);
            }
        }
    }

    private void paintWallSoft(Graphics g, int x, int y) {
        g.drawImage(image.getWallSoft(), x, y, null);
    }

    private void paintWallHard(Graphics g, int x, int y) {
        g.drawImage(image.getWallHard(), x, y, null);
    }

    private void paintRoad(Graphics g, int x, int y) {
        g.setColor(Color.gray);
        g.fillRect(x, y, 50, 50);
    }

    private void paintBomb(Graphics g, int x, int y) {
        g.drawImage(image.getBomb(), x, y, null);
    }

    private void paintBomberMan(Graphics g, int x, int y, BomberMan man) {
        if (man.getId() == 1) g.drawImage(image.getPlayer1(), x, y, Color.gray, null);
        if (man.getId() == 2) g.drawImage(image.getPlayer2(), x, y, Color.gray, null);
        if (man.getId() == 3) g.drawImage(image.getPlayer3(), x, y, Color.gray, null);
        if (man.getId() == 4) g.drawImage(image.getPlayer4(), x, y, Color.gray, null);


    }

}
