package AI;

import GameWorld.*;
import MLP.AbstractActivationFunction;
import MLP.ActivationVectorList;
import MLP.MLP;
import util.GameSettings;
import util.NNSettings;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.pow;

/**
 * Created by joseph on 15/02/2017.
 */
public class eGreedyDiminishingTroughTurns extends AIHandler {
    ArrayList<Double> totalError;
    private ActivationVectorList activationList;
    private int inputSize = 6; // all basic features

    static private int viewingRange = 2;
    private int amountOfInputs = 4;
    private String tostring ="";

    public eGreedyDiminishingTroughTurns(GameWorld world, int manIndex, NNSettings setting, GameSettings gSet) {
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

        double value = new Random().nextDouble() % 1;
        double time = getGenerationSize()/(double)(getGenerationError().size()+1);
        time = Math.max(1.2,time);
        double logbeta = 1/(Math.exp(getGenerationError().size() * getEpochError().size()*75/Math.pow(5,error.size())));
        logbeta=Math.max(0.05,logbeta);
      // double curExplorationChance = Math.min(0.4,logbeta);
     //   System.out.println("step:" + error.size() + ":");
     //   System.out.println(logbeta);
     //   System.out.println("time" + time);
     //   System.out.println("Qvalues"+Arrays.toString(output));
        if (logbeta > value && !testing) {
            move = new Random().nextInt(6);
            if (PRINT) System.out.println("random!" );
        }
        if (PRINT) System.out.println("move:" + move);
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

    public String toString(){
        return tostring;
    }

    @Override
    public ActivationVectorList getActivationVectorlist() {
        return activationList;
    }
}