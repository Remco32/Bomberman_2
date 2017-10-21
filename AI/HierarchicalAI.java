package AI;

import GameWorld.*;
import MLP.AbstractActivationFunction;
import MLP.ActivationVectorList;
import MLP.MLP;
import util.GameSettings;
import util.NNSettings;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.pow;

/**
 * Created by Remco on 21-10-2017.
 */


public class HierarchicalAI extends AIHandler {
    private boolean DEBUG = true;

    public HierarchicalAI(GameWorld world, int manIndex, NNSettings setting, GameSettings gSet) {
        super(world, manIndex);
        this.world = world;

        mlp = new MLP(learningRate);

        this.setDiscount(setting.getDiscount());
        this.setLearningRate(setting.getLearningRate());
        this.setExplorationChance(setting.getExplorationRate());
        this.setGenerationSize(gSet.getAmountOfGenerations());
        this.setEpochSize(gSet.getAmountOfEpochs());

        if (DEBUG) System.out.println("Hierch");
    }

    public void AddMoveToBuffer() {
        if (man.getAlive()) moves.add(0); //do nothing
    }

}
