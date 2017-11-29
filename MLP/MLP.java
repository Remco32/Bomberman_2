package MLP;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static org.apache.commons.math3.util.FastMath.pow;

/**
 * Created by joseph on 8-3-2017.
 */
public class MLP {
    public static final int LINEAR = 0;
    public static final int SIGMOID = 1;
    public static final int LeakyRELU = 2;

    boolean print=true;

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    double learningRate;
    public static void main(String[] args) {
        MLP mlp = new MLP(0.1,false);

        double[][] input = {{0, 0}, {0, 1}, {1, 0}, {1, 1}};
        double[][] target = {{0,1,0.5}, {1,0,0.3}, {1,0,0.7}, {0,1,0.1}};
        double[][][] weigths = //{CreateRandomWeights(3, 2), CreateRandomWeights(3, 3),CreateRandomWeights(4,1)};
                mlp.CreateWeights(new int[] {40,target[0].length},2);
        ArrayList<AbstractActivationFunction> activationFunctionsList = new ArrayList<>();
        activationFunctionsList.add(new LeakyRelu());
        activationFunctionsList.add(new LinearOutput());

        //activationFunctionsList shuld have the same size as weights.size
        ActivationVectorList activationClass = new ActivationVectorList(weigths,activationFunctionsList);

        double error=0;
        for (int epoch = 0; epoch < 20000; epoch++) {
            error = 0;
            for (int x = 0; x < 4; x++) {
              // System.out.println("weigths:" + Arrays.deepToString(weigths));
                activationClass = mlp.forwardPass(input[x],activationClass);
               System.out.println("input:" + Arrays.toString(input[x]) + " output: " + activationClass.getVectorOfLayer(activationClass.getVectorListSize()-1));
                activationClass = mlp.BackwardPass(activationClass, target[x]);
                error += mlp.TotalError(activationClass.getVectorOfLayer(activationClass.getVectorListSize()-1).toArray(), target[x]);
            }

        }
        System.out.println("Error = :" + error);
    }
    public MLP(double learningRate, boolean print){
        this.print=print;
        this.learningRate = learningRate;
    }

    public MLP(double learningRate){
        this.print=false;
        this.learningRate = learningRate;
    }

    //inputValues is a vector representation of the gamestate
    //inputVectorList is a neural net with node values, weights and corresponding activation functions.
    public ActivationVectorList forwardPass(double[] inputValues, ActivationVectorList inputVectorList) {

        ActivationVectorList returnVectorList = new ActivationVectorList(inputVectorList); //create a copy of the inputVectorList, but without the values inside the nodes.
        RealVector outputOfLayer = MatrixUtils.createRealVector(inputValues); //fill vector with inputvalues passed to the method
        outputOfLayer = outputOfLayer.append(1); // add the bias
        double[][][] weigths = returnVectorList.getWeigths(); //These are the original weights from the inputVectorList
        ArrayList<AbstractActivationFunction> activationFunctions = returnVectorList.getActivationFunctionList(); //These are the activation functions from the inputVectorList
        returnVectorList.setActivationList(new ArrayList<RealVector>());
        returnVectorList.AddActivationLayer(outputOfLayer); //set the node values of the returnVectorList

        for (int layer = 0; layer < weigths.length; layer++) { // go through all the layers

            RealMatrix weigthMatrixOfLayer = MatrixUtils.createRealMatrix(weigths[layer]); //put the weights of the corresponding layer in the matrix

            outputOfLayer = weigthMatrixOfLayer.preMultiply(outputOfLayer); //premultiply the weights with the layer nodes
            outputOfLayer = activationFunctions.get(layer).ActivationFunction(outputOfLayer); //Apply activation function to squash the results

            if (layer < weigths.length - 1) outputOfLayer = outputOfLayer.append(1); // add the bias
            returnVectorList.AddActivationLayer(outputOfLayer); //Add the results to the return list

        }

        return returnVectorList; //Return the resulting network
    }

   public ActivationVectorList BackwardPass(ActivationVectorList activationList, double[] target) { //gets called by updateWeights() in each AI class
        ArrayList<RealMatrix> deltaMatrixList = new ArrayList<>();
        double[][][] weigthList = activationList.getWeigths(); //get the weights from the

        int amountOfWeigthMatrixes = activationList.getWeigths().length; // the amount of weigthmatrixes that should be updated
        if(print)System.out.println("new round\n\n\n\n");

        ArrayList<AbstractActivationFunction> activationFunctions= activationList.getActivationFunctionList();


        deltaMatrixList.add(CalculateDeltaOutput(activationList, target,activationFunctions.get(activationFunctions.size()-1)));

        for (int layer = amountOfWeigthMatrixes - 2; layer >= 0; layer--) { // the output layer has allready been done
            if(print) System.out.println("\nhidden layer");
            int deltalistSize = deltaMatrixList.size() - 1;
            deltaMatrixList.add(CreateDeltaHidden(activationList, layer, deltaMatrixList.get(deltalistSize),activationFunctions.get(layer)));
        }

        int deltaListSize= deltaMatrixList.size() - 1;
        if(print) System.out.println("\nprintlayer");
        if(print)System.out.println("deltalistsize" + deltaListSize);
        for (int layer = 0; layer <= deltaListSize; layer++) {

            RealVector activationOfLayerVector = activationList.getVectorOfLayer(layer);
            RealMatrix deltaWeigths = CreateDeltaWeights(activationOfLayerVector, deltaMatrixList.get(deltaListSize-layer));//delta list has ben created in reverse

            RealMatrix weigths = MatrixUtils.createRealMatrix(activationList.getWeigthLayer(layer));
            if(print)System.out.println("deltaWeights: " + deltaWeigths);
            if(print)System.out.println("weights: " + weigths);
            deltaWeigths = deltaWeigths.scalarMultiply(learningRate);

            weigths = weigths.subtract(deltaWeigths);
            weigthList[layer] = weigths.getData();
        }
        activationList.setWeigths(weigthList);
        return activationList;
    }

    // calculate the Delta for the outputlayer
    RealMatrix CalculateDeltaOutput(ActivationVectorList activationList, double[] target,AbstractActivationFunction activationFunc) {
        int idx = activationList.getVectorListSize() - 1; // idx of the last element
        RealVector outputActivation = activationList.getVectorOfLayer(idx);

        RealVector targetVector = MatrixUtils.createRealVector(target);
        if(print)System.out.println("target:" + Arrays.toString(target));
        if(print)System.out.println("outputActivation:" + Arrays.toString(outputActivation.toArray()));

        int rowSize = activationList.getWeigthLayer(idx - 1).length; //there is always 1 less row of weights than of activation layers
        int columnSize = activationList.getWeigthLayer(idx - 1)[0].length;
        // returns the amount of rows in the weights
        // which is equal to the amount hidden nodes in the previous layer

        RealVector dTotal_dOut = outputActivation.subtract(targetVector);

        RealVector dOut_dNet = activationFunc.DerivativeActivationFunction(outputActivation,columnSize);

        RealMatrix delta = MatrixUtils.createRealMatrix(rowSize, dOut_dNet.getDimension());
        for (int row = 0; row < rowSize; row++) {
            //dTotal_dOut.ebeMultiply(MatrixUtils.createRealVector(new double[]{1}) in case of linear output
            delta.setRowVector(row, dOut_dNet.ebeMultiply(dTotal_dOut));
        }
        if(print)System.out.println("DeltaMatrixOutput" + delta);
        return delta;
    }

    RealMatrix CreateDeltaHidden(ActivationVectorList inputActivationVector, int layer, RealMatrix delta, AbstractActivationFunction activationFunc) {
        // should be the delta of layer+1
        double[][] OutGoingweights = inputActivationVector.getWeigthLayer(layer+1); // we want the weights of the previous layer
        int incomingWeightsRow = inputActivationVector.getWeigthLayer(layer).length;
        int incomingWeightsColumn = inputActivationVector.getWeigthLayer(layer)[0].length;

        RealVector outputActivation = inputActivationVector.getVectorOfLayer(layer+1); // the output has 1 extra layer
        RealVector dOut_dNet = activationFunc.DerivativeActivationFunction(outputActivation,outputActivation.getDimension());



        if(print)System.out.println("hiddenOutput" + Arrays.toString(outputActivation.toArray()));
        if(print)System.out.println("DOut-DNet" + Arrays.toString(dOut_dNet.toArray()));

        RealMatrix deltaReturnMatrix = MatrixUtils.createRealMatrix(incomingWeightsRow, incomingWeightsColumn);

        RealVector returnVectorDelta = MatrixUtils.createRealVector(new double[]{});
        if(print)System.out.println("weights " + Arrays.deepToString(OutGoingweights));


        //calculate DoutDTotal
        for (int column1 = 0; column1 < incomingWeightsColumn; column1++) {//every column(node) has its own delta
            RealVector rowVectorPreviousLayerDelta = delta.getRowVector(column1);
            RealVector columnWeigths = MatrixUtils.createRealVector(new double[]{});

            for (int column = 0; column < OutGoingweights[0].length; column++) { // for every column of the outgoing weights
                columnWeigths = columnWeigths.append(OutGoingweights[column1][column]);
            }

                  if(print)System.out.println("column weights" + Arrays.toString(columnWeigths.toArray()));
                  if(print)System.out.println("DeltaPrevious" + Arrays.toString(rowVectorPreviousLayerDelta.toArray()));

            double dTotal_dOut = rowVectorPreviousLayerDelta.dotProduct(columnWeigths);
            returnVectorDelta = returnVectorDelta.append(dOut_dNet.getEntry(column1) * dTotal_dOut);
        }

        if(print)System.out.println("returnVectorDeltaHidden" + Arrays.toString(returnVectorDelta.toArray()));


        for (int row = 0; row < incomingWeightsRow; row++) { // set the returnmatrix accordingly
            deltaReturnMatrix.setRow(row, returnVectorDelta.toArray());
        }
        if(print)System.out.println("ReturnDeltaMatrixHidden" + Arrays.deepToString(deltaReturnMatrix.getData()));
        if(print)System.out.println();
        if(print)System.out.println();
        return deltaReturnMatrix;
    }


    RealMatrix CreateDeltaWeights(RealVector inputActivationVector, RealMatrix delta) {
        int columnSize = delta.getColumnDimension();
        int rowSize = delta.getRowDimension();

        RealMatrix returnDeltaMatrix = MatrixUtils.createRealMatrix(rowSize, columnSize);
        if(print)System.out.println("InputActivation" + Arrays.toString(inputActivationVector.toArray()));
        if(print)System.out.println("DeltaColumn" + Arrays.toString(delta.getColumnVector(0).toArray()) + "\n\n");

        for (int column = 0; column < columnSize; column++) {
            RealVector deltaColumnVector = delta.getColumnVector(column);
            double[] inputByDeltaMulti = inputActivationVector.ebeMultiply(deltaColumnVector).toArray();
            returnDeltaMatrix.setColumn(column, inputByDeltaMulti);
        }
        return returnDeltaMatrix;
    }

    public double TotalError(double[] finalOutput, double[] target) {

        double totalError = 0;
        if (finalOutput.length != target.length) System.err.println("Target has wrong length");
        for (int idx = 0; idx < finalOutput.length; idx++) {
            totalError += 0.5 * pow((target[idx] - finalOutput[idx]), 2);
        }
        return totalError;
    }

    private double[][] CreateRandomWeights(int rows, int columns) {
        Random rnd = new Random();
        double[][] matrix = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                matrix[i][j] = rnd.nextDouble()-0.5;
            }
        }

        return matrix;
    }

    public double[][][] CreateWeights(int[] arrayOfHidenNodesLengths,int inputSize){
        ArrayList<double[][]> weightList = new ArrayList<>();
        if(arrayOfHidenNodesLengths.length==0) System.err.println("input to short");

        weightList.add(CreateRandomWeights(inputSize+1,arrayOfHidenNodesLengths[0])); // +1 for the bias

        for(int x=0;x<arrayOfHidenNodesLengths.length-1;x++){
            weightList.add(CreateRandomWeights(arrayOfHidenNodesLengths[x]+1,arrayOfHidenNodesLengths[x+1]));
        }
        double[][][] weights = new double[weightList.size()][][];
        for(int x=0;x<weightList.size();x++) weights[x] = weightList.get(x);
        return weights;
    }

    public ArrayList<AbstractActivationFunction> CreateActivationFunctionList(int[] functions){

        ArrayList<AbstractActivationFunction> activationFunction = new ArrayList<>();

        for(int temp:functions){
            if(temp==0)activationFunction.add(new LinearOutput());
            if(temp==1)activationFunction.add(new Sigmoid());
            if(temp==2)activationFunction.add(new LeakyRelu());
        }
        return activationFunction;
    }

}
