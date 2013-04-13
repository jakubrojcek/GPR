import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: Jakub
 * Date: 13.9.12
 * Time: 16:44
 * To change this template use File | Settings | File Templates.
 */
public class MultiplePayoff extends Payoff{

    private float[] p = new float[1];         // vector of payoff beliefs 13x SLO, SMO, 13x BLO, BMO, No order/cancellation
    private float max = 0.0f;                 // max payoff belief in the state
    private short maxIndex;                    // index of max payoff belief in the state
    private short[] n = new short[1];         // occurrences of action in the state for alpha
    private ArrayList<Short> Actions = new ArrayList<Short>(1);  // actions for n's
    private double EventTime = 0.0;           // event time
    private boolean fromPreviousRound = false;// set to true when you move to the next round
    static int nSize;                         // current size of n array, for copying to +1 bigger
    static int nIndex;                        // holder for index of n, for updating ++
    private double diff = 0.0;                //TODO: delete afterwards
    private int nDiff = 0;                    //TODO: delete afterwards
    private int printIndex = 0;               //TODO: delete afterwards

    public MultiplePayoff(float[] payoffs, double et, SinglePayoff sp){
        diff = 0.0;
        payoffs = new float[nPayoffs];
        for (int j = 0; j < nPayoffs; j++){                //TODO: delete this part after testing
            payoffs[j] = (float)Math.random() + 0.05f;
            diff = diff + payoffs[j];
        }
        this.EventTime = et;    // time when new action for the Payoff is chosen
        max = payoffs[0];//sp.getMax();
        maxIndex = 0;//sp.getMaxIndex();
        p[0] = max;
        Actions.add(maxIndex);

        for(byte i = 0; i < nPayoffs; i++){ // searching for best payoff
            /*if (i == maxIndex){
                payoffs[i] = sp.getMax();
            }*/
            if(payoffs[i] > max){
                max = payoffs[i];
                maxIndex = i;
            }
        }

        if (maxIndex == sp.getMaxIndex()){
            n[0] = 2;
        } else{
            n = new short[2];
            n[0] = 1;
            n[1] = 1;
            float pOld = p[0];
            p = new float[2];
            p[0] = pOld;
            p[1] = max;
            Actions.add(maxIndex);
        }
    }

    // update old state upon return of a trader whose previous state is captured in this MultiplePayoff
    public void updateMax(float[] payoffs, double et, boolean tremble){ // overloading update method MP happens more times
        diff = 0.0;
        payoffs = new float[nPayoffs];
        for (int j = 0; j < nPayoffs; j++){                //TODO: delete this part after testing
            payoffs[j] = (float)Math.random() + 0.05f;
            diff = diff + payoffs[j];
        }
        for (byte i = 0; i < nPayoffs; i++){
            /*if (Actions.contains(i)){
                payoffs[i] = p[Actions.indexOf(i)];
            }*/
            if(payoffs[i] > max){
                max = payoffs[i];
                maxIndex = i;
            }
        }
        if (tremble){
            maxIndex = (byte) (Math.random() * payoffs.length);
            max = payoffs[maxIndex];
        }
        if(!Actions.contains(maxIndex)){
            nSize = Actions.size();
            short[] nNew = new short[nSize + 1];
            System.arraycopy(n, 0, nNew, 0, nSize);
            n = nNew;
            n[nSize] = 1;

            p = Arrays.copyOf(p, nSize + 1);
            p[nSize] = max;

            Actions.add(maxIndex);
            //System.out.println("occurrences " + n[nIndex] + " size " + nSize);
        } else {
            /*System.out.println("maxIndex: " + maxIndex + "  actions size: " + Actions.size()
            + "  n.length: " + n.length);*/
            //System.out.println("maxIndex:" + maxIndex + " max: " + max);
            nIndex = Actions.indexOf(maxIndex);
            if (n[nIndex] < nResetMax){
                n[Actions.indexOf(maxIndex)]++;
            }
        }
        EventTime = et;
        fromPreviousRound = false;
    }

    // updates payoff from the oldAction upon execution
    public void update(short oldAction, double payoff, double et){
        // after the new belief is computed, it comes back to payoff vector, max, maxIndex is updated
        boolean mo;
        /*if (oldAction == 7){
            System.out.println(mo = (payoff==p[Actions.indexOf(oldAction)]));
        }
        if (oldAction == 15){
            System.out.println(mo = (payoff==p[Actions.indexOf(oldAction)]));
        }*/
        nIndex = Actions.indexOf(oldAction);
        /*if(n[nIndex] > 20){
            System.out.println("average diff: " + (double)(diff)/n[nIndex]);
        }*/

        double alpha = (1.0/(1 + n[nIndex]));  // updating factor
        //System.out.println(p[nIndex] + " before " + payoff +  " now " + (payoff - p[nIndex]) + " difference");
        double previous = p[nIndex];
        //p[nIndex] = (float) ((1.0 - alpha) * p[nIndex] +
        //        + alpha * Math.exp( - rho * (et - EventTime)) * payoff); // TODO: check the values produced
        //diff = p[nIndex];
        //System.out.println("diff MP: " + (p[nIndex] - previous));
    }

    public String printDiff(int t2){
        String s = new String();
        int len = n.length;
        int index = 0;
        for (int i = 0; i < len; i++){
            if (n[i] > n[index]){
                index = i;
            }
        }
        if (n[index] > t2 && index == printIndex){
            dof++;
            s = (n[index] - nDiff) + ";";
            s = s + (p[index] - diff) + ";";
            s = s + dof + "\r";
            nDiff = n[index];
            diff = p[index];
            printIndex = index; //TODO: delete afterwards
        } else {
            nDiff = n[index];
            diff = p[index];
            printIndex = index; //TODO: delete afterwards
        }
        return s;
    }

    public int getN(){
        int sum = 0;
        for (short s : n){
            sum += s;
        }
        return sum;
    }

    public boolean canBeDeleted(){
        return fromPreviousRound;
    }

    public void setFromPreviousRound(boolean fpr){
        fromPreviousRound = fpr;
    }

    public void nReset(){
        int len = n.length;
        for (int i = 0; i < len; i++){
            n[i] = (short) Math.min(n[i], nReset); //TODO: check if comparing short and byte OK
        }
    }

    public float[] getP() {
        return p;
    }

    public short [] getNarray(){
        return n;
    }

    public double getDiff(){
        return diff;
    }

    public double getMax(){
        return max;
    }
    public short getMaxIndex(){
        return maxIndex;
    }
}

