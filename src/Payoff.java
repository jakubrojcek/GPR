import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Jakub
 * Date: 29.5.12
 * Time: 22:48
 * To change this template use File | Settings | File Templates.
 */
public class Payoff {
    static float rho;                      // discount rate
    static int nPayoffs;                   // size of payoff vectors
    static byte nReset;                    // nReset-> resets n to specific value for "forced learning"
    static short nResetMax = 32767;        // nReset-> resets n to specific value for "forced learning"


    public Payoff(){}


    public void update(){

    }

    public boolean canBeDeleted(){
        return false;
    }

    public void setnReset(byte nSet, short mSet){
        nReset = nSet;
        nResetMax = mSet;
    }
    public void nReset(){}

    /*public float[] getP() {
        return p;
    }*/

    /*public void setP(float[] p) {
        this.p = p;
    }*/

    /*public void setMax(float max) {
        this.max = max;
    }*/

   /* public void setMaxIndex(byte maxIndex) {
        this.maxIndex = maxIndex;
    }

    public int getRecursiveStatesCount(){
        return recursiveStatesCount;
    }*/
}
