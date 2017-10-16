package MLP;

import org.apache.commons.math3.linear.RealVector;

import java.io.Serializable;

/**
 * Created by joseph on 20/03/2017.
 */
public abstract class AbstractActivationFunction implements Serializable {
   public abstract RealVector ActivationFunction(RealVector activationLayer);
   public abstract RealVector DerivativeActivationFunction(RealVector outputActivation,int size);

}
