package util;

import AI.*;
import MLP.ActivationVectorList;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by joseph on 10/04/2017.
 */
public class Store {
    private static String OS = null;
    String dir = System.getProperty("user.dir");

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    String str = sdf.format(new Date());


    public Store(AIHandler ai) {

        if (OS == null) OS = System.getProperty("os.name").toLowerCase();
        if (isWindows()) dir = System.getProperty("user.dir") + "\\..\\results\\images\\" + ai.toString() + "\\";
        if (isUnix()) dir = System.getProperty("user.dir") + "/../results/images/" + ai.toString() + "/";

        /**
         String timestamp = Instant.now().toString();
         timestamp = timestamp.replace(":", "-");
         dir = dir.concat(" ");
         dir = dir.concat(timestamp);
         */

        try {
            File f = new File(dir);
            f.mkdirs();

            FileOutputStream stream = new FileOutputStream(dir + ai.getGenerationError().size() + ".nn"); //filename is the generation number
            ObjectOutput s = new ObjectOutputStream(stream);

            //in case of having to save multiple networks
            if (ai.toString().contains("Hierarchical")) {
                /**
                 ArrayList list = ((HierarchicalAIErrorDriven)ai).getAllNetworks();

                 for(int i = 0; i < list.size(); i++ ){
                 s.writeObject(i);
                 }
                 **/
                ArrayList<ErrorDrivenBoltzmanNNFullInput> neuralNetList = null;

                //System.out.println(ai.toString());
                if (ai.toString().contains("HierarchicalAIErrorDriven")){
                    neuralNetList = ((HierarchicalAIErrorDriven) ai).getAllNetworks();
                }

                if (ai.toString().contains("HierarchicalAIEpsilonGreedy")){
                    neuralNetList = ((HierarchicalAIEpsilonGreedy) ai).getAllNetworks();
                }
                if (ai.toString().contains("HierarchicalAIGreedy")){
                    neuralNetList = ((HierarchicalAIGreedy) ai).getAllNetworks();
                }


                for (int i = 0; i < 4; i++) {
                    try {
                        FileOutputStream fo = new FileOutputStream(new File(dir + "gen" + ai.getGenerationError().size() + "_NeuralNetwork" + i + ".nn"));
                        ObjectOutputStream o = new ObjectOutputStream(fo);

                        // Write objects to file
                        ErrorDrivenBoltzmanNNFullInput network = neuralNetList.get(i); //get each individual network
                        ActivationVectorList list = network.getActivationList();
                        o.writeObject(list);

                        o.close();
                        fo.close();

                    } catch (FileNotFoundException e) {
                        System.out.println("File not found");
                    } catch (IOException e) {
                        System.out.println("Error initializing stream");
                        System.err.println(e);

                    }
                }

            } else {//single network case

                //stores its neural net

                s.writeObject(ai.getClass());
                s.writeObject(ai.getActivationVectorlist());

                s.close();
            }
            //Stores winrate, error, generationPoints
            stream = new FileOutputStream(dir + ai.getGenerationError().size() + ".data");
            s = new ObjectOutputStream(stream);
            s.writeObject(ai.getWinrate()); //are all arrays, so multiple values
            s.writeObject(ai.getError());
            s.writeObject(ai.getGenerationPoints());
            s.close();



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String toCSV(ArrayList array) {
        String result = "";

        if (array.size() > 0) {
            StringBuilder sb = new StringBuilder();

            for (Object s : array) {
                sb.append(s).append(",");
            }

            result = sb.deleteCharAt(sb.length() - 1).toString();
        }
        return result;
    }

    public Store(ArrayList<Double> win, ArrayList<Double> points, ArrayList<Double> error, AIHandler ai, int accum) {
        if (OS == null) OS = System.getProperty("os.name").toLowerCase();
        if (isWindows()) dir = System.getProperty("user.dir") + "\\..\\results\\images\\" + ai.toString() + "\\";
        if (isUnix()) dir = System.getProperty("user.dir") + "/../results/images/" + ai.toString() + "/";


        try {
            File f = new File(dir);
            f.mkdirs();
            FileOutputStream stream = new FileOutputStream(dir + "accum[" + accum + "]" + ".data");
            ObjectOutput s = new ObjectOutputStream(stream);
            s.writeObject(win);
            s.writeObject(error);
            s.writeObject(points);
            s.close();

            PrintWriter out = new PrintWriter(dir + ai.getGenerationError().size() + ".csv");
            out.print("Mean Winrate, ");
            out.println(toCSV(win));
            out.print("Mean Error network 0, ");
            out.println(toCSV(error));
            out.print("Mean Points, ");
            out.println(toCSV(points));

            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //for hierarchical
    public Store(ArrayList<Double> win, ArrayList<Double> points, ArrayList<Double> error, AIHandler ai, int accum, ArrayList<Double> error1, ArrayList<Double> error2, ArrayList<Double> error3) {
        if (OS == null) OS = System.getProperty("os.name").toLowerCase();
        if (isWindows()) dir = System.getProperty("user.dir") + "\\..\\results\\images\\" + ai.toString() + "\\";
        if (isUnix()) dir = System.getProperty("user.dir") + "/../results/images/" + ai.toString() + "/";


        try {
            File f = new File(dir);
            f.mkdirs();
            FileOutputStream stream = new FileOutputStream(dir + "accum[" + accum + "]" + ".data");
            ObjectOutput s = new ObjectOutputStream(stream);
            s.writeObject(win);
            s.writeObject(error);
            s.writeObject(points);
            s.close();

            PrintWriter out = new PrintWriter(dir + ai.getGenerationError().size() + ".csv");
            out.print("Mean Winrate, ");
            out.println(toCSV(win));
            out.print("Mean Error network 0, ");
            out.println(toCSV(error));
            out.print("Mean Error network 1, ");
            out.println(toCSV((error1)));
            out.print("Mean Error network 2, ");
            out.println(toCSV((error2)));
            out.print("Mean Error network 3, ");
            out.println(toCSV((error3)));
            out.print("Mean Points, ");
            out.println(toCSV(points));

            out.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Store(ArrayList<Double> win, ArrayList<Double> points, ArrayList<Double> error, AIHandler ai, String string) {

        if (OS == null) OS = System.getProperty("os.name").toLowerCase();
        if (isWindows()) dir = System.getProperty("user.dir") + "\\..\\results\\images\\" + ai.toString() + "\\";
        if (isUnix()) dir = System.getProperty("user.dir") + "/../results/images/" + ai.toString() + "/";


        try {
            File f = new File(dir);
            f.mkdirs();
            FileOutputStream stream = new FileOutputStream(dir + string + ".data");
            ObjectOutput s = new ObjectOutputStream(stream);
            s.writeObject(win);
            s.writeObject(error);
            s.writeObject(points);
            s.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Store(JFrame frame, String title) {
        if (OS == null) OS = System.getProperty("os.name").toLowerCase();
        if (isWindows()) dir = System.getProperty("user.dir") + "\\..\\results\\images\\" + title + "\\";
        if (isUnix()) dir = System.getProperty("user.dir") + "/../results/images/" + title + "/";


        Container c = frame.getContentPane();
        BufferedImage im = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
        c.print(im.getGraphics());
        try {
            File f = new File(dir + frame.getTitle() + ".png");
            f.mkdirs();
            ImageIO.write(im, "PNG", f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isWindows() {

        return (OS.indexOf("win") >= 0);

    }

    public static boolean isMac() {

        return (OS.indexOf("mac") >= 0);

    }

    public static boolean isUnix() {

        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);

    }


    //store parameters in textfile
    public Store(AIHandler ai, int roundTime, NNSettings settings) {
        if (OS == null) OS = System.getProperty("os.name").toLowerCase();
        if (isWindows()) dir = System.getProperty("user.dir") + "\\..\\results\\images\\" + ai.toString() + "\\";
        if (isUnix()) dir = System.getProperty("user.dir") + "/../results/images/" + ai.toString() + "/";

        try {
            File f = new File(dir);
            f.mkdirs();


            PrintWriter out = new PrintWriter(dir + "extra_parameters.txt");
            out.print("Round time in ms: ");
            out.println(roundTime);

            if(ai.toString().contains("Hierarchical")) {


                /*
                out.print("Rewards pathfinding strategy (death, kill, wall, move): ");
                out.println(((HierarchicalAIErrorDriven)ai).DEATH_REWARD_PATHFINDING + "," + ((HierarchicalAIErrorDriven)ai).KILL_REWARD_PATHFINDING + "," + ((HierarchicalAIErrorDriven)ai).WALL_REWARD_PATHFINDING + "," + ((HierarchicalAIErrorDriven)ai).MOVE_REWARD_PATHFINDING );

                out.print("Rewards attacking strategy (death, kill, wall, move): ");
                out.println(((HierarchicalAIErrorDriven)ai).DEATH_REWARD_ATTACKING + "," + ((HierarchicalAIErrorDriven)ai).KILL_REWARD_ATTACKING + "," + ((HierarchicalAIErrorDriven)ai).WALL_REWARD_ATTACKING + "," + ((HierarchicalAIErrorDriven)ai).MOVE_REWARD_ATTACKING );
                 */

                out.print("Rewards pathfinding strategy (kill, wall): ");
                out.println(ai.getWorld().KILL_REWARD_PATHFINDING + "," + ai.getWorld().WALL_REWARD_PATHFINDING);

                out.print("Rewards attacking strategy (kill, wall,): ");
                out.println((ai.getWorld().KILL_REWARD_ATTACKING + "," + ai.getWorld().WALL_REWARD_ATTACKING ));
            }

            out.print("Discount rate: ");
            out.println(settings.getDiscount());

            out.print("Exploration rate: ");
            out.println(settings.getExplorationRate());

            out.print("Learning rate: ");
            out.println(settings.getLearningRate());

            out.print("Amount of hidden nodes: ");
            out.println(settings.getWeigths()[0]); // get hidden layer only

            out.print("Activation functions (0=linear, 1=sigmoid): ");
            out.print(settings.getFunctions()[0]);
            out.print(", ");
            out.println(settings.getFunctions()[1]);

                out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

}

}
