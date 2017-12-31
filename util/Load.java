package util;

import AI.TimeDrivenBoltzmanNNFullInput;
import MLP.ActivationVectorList;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created by joseph on 10/04/2017.
 */
public class Load {
   public double[][][] Load(){
        try {
            final JFileChooser chooser=new JFileChooser(System.getProperty("user.dir")+"\\..\\results\\images");
            final int returnVal = chooser.showOpenDialog(chooser);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(chooser.getSelectedFile().getAbsolutePath()));
                ois.readObject();
                ActivationVectorList ai = (ActivationVectorList) ois.readObject();
                return ai.getWeigths(); //only returns the weights
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

       System.err.println("cannot find specified file");
       return new double[][][]{};
       }

    //loads a single network file for hierarchical
    public ActivationVectorList loadHierarchical() {
        try {
            final JFileChooser chooser = new JFileChooser(System.getProperty("user.dir") + "\\..\\results\\images");
            final int returnVal = chooser.showOpenDialog(chooser);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(chooser.getSelectedFile().getAbsolutePath()));

                //ois.readObject();
                ActivationVectorList loadedNetwork = (ActivationVectorList) ois.readObject();
                ois.close();

                return loadedNetwork;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        System.err.println("cannot find specified file");
        return null;
    }

}
