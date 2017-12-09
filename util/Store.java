package util;

import AI.AIHandler;
import AI.NeuralNetworkAIFullInput;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
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
        try {
            File f = new File(dir);
            f.mkdirs();
            FileOutputStream stream = new FileOutputStream(dir + ai.getGenerationError().size() + ".nn"); //filename is the generation number
            ObjectOutput s = new ObjectOutputStream(stream);
            s.writeObject(ai.getClass());
            s.writeObject(ai.getActivationVectorlist());
            s.close();
            stream = new FileOutputStream(dir + ai.getGenerationError().size() + ".data");
            s = new ObjectOutputStream(stream);
            s.writeObject(ai.getWinrate());
            s.writeObject(ai.getError());
            s.writeObject(ai.getGenerationPoints());
            s.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Store(ArrayList<Double> win,ArrayList<Double> points,ArrayList<Double> error,AIHandler ai,int accum) {
        if(OS==null) OS = System.getProperty("os.name").toLowerCase();
        if(isWindows()) dir = System.getProperty("user.dir") + "\\..\\results\\images\\" + ai.toString() + "\\";
        if(isUnix()) dir = System.getProperty("user.dir") + "/../results/images/" + ai.toString() + "/";

        try {
            File f = new File(dir);
            f.mkdirs();
            FileOutputStream stream = new FileOutputStream(dir + "accum[" + accum +"]"+".data");
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
