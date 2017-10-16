package GameWorld;

import GameWorld.GameWorld;
import org.apache.commons.math3.analysis.function.Max;

/**
 * Created by joseph on 09/02/2017.
 */
public class Bomb {
    Boolean PRINT = false; // debugging

    static private int MAXTIMER = 5;
    private int currentTimer=MAXTIMER;
    private int range = 2;
    private int x_location;
    private int y_location;
    private int id;
    private BomberMan placedBy;
    private Boolean exploded;
    private GameWorld world;
    static int DIECOST=-30;
    static int KillReward=15;
    static int wallReward=8;

    Bomb(int x, int y, BomberMan by, GameWorld world) {

        this.x_location = x;
        this.y_location = y;
        this.id = world.activeBombList.size()+world.explodedBombList.size();
        this.placedBy = by;
        this.world = world;
        exploded = false;
    }
    Bomb(WorldPosition pos,BomberMan by,GameWorld world) {

        this(pos.getX_location(),pos.getY_location(),by,world);
    }

    void Round() {
        if (exploded) return;
        if (currentTimer > 0) currentTimer--;
        else Explode();
    }

    void add_SubtractPoints(BomberMan man, int amount){
        man.getPointBuffer().add(amount);
    }

    void Explode() {
        if (exploded == true) return;

        // check for exploding up
        for (int yTemp = y_location; yTemp <= y_location + range; yTemp++) {
            if (yTemp >= 0 && yTemp < world.gridSize) {
               if(ExplodeHitLocation(x_location,yTemp)) yTemp = y_location + range+1;
            }
        }
        //check for exploding down
        for (int yTemp = y_location; yTemp >= y_location - range; yTemp--) {
            if (yTemp >= 0 && yTemp < world.gridSize) {
               if(ExplodeHitLocation(x_location,yTemp)) yTemp = y_location-range-1;
            }
        }
        // check for exploding left
        for (int xTemp = x_location; xTemp <= x_location + range; xTemp ++) {
            if (xTemp  >= 0 && xTemp  < world.gridSize) {
                if(ExplodeHitLocation(xTemp,y_location)) xTemp = x_location + range +1;
            }
        }
        // check for exploding right
        for (int xTemp = x_location; xTemp >= x_location - range; xTemp--) {
            if (xTemp >= 0 && xTemp < world.gridSize) {
                if(ExplodeHitLocation(xTemp,y_location)) xTemp = x_location - range -1;
            }
        }

        exploded = true;
        world.positions[x_location][y_location].deleteBomb();
    }


    Boolean ExplodeHitLocation(int xTemp,int yTemp){
        if (!world.getPositions(xTemp,yTemp).bombermanList.isEmpty()) {
            for (BomberMan man : world.positions[xTemp][yTemp].bombermanList) {
                add_SubtractPoints(placedBy,KillReward); // 300 points for killing
                add_SubtractPoints(man,DIECOST);//-100 points for dying
                man.Die();
                if(world.getPrint())System.out.println("player " + man.getId() + " has been killed by player " + placedBy.getId());
            }
            world.positions[xTemp][yTemp].bombermanList.clear();
        }

        if (world.positions[xTemp][yTemp].type == 0) {
            return true;
        } else if (world.positions[xTemp][yTemp].type == 1) {
            world.positions[xTemp][yTemp].type = 2;
            add_SubtractPoints(placedBy,wallReward);//20 points for destroying a wall
            return true;
        }
        return false;
    }

    public Boolean getPRINT() {
        return PRINT;
    }
    public static int getMAXTIMER() {
        return MAXTIMER;
    }
    public int getCurrentTimer() {
        return currentTimer;
    }
    public int getRange() {
        return range;
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
    public BomberMan getPlacedBy() {
        return placedBy;
    }
    public Boolean getExploded() {
        return exploded;
    }
    public GameWorld getWorld() {
        return world;
    }

    public static void setDIECOST(int DIECOST) {
        Bomb.DIECOST = DIECOST;
    }
    public static void setKillReward(int killReward) {
        KillReward = killReward;
    }
    public static void setWallReward(int wallReward) {
        Bomb.wallReward = wallReward;
    }
}

