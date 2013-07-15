package com.jakubrojcek.gpr2005a;

import java.io.FileWriter;
import java.util.*;

import com.jakubrojcek.Decision;
import com.jakubrojcek.Diagnostics;
import com.jakubrojcek.Order;
import com.jakubrojcek.PriceOrder;
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
    private boolean execBefore = false; // in case of ac[0] == ac[1], was first one executed? or cancelled?
    private boolean isTraded = false;   // set by TransactionRule in book
    private boolean isReturning = false;// is he returning this time? info from priorities
    private long[] oldCode = new long[2];// oldCode holds old hash code for the last state in which he took action
    private Short[] oldAction;             // which action he took in the old state
    private float rho = 0.05f;          // trading "impatience" parameter
    private double PriceFV;             // current fundamental value-> price at middle position //TODO: should this be in the payoff?

    static int TraderCount = 0;         // counting number of traders, gives traderID as well
    static int tradeCount = 0;          // counting number of trader
    static int statesCount = 0;         // counting number of unique states
    static HashMap<Long, Payoff> Payoffs = new HashMap<Long, Payoff>();/* Beliefs about payoffs for different actions
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
    static GPR2005Payoff_test3 InitalGPR2005;  // GPR2005_test3 com.jakubrojcek.gpr2005a.Payoff, auxiliary for computing mu and deltaV for second share in case no com.jakubrojcek.gpr2005a.Payoff already exists
    static Float[][] mu0;
    static Float[][] deltaV0;
    static ArrayList<Order> orders;     // result of decision, passed to LOB.transactionRule
    static HashMap<Short, Float> ps;
    //private double CFEE;              // cancellation fee


    // constructor bool, bool, float, int
    public Trader(boolean HFT, float privateValue, byte u2t) {
        this.isHFT = HFT;
        this.privateValue = privateValue;
        this.units2trade = u2t;
        TraderCount++;
        this.traderID = TraderCount;

        if (privateValue > -0.0625){pv = 2;}
        else if (privateValue < - 0.0625){pv = 1;}
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
        InitialPayoff = new Payoff(rho, nPayoffs, end, LL, maxDepth, nP);              // initialize static variables for com.jakubrojcek.gpr2005a.Payoff class
        InitalGPR2005 = new GPR2005Payoff_test3();
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
        Long code = HashCode(P, q, x, BookInfo, BookSizes);
        /*if (isReturning){
            System.out.println("com.jakubrojcek.gpr2005a.Trader ID " + traderID);
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

        if (!Payoffs.containsKey(code)){ // new state, new com.jakubrojcek.gpr2005a.SinglePayoff created
            statesCount++;
            SinglePayoff pay = new SinglePayoff(p, EventTime);
            Payoffs.put(code, pay);      // insert a com.jakubrojcek.gpr2005a.Payoff object made of com.jakubrojcek.gpr2005a.SinglePayoff to the Payoffs table
            action = pay.getMaxIndex();
            continuationValue = pay.getMax();
        } else{
            Payoff pay = Payoffs.get(code);   // get from HashTable
            if (pay instanceof SinglePayoff){ //Transfer com.jakubrojcek.gpr2005a.SinglePayoff to com.jakubrojcek.gpr2005a.MultiplePayoff
                //statesCount++;
                MultiplePayoff pay2 = new MultiplePayoff(p, EventTime, (SinglePayoff) pay);//
                Payoffs.put(code, pay2);
                continuationValue = pay2.getMax();
                action = pay2.getMaxIndex();
            } else {                                                 // com.jakubrojcek.gpr2005a.MultiplePayoff occurring again
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
        //if (writeDiagnostics){writeDiagnostics(diff, (short)action);}

        // update old-state beliefs
        if (isReturning){
            if (Payoffs.containsKey(oldCode)){
                Payoff pay = Payoffs.get(oldCode);
                if (pay instanceof SinglePayoff){ // updating old com.jakubrojcek.gpr2005a.SinglePayoff
                    ((SinglePayoff) pay).update(oldAction[0], continuationValue, EventTime);
                } else {                          // updating old com.jakubrojcek.gpr2005a.MultiplePayoff
                    ((MultiplePayoff) pay).update(oldAction[0], continuationValue, EventTime);
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

        Order currentOrder = new Order(traderID, EventTime, buyOrder, true, action, (short) 0, pricePosition);

        oldCode[0] = code;                                          // save for later updating
        oldAction[0] = action;                                      // save for later updating

        if (action == end){action = (short)(2 * end + 2);}           // TODO: delete this part
        if (action == 2 * end + 1){action = (short)(2 * end + 2);}

        Short[] action2 = {action, 127};
        if (writeDecisions){writeDecision(BookInfo, BookSizes, action2);}                                     // printing data for output tables


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
        short q1 = 0;
        short q2;
        //Short[] q = new Short[2];           // priority
        double diff = 0.0;

        long Bt = BookInfo[0];              // Best Bid position, temporary storage
        long At = BookInfo[1];              // Best Ask position, temporary storage
        int B2t = (int)Bt;                  // second best bid
        int A2t = (int)At;                  // second best ask

        if (Bt != 0 && BookInfo[2] == 1){
            for (--B2t; B2t > 0; B2t--){                        // computing second best Bid
                if(BookSizes[B2t] > 0){
                    break;
                }
            }
        }
        if ((At != nP - 1) && BookInfo[3] == 1){
            for (++A2t; A2t < (nP - 1); A2t++){                   // computing second best Ask
                if(BookSizes[A2t] < 0){
                    break;
                }
            }
        }

        // computing payoffs from different actions based on overly optimistic beliefs
        GPR2005Payoff_test3 pay;            // temporary com.jakubrojcek.gpr2005a.Payoff of the current state
        GPR2005Payoff_test3 pay2;           // temporary com.jakubrojcek.gpr2005a.Payoff of hypothetical state
        int[] BsNew = new int[nP];          // temporary BookSizes
        int[] BiNew = new int[8];           // temporary BookInfo
        float[] mu = new float[nPayoffs];                         // exec probabilities for fist share
        float[] dV = new float[nPayoffs];                         // expected jumps for fist share
        float[] mu2 = new float[nPayoffs];  // exec probabilities for second share
        float[] dV2 = new float[nPayoffs];  // expected jumps for second share
        float mu2max = 0.0f;// exec probabilities for best second action
        float dV2max = 0.0f;// expected jumps for best second action
        float p1, p2;                 // temporary storages for payoff value
        //float [] p1 = new float[nPayoffs];
        //float [] p2 = new float[nPayoffs];
        float max = - 1.0f;                 // maximum payoff available
        Short[] MaxAction = {127, 127};     // action maximizing payoff in the state
        Short[] MaxQ = {127, 127};          // q at which MaxAction takes place (priority)
        long code2max = 0;                  //


        Long code = HashCode(0, 0, 0, BookInfo, BookSizes);
        long code2 = 0;

        short b = (short) Math.max(BookInfo[0] - LL + 1, 0);             // + 1 in order to start from one above B
        b = (short) Math.min(end, b);
        short a = (short) Math.min(BookInfo[1] - LL + end, 2 * end);
        a = (short) Math.max(end, a);
        short b2;                                   // a, b for second share
        short a2;

        if (Payoffs.containsKey(code)){ // new state, new com.jakubrojcek.gpr2005a.SinglePayoff created
            pay = ((GPR2005Payoff_test3) Payoffs.get(code));
        } else {
            statesCount++;
            System.out.println(statesCount);
            pay = new GPR2005Payoff_test3();
            Payoffs.put(code, pay); // insert a com.jakubrojcek.gpr2005a.Payoff object made of com.jakubrojcek.gpr2005a.GPR2005Payoff to the Payoffs table
        }
        HashMap<String, float[]> MuDv = pay.getMuDv(BookSizes, b, a);  // TODO: can I use BookSizes here?
        mu = MuDv.get("mu");
        dV = MuDv.get("dv");

        for(short i = b; i < nPayoffs; i++){ // searching for best payoff
            p1 = -1.0f;
            p2 = -1.0f;
            System.arraycopy(BookSizes, 0, BsNew, 0, nP);
            System.arraycopy(BookInfo, 0, BiNew, 0, 8);
            b2 = b;
            a2 = a;
            short j;
            if (i < end){                                       // sell limit order first
                q1 = (short) Math.min(- BsNew[i + LL], maxDepth);
                p1 = (float)(mu[i] * ((i - breakPoint) * tickSize - privateValue - dV[i]));
                //p2[i] = p1;//(float) Math.random();
                if (units2trade == 2){
                    if ((i + LL) < BookInfo[1]) {BiNew[1] = i + LL;}
                    BsNew[i + LL]--;
                    a2 = (short) Math.min(i + 1, a);
                    code2 = HashCode(0, 0, 0, BiNew, BsNew);
                    if (Payoffs.containsKey(code2)){ // new state, new com.jakubrojcek.gpr2005a.SinglePayoff created
                        pay2 = ((GPR2005Payoff_test3) Payoffs.get(code2));
                        HashMap<String, float[]> MuDv2 = pay2.getMuDv(BsNew, b2, a2);
                        mu2 = MuDv2.get("mu");
                        dV2 = MuDv2.get("dv");
                    } else {
                        HashMap<String, float[]> MuDv2 = InitalGPR2005.getMuDv(BsNew, b2, a2);  // TODO: can I use BookSizes here?
                        mu2 = MuDv2.get("mu");
                        dV2 = MuDv2.get("dv");
                    }
                    for (j = b; j < a2; j++){                  // i + 1, bcz j can equal i
                        q2 = (short) Math.min((- BsNew[j + LL]), maxDepth);
                        p2 = (float)(mu2[j] * ((j - breakPoint) * tickSize - privateValue - dV2[j]));
                        //System.out.println(i+ "" + j);
                        if ((p1 + p2) > max){
                            max = p1 + p2;
                            MaxAction[0] = i;
                            MaxAction[1] = j;
                            MaxQ[0] = q1;
                            MaxQ[1] = q2;
                            code2max = code2;
                            mu2max = mu2[j];
                            dV2max = dV2[j];
                        }
                    }
                }
            } else if (i < a) {                                 // buy limit order first
                q1 = (short) Math.min(BsNew[i - end + LL], maxDepth);
                p1 = (float)((mu[i]) * (privateValue + dV[i] - (i - end - breakPoint) * tickSize));
                //p2[i] = p1;//(float) Math.random();
                if (units2trade == 2){
                    if ((i - end + LL) > BookInfo[0]) {BiNew[0] = (i - end + LL);}
                    BsNew[i - end + LL]++;
                    b2 = (short) Math.max(i - end + 1, b);
                    a2 = (short) Math.min(i + 1, a);
                    code2 = HashCode(0, 0, 0, BiNew, BsNew);
                    if (Payoffs.containsKey(code2)){ // new state, new com.jakubrojcek.gpr2005a.SinglePayoff created
                        pay2 = ((GPR2005Payoff_test3) Payoffs.get(code2));
                        HashMap<String, float[]> MuDv2 = pay2.getMuDv(BsNew, b2, a2);
                        mu2 = MuDv2.get("mu");
                        dV2 = MuDv2.get("dv");
                    } else {
                        HashMap<String, float[]> MuDv2 = InitalGPR2005.getMuDv(BsNew, b2, a2);
                        mu2 = MuDv2.get("mu");
                        dV2 = MuDv2.get("dv");
                    }
                    for (j = b2; j < a2; j++){                  // i + 1, bcz j can equal i
                        if (j > (i - end)){
                            if (j < end){
                                q2 = (short) Math.min(- BsNew[j + LL], maxDepth);
                                p2 = (float)(mu2[j] * ((j - breakPoint) * tickSize - privateValue - dV2[j]));
                            } else {
                                q2 = (short) Math.min(BsNew[j - end + LL], maxDepth);
                                p2 = (float)((mu2[j]) * (privateValue + dV2[j] - (j - end - breakPoint) * tickSize));
                            }
                            //System.out.println(i+ "" + j);
                            if ((p1 + p2) > max){
                                max = p1 + p2;
                                MaxAction[0] = i;
                                MaxAction[1] = j;
                                MaxQ[0] = q1;
                                MaxQ[1] = q2;
                                code2max = code2;
                                mu2max = mu2[j];
                                dV2max = dV2[j];
                            }
                        }
                    }
                }
            } else if (i == (2 * end)){                             // sell market order
                q1 = 0;
                p1 = (float)((BookInfo[0] - fvPos) * tickSize - privateValue );
                //p2[i] = (float) Math.random();
                if (units2trade == 2){
                    short c2 = (short) Math.max(B2t - LL + 1, 0);
                    short c = (short) Math.min(end, c2);
                    BiNew[0] = B2t;
                    BsNew[BookInfo[0]]--;
                    code2 = HashCode(0, 0, 0, BiNew, BsNew);
                    if (Payoffs.containsKey(code2)){ // new state, new com.jakubrojcek.gpr2005a.SinglePayoff created
                        pay2 = ((GPR2005Payoff_test3) Payoffs.get(code2));
                        HashMap<String, float[]> MuDv2 = pay2.getMuDv(BsNew, c, a);  // TODO: can I use BookSizes here?
                        mu2 = MuDv2.get("mu");
                        dV2 = MuDv2.get("dv");
                    } else {
                        HashMap<String, float[]> MuDv2 = InitalGPR2005.getMuDv(BsNew, c, a);  // TODO: can I use BookSizes here?
                        mu2 = MuDv2.get("mu");
                        dV2 = MuDv2.get("dv");
                    }
                    for (j = c; j < a; j++){
                        p2 = -1.0f;     // TODO: fine here?
                        q2 = 0;
                        if (j < end){
                            q2 = (short) Math.min(- BsNew[j + LL], maxDepth);
                            p2 = (float)(mu2[j] * ((j - breakPoint) * tickSize - privateValue - dV2[j]));
                        } else if ((j - end + LL) != BookInfo[0]){
                            q2 = (short) Math.min(BsNew[j - end + LL], maxDepth);
                            p2 = (float)((mu2[j]) * (privateValue + dV2[j] - (j - end - breakPoint) * tickSize));
                        }
                        //System.out.println(i+ "" + j);
                        if ((p1 + p2) > max){
                            max = p1 + p2;
                            MaxAction[0] = i;
                            MaxAction[1] = j;
                            MaxQ[0] = q1;
                            MaxQ[1] = q2;
                            code2max = code2;
                            mu2max = mu2[j];
                            dV2max = dV2[j];
                        }
                    }
                    // SMO
                    q2 = 0;
                    j = (short) (2 * end);
                    p2 = (float)((B2t - fvPos) * tickSize - privateValue);
                    //System.out.println(i+ "" + j);
                    if ((p1 + p2) > max){
                        max = p1 + p2;
                        mu2max = mu2[j];
                        dV2max = dV2[j];
                        if (B2t != Bt){
                            j = (short) (2 * end + 3);
                        }
                        MaxAction[0] = i;
                        MaxAction[1] = j;
                        MaxQ[0] = q1;
                        MaxQ[1] = q2;
                        code2max = code2;
                    }
                }
            } else if (i == (2 * end + 1)){                       // BMO
                q1 = 0;
                p1 = (float)((fvPos - BookInfo[1]) * tickSize + privateValue);
                //p2 = (float) Math.random();
                if (units2trade == 2){
                    short c2 = (short) Math.min(A2t - LL + end, 2 * end);
                    short c = (short) (Math.max(end, c2));
                    BiNew[1] = A2t;
                    BsNew[BookInfo[1]]++;
                    code2 = HashCode(0, 0, 0, BiNew, BsNew);
                    if (Payoffs.containsKey(code2)){ // new state, new com.jakubrojcek.gpr2005a.SinglePayoff created
                        pay2 = ((GPR2005Payoff_test3) Payoffs.get(code2));
                        HashMap<String, float[]> MuDv2 = pay2.getMuDv(BsNew, b, c);  // TODO: can I use BookSizes here?
                        mu2 = MuDv2.get("mu");
                        dV2 = MuDv2.get("dv");
                    } else {
                        HashMap<String, float[]> MuDv2 = InitalGPR2005.getMuDv(BsNew, b, c);  // TODO: can I use BookSizes here?
                        mu2 = MuDv2.get("mu");
                        dV2 = MuDv2.get("dv");
                    }
                    p2 = -1.0f;
                    q2 = 0;
                    for (j = b; j < c; j++){
                        if (j < end){
                            if ((j + LL) != BookInfo[1]){
                                q2 = (short) Math.min(- BsNew[j + LL], maxDepth);
                                p2 = (float)(mu2[j] * ((j - breakPoint) * tickSize - privateValue - dV2[j]));
                            }
                        } else {
                            q2 = (short) Math.min(BsNew[j - end + LL], maxDepth);
                            p2 = (float)((mu2[j]) * (privateValue + dV2[j] - (j - end - breakPoint) * tickSize));
                        }
                        //System.out.println(i+ "" + j);
                        if ((p1 + p2) > max){
                            max = p1 + p2;
                            MaxAction[0] = i;
                            MaxAction[1] = j;
                            MaxQ[0] = q1;
                            MaxQ[1] = q2;
                            code2max = code2;
                            mu2max = mu2[j];
                            dV2max = dV2[j];
                        }
                    }

                    j = (short) (2 * end);                      // BMO & SMO at the same time
                    q2 = 0;
                    p2 = (float)((BookInfo[0] - fvPos) * tickSize - privateValue);
                    //System.out.println(i+ "" + j);
                    if ((p1 + p2) > max){
                        max = p1 + p2;
                        MaxAction[0] = i;
                        MaxAction[1] = j;
                        MaxQ[0] = q1;
                        MaxQ[1] = q2;
                        code2max = code2;
                        mu2max = mu2[j];
                        dV2max = dV2[j];
                    }

                    j = (short) (2 * end + 1);
                    p2 = (float)((fvPos - A2t) * tickSize + privateValue);
                    //System.out.println(i+ "" + j);
                    if ((p1 + p2) > max){
                        max = p1 + p2;
                        mu2max = mu2[j];
                        dV2max = dV2[j];
                        if (A2t != At){                             // SMO
                            j = (short) (2 * end + 4);
                        }
                        MaxAction[0] = i;
                        MaxAction[1] = j;
                        MaxQ[0] = q1;
                        MaxQ[1] = q2;
                        code2max = code2;
                    }
                }
            } else if (i == 2 * end + 2) {                                            // no order
                q1 = 0;
                p1 = 0.0f;
                //p2[i] = (float) Math.random();
                if (units2trade == 2) {
                    for(j = b; j < a; j++){
                        if (j < end){
                            q2 = (short) Math.min(- BsNew[j + LL], maxDepth);
                            p2 = (float)(mu[j] * ((j - breakPoint) * tickSize - privateValue - dV[j]));
                        } else {
                            q2 = (short) Math.min(BsNew[j - end + LL], maxDepth);
                            p2 = (float)((mu[j]) * (privateValue + dV[j] - (j - end - breakPoint) * tickSize));
                        }
                        //System.out.println(i+ "" + j);
                        if ((p1 + p2) > max){
                            max = p1 + p2;
                            MaxAction[0] = i;
                            MaxAction[1] = j;
                            MaxQ[0] = q1;
                            MaxQ[1] = q2;
                            code2max = code;
                            mu2max = mu2[j];
                            dV2max = dV2[j];
                        }

                    }
                    j = (short) (2 * end);                      // NO and SMO
                    q2 = 0;
                    p2 = (float)((BookInfo[0] - fvPos) * tickSize - privateValue );
                    //System.out.println(i+ "" + j);
                    if ((p1 + p2) > max){
                        max = p1 + p2;
                        MaxAction[0] = i;
                        MaxAction[1] = j;
                        MaxQ[0] = q1;
                        MaxQ[1] = q2;
                        code2max = code;
                        mu2max = mu2[j];
                        dV2max = dV2[j];
                    }
                    j = (short) (2 * end + 1);                  // NO and BMO
                    p2 = (float)((fvPos - BookInfo[1]) * tickSize + privateValue);
                    //System.out.println(i+ "" + j);
                    if ((p1 + p2) > max){
                        max = p1 + p2;
                        MaxAction[0] = i;
                        MaxAction[1] = j;
                        MaxQ[0] = q1;
                        MaxQ[1] = q2;
                        code2max = code;
                        mu2max = mu2[j];
                        dV2max = dV2[j];
                    }
                    j = (short) (2 * end + 2);                  // NO and NO
                    p2 = 0.0f;
                    //System.out.println(i+ "" + j);
                    if ((p1 + p2) > max){
                        max = p1 + p2;
                        MaxAction[0] = i;
                        MaxAction[1] = j;
                        MaxQ[0] = q1;
                        MaxQ[1] = q2;
                        code2max = code;
                        mu2max = mu2[j];
                        dV2max = dV2[j];
                    }
                }
            }
            if (units2trade == 1 && p1 >= max){                 // equal because of exec probability at maxDepth is 0.0
                max = p1;
                MaxAction[0] = i;
                MaxAction[1] = 127;
                MaxQ[0] = q1;
                MaxQ[1] = 127;
                //System.out.println(q[0]);
            }
        }
        if (MaxQ[0] >= maxDepth){
            System.out.println("too low priority");
        }
        //System.out.println(q[0]);
        //MaxQ[0] = 0;
        pay.updateMax(MaxAction[0], MaxQ[0], mu, dV);
        if (units2trade == 2){
            if (Payoffs.containsKey(code2max)){
                pay2 = ((GPR2005Payoff_test3) Payoffs.get(code2max));
                pay2.updateMax(MaxAction[1], MaxQ[1], mu2max, dV2max);
                diff += pay2.getDiff();
            } else {
                statesCount++;
                System.out.println(statesCount);
                pay2 = new GPR2005Payoff_test3();
                Payoffs.put(code2max, pay2);
                pay2.updateMax(MaxAction[1], MaxQ[1], mu2max, dV2max);
                diff += pay2.getDiff();
            }
        }

        //if (pv == 2){b = (short) end;}
        //if (pv == 1){a = (short) end;}

        /*if (units2trade == 1){
            *//*for(short i = b; i < a; i++){
                q[0] = (i < end) ? (short) (- BookSizes[i + LL])
                                 : (short) BookSizes[i + LL - end];
                ps.put((short)((i<<7) + 127), (float)(p[i]*//**//* * Math.pow(0.96, q[0])*//**//*));
            }
            ps.put((short)(((2 * end)<<7) + 127),         p[2 * end]); // SMO
            ps.put((short)(((2 * end + 1)<<7) + 127), p[2 * end + 1]); // BMO
            ps.put((short)(((2 * end + 2)<<7) + 127), p[2 * end + 2]); // NO*//*
            for(short i = b; i < a; i++){ // searching for best payoff and not marketable LO
                q[0] = 0;//(i < end) ? (short) (- BookSizes[i + LL])
                        //: (short) BookSizes[i + LL - end];
                ps.put((short)((i<<7) + 127), (float)(p[i] * Math.pow(0.96, q[0])));
            }
            ps.put((short)(((2 * end)<<7) + 127),         p[2 * end]); // SMO
            ps.put((short)(((2 * end + 1)<<7) + 127), p[2 * end + 1]); // BMO
            ps.put((short)(((2 * end + 2)<<7) + 127), p[2 * end + 2]); // NO
        } else if (units2trade == 2){
            // Rule 1a and Rule 2
            for (short i = b; i < a; i++){
                for (short j = b; j < Math.min(i + 1, a); j++){                  // i + 1, bcz j can equal i
                    if (j > (i - end)){
                        q[0] = 0;//(i < end) ? (short) (- BookSizes[i + LL])
                                 //        : (short) BookSizes[i + LL - end];
                        q[1] = 0;//(j < end) ? (short) (- BookSizes[j + LL])
                                 //        : (short) BookSizes[j + LL - end];
                        ps.put((short)((i<<7) + j),
                                (float) (p[i] * Math.pow(0.96, q[0]) + p[j] * Math.pow(0.96, q[1])));
                    }
                }
            }

            // Rule 3
            if (BookInfo[2] > 1 || Bt == 0){                            // SMO
                ps.put((short)(((2 * end)<<7) + (2 * end)), (p[2 * end] + p[2 * end]));
            } else {
                *//*for (--B2t; B2t > 0; B2t--){                        // computing second best Bid
                    if(BookSizes[B2t] > 0){
                        break;
                    }
                }*//*
                ps.put((short)(((2 * end)<<7) + (2 * end + 3)), (p[2 * end] +
                        (float)((B2t - fvPos) * tickSize - privateValue )));        // second SMO
            }
            if (BookInfo[3] > 1 || (At == (nP - 1))){                   // BMO
                ps.put((short)(((2 * end + 1)<<7) + 2 * end + 1), (p[2 * end + 1] + p[2 * end + 1]));
            } else {
                *//*for (++A2t; A2t < (nP - 1); A2t++){                   // computing second best Ask
                    if(BookSizes[A2t] < 0){
                        break;
                    }
                }*//*
                ps.put((short)(((2 * end + 1)<<7) + 2 * end + 4), (p[2 * end + 1] +
                        (float)((fvPos - A2t) * tickSize + privateValue)));      // second BMO
            }

            // Rule 1b
            // SMO part
            short c2 = (short) Math.max(B2t - LL + 1, 0);
            short c = (short) Math.min(end, c2);
            for (int i = c; i < a; i++){
                    q[1] = 0;//(i < end) ? (short) (- BookSizes[i + LL])
                             //        : (short) BookSizes[i + LL - end];
                if (i != Bt - LL + end){
                    ps.put((short)(((2 * end)<<7) + i), (float)(p[2 * end] + p[i] * Math.pow(0.96, q[1])));
                }
            }
            // BMO part
            c2 = (short) Math.min(A2t - LL + end, 2 * end);
            c = (short)(Math.max(end, c2));

            for (int i = b; i < c; i++){
                q[1] = 0;//(i < end) ? (short) (- BookSizes[i + LL])
                         //        : (short) BookSizes[i + LL - end];
                if (i != At - LL){
                    ps.put((short)(((2 * end + 1)<<7) + i), (float)(p[2 * end + 1] + p[i] * Math.pow(0.96, q[1])));
                }
            }
            // BMO & SMO at the same time
            ps.put((short)(((2 * end + 1)<<7) + (2 * end)), (p[2 * end + 1] + p[2 * end]));

            // Rule 4
            for(short i = b; i < a; i++){                              // searching for best payoff and not marketable LO
                q[1] = 0;//(i < end) ? (short) (- BookSizes[i + LL])
                         //        : (short) BookSizes[i + LL - end];
                ps.put((short)(((2 * end + 2)<<7) + i), (float)(p[2 * end + 2] + p[i] * Math.pow(0.96, q[1])));

            }
            ps.put((short)(((2 * end + 2)<<7) + (2 * end)),     p[2 * end]);                      // SMO
            ps.put((short)(((2 * end + 2)<<7) + (2 * end + 1)), p[2 * end + 1]);                  // BMO
            ps.put((short)(((2 * end + 2)<<7) + (2 * end + 2)), p[2 * end + 2] + p[2 * end + 2]); // NO
            // Rule 1a and Rule 2
            *//*for(short i = b; i < a; i++){
                for (short j = b; j < Math.min(i + 1, a); j++){                  // i + 1, bcz j can equal i
                    if ((i - end) != j){
                        ps.put((short)((i<<7) + j), (p[i] + p[j]));
                    }
                }
            }

            // Rule 3
            if (BookInfo[2] > 1 || Bt == 0){                            // SMO
                ps.put((short)(((2 * end)<<7) + (2 * end)), (p[2 * end] + p[2 * end]));
            } else {
                *//**//*for (--B2t; B2t > 0; B2t--){                        // computing second best Bid
                    if(BookSizes[B2t] > 0){
                        break;
                    }
                }*//**//*
                ps.put((short)(((2 * end)<<7) + (2 * end + 3)), (p[2 * end] +
                        (float)((B2t - fvPos) * tickSize - privateValue )));        // second SMO
            }
            if (BookInfo[3] > 1 || (At == (nP - 1))){                   // BMO
                ps.put((short)(((2 * end + 1)<<7) + 2 * end + 1), (p[2 * end + 1] + p[2 * end + 1]));
            } else {
                *//**//*for (++A2t; A2t < (nP - 1); A2t++){                   // computing second best Ask
                    if(BookSizes[A2t] < 0){
                        break;
                    }
                }*//**//*
                ps.put((short)(((2 * end + 1)<<7) + 2 * end + 4), (p[2 * end + 1] +
                        (float)((fvPos - A2t) * tickSize + privateValue)));      // second BMO
            }

            // Rule 1b
            // SMO part
            short c2 = (short) Math.max(B2t - LL + 1, 0);
            short c = (short) Math.min(end, c2);
            for (int i = c; i < a; i++){
                ps.put((short)(((2 * end)<<7) + i), (p[2 * end] + p[i]));
            }
            // BMO part
            c2 = (short) Math.min(A2t - LL + end, 2 * end);
            c = (short)(Math.max(end, c2));

            for (int i = b; i < c; i++){
                ps.put((short)(((2 * end + 1)<<7) + i), (p[2 * end + 1] + p[i]));
            }
            // BMO & SMO at the same time
            ps.put((short)(((2 * end + 1)<<7) + (2 * end)), (p[2 * end + 1] + p[2 * end]));

            // Rule 4
            for(short i = b; i < a; i++){                              // searching for best payoff and not marketable LO
                ps.put((short)(((2 * end + 2)<<7) + i), (p[2 * end + 2] + p[i]));
            }
            ps.put((short)(((2 * end + 2)<<7) + (2 * end)),     p[2 * end]);                      // SMO
            ps.put((short)(((2 * end + 2)<<7) + (2 * end + 1)), p[2 * end + 1]);                  // BMO
            ps.put((short)(((2 * end + 2)<<7) + (2 * end + 2)), p[2 * end + 2] + p[2 * end + 2]); // NO*//*
        }*/



        /*if (!Payoffs.containsKey(code)){ // new state, new com.jakubrojcek.gpr2005a.SinglePayoff created
            statesCount++;
            System.out.println(statesCount);
            com.jakubrojcek.gpr2005a.GPR2005Payoff_test pay = new com.jakubrojcek.gpr2005a.GPR2005Payoff_test(ps, units2trade);
            Payoffs.put(code, pay); // insert a com.jakubrojcek.gpr2005a.Payoff object made of com.jakubrojcek.gpr2005a.GPR2005Payoff to the Payoffs table
            MaxIndex = pay.getMaxIndex();
            action[0] = (short) (MaxIndex>>7);
            action[1] = (short) (MaxIndex - (action[0]<<7));
        } else {
            /*com.jakubrojcek.gpr2005a.GPR2005Payoff_test2 pay = (com.jakubrojcek.gpr2005a.GPR2005Payoff_test2)Payoffs.get(code);
            Iterator keys = ps.keySet().iterator();
            while (keys.hasNext()){
                short key = (Short) keys.next();
                float tempPayoff = ps.get(key);
                float tempDiff = 0.0f;
                boolean LO = false;
                short acKey = 0;
                action[0] = (short) (key>>7);
                action[1] = (short) (key - (action[0]<<7));
                for (int i = 0; i < units2trade; i++){
                    acKey = action[i];
                    if (action[i] < end && pay.getX().containsKey(acKey)){
                        // SLO
                        LO = true;
                        tempDiff += (float)((pay.getX().get(acKey).getMu()) *
                                ((action[i] - breakPoint) * tickSize - privateValue -
                                        pay.getX().get(acKey).getDeltaV())) - p[action[i]];
                    } else if (action[i] < (2 * end) && pay.getX().containsKey(acKey)) {
                        // BLO
                        LO = true;
                        tempDiff += (float)((pay.getX().get(acKey).getMu()) *
                                (privateValue + pay.getX().get(acKey).getDeltaV() -
                                        (action[i] - breakPoint - end) * tickSize)) - p[action[i]];
                    }
                }
                if (LO){ps.put(key, tempDiff + tempPayoff);}*/
            /*com.jakubrojcek.gpr2005a.GPR2005Payoff_test pay = (com.jakubrojcek.gpr2005a.GPR2005Payoff_test)Payoffs.get(code);
            Iterator keys = pay.getX().keySet().iterator();
            while (keys.hasNext()){
                float tempPayoff = 0.0f;
                float tempDiff = 0.0f;
                boolean LO = false;
                short key = (Short) keys.next();
                action[0] = (short) (key>>7);
                action[1] = (short) (key - (action[0]<<7));
                for (int i = 0; i < units2trade; i++){
                    if (action[i] < (short) end){
                        // SLO
                        LO = true;
                        tempDiff += (float)((pay.getX().get(key)[i].getMu()) *
                                ((action[i] - breakPoint) * tickSize - privateValue -
                                        pay.getX().get(key)[i].getDeltaV())) - p[action[i]];
                    } else if (action[i] < (short) (2 * end)) {
                        // BLO
                        LO = true;
                        tempDiff += (float)((pay.getX().get(key)[i].getMu()) *
                                (privateValue + pay.getX().get(key)[i].getDeltaV() -
                                        (action[i] - breakPoint - end) * tickSize)) - p[action[i]];
                    }
                }
                if (LO && ps.containsKey(key)){
                    tempPayoff = ps.get(key);
                    ps.put(key, tempDiff + tempPayoff);
                }
            }
            if (prTremble > 0 && Math.random() < prTremble){
                pay.updateMax(ps, units2trade, true);
            } else {
                pay.updateMax(ps, units2trade, false);
                diff = pay.getDiff();
            }
            MaxIndex = pay.getMaxIndex();
            action[0] = (short) (MaxIndex>>7);
            action[1] = (short) (MaxIndex - (action[0]<<7));
        }*/
        // old code, don't delete, maybe still useful
        diff += pay.getDiff();
        orders = new ArrayList<Order>();
        byte outstanding = units2trade;
        boolean firstShare;
        for (int i = 0; i < units2trade; i++){
            buyOrder = false;
            firstShare = false;
            if (MaxAction[i] >= end){       //  || (action[i] != 2 * end + 3)
                buyOrder = true;
            }
            pricePosition = (MaxAction[i] < end) ? LL + MaxAction[i]
                                                 : LL + MaxAction[i] - end;
            if (MaxAction[i] == 2 * end){                                      // position is Bid
                pricePosition = BookInfo[0];
                buyOrder = false;
                outstanding--;
            } else if (MaxAction[i] == 2 * end + 1){                                  // position is Ask
                pricePosition = BookInfo[1];
                outstanding--;
            } else if (MaxAction[i] == 2 * end + 3){                                  // position is second Bid
                pricePosition = B2t;
                buyOrder = false;
                outstanding--;
            } else if (MaxAction[i] == 2 * end + 4){                                  // position is second Ask
                pricePosition = A2t;
                outstanding--;
            }
            if (i == 0){
                firstShare = true;
            }
            //com.jakubrojcek.Order CurrentOrder = new com.jakubrojcek.Order(traderID, EventTime, buyOrder, action[i], pricePosition);
            Order CurrentOrder = new Order(traderID, EventTime, buyOrder, firstShare, MaxAction[i], MaxQ[i], pricePosition);
            if (MaxAction[i] != (2 * end + 2)){orders.add(CurrentOrder);}
        }
        if (outstanding == 0){isTraded = true;}

        oldCode[0] = code;
        oldCode[1] = code2max;
        //MaxAction[0] = 17;
        if (writeDiagnostics){writeDiagnostics(diff, MaxAction);}
        units2trade = outstanding;
        if (writeDecisions){writeDecision(BookInfo, BookSizes, MaxAction);}
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
        if (oldAction[0] < end){ // sell LO executed
            payoff = (oldAction[0] - breakPoint) * tickSize - (fundamentalValue - PriceFV) - privateValue;
            //System.out.println("seller oldAction = " + oldAction + " payoff: " + payoff);
        } else {              // buy LO executed
            payoff = (breakPoint - (oldAction[0] - end - 1)) * tickSize + privateValue +
                    (fundamentalValue - PriceFV);
            //System.out.println("buyer oldAction = " + oldAction + " payoff: " + payoff);
        }
        if (Payoffs.containsKey(oldCode)){
            Payoff pay = Payoffs.get(oldCode);
            if (pay instanceof SinglePayoff){
                ((SinglePayoff) pay).update(oldAction[0], payoff, EventTime);
            } else {
                ((MultiplePayoff) pay).update(oldAction[0], payoff, EventTime);
            }
        }
        isTraded = true;
    }

    // execution as in GPR 2005 setting
    public void execution(double fundamentalValue, Order o){
        tradeCount++;
        long code;
        code = (o.isFirstShare()) ? oldCode[0] : oldCode[1];   // is it the first share or not
        /*if (!o.isFirstShare()){
            System.out.println("Second share exec");
        }*/
        if (Payoffs.containsKey(code)){
            GPR2005Payoff_test3 pay = (GPR2005Payoff_test3) Payoffs.get(code);
            if(pay.getX().containsKey((short) ((o.getAction() << 7) + o.getQ()))){
                pay.update(o.getAction(), o.getQ(), (float) (fundamentalValue - PriceFV), false);
            }
        }
       /* short[] action = new short[2];
        action[0] = (short) (oldAction>>7);
        action[1] = (short) (oldAction - (action[0]<<7));


        byte unitTraded = 0;
        if (action[1] != 127){
            unitTraded = (action[0] > o.getAction()) ? (byte) 1 : (byte) 0;
            if ((action[0] == action[1])){
                if (units2trade == 1 && execBefore){
                    unitTraded = 1;
                }
                execBefore = true;
            }
        }*/
        /*if (Payoffs.containsKey(oldCode)){
            if (!((com.jakubrojcek.gpr2005a.GPR2005Payoff_test) Payoffs.get(oldCode)).getX().containsKey(oldAction)){
                System.out.println("stop");
            } else {
                //((com.jakubrojcek.gpr2005a.GPR2005Payoff_test) Payoffs.get(oldCode)).update(oldAction, (float)(fundamentalValue - PriceFV), false, unitTraded);
            }
        }*/
       /* if (Payoffs.containsKey(oldCode)){
            if (((com.jakubrojcek.gpr2005a.GPR2005Payoff_test2) Payoffs.get(oldCode)).getX().containsKey(o.getAction())){
                ((com.jakubrojcek.gpr2005a.GPR2005Payoff_test2) Payoffs.get(oldCode)).update(o.getAction(), (float)(fundamentalValue - PriceFV), false);
            }
        }*/
        /*if (writeDiagnostics && (action[1] != 127)){
            action[0] = unitTraded; action[1] = unitTraded;
            writeDiagnostics(diff, action);
        }*/
        if (--units2trade == 0){isTraded = true;}

    }

    // use to cancel limit order in normal setting
    public void cancel(double EventTime){
        double payoff = 0.0;
        if (Payoffs.containsKey(oldCode)){
            Payoff pay = Payoffs.get(oldCode);
            if (pay instanceof SinglePayoff){
                ((SinglePayoff) pay).update(oldAction[0], payoff, EventTime);
            } else {
                ((MultiplePayoff) pay).update(oldAction[0], payoff, EventTime);
            }
        }
        isTraded = true;
    }

    // use to cancel limit order in a GPR2005 setting
    public void cancel(Order o){
        /*if (Payoffs.containsKey(oldCode)){
            if (((com.jakubrojcek.gpr2005a.GPR2005Payoff_test2) Payoffs.get(oldCode)).getX().containsKey(o.getAction())){
                ((com.jakubrojcek.gpr2005a.GPR2005Payoff_test2) Payoffs.get(oldCode)).update(o.getAction(), 0.0f, true);
            }
        }*/
        /*short[] action = new short[2];
        action[0] = (short) (oldAction>>7);
        action[1] = (short) (oldAction - (action[0]<<7));

        byte unitTraded = 0;
        if (action[1] != 127){
            unitTraded = (action[0] > o.getAction()) ? (byte) 1 : (byte) 0;
            if ((action[0] == action[1])){
                unitTraded = 1;
                if (!execBefore && units2trade == 1){
                    unitTraded = 0;
                }
            }
        }*/
        long code;
        code = (o.isFirstShare()) ? oldCode[0] : oldCode[1];   // is it the first share or not
        if (Payoffs.containsKey(code)){
            GPR2005Payoff_test3 pay = (GPR2005Payoff_test3) Payoffs.get(code);
            if(pay.getX().containsKey((short) ((o.getAction() << 7) + o.getQ()))){
                pay.update(o.getAction(), o.getQ(), 0.0f, true);
            }
        } else {
            System.out.println("stop & think");
        }

        if (--units2trade == 0){isTraded = true;}

        /*if (writeDiagnostics && (action[1] != 127)){
            action[0] = (short)(unitTraded + 2); action[1] = (short)(unitTraded + 2);
            writeDiagnostics(diff, action);
        }*/
    }

    // writing decisions
    private void writeDecision(int[] BookInfo, int[] BookSizes, Short[] action){
        // tables I, V

        previousTraderAction[0] = decision.addDecision(BookInfo, action, previousTraderAction);
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
    private void writeDiagnostics(double diff, Short[] ac){
        diag.addDiff(diff);
        diag.addAction(ac, units2trade);
    }


    // Binomial distribution
    public double Binomial(int x, int n, double delta){    /* x is number of drops, n is number of orders on books */
        double f = 1.0 ;
        int i;
        for(i = n;   i > x; i--)  f *= (double)i;    /* f = n! / x! = n*(n-1)*...*(x+1), since x<n.  Faster than computing n! , x! separately */
        for(i = n-x; i > 1; i--)  f /= (double)i;    /* now f = n! / ( x! * (n-x)! ) = "n choose x" */
        return( f * Math.pow(delta, (double) x ) * Math.pow( 1.0 - delta, (double)(n - x)));
    }
    // computes initial beliefs for GPR2005 Payoffs
    public HashMap<String, Float[][]> computeInitialBeliefs(float deltaLow, double muND, double stdevND){
        // compute execution probabilities
        NormalDistribution nd = new NormalDistribution(muND, stdevND);   // mean 0, stddev $0.35 - GPR 2005
        int t1;
        int m1;
        double v;
        double w;
        double v1;
        double FB;
        double u = 1.0 - deltaLow;
        mu0 = new Float[nPayoffs][maxDepth + 1];
        deltaV0 = new Float[nPayoffs][maxDepth + 1];
        for (int i = 0; i < end; i++){      // Limit order payoffs for ticks LL through HL
            FB = nd.cumulativeProbability((i - breakPoint) * tickSize);  // probability of interested seller coming
            mu0[i][0] = (float) ((u * (1.0 - FB)) / (1.0 - u * FB));
            mu0[i][1] = (float) (u / (1.0 - u * u * FB) * ((1.0 + deltaLow) / 2.0 * (1.0 - FB) +
                    deltaLow * mu0[i][0] + (1.0 - 3.0  * deltaLow) / 2.0 *
                    (1.0 - FB) * mu0[i][0]));
            deltaV0[i][0] = (float)( - (i + 1) * tickSize);
            deltaV0[i][1] = (float)( - (i + 1) * tickSize);
            // SLOs
            mu0[i + end][0] = (float) ((u * FB) / (1.0 - u * (1.0 - FB)));
            mu0[i + end][1] = (float) (u / (1.0 - u  * u * (1.0 - FB)) * ((1.0 + deltaLow) / 2.0 * FB +
                    deltaLow * mu0[i + end][0] + (1.0 - 3.0  * deltaLow) / 2.0 *
                    FB * mu0[i + end][0]));
            deltaV0[i + end][0] = (float)((end - i) * tickSize);
            deltaV0[i + end][1] = (float)((end - i) * tickSize);
            // BLOs
            /*for (int k = 2; k < maxDepth; k++){
                t1 = k + 1;     *//* in Uday exposition: m1 is m, t1 is k.  NOTE: I distribute the (1-delta) in Uday to retain Binomial() *//*
                m1 = t1 - 2;    *//* m= k-2 case in Uday exposition handled outside m1 loop since eUB[i][-1][p] = 1 has negative index.   *//*
                v =     Binomial(m1, t1 - 1, deltaLow) * (FB * (1.0 + mu0[i][t1 - m1 - 1 - 1]) /
                        2.0 + (1.0 - FB) * mu0[i][t1 - m1 - 1]);
                w =     Binomial(m1, t1 - 1, deltaLow) * ((1.0 - FB) * (1.0 + mu0[i + end][t1 - m1 - 1 - 1]) /
                        2.0 + FB * mu0[i + end][t1 - m1 - 1]);
                for (m1 = 1; m1 < t1 - 2; m1++){
                    v +=  Binomial(m1, t1 - 1, deltaLow) * (FB * (mu0[i][t1-m1-2 -1] +
                            mu0[i][t1 - m1 - 1 - 1]) / 2.0 + (1.0 - FB) * mu0[i][t1 - m1 - 1]);  *//* extra -1 for C index *//*
                    w +=  Binomial(m1, t1 - 1, deltaLow) * ((1.0 - FB) * (mu0[i + end][t1 - m1 - 2 -1] +
                            mu0[i + end][t1 - m1 - 1 - 1]) / 2.0 + FB * mu0[i + end][t1 - m1 - 1]);
                }
                v1 = Math.pow(u,(double)t1);
                mu0[i][k]=  (float) ((v1 * FB * (mu0[i][k - 2] + mu0[i][k-1]) / 2.0 + u * v ) /
                        (1.0 - v1 * (1.0 - FB)));
                mu0[i + end][k]=  (float)((v1 * (1 - FB) * (mu0[i + end][k-2] + mu0[i + end][k-1]) / 2.0 + u * w ) /
                        (1.0 - v1*(FB)));
                deltaV0[i][k] = (float)( -(i + 1) * tickSize);
                deltaV0[i + end][k] = (float)((end - i) * tickSize);
            }*/
            for (int k = 2; k < maxDepth; k++){
                t1 = k + 1;     /* in Uday exposition: m1 is m, t1 is k.  NOTE: I distribute the (1-delta) in Uday to retain Binomial() */
                m1 = t1 - 2;    /* m= k-2 case in Uday exposition handled outside m1 loop since eUB[i][-1][p] = 1 has negative index.   */
                v =     Binomial(m1, t1 - 1, deltaLow) * ((1.0 - FB) * (1.0 + mu0[i][t1 - m1 - 1 - 1]) /
                        2.0 + FB * mu0[i][t1 - m1 - 1]);
                w =     Binomial(m1, t1 - 1, deltaLow) * (FB * (1.0 + mu0[i + end][t1 - m1 - 1 - 1]) /
                        2.0 + (1.0 - FB) * mu0[i + end][t1 - m1 - 1]);
                for (m1 = 1; m1 < t1 - 2; m1++){
                    v +=  Binomial(m1, t1 - 1, deltaLow) * ((1.0 - FB) * (mu0[i][t1 - m1 - 2 -1] +
                            mu0[i][t1 - m1 - 1 - 1]) / 2.0 + FB * mu0[i][t1 - m1 - 1]);
                    w +=  Binomial(m1, t1 - 1, deltaLow) * (FB * (mu0[i + end][t1-m1-2 -1] +
                            mu0[i + end][t1 - m1 - 1 - 1]) / 2.0 + (1.0 - FB) * mu0[i + end][t1 - m1 - 1]);  /* extra -1 for C index */
                }
                v1 = Math.pow(u,(double)t1);
                mu0[i][k]=  (float)((v1 * (1 - FB) * (mu0[i][k-2] + mu0[i][k-1]) / 2.0 + u * w ) /
                        (1.0 - v1*(FB)));
                mu0[i + end][k]=  (float) ((v1 * FB * (mu0[i + end][k - 2] + mu0[i + end][k-1]) / 2.0 + u * v ) /
                        (1.0 - v1 * (1.0 - FB)));
                deltaV0[i][k] = (float)( -(i + 1) * tickSize);
                deltaV0[i + end][k] = (float)((end - i) * tickSize);
            }
            mu0[i][maxDepth] = 0.0f;
            deltaV0[i][maxDepth] = 99f;
            mu0[i + end][maxDepth] = 0.0f;
            deltaV0[i + end][maxDepth] = -99f;
        }
        mu0[2 * end][0] = 1.0f;                             // market sell order
        mu0[2 * end + 1][0] = 1.0f;                         // market buy order
        mu0[2 * end + 2][0] = 0.0f;                         // no order

        // compute FV change upon execution
        deltaV0[2 * end][0] = 0.0f;                           // market sell order
        deltaV0[2 * end + 1][0] = 0.0f;                       // market buy order
        deltaV0[2 * end + 2][0] = 0.0f;                       // no order
        InitialPayoff.setInitialBeliefs(mu0, deltaV0);
        HashMap<String, Float[][]> tempInitialBeliefs = new HashMap<String, Float[][]>();
        tempInitialBeliefs.put("mu0", mu0);
        tempInitialBeliefs.put("deltaV0", deltaV0);
        return tempInitialBeliefs;
    }

    // Hash code computed dependent on various Information Size (2, 4, 6, 7, 8)
    public Long HashCode(int P, int q, int x, int[] BookInfo, int [] BS){
        long code = (long) 0;
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
            long tempCode;
            int buy;
            for (int i = 1; i < nP - 1; i++){
                buy = (BS[i] > 0) ? 1 : 0;
                tempCode = (code<<5) + (buy<<4) + Math.abs(BS[i]);
                code = tempCode;
            }
            /*long Bt = BookInfo[0];                  // Best Bid position
            long At = BookInfo[1];                  // Best Ask position
            long lBt = BookInfo[2];                 // depth at best Bid
            long lAt = BookInfo[3];                 // depth at best Ask
            long dBt = (BookInfo[4]);               // depth at buy
            int dSt = (BookInfo[5]);                // depth at sell
            int l = (isHFT) ? 1 : 0;                // arrival frequency slow (0), fast (1)
            //int u2t = (units2trade == 2) ? 1 : 0;
            int u2t = 0;

            *//*code = (Bt<<30) + (At<<26) + (lBt<<21) + (lAt<<16) +
                    (dBt<<9) + (dSt<<2) + (l<<1) + u2t;*//*
            code = (B2<<48) + (A2<<44) + (lB2<<39) + (lA2<<34) + (Bt<<30) + (At<<26) + (lBt<<21) + (lAt<<16) +
                    (dBt<<9) + (dSt<<2) + (l<<1) + u2t;*/

           /* boolean[] test = new boolean[13];
            long code2 = code;
            test[0] = (code2>>27 == Bt);
            System.out.println(test[0]);
            code2 = code - (Bt<<27);
            test[1] = (code2>>22 == At);
            System.out.println(test[1]);
            code2 = code2 - (At<<22);
            test[2] = (code2>>18 == lBt);
            System.out.println(test[2]);
            code2 = code2 - (lBt<<18);
            test[3] = (code2>>14 == lAt);
            System.out.println(test[3]);
            code2 = code2 - (lAt<<14);
            test[4] = (code2>>8 == dBt);
            System.out.println(test[4]);
            code2 = code2 - (dBt<<8);
            test[5] = (code2>>2 == dSt);
            System.out.println(test[5]);
            code2 = code2 - (dSt<<2);
            test[6] = (code2>>1 == l);
            System.out.println(test[6]);
            code2 = code2 - (l<<1);
            test[7] = (code2 == u2t);
            System.out.println(test[7]);
            code2 = code2 - u2t;
            System.out.println(Long.toBinaryString(code));
            if (code2 !=0){
                System.out.println("problem");
            } */
            /*boolean[] test = new boolean[2 * (nP - 2)];
            long code2 = code;
            int buy2;
            long bs;
            for (int i = nP - 2; i > 0; i--){
                buy2 = (BS[nP - 1 - i] > 0) ? 1 : 0;
                test[nP - 3 + i] = (code2 >> ((i - 1) * 5 + 4)) == buy2;
                code2 = code2 - (buy2 << ((i - 1) * 5 + 4));
                bs = code2 >> ((i - 1) * 5);
                test[i - 1] = bs == Math.abs(BS[nP - 1 - i]);
                code2 = code2 - (bs<<(i - 1) * 5);
            }
            if (code2 !=0){
                System.out.println("problem");
            }*/// tests

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

               /* if (pay instanceof com.jakubrojcek.gpr2005a.SinglePayoff){
                    writer.writeDecisions(et - ((com.jakubrojcek.gpr2005a.SinglePayoff) pay).getEventTime() + ";" + "\r");
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

    // deletes the com.jakubrojcek.gpr2005a.Payoff of a state which has fromPreviousRound set to true
    public void purge(){
        double rn;
        Iterator it = Payoffs.keySet().iterator();
        ArrayList<Long> toDelete = new ArrayList<Long>();
        int all = 0;
        int deleted = 0;
        Long code;
        while (it.hasNext()){
            all++;
            code = (Long) it.next();
            rn = Math.random();
            if (rn < 0.25){                        // portion of cancelled payoffs
                toDelete.add(code);
                deleted++;
            }
        }
        for (Long L : toDelete){
            Payoffs.remove(L);
        }
        System.out.println("all: " + all + " deleted: " + deleted);
    }

    // resets n in Payoffs, sets purge indicator to true-> true until next time the state is hit
    public void nReset(byte n, short m){
        Long code;
        Payoff pay;
        Iterator keys = Payoffs.keySet().iterator();
/*        code = (Long) keys.next();
        pay =  Payoffs.get(code);
        pay.setnReset(n, m);
        pay.nReset();*/
        while (keys.hasNext()){
            code = (Long) keys.next();
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
            writer.write(diag.printDiagnostics("diffs"));
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
        Iterator keys = Payoffs.keySet().iterator();
        try{
            String outputFileName = folder + "convergence.csv";
            FileWriter writer = new FileWriter(outputFileName, true);
            while (keys.hasNext()){
                code = (Long) keys.next();
                pay =  Payoffs.get(code);
                if (pay instanceof MultiplePayoff){
                    writer.write(((MultiplePayoff) pay).printDiff(t2));
                }
            }
            keys = Payoffs.keySet().iterator();
            if (keys.hasNext()){
                Payoffs.get(keys.next()).setDof(0);
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

    // resets com.jakubrojcek.Decision history, memory management
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

    public long[] getOldCode(){
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

    public Short[] getOldAction(){
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

    public HashMap<Long, Payoff> getPayoffs(){
        return Payoffs;
    }

    public static HashMap<Short, Float> getPs() {
        return ps;
    }
}
