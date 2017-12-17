package GameWorld;

import GameWorld.Bomb;
import GameWorld.BomberMan;

import java.util.ArrayList;

/**
 * Created by joseph on 09/02/2017.
 * <p>
 * Every position in the world grid is a world position
 */
public class WorldPosition {
    protected ArrayList<BomberMan> bombermanList;
    protected Bomb bomb;
    protected int x_location;
    private int y_location;
    protected int type; //0=hardwall,1=softwall,2=no wall

    // for A* algorithm
    public int movementCostSoFor = 0; //g
    public int estimationRemainingPathscore = 0; //h
    private int pathScore = 0;  //f
    private int pathScoreFromStartposition = 0;

    public ArrayList<BomberMan> getBombermanList() {
        return bombermanList;
    }

    public Bomb getBomb() {
        return bomb;
    }

    public int getX_location() {
        return x_location;
    }

    public int getY_location() {
        return y_location;
    }

    public int getType() {
        return type;
    }



    WorldPosition(int x, int y, int type) {
        this.x_location = x;
        this.y_location = y;
        this.type = type;
        bombermanList = new ArrayList<>();
    }

    void add_Bomb(Bomb bomb) {
        this.bomb = bomb;
    }

    void deleteBomb() {
        this.bomb = null;
    }

    void add_bomberman(BomberMan bomberman) {
        bombermanList.add(bomberman);
    }

    void deleteBomberman(BomberMan bomberman) {
        bombermanList.remove(bomberman);
    }

    public void setType(int type) {
        this.type = type;
    }

    public void resetPathScores() {
        this.pathScore = 0;
        this.pathScoreFromStartposition = 0;
    }

    public int getPathScore() {
        return pathScore;
    }

    public void setPathScore(int value) {
        this.pathScore = value;
    }

    public int getPathScoreFromStartposition() {
        return pathScoreFromStartposition;
    }

    public void setPathScoreFromStartposition(int value) {
        this.pathScoreFromStartposition = value;
    }

}
