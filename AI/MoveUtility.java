package AI;

/**
 * Created by joseph on 17/02/2017.
 */
public class MoveUtility {
   private int move;
   private double utility;
   private double[] values;

    MoveUtility(int move,double utility){
        this.move = move;
        this.utility = utility;
        values=null;
    }

    MoveUtility(int move,double utility,double [] values){
        this.move = move;
        this.utility = utility;
        this.values = values;
    }

    public double getUtility() {
        return utility;
    }

    public int getMove() {
        return move;
    }

    public double[] getValues() {
        return values;
    }
}
