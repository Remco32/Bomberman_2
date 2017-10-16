package GameWorld;

import GameWorld.GameWorld;

import java.util.ArrayList;

/**
 * Created by joseph on 09/02/2017.
 */
public class BomberMan {
    private int x_location;
    private int y_location;
    private int id;
    private ArrayList<Integer> points = new ArrayList<>();
    private ArrayList<Integer> pointBuffer = new ArrayList<>();
    private Boolean alive;
    private GameWorld world;
    static int MOVECOST = -1;


    BomberMan(int x, int y, int id, GameWorld world) {
        this.x_location = x;
        this.y_location = y;
        this.id = id;
        this.world = world;
        points.add(new Integer(0));
        alive = true;
    }

    public void Move(int type) {
        if (!alive) return;
        pointBuffer.add(MOVECOST);
        if (type == 0) ; // do nothing
        if (type == 1) MakeMove(-1, 0);//move left
        if (type == 2) MakeMove(0, -1); //move up
        if (type == 3) MakeMove(0, 1);//move down
        if (type == 4) MakeMove(1, 0);//move right

        if (type == 5) { //place bomb
       //     System.out.println("player " + this.id + " placed a bomb");
            if (world.positions[x_location][y_location].bomb == null) {
                Bomb bomb = new Bomb(x_location, y_location, this, world);
                world.activeBombList.add(bomb);
                world.positions[x_location][y_location].add_Bomb(bomb);
            } //else System.out.println("Bomb has already been placed at this location");
        }
    }

    private void MakeMove(int x, int y) {
        world.positions[x_location][y_location].deleteBomberman(this);
        if (x == 0) {
            if (y_location + y >= 0 && y_location + y < world.gridSize &&
                    world.positions[x_location][y_location + y].type == 2) {
                y_location += y;
            } else {
                if(world.getPrint())System.out.println("player " + this.id + "cannot go to: x" + (x_location + x) + "y:" + (y_location + y));
                 pointBuffer.add(MOVECOST*2);
            }
        }

        if (y == 0) {
            if (x_location + x >= 0 && x_location + x < world.gridSize &&
                    world.positions[x_location + x][y_location].type == 2) {
                x_location += x;
            } else {
                if(world.getPrint())System.out.println("player " + this.id + "cannot go to: x" + (x_location + x) + "y:" + (y_location + y));
                pointBuffer.add(MOVECOST*2);
            }
        }
        world.positions[x_location][y_location].add_bomberman(this);
    }

    public ArrayList<Integer> AbleMoves() {
        ArrayList<Integer> moves = new ArrayList<>();
        moves.add(0);// always possible to do nothing


        if (x_location - 1 >= 0 && world.positions[x_location - 1][y_location].type == 2) {
            moves.add(1); // move left
        }
        if (y_location - 1 >= 0 && world.positions[x_location][y_location - 1].type == 2) {
            moves.add(2); // move up
        }
        if (y_location + 1 < world.gridSize && world.positions[x_location][y_location + 1].type == 2) {
            moves.add(3); // move down
        }
        if (x_location + 1 < world.gridSize && world.positions[x_location + 1][y_location].type == 2) {
            moves.add(4); // move right
        }
        if (world.positions[x_location][y_location].bomb == null) {
            moves.add(5); // place bomb
        }
        return moves;
    }

    public void Die() {
        alive = false;
    }

    public int getX_location() {
        return x_location;
    }

    public int getY_location() {
        return y_location;
    }

    public int getId() {
        return id;
    }

    public ArrayList<Integer> getPoints() {
        return points;
    }

    public Boolean getAlive() {
        return alive;
    }

    public GameWorld getWorld() {
        return world;
    }

    public ArrayList<Integer> getPointBuffer() {return this.pointBuffer;}

    public void loadBufferIntoPoints(){
        int addition=0;
        for(int temp:pointBuffer){
            addition+=temp;
        }
        points.add(addition);
        pointBuffer = new ArrayList<>();
    }

    static public void setMOVECOST(int cost){MOVECOST = cost;}

}
