package com.jakubrojcek.hftRegulation;

import com.jakubrojcek.*;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import java.io.FileWriter;
import java.util.*;

import com.jakubrojcek.*;

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
    private boolean isTraded = false;   // set by TransactionRule in book
    private boolean isReturning = false;// is he returning this time? info from priorities
    private float rho = 0.05f;          // trading "impatience" parameter
    private double PriceFV;             // current fundamental value-> price at middle position
    private double EventTime = 0.0;     // event time
    private BeliefQ belief;             // reference to an old Belief, to be updated
    private Order order = null;         // reference to an old Order, to update the old Belief

    static int TraderCount = 0;         // counting number of traders, gives traderID as well
    static int tradeCount = 0;          // counting number of trader
    static int statesCount = 0;         // counting number of unique states
    static HashMap<Long, HashMap<Integer, BeliefQ>> states;/* Beliefs about payoffs for different actions
    + max + maxIndex in state=code */
    HashMap<Integer, BeliefQ> tempQs;
    LOB_LinkedHashMap book;             // reference to the book
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
    static int nResetMax = 32767;       // nReset-> resets n to specific value for "forced learning" 32767 is max short             //
    static double prTremble = 0.0;      // probability of trembling
    static boolean writeDecisions = false;   // to writeDecisions things in trader?
    static boolean writeDiagnostics = false; // to writeDiagnostics things in trader?
    static boolean writeHistogram = false;   // to writeHistogram in trader?
    static String folder;
    static Decision decision;
    static Diagnostics diag;
    static int [] bookSizesHistory;
    static int[] previousTraderAction;
    static ArrayList<Order> orders;     // result of decision, passed to LOB.transactionRule
    static HashMap<Short, Float> ps;
    static boolean fixedBeliefs = false;// when true, doesn't update, holds beliefs fixed
    static boolean similar = false;     // looks for similar state|action if action not in current state
    static boolean symm = false;        // uses/updates/stores seller's beliefs for buyer
    //private double CFEE;              // cancellation fee


    // constructor bool, bool, float, int
    public Trader(boolean HFT, float privateValue) {
        this.isHFT = HFT;
        this.privateValue = privateValue;
        TraderCount++;
        this.traderID = TraderCount;
        if (privateValue > 0){pv = 2;}
        else if (privateValue < 0){pv = 1;}
        else {pv = 0;}
    }

    // constructor of the main trader- loads parameters from main
    public Trader(int is, double[] tb, double[] ts, byte numberPrices, int FVpos, double tickS, double rFast, double rSlow,
                  int ll, int hl, int e, int md, int bp, int hti, double pt, String f, LOB_LinkedHashMap b){
        states = new HashMap<Long, HashMap<Integer, BeliefQ>>(hti);
        book = b;
        infoSize = is;
        LL = ll;
        HL = hl;
        end = e;
        breakPoint = bp;
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
    }

    // decision about the price is made here, so far random
    public ArrayList<Order> decision(int[] BookSizes, int[] BookInfo,
                               double et, double priceFV){
        orders = new ArrayList<Order>();
        PriceFV = priceFV;                  // get price of the fundamental value = fundamental value
        int pricePosition;                  // pricePosition at which to submit
        boolean buyOrder = false;           // buy order?

        int oldPos = 0;
        int q = 0;
        int x = 0;
        if (isReturning){                                       // pricePosition position in previous action
            oldPos = order.getPosition() - book.getPositionShift();
            q  = order.getQ();
            x = order.isBuyOrder() ? 1 : 0;
        }
        // TODO: make sure, he doesn't execute against himself
        long Bt = BookInfo[0];              // Best Bid position
        long At = BookInfo[1];              // Best Ask position
        Long code = HashCode(oldPos, q, x, BookInfo, BookSizes);
        // TODO: do I need to declare the tempQs here or is static OK? otherwise it erases the states entries??
        tempQs = states.containsKey(code) ? states.get(code)
                                          : new HashMap<Integer, BeliefQ>();
        int action = -1, nLO = 0;               // TODO: if initialize to NO, then make nPayoffs shorter? or just the for loop till nPayoffs - 1?
        double max = -1.0, p1 = -1.0, sum = 0.0;

        short b = (short) Math.max(BookInfo[0] - LL + 1, 0);             // + 1 in order to start from one above B
        b = (short) Math.min(end, b);
        short a = (short) Math.min(BookInfo[1] - LL + end, 2 * end);
        a = (short) Math.max(end, a);

        if (isReturning){
            action = order.isBuyOrder() ? oldPos - LL + end
                                        : oldPos - LL;
            if (action >= b && action < a){                         // still in the range for LO, else is cancelled for sure TODO: b and a work here?
                p1 = order.isBuyOrder() ? (discountFactorS.get(action - end)[Math.abs(order.getQ())] * ((breakPoint - action - end) * tickSize + privateValue))
                                        : (discountFactorB.get(action)[Math.abs(order.getQ())] * ((action - breakPoint) * tickSize - privateValue));
                // TODO: make sure the Q updates as the priority increases-> in the book
                max = tempQs.containsKey(action) ? tempQs.get(action).getQ()
                                                 : -1.0;
                max = Math.max(max, p1);                            // max, because priority should have improved
                                                    // TODO: but still, won't this create some cicularity in udpating?
            } else {action = -1;}                                   // otherwise could have action == 2 * end and the SMO would not be computed later
        }

        for(int i = b; i < nPayoffs; i++){ // searching for best payoff
            p1 = -1.0f;
            if (i != action){
                if (tempQs.containsKey(i)){
                    p1 = tempQs.get(i).getQ();
                } else {
                    if (i < end){                                                // payoff to sell limit order
                        p1 = (discountFactorB.get(i)[Math.abs(BookSizes[LL + i])] *
                                ((i - breakPoint) * tickSize - privateValue));
                    } else if (i < a) {                                          // payoff to buy limit order
                        p1 = (discountFactorS.get(i - end)[Math.abs(BookSizes[LL + i - end])] *
                                ((breakPoint - i - end) * tickSize + privateValue));
                    } else if (i == (2 * end)){
                        p1 = ((Bt - fvPos) * tickSize - privateValue);           // payoff to sell market order
                    } else if (i == (2 * end + 1)){
                        p1 = ((fvPos - At) * tickSize + privateValue);           // payoff to buy market order
                    } else if (i == (2 * end + 2)){
                        double Rt = (isHFT) ? 1.0 / ReturnFrequencyHFT           // TODO: can I integrate over future differently?
                                            : 1.0 / ReturnFrequencyNonHFT;       // expected return time
                        p1 = (Math.exp(-rho * (Rt)) * (sum / Math.max(1, nLO))); // 2 for averaging over 14
                    }
                }
                if (p1 > 0){
                    nLO++;
                    sum += p1;
                    if (p1 > max){
                        max = p1;
                        action = i;
                    }
                }
            }
        }

        if (isReturning){                                  // updating old belief if trader is returning
            if(belief.getN() < nResetMax) {
                belief.increaseN();
            }
            double alpha = (1.0/(1.0 + (belief.getN())));  // updating factor
            double previousQ = belief.getQ();
            belief.setQ((1.0 - alpha) * previousQ +
                    alpha * Math.exp( - rho * (EventTime - et)) * max);
        }

        if (tempQs.containsKey(action)){
            belief = tempQs.get(action);                   // obtaining the belief-> store as private value
        } else {
            belief = new BeliefQ((short) 1, max);
            tempQs.put(action, belief);                    // obtaining the belief-> store as private value
        }
        // TODO: put the tempQs to the states HashMap, otherwise what is this exercise for?
        //if (writeDiagnostics){writeDiagnostics(diff, (short)action);}

        // creating an order
        if (action > end){
            buyOrder = true;
        }
        pricePosition = (action < end) ? action + LL
                                       : action + LL - end;
        if (action == 2 * end){                                     // position is Bid
            pricePosition = BookInfo[0];
            buyOrder = false;
        }
        if (action == 2 * end + 1){pricePosition = BookInfo[1];}    // position is Ask
        Order currentOrder = new Order(traderID, et, buyOrder, 1, 0, pricePosition);
        if (isReturning){
            if ((order != null)){
                oldPos = order.getPosition() - book.getPositionShift();
                if (action == (2 * end + 2)){
                    order.setCancelled(true);
                    Order orderCancelled = order;
                    orders.add(orderCancelled);
                } else if ((oldPos != pricePosition) || (order.isBuyOrder() != buyOrder)){
                    order.setCancelled(true);
                    Order orderCancelled = order;
                    orders.add(orderCancelled);
                    orders.add(currentOrder);
                    order = currentOrder;
                }
            } else if (action != (2 * end + 2)){
                order = currentOrder;
                orders.add(currentOrder);
            }
        } else if (action != (2 * end + 2)){
            order = currentOrder;
            orders.add(currentOrder);
        }
        isReturning = true;
        if (action == 2 * end || action == 2 * end + 1) {
            isTraded = true;                                    // isTraded set to true if submitting MOs
        }

        //if (writeDecisions){writeDecision(BookInfo, BookSizes, (short)action);}                                     // printing data for output tables
        EventTime = et;
        return orders;
    }

    // used to update payoff of action taken in a state upon execution
    public void execution(double fundamentalValue, double et){
        int pos = order.getPosition() - book.getPositionShift();
        double payoff;
        tradeCount++;            // TODO: change using action to using only FV, FVpos
        if (order.isBuyOrder()){                    // buy LO executed
            payoff = (breakPoint - (pos - LL)) * tickSize + privateValue
                    + (fundamentalValue - PriceFV);
        } else {                                // sell LO executed
            payoff = (pos - LL - breakPoint) * tickSize - privateValue
                    - (fundamentalValue - PriceFV);
        }
        if(belief.getN() < nResetMax) {
            belief.increaseN();
        }
        double alpha = (1.0/(1.0 + (belief.getN())));  // updating factor
        double previousQ = belief.getQ();
        belief.setQ((1.0 - alpha) * previousQ +
                alpha * Math.exp( - rho * (EventTime - et)) * payoff);
        isTraded = true;
    }

    // use to cancel limit order in normal setting
    public void cancel(double et){
        order = null;
    }

    // writing decisions
    private void writeDecision(int[] BookInfo, int[] BookSizes, Short[] action){
        // tables I, V

        previousTraderAction[0] = decision.addDecision(BookInfo, action, previousTraderAction);
        previousTraderAction[1] = BookInfo[0];
        previousTraderAction[2] = BookInfo[1];
    }

    public void writeHistogram(int[] BookSizes){
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
    private void writeDiagnostics(double diff, Short[] ac, double max){
        diag.addDiff(diff);
        diag.addAction(ac, (byte)1, max);
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
        /*Payoff pay;
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

                *//* if (pay instanceof com.jakubrojcek.gpr2005a.SinglePayoff){
                    writer.writeDecisions(et - ((com.jakubrojcek.gpr2005a.SinglePayoff) pay).getEventTime() + ";" + "\r");
                }*//*
            }
            writer.close();
            writer2.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }*/
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
        /*double rn;
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
        System.out.println("all: " + all + " deleted: " + deleted);*/
    }

    // resets n in Payoffs, sets purge indicator to true-> true until next time the state is hit
    public void nReset(byte n, short m){
        /*Long code;
        Payoff pay;
        Iterator keys = Payoffs.keySet().iterator();
*//*        code = (Long) keys.next();
        pay =  Payoffs.get(code);
        pay.setnReset(n, m);
        pay.nReset();*//*
        while (keys.hasNext()){
            code = (Long) keys.next();
            pay =  Payoffs.get(code);
            ((GPR2005Payoff_test3)pay).nReset(n, m);
            if (pay instanceof SinglePayoff){
                ((SinglePayoff) pay).setFromPreviousRound(true);
            } else if (pay instanceof MultiplePayoff) {
                ((MultiplePayoff) pay).setFromPreviousRound(true);
            }
        }*/
    }

    // prints diagnostics collected from data in decisions
    public void printDiagnostics(){
        try{
            String outputFileName = folder + "diagnostics1.csv";
            FileWriter writer = new FileWriter(outputFileName, true);
            writer.write(diag.printDiagnostics("diffs"));
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        try{
            String outputFileName = folder + "diagnostics2.csv";
            FileWriter writer = new FileWriter(outputFileName, true);
            writer.write(diag.printDiagnostics("payoffs"));
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void printConvergence(int t2){
        /*Long code;
        //Payoff pay;
        GPR2005Payoff_test3 pay;
        Iterator keys = Payoffs.keySet().iterator();
        Iterator keys2;
        Short acKey;
        int DOF = 0;            // degrees of freedom
        int NN = 0;             // number of times visited
        int Ee = 0;             // number of times executed
        double e = 0.0;         // estimated execution probability
        double mu = 0.0;        // empirical execution probability
        double chiSq = 0.0;     // chiSq statistic
        double sumDiff = 0.0;   // average difference
        BeliefQ beliefConv;

        try{
            String outputFileName = folder + "convergence.csv";
            FileWriter writer = new FileWriter(outputFileName, true);
            while (keys.hasNext()){
                code = (Long) keys.next();
                pay =  (GPR2005Payoff_test3)Payoffs.get(code);
                keys2 = pay.getX().keySet().iterator();
                while (keys2.hasNext()){
                    acKey = (Short) keys2.next();
                    beliefConv = pay.getX().get(acKey);
                    NN = beliefConv.getN();
                    Ee = beliefConv.getNe();
                    e = beliefConv.getQ();
                    mu = (double)Ee/NN;
                    mu = Math.abs(mu - e);
                    if (NN >= t2){
                        DOF++;

                        if (e != 1.0 || e != 0.0) {
                            chiSq += mu * mu * NN / (e * (1 - e));
                        }
                        sumDiff += mu;
                    }
                    writer.write(NN + ";" + Ee + ";" + e + ";" + mu + ";");
                    writer.write("\r");
                }
            }
            writer.write("chiSq = :;" + chiSq + ";DOF:;" + DOF + ";sumDiff:;" + sumDiff);
            writer.write("\r");
            *//*keys = Payoffs.keySet().iterator();
            if (keys.hasNext()){
                Payoffs.get(keys.next()).setDof(0);
            }*//*
            *//*while (keys.hasNext()){
                code = (Long) keys.next();
                pay =  Payoffs.get(code);
                if (pay instanceof MultiplePayoff){
                    writer.write(((MultiplePayoff) pay).printDiff(t2));
                }
            }
            keys = Payoffs.keySet().iterator();
            if (keys.hasNext()){
                Payoffs.get(keys.next()).setDof(0);
            }*//*
            writer.close();
        }
        catch (Exception ex){
            ex.printStackTrace();
            System.exit(1);
        }*/
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

    public void setFixedBeliefs(boolean f){
        fixedBeliefs = f;
    }

    public void setSimilar(boolean s){
        similar = s;
    }

    public void setSymm(boolean s){
        symm = s;
    }

    public static HashMap<Short, Float> getPs() {
        return ps;
    }

    public void setStatesCount(int n){
        statesCount = n;
    }
}
