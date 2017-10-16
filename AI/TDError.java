package AI;


import GameWorld.*;
import MLP.AbstractActivationFunction;
import MLP.ActivationVectorList;
import MLP.MLP;
import util.NNSettings;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;

import java.util.*;

import static java.lang.Double.doubleToLongBits;
import static java.lang.Double.max;

/**
 * Created by Joseph on 6/8/2017.
 */
public class TDError extends AIHandler {
    private ActivationVectorList activationList;
    private int inputSize = 6; // all basic features


    static private int viewingRange = 2;
    private int amountOfInputs = 4;
    private String tostring = "";

    public TDError(GameWorld world, int manIndex, int[] hiddenNodesLayers, int[] activationFunctionList, NNSettings nnset) {
        super(world, manIndex);
        if (hiddenNodesLayers.length != activationFunctionList.length) {
            System.err.println("weights and functionlist unequal size");
            System.exit(-1);
        }

        viewingRange = world.getGridSize(); //only for whole grid
        inputSize = viewingRange * viewingRange * amountOfInputs + 1; //same +1 for td error

        //small viewing grid
        //int range = viewingRange*2+1;
        //inputSize = range*range*3;

        mlp = new MLP(learningRate);

        ArrayList<AbstractActivationFunction> activationFunctionArrayList = mlp.CreateActivationFunctionList(activationFunctionList);

        //for td error every output also has an extra td node.
        hiddenNodesLayers[hiddenNodesLayers.length-1] = 12;

        System.out.println(Arrays.toString(hiddenNodesLayers));
        double[][][] initWeights = mlp.CreateWeights(hiddenNodesLayers, inputSize);

        //create the activation vectorList (contains activation of every row, the weights, and the used functions
        activationList = new ActivationVectorList(initWeights, activationFunctionArrayList);
        setExplorationChance(nnset.getExplorationRate());


        SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd");
        Date localDate = new Date();
        tostring = dtf.format(localDate) + Arrays.toString(hiddenNodesLayers) + ", " + this.getClass() + ", " + ", man" + manIndex + "activation functions" + Arrays.toString(activationFunctionList) + "size" + getGenerationSize() + "," + getEpochSize() + ",Exploration" + explorationChance;
    }

    public ActivationVectorList getActivationList() {
        return activationList;
    }


    public double[] CompleteGameTDError() {
        int worldSize = world.getGridSize();

        ArrayList<Double> gridList = new ArrayList<>();
        gridList.add((getGenerationError().size()*getEpochSize()+getEpochError().size())/(double)(getGenerationSize()*getEpochSize()));
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
        activationList = mlp.forwardPass(CompleteGameTDError(), activationList);
        return activationList;
    }

    public void AddMoveToBuffer() {
        double[] output = CalculateBestMove().getOutput();
        int move = 0;
        double maxOutcome = Double.NEGATIVE_INFINITY;
        // length over 2 because half is for td error
        for (int idx = 0; idx < output.length/2; idx++) {
            double estVal = output[idx]+ Math.sqrt(output[idx+output.length/2]);
            if (maxOutcome < estVal) {
                move = idx;
                maxOutcome = output[idx];
            }
        }

        double value = new Random().nextDouble() % 1;
        if(testing || explorationChance > value) {
            maxOutcome = Double.NEGATIVE_INFINITY;
            // length over 2 because half is for td error
            for (int idx = 0; idx < output.length / 2; idx++) {
                double estVal = output[idx];
                if (maxOutcome < estVal) {
                    move = idx;
                    maxOutcome = output[idx];
                }
            }
        }
        moves.add(move);// add the move to the move list
    }


    public void UpdateWeights() {

        double[] targets = activationList.getOutput(); // get the expected Q-values
        double[] targetforError = targets.clone();
        if (PRINT) System.out.println("expected" + Arrays.toString(targets));

        int idx = man.getPoints().size() - 1;
        double qOutcome = man.getPoints().get(idx);
        if (PRINT) System.out.println("reward:" + qOutcome);

        //calculate max q value for q learning
        if (man.getAlive()) {
            ActivationVectorList secondpasslist = mlp.forwardPass(CompleteGameTDError(), activationList);
            double[] secondpass = secondpasslist.getOutput();
            double maxOutcome = Double.NEGATIVE_INFINITY;
            for (int moveidx = 0; moveidx < secondpass.length/2; moveidx++) {
                if (maxOutcome < secondpass[moveidx]) {
                    maxOutcome = secondpass[moveidx];
                }
            }
            if (PRINT) System.out.println("second pass:" + discount * maxOutcome);
            qOutcome += discount * maxOutcome;

        }

        //calculate td error.
        double tdError = Math.sqrt(Math.pow(qOutcome - targets[getLastMove()],2));
        targets[getLastMove()] = qOutcome;

        targets[getLastMove()+targets.length/2] = tdError;


        if (PRINT) System.out.println("outcome:" + qOutcome);
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


