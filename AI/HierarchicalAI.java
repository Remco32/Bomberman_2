package AI;

import GameWorld.*;
import MLP.MLP;
import util.GameSettings;
import util.NNSettings;

import java.io.BufferedWriter;
import java.util.*;

import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.pow;

import GameWorld.*;
import MLP.AbstractActivationFunction;
import MLP.ActivationVectorList;
import MLP.MLP;
import util.GameSettings;
import util.NNSettings;

import java.io.*;
import java.lang.reflect.Array;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Remco on 21-10-2017.
 */

public class HierarchicalAI extends TimeDrivenBoltzmanNNFullInput {
    private boolean DEBUG = true;
    WorldPosition targetPosition;




    public HierarchicalAI(GameWorld world, int manIndex, NNSettings setting, GameSettings gSet) {


        super(world, manIndex, setting, gSet);
        this.world = world;


/**
        mlp = new MLP(learningRate);

        this.setDiscount(setting.getDiscount());
        this.setLearningRate(setting.getLearningRate());
        this.setExplorationChance(setting.getExplorationRate());
        this.setGenerationSize(gSet.getAmountOfGenerations());
        this.setEpochSize(gSet.getAmountOfEpochs());
**/
        if (DEBUG) System.out.println("Using hierachical");
    }

    public void AddMoveToBuffer() {
        int targetEnemy = checkPathToEnemies();
        if(targetEnemy == -2) {
            double[] output = CalculateBestMove().getOutput();
            int move = 0;
            double maxOutcome = Double.NEGATIVE_INFINITY;
            for (int idx = 0; idx < output.length; idx++) {
                if (maxOutcome < output[idx]) {
                    move = idx;
                    maxOutcome = output[idx];
                }
            }
            double random = rnd.nextDouble();
            if (!testing && random < explorationChance) {
                move = TimeBoltzMan(output);
                if (PRINT) System.out.println("random!");
            }
            moves.add(move);// add the move to the move list
        }else {
            if (DEBUG) System.out.println("Enemy accessible, we should switch strategies.");
        }

    }

    /**

    public void AddMoveToBuffer() {

        ArrayList<AIHandler> listOfEnemies = world.getAi();
        for (int x = 1; x < listOfEnemies.size(); x++) { //offset by one so our own AI is ignored
            //get the X and Y of the enemy
            WorldPosition enemyLocation = world.getPositions(listOfEnemies.get(x).getMan().getX_location(), listOfEnemies.get(x).getMan().getY_location());

            //search for paths using aStar
            aStar(enemyLocation);
        }

        if (man.getAlive()) moves.add(0); //do nothing
    }
     **/

    int checkPathToEnemies(){ //returns ID of enemy to which a path is possible. TODO: Does not return multiple values when more enemies are accessible

        ArrayList<AIHandler> listOfEnemies = world.getAi();
        for (int x = 1; x < listOfEnemies.size(); x++) { //offset by one so our own AI is ignored
            //get the X and Y of the enemy
            WorldPosition enemyLocation = world.getPositions(listOfEnemies.get(x).getMan().getX_location(), listOfEnemies.get(x).getMan().getY_location());

            //search for paths using aStar
            return aStar(enemyLocation);
        }
        return -2; //no paths
    }

    //TODO doesn't work completely like it should
    int aStar(WorldPosition targetPosition) { //returns ID of enemy to which a path is found
        this.targetPosition = targetPosition;
        int targetID;
        if (targetPosition.getBombermanList().isEmpty()) {
            targetID = -1;
        } else {
            targetID = targetPosition.getBombermanList().get(0).getId();
        }
        WorldPosition previousPosition = world.getPositions(man.getX_location(), man.getY_location()); // set our startingposition as the previous position. This object is used to compare pathlenghts.
        calculateAndSetPathscore(previousPosition); //set its score, which should be 0

        ArrayList<WorldPosition> openList = new ArrayList<>(); //locations that are being considered
        ArrayList<WorldPosition> closedList = new ArrayList<>(); //locations that do not have to be considered

        //add possible locations around agent to the openlist

        addSurroundingLocations(openList, world.getPositions(man.getX_location(), man.getY_location()));
        WorldPosition positionConsidering = openList.get(0); //take first item

        //loop until we found our targetPosition, our until we run out of positions in the openlist
        while (!openList.isEmpty()) {
            if(targetPosition.getX_location() == positionConsidering.getX_location() && targetPosition.getY_location() == positionConsidering.getY_location()){
                if (DEBUG) System.out.println("Path from " +man.getX_location()+ "," + man.getY_location() +" to " + targetPosition.getX_location() + "," + targetPosition.getY_location() + " (enemy "+ targetID + ")") ;
                return targetID;
            }

            //Sort the resulting list from lowest pathscore to highest
            sortListByPathScore(openList);

            //take first object in the open list and move it to the closed list
            positionConsidering = openList.get(0); //take first item
            openList.remove(0); //remove it
            closedList.add(positionConsidering); // add to closed list

            ArrayList<WorldPosition> tempList = new ArrayList<>(); //locations that are adjacent to current position
            addSurroundingLocations(tempList, positionConsidering);

            //Process all new positions
            while (!tempList.isEmpty()) {
                WorldPosition tempPosition = tempList.get(0); // get first element of list
                tempList.remove(0); //remove it

                if (closedList.contains(tempPosition)) {
                    break; //ignore positions that are in the closed list
                }

                if (!openList.contains(tempPosition)) { //not in the open list
                    //calculate pathScore
                    calculateAndSetPathscore(tempPosition);
                    openList.add(tempPosition); //add location to open list
                }

                if (openList.contains(tempPosition)) { //in open list
                    //Check if the pathscore is lower when we use the current generated path to get there.
                    if (tempPosition.getPathScore() < previousPosition.getPathScore()) {
                        //update its score and update its parent as well.
                        calculateAndSetPathscore(tempPosition);
                        calculateAndSetPathscore(previousPosition);

                    }

                }

                //set previous considered position to variable
                previousPosition = tempPosition;

            }

        }

       // if (DEBUG) System.out.println("Processed all positions.");
        return -2;

    }

    int cityblockDistance(WorldPosition currentLocation, WorldPosition targetLocation) {
        return abs(currentLocation.getX_location() - targetLocation.getX_location())
                + abs(currentLocation.getY_location() - targetLocation.getY_location());
    }

    boolean checkForNoObstruction(WorldPosition targetPosition) {
        int targetX = targetPosition.getX_location();
        int targetY = targetPosition.getY_location();
        return checkForNoObstruction(targetX, targetY);
    }

    boolean checkForNoObstruction(int targetX, int targetY) {
        //out of bounds
        if (targetX > world.gridSize - 1 || targetY > world.gridSize - 1 || targetX < 0 || targetY < 0) {
            return false;
        }

        //Return a false if there is a hardwall or softwall at this position
        if (world.getPositions(targetX, targetY).getType() == 0 || world.getPositions(targetX, targetY).getType() == 1) { //0=hardwall,1=softwall,2=no wall
            return false;

        }
        return true;

    }

    //add possible locations around target to a list
    ArrayList<WorldPosition> addSurroundingLocations(ArrayList<WorldPosition> openList, WorldPosition location) {

        if (checkForNoObstruction(location.getX_location() + 1, location.getY_location())) {
            openList.add(world.getPositions(location.getX_location() + 1, location.getY_location())); //add this location
        }
        if (checkForNoObstruction(location.getX_location() - 1, location.getY_location())) {
            openList.add(world.getPositions(location.getX_location() - 1, location.getY_location())); //add this location
        }
        if (checkForNoObstruction(location.getX_location(), location.getY_location() + 1)) {
            openList.add(world.getPositions(location.getX_location(), location.getY_location() + 1)); //add this location
        }
        if (checkForNoObstruction(location.getX_location() + 1, location.getY_location() - 1)) {
            openList.add(world.getPositions(location.getX_location(), location.getY_location() - 1)); //add this location
        }

        return openList;

    }

    public void sortListByPathScore(ArrayList<WorldPosition> list) {

        Collections.sort(list, new Comparator<WorldPosition>() {
            @Override
            public int compare(WorldPosition w1, WorldPosition w2) {
                if (w1.getPathScore() > w2.getPathScore())
                    return 1;
                if (w1.getPathScore() < w2.getPathScore())
                    return -1;
                return 0;
            }
        });

    }

    void calculateAndSetPathscore(WorldPosition position) {
        //Add pathscore from startingposition to manhattan distance
        position.setPathScore(position.getPathScoreFromStartposition() + cityblockDistance(position, targetPosition));

    }

}
