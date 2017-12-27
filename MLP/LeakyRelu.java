package MLP;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

/**
 * Created by joseph on 30-5-2017.
 */
public class LeakyRelu extends AbstractActivationFunction {


    @Override
    public RealVector ActivationFunction(RealVector activationLayer) {
        double[] activation = activationLayer.toArray();
        RealVector returnVector = MatrixUtils.createRealVector(new double[]{});

        for (int idx = 0; idx < activation.length; idx++) {
            returnVector = returnVector.append(Math.max(0.1*activation[idx],activation[idx]));
        }
        return returnVector;
    }

    @Override
    public RealVector DerivativeActivationFunction(RealVector outputActivation, int size) {
        RealVector returnVector = MatrixUtils.createRealVector(new double[]{});
        double[] activation = outputActivation.toArray();

        for(int idx=0;idx<size;idx++){
            returnVector = returnVector.append( ((activation[idx]>0)? 1.0:-0.1));
        }

        return returnVector;
    }
}
