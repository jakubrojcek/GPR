import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: rojcek
 * Date: 21.02.13
 * Time: 10:53
 * To change this template use File | Settings | File Templates.
 */
public class GPR2005Payoff_test extends Payoff {
    private float max = 0.0f;
    private Short maxIndex = 0;
    private double diff;
    private HashMap<Short, Belief[]> x;
    // action, belief- number times the action chose, execution probability, fv change upon execution

    public GPR2005Payoff_test(HashMap<Short, Float> hm, byte units2trade) {
        max = 0.0f;
        Iterator it = hm.keySet().iterator();
        while (it.hasNext()){
            Short s = (Short) it.next();
            if (hm.get(s) >= max){            // equal makes sure that NO is chosen at least
                max = hm.get(s);
                maxIndex = s;
            }
        }

        short[] ac =  new short[2];
        ac[0] = (short) (maxIndex>>7);
        ac[1] = (short) (maxIndex - (ac[0]<<7));
        Belief[] beliefs = new Belief[units2trade];
        if (ac[1] == 2 * end + 3){             // SMO at second best Bid
            ac[1] = (short)(2 * end);
        } else if (ac[1] == 2 * end + 4){      // BMO at second best Ask
            ac[1] = (short)(2 * end + 1);
        }
        for (int i = 0; i < units2trade; i++){
            beliefs[i] = new Belief((short) 1, mu0[ac[i]], deltaV0[ac[i]]);
        }
        x = new HashMap<Short, Belief[]>();
        x.put(maxIndex, beliefs);
    }

    public void updateMax(HashMap<Short, Float> hm, byte units2trade, boolean tremble){ // overloading update method MP happens more times
        max = 0.0f;
        Iterator it = hm.keySet().iterator();
        while (it.hasNext()){
            Short s = (Short) it.next();
            if (hm.get(s) >= max){            // equal makes sure that NO is chosen at least
                max = hm.get(s);
                maxIndex = s;
            }
        }

        if (tremble){
            List<Short> keys = new ArrayList<Short>(hm.keySet());
            maxIndex = keys.get(random.nextInt(keys.size()));        // TODO: make sure
            max = hm.get(maxIndex);
            if (!hm.containsKey(maxIndex)){
                System.out.println("Key not in the HashMap");
            }
        }

        short[] ac =  new short[2];
        ac[0] = (short) (maxIndex>>7);
        ac[1] = (short) (maxIndex - (ac[0]<<7));

        if(!x.containsKey(maxIndex)){
            Belief[] beliefs = new Belief[units2trade];
            if (ac[1] == 2 * end + 3){             // SMO at second best Bid
                ac[1] = (short)(2 * end);
            } else if (ac[1] == 2 * end + 4){      // BMO at second best Ask
                ac[1] = (short)(2 * end + 1);
            }
            for (int i = 0; i < units2trade; i++){
                beliefs[i] = new Belief((short) 1, mu0[ac[i]], deltaV0[ac[i]]);
            }
            x.put(maxIndex, beliefs);
        } else if(x.get(maxIndex)[0].getN() < nResetMax) {
            for (int i = 0; i < units2trade; i++){
                x.get(maxIndex)[i].increaseN();
            }
        }
    }

    public double update(short oldAction, float realDelta, boolean cancelled, byte unitTraded){   // TODO: this part works?
    // TODO: unitTraded 0- first action, 1- second action

        Belief[] b = x.get(oldAction);
        double alpha = (1.0/(1 + (x.get(oldAction))[unitTraded].getN()));  // updating factor
        double previousMu = x.get(oldAction)[unitTraded].getMu();

        if (cancelled){
            x.get(oldAction)[unitTraded].setMu((float) ((1.0 - alpha) * previousMu));
        } else {
            x.get(oldAction)[unitTraded].setMu((float) ((1.0 - alpha) * previousMu + alpha));
            x.get(oldAction)[unitTraded].setDeltaV((float) ((1.0 - alpha) * x.get(oldAction)[unitTraded].getDeltaV() +
                    alpha * realDelta));
        }

        diff = Math.abs(x.get(oldAction)[unitTraded].getMu() - previousMu);
        if (unitTraded == 0){
            diff = 0.0;
        }
        return diff;
    }

    public void nReset(){
        Iterator it = x.keySet().iterator();
        short[] ac =  new short[2];

        while (it.hasNext()){
            int bound = 1;
            short key = (Short) it.next();
            ac[0] = (short) (key>>7);
            ac[1] = (short) (key - (ac[0]<<7));
            if (ac[1] != 127){bound = 2;}
            for (int i = 0; i < bound; i++){
                x.get(key)[i].setN(nReset);
            }
        }
    }

    public short getMaxIndex(){
        return maxIndex;
    }

    public double getDiff() {
        return diff;
    }

    public HashMap<Short, Belief[]> getX() {
        return x;
    }

    public float getMax() {
        return max;
    }
}
