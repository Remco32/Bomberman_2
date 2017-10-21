package AI;

import GameWorld.*;
import MLP.AbstractActivationFunction;
import MLP.ActivationVectorList;
import MLP.MLP;
import util.GameSettings;
import util.NNSettings;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.pow;

/**
 * Created by Remco on 21-10-2017.
 */


public class HierarchicalAI extends AIHandler {
    private boolean DEBUG = true;

    public HierarchicalAI(GameWorld world, int manIndex, NNSettings setting, GameSettings gSet) {
        super(world, manIndex);
        this.world = world;

        mlp = new MLP(learningRate);

        this.setDiscount(setting.getDiscount());
        this.setLearningRate(setting.getLearningRate());
        this.setExplorationChance(setting.getExplorationRate());
        this.setGenerationSize(gSet.getAmountOfGenerations());
        this.setEpochSize(gSet.getAmountOfEpochs());

        if (DEBUG) System.out.println("Hierch");
    }

    public void AddMoveToBuffer() {
        if (man.getAlive()) moves.add(0); //do nothing
    }

    public void aStar(){
        ArrayList<WorldPosition> openList; //locations that are being considered
        ArrayList<WorldPosition> closedList; //locations that do not have to be considered

        //add possible locations around agent to the openlist





    }

    public int cityblockDistance(WorldPosition currentLocation, WorldPosition targetLocation){
        return abs(currentLocation.getX_location() - targetLocation.getX_location())
                + abs(currentLocation.getY_location() - targetLocation.getY_location());
    }




    boolean checkValidMovement(WorldPosition targetPosition) {
        int targetX = targetPosition.getX_location();
        int targetY = targetPosition.getY_location();

        //out of bounds
        if (targetX > world.gridSize - 1 || targetY > world.gridSize - 1 || targetX < 0 || targetY < 0) {
            return false;
        }

        //Return a false if there is a hardwall or softwall at this position
        if (world.getPositions(targetX,targetY).getType() == 0 || world.getPositions(targetX,targetY).getType() == 1) { //0=hardwall,1=softwall,2=no wall
            return false;

        }
        return true;

    }

}
