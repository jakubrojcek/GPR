import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: rojcek
 * Date: 21.02.13
 * Time: 10:53
 * To change this template use File | Settings | File Templates.
 */
public class GPR2005Payoff extends Payoff {
    private float max = 0.0f;
    private Short maxIndex = 0;
    private double diff;

    private IdentityHashMap<Short, Belief> x1 = new IdentityHashMap<Short, Belief>();
    // action, belief- number times the action chose, execution probability, fv change upon execution
    private IdentityHashMap<Short, Belief> x2 = new IdentityHashMap<Short, Belief>();
    // second available share action, belief


    public GPR2005Payoff(float[] payoffs, int Bid, int Ask) {
        short b = (short) Math.max(Bid + 1, 0);             // + 1 in order to start from one above B
        b = (short) Math.min(end, b); // TODO: test this change
        short a = (short) Math.min(Ask + end, 2 * end);
        a = (short) Math.max(end, a);
        max = payoffs[b];
        maxIndex = b;
        for(short i = b; i < a; i++){            // searching for best payoff and not marketable LO
            if(payoffs[i] > max){
                max = payoffs[i];
                maxIndex = i;
            }
        }
        if(payoffs[2 * end] > max){              // trying SMO
            max = payoffs[2 * end];
            maxIndex = (short) (2 * end);
        }
        if(payoffs[2 * end + 1] > max){          // trying BMO
            max = payoffs[2 * end + 1];
            maxIndex = (short) (2 * end + 1);
        }
        if(payoffs[2 * end + 2] > max){          // trying NO
            max = payoffs[2 * end + 2];
            maxIndex = (short) (2 * end + 2);
        }

        x1.put(maxIndex, new Belief((short) 1, mu0[maxIndex], deltaV0[maxIndex]));
    }

    public void updateMax(float[] payoffs, int Bid, int Ask, boolean tremble){ // overloading update method MP happens more times
        short b = (short) Math.max(Bid + 1, 0);             // + 1 in order to start from one above B
        b = (short) Math.min(end, b);
        short a = (short) Math.min(Ask + end, 2 * end);
        a = (short) Math.max(end, a);
        max = payoffs[b];
        maxIndex = b;
        for(short i = b; i < a; i++){            // searching for the best payoff and not marketable LO
            if(payoffs[i] > max){
                max = payoffs[i];
                maxIndex = i;
            }
        }

        if(payoffs[2 * end] > max){              // trying SMO
            max = payoffs[2 * end];
            maxIndex = (short) (2 * end);
        }
        if(payoffs[2 * end + 1] > max){          // trying BMO
            max = payoffs[2 * end + 1];
            maxIndex = (short) (2 * end + 1);
        }
        if(payoffs[2 * end + 2] > max){          // trying NO
            max = payoffs[2 * end + 2];
            maxIndex = (short) (2 * end + 2);
        }
        if (tremble){
            short rIndex = (short) (Math.random() * payoffs.length);
            maxIndex = ((rIndex >= b && rIndex <a) || (rIndex >= 2 * end)) ? rIndex
                                                                          : maxIndex;
            max = payoffs[maxIndex];
        }
        if(!x1.containsKey(maxIndex)){
            x1.put(maxIndex, new Belief((short) 1, mu0[maxIndex], deltaV0[maxIndex]));   // TODO: this part works?
        } else if(x1.get(maxIndex).getN() < nResetMax) {
            x1.get(maxIndex).increaseN();
        }
        //System.out.println("updating here");
    }

    public void update(short oldAction, float realDelta, boolean cancelled){   // TODO: this part works?
        double alpha = (1.0/(1 + x1.get(oldAction).getN()));  // updating factor
        double previousMu = x1.get(oldAction).getMu();
        //double previousDeltaV = x1.get(oldAction).getDeltaV();
        //System.out.println(p[nIndex] + " before " + payoff +  " now " + (payoff - p[nIndex]) + " difference");
        if (cancelled){
            x1.get(oldAction).setMu((float) ((1.0 - alpha) * previousMu));
        } else {
            x1.get(oldAction).setMu((float) ((1.0 - alpha) * previousMu + alpha));
            x1.get(oldAction).setDeltaV((float) ((1.0 - alpha) * x1.get(oldAction).getDeltaV() +
                    alpha * realDelta));
        }
        diff = x1.get(oldAction).getMu() - previousMu;

    }

    public void nReset(){
        Iterator it = x1.keySet().iterator();
        while (it.hasNext()){
            x1.get(it.next()).setN(nReset);
        }
    }

    public short getMaxIndex(){
        return maxIndex;
    }

    public double getDiff() {
        return diff;
    }

    public IdentityHashMap<Short, Belief> getX1() {
        return x1;
    }

    public IdentityHashMap<Short, Belief> getX2() {
        return x2;
    }
}
