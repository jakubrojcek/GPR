/**
 * Created with IntelliJ IDEA.
 * User: Jakub
 * Date: 13.9.12
 * Time: 16:42
 * To change this template use File | Settings | File Templates.
 */
public class SinglePayoff extends Payoff{
    private float max;
    private byte maxIndex;
    private double EventTime;
    private boolean fromPreviousRound = false; // set to true when you move to the next round

    public SinglePayoff(float[] payoffs, double et){
        payoffs = new float[nPayoffs];
        for (int j = 0; j < nPayoffs; j++){                //TODO: delete this part after testing
            payoffs[j] = (float)Math.random() + 0.05f;
        }
        this.EventTime = et;
        max = payoffs[0];
        maxIndex = 0;
        for(byte i = 1; i < nPayoffs; i++){
            if(payoffs[i] > max){
                max = payoffs[i];
                maxIndex = i;
            }
        }
        //System.out.println("maxIndex: " + maxIndex + " max: " + max);
    }

    public void update(short oldAction, double payoff, double et){ //only if executed-> can be only once
        //max = (float) (0.5 * max + 0.5 * Math.exp( - rho * (et - EventTime)) * payoff);
        //System.out.println("maxIndex: " + maxIndex + " max: " + max);
        //System.out.println("diff SP: " + (max - previous));
    }

    public boolean canBeDeleted(){
        return fromPreviousRound;
    }

    public void setFromPreviousRound(boolean fpr){
        fromPreviousRound = fpr;
    }

    public void nReset(){

    }

    public float getMax(){
        return max;
    }
    public byte getMaxIndex(){
        return maxIndex;
    }

    public double getEventTime(){
        return EventTime;
    }
}
