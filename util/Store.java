package util;

import AI.AIHandler;
import AI.NeuralNetworkAIFullInput;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.Instant;
import java.util.ArrayList;

/**
 * Created by joseph on 10/04/2017.
 */
public class Store {
    private static String OS =null;
    String dir = System.getProperty("user.dir");


    public Store(AIHandler ai) {

        if(OS==null) OS = System.getProperty("os.name").toLowerCase();
        if(isWindows()) dir = System.getProperty("user.dir") + "\\..\\results\\images\\" + ai.toString() + "\\";
        if(isUnix()) dir = System.getProperty("user.dir") + "/../results/images/" + ai.toString() + "/";
        String timestamp = Instant.now().toString();
        timestamp = timestamp.replace(":", "-");
        dir = dir.concat(" ");
        dir = dir.concat(timestamp);
        try {
            File f = new File(dir);
            f.mkdirs();
            //stores class itself + its neural net
            FileOutputStream stream = new FileOutputStream(dir + ai.getGenerationError().size() + ".nn"); //filename is the generation number
            ObjectOutput s = new ObjectOutputStream(stream);
            s.writeObject(ai.getClass());
            s.writeObject(ai.getActivationVectorlist());
            s.close();
            //Stores winrate, error, generationPoints
            stream = new FileOutputStream(dir + ai.getGenerationError().size() + ".data");
            s = new ObjectOutputStream(stream);
            s.writeObject(ai.getWinrate()); //are all arrays, so multiple values
            s.writeObject(ai.getError());
            s.writeObject(ai.getGenerationPoints());
            s.close();

            /**
            stream = new FileOutputStream(dir + ai.getGenerationError().size() + ".csv");
            s = new ObjectOutputStream(stream);
            s.writeObject(toCSV(ai.getWinrate())); //are all arrays, so multiple values
            //s.writeObject(System.lineSeparator());
            //s.writeObject(toCSV(ai.getError()));
            //s.writeObject(toCSV(ai.getGenerationPoints()));
            s.close();
             **/

        /**
            PrintWriter out = new PrintWriter(dir + ai.getGenerationError().size() + ".csv");
            out.print("Winrate: ");
            out.println(toCSV(ai.getWinrate()));
            out.print("Error: ");
            out.println(toCSV(ai.getError()));

            out.close();
**/





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

    public Store(ArrayList<Double> win,ArrayList<Double> points,ArrayList<Double> error,AIHandler ai,int accum) {
        if(OS==null) OS = System.getProperty("os.name").toLowerCase();
        if(isWindows()) dir = System.getProperty("user.dir") + "\\..\\results\\images\\" + ai.toString() + "\\";
        if(isUnix()) dir = System.getProperty("user.dir") + "/../results/images/" + ai.toString() + "/";

        String timestamp = Instant.now().toString();
        timestamp = timestamp.replace(":", "-");
        dir = dir.concat(" ");
        dir = dir.concat(timestamp);

        try {
            File f = new File(dir);
            f.mkdirs();
            FileOutputStream stream = new FileOutputStream(dir + "accum[" + accum +"]"+".data");
            ObjectOutput s = new ObjectOutputStream(stream);
            s.writeObject(win);
            s.writeObject(error);
            s.writeObject(points);
            s.close();

            PrintWriter out = new PrintWriter(dir + ai.getGenerationError().size() + ".csv");
            out.print("Mean Winrate: ");
            out.println(toCSV(win));
            out.print("Mean Error: ");
            out.println(toCSV(error));
            out.print("Mean Points: ");
            out.println(toCSV(points));
            out.close();



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Store(ArrayList<Double> win,ArrayList<Double> points,ArrayList<Double> error,AIHandler ai,String string) {

        if(OS==null) OS = System.getProperty("os.name").toLowerCase();
        if(isWindows()) dir = System.getProperty("user.dir") + "\\..\\results\\images\\" + ai.toString() + "\\";
        if(isUnix()) dir = System.getProperty("user.dir") + "/../results/images/" + ai.toString() + "/";

        try {
            File f = new File(dir);
            f.mkdirs();
            FileOutputStream stream = new FileOutputStream(dir + string +".data");
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
        if(OS==null) OS = System.getProperty("os.name").toLowerCase();
        if(isWindows()) dir = System.getProperty("user.dir") + "\\..\\results\\images\\" + title + "\\";
        if(isUnix()) dir = System.getProperty("user.dir") + "/../results/images/" + title + "/";

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

        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );

    }
}
