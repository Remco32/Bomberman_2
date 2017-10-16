package MLP;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

import java.util.Arrays;

/**
 * Created by joseph on 20/03/2017.
 */
public class LinearOutput extends AbstractActivationFunction {
    @Override
    public RealVector ActivationFunction(RealVector activationLayer) {
        return activationLayer;
    }

    @Override
    public RealVector DerivativeActivationFunction(RealVector outputActivation, int size) {
        double[] oneArray = new double[size];
        Arrays.fill(oneArray, 1);
        return MatrixUtils.createRealVector(oneArray);
    }
}
