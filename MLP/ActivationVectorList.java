package MLP;

import org.apache.commons.math3.linear.RealVector;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by joseph on 8-3-2017.
 */
public class ActivationVectorList implements Serializable{
    private ArrayList<RealVector> activationList;
    private double[][][] weigths;
    private ArrayList<AbstractActivationFunction> activationFunctionList;

    public ActivationVectorList(double[][][] weigths, ArrayList<AbstractActivationFunction> activationFunctionList) {
        this.weigths = weigths;
        activationList = new ArrayList<>();
        this.activationFunctionList = activationFunctionList;
    }

    public ActivationVectorList(ActivationVectorList input){
        weigths = input.getWeigths().clone();
        activationList = new ArrayList<>();
        activationFunctionList = input.activationFunctionList;
    }
    void AddActivationLayer(RealVector activation) {
        activationList.add(activation);
    }

    RealVector getVectorOfLayer(int idx) {
        return activationList.get(idx);
    }

    int getVectorListSize() {
        return this.activationList.size();
    }

    public double[][][] getWeigths() {
        return weigths;
    }

    public ArrayList<AbstractActivationFunction> getActivationFunctionList() {
        return activationFunctionList;
    }

    public void AddActivationFunction(AbstractActivationFunction func) {
        activationFunctionList.add(func);
    }

    public double[][] getWeigthLayer(int idx) {
        return weigths[idx];
    }

    public void SetActivationFunctionList(ArrayList<AbstractActivationFunction> activationFunctionList) {
        this.activationFunctionList = activationFunctionList;
    }

    public void setWeigths(double[][][] weigths) {
        this.weigths = weigths;
    }

    public void setActivationList(ArrayList<RealVector> activationList) {
        this.activationList = activationList;
    }

   public double[] getOutput(){
        int size = activationList.size()-1;
        return (activationList.get(size)).toArray();
   }


}
