import java.io.FileWriter;
import java.util.*;
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Created by IntelliJ IDEA.
 * User: Jakub
 * Date: 22.3.12
 * Time: 19:24
 * To change this template use File | Settings | File Templates.
 * class trader serves as (i) parameter container for individual traders (ii) individual trader object
 */
public class Trader {
    private int traderID;               // initialized
    private float privateValue;         // initialized, can be changed to double later on
    private int pv;                     // holder for private value 0(0), 1(negative), 2(positive)
    private boolean isHFT;              // initialized
    private byte units2trade;           // zt in GPR2005 model
    private boolean isTraded = false;   // set by TransactionRule in book
    private boolean isReturning = false;// is he returning this time? info from priorities
    private long oldCode;               // oldCode holds old hash code for the last state in which he took action
    private short oldAction;             // which action he took in the old state
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
    static boolean writeDecisions = false;   // to writeDecisions things in trader?
    static boolean writeDiagnostics = false; // to writeDiagnostics things in trader?
    static boolean writeHistogram = false;   // to writeHistogram in trader?
    static String folder;
    static Decision decision;
    static Diagnostics diag;
    static int [] bookSizesHistory;
    static int[] previousTraderAction;
    static Payoff InitialPayoff;
    static Float[] mu;
    static Float[] deltaV0;
    static ArrayList<Order> orders;     // result of decision, passed to LOB.transactionRule
    //private double CFEE;              // cancellation fee


    // constructor bool, bool, float, int
    public Trader(boolean HFT, float privateValue, byte u2t) {
        this.isHFT = HFT;
        this.privateValue = privateValue;
        this.units2trade = u2t;
        TraderCount++;
        this.traderID = TraderCount;

        if (privateValue > 0.0001){pv = 2;}
        else if (privateValue < - 0.0001){pv = 1;}
        else {pv = 0;}
    }

    // constructor of the main trader- loads parameters from main
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
        decision = new Decision(numberPrices, FVpos, e, bp, LL);
        diag = new Diagnostics(numberPrices, e);
        bookSizesHistory = new int[2 * nP + 1];
        bookSizesHistory[0] = 0;
        previousTraderAction = new int[3];
        InitialPayoff = new Payoff(rho, nPayoffs, end);              // initialize static variables for Payoff class
    }

    // decision about the price is made here, so far random
    public PriceOrder decision(Hashtable<Byte, Byte> Priorities, int[] BookSizes, int[] BookInfo,
                               double EventTime, double priceFV){
        /*if (isTraded){
            System.out.println("What is this guy doing here?");
        }*/
        // positions , priorities table, & isReturning? 1 if yes, last transaction pricePosition
        // signed sizes vector, (best Bid, Ask), (depth at B, A), (depth Buy, Sell), (last pricePosition, WasBuy)
        // event time
        PriceFV = priceFV;                  // get price of the fundamental value = fundamental value
        int pricePosition;                  // pricePosition at which to submit
        boolean buyOrder = false;           // buy order?

        int P = Priorities.get(nP);                             // pricePosition position in previous action
        int q = (P != (nP + 1)) ? Priorities.get((byte)P) : 0;  // priority of the order at P
        int x = 0;                                              // order was buy(2) or sell(1) or no order (0)
        if (isReturning && P != nP + 1){                        // returning and has outstanding order
            x = (BookSizes[P] > 0) ? 2 : 1;
            if (P == BookInfo[0] && BookSizes[P] == 1 && x == 2){
                BookSizes[P] = 0;
                int j = nP - 1;
                while (BookSizes[j] <= 0 && j > 0){
                    j--;
                }
                BookInfo[0] = j;
            } else if (P == BookInfo[1] && BookSizes[P] == -1 && x == 1){
                BookSizes[P] = 0;
                int j = 0;
                while (BookSizes[j] >= 0 && j < nP - 1){
                    j++;
                }
                BookInfo[1] = j;
            }
        }
        long Bt = BookInfo[0];              // Best Bid position
        long At = BookInfo[1];              // Best Ask position
        Long code = HashCode(P, q, x, BookInfo);
        /*if (isReturning){
            System.out.println("Trader ID " + traderID);
            System.out.println("new hash code " + Long.toBinaryString(code));
            System.out.println("old hash code " + Long.toBinaryString(oldCode));
            System.out.println("Occurred old code " + Occurrences.get(oldCode));
        }*/                      // printing code commented

        double continuationValue;
        short action;
        double diff = 0.0;

        float[] p = new float[nPayoffs];
        double sum = 0;                     // used to compute payoff to no-order
        for (int i = 0; i < end; i++){      // Limit order payoffs for ticks LL through HL
            // SLOs
            p[i] = (float) (discountFactorB.get(i)[Math.abs(BookSizes[LL + i])] *
                    ((i - breakPoint) * tickSize - privateValue));
            //p[i] = (float)Math.random() + 5.5f;

            // BLOs
            p[i + end + 1] = (float) (discountFactorS.get(i)[Math.abs(BookSizes[LL + i])] *
                    ((breakPoint - i) * tickSize + privateValue));
            //p[i + end + 1] = (float)Math.random() + 5.5f;

            double LOpayoff = (p[i] > p[i + end + 1]) ? p[i]
                                                      : p[i + end + 1];
            // choose bigger one -> the one with positive payoff
            sum += LOpayoff;
        }

        // computing payoff from MO
        p[end] = (float)((Bt - fvPos) * tickSize - privateValue); // payoff to sell market order
        p[2 * end + 1] = (float)((fvPos - At) * tickSize + privateValue); // payoff to buy market order

        sum +=  (p[end] > p[2 * end + 1]) ? p[end]
                                          : p[2 * end + 1];

        if (P >= LL && P <= HL){  // previous action position is in the LO range
            int p25 = P - LL;
            if (x == 1){ // last LO was sell
                sum -= p[p25];
                p[p25] = (float)(discountFactorB.get(p25)[q] * ((p25 - breakPoint) * tickSize - privateValue));  //TODO: breakPoint is correct here?    //SLO
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
            SinglePayoff pay = new SinglePayoff(p, EventTime);
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
                    ((MultiplePayoff) pay).updateMax(p, EventTime, true);
                } else{                                              // here no trembling
                    ((MultiplePayoff) pay).updateMax(p, EventTime, false);
                }
                continuationValue = ((MultiplePayoff) pay).getMax();
                action = ((MultiplePayoff) pay).getMaxIndex();
                diff = ((MultiplePayoff) pay).getDiff();      // TODO: delete afterwards
            }
        }
        if (writeDiagnostics){writeDiagnostics(diff, (short)action);}

        // update old-state beliefs
        if (isReturning){
            if (Payoffs.containsKey(oldCode)){
                Payoff pay = Payoffs.get(oldCode);
                if (pay instanceof SinglePayoff){ // updating old SinglePayoff
                    ((SinglePayoff) pay).update(oldAction, continuationValue, EventTime);
                } else {                          // updating old MultiplePayoff
                    ((MultiplePayoff) pay).update(oldAction, continuationValue, EventTime);
                    diff = ((MultiplePayoff) pay).getDiff();
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

        Order currentOrder = new Order(traderID, EventTime, buyOrder, action, pricePosition);

        oldCode = code;                                          // save for later updating
        oldAction = action;                                      // save for later updating

        if (action == end){action = (short)(2 * end + 2);}           // TODO: delete this part
        if (action == 2 * end + 1){action = (short)(2 * end + 2);}

        if (writeDecisions){writeDecision(BookInfo, BookSizes, (int) action);}                                     // printing data for output tables


        if (action == end || action == 2 * end + 1) {
            isTraded = true;                                    // isTraded set to true if submitting MOs
        }

        return (action < end) ? new PriceOrder(pricePosition, currentOrder) : null;
    }

    // method for making decision based on GPR 2005
    public ArrayList decision(int[] BookSizes, int[] BookInfo, double EventTime, double priceFV){
        PriceFV = priceFV;
        int pricePosition;
        boolean buyOrder = false;
        short[] action = {127, 127};        // 127 as second action signals units2trade == 1
        short MaxIndex;
        double diff = 0.0;

        long Bt = BookInfo[0];              // Best Bid position
        long At = BookInfo[1];              // Best Ask position
        Long code = HashCode(0, 0, 0, BookInfo);

        // computing payoffs from different actions based on overly optimistic beliefs
        float[] p = new float[nPayoffs];
        HashMap<Short, Float> ps = new HashMap<Short, Float>();


        p[2 * end] = (float)((Bt - fvPos) * tickSize - privateValue );      // SMO
        p[2 * end + 1] = (float)((fvPos - At) * tickSize + privateValue);   // BMO
        p[2 * end + 2] = 0.0f;                                              // payoff to no order- but that's also a choice

        for (int i = 0; i < end; i++ ){
            // SLO
            p[i] = (float)(mu[i] * ((i - breakPoint) * tickSize - privateValue - deltaV0[i]));
            // BLO
            p[end + i] = (float)((mu[end + i]) * (privateValue + deltaV0[end + i] - (i - breakPoint) * tickSize));
        }

        short b = (short) Math.max(BookInfo[0] - LL + 1, 0);             // + 1 in order to start from one above B
        b = (short) Math.min(end, b); // TODO: test this change
        short a = (short) Math.min(BookInfo[1] - LL + end, 2 * end);
        a = (short) Math.max(end, a);

        if (units2trade == 1){
            for(short i = b; i < a; i++){            // searching for best payoff and not marketable LO
                ps.put((short)((i<<7) + 127), p[i]);
            }
            ps.put((short)(((2 * end)<<7) + 127),     p[2 * end]);             // SMO
            ps.put((short)(((2 * end + 1)<<7) + 127), p[2 * end + 1]);         // BMO
            ps.put((short)(((2 * end + 2)<<7) + 127), p[2 * end + 2]);         // NO
        } else if (units2trade == 2){
            // Rule 1a and Rule 2
            for(short i = b; i < a; i++){
                for (int j = b; j < Math.min(i + 1, a); j++){                  // i + 1, bcz j can equal i
                    if ((i - end) != j){                                       // TODO: check
                        ps.put((short)((i<<7) + j), (p[i] + p[j]));
                    }
                }
            }
            // Rule 1b
            short c = (BookInfo[2] == 1 || Bt == 0) ? (short)Math.max(0, b - 1) : b;    // SMO part
            for (int i = c; i < a; i++){
                ps.put((short)(((2 * end)<<7) + i), (p[2 * end] + p[i]));
            }

            c = (BookInfo[3] == 1 || (At == (nP - 1))) ? (short)Math.min(2 *end, a + 1) : a;
            for (int i = b; i < c; i++){
                ps.put((short)(((2 * end + 1)<<7) + i), (p[2 * end + 1] + p[i]));
            }

            // Rule 3
            if (BookInfo[2] > 1 || Bt == 0){            // SMO
                ps.put((short)(((2 * end)<<7) + (2 * end)), (p[2 * end] + p[2 * end]));
            } else {
                // TODO: find next best Bid
            }
            if (BookInfo[3] > 1 || (At == (nP - 1))){   // BMO
                ps.put((short)(((2 * end + 1)<<7) + 2 * end + 1), (p[2 * end + 1] +p[2 * end + 1]));
            } else {
                // TODO: find next best Ask
            }

            // Rule 4 TODO: No Order payoffs - basically equal to units2trade == 1
        }

        if (!Payoffs.containsKey(code)){ // new state, new SinglePayoff created
            statesCount++;
            System.out.println(statesCount);
            GPR2005Payoff_test pay = new GPR2005Payoff_test(ps, units2trade);
            Payoffs.put(code, pay);      // insert a Payoff object made of GPR2005Payoff to the Payoffs table
            MaxIndex = pay.getMaxIndex();
            action[0] = (short)(MaxIndex>>7);
            action[1] = (short) (MaxIndex - (action[0]<<7));
        } else {
            GPR2005Payoff_test pay = (GPR2005Payoff_test)Payoffs.get(code);
            Iterator keys = pay.getX().keySet().iterator();
            while (keys.hasNext()){
                float tempPayoff = 0.0f;
                boolean LO = false;
                short key = (Short) keys.next();
                action[0] = (short)(key>>7);
                action[1] = (short) (key - (action[0]<<7));
                for (int i = 0; i < units2trade; i++){
                    if (action[i] < end){
                        // SLO
                        LO = true;
                        tempPayoff = tempPayoff + (float)((pay.getX().get(key)[i].getMu()) *
                                        ((action[i] - breakPoint) * tickSize - privateValue -
                                                pay.getX().get(key)[i].getDeltaV()));
                    } else if (action[i] < (2 * end)) {
                        // BLO
                        LO = true;
                        tempPayoff = tempPayoff + (float)((pay.getX().get(key)[i].getMu()) *
                                        (privateValue + pay.getX().get(key)[i].getDeltaV() -
                                                (action[i] - breakPoint - end) * tickSize));
                    }
                }
                if (LO){ps.put(key, tempPayoff);}
            }
            if (prTremble > 0 && Math.random() < prTremble){
                pay.updateMax(ps, units2trade, true);
            } else {
                pay.updateMax(ps, units2trade, false);
                diff = pay.getDiff();
            }
            MaxIndex = pay.getMaxIndex();
            action[0] = (short)(MaxIndex>>7);
            action[1] = (short) (MaxIndex - (action[0]<<7));
        }

        orders = new ArrayList<Order>();
        for (int i = 0; i < units2trade; i++){
            buyOrder = false;
            if ((action[i] >= end) && (action[i] != 2 * end)){
                buyOrder = true;
            }
            pricePosition = (action[i] < end) ? LL + action[i]
                                              : LL + action[i] - end;
            if (action[i] == 2 * end || (!buyOrder && pricePosition < Bt)){pricePosition = BookInfo[0];}       // position is Bid
            if (action[i] == 2 * end + 1 || (buyOrder && pricePosition > At)){pricePosition = BookInfo[1];}    // TODO: for the second action, this is at second best Ask
            Order CurrentOrder = new Order(traderID, EventTime, buyOrder, action[i], pricePosition);
            orders.add(CurrentOrder);
        }

        oldCode = code;
        oldAction = MaxIndex;

        if (writeDiagnostics){writeDiagnostics(diff, action[0]);}
        //if (writeDecisions){writeDecision(BookInfo, BookSizes, (int) action);}
        if (writeHistogram){writeHistogram(BookSizes);}
        //System.out.println("action = " + action);
                /*System.out.println("action = " + action + " pricePosition = " + pricePosition
           + " buy? " + buyOrder);*/

        return orders;
    }

    // used to update payoff of action taken in a state upon execution
    public void execution(double fundamentalValue, double EventTime){
        double payoff;
        tradeCount++;
        if (oldAction < end){ // sell LO executed
            payoff = (oldAction - breakPoint) * tickSize - (fundamentalValue - PriceFV) - privateValue;
            //System.out.println("seller oldAction = " + oldAction + " payoff: " + payoff);
        } else {              // buy LO executed
            payoff = (breakPoint - (oldAction - end - 1)) * tickSize + privateValue +
                    (fundamentalValue - PriceFV);
            //System.out.println("buyer oldAction = " + oldAction + " payoff: " + payoff);
        }
        if (Payoffs.containsKey(oldCode)){
            Payoff pay = Payoffs.get(oldCode);
            if (pay instanceof SinglePayoff){
                ((SinglePayoff) pay).update(oldAction, payoff, EventTime);
            } else {
                ((MultiplePayoff) pay).update(oldAction, payoff, EventTime);
            }
        }
        isTraded = true;
    }

    // execution as in GPR 2005 setting
    public void execution(double fundamentalValue, Order o){
        tradeCount++;
        byte unitTraded = 0;
        if (units2trade > 1){
            unitTraded = ((oldAction>>7) > o.getAction()) ? (byte) 1 : (byte) 0;
        }
        if (Payoffs.containsKey(oldCode)){
            ((GPR2005Payoff_test) Payoffs.get(oldCode)).update(oldAction, (float)(fundamentalValue - PriceFV), false, unitTraded);
        }
    }

    // use to cancel limit order in normal setting
    public void cancel(double EventTime){
        double payoff = 0.0;
        if (Payoffs.containsKey(oldCode)){
            Payoff pay = Payoffs.get(oldCode);
            if (pay instanceof SinglePayoff){
                ((SinglePayoff) pay).update(oldAction, payoff, EventTime);
            } else {
                ((MultiplePayoff) pay).update(oldAction, payoff, EventTime);
            }
        }
        isTraded = true;
    }

    // use to cancel limit order in a GPR2005 setting
    public void cancel(Order o){
        byte unitTraded = 0;
        if (units2trade > 1){
            unitTraded = ((oldAction>>7) > o.getAction()) ? (byte) 1 : (byte) 0;
        }
        if (Payoffs.containsKey(oldCode)){
            ((GPR2005Payoff_test) Payoffs.get(oldCode)).update(oldAction, 0.0f, true, unitTraded);
        }
    }

    // writing decisions
    private void writeDecision(int[] BookInfo, int[] BookSizes, int action){
        // tables I, V
        decision.addDecision(BookInfo, action, previousTraderAction);
        previousTraderAction[0] = action;
        previousTraderAction[1] = BookInfo[0];
        previousTraderAction[2] = BookInfo[1];
    }

    private void writeHistogram(int[] BookSizes){
        // histogram
        bookSizesHistory[0]++;
        int sz = BookSizes.length;
        for (int i = 0; i < sz; i++){
            if (BookSizes[i] < 0){
                bookSizesHistory[i + 1] = bookSizesHistory[i + 1] + BookSizes[i];
            } else {
                bookSizesHistory[nP + i + 1] = bookSizesHistory[nP + i + 1] + BookSizes[i];
            }
        }
    }

    // writing diagnostics
    private void writeDiagnostics(double diff, short ac){
        diag.addDiff(diff);
        diag.addAction(ac);
    }

    // computes initial beliefs for GPR2005 Payoffs
    public HashMap<String, Float[]> computeInitialBeliefs(float deltaLow, double muND, double stdevND){
        // compute execution probabilities
        NormalDistribution nd = new NormalDistribution(muND, stdevND);   // mean 0, stddev $0.35 - GPR 2005
        double FB;
        mu = new Float[nPayoffs];
        deltaV0 = new Float[nPayoffs];
        for (int i = 0; i < end; i++){      // Limit order payoffs for ticks LL through HL
            FB = nd.cumulativeProbability((i - breakPoint) * tickSize);
            mu[i] = (float) (((1.0 - deltaLow) * (1.0 - FB)) / (1.0 - (1.0 - deltaLow) * (FB)));
            deltaV0[i] = (float)(- i * tickSize);
            // SLOs
            mu[i + end] = (float) (((1.0 - deltaLow) * FB) / (1.0 - (1.0 - deltaLow) * (1.0 - FB)));
            deltaV0[i + end] = (float)((end - 1 - i) * tickSize);
            // BLOs
        }
        mu[2 * end] = 1.0f;                             // market sell order
        mu[2 * end + 1] = 1.0f;                         // market buy order
        mu[2 * end + 2] = 0.0f;                         // no order

        // compute FV change upon execution
        deltaV0[2 * end] = 0.0f;                           // market sell order
        deltaV0[2 * end + 1] = 0.0f;                       // market buy order
        deltaV0[2 * end + 2] = 0.0f;                       // no order
        InitialPayoff.setInitialBeliefs(mu, deltaV0);
        HashMap<String, Float[]> tempInitialBeliefs = new HashMap<String, Float[]>();
        tempInitialBeliefs.put("mu0", mu);
        tempInitialBeliefs.put("deltaV0", deltaV0);
        return tempInitialBeliefs;
    }

    // Hash code computed dependent on various Information Size (2, 4, 6, 7, 8)
    public Long HashCode(int P, int q, int x, int[] BookInfo){
        Long code = (long) 0;
        if (infoSize == 2){
            long Bt = BookInfo[0];                  // Best Bid position
            long At = BookInfo[1];                  // Best Ask position
            int a = pv;                             // private value zero(0), negative (1), positive (2)
            code = (Bt<<17) + (At<<12) + (P<<7) + (q<<4) + (x<<2) + a;

            /*boolean[] test = new boolean[13];
            long code2 = code;
            test[0] = (code2>>17 == Bt);
            System.out.println(test[0]);
            code2 = code - (Bt<<17);
            test[1] = (code2>>12 == At);
            System.out.println(test[1]);
            code2 = code2 - (At<<12);
            test[2] = (code2>>10 == lBt);
            System.out.println(test[2]);
            code2 = code2 - (lBt<<10);
            test[3] = (code2>>8 == lAt);
            System.out.println(test[3]);
            code2 = code2 - (lAt<<8);
            test[4] = (code2>>5 == dBt);
            System.out.println(test[4]);
            code2 = code2 - (dBt<<5);
            test[5] = (code2>>2 == dSt);
            System.out.println(test[5]);
            code2 = code2 - (dSt<<2);
            test[6] = (code2>>14 == Pt);
            System.out.println(test[6]);
            code2 = code2 - (Pt<<14);
            test[7] = (code2>>13 == b);
            System.out.println(test[7]);
            code2 = code2 - (b<<13);
            test[8] = (code2>>7 == P);
            System.out.println(test[8]);
            code2 = code2 - (P<<7);
            test[9] = (code2>>4 == q);
            System.out.println(test[9]);
            code2 = code2 - (q<<4);
            test[10] = (code2>>2 == x);
            System.out.println(test[10]);
            code2 = code2 - (x<<2);
            test[11] = (code2 == a);
            System.out.println(test[11]);
            code2 = code2 - a;
            System.out.println(Long.toBinaryString(code));
            if (code2 !=0){
                System.out.println("problem");
            }*/     // tests

        } else if (infoSize == 5) {                 // GPR 2005 state space
            long Bt = BookInfo[0];                  // Best Bid position
            long At = BookInfo[1];                  // Best Ask position
            long lBt = BookInfo[2];                 // depth at best Bid
            long lAt = BookInfo[3];                 // depth at best Ask
            long dBt = (BookInfo[4]);               // depth at buy
            int dSt = (BookInfo[5]);                // depth at sell
            int l = (isHFT) ? 1 : 0;                // arrival frequency slow (0), fast (1)

            code = (Bt<<26) + (At<<21) + (lBt<<17) + (lAt<<13) + (dBt<<7) + (dSt<<1) + l;
            /*long Bt = BookInfo[0];                  // Best Bid position
            long At = BookInfo[1];                  // Best Ask position
            long lBt = BookInfo[2] / 3;             // depth at best Bid
            long lAt = BookInfo[3] / 3;             // depth at best Ask
            long dBt = (BookInfo[4] / maxDepth);    // depth at buy
            int dSt = (BookInfo[5] / maxDepth);     // depth at sell
            code = (Bt<<15) + (At<<10) + (lBt<<8) + (lAt<<6) + (dBt<<3) + dSt;*/
            /*boolean[] test = new boolean[13];
            long code2 = code;
            test[0] = (code2>>26 == Bt);
            System.out.println(test[0]);
            code2 = code - (Bt<<26);
            test[1] = (code2>>21 == At);
            System.out.println(test[1]);
            code2 = code2 - (At<<21);
            test[2] = (code2>>17 == lBt);
            System.out.println(test[2]);
            code2 = code2 - (lBt<<17);
            test[3] = (code2>>13 == lAt);
            System.out.println(test[3]);
            code2 = code2 - (lAt<<13);
            test[4] = (code2>>7 == dBt);
            System.out.println(test[4]);
            code2 = code2 - (dBt<<7);
            test[5] = (code2>>1 == dSt);
            System.out.println(test[5]);
            code2 = code2 - (dSt<<1);
            test[6] = (code2 == l);
            System.out.println(test[6]);
            code2 = code2 - l;
            System.out.println(Long.toBinaryString(code));
            if (code2 !=0){
                System.out.println("problem");
            }*/     // tests

        } else if (infoSize == 6) {
            long Bt = BookInfo[0];                  // Best Bid position
            long At = BookInfo[1];                  // Best Ask position
            long lBt = BookInfo[2] / 3;             // depth at best Bid
            long lAt = BookInfo[3] / 3;             // depth at best Ask
            int a = pv;                             // private value zero(0), negative (1), positive (2)
            code = (Bt<<21) + (At<<16) + (lBt<<14) + (lAt<<12) + (P<<7) + (q<<4) + (x<<2) + a;

        } else if (infoSize == 7){
            long Bt = BookInfo[0];                  // Best Bid position
            long At = BookInfo[1];                  // Best Ask position
            long lBt = BookInfo[2] / 3;             // depth at best Bid
            long lAt = BookInfo[3] / 3;             // depth at best Ask
            long dBt = (BookInfo[4] / maxDepth);    // depth at buy
            int dSt = (BookInfo[5] / maxDepth);     // depth at sell
            int Pt = BookInfo[6];                   // last transaction pricePosition position
            int b = BookInfo[7];                    // 1 if last transaction buy, 0 if sell
            int a = pv;                             // private value zero(0), negative (1), positive (2)
            int l = (isHFT) ? 1 : 0;                // arrival frequency slow (0), fast (1)

            /*Long code = (Bt<<50) + (At<<44) + (lBt<<40) + (lAt<<36) + (dBt<<29) + (dSt<<22) + (Pt<<16) + (b<<15) +
                    + (P<<9) + (q<<5) + (x<<3) + (a<<1) + l;*/
            code = (Bt<<34) + (At<<29) + (lBt<<27) + (lAt<<25) + (dBt<<22) + (dSt<<19) + (Pt<<14) + (b<<13) +
                    + (P<<8) + (q<<5) + (x<<3) + (a<<1) + l;

        }
        else if (infoSize == 8){
            long Bt = BookInfo[0];              // Best Bid position
            long At = BookInfo[1];              // Best Ask position
            long lBt = BookInfo[2];             // depth at best Bid
            long lAt = BookInfo[3];             // depth at best Ask
            long dBt  = BookInfo[4];            // depth at buy
            int dSt = BookInfo[5];              // depth at sell
            int Pt = BookInfo[6];               // last transaction pricePosition position
            int b = BookInfo[7];                // 1 if last transaction buy, 0 if sell
            int a = pv;                         // private value zero(0), negative (1), positive (2)
            int l = (isHFT) ? 1 : 0;            // arrival frequency slow (0), fast (1)
            //System.out.println(Bt + " : " + lBt + " ; " + At + " : " + lAt);
            /*Long code = (Bt<<50) + (At<<44) + (lBt<<40) + (lAt<<36) + (dBt<<29) + (dSt<<22) + (Pt<<16) + (b<<15) +
                    + (P<<9) + (q<<5) + (x<<3) + (a<<1) + l;*/
            code = (Bt<<42) + (At<<37) + (lBt<<34) + (lAt<<31) + (dBt<<25) + (dSt<<19) + (Pt<<14) + (b<<13) +
                    + (P<<8) + (q<<5) + (x<<3) + (a<<1) + l;
        }

        return code;
    }

    // used to print the data analysed for convergence- occurrences os states, payoffs, time since last hit
    public void printStatesDensity(double et){
        Payoff pay;
        short [] n;
        float[] p;
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
                    writer.writeDecisions(et - ((SinglePayoff) pay).getEventTime() + ";" + "\r");
                }*/
            }
            writer.close();
            writer2.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    // prints Histogram of bookSizes-> so the book into a csv file. The first var is the actual FV
    public void printHistogram(){
        try{
            String outputFileName3 = folder + "histogram.csv";
            FileWriter writer3 = new FileWriter(outputFileName3, true);
            int sz = bookSizesHistory.length;
            writer3.write(bookSizesHistory[0] + ";");
            for (int i = 1; i < sz; i++){
                writer3.write((double)bookSizesHistory[i]/(double)bookSizesHistory[0] + ";");
            }
            writer3.write("\r");
            writer3.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    // deletes the Payoff of a state which has fromPreviousRound set to true
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

    // resets n in Payoffs, sets purge indicator to true-> true until next time the state is hit
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
                } else if (pay instanceof MultiplePayoff) {
                    ((MultiplePayoff) pay).setFromPreviousRound(true);
                }
            }
     }

    // prints diagnostics collected from data in decisions
    public void printDiagnostics(){
        try{
            String outputFileName = folder + "diagnostics.csv";
            FileWriter writer = new FileWriter(outputFileName, true);
            writer.write(diag.printDiagnostics("actions"));
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void printConvergence(int t2){
        Long code;
        Payoff pay;
        Enumeration keys = Payoffs.keys();
        try{
            String outputFileName = folder + "convergence.csv";
            FileWriter writer = new FileWriter(outputFileName, true);
            while (keys.hasMoreElements()){
                code = (Long) keys.nextElement();
                pay =  Payoffs.get(code);
                if (pay instanceof MultiplePayoff){
                    writer.write(((MultiplePayoff) pay).printDiff(t2));
                }
            }
            keys = Payoffs.keys();
            if (keys.hasMoreElements()){
                Payoffs.get(keys.nextElement()).setDof(0);
            }
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    // prints decisions data collected from actions in different states
    public void printDecisions(){
        try{
            String outputFileName = folder + "decisions.csv";
            FileWriter writer = new FileWriter(outputFileName, true);
            writer.write(decision.printDecision());
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    // resets Decision history, memory management
    public void resetDecisionHistory(){
        decision = new Decision(nP, fvPos, end, breakPoint, LL);
    }

    public void resetDiagnostics(){
        diag = new Diagnostics(nP, end);
    }

    // resets the history of bookSizes data used for histogram
    public void resetHistogram(){
        bookSizesHistory = new int[2 * nP + 1];
        bookSizesHistory[0] = 0;
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

    public boolean getIsReturning(){
        return isReturning;
    }

    public int getStatesCount(){
        return statesCount;
    }

    public double getPriceFV() {
        return PriceFV;
    }

    public short getOldAction(){
        return oldAction;
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

    public void setWriteDec(boolean b){
        writeDecisions = b;
    }

    public void setWriteDiag(boolean b){
        writeDiagnostics = b;
    }

    public void setWriteHist(boolean b){
        writeHistogram = b;
    }

    public Hashtable getPayoffs(){
        return Payoffs;
    }

}
