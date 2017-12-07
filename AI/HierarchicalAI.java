package AI;

import GameWorld.*;
import MLP.AbstractActivationFunction;
import util.GameSettings;
import util.NNSettings;

import java.util.*;

import MLP.ActivationVectorList;

import static org.apache.commons.math3.util.FastMath.abs;


/**
 * Created by Remco on 21-10-2017.
 */

public class HierarchicalAI extends TimeDrivenBoltzmanNNFullInput {
    private boolean DEBUG = true;
    private boolean SPECIALIZED_NETWORKS_FOR_AMOUNT_OF_ENEMIES = false;
    WorldPosition targetPosition;
    //protected MLP mlp2;
    TimeDrivenBoltzmanNNFullInput oneEnemyNetwork;
    TimeDrivenBoltzmanNNFullInput twoEnemiesNetwork;
    TimeDrivenBoltzmanNNFullInput threeEnemiesNetwork;

    private int currentStrategy = 0; //0 = pathfinding, 1 = one enemy, etc

    public ActivationVectorList activationList;



    public HierarchicalAI(GameWorld world, int manIndex, NNSettings setting, GameSettings gSet) {


        super(world, manIndex, setting, gSet);
        this.world = world;

        oneEnemyNetwork = new TimeDrivenBoltzmanNNFullInput(world, manIndex, setting, gSet);

        if(world.getAmountPlayers() >= 3){
            twoEnemiesNetwork = new TimeDrivenBoltzmanNNFullInput(world, manIndex, setting, gSet);

        }

        if(world.getAmountPlayers() >= 4){
            threeEnemiesNetwork = new TimeDrivenBoltzmanNNFullInput(world, manIndex, setting, gSet);
        }

        ArrayList<AbstractActivationFunction> activationFunctionArrayList = mlp.CreateActivationFunctionList(setting.getFunctions());
        double[][][] initWeights = mlp.CreateWeights(setting.getWeigths(), inputSize);
        String name = "Hoi";
        activationList = new ActivationVectorList(initWeights, activationFunctionArrayList, name);

        if (DEBUG) System.out.println("Using hierachical");
    }

    public void AddMoveToBuffer() {
        List targetEnemies = checkPathToEnemies();
        int move;
        int enemyCount;

        enemyCount = targetEnemies.size();

        if (enemyCount > 0) { // Second strategy: Attacking
            if (DEBUG) System.out.println("Second strategy: Attacking. Amount of targets: " + enemyCount);
            //change rewards to rewards for this strat
            currentStrategy = enemyCount;
            changeStrategyRewards(2); //Rewards for attacking strat
            move = calculateMove();

        } else {
            if (DEBUG) System.out.println("First strategy: Pathfinding");
            //change rewards to rewards for this strat
            currentStrategy = enemyCount;
            changeStrategyRewards(1); //Rewards for pathfinding strat
            move = calculateMove();

        }
        //add to movebuffer
        moves.add(move);// add the move to the move list

    }

    int calculateMove() {
        int move = 0;
        TimeDrivenBoltzmanNNFullInput network = getCorrectNetworkForStrategy();

        //Forwardpass
        network.activationList = mlp.forwardPass(CompleteGame(), activationList);

        double[] output = network.CalculateBestMove().getOutput(); //get the outputlayer of the network
        double maxOutcome = Double.NEGATIVE_INFINITY;
        for (int idx = 0; idx < output.length; idx++) { //finds highest value
            if (maxOutcome < output[idx]) {
                move = idx;
                maxOutcome = output[idx];
            }
        }
        double random = rnd.nextDouble();
        if (!network.testing && random < network.explorationChance) {
            move = network.TimeBoltzMan(output); //make a "random" move instead
            if (PRINT) System.out.println("random!");
        }
        return move;
    }

    //doesn't change the values for already active bombs. Shouldn't be a problem. A kill caused during the pathfinding strategy is a mere accident.
    //Changes the rewards to the other set
    void changeStrategyRewards(int strategyNumber){
        if(strategyNumber == 1) { //Pathfinding
            //Bomb.setDIECOST(-300); redundant
            man.setKillReward(0);
            man.setWallReward(30);
            //BomberMan.setMOVECOST(-1); redundant
        }

        if(strategyNumber == 2) { //Attacking
            //Bomb.setDIECOST(-300); redundant
            man.setKillReward(100);
            man.setWallReward(0);
            //BomberMan.setMOVECOST(-1); redundant
        }
    }

    List checkPathToEnemies() { //returns ID of enemy to which a path is possible.
        List<Integer> returnList = new ArrayList<>();
        ArrayList<AIHandler> listOfEnemies = world.getAi();

        //go through all enemies
        for (int x = 1; x < listOfEnemies.size(); x++) { //offset by one so our own agent is ignored
            //get the X and Y of the enemy
            WorldPosition enemyLocation = world.getPositions(listOfEnemies.get(x).getMan().getX_location(), listOfEnemies.get(x).getMan().getY_location());

            //search for paths using aStar
            returnList.add(aStar(enemyLocation));
            //list can contain negative values that have to be filtered
        }

        //filter duplicates
        Set<Integer> hashSet = new HashSet<>();
        hashSet.addAll(returnList); //add all to the hashset
        returnList.clear(); //empty the returnlist
        returnList.addAll(hashSet); //fill it again, now without any duplicates

        //go through the list to see if -2 is returned for some of the agents, remove those elements from the list
        for(int i = 0; i < returnList.size(); i++ ){
            if(returnList.get(i) < 0){
                returnList.remove(i);
                i--; // to offset the fact that the list is now one element shorter
            }

        }

        return returnList;
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

    //TODO AStar doesn't work perfectly all the time. Sometimes accessible enemies are not detected.
    //One issue is that enemies move during the pathfinding. This means a path can be found to an old location with no enemy on it anymore.
    //Receives the position of an enemy as argument.
    int aStar(WorldPosition targetPosition) { //returns ID of enemy to which a path is found
        this.targetPosition = targetPosition;
        int targetID;
        if (targetPosition.getBombermanList().isEmpty()) {
            targetID = -1; //position given has no enemy at this point
        } else {
            targetID = targetPosition.getBombermanList().get(0).getId(); //get the ID of the enemy we are finding a path to
        }
        WorldPosition previousPosition = world.getPositions(man.getX_location(), man.getY_location()); // set our startingposition as the previous position. This object is used to compare pathlenghts.
        calculateAndSetPathscore(previousPosition); //set its score, which should be 0

        ArrayList<WorldPosition> openList = new ArrayList<>(); //locations that are being considered
        ArrayList<WorldPosition> closedList = new ArrayList<>(); //locations that do not have to be considered

        //add possible locations around agent to the openlist

        addSurroundingLocations(openList, world.getPositions(man.getX_location(), man.getY_location())); //addSurroundingLocations() doesn't add locations that are inaccessible

        if(openList.isEmpty()){
            return -3; //something went wrong with the open list
        }

        WorldPosition positionConsidering = openList.get(0); //take first item


        //loop until we found our targetPosition, our until we run out of positions in the openlist
        while (!openList.isEmpty()) {
            if (targetPosition.getX_location() == positionConsidering.getX_location() && targetPosition.getY_location() == positionConsidering.getY_location()) {
                if (DEBUG)
                    System.out.println("Path from " + man.getX_location() + "," + man.getY_location() + " to " + targetPosition.getX_location() + "," + targetPosition.getY_location() + " (enemy " + targetID + ")");
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
        return -2; //case of no enemy

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

    int getCurrentStrategy(){
        return currentStrategy;
    }

/**
    public ActivationVectorList CalculateBestMove(MLP mlp) {
        activationList = mlp.forwardPass(CompleteGame(), activationList);
        return activationList;
    }
**/


    public void UpdateWeights(){

        //select correct network
        activationList = getCorrectNetworkForStrategy().getActivationList(); //get the network of the right strategy
        //filthy hack //todo remove filthy hack
        activationList.setNetworkName("Strategy "+currentStrategy);
        //debug
        if(currentStrategy > 0){
            if (DEBUG) System.out.println("Attacking");
        }
        super.UpdateWeights();
    }

    TimeDrivenBoltzmanNNFullInput getCorrectNetworkForStrategy(){
        if(SPECIALIZED_NETWORKS_FOR_AMOUNT_OF_ENEMIES) { //global variable to decide if we use specialized networks for each amount of enemies
            //switch to determine the use of the right network
            switch (currentStrategy) {
                case 0:
                    return this;
                case 1:
                    return this.oneEnemyNetwork;
                case 2:
                    return this.twoEnemiesNetwork;
                case 3:
                    return this.threeEnemiesNetwork;
            }
        }else{ //use only two networks: pathfinding and fight
            if(currentStrategy > 0){
                return this.oneEnemyNetwork;
            }
            else{
                return this;
            }

        }

        return this;

    }

}
