package AI;

import GameWorld.Bomb;
import GameWorld.BomberMan;
import GameWorld.GameWorld;

import java.util.ArrayList;

/**
 * Created by joseph on 15-2-2017.
 */
public class RandomAI extends AIHandler {

    public RandomAI(GameWorld world, int manIndex) {
        super(world,manIndex);
        this.world = world;
    }
    public void AddMoveToBuffer() {if(man.getAlive()) moves.add(MakeEducatedMove(man));}

    int MakeEducatedMove(BomberMan man) {
        ArrayList<Integer> moveList = man.AbleMoves();
        ArrayList<Bomb> bombList = findBombLocations(man.getX_location(), man.getY_location(), 2);//range can changed
        double[] utilityList = new double[moveList.size()];
        if (!bombList.isEmpty()) {
            for (int x = 0; x < moveList.size(); x++) {
                utilityList[x] = 0;
                for (Bomb bomb : bombList) {
                    utilityList[x] += CalcUtility(bomb, moveList.get(x), man);
                }
            }
            int maxIndex = 0;
            for (int i = 1; i < moveList.size(); i++) {
                double newNumber = utilityList[i];
                if ((newNumber > utilityList[maxIndex])) {
                    maxIndex = i;
                }
            }
            return (moveList.get(maxIndex));
        }
       return SemiRandomMove(man);

    }

    double CalcUtility(Bomb bomb, int move, BomberMan man) {
        int x = man.getX_location();
        int y = man.getY_location();
        if (move == 1) x--;
        if (move == 2) y--;
        if (move == 3) y++;
        if (move == 4) x++;
        int xUtility = Math.abs(x - bomb.getX_location());
        int yUtility = Math.abs(y - bomb.getY_location());
        return Math.sqrt(xUtility) + Math.sqrt(yUtility);
//root

    }

    ArrayList<Bomb> findBombLocations(int x_location, int y_location, int range) {
        ArrayList<Bomb> bombList = new ArrayList<>();
        range++;
        for (int xIdx = x_location - range; xIdx <= x_location + range; xIdx++) {
            for (int yIdx = y_location - range; yIdx <= y_location + range; yIdx++) {
                if (xIdx >= 0 && xIdx < world.getGridSize() && yIdx >= 0 && yIdx < world.getGridSize()) {
                    if (world.getPositions(xIdx,yIdx).getBomb() != null)
                        bombList.add(world.getPositions(xIdx,yIdx).getBomb());
                }
            }
        }
        return bombList;
    }


    int SemiRandomMove(BomberMan man) {
        int y_location = man.getY_location();
        int x_location = man.getX_location();

        int surround = 0;
        if (x_location + 1 < world.getGridSize() && world.getPositions(x_location + 1,y_location).getType()== 2) {
            surround++;
        }

        if (x_location - 1 >= 0 && world.getPositions(x_location - 1,y_location).getType()== 2) {
            surround++;
        }
        if (y_location + 1 < world.getGridSize() && world.getPositions(x_location,y_location + 1).getType()== 2) {
            surround++;
        }

        if (y_location - 1 >= 0 && world.getPositions(x_location,y_location - 1).getType()== 2) {
            surround++;
        }
        if (surround < 2) {
            return 5; //place bomb because is surrounded
        }

        while (true) { // semi random move
            int random = rnd.nextInt() % 5;
            if (x_location + 1 < world.getGridSize() && world.getPositions(x_location + 1,y_location).getType()== 2 && random == 0) {
                return 4; // move right
            }

            if (x_location - 1 >= 0 && world.getPositions(x_location - 1,y_location).getType()== 2 && random == 1) {
                return 1; // move left
            }
            if (y_location + 1 < world.getGridSize() && world.getPositions(x_location,y_location + 1).getType()== 2 && random == 2) {
                return 3; // move down
            }

            if (y_location - 1 >= 0 && world.getPositions(x_location,y_location - 1).getType()== 2 && random == 3) {
                return 2; // move up
            }
            if (random == 4 && world.getPositions(x_location,y_location).getBomb()== null) {
                return 5;
            }
        }
    }

}
