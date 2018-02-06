package AI;

import GameWorld.GameWorld;
import GameWorld.WorldPosition;
import MLP.AbstractActivationFunction;
import MLP.ActivationVectorList;
import util.GameSettings;
import util.NNSettings;

import java.io.Serializable;
import java.util.*;

import static org.apache.commons.math3.util.FastMath.abs;

/**
 * Created by Remco on 21-10-2017.
 */

public class HierarchicalAIMaxBoltzman extends ErrorDrivenBoltzmanNNFullInput implements Serializable {
    private boolean DEBUG = false;
    private boolean DEBUG_PRINT_ENEMYCOUNT = false;
    private boolean DEBUG_PRINT_FOUND_PATH = false;

    private boolean SPECIALIZED_NETWORKS_FOR_AMOUNT_OF_ENEMIES = true;
    private boolean USE_SINGLE_NETWORK = false;

    WorldPosition targetPosition;

    double startingTemperature = 200;
    double currentTemperature;

    //Class of these networks are not important
    public ErrorDrivenBoltzmanNNFullInput pathFindingNetwork;
    public ErrorDrivenBoltzmanNNFullInput oneEnemyNetwork;
    public ErrorDrivenBoltzmanNNFullInput twoEnemiesNetwork;
    public ErrorDrivenBoltzmanNNFullInput threeEnemiesNetwork;

    private int currentStrategy = 0; //0 = pathfinding, 1 = one enemy, etc

    public int DEATH_REWARD_PATHFINDING = -300;
    public int KILL_REWARD_PATHFINDING = 0;
    public int WALL_REWARD_PATHFINDING = 30;
    public int MOVE_REWARD_PATHFINDING = -1;

    public int DEATH_REWARD_ATTACKING = -300;
    public int KILL_REWARD_ATTACKING = 100;
    public int WALL_REWARD_ATTACKING = 0;
    public int MOVE_REWARD_ATTACKING = -1;


    public ActivationVectorList activationList;



    public HierarchicalAIMaxBoltzman(GameWorld world, int manIndex, NNSettings setting, GameSettings gSet) {


        super(world, manIndex, setting, gSet);
        this.world = world;

        pathFindingNetwork = new ErrorDrivenBoltzmanNNFullInput(world, manIndex, setting, gSet);


        oneEnemyNetwork = new ErrorDrivenBoltzmanNNFullInput(world, manIndex, setting, gSet);

        if(world.getAmountPlayers() >= 3){
            twoEnemiesNetwork = new ErrorDrivenBoltzmanNNFullInput(world, manIndex, setting, gSet);
        }

        if(world.getAmountPlayers() >= 4){
            threeEnemiesNetwork = new ErrorDrivenBoltzmanNNFullInput(world, manIndex, setting, gSet);
        }

        ArrayList<AbstractActivationFunction> activationFunctionArrayList = mlp.CreateActivationFunctionList(setting.getFunctions());
        double[][][] initWeights = mlp.CreateWeights(setting.getWeigths(), inputSize);
        String name = "Hoi";
        activationList = new ActivationVectorList(initWeights, activationFunctionArrayList, name);

        //Set temperature
        currentTemperature = startingTemperature;
    }

    public void AddMoveToBuffer() {
        int move;
        int targetableEnemyCount;

        List targetEnemies = checkPathToEnemies();
        targetableEnemyCount = targetEnemies.size(); //targetable enemies

        if(!USE_SINGLE_NETWORK) {
            if (targetableEnemyCount > 0) { // Second strategy: Attacking
                if (DEBUG_PRINT_ENEMYCOUNT)
                    System.out.println("Second strategy: Attacking. Amount of targets: " + targetableEnemyCount);
                if (DEBUG_PRINT_ENEMYCOUNT)
                    System.out.println("Enemies alive: " + (world.getAmountOfAlivePlayers() - 1));
                //change rewards to rewards for this strat
                currentStrategy = targetableEnemyCount;
                changeStrategyRewards(2); //Rewards for attacking strat
                move = calculateMove();

            } else {
                if (DEBUG) System.out.println("First strategy: Pathfinding");
                //change rewards to rewards for this strat
                currentStrategy = targetableEnemyCount;
                changeStrategyRewards(1); //Rewards for pathfinding strat
                move = calculateMove();

            }
        }
        else{
            currentStrategy = 0; //force single network
            changeStrategyRewards(-1);
            move = calculateMove();
        }
        //add to movebuffer
        moves.add(move);// add the move to the move list

    }

    int calculateMove() {
        int move = 0;
        ErrorDrivenBoltzmanNNFullInput network = getCorrectNetworkForStrategy();

        //Forwardpass
        network.activationList = mlp.forwardPass(CompleteGame(), activationList);

        //Greedy option
        double[] output = network.CalculateBestMove().getOutput(); //get the outputlayer of the network
        double maxOutcome = Double.NEGATIVE_INFINITY;
        for (int idx = 0; idx < output.length; idx++) { //finds highest value
            if (maxOutcome < output[idx]) {
                move = idx;
                maxOutcome = output[idx];
            }
        }

        /* Exploration strategy */
        double randomValue = new Random().nextDouble() % 1;

        if (randomValue < explorationChance && !testing) {
            //Boltzmann distribution option
            move = boltzmannMove(output);

        }
        if (PRINT) System.out.println("move:" + move);
        return move;
    }

    //Receives the Qvalues of the current state
    //Returns a move according to the Boltzmann distribution
    public int boltzmannMove(double[] qValues) {
        int move = 0;
        double[] probabilities = new double[5];
        double numerator;
        int gensize = getGenerationError().size();
        currentTemperature = startingTemperature - startingTemperature * (gensize/generationSize) + 1; //+1 to have a minimum

        //Calculate denominator first
        double denominator = 0;
        for (int i = 0; i < 6; i++) { //6 possible actions
            denominator += Math.exp(qValues[i] / currentTemperature);
        }

        for (int i = 0; i < 6; i++) { //6 possible actions
            numerator = Math.exp(qValues[i] / currentTemperature);
            //Calculate probabilities for all actions
            probabilities[i] = numerator / denominator;
        }

        //accumulate the probabilities, so they can be used in a range when generating a value in [0,1]
        double[] accumulatedProbabilities = new double[5];
        accumulatedProbabilities[0] = probabilities[0]; //first case is outside of the loop
        for (int i = 1; i < 6; i++) {
            accumulatedProbabilities[i] = accumulatedProbabilities[i - 1] + probabilities[i];
        }

        //Generate a value between 0 and 1.
        double randomValue = new Random().nextDouble() % 1;
        double lowerBound = 0;
        double upperBound;
        for (int i = 0; i < 6; i++) {
            if (i != 0) {
                lowerBound = accumulatedProbabilities[i - 1];
            }
            upperBound = accumulatedProbabilities[i];
            if (randomValue > lowerBound && randomValue <= upperBound) { //randomValue is in range of our probabilities
                move = i;
            }
        }
        return move;
    }

    /*
        //Return the index containing the highest value of an array
        public int maxIndexValueArray(double[] array) {
            double maxValue = Double.NEGATIVE_INFINITY;
            int index = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] > maxValue) {
                    maxValue = array[i];
                    index = i;
                }
            }
            return index;
        }
    */
    //doesn't change the values for already active bombs. Shouldn't be a problem. A kill caused during the pathfinding strategy is a mere accident. //TODO change rewards all active bombs
    //Changes the rewards to the other set
    void changeStrategyRewards(int strategyNumber){
        if(strategyNumber == 1) { //Pathfinding
            //world.Bomb.setDIECOST(DEATH_REWARD_PATHFINDING);
            man.setKillReward(world.KILL_REWARD_PATHFINDING);
            man.setWallReward(world.WALL_REWARD_PATHFINDING);
            //man.setMOVECOST(MOVE_REWARD_PATHFINDING);
        }

        if(strategyNumber == 2) { //Attacking
            //world.Bomb.setDIECOST(DEATH_REWARD_ATTACKING);
            man.setKillReward(world.KILL_REWARD_ATTACKING);
            man.setWallReward(world.WALL_REWARD_ATTACKING);
            //man.setMOVECOST(MOVE_REWARD_ATTACKING);
        }

        if(strategyNumber == -1) { //Single network
            //world.Bomb.setDIECOST(DEATH_REWARD_ATTACKING);
            man.setKillReward(world.KILL_REWARD_ATTACKING);
            man.setWallReward(world.WALL_REWARD_PATHFINDING);
            //man.setMOVECOST(MOVE_REWARD_ATTACKING);
        }
    }

    List checkPathToEnemies() { //returns ID of enemy to which a path is possible.
        List<Integer> returnList = new ArrayList<>();
        ArrayList<AIHandler> listOfEnemies = world.getAi();

        //go through all enemies
        for (int x = 1; x < listOfEnemies.size(); x++) { //offset by one so our own agent is ignored
            //don't  look for dead enemies
            if(listOfEnemies.get(x).man.getAlive()) {

                //get the X and Y of the enemy
                WorldPosition enemyLocation = world.getPositions(listOfEnemies.get(x).getMan().getX_location(), listOfEnemies.get(x).getMan().getY_location());

                //search for paths using aStar
                returnList.add(aStar(enemyLocation));
                //list can contain negative values that have to be filtered
            }
        }

        /**
        //filter duplicates
        Set<Integer> hashSet = new HashSet<>();
        hashSet.addAll(returnList); //add all to the hashset
        returnList.clear(); //empty the returnlist
        returnList.addAll(hashSet); //fill it again, now without any duplicates
         **/

        //go through the list to see if -2 is returned for some of the agents, remove those elements from the list
        for(int i = 0; i < returnList.size(); i++ ){
            if(returnList.get(i) < -1){
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

    //TODO AStar doesn't work perfectly all the time. Sometimes multiple accessible enemies are not detected. Might have something to do with the distance of the undetected enemy.
    //TODO accept a list of locations instead of a single one
    //One issue is that enemies move during the pathfinding. This means a path can be found to an old location with no enemy on it anymore.
    //This isn't really a concern: if there was a path to a certain enemy, it will remain for the rest of the game
    //Receives the position of an enemy as argument.
    int aStar(WorldPosition targetPosition) { //returns ID of enemy to which a path is found



        this.targetPosition = targetPosition;
        /** int targetID; **/
        boolean skipSuccessor = false;

        /**
        if (targetPosition.getBombermanList().isEmpty()) {
            targetID = -1; //position given has no enemy anymore at this point
        } else {
            targetID = targetPosition.getBombermanList().get(0).getId(); //get the ID of the enemy we are finding a path to
        }
         **/


        ArrayList<WorldPosition> openList = new ArrayList<>(); //locations that are being considered
        ArrayList<WorldPosition> closedList = new ArrayList<>(); //locations that do not have to be considered

        WorldPosition startingPosition = world.getPositions(man.getX_location(), man.getY_location()); // set our startingposition. This object is used to compare pathlenghts.
        //calculateAndSetPathscore(startingPosition); //set its score, which should be 0
        startingPosition.setPathScore(0);
        //add our starting position to the open list
        openList.add(startingPosition);


        /**
        //add possible locations around agent to the openlist
        addSurroundingLocations(openList, world.getPositions(man.getX_location(), man.getY_location())); //addSurroundingLocations() doesn't add locations that are inaccessible
         **/

        /**
        if (openList.isEmpty()) {
            return -3; //something went wrong with the open list //doesnt seem to occur anymore. Leaving it in just in case...
        }
         **/

        /**
        WorldPosition positionConsidering = openList.get(0); //take first item
**/

        //loop until we found our targetPosition, our until we run out of positions in the openlist
        while (!openList.isEmpty()) {

            //Sort the resulting list from lowest pathscore to highest
            sortListByPathScore(openList);

            //take first object in the open list and move it to the closed list
            WorldPosition positionConsidering = openList.get(0); //take first item
            openList.remove(0); //remove it

            ArrayList<WorldPosition> consideringSuccessorsList = new ArrayList<>(); //for locations that are adjacent to current position
            addSurroundingLocations(consideringSuccessorsList, positionConsidering); //fills the first argument (arraylist) with possible locations around the second argument.

            //Process all successor positions
            for(WorldPosition successorPositionConsidering : consideringSuccessorsList ) {

                //reset boolean for skipping current successor
                skipSuccessor = false;


                //x and y coordinates correspond to each other, we found our target.
                if (targetPosition.getX_location() == successorPositionConsidering.getX_location() && targetPosition.getY_location() == successorPositionConsidering.getY_location()) {
                    if (DEBUG_PRINT_FOUND_PATH) {
                        System.out.println("Path from " + man.getX_location() + "," + man.getY_location() +
                                " to " + targetPosition.getX_location() + "," + targetPosition.getY_location() + " (enemy " +  ")");
                    }
                    return 1; //found the target
                }

                //update pathscores
                calculateAndSetPathscore(successorPositionConsidering, positionConsidering);

                /**
                 if (closedList.contains(successorPositionConsidering)) {
                 break; //ignore positions that are in the closed list //TODO
                 }
                 **/

                /**
                 if (!openList.contains(successorPositionConsidering)) { //not in the open list
                 //calculate pathScore
                 calculateAndSetPathscore(successorPositionConsidering);
                 openList.add(successorPositionConsidering); //add location to open list
                 }
                 **/

                //check if this node is already in the open list
                for (WorldPosition position : openList) {
                    //check if already in the list
                    if (position.getX_location() == successorPositionConsidering.getX_location() && position.getY_location() == successorPositionConsidering.getY_location()) {
                        //Pathscore of position already in the list is better
                        if (position.getPathScore() <= successorPositionConsidering.getPathScore()) {
                            skipSuccessor = true;
                        }
                    }
                }

                //check if this node is already in the closed list
                for (WorldPosition position : closedList) {
                    //check if already in the list
                    if (position.getX_location() == successorPositionConsidering.getX_location() && position.getY_location() == successorPositionConsidering.getY_location()) {
                        //Pathscore of position already in the list is better
                        if (position.getPathScore() <= successorPositionConsidering.getPathScore()) {
                            skipSuccessor = true;

                        }
                    }
                }

                //Add position to open list, if position is not being skipped
                if (!skipSuccessor) {
                    openList.add(successorPositionConsidering);
                }

                /**
                 if (openList.contains(successorPositionConsidering)) { //already in open list
                 //node already in the openList has a lower pathscore
                     if (previousPosition.getPathScore() < successorPositionConsidering.getPathScore() ) {
                     //update its score and update its parent as well.
                     calculateAndSetPathscore(successorPositionConsidering);
                     calculateAndSetPathscore(previousPosition);

                     }

                     }
                     **/

                    /**
                     //set previous considered position to variable
                     previousPosition = tempPosition;
                     **/



                }
            closedList.add(positionConsidering); // add to closed list
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

    void calculateAndSetPathscore(WorldPosition position, WorldPosition parent) {
        /**
        //Add pathscore from startingposition to manhattan distance
        position.setPathScore(position.getPathScoreFromStartposition() + cityblockDistance(position, targetPosition));
         **/

        position.movementCostSoFor = parent.movementCostSoFor + cityblockDistance(position, parent);
        position.estimationRemainingPathscore = cityblockDistance(position, targetPosition);
        position.setPathScore(position.movementCostSoFor + position.estimationRemainingPathscore);

    }

    int getCurrentStrategy(){
        return currentStrategy;
    }

    public ArrayList<ErrorDrivenBoltzmanNNFullInput> getAllNetworks(){

        ArrayList<ErrorDrivenBoltzmanNNFullInput> returnList = new ArrayList<>() ;
        returnList.add(pathFindingNetwork);
        returnList.add(oneEnemyNetwork);
        returnList.add(twoEnemiesNetwork);
        returnList.add(threeEnemiesNetwork);

        return returnList;
    }

/**
    public ActivationVectorList CalculateBestMove(MLP mlp) {
        activationList = mlp.forwardPass(CompleteGame(), activationList);
        return activationList;
    }
**/


    public void UpdateWeights(){

        //ActivationVectorList activationList;
        //select correct network
        ErrorDrivenBoltzmanNNFullInput correctNetwork = getCorrectNetworkForStrategy();
        activationList = correctNetwork.getActivationList(); //get the network of the right strategy
        //filthy hack //todo remove filthy hack
        activationList.setNetworkName("Strategy "+currentStrategy);
        /*
        //debug
        if(currentStrategy > 0){
            if (DEBUG) System.out.println("Attacking");
        }
        */
        double[] targets = activationList.getOutput(); // get the expected Q-values
        double[] targetforError = targets.clone(); //make a copy
        if (PRINT) System.out.println("expected" + Arrays.toString(targets));

        int idx = man.getPoints().size() - 1; // get index of array location latest points received of agent
        double outcome = man.getPoints().get(idx); //save this point value
        if (PRINT) System.out.println("reward:" + outcome);

        //calculate max q value
        if (man.getAlive()) {
            ActivationVectorList secondpasslist = mlp.forwardPass(CompleteGame(), activationList); //do a forwardpass with the current gamestate and the network of this object. Save the result.
            double[] secondpass = secondpasslist.getOutput(); // get the values of the output layer
            double maxOutcome = Double.NEGATIVE_INFINITY;
            for (int moveidx = 0; moveidx < secondpass.length; moveidx++) { // go through all output values
                if (maxOutcome < secondpass[moveidx]) { //sets the value of the output node to negative infinity if it was somehow lower.
                    maxOutcome = secondpass[moveidx];
                }
            }
            if (PRINT) System.out.println("second pass:" + discount * maxOutcome);
            outcome += discount * maxOutcome; //add the Q-value after compensating for the discount
        }

        targets[getLastMove()] = outcome; //change the target value of the last move made to the outcome

        if (PRINT) System.out.println("outcome:" + outcome);
        if (PRINT) System.out.println("output" + Arrays.toString(targets) + "\n\n");
        //if(currentStrategy > 0) {
            correctNetwork.error.add(mlp.TotalError(targets, targetforError)); //calculate the total error and add it to the array
        //}else{
        //    error.add(mlp.TotalError(targets, targetforError)); //calculate the total error and add it to the array
        //}

        activationList = mlp.BackwardPass(activationList, targets); //update the weights using the new target
        correctNetwork.setActivationList(activationList);

    }

    ErrorDrivenBoltzmanNNFullInput getCorrectNetworkForStrategy() {
        if (SPECIALIZED_NETWORKS_FOR_AMOUNT_OF_ENEMIES) { //global variable to decide if we use specialized networks for each amount of enemies
            //switch to determine the use of the right network
            switch (currentStrategy) {
                case 0:
                    return this.pathFindingNetwork;
                case 1:
                    return this.oneEnemyNetwork;
                case 2:
                    return this.twoEnemiesNetwork;
                case 3:
                    return this.threeEnemiesNetwork;
            }
        } else { //use only two networks: pathfinding and fight
            if (currentStrategy > 0) {
                return this.oneEnemyNetwork;
            } else {
                return this.pathFindingNetwork;
            }

        }

        return this;

    }

    public ArrayList<Double> getGenerationErrorNetwork1() {
        return oneEnemyNetwork.getGenerationError();
    }
    public ArrayList<Double> getGenerationErrorNetwork2() {
        return twoEnemiesNetwork.getGenerationError();
    }
    public ArrayList<Double> getGenerationErrorNetwork3() {
        return threeEnemiesNetwork.getGenerationError();
    }

    public ErrorDrivenBoltzmanNNFullInput getOneEnemyNetwork() {
        return oneEnemyNetwork;
    }

    public ErrorDrivenBoltzmanNNFullInput getTwoEnemiesNetwork() {
        return twoEnemiesNetwork;
    }

    public ErrorDrivenBoltzmanNNFullInput getThreeEnemiesNetwork() {
        return threeEnemiesNetwork;
    }

    public void EndOfRound(double win) {

        ErrorDrivenBoltzmanNNFullInput network = pathFindingNetwork;

        //account for the four networks
        for (int i = 0; i <= 3; i++) {
            switch (i) {
                case 0:
                    network = pathFindingNetwork;
                    break;
                case 1:
                    network = oneEnemyNetwork;
                    break;
                case 2:
                    network = twoEnemiesNetwork;
                    break;
                case 3:
                    network = threeEnemiesNetwork;
                    break;
            }

            ArrayList<Double> winrate = network.getWinrate();
            ArrayList<Double> epochError = network.getEpochError();
            ArrayList<Integer> epochPoints = network.getEpochPoints();

            if (testing) {
                win += winrate.get(winrate.size() - 1);
                winrate.set(winrate.size() - 1, win);
                int tempPoints = 0;
                for (double temp : man.getPoints()) tempPoints += temp; //all networks correspond to the same agent
                epochPoints.add(tempPoints);
            }
            double tempError = 0;
            for (double temp : network.getError()) tempError += temp;
            epochError.add(tempError / (double) network.getError().size());
            network.setError(new ArrayList<>());
            //error = new ArrayList<>();
        }

    }

    public void newGeneration() {

        ErrorDrivenBoltzmanNNFullInput network = pathFindingNetwork;

        //account for the four networks
        for (int i = 0; i <= 3; i++) {
            switch (i) {
                case 0:
                    network = pathFindingNetwork;
                    break;
                case 1:
                    network = oneEnemyNetwork;
                    break;
                case 2:
                    network = twoEnemiesNetwork;
                    break;
                case 3:
                    network = threeEnemiesNetwork;
                    break;
            }

            ArrayList<Double> winrate = network.getWinrate();
            ArrayList<Double> epochError = network.getEpochError();
            ArrayList<Integer> epochPoints = network.getEpochPoints();
            ArrayList<Double> generationError = network.getGenerationError();
            ArrayList<Double>  generationPoints = network.getGenerationPoints();

            double tempError = 0;
            int correctedEpochSize = epochError.size();
            for (Double temp : epochError){
                if(!temp.isNaN()) {//only add the value if it is a number
                    tempError += temp;
                }else{
                    correctedEpochSize--; //decrease epoch size to correct for amount of values.
                }

            }
            generationError.add(tempError / correctedEpochSize);

            Double tempPoints = 0.0;
            for (double temp : epochPoints) tempPoints += temp;
            generationPoints.add(tempPoints / epochPoints.size());

            int wsize = winrate.size() - 1;
            winrate.set(wsize, (winrate.get(wsize) / (double) (2 * epochPoints.size())) + 0.5);
            winrate.add(0.0);

            network.setEpochPoints(new ArrayList<>());
            network.setEpochError(new ArrayList<>());
        }
    }

    @Override
    public ArrayList<Double> getGenerationError() {
        return pathFindingNetwork.getGenerationError();
    }

    @Override
    public ArrayList<Double> getGenerationPoints() {
        return pathFindingNetwork.getGenerationPoints();
    }

    @Override
    public ArrayList<Double> getWinrate() {
        return pathFindingNetwork.getWinrate();
    }

    @Override
    public void setNewBomberman() {
        error = new ArrayList<>();
        moves = new ArrayList<>();
        man = world.getBomberManList().get(manIndex);
        pathFindingNetwork.setMan(man);
        oneEnemyNetwork.setMan(man);
        if(world.getAmountPlayers() >= 3) {
            twoEnemiesNetwork.setMan(man);
        }
        if(world.getAmountPlayers() >= 4) {
            threeEnemiesNetwork.setMan(man);
        }




    }

}
