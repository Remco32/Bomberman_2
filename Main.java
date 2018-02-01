import AI.*;
import GameWorld.Bomb;
import GameWorld.BomberMan;
import GameWorld.GameWorld;
import Graphics.GraphError;
import MLP.MLP;
import util.GameSettings;
import util.Load;
import util.NNSettings;
import util.Store;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by joseph on 25/03/2017.
 */
public class Main {

    long startTimeTrials = System.currentTimeMillis();
    int currentEpoch;
    int currentGeneration;

    /** Parameters **/
    static int AMOUNT_OF_EPOCHS = 10000;
    static int AMOUNT_OF_TESTS = 100;
    static int AMOUNT_OF_GENERATIONS = 100;
    static int ROUND_TIME = 1; //Time for a single gamestep in ms.
    static double EXPLORATION_RATE = 0.3;
    static double LEARNING_RATE = 0.0001;

    //For rewards, check GameWorld


    /** SAVING **/
    static boolean SAVE_EVERY_GENERATION = false;
    static boolean STOREDATA = false;
    /** LOADING **/
    static boolean SELECT_NETWORK_TO_LOAD = false; //also needs to be enabled to load hierarchical
    static boolean LOAD_HIERARHCIAL = false;

    /** DEBUG **/
    static boolean FIND_MINIMUM_ROUND_TIME = false; //decreases the roundtime by 1 ms every generation. Should eventually crash the program.



    public static void main(String[] args) {
        // parameters
        Main main = new Main();
        GameSettings Gset = new GameSettings();
        ArrayList<NNSettings> nnSettingsArrayList = new ArrayList<NNSettings>();

        NNSettings nn1 = new NNSettings();
        nn1.setWeigths(new int[]{100, 6});
        nn1.setFunctions(new int[]{MLP.SIGMOID, MLP.LINEAR});
        nn1.setTypeNetwork(NNSettings.HIERARCHICAL_EPSILON_GREEDY);
        nnSettingsArrayList.add(nn1);
        nn1.setExplorationRate(EXPLORATION_RATE);
        nn1.setLOADWEIGHTS(SELECT_NETWORK_TO_LOAD);
        nn1.setSTOREDATA(STOREDATA);


//        NNSettings nn2 = new NNSettings();
//        nn2.setWeigths(new int[]{100, 6});
//        nn2.setFunctions(new int[]{MLP.SIGMOID, MLP.LINEAR});
//        nn2.setTypeNetwork(NNSettings.NEURAL_NETWORK_FULL_INPUT);
//        nnSettingsArrayList.add(nn2);
        nn1.setLearningRate(LEARNING_RATE);
        Gset.setAmountOfEpochs(AMOUNT_OF_EPOCHS); //Amount of games played per set
        Gset.setAmountOfGenerations(AMOUNT_OF_GENERATIONS); //Amount of sets of all training games and then all test games
        Gset.setAmountOfTests(AMOUNT_OF_TESTS); //amount of test trials after each set of training trials
        Gset.setAmountOfPlayers(4);
        Gset.setAcummulateTest(1);



        //System.out.println("Current time" + System.currentTimeMillis());
        main.StartTraining(Gset, nnSettingsArrayList);


    }

    void printCurrentTime(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.println("Starting date: " + dateFormat.format(date));
    }

    void printTimeRemaining(){
        double estimatedTimeLeft = 0;
        long totalTimeElapsed = System.currentTimeMillis() - startTimeTrials;
        if (currentGeneration > 1) {

            int remainingGenerations = AMOUNT_OF_GENERATIONS - currentGeneration;
            int completedGenerations = currentGeneration - 1;
            double averageTimePerTrial = totalTimeElapsed / completedGenerations;

            estimatedTimeLeft = averageTimePerTrial * remainingGenerations + averageTimePerTrial; // + averageTime for off-by-one error

        }

        int minutes = (int) (estimatedTimeLeft / 1000 / 60);
        int seconds = (int) (estimatedTimeLeft / 1000 % 60);
        int hours = minutes/60;
        minutes = minutes - hours*60;

        String timeLeft = "Time left: ~";
        if(hours > 0) timeLeft = timeLeft.concat(hours + "h");
        if(minutes > 0) timeLeft = timeLeft.concat(minutes + "m");
        if(seconds > 0)  timeLeft = timeLeft.concat(seconds + "s");
        if(seconds == 0 && minutes == 0) timeLeft = timeLeft.concat("unknown");

        System.out.println(timeLeft);
    }

    void printTimeSpent() {

        long totalTimeElapsed = System.currentTimeMillis() - startTimeTrials;
        int totalGames = AMOUNT_OF_EPOCHS+AMOUNT_OF_TESTS*AMOUNT_OF_GENERATIONS;


        int minutes = (int) (totalTimeElapsed / 1000 / 60);
        int seconds = (int) (totalTimeElapsed / 1000 % 60);
        int hours = minutes / 60;
        minutes = minutes - hours * 60;



        String timeSpent = "Time spent: ";
        if (hours > 0) timeSpent = timeSpent.concat(hours + "h");
        if (minutes > 0) timeSpent = timeSpent.concat(minutes + "m");
        if (seconds > 0) timeSpent = timeSpent.concat(seconds + "s");
        //if(seconds == 0 && minutes == 0) timeLeft = timeLeft.concat("unknown");


        System.out.println();
        System.out.println("Finished. " + timeSpent + " on "
                + totalGames + " games.");
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

        ArrayList<ArrayList<Double>> accumulateError1 = new ArrayList<>();
        ArrayList<ArrayList<Double>> accumulateError2 = new ArrayList<>();
        ArrayList<ArrayList<Double>> accumulateError3 = new ArrayList<>();

        printCurrentTime();
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
            world.setRoundTime(ROUND_TIME);
            new Store(ai.get(0), ROUND_TIME, NNSettingsList.get(0)); //stores parameters

            // start training
            for (int gen = 0; gen < gameSettings.getAmountOfGenerations(); gen++) {
                this.currentGeneration = gen;

                if(FIND_MINIMUM_ROUND_TIME){
                    world.setRoundTime(world.getRoundtime()-1); //decrease by one every generation
                    System.out.println("Roundtime set to " + world.getRoundtime() + "ms");
                }

                //set the exploration chance
                for (int x = 0; x < NNSettingsList.size(); x++)
                    ai.get(x).setExplorationChance(NNSettingsList.get(x).getExplorationRate());

                for (int x = 0; x < NNSettingsList.size(); x++) ai.get(x).setTesting(false);
                //only test the first time because we need a baseline
                if (gen > 0) {
                    for (int epoch = 0; epoch < gameSettings.getAmountOfEpochs(); epoch++) {
                        this.currentEpoch = epoch;
                        world.InitWorld();
                        for (AIHandler temp : ai) temp.setNewBomberman(); // update the bombermans in the handlers
                        world.GameLoop();
                    }
                }
                //testing
                System.out.println("Testing generation "+gen);
                for (int x = 0; x < NNSettingsList.size(); x++) ai.get(x).setExplorationChance(0);
                for (int x = 0; x < NNSettingsList.size(); x++) ai.get(x).setTesting(true);
                for (int test = 0; test < gameSettings.getAmountOfTests(); test++) {
                    world.InitWorld();
                    for (AIHandler temp : ai) temp.setNewBomberman(); // update the bombermans in the handlers
                    world.GameLoop();
                }

                //updates all the testvalues from currentEpoch level to generation level
                for (int x = 0; x < NNSettingsList.size(); x++) ai.get(x).newGeneration();
                if(ai.toString().contains("Hierarchical")) {
                    for (int x = 0; x < NNSettingsList.size(); x++)  ((HierarchicalAIErrorDriven) ai.get(0)).getOneEnemyNetwork().newGeneration();
                    for (int x = 0; x < NNSettingsList.size(); x++) ((HierarchicalAIErrorDriven) ai.get(0)).getTwoEnemiesNetwork().newGeneration();
                    for (int x = 0; x < NNSettingsList.size(); x++) ((HierarchicalAIErrorDriven) ai.get(0)).getThreeEnemiesNetwork().newGeneration();
                }

                //double procentDone = ((accumulate + 1) / (double) gameSettings.getAcummulateTest()) * ((gen + 1) / (double) gameSettings.getAmountOfGenerations()) * 100;
                //System.out.println("\rPercent done:" + procentDone);
                //System.out.flush();
                if ( SAVE_EVERY_GENERATION || (gen == gameSettings.getAmountOfGenerations() - 1)) {
                    for (int x = 0; x < NNSettingsList.size(); x++) {
                        NNSettings setting = NNSettingsList.get(x);
                        if (!setting.isLOADWEIGHTS() || setting.isSTOREDATA()) {
                            //ai.get(x).getWinrate().remove(ai.get(x).getWinrate().size() - 1);
                            new Store(ai.get(x)); //save network
                        }
                    }

                    accumulateWinrate.add(new ArrayList<Double>());
                    accumulateError.add(new ArrayList<Double>());
                    accumulatePoints.add(new ArrayList<Double>());

                    accumulateError1.add(new ArrayList<Double>());
                    accumulateError2.add(new ArrayList<Double>());
                    accumulateError3.add(new ArrayList<Double>());

                    for (int x = 0; x <= currentGeneration; x++) {
                        AIHandler temp = ai.get(0);
                        accumulateWinrate.get(accumulate).add(temp.getWinrate().get(x));
                        accumulateError.get(accumulate).add(temp.getGenerationError().get(x));
                        accumulatePoints.get(accumulate).add(temp.getGenerationPoints().get(x));
                        if(ai.toString().contains("Hierarchical")) {
                            accumulateError1.get(accumulate).add( ((HierarchicalAIErrorDriven)temp).getGenerationErrorNetwork1().get(x));
                            accumulateError2.get(accumulate).add( ((HierarchicalAIErrorDriven)temp).getGenerationErrorNetwork2().get(x));
                            accumulateError3.get(accumulate).add( ((HierarchicalAIErrorDriven)temp).getGenerationErrorNetwork3().get(x));
                        }
                    }

                    if(ai.toString().contains("Hierarchical")) {
                        new Store(accumulateWinrate.get(accumulate), accumulatePoints.get(accumulate), accumulateError.get(accumulate), ai.get(0), accumulate,
                                accumulateError1.get(accumulate), accumulateError2.get(accumulate), accumulateError3.get(accumulate));
                    }else {
                        new Store(accumulateWinrate.get(accumulate), accumulatePoints.get(accumulate), accumulateError.get(accumulate), ai.get(0), accumulate); //saves to CSV as well
                    }


                    accumulateWinrate.clear();
                    accumulateError.clear();
                    accumulatePoints.clear();
                    accumulateError1.clear();
                    accumulateError2.clear();
                    accumulateError3.clear();

                }
                printTimeRemaining();



            }
            //store in arraylists
            accumulateWinrate.add(new ArrayList<Double>());
            accumulateError.add(new ArrayList<Double>());
            accumulatePoints.add(new ArrayList<Double>());
            accumulateError1.add(new ArrayList<Double>());
            accumulateError2.add(new ArrayList<Double>());
            accumulateError3.add(new ArrayList<Double>());
            for (int x = 0; x < gameSettings.getAmountOfGenerations(); x++) {
                AIHandler temp = ai.get(0);
                accumulateWinrate.get(accumulate).add(temp.getWinrate().get(x));
                accumulateError.get(accumulate).add(temp.getGenerationError().get(x));
                accumulatePoints.get(accumulate).add(temp.getGenerationPoints().get(x));
                if(ai.toString().contains("Hierarchical")) {
                    accumulateError1.get(accumulate).add( ((HierarchicalAIErrorDriven)temp).getGenerationErrorNetwork1().get(x));
                    accumulateError2.get(accumulate).add( ((HierarchicalAIErrorDriven)temp).getGenerationErrorNetwork2().get(x));
                    accumulateError3.get(accumulate).add( ((HierarchicalAIErrorDriven)temp).getGenerationErrorNetwork3().get(x));
                }
            }
            new Store(accumulateWinrate.get(accumulate), accumulatePoints.get(accumulate), accumulateError.get(accumulate), ai.get(0), accumulate); //saves to CSV as well
        }

        printTimeSpent();

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

        //Close program
        System.exit(0);
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
            if (setting.getTypeNetwork() == setting.HIERARCHICAL_ERROR_DRIVEN)
                nn = new HierarchicalAIErrorDriven(world, idx, setting, gSet);
            if (setting.getTypeNetwork() == setting.HIERARCHICAL_EPSILON_GREEDY)
                nn = new HierarchicalAIEpsilonGreedy(world, idx, setting, gSet);

            /**
            AIHandler nn2 = new AIHandler(world, idx);
            nn2 = new HierarchicalAIErrorDriven(world, idx, setting, gSet);
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
            //System.out.println("Created neural net for player " + idx);
            if (setting.isLOADWEIGHTS() && !LOAD_HIERARHCIAL) {
            nn.getActivationVectorlist().setWeigths(new Load().Load());
            //nn.setLearningRate(0);
            }

            if (setting.isLOADWEIGHTS() && LOAD_HIERARHCIAL) {
                System.out.println("Select each hiearchical network in order.");

                ((HierarchicalAIErrorDriven)nn).pathFindingNetwork.setActivationList(new Load().loadHierarchical());
                ((HierarchicalAIErrorDriven)nn).oneEnemyNetwork.setActivationList(new Load().loadHierarchical());
                ((HierarchicalAIErrorDriven)nn).twoEnemiesNetwork.setActivationList(new Load().loadHierarchical());
                ((HierarchicalAIErrorDriven)nn).threeEnemiesNetwork.setActivationList(new Load().loadHierarchical());
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

        //TODO close program
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
