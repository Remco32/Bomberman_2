package util;

/**
 * Created by Joseph on 5/9/2017.
 */
public class NNSettings {

    private int[] weigths;
    private int[] functions;
    private double discount =0.90;
    private double explorationRate = 0.2;
    private double learningRate = 0.01;
    private int typeNetwork;

    private boolean LOADWEIGHTS = false;
    private boolean STOREDATA = false;


    public void setSTOREDATA(boolean STOREDATA) {
        this.STOREDATA = STOREDATA;
    }

    public boolean isSTOREDATA() {

        return STOREDATA;
    }

    public static final int NEURAL_NETWORK_FULL_INPUT = 0;
    public static final int NEURAL_NETWORK_VISION_GRID = 1;
    public static final int NEURAL_NETWORK_SOFT_MAX = 2;
    public static final int NEURAL_NETWORK_ERROR_BOLTZMAN = 3;
    public static final int NEURAL_NETWORK_TIME_BOLTZMAN = 4;
    public static final int NEURAL_NETWORK_GREEDY_DIMINISHING_OVER_TURN = 5;
    public static final int NEURAL_NETWORK_GREEDY_DIMINISHING_OVER_TIME = 6;
    public static final int TDERROR = 7;
    public static final int RANDOM_WALK = 8;
    public static final int GREEDY = 9;
    public static final int HIERARCHICAL_ERROR_DRIVEN = 10;
    public static final int HIERARCHICAL_EPSILON_GREEDY = 11;
    public static final int HIERARCHICAL_GREEDY = 12;

    public int[] getWeigths() {
        return weigths;
    }
    public int[] getFunctions() {
        return functions;
    }
    public double getDiscount() {
        return discount;
    }
    public double getExplorationRate() {
        return explorationRate;
    }
    public double getLearningRate() {
        return learningRate;
    }

    public boolean isLOADWEIGHTS() {
        return LOADWEIGHTS;
    }
    public int getTypeNetwork() {
        return typeNetwork;
    }

    public void setWeigths(int[] weigths) {
        this.weigths = weigths;
    }
    public void setFunctions(int[] functions) {
        this.functions = functions;
    }
    public void setDiscount(double discount) {
        this.discount = discount;
    }
    public void setExplorationRate(double explorationRate) {
        this.explorationRate = explorationRate;
    }
    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }
    public void setLOADWEIGHTS(boolean LOADWEIGHTS) {
        this.LOADWEIGHTS = LOADWEIGHTS;
    }
    public void setTypeNetwork(int typeNetwork) {
        this.typeNetwork = typeNetwork;
    }

}
