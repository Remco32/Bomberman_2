package AI;

import GameWorld.BomberMan;
import GameWorld.GameWorld;
import MLP.ActivationVectorList;
import MLP.MLP;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by joseph on 9-2-2017.
 */

public class AIHandler implements Serializable {

    protected GameWorld world;
    protected BomberMan man;
    protected int manIndex;
    protected Random rnd;
    protected ArrayList<Double> error;

    protected ArrayList<Integer> moves;
    protected MLP mlp;
    protected double learningRate = 0.0001;
    protected double explorationChance = 0.3;
    protected double discount = 1;
    protected boolean testing = false;

    private ArrayList<Double> epochError = new ArrayList<>();
    private ArrayList<Double> generationError = new ArrayList<>();
    private ArrayList<Integer> epochPoints = new ArrayList<>();
    private ArrayList<Double> generationPoints = new ArrayList<>();
    private ArrayList<Double> winrate = new ArrayList<>();

    protected int generationSize=100;
    protected int epochSize = 1000;
    protected boolean PRINT = false;

    public AIHandler(GameWorld world, int manIndex) {
        this.world = world;
        this.manIndex = manIndex;
        moves = new ArrayList<>();
        error = new ArrayList<>();
        winrate.add(0.0);
        rnd = new Random();
    }

    public void MakeMove() {
        man.Move(moves.get(moves.size() - 1));
    }

    public void AddMoveToBuffer() {
    }

    public void setNewBomberman() {
        error = new ArrayList<>();
        moves = new ArrayList<>();
        man = world.getBomberManList().get(manIndex);
    }

    public void setExplorationChance(double explorationChance) {
        this.explorationChance = explorationChance;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
        mlp.setLearningRate(learningRate);
    }

    public void setPRINT(boolean val) {
        PRINT = val;
    }


    public GameWorld getWorld() {
        return world;
    }

    public BomberMan getMan() {
        return man;
    }

    public int getManIndex() {
        return manIndex;
    }

    public ArrayList<Integer> getMoves() {
        return moves;
    }

    public int getLastMove() {
        return moves.get(moves.size() - 1);
    }

    public int getMove(int lookahead) {
        return moves.get(moves.size() - 1 - lookahead);
    }

    public ArrayList<Double> getError() {
        return error;
    }

    public void UpdateWeights() {
    }

    public ArrayList<Double> getEpochError() {
        return epochError;
    }

    public void EndOfRound(double win) {
        if (testing) {
            win += winrate.get(winrate.size() - 1);
            winrate.set(winrate.size() - 1, win);
            int tempPoints = 0;
            for (double temp : man.getPoints()) tempPoints += temp;
            epochPoints.add(tempPoints);
        }
        double tempError = 0;
        for (double temp : error) tempError += temp;
        epochError.add(tempError / (double) error.size());
        error = new ArrayList<>();


    }

    public ArrayList<Double> getGenerationError() {
        return generationError;
    }

    public boolean isPRINT() {
        return PRINT;
    }

    public ArrayList<Double> getWinrate() {
        return winrate;
    }

    public ArrayList<Double> getGenerationPoints() {
        return generationPoints;
    }

    public void setWinrate(ArrayList<Double> winrate) {
        this.winrate = winrate;
    }

    public void newGeneration() {
        double tempError = 0;
        for (double temp : epochError) tempError += temp;
        generationError.add(tempError / epochError.size());

        Double tempPoints = 0.0;
        for (double temp : epochPoints) tempPoints += temp;
        generationPoints.add(tempPoints / epochPoints.size());

        int wsize = winrate.size()-1;
        winrate.set(wsize,(winrate.get(wsize)/(double) (2*epochPoints.size()))+0.5);
        winrate.add(0.0);

        epochPoints = new ArrayList<>();
        epochError = new ArrayList<>();
    }



    public String toString(){
        return "aiHandler";
    }


    public void setTesting(boolean testing) {
        this.testing = testing;
    }

    public ActivationVectorList getActivationVectorlist(){return null;}

    public int getGenerationSize() {
        return generationSize;
    }

    public void setGenerationSize(int generationSize) {
        this.generationSize = generationSize;
    }

    public int getEpochSize() {
        return epochSize;
    }

    public void setEpochSize(int epochSize) {
        this.epochSize = epochSize;
    }

    public ArrayList<Integer> getEpochPoints(){
        return epochPoints;
    }

    public void setError(ArrayList<Double> error) {
        this.error = error;
    }

    public void setEpochError(ArrayList<Double> epochError) {
        this.epochError = epochError;
    }

    public void setEpochPoints(ArrayList<Integer> epochPoints) {
        this.epochPoints = epochPoints;
    }

    public void setManIndex(int manIndex) {
        this.manIndex = manIndex;
    }

    public void setMan(BomberMan man) {
        this.man = man;
    }
}
