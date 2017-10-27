import AI.*;
import GameWorld.*;
import Graphics.GraphError;
import MLP.MLP;
import util.GameSettings;
import util.Load;
import util.NNSettings;
import util.Store;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by joseph on 25/03/2017.
 */
public class Main {

    public static void main(String[] args) {
        // parameters
        Main main = new Main();
        GameSettings Gset = new GameSettings();
        ArrayList<NNSettings> nnSettingsArrayList = new ArrayList<NNSettings>();

        NNSettings nn1 = new NNSettings();
        nn1.setWeigths(new int[]{100, 6});
        nn1.setFunctions(new int[]{MLP.SIGMOID, MLP.LINEAR});
        nn1.setTypeNetwork(NNSettings.HIERARCHICAL);
        nnSettingsArrayList.add(nn1);
        nn1.setExplorationRate(0.3);
        nn1.setLOADWEIGHTS(false);


//        NNSettings nn2 = new NNSettings();
//        nn2.setWeigths(new int[]{100, 6});
//        nn2.setFunctions(new int[]{MLP.SIGMOID, MLP.LINEAR});
//        nn2.setTypeNetwork(NNSettings.NEURAL_NETWORK_FULL_INPUT);
//        nnSettingsArrayList.add(nn2);
        nn1.setLearningRate(0.0001);
        Gset.setAmountOfEpochs(1000);
        Gset.setAmountOfGenerations(100);
        Gset.setAmountOfTests(100);
        Gset.setAmountOfPlayers(4);
        Gset.setAcummulateTest(1);

       // double time = System.currentTimeMillis();
       // System.out.println("Current time" + System.currentTimeMillis());
        main.StartTraining(Gset, nnSettingsArrayList);

      //  System.out.println("passed time:" + (System.currentTimeMillis()-time)/1000);


    }

    public void StartTraining(GameSettings gameSettings, ArrayList<NNSettings> NNSettingsList) {
        //init static vars
        Bomb.setDIECOST(gameSettings.getDieCost());
        Bomb.setKillReward(gameSettings.getKillReward());
        Bomb.setWallReward(gameSettings.getWallReward());
        BomberMan.setMOVECOST(gameSettings.getMovecost());

        //create world and ai's
        GameWorld world = new GameWorld(gameSettings.getWorldSize(), gameSettings.getAmountOfPlayers(), gameSettings.isShowWindow());
        ArrayList<AIHandler> ai = new ArrayList<AIHandler>();
        ShowWindow window = new ShowWindow(world);

        ArrayList<ArrayList<Double>> accumulateWinrate = new ArrayList<>();
        ArrayList<ArrayList<Double>> accumulatePoints = new ArrayList<>();
        ArrayList<ArrayList<Double>> accumulateError = new ArrayList<>();

        for (int accumulate = 0; accumulate < gameSettings.getAcummulateTest(); accumulate++) {
            world = new GameWorld(gameSettings.getWorldSize(), gameSettings.getAmountOfPlayers(), gameSettings.isShowWindow());
            window.setWorld(world);
            ai = new ArrayList<>();

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

                //updates all the testvalues from epoch level to generation level
                for (int x = 0; x < NNSettingsList.size(); x++) ai.get(x).newGeneration();

                double procentDone = ((accumulate + 1) / (double) gameSettings.getAcummulateTest()) * ((gen + 1) / (double) gameSettings.getAmountOfGenerations()) * 100;
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
            //store in arraylists
            accumulateWinrate.add(new ArrayList<Double>());
            accumulateError.add(new ArrayList<Double>());
            accumulatePoints.add(new ArrayList<Double>());
            for (int x = 0; x < gameSettings.getAmountOfGenerations(); x++) {
                AIHandler temp = ai.get(0);
                accumulateWinrate.add(new ArrayList<Double>());

                accumulateWinrate.get(accumulate).add(temp.getWinrate().get(x));
                accumulateError.get(accumulate).add(temp.getGenerationError().get(x));
                accumulatePoints.get(accumulate).add(temp.getGenerationPoints().get(x));
            }
            new Store(accumulateWinrate.get(accumulate), accumulatePoints.get(accumulate), accumulateError.get(accumulate), ai.get(0), accumulate);
        }

        // calculate mean
        ArrayList<Double> meanWinrate = new ArrayList<>();
        ArrayList<Double> meanError = new ArrayList<>();
        ArrayList<Double> meanPoints = new ArrayList<>();

        int accumsize = gameSettings.getAcummulateTest();
        for (int genIdx = 0; genIdx < gameSettings.getAmountOfGenerations(); genIdx++) {
            double meanWinrateDouble = 0;
            double meanPointsDouble = 0;
            double meanErrorDouble = 0;
            //sum together
            for (int idx = 0; idx < accumsize; idx++) {
                meanWinrateDouble += accumulateWinrate.get(idx).get(genIdx);
                meanPointsDouble += accumulatePoints.get(idx).get(genIdx);
                meanErrorDouble += accumulateError.get(idx).get(genIdx);
            }
            //devide by samplesize
            meanWinrate.add(meanWinrateDouble / (double) accumsize);
            meanPoints.add(meanPointsDouble / (double) accumsize);
            meanError.add(meanErrorDouble / (double) accumsize);
        }
        new Store(meanWinrate, meanPoints, meanError, ai.get(0), "mean");

        //calculate std deviation;
        ArrayList<Double> stdDevWinrate = new ArrayList<>();
        ArrayList<Double> stdDevError = new ArrayList<>();
        ArrayList<Double> stdDevPoints = new ArrayList<>();
        for (int genIdex = 0; genIdex < gameSettings.getAmountOfGenerations(); genIdex++) {
            double sumWinrate = 0;
            double sumError = 0;
            double sumPoints = 0;
            for (int idx = 0; idx < accumsize; idx++) {
                sumWinrate += Math.pow(accumulateWinrate.get(idx).get(genIdex) - meanWinrate.get(genIdex), 2);
                sumError += Math.pow(accumulateError.get(idx).get(genIdex) - meanError.get(genIdex), 2);
                sumPoints += Math.pow(accumulatePoints.get(idx).get(genIdex) - meanPoints.get(genIdex), 2);
            }
            stdDevWinrate.add(Math.sqrt(sumWinrate / (double) accumsize));
            stdDevPoints.add(Math.sqrt(sumPoints / (double) accumsize));
            stdDevError.add(Math.sqrt(sumError / (double) accumsize));
        }
        new Store(stdDevWinrate, stdDevPoints, stdDevError, ai.get(0), "stdDev");


        for (int idx = 0; idx < gameSettings.getAmountOfPlayers() && idx < NNSettingsList.size(); idx++) {
            AIHandler temp = ai.get(idx);
            String params = temp.toString();
            new GraphError(meanWinrate, "Mean winrate", params, 0);
            new GraphError(meanError, "Mean Error of", params, 0);
            new GraphError(meanPoints, "Mean points", params);
            new GraphError(stdDevWinrate, "StdDev winrate", params, 0);
            new GraphError(stdDevError, "StdDev Error of", params, 0);
            new GraphError(stdDevPoints, "StdDev points", params, 0);
        }
    }


    public AIHandler CreateNN(NNSettings setting, GameSettings gSet, GameWorld world, int idx) {
        AIHandler nn = new AIHandler(world, idx);

        if (setting == null) nn = (new RandomAI(world, idx));
        else {
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
                nn = new TDError(world, idx, setting.getWeigths(), setting.getFunctions(), setting);
            if (setting.getTypeNetwork() == setting.RANDOM_WALK)
                nn = new RandomWalk(world, idx, setting, gSet);
            if (setting.getTypeNetwork() == setting.GREEDY)
                nn = new Greedy(world, idx, setting, gSet);
            if (setting.getTypeNetwork() == setting.HIERARCHICAL)
                nn = new HierarchicalAI(world, idx, setting, gSet);
                /**
                AIHandler nn2 = new AIHandler(world, idx);
                nn2 = new HierarchicalAI(world, idx, setting, gSet);
                nn2.setDiscount(setting.getDiscount());
                nn2.setLearningRate(setting.getLearningRate());
                nn2.setExplorationChance(setting.getExplorationRate());
                nn2.setGenerationSize(gSet.getAmountOfGenerations());
                nn2.setEpochSize(gSet.getAmountOfEpochs());
                System.out.println("Created 2nd neural net for player " + idx);
                if (setting.isLOADWEIGHTS()) {
                    nn2.getActivationVectorlist().setWeigths(new Load().Load());
                    nn2.setLearningRate(0);
                }
                 **/



            nn.setDiscount(setting.getDiscount());
            nn.setLearningRate(setting.getLearningRate());
            nn.setExplorationChance(setting.getExplorationRate());
            nn.setGenerationSize(gSet.getAmountOfGenerations());
            nn.setEpochSize(gSet.getAmountOfEpochs());
            System.out.println("Created neural net for player " + idx);
            if (setting.isLOADWEIGHTS()) {
            nn.getActivationVectorlist().setWeigths(new Load().Load());
            nn.setLearningRate(0);

            }

        }
        return nn;
    }


    void StartTrainingParallel(GameSettings gameSettings, ArrayList<NNSettings> NNSettingsList){
        Bomb.setDIECOST(gameSettings.getDieCost());
        Bomb.setKillReward(gameSettings.getKillReward());
        Bomb.setWallReward(gameSettings.getWallReward());
        BomberMan.setMOVECOST(gameSettings.getMovecost());

        //create world and ai's

        ArrayList<AIHandler> ai = new ArrayList<>();
        GameWorld world = new GameWorld(gameSettings.getWorldSize(), gameSettings.getAmountOfPlayers(), gameSettings.isShowWindow());


        for (int x = 0; x < gameSettings.getAmountOfPlayers(); x++) {
            NNSettings setting = null;
            if (x < NNSettingsList.size()) {
                setting = NNSettingsList.get(x);
            }
            ai.add(CreateNN(setting, gameSettings, world, x));
        }

        ArrayList<ArrayList<Double>> accumulateWinrate = new ArrayList<>();
        ArrayList<ArrayList<Double>> accumulatePoints = new ArrayList<>();
        ArrayList<ArrayList<Double>> accumulateError = new ArrayList<>();

        ArrayList<Thread> threadList = new ArrayList<>();
        for (int accumulate = 0; accumulate < gameSettings.getAcummulateTest(); accumulate++) {
            accumulateWinrate.add(new ArrayList<Double>());
            accumulatePoints.add(new ArrayList<Double>());
            accumulateError.add(new ArrayList<Double>());
            threadList.add(new Thread(new TrainParallel(accumulateWinrate.get(accumulate),accumulatePoints.get(accumulate),accumulateError.get(accumulate),gameSettings,NNSettingsList)));
            threadList.get(accumulate).start();
        }
        //wait for threads to finish
        for (int accumulate = 0; accumulate < gameSettings.getAcummulateTest(); accumulate++) {
            try {
                threadList.get(accumulate).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        // calculate mean
        ArrayList<Double> meanWinrate = new ArrayList<>();
        ArrayList<Double> meanError = new ArrayList<>();
        ArrayList<Double> meanPoints = new ArrayList<>();

        int accumsize = gameSettings.getAcummulateTest();
        for (int genIdx = 0; genIdx < gameSettings.getAmountOfGenerations(); genIdx++) {
            double meanWinrateDouble = 0;
            double meanPointsDouble = 0;
            double meanErrorDouble = 0;
            //sum together
            for (int idx = 0; idx < accumsize; idx++) {
                meanWinrateDouble += accumulateWinrate.get(idx).get(genIdx);
                meanPointsDouble += accumulatePoints.get(idx).get(genIdx);
                meanErrorDouble += accumulateError.get(idx).get(genIdx);
            }
            //devide by samplesize
            meanWinrate.add(meanWinrateDouble / (double) accumsize);
            meanPoints.add(meanPointsDouble / (double) accumsize);
            meanError.add(meanErrorDouble / (double) accumsize);
        }
        new Store(meanWinrate, meanPoints, meanError, ai.get(0), "mean");

        //calculate std deviation;
        ArrayList<Double> stdDevWinrate = new ArrayList<>();
        ArrayList<Double> stdDevError = new ArrayList<>();
        ArrayList<Double> stdDevPoints = new ArrayList<>();
        for (int genIdex = 0; genIdex < gameSettings.getAmountOfGenerations(); genIdex++) {
            double sumWinrate = 0;
            double sumError = 0;
            double sumPoints = 0;
            for (int idx = 0; idx < accumsize; idx++) {
                sumWinrate += Math.pow(accumulateWinrate.get(idx).get(genIdex) - meanWinrate.get(genIdex), 2);
                sumError += Math.pow(accumulateError.get(idx).get(genIdex) - meanError.get(genIdex), 2);
                sumPoints += Math.pow(accumulatePoints.get(idx).get(genIdex) - meanPoints.get(genIdex), 2);
            }
            stdDevWinrate.add(Math.sqrt(sumWinrate / (double) accumsize));
            stdDevPoints.add(Math.sqrt(sumPoints / (double) accumsize));
            stdDevError.add(Math.sqrt(sumError / (double) accumsize));
        }
        new Store(stdDevWinrate, stdDevPoints, stdDevError, ai.get(0), "stdDev");


        for (int idx = 0; idx < gameSettings.getAmountOfPlayers() && idx < NNSettingsList.size(); idx++) {
            AIHandler temp = ai.get(idx);
            String params = temp.toString();
            new GraphError(meanWinrate, "Mean winrate", params, 0);
            new GraphError(meanError, "Mean Error of", params, 0);
            new GraphError(meanPoints, "Mean points", params);
            new GraphError(stdDevWinrate, "StdDev winrate", params, 0);
            new GraphError(stdDevError, "StdDev Error of", params, 0);
            new GraphError(stdDevPoints, "StdDev points", params, 0);
        }

    }


    public class ShowWindow extends JFrame {
        GameWorld world;
        ShowWindow(GameWorld world) {
            super();
            this.world=world;
            createGUI();
            pack();
            setVisible(true);
        }

        void createGUI(){
            JButton showWindow = new JButton("hide/show windows");
            showWindow.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    world.setWindowBool(!world.getWindowBool());
                }
            });
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

            this.add(showWindow);

        }
        void setWorld(GameWorld world){
            this.world=world;
        }

    }
}
