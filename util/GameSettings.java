package util;

/**
 * Created by Joseph on 5/9/2017.
 */
public class GameSettings {
    private int amountOfEpochs =  1000;
    private int amountOfGenerations = 100 ;
    private int amountOfTests = 100;
    private int acummulateTest = 100;
    private int amountOfPlayers=4;
    private int worldSize = 7;

    private int DieCost=-300;
    private int killReward=100;
    private int wallReward=30;
    private int movecost=-1;

    private boolean showWindow=true;

    public int getAmountOfEpochs() {
        return amountOfEpochs;
    }
    public int getAmountOfGenerations() {
        return amountOfGenerations;
    }
    public int getAmountOfTests() {
        return amountOfTests;
    }
    public int getAmountOfPlayers() {

        return amountOfPlayers;
    }
    public int getWorldSize() {
        return worldSize;
    }
    public boolean isShowWindow() {

        return showWindow;
    }
    public int getKillReward() {
        return killReward;
    }
    public int getWallReward() {
        return wallReward;
    }
    public int getMovecost() {
        return movecost;
    }
    public int getDieCost() {
        return DieCost;
    }
    public int getAcummulateTest() {
        return acummulateTest;
    }

    public void setAcummulateTest(int acummulateTest) {
        this.acummulateTest = acummulateTest;
    }
    public void setDieCost(int dieCost) {
        DieCost = dieCost;
    }
    public void setAmountOfEpochs(int amountOfEpochs) {
        this.amountOfEpochs = amountOfEpochs;
    }
    public void setAmountOfGenerations(int amountOfGenerations) {
        this.amountOfGenerations = amountOfGenerations;
    }
    public void setAmountOfTests(int amountOfTests) {
        this.amountOfTests = amountOfTests;
    }
    public void setAmountOfPlayers(int amountOfPlayers){this.amountOfPlayers = amountOfPlayers;}
    public void setWorldSize(int worldSize) {

        this.worldSize = worldSize;
    }
    public void setShowWindow(boolean showWindow) {
        this.showWindow = showWindow;
    }
    public void setKillReward(int killReward) {
        this.killReward = killReward;
    }
    public void setWallReward(int wallReward) {
        this.wallReward = wallReward;
    }
    public void setMovecost(int movecost) {
        this.movecost = movecost;
    }

}
