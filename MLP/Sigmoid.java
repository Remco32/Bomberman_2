package MLP;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

import java.util.Arrays;

/**
 * Created by joseph on 20/03/2017.
 */
public class Sigmoid extends AbstractActivationFunction {
    @Override
    public RealVector ActivationFunction(RealVector activationLayer) {
        double[] activation = activationLayer.toArray();
        RealVector returnVector = MatrixUtils.createRealVector(new double[]{});

        for (int idx = 0; idx < activation.length; idx++) {
            double sigmoidActivation = 1.0 / (1 + Math.exp(-activation[idx]));
            returnVector = returnVector.append(sigmoidActivation);
        }
        return returnVector;
    }

    @Override
    public RealVector DerivativeActivationFunction(RealVector outputActivation, int size) {
        double[] oneArray = new double[size];
        Arrays.fill(oneArray, 1);
        RealVector oneVector = MatrixUtils.createRealVector(oneArray);
        RealVector dOut_dNet = outputActivation.ebeMultiply(oneVector.subtract(outputActivation)); // element by element multiplication
        return dOut_dNet;
    }

}
