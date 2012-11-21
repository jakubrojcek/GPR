import java.io.FileWriter;
import java.util.*;
import java.util.Vector;

import static java.lang.System.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jakub
 * Date: 22.3.12
 * Time: 19:24
 * To change this template use File | Settings | File Templates.
 */
public class Trader {
    private int traderID;               // initialized
    private float privateValue;         // initialized, can be changed to double later on
    private int pv;                     // holder for private value 0(0), 1(negative), 2(positive)
    private boolean isHFT;              // initialized
    private boolean isTraded = false;   // set by TransactionRule in book
    private boolean isReturning = false;// is he returning this time? info from priorities
    private long oldCode;               // oldCode holds old hash code for the last state in which he took action
    private byte oldAction;             // which action he took in the old state
    private float rho = 0.05f;          // trading "impatience" parameter
    private double PriceFV;             // current fundamental value-> price at middle position //TODO: should this be in the payoff?

    static int TraderCount = 0;         // counting number of traders, gives traderID as well
    static int tradeCount = 0;          // counting number of trader
    static int statesCount = 0;         // counting number of unique states
    static Hashtable<Long, Payoff> Payoffs = new Hashtable<Long, Payoff>();/* Beliefs about payoffs for different actions
    + max + maxIndex in state=code */
    static double ReturnFrequencyHFT;   // returning frequency of HFT
    static double ReturnFrequencyNonHFT;// returning frequency of NonHFT
    static Hashtable<Integer, Double[]> discountFactorB = new Hashtable<Integer, Double[]>();    // container for discount factors computed using tauB
    static Hashtable<Integer, Double[]> discountFactorS = new Hashtable<Integer, Double[]>();    // container for discount factors computed using tauS
    static int infoSize;                    // 2-bid, ask, 4-last price, direction, 6-depth at bid,ask, 8-depth off bid,ask
    static byte nP;                     // number of prices
    static double tickSize;             // size of one tick
    static int LL;                      // Lowest  allowed limit order price.  LL + HL = nP-1 for allowed orders centered around E(v)
    static int HL;                      // Highest allowed limit order price
    static int end;                     // HL - LL + 1 - number of LO prices on the grid
    static int maxDepth;                // 0 to 7 which matter
    static int breakPoint;              // breaking point for positive, negative, represents FV position on the LO grid
    static int nPayoffs;                // size of payoff vectors
    static int fvPos;                   // tick of fundamental value
    static int HTinit;                  // initial capacity for Payoffs HashTable
    static double prTremble = 0.0;      // probability of trembling
    static boolean write = false;              // to write things in trader?
    static String folder;
    static Vector<Decision> decisions;

    //private double CFEE;              // cancellation fee


    // constructor bool, bool, float, int
    public Trader(boolean HFT, float privateValue) {
        this.isHFT = HFT;
        this.privateValue = privateValue;
        TraderCount++;
        this.traderID = TraderCount;

        if (privateValue > 0.0001){pv = 2;}
        else if (privateValue < 0.0001){pv = 1;}
        else {pv = 0;}

    }

    public Trader(int is, double[] tb, double[] ts, byte numberPrices, int FVpos, double tickS, double rFast, double rSlow,
                  int ll, int hl, int e, int md, int bp, int hti, double pt, String f){
        infoSize = is;
        LL = ll;
        HL = hl;
        end = e;
        breakPoint = bp;
        HTinit = hti;
        nPayoffs = 2 * end + 3;
        for (int i = 0; i < end; i++){
            Double[] DiscountsB = new Double[16];
            Double[] DiscountsS = new Double[16];
            for (int j = 0; j < 16; j++){
                DiscountsB[j] = Math.exp(-rho * tb[i] * (j + 1));
                DiscountsS[j] = Math.exp(-rho * ts[i] * (j + 1));
            }
            discountFactorB.put(i, DiscountsB);
            discountFactorS.put(i, DiscountsS);
        }
        nP = numberPrices;
        ReturnFrequencyHFT = rFast;
        ReturnFrequencyNonHFT = rSlow;
        fvPos = FVpos;
        maxDepth = md;
        tickSize = tickS;
        prTremble = pt;
        folder = f;
        decisions = new Vector<Decision>();
    }

    // decision about the price is made here, so far random
    public PriceOrder decision(Hashtable<Byte, Byte> Priorities, int[] BookSizes, int[] BookInfo,
                               double EventTime, double priceFV){
        
        // positions , priorities table, & isReturning? 1 if yes, last transaction pricePosition
        // signed sizes vector, (best Bid, Ask), (depth at B, A), (depth Buy, Sell), (last pricePosition, WasBuy)
        // event time
        PriceFV = priceFV;             // get price of the fundamental value = fundamental value
        int pricePosition;             // pricePosition at which to submit
        boolean buyOrder = false;      // buy order?

        long Bt = BookInfo[0];         // Best Bid position
        long At = BookInfo[1];         // Best Ask position
        int P = Priorities.get(nP);    // pricePosition position in previous action
        int q = Priorities.get((byte)P);     // priority of the order at P
        int x = 0;                     // order was buy(2) or sell(1) or no order (0)
        if (isReturning){
            x = (BookSizes[P] > 0) ? 2 : 1;
        }
        Long code = HashCode(Priorities, BookSizes, BookInfo);
        /*if (isReturning){
            System.out.println("Trader ID " + traderID);
            System.out.println("new hash code " + Long.toBinaryString(code));
            System.out.println("old hash code " + Long.toBinaryString(oldCode));
            System.out.println("Occurred old code " + Occurrences.get(oldCode));
        }*/                      // printing code commented

        double continuationValue;
        byte action;

        float [] p = new float[nPayoffs];
        double sum = 0;                     // used to compute payoff to no-order
        for (int i = 0; i < end; i++){      // Limit order payoffs for ticks LL through HL
            p[i] = (float) (discountFactorB.get(i)[Math.abs(BookSizes[LL + i])] * ((i - breakPoint) * tickSize - privateValue));
            // SLOs
            p[i + end + 1] = (float) (discountFactorS.get(i)[Math.abs(BookSizes[LL + i])] * ((breakPoint - i) *
                    tickSize + privateValue));
            // BLOs

            double LOpayoff = (p[i] > p[i + end + 1]) ? p[i] : p[i + end + 1];
            // choose bigger one -> the one with positive payoff
            sum += LOpayoff;
        }

        // computing payoff from MO
        p[end] = (float)((Bt - fvPos) * tickSize - privateValue); // payoff to sell market order
        p[2 * end + 1] = (float)((fvPos - At) * tickSize + privateValue); // payoff to buy market order

        sum +=  (p[end] > p[2 * end + 1]) ? p[end] : p[2 * end + 1];

        if (P >= LL && P <= HL){  // previous action position is in the LO range
            int p25 = P - LL;
            if (x == 1){ // last LO was sell
                sum -= p[p25];
                p[p25] = (float)(discountFactorB.get(p25)[q] * ((p25 - breakPoint) * tickSize - privateValue));      //SLO
                sum += p[p25];
            } else {     // last LO was buy
                sum -= p[p25 + end + 1];
                p[p25 + end + 1] = (float)(discountFactorS.get(p25)[q] * ((breakPoint - p25) * tickSize + privateValue)); //BLO
                sum += p[p25 + end + 1];
            }
            /*System.out.println("Position is " + P + " p25 is " + p25 + " p[p25] is " + p[p25]
            + " p[p25 + 14] is " + p[p25 + 14] + " q- priority at P is " + q);*/
        }

        // computing payoff from no-order or cancellation //TODO: put CFEE, TIF here
        double Rt = (isHFT) ? 1.0 / ReturnFrequencyHFT
                : 1.0 / ReturnFrequencyNonHFT; // expected return time
        p[2 * end + 2] = (float) (Math.exp(-rho * (Rt)) * (2 * sum / (p.length - 1))); // 2 for averaging over 14
        /* no-order payoff is average of other actions payoffs, discounted by expected return time */

        if (!Payoffs.containsKey(code)){ // new state, new SinglePayoff created
            statesCount++;
            SinglePayoff pay = new SinglePayoff(p, EventTime, rho, nPayoffs);
            Payoffs.put(code, pay);      // insert a Payoff object made of SinglePayoff to the Payoffs table
            action = pay.getMaxIndex();
            continuationValue = pay.getMax();
        } else{
            Payoff pay = Payoffs.get(code);   // get from HashTable
            if (pay instanceof SinglePayoff){ //Transfer SinglePayoff to MultiplePayoff
                //statesCount++;
                MultiplePayoff pay2 = new MultiplePayoff(p, EventTime, (SinglePayoff) pay);//
                Payoffs.put(code, pay2);
                continuationValue = pay2.getMax();
                action = pay2.getMaxIndex();
            } else {                                                 // MultiplePayoff occurring again
                if (prTremble > 0 && Math.random() < prTremble){     // trembling comes here
                    ((MultiplePayoff) pay).updateMaxTremble(p, EventTime);
                } else{
                    ((MultiplePayoff) pay).updateMax(p, EventTime);
                }
                continuationValue = ((MultiplePayoff) pay).getMax();
                action = ((MultiplePayoff) pay).getMaxIndex();
            }
        }
        // update old-state beliefs
        if (isReturning){
            if (Payoffs.containsKey(oldCode)){
                Payoff pay = Payoffs.get(oldCode);
                if (pay instanceof SinglePayoff){ // updating old SinglePayoff
                    ((SinglePayoff) pay).update(oldAction, continuationValue, EventTime);
                } else {                          // updating old MultiplePayoff
                    ((MultiplePayoff) pay).update(oldAction, continuationValue, EventTime);
                }
            }
        } else {isReturning = true;} // sets to true even if hasn't submitted LO/MO

        if (action > end){
            buyOrder = true;
        }

        pricePosition = (action < end) ? LL + action : LL + action - end - 1;
        if (action == end){
            pricePosition = BookInfo[0];}            // position is Bid
        if (action == 2 * end + 1){pricePosition = BookInfo[1];}    // position is Ask
        /*System.out.println("action = " + action + " pricePosition = " + pricePosition
        + " buy? " + buyOrder + " payoff " + continuationValue);*/
        /*for (float f : p){
            System.out.println(f);
        }*/

        Order currentOrder = new Order(traderID, EventTime, buyOrder);

        oldCode = code;                                          // save for later updating
        oldAction = action;                                      // save for later updating

        if (write){                                              // printing data for output tables
            addDecisions((int) Bt, BookInfo[2], (int) (At - Bt), action);
        }

        if (action == end || action == 2 * end + 1) {
            isTraded = true;                                    // isTraded set to true if submitting MOs
            if (action == end && BookInfo[2] == 0 || action == 2 * end + 1 && BookInfo[3] == 0){
                execution(priceFV, EventTime);
            }
        }

        return (action != 2 * end + 2) ? new PriceOrder(pricePosition, currentOrder) : null;
    }

    public void execution(double fundamentalValue, double EventTime){
        double payoff;
        tradeCount++;
        if (oldAction < end){ // sell LO executed
            payoff = (oldAction - breakPoint) * tickSize - (fundamentalValue - PriceFV) - privateValue; //TODO: put priceFV
            //System.out.println("seller oldAction = " + oldAction + " payoff: " + payoff);
        } else {              // buy LO executed
            payoff = (breakPoint - (oldAction - end - 1)) * tickSize + privateValue +
                    (fundamentalValue - PriceFV);
            //System.out.println("buyer oldAction = " + oldAction + " payoff: " + payoff);
        }
        if (Payoffs.containsKey(oldCode)){ //TODO: check if deleting OK
            Payoff pay = Payoffs.get(oldCode);
            if (pay instanceof SinglePayoff){
                ((SinglePayoff) pay).update(oldAction, payoff, EventTime);
            } else {
                ((MultiplePayoff) pay).update(oldAction, payoff, EventTime);
            }
        }
        isTraded = true;
    }

    public Long HashCode(Hashtable<Byte, Byte> Priorities, int[] BookSizes, int[] BookInfo){
        Long code = (long) 0;
        if (infoSize == 2){
            long Bt = BookInfo[0];         // Best Bid position
            long At = BookInfo[1];         // Best Ask position
            int P = Priorities.get(nP);    // pricePosition position in previous action
            int q = Priorities.get((byte)P);     // priority of the order at P
            int x = 0;                     // order was buy(2) or sell(1) or no order (0)
            int a = pv;                    // private value zero(0), negative (1), positive (2)
            code = (Bt<<17) + (At<<12) + (P<<7) + (q<<4) + (x<<2) + a;

        } else if (infoSize == 7){
            long Bt = BookInfo[0];         // Best Bid position
            long At = BookInfo[1];         // Best Ask position
            long lBt = BookInfo[2] / 3;        // depth at best Bid TODO: needed?
            long lAt = BookInfo[3] / 3;        // depth at best Ask TODO: needed?
            long dBt = (BookInfo[4] / maxDepth);       // depth at buy    TODO: discretize
            int dSt = (BookInfo[5] / maxDepth);        // depth at sell   TODO: discretize
            int Pt = BookInfo[6];          // last transaction pricePosition position // TODO:needed?
            int b = BookInfo[7];           // 1 if last transaction buy, 0 if sell
            int P = Priorities.get(nP);    // pricePosition position in previous action
            int q = Priorities.get((byte)P);     // priority of the order at P
            int x = 0;                     // order was buy(2) or sell(1) or no order (0) //TODO: if 0, then P and q not needed
            int a = pv;                    // private value zero(0), negative (1), positive (2)
            int l = (isHFT) ? 1 : 0;       // arrival frequency slow (0), fast (1)
            if (isReturning){
                x = (BookSizes[P] > 0) ? 2 : 1;
            }

            /*Long code = (Bt<<50) + (At<<44) + (lBt<<40) + (lAt<<36) + (dBt<<29) + (dSt<<22) + (Pt<<16) + (b<<15) +
                    + (P<<9) + (q<<5) + (x<<3) + (a<<1) + l;*/
            code = (Bt<<32) + (At<<27) + (lBt<<26) + (lAt<<25) + (dBt<<22) + (dSt<<19) + (Pt<<14) + (b<<13) +
                    + (P<<8) + (q<<5) + (x<<3) + (a<<1) + l;

        }
        else if (infoSize == 8){
            long Bt = BookInfo[0];         // Best Bid position
            long At = BookInfo[1];         // Best Ask position
            long lBt = BookInfo[2];        // depth at best Bid TODO: needed?
            long lAt = BookInfo[3];        // depth at best Ask TODO: needed?
            long dBt  = BookInfo[4];       // depth at buy    TODO: discretize
            int dSt = BookInfo[5];         // depth at sell   TODO: discretize
            int Pt = BookInfo[6];          // last transaction pricePosition position // TODO:needed?
            int b = BookInfo[7];           // 1 if last transaction buy, 0 if sell
            int P = Priorities.get(nP);    // pricePosition position in previous action
            int q = Priorities.get((byte)P);     // priority of the order at P
            int x = 0;                     // order was buy(2) or sell(1) or no order (0)
            int a = pv;                    // private value zero(0), negative (1), positive (2)
            int l = (isHFT) ? 1 : 0;       // arrival frequency slow (0), fast (1)
            if (isReturning){
                x = (BookSizes[P] > 0) ? 2 : 1;
            }
            //System.out.println(Bt + " : " + lBt + " ; " + At + " : " + lAt);
            /*Long code = (Bt<<50) + (At<<44) + (lBt<<40) + (lAt<<36) + (dBt<<29) + (dSt<<22) + (Pt<<16) + (b<<15) +
                    + (P<<9) + (q<<5) + (x<<3) + (a<<1) + l;*/
            code = (Bt<<42) + (At<<37) + (lBt<<34) + (lAt<<31) + (dBt<<25) + (dSt<<19) + (Pt<<14) + (b<<13) +
                    + (P<<8) + (q<<5) + (x<<3) + (a<<1) + l;
        }

        return code;
    }

    public void printStatesDensity(double et){
        Payoff pay;
        short [] n;
        float [] p;
        try{
            String outputFileName = folder + "occurrences.csv";
            String outputFileName2 = folder + "payoffs.csv";
            FileWriter writer = new FileWriter(outputFileName, true);
            FileWriter writer2 = new FileWriter(outputFileName2, true);
            Iterator keys = Payoffs.keySet().iterator();
            while (keys.hasNext()){
                pay =  Payoffs.get(keys.next());
                if (pay instanceof MultiplePayoff){
                    writer.write(((MultiplePayoff)pay).getN() + ";" + "\r");
                    n = ((MultiplePayoff)pay).getNarray();
                    p = ((MultiplePayoff)pay).getP();
                    int sz = n.length;
                    String s = new String();
                    for (int i = 0; i < sz; i++){
                        s = s + n[i] + ";" + p[i] + ";";
                    }
                    writer2.write(s + "\r");
                }
               /* if (pay instanceof SinglePayoff){
                    writer.write(et - ((SinglePayoff) pay).getEventTime() + ";" + "\r");
                }*/
            }
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }


    }

    public void purge(){
        Long code;
        Payoff pay;
        Enumeration keys = Payoffs.keys();
        int all = 0;
        int deleted = 0;
        while (keys.hasMoreElements()){
            all++;
            code = (Long) keys.nextElement();
            pay =  Payoffs.get(code);
            if (pay.canBeDeleted()){
                deleted++;
                Payoffs.remove(code);
            }
        }
        System.out.println("all: " + all + " deleted: " + deleted);
    }

    public void nReset(byte n, short m){
        Long code;
        Payoff pay;
        Enumeration keys = Payoffs.keys();
        code = (Long) keys.nextElement();
        pay =  Payoffs.get(code);
        pay.setnReset(n, m);
        pay.nReset();
        while (keys.hasMoreElements()){
            code = (Long) keys.nextElement();
            pay =  Payoffs.get(code);
            pay.nReset();
                if (pay instanceof SinglePayoff){
                    ((SinglePayoff) pay).setFromPreviousRound(true);
                } else {
                    ((MultiplePayoff) pay).setFromPreviousRound(true);
                }
            }
     }

    public void addDecisions(int b, int lb, int s, int ac){
        decisions.add(new Decision(b, lb, s, ac));
    }

    public void printDepthFrequency(){
        try{
            String outputFileName = folder + "frequency.csv";
            FileWriter writer = new FileWriter(outputFileName, true);
            for (Decision d:decisions){
                writer.write(d.printDecision());
            }
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void resetDecisionHistory(){
        decisions = new Vector<Decision>();
    }

    // getters
    public int getTraderID(){
        return traderID;
    }

    public double getPrivateValue(){
        return privateValue;
    }

    public boolean getIsHFT(){
        return isHFT;
    }

    public long getOldCode(){
        return oldCode;
    }
    
    public boolean getIsTraded(){
        return isTraded;
    }

    public int getTraderCount(){
        return TraderCount;
    }

    public int getStatesCount(){
        return statesCount;
    }

    public int getPv(){
        return pv;
    }

    // setters
    public void setIsTraded(boolean traded){
        isTraded = traded;
    }

    public void setTradeCount(int c){
        tradeCount = c;
    }

    public void setTraderCount(int tc){
        TraderCount = tc;
    }

    public void setPrTremble(double pt){
        prTremble = pt;
    }

    public void setWrite(boolean b){
        write = b;
    }


}
