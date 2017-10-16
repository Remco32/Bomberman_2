package util;

import AI.AIHandler;
import MLP.ActivationVectorList;

import javax.swing.*;
import java.io.*;
import java.util.Arrays;

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
                return ai.getWeigths();
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

}
