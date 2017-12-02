import AI.*;
import GameWorld.GameWorld;
import util.GameSettings;
import util.NNSettings;
import util.Store;

import java.util.ArrayList;

/**
 * Created by Joseph on 6/17/2017.
 */
public class TrainParallel implements Runnable{
    ArrayList<Double> winrate;
    ArrayList<Double> points;
    ArrayList<Double> error;
    ArrayList<NNSettings> NNSettingsList;
    GameSettings gameSettings;
    public static double procentDone=0;

    TrainParallel(ArrayList<Double> winrate,ArrayList<Double>points ,ArrayList<Double> error,GameSettings gameSettings,ArrayList<NNSettings> setting) {
        this.winrate=winrate;
        this.points=points;
        this.error=error;
        this.gameSettings=gameSettings;
        this.NNSettingsList = setting;
    }

    public void run() {
        GameWorld world = new GameWorld(gameSettings.getWorldSize(), gameSettings.getAmountOfPlayers(), gameSettings.isShowWindow());
        ArrayList<AIHandler> ai = new ArrayList<>();

        for (int x = 0; x < gameSettings.getAmountOfPlayers(); x++) {
            NNSettings setting = null;
            if (x < NNSettingsList.size()) {
                setting = NNSettingsList.get(x);
            }
            ai.add(CreateNN(setting, gameSettings, world, x));
        }
        world.SetAi(ai);

        // start training
        for (int gen = 0; gen < gameSettings.getAmountOfGenerations(); gen++) {
            //set the exploration chance
            for (int x = 0; x < NNSettingsList.size(); x++)
                ai.get(x).setExplorationChance(NNSettingsList.get(x).getExplorationRate());

            for (int x = 0; x < NNSettingsList.size(); x++) ai.get(x).setTesting(false);
            //only test the first time because we need a baseline
            if (gen > 0) {
                for (int epoch = 0; epoch < gameSettings.getAmountOfEpochs(); epoch++) {
                    world.InitWorld();
                    for (AIHandler temp : ai) temp.setNewBomberman(); // update the bombermans in the handlers
                    world.GameLoop();
                }
            }
            //testing
            System.out.print(" testing");
            for (int x = 0; x < NNSettingsList.size(); x++) ai.get(x).setExplorationChance(0);
            for (int x = 0; x < NNSettingsList.size(); x++) ai.get(x).setTesting(true);
            for (int test = 0; test < gameSettings.getAmountOfTests(); test++) {
                world.InitWorld();
                for (AIHandler temp : ai) temp.setNewBomberman(); // update the bombermans in the handlers
                world.GameLoop();
            }

            //updates all the testvalues from currentEpoch level to generation level
            for (int x = 0; x < NNSettingsList.size(); x++) ai.get(x).newGeneration();

            procentDone += ((1 / (double) gameSettings.getAmountOfGenerations())/(double) gameSettings.getAcummulateTest())*100;

            System.out.print("\rProcent done:" + procentDone);
            System.out.flush();
            if (gen == gameSettings.getAmountOfGenerations() - 1) {
                for (int x = 0; x < NNSettingsList.size(); x++) {
                    NNSettings setting = NNSettingsList.get(x);
                    if (!setting.isLOADWEIGHTS() || setting.isSTOREDATA()) {
                        ai.get(x).getWinrate().remove(ai.get(x).getWinrate().size() - 1);
                        new Store(ai.get(x));
                    }
                }
            }

        }
        for (int x = 0; x < gameSettings.getAmountOfPlayers(); x++) {

            AIHandler temp = ai.get(0);
            for(int idx=0;idx<gameSettings.getAmountOfGenerations();idx++) {
                winrate.add(temp.getWinrate().get(idx));
                points.add(temp.getGenerationPoints().get(idx));
                error.add(temp.getGenerationError().get(idx));
            }
        }
        new Store(winrate,points,error, ai.get(0), Thread.currentThread().getName());
    }


    public AIHandler CreateNN(NNSettings setting, GameSettings gSet, GameWorld world, int idx) {
        AIHandler nn = new AIHandler(world, idx);

        if (setting == null) nn = (new RandomAI(world, idx));
        else if (setting.isLOADWEIGHTS()) {
            //load
        } else {
            if (setting.getTypeNetwork() == setting.NEURAL_NETWORK_FULL_INPUT)
                nn = new NeuralNetworkAIFullInput(world, idx, setting, gSet);
            if (setting.getTypeNetwork() == setting.NEURAL_NETWORK_VISION_GRID)
                nn = new NeuralNetworkVisionGrid(world, idx, setting, gSet);
            if (setting.getTypeNetwork() == setting.NEURAL_NETWORK_SOFT_MAX)
                nn = new SoftMaxNNAIFullInput(world, idx, setting, gSet);
            if (setting.getTypeNetwork() == setting.NEURAL_NETWORK_ERROR_BOLTZMAN)
                nn = new ErrorDrivenBoltzmanNNFullInput(world, idx, setting, gSet);
            if (setting.getTypeNetwork() == setting.NEURAL_NETWORK_TIME_BOLTZMAN)
                nn = new TimeDrivenBoltzmanNNFullInput(world, idx, setting, gSet);
            if (setting.getTypeNetwork() == setting.NEURAL_NETWORK_GREEDY_DIMINISHING_OVER_TURN)
                nn = new eGreedyDiminishingTroughTurns(world, idx, setting, gSet);
            if (setting.getTypeNetwork() == setting.NEURAL_NETWORK_GREEDY_DIMINISHING_OVER_TIME)
                nn = new diminishingEGreedy(world, idx, setting, gSet);
            if (setting.getTypeNetwork() == setting.TDERROR)
                nn = new TDError(world, idx, setting.getWeigths(), setting.getFunctions(),setting);
            if (setting.getTypeNetwork() == setting.RANDOM_WALK)
                nn = new RandomWalk(world, idx, setting, gSet);
            if (setting.getTypeNetwork() == setting.GREEDY)
                nn = new Greedy(world, idx, setting, gSet);
            nn.setDiscount(setting.getDiscount());
            nn.setLearningRate(setting.getLearningRate());
            nn.setExplorationChance(setting.getExplorationRate());
            nn.setGenerationSize(gSet.getAmountOfGenerations());
            nn.setEpochSize(gSet.getAmountOfEpochs());
            System.out.println("created:" + nn);
        }
        return nn;
    }


}
