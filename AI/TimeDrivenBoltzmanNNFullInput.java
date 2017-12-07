package AI;

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
 * Created by joseph on 27-5-2017.
 */
public class TimeDrivenBoltzmanNNFullInput extends AIHandler{

    public ActivationVectorList activationList;
    public int inputSize = 6; // all basic features

    public int TIME=200;
    public int localTime;

    static public int viewingRange = 2;
    public int amountOfInputs = 4;
    public String tostring = "";
    BufferedWriter writer = null;

    public TimeDrivenBoltzmanNNFullInput(GameWorld world, int manIndex, NNSettings setting, GameSettings gSet) {
        super(world, manIndex);
        if (setting.getWeigths().length != setting.getFunctions().length) {
            System.err.println("weights and functionlist unequal size");
            System.exit(-1);
        }
        mlp = new MLP(learningRate);

        this.setDiscount(setting.getDiscount());
        this.setLearningRate(setting.getLearningRate());
        this.setExplorationChance(setting.getExplorationRate());
        this.setGenerationSize(gSet.getAmountOfGenerations());
        this.setEpochSize(gSet.getAmountOfEpochs());

        localTime = TIME;

        //System.out.println("localTIme =" + localTime);


        viewingRange = world.getGridSize(); //only for whole grid
        inputSize = viewingRange * viewingRange * amountOfInputs; //same

        //small viewing grid
        //int range = viewingRange*2+1;
        //inputSize = range*range*3;


        ArrayList<AbstractActivationFunction> activationFunctionArrayList = mlp.CreateActivationFunctionList(setting.getFunctions());
        double[][][] initWeights = mlp.CreateWeights(setting.getWeigths(), inputSize);
        //create the activation vectorList (contains activation of every row, the weights, and the used functions
        activationList = new ActivationVectorList(initWeights, activationFunctionArrayList);

        SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd");
        Date localDate = new Date();
        tostring = dtf.format(localDate) + Arrays.toString(setting.getWeigths()) +", " + this.getClass() +  ", man"+ manIndex +"activation functions" + Arrays.toString(setting.getFunctions()) +"size" +getGenerationSize() + "," + getEpochSize()+"learningrate" + learningRate +", exploration" + explorationChance;

    }

    public ActivationVectorList getActivationList() {
        return activationList;
    }

//Seems to create and return a vector. Probably returns the current state of the game as a single vector, and is possibly unrelated to completing an actual game.
// n = gridsize. First n items in the vector are the type of tile (hardwall, softwall, open).
// The second n items are the dangerlevels.
// The third n items are enemy amounts on a tile. Final n items are indicating if the player is there.
    public double[] CompleteGame() {
        int worldSize = world.getGridSize();
        //int x = man.getX_location();
        //int y = man.getY_location();

        ArrayList<Double> gridList = new ArrayList<>();
        for (int xIdx = 0; xIdx < worldSize; xIdx++) {
            for (int yIdx = 0; yIdx < worldSize; yIdx++) {

                WorldPosition pos = world.getPositions(xIdx, yIdx); // Go through all grid locations
                gridList.add((double) pos.getType() - 1); // get the type of the location ||| Add for every position
                //-1 hardwall,0 softwall,1 path

                // how long until the bomb explodes?
                double danger = 0;
                if (pos.getBomb() != null) {
                    Bomb bomb = pos.getBomb();
                    danger = 1 - bomb.getCurrentTimer() / bomb.getMAXTIMER();
                    danger *= (bomb.getPlacedBy() == man ? -1 : 1);
                }
                gridList.add(danger); //adds dangerlevel to all positions in the world
                //is there an enemy bomberman on this position?
                double isEnemyBomberman = 0;
                int idx = 0;
                while ((isEnemyBomberman == 0) && idx < pos.getBombermanList().size()) {
                    BomberMan bombman = pos.getBombermanList().get(idx);
                    if (bombman != man) {
                        isEnemyBomberman = 1; // is enemy
                    }
                    idx++;
                }
                gridList.add(isEnemyBomberman); //adds the amount of enemies to all positions in the world
                double isPlayer = 0;
                idx = 0;
                while ((isPlayer == 0) && idx < pos.getBombermanList().size()) {
                    BomberMan bombman = pos.getBombermanList().get(idx);
                    if (bombman == man) {
                        isPlayer = 1; // is enemy
                    }
                    idx++;
                }
                gridList.add(isPlayer); //adds to each tile if the player is there or not

            }
        }

        double[] returnDouble = new double[gridList.size()];
        Iterator<Double> it = gridList.iterator();
        int i = 0;
        while (it.hasNext()) {
            returnDouble[i++] = it.next();
        }

        return returnDouble;
    }


    public ActivationVectorList CalculateBestMove() {
        activationList = mlp.forwardPass(CompleteGame(), activationList);
        return activationList;
    }

    // https://en.wikipedia.org/wiki/Softmax_function
    public int TimeBoltzMan(double[] Qvalues) {
        int move = 0;

        double[] Qclone = Qvalues.clone();
        Arrays.sort(Qclone);
        double maxQ = (Qclone[Qvalues.length - 1]); // makes it uniform

        //calculate the sum
        double Qsum = 0;
        int gensize = getGenerationError().size();
        double time = localTime * generationSize/(double) (gensize*gensize); //Math.pow(1.1,generationSize) / Math.pow(1.1,getGenerationError().size());

        double logBeta = Math.max(0,Math.log(getGenerationError().size() * getEpochError().size()/Math.pow(2,error.size())));

        double timeBeta = 1/(double)(generationSize - gensize + 1);
        for (double val : Qvalues) {
            Qsum += Math.exp((val-maxQ)*timeBeta);
        }
        //System.out.println("timeBeta" + timeBeta);
       // System.out.println("logBeta" + logBeta);
       // System.out.println("time" + time);

        //transform Qvalues into probability array
        double[] probabilitys = new double[Qvalues.length];
        for (int idx = 0; idx < Qvalues.length; idx++) {
            probabilitys[idx] = Math.exp((Qvalues[idx]-maxQ)*timeBeta) / Qsum;
        }

        //transform probabilitys into cumulative sum array;
        double cumsum = 0;
        double[] cumsumprobability = new double[Qvalues.length];
        for (int idx = 0; idx < Qvalues.length; idx++) {
            cumsum += probabilitys[idx] * 100;
            cumsumprobability[idx] = cumsum;
        }

        //choose a move
        double randomValue = rnd.nextDouble() * 100;

        for (int idx = 0; idx < Qvalues.length; idx++) {
            if (randomValue < cumsumprobability[idx]) return idx;
        }
        return move;

    }

    public void AddMoveToBuffer() {
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
        if (!testing && random<explorationChance) {
            move = TimeBoltzMan(output);
            if (PRINT) System.out.println("random!");
        }
        moves.add(move);// add the move to the move list

    }

    //Gets called by the gameloop
    public void UpdateWeights() {

        double[] targets = activationList.getOutput(); // get the expected Q-values
        double[] targetforError = targets.clone(); //make a copy
        if (PRINT) System.out.println("expected" + Arrays.toString(targets));

        int idx = man.getPoints().size() - 1; // get index of array location latest points received of agent
        double outcome = man.getPoints().get(idx); //save this point value
        if (PRINT) System.out.println("reward:" + outcome);

        //calculate max q value
        if (man.getAlive()) {
            ActivationVectorList secondpasslist = mlp.forwardPass(CompleteGame(), activationList); //do a forwardspass with the current gamestate and the network of this object. Save the result.
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
        error.add(mlp.TotalError(targets, targetforError)); //calculate the total error and add it to the array
        activationList = mlp.BackwardPass(activationList, targets); //update the weights using the new target
        if (PRINT) System.out.println();
    }

    public String toString() {
        return tostring;
    }

    @Override
    public ActivationVectorList getActivationVectorlist() {
        return activationList;
    }

    public void setActivationList(ActivationVectorList activationList){
        this.activationList = activationList;
    }

}
