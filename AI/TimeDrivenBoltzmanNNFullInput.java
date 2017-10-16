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

    private ActivationVectorList activationList;
    private int inputSize = 6; // all basic features

    private int TIME=200;
    private int localTime;

    static private int viewingRange = 2;
    private int amountOfInputs = 4;
    private String tostring = "";
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

        System.out.println("localTIme =" + localTime);


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


    public double[] CompleteGame() {
        int worldSize = world.getGridSize();
        int x = man.getX_location();
        int y = man.getY_location();

        ArrayList<Double> gridList = new ArrayList<>();
        for (int xIdx = 0; xIdx < worldSize; xIdx++) {
            for (int yIdx = 0; yIdx < worldSize; yIdx++) {

                WorldPosition pos = world.getPositions(xIdx, yIdx);
                gridList.add((double) pos.getType() - 1); // get the type of the location
                //-1 hardwall,0 softwall,1 path

                // how long until the bomb explodes?
                double danger = 0;
                if (pos.getBomb() != null) {
                    Bomb bomb = pos.getBomb();
                    danger = 1 - bomb.getCurrentTimer() / bomb.getMAXTIMER();
                    danger *= (bomb.getPlacedBy() == man ? -1 : 1);
                }
                gridList.add(danger);
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
                gridList.add(isEnemyBomberman);
                double isPlayer = 0;
                idx = 0;
                while ((isPlayer == 0) && idx < pos.getBombermanList().size()) {
                    BomberMan bombman = pos.getBombermanList().get(idx);
                    if (bombman == man) {
                        isPlayer = 1; // is enemy
                    }
                    idx++;
                }
                gridList.add(isPlayer);

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


    private ActivationVectorList CalculateBestMove() {
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


    public void UpdateWeights() {

        double[] targets = activationList.getOutput(); // get the expected Q-values
        double[] targetforError = targets.clone();
        if (PRINT) System.out.println("expected" + Arrays.toString(targets));

        int idx = man.getPoints().size() - 1;
        double outcome = man.getPoints().get(idx);
        if (PRINT) System.out.println("reward:" + outcome);
        //calculate max q value
        if (man.getAlive()) {
            ActivationVectorList secondpasslist = mlp.forwardPass(CompleteGame(), activationList);
            double[] secondpass = secondpasslist.getOutput();
            double maxOutcome = Double.NEGATIVE_INFINITY;
            for (int moveidx = 0; moveidx < secondpass.length; moveidx++) {
                if (maxOutcome < secondpass[moveidx]) {
                    maxOutcome = secondpass[moveidx];
                }
            }
            if (PRINT) System.out.println("second pass:" + discount * maxOutcome);
            outcome += discount * maxOutcome;
        }

        targets[getLastMove()] = outcome;

        if (PRINT) System.out.println("outcome:" + outcome);
        if (PRINT) System.out.println("output" + Arrays.toString(targets) + "\n\n");
        error.add(mlp.TotalError(targets, targetforError));
        activationList = mlp.BackwardPass(activationList, targets);
    }

    public String toString() {
        return tostring;
    }

    @Override
    public ActivationVectorList getActivationVectorlist() {
        return activationList;
    }

}
