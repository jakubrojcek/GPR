package com.jakubrojcek.hftRegulation;

import com.jakubrojcek.*;

import java.io.*;
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
    private int traderID; // initialized
    private float privateValue; // initialized, can be changed to double later on
    private int pv; // holder for private value 0(0), 1(negative), 2(positive)
    private boolean isHFT; // initialized
    private boolean isTraded = false; // set by TransactionRule in book
    private boolean isReturning = false;// is he returning this time? info from priorities
    private float rho = 0.15f; // trading "impatience" parameter
    private double PriceFV; // current fundamental value-> price at middle position
    private double EventTime = 0.0; // event time
    private BeliefQ belief; // reference to an old Belief, to be updated
    private Order order = null; // reference to an old Order, to update the old Belief
    private short cancelCount = 0;

    static int TraderCount = 0;         // counting number of traders, gives traderID as well
    static int TraderCountHFT = 0;      // counting HFT traders
    static int TraderCountNonHFT = 0;   // counting nonHFT traders
    static int tradeCount = 0;          // counting number of trader
    static int statesCount = 0;         // counting number of unique states
    static double [] FprivateValues;
    static HashMap<Long, HashMap<Integer, BeliefQ>> states;/* Beliefs about payoffs for different actions
+ max + maxIndex in state=code */
    static HashMap<Long, HashMap<Integer, BeliefQ>> previousStates;    // used to compute convergence type 1
    static HashMap<Long, HashMap<Integer, BeliefQ>> convergenceStates = new HashMap<Long, HashMap<Integer, BeliefQ>>();
    // used to compute convergence type 2
    static ArrayList<Integer> updatedTraders = new ArrayList<Integer>();// list of traders already updated their one-step-ahead beliefs
    static previousStates statesConstructor;    // copy constructor for previous states
    static HashMap<Integer, BeliefQ> tempQs;
    static LOB_LinkedHashMap book;              // reference to the book
    static double ReturnFrequencyHFT;           // returning frequency of HFT
    static double ReturnFrequencyNonHFT;        // returning frequency of NonHFT
    static Hashtable<Integer, Double[]> discountFactorB = new Hashtable<Integer, Double[]>(); // container for discount factors computed using tauB
    static Hashtable<Integer, Double[]> discountFactorS = new Hashtable<Integer, Double[]>(); // container for discount factors computed using tauS
    static double[] numberOfCancels;            // initial beliefs for number of cancellations
    static Random random = new Random();        // random generator, used for trembling
    static int infoSize;                        // 2-bid, ask, 4-last price, direction, 6-depth at bid,ask, 8-depth off bid,ask
    static byte nP;                             // number of prices
    static double tickSize;                     // size of one tick
    static int LL;                              // Lowest allowed limit order price. LL + HL = nP-1 for allowed orders centered around E(v)
    static int HL;                              // Highest allowed limit order price
    static int end;                             // HL - LL + 1 - number of LO prices on the grid
    static int maxDepth;                        // 0 to 7 which matter
    static int breakPoint;                      // breaking point for positive, negative, represents FV position on the LO grid
    static int nPayoffs;                        // size of payoff vectors
    static int fvPos;                           // tick of fundamental value
    static int nResetMax = 32767;               // nReset-> resets n to specific value for "forced learning" 32767 is max short //
    static double prTremble = 0.0;              // probability of trembling
    static boolean writeDecisions = false;      // to writeDecisions things in trader?
    static boolean writeDiagnostics = false;    // to writeDiagnostics things in trader?
    static boolean writeHistogram = false;      // to writeHistogram in trader?
    static boolean online = false;              // updates beliefs also for returning trader
    static String folder;
    static Decision decision;                   // decisions of all nonHFT and liquidity provision
    static Decision decision_4;                 // decisions of PV -4
    static Decision decision_2;                 // decisions of PV -2
    static Decision decision0;                  // decisions of PV 0
    static Decision decision2;                  // decisions of PV +2
    static Decision decision4;                  // decisions of PV +4
    static Decision decisionHFT;                // decisions of HFTs

    static Diagnostics diag;
    static int[] bookSizesHistory;
    static int[] previousTraderAction;
    static ArrayList<Order> orders;             // result of decision, passed to LOB.transactionRule
    static HashMap<Short, Float> ps;
    static boolean fixedBeliefs = false;        // when true, doesn't update, holds beliefs fixed
    static boolean similar = false;             // looks for similar state|action if action not in current state
    static boolean symm = false;                // uses/updates/stores seller's beliefs for buyer
    static double CFEE = 0.0f;                   // cancellation fee
    static double TTAX = 0.0f;                   // transaction tax


    // constructor bool, bool, float, int
    public Trader(boolean HFT, double privateValue) {
        this.isHFT = HFT;
        TraderCount++;
        this.traderID = TraderCount;
        if (HFT){
            TraderCountHFT++;
        } else {
            TraderCountNonHFT++;
        }
        /*if (privateValue > 0){pv = 2;}
        else if (privateValue < 0){pv = 1;}
        else {pv = 0;}*/
        if (privateValue == FprivateValues[4]){pv = 4;}
        else if (privateValue == FprivateValues[3]){pv = 3;}
        else if (privateValue == FprivateValues[2]){pv = 2;}
        else if (privateValue == FprivateValues[1]){pv = 1;}
        else {pv = 0;}
        this.privateValue = (float) privateValue;// - 0.0625f;
    }

    // constructor of the main trader- loads parameters from main
    public Trader(int is, double[] tb, double[] ts, byte numberPrices, int FVpos, double tickS, double rFast, double rSlow,
                  int ll, int hl, int e, int md, int bp, int hti, double pt, String f, LOB_LinkedHashMap b,
                  double [] PVs, float r, double tt, double cf){
        states = new HashMap<Long, HashMap<Integer, BeliefQ>>(hti);
        previousStates = new HashMap<Long, HashMap<Integer, BeliefQ>>();
        convergenceStates = new HashMap<Long, HashMap<Integer, BeliefQ>>();
        statesConstructor = new previousStates();
        statesConstructor.storeStates(states);
        updatedTraders = new ArrayList<Integer>();
        book = b;
        infoSize = is;
        LL = ll;
        HL = hl;
        end = e;
        breakPoint = bp;
        nPayoffs = 2 * end + 3;
        maxDepth = md;
        for (int i = 0; i < end; i++){
            Double[] DiscountsB = new Double[maxDepth + 1];
            Double[] DiscountsS = new Double[maxDepth + 1];
            for (int j = 0; j < maxDepth; j++){
                DiscountsB[j] = Math.exp(-rho * tb[i] * (j + 1));
                DiscountsS[j] = Math.exp(-rho * ts[i] * (j + 1));
            }
            DiscountsB[maxDepth] = 0.0;
            DiscountsS[maxDepth] = 0.0;
            //discountFactorB.put(i, DiscountsB);
            discountFactorB.put(end - i - 1, DiscountsS);
            discountFactorS.put(i, DiscountsS);
        }
        nP = numberPrices;
        ReturnFrequencyHFT = rFast;
        ReturnFrequencyNonHFT = rSlow;
        fvPos = FVpos;
        tickSize = tickS;
        prTremble = pt;
        folder = f;
        decision = new Decision(numberPrices, FVpos, e, bp, LL);
        decision_4 = new Decision(numberPrices, FVpos, e, bp, LL);
        decision_2 = new Decision(numberPrices, FVpos, e, bp, LL);
        decision0 = new Decision(numberPrices, FVpos, e, bp, LL);
        decision2 = new Decision(numberPrices, FVpos, e, bp, LL);
        decision4 = new Decision(numberPrices, FVpos, e, bp, LL);
        decisionHFT = new Decision(numberPrices, FVpos, e, bp, LL);
        diag = new Diagnostics(numberPrices, e);
        bookSizesHistory = new int[2 * nP + 1];
        bookSizesHistory[0] = 0;
        previousTraderAction = new int[3];
        FprivateValues = PVs;
        rho = r;
        TTAX = tt;
        CFEE = cf;
        if (CFEE != 0.0){
            numberOfCancels = new double[nPayoffs];
            for (int i = 0; i < end; i++){
                numberOfCancels[i] = (1 + i) * 0.5;
                numberOfCancels[i + end] = (end - i) * 0.5;
            }
        }
        TraderCount = 0;         // counting number of traders, gives traderID as well
        TraderCountHFT = 0;      // counting HFT traders
        TraderCountNonHFT = 0;   // counting nonHFT traders
        tradeCount = 0;          // counting number of trader
        statesCount = 0;
    }

    // decision about the price is made here, so far random
    public ArrayList<Order> decision(int[] BookSizes, int[] BookInfo,
                                     double et, double priceFV){
        orders = new ArrayList<Order>();
        PriceFV = priceFV;              // get price of the fundamental value = fundamental value
        int pricePosition;              // pricePosition at which to submit
        int forbiddenMarketOrder = -1;  // if 2 * end, SMO forbidden, 2 * end + 1 BMO forbidden, to not execute against himself
        boolean buyOrder = false;       // buy order?
        long Bt = BookInfo[0];          // Best Bid position
        long At = BookInfo[1];          // Best Ask position
        int oldPos = 0, oldAction = -1;
        int q = 0;
        int x = 0;
        if (isReturning && order != null){ // pricePosition position in previous action
            oldPos = order.getPosition() - book.getPositionShift(); //
            q = Math.min(maxDepth, order.getQ());
            if(order.isBuyOrder()){
                x = 2;
                if (oldPos == Bt){
                    forbiddenMarketOrder = 2 * end;
                }
            } else {
                x = 1;
                if (oldPos == At){
                    forbiddenMarketOrder = 2 * end + 1;
                }
            }
        }

        Long code = HashCode(oldPos, q, x, BookInfo, BookSizes);
        tempQs = states.containsKey(code) ? states.get(code)
                                          : new HashMap<Integer, BeliefQ>();
        int action = -1, nLO = 0;
        double max = -1.0, p1 = -1.0, sum = 0.0, sumNC = 0.0, Q = -1.0, nC = -1.0, maxQ = -1.0, maxNC = -1.0;

        short b = (short) Math.max(BookInfo[0] - LL + 1, 0); // + 1 in order to start from one above B
        b = (short) Math.min(end, b);
        short a = (short) Math.min(BookInfo[1] - LL + end, 2 * end);
        a = (short) Math.max(end, a);

        if (isReturning && order != null){
            boolean buy = order.isBuyOrder();
            if (buy){
                action = oldPos - LL + end;
                if (action >= end && action < a){   // still in the range for LO, else is cancelled for sure
                    if (CFEE == 0.0){               // CFEE == 0.0 so normal payoff for old action
                        if (tempQs.containsKey(action)){
                            max = tempQs.get(action).getQ();
                        } else if (similar){
                            max = getSimilarBelief(code, action, BookInfo, q, oldPos);
                        } else {
                            max = (discountFactorS.get(action - end)[Math.abs(q)] *
                                    ((breakPoint - action + end) * tickSize + privateValue - TTAX));
                        }
                    } else {
                        if (tempQs.containsKey(action)){
                            maxQ = tempQs.get(action).getQ();
                            maxNC = tempQs.get(action).getnC();
                            max = maxQ - maxNC * CFEE;
                        } else if (similar){
                            double[] similarOutcome = getSimilarQnC(code, action, BookInfo, q, oldPos);
                            maxQ = similarOutcome[0];
                            maxNC = similarOutcome[1];
                            max = maxQ - maxNC * CFEE;
                        } else {
                            maxQ = (discountFactorS.get(action - end)[Math.abs(q)] *
                                    ((breakPoint - action + end) * tickSize + privateValue - TTAX));
                            maxNC = discountFactorS.get(action - end)[Math.abs(q)] * (numberOfCancels[action]);
                            max = maxQ - maxNC * CFEE;
                        }
                    }
                    oldAction = action;
                } else {
                    action = -1; // otherwise could have action == 2 * end and the SMO would not be computed later
                }
            } else {
                action = oldPos - LL;
                if (action >= 0 && action < end){ // still in the range for LO, else is cancelled for sure
                    if (CFEE == 0.0){               // CFEE == 0.0 so normal payoff for old action
                        if (tempQs.containsKey(action)){
                            max = tempQs.get(action).getQ();
                        } else if (similar){
                            max = getSimilarBelief(code, action, BookInfo, q, oldPos);
                        } else {
                            max = (discountFactorB.get(action)[Math.abs(q)] *
                                    ((action - breakPoint) * tickSize - privateValue - TTAX));
                        }
                    } else {
                        if (tempQs.containsKey(action)){
                            maxQ = tempQs.get(action).getQ();
                            maxNC = tempQs.get(action).getnC();
                            max = maxQ - maxNC * CFEE;
                        } else if (similar){
                            double[] similarOutcome = getSimilarQnC(code, action, BookInfo, q, oldPos);
                            maxQ = similarOutcome[0];
                            maxNC = similarOutcome[1];
                            max = maxQ - maxNC * CFEE;
                        } else {
                            maxQ = (discountFactorB.get(action)[Math.abs(q)] *
                                    ((action - breakPoint) * tickSize - privateValue - TTAX));
                            maxNC = discountFactorB.get(action)[Math.abs(q)] * (numberOfCancels[action]);
                            max = maxQ - maxNC * CFEE;
                        }
                    }
                    oldAction = action;
                } else {
                    action = -1; // otherwise could have action == 2 * end and the SMO would not be computed later
                }
            }
        }
        if (CFEE == 0.0){
            if (prTremble > 0.0 && Math.random() < prTremble){          // trembling
                HashMap<Integer, Double> p = new HashMap<Integer, Double>();
                if (action != -1){
                    p.put(action, max);
                }
                for(int i = b; i < nPayoffs; i++){ // searching for best payoff
                    p1 = -1.0;
                    if (i != oldAction && i != forbiddenMarketOrder){
                        if (tempQs.containsKey(i)){
                            p1 = tempQs.get(i).getQ();
                            p.put(i, p1);
                        } else {
                            if (i < end){ // payoff to sell limit order
                                p1 = (discountFactorB.get(i)[Math.abs(BookSizes[LL + i])] *
                                        ((i - breakPoint) * tickSize - privateValue - TTAX));
                                p.put(i, p1);
                            } else if (i < a) { // payoff to buy limit order
                                p1 = (discountFactorS.get(i - end)[Math.abs(BookSizes[LL + i - end])] *
                                        ((breakPoint - i + end) * tickSize + privateValue - TTAX));
                                p.put(i, p1);
                            } else if (i == (2 * end)){
                                p1 = ((Bt - fvPos) * tickSize - privateValue - TTAX);
                                p.put(i, p1); // payoff to sell market order
                            } else if (i == (2 * end + 1)){
                                p1 = ((fvPos - At) * tickSize + privateValue - TTAX);
                                p.put(i, p1); // payoff to buy market order
                            } else if (i == (2 * end + 2)){
                                double Rt = (isHFT) ? 1.0 / ReturnFrequencyHFT
                                                    : 1.0 / ReturnFrequencyNonHFT; // expected return time
                                p1 = Math.exp(-rho * Rt) * (sum / Math.max(1, nLO));
                                p.put(i, p1); // 2 for averaging over 14
                            }
                        }
                        if (p1 >= 0){
                            nLO++;
                            sum += p1;
                        }
                    }
                }
                List<Integer> actions = new ArrayList <Integer>(p.keySet());
                action = actions.get(random.nextInt(actions.size()));
                max = p.get(action);
            } else {                                // get the best payoff
                for(int i = b; i < nPayoffs; i++){  // searching for best payoff
                    p1 = -1.0f;
                    if (i != oldAction && i != forbiddenMarketOrder){
                        if (tempQs.containsKey(i)){
                            if (i < end){
                                //p1 = tempQs.get(i).getQ();
                                if (BookSizes[i + LL] > -maxDepth){
                                    p1 = tempQs.get(i).getQ();
                                } /*else {
                                    System.out.println("too low priority");
                                }*/
                            } else if (i < a){
                                //p1 = tempQs.get(i).getQ();
                                if (BookSizes[i + LL - end] < maxDepth){
                                    p1 = tempQs.get(i).getQ();
                                } /*else {
                                    System.out.println("too low priority");
                                }*/
                            } else {
                                p1 = tempQs.get(i).getQ();
                            }
                        } else {
                            if (similar){
                                p1 = getSimilarBelief(code, i, BookInfo, q, oldPos);
                            }
                            if (p1 == -1.0){
                                if (i < end){ // payoff to sell limit order
                                    p1 = (discountFactorB.get(i)[Math.abs(BookSizes[LL + i])] *
                                            ((i - breakPoint) * tickSize - privateValue - TTAX));
                                } else if (i < a) { // payoff to buy limit order
                                    p1 = (discountFactorS.get(i - end)[Math.abs(BookSizes[LL + i - end])] *
                                            ((breakPoint - i + end) * tickSize + privateValue - TTAX));
                                } else if (i == (2 * end)){
                                    p1 = ((Bt - fvPos) * tickSize - privateValue - TTAX); // payoff to sell market order
                                } else if (i == (2 * end + 1)){
                                    p1 = ((fvPos - At) * tickSize + privateValue - TTAX); // payoff to buy market order
                                } else if (i == (2 * end + 2)){
                                    double Rt = (isHFT) ? 1.0 / ReturnFrequencyHFT
                                                        : 1.0 / ReturnFrequencyNonHFT; // expected return time
                                    p1 = Math.exp(-rho * Rt) * (sum / Math.max(1, nLO)); // 2 for averaging over 14
                                }
                            }
                        }
                        if (p1 >= 0.0){
                            nLO++;
                            sum += p1;
                            if (p1 >= max){
                                max = p1;
                                action = i;
                            }
                        }
                    }
                }
                if (isReturning && online){ // updating old belief if trader is returning
                    if (fixedBeliefs){
                        if (!updatedTraders.contains(traderID) && (belief.getNe() > 0)){
                            if(belief.getNe() < nResetMax) {
                                belief.increaseNe();
                            }
                            double alpha = (1.0 / (1.0 + (belief.getNe()))); // updating factor
                            double previousDiff = belief.getDiff();
                            belief.setDiff((1.0 - alpha) * previousDiff +
                                    alpha * Math.exp( - rho * (et - EventTime)) * max);
                            updatedTraders.add(traderID);
                        }
                    } else {
                        if(belief.getN() < nResetMax) {
                            belief.increaseN();
                        }
                        double alpha = (1.0 / (1.0 + (belief.getN()))); // updating factor
                        double previousQ = belief.getQ();
                        belief.setQ((1.0 - alpha) * previousQ +
                                alpha * Math.exp( - rho * (et - EventTime)) * max);
                    }
                }
            }
            if (fixedBeliefs){                            // just save the realized beliefs for comparison
                if (convergenceStates.containsKey(code)){
                    tempQs = convergenceStates.get(code);
                    if (tempQs.containsKey(action)){
                        belief = tempQs.get(action);
                    } else {
                        belief = new BeliefQ(1, 1, max, max);
                        tempQs.put(action, belief);
                    }
                } else {
                    tempQs = new HashMap<Integer, BeliefQ>();
                    belief = new BeliefQ(1, 1, max, max);
                    tempQs.put(action, belief);
                    convergenceStates.put(code, tempQs);
                }
            } else {                                      // beliefs saved to states HashMap
                if (tempQs.containsKey(action)){
                    belief = tempQs.get(action); // obtaining the belief-> store as private value
                } else {
                    statesCount++;
                    belief = new BeliefQ((short) 1, max);
                    tempQs.put(action, belief); // obtaining the belief-> store as private value
                }
                if (!states.containsKey(code)){
                    states.put(code, tempQs);
                }
            }
        } else {        // CFEE != 0.0
            boolean cancelled = false;              // is it necessary to cancel the previous order?
            boolean buyO = false;                   // is the would be order a buy order?
            int pPosition = 0;                      // is the would be position of a would be order
            if (prTremble > 0.0 && Math.random() < prTremble){          // trembling
                HashMap<Integer, Double[]> p = new HashMap<Integer, Double[]>();
                Double[] payoffArray = new Double[3];      // TODO: test if I pass values of payoffArray into p
                if (action != -1){
                    payoffArray[0] = max;
                    payoffArray[1] = maxQ;
                    payoffArray[2] = maxNC;
                    p.put(action, payoffArray);
                }
                for(int i = b; i < nPayoffs; i++){ // searching for best payoff
                    p1 = -1.0;
                    if (i != oldAction && i != forbiddenMarketOrder){
                        if (tempQs.containsKey(i)){
                            Q = tempQs.get(i).getQ();
                            nC = tempQs.get(i).getnC();
                            p1 = Q - nC * CFEE;
                            payoffArray = new Double[3];
                            payoffArray[0] = p1;
                            payoffArray[1] = Q;
                            payoffArray[2] = nC;
                            p.put(i, payoffArray);
                        } else {
                            if (i < end){ // payoff to sell limit order
                                Q = (discountFactorB.get(i)[Math.abs(BookSizes[LL + i])] *
                                        ((i - breakPoint) * tickSize - privateValue - TTAX));
                                nC = discountFactorB.get(i)[Math.abs(BookSizes[LL + i])] * numberOfCancels[i];
                                p1 = Q - nC * CFEE;
                                payoffArray = new Double[3];
                                payoffArray[0] = p1;
                                payoffArray[1] = Q;
                                payoffArray[2] = nC;
                                p.put(i, payoffArray);
                            } else if (i < a) { // payoff to buy limit order
                                Q = (discountFactorS.get(i - end)[Math.abs(BookSizes[LL + i - end])] *
                                        ((breakPoint - i + end) * tickSize + privateValue - TTAX));
                                nC = discountFactorS.get(i - end)[Math.abs(BookSizes[LL + i - end])] * numberOfCancels[i];
                                p1 = Q - nC * CFEE;
                                payoffArray = new Double[3];
                                payoffArray[0] = p1;
                                payoffArray[1] = Q;
                                payoffArray[2] = nC;
                                p.put(i, payoffArray);
                            } else if (i == (2 * end)){
                                Q = ((Bt - fvPos) * tickSize - privateValue - TTAX);
                                nC = 0.0;
                                p1 = Q - nC * CFEE;
                                payoffArray = new Double[3];
                                payoffArray[0] = p1;
                                payoffArray[1] = Q;
                                payoffArray[2] = nC;
                                p.put(i, payoffArray);
                            } else if (i == (2 * end + 1)){
                                Q = ((fvPos - At) * tickSize + privateValue - TTAX);
                                nC = 0.0;
                                p1 = Q - nC * CFEE;
                                payoffArray = new Double[3];
                                payoffArray[0] = p1;
                                payoffArray[1] = Q;
                                payoffArray[2] = nC;
                                p.put(i, payoffArray);
                            } else if (i == (2 * end + 2)){
                                double Rt = (isHFT) ? 1.0 / ReturnFrequencyHFT
                                                    : 1.0 / ReturnFrequencyNonHFT; // expected return time
                                Q = Math.exp(-rho * Rt) * (sum / Math.max(1, nLO));
                                nC = Math.exp(-rho * Rt) * (sumNC / Math.max(1, nLO));
                                p1 = Q - nC * CFEE;
                                payoffArray = new Double[3];
                                payoffArray[0] = p1;
                                payoffArray[1] = Q;
                                payoffArray[2] = nC;
                                p.put(i, payoffArray);
                            }
                        }
                        if (isReturning && order != null){
                            if (i >= (2 * end) && i <= (2 * end + 2)){                        // no action or MO
                                cancelled = true;
                            } else{
                                if (i >= end){
                                    buyO = true;
                                    pPosition = i + LL - end;
                                } else {
                                    pPosition = i + LL;
                                }
                                if ((oldPos != pPosition) || (order.isBuyOrder() != buyO)){   // different position or different direction
                                    cancelled = true;
                                }
                            }
                            if (cancelled && oldAction != -1){
                                p1 -= CFEE;
                                nC += 1.0;
                                payoffArray[0] -= CFEE;
                                payoffArray[2] += 1.0;
                            }
                            cancelled = false;
                        }
                        if (p1 >= 0){
                            nLO++;
                            sum += Q;
                            sumNC += nC;
                        }
                    }
                }
                List<Integer> actions = new ArrayList <Integer>(p.keySet());
                action = actions.get(random.nextInt(actions.size()));
                payoffArray = p.get(action);
                max = payoffArray[0];
                maxQ = payoffArray[1];
                maxNC = payoffArray[2];
            } else {                                // get the best payoff
                for(int i = b; i < nPayoffs; i++){  // searching for best payoff
                    p1 = -1.0f;
                    if (i != oldAction && i != forbiddenMarketOrder){
                        if (tempQs.containsKey(i)){
                            if (i < end){
                                //p1 = tempQs.get(i).getQ();
                                if (BookSizes[i + LL] > -maxDepth){
                                    Q = tempQs.get(i).getQ();
                                    nC = tempQs.get(i).getnC();
                                    p1 = Q - nC * CFEE;
                                } /*else {
                                    System.out.println("too low priority");
                                }*/
                            } else if (i < a){
                                //p1 = tempQs.get(i).getQ();
                                if (BookSizes[i + LL - end] < maxDepth){
                                    Q = tempQs.get(i).getQ();
                                    nC = tempQs.get(i).getnC();
                                    p1 = Q - nC * CFEE;
                                } /*else {
                                    System.out.println("too low priority");
                                }*/
                            } else {
                                Q = tempQs.get(i).getQ();
                                nC = tempQs.get(i).getnC();
                                p1 = Q - nC * CFEE;
                            }
                        } else {
                            if (similar){
                                double[] similarOutcome = getSimilarQnC(code, i, BookInfo, q, oldPos);
                                Q = similarOutcome[0];
                                nC = similarOutcome[1];
                                p1 = Q - nC * CFEE;
                            }
                            if (p1 == -1.0){
                                if (i < end){ // payoff to sell limit order
                                    Q = (discountFactorB.get(i)[Math.abs(BookSizes[LL + i])] *
                                            ((i - breakPoint) * tickSize - privateValue - TTAX));
                                    nC = discountFactorB.get(i)[Math.abs(BookSizes[LL + i])] * numberOfCancels[i];
                                    p1 = Q - nC * CFEE;
                                } else if (i < a) { // payoff to buy limit order
                                    Q = (discountFactorS.get(i - end)[Math.abs(BookSizes[LL + i - end])] *
                                            ((breakPoint - i + end) * tickSize + privateValue - TTAX));
                                    nC = discountFactorS.get(i - end)[Math.abs(BookSizes[LL + i - end])] * numberOfCancels[i];
                                    p1 = Q - nC * CFEE;
                                } else if (i == (2 * end)){
                                    Q = ((Bt - fvPos) * tickSize - privateValue - TTAX); // payoff to sell market order
                                    nC = 0.0;
                                    p1 = Q - nC * CFEE;
                                } else if (i == (2 * end + 1)){
                                    Q = ((fvPos - At) * tickSize + privateValue - TTAX); // payoff to buy market order
                                    nC = 0.0;
                                    p1 = Q - nC * CFEE;
                                } else if (i == (2 * end + 2)){
                                    double Rt = (isHFT) ? 1.0 / ReturnFrequencyHFT
                                                        : 1.0 / ReturnFrequencyNonHFT; // expected return time
                                    Q = Math.exp(-rho * Rt) * (sum / Math.max(1, nLO));
                                    nC = Math.exp(-rho * Rt) * (sumNC / Math.max(1, nLO));
                                    p1 = Q - nC * CFEE;
                                }
                            }
                        }
                        if (isReturning && order != null){
                            if (i >= (2 * end) && i <= (2 * end + 2)){                        // no action or MO
                                cancelled = true;
                            } else{
                                if (i >= end){
                                    buyO = true;
                                    pPosition = i + LL - end;
                                } else {
                                    pPosition = i + LL;
                                }
                                if ((oldPos != pPosition) || (order.isBuyOrder() != buyO)){   // different position or different direction
                                    cancelled = true;
                                }
                            }
                            if (cancelled && oldAction != -1){
                                p1 -= CFEE;
                                nC += 1.0;
                            }
                            cancelled = false;
                        }
                        if (p1 >= 0.0){
                            nLO++;
                            sum += Q;
                            sumNC += nC;
                            if (p1 >= max){
                                max = p1;
                                maxQ = Q;
                                maxNC = nC;
                                action = i;
                            }
                        }
                    }
                }
                if (isReturning && online){ // updating old belief if trader is returning
                    if (fixedBeliefs){
                        if (!updatedTraders.contains(traderID) && (belief.getNe() > 0)){
                            if(belief.getNe() < nResetMax) {
                                belief.increaseNe();
                            }
                            double alpha = (1.0 / (1.0 + (belief.getNe()))); // updating factor
                            double previousDiff = belief.getDiff();
                            belief.setDiff((1.0 - alpha) * previousDiff +
                                    alpha * Math.exp( - rho * (et - EventTime)) * max);
                            updatedTraders.add(traderID);
                        }
                    } else {
                        if(belief.getN() < nResetMax) {
                            belief.increaseN();
                        }
                        double alpha = (1.0 / (1.0 + (belief.getN()))); // updating factor
                        double previousQ = belief.getQ();
                        double previousNC = belief.getnC();
                        belief.setQ((1.0 - alpha) * previousQ +
                                alpha * Math.exp( - rho * (et - EventTime)) * maxQ);
                        belief.setnC((1.0 - alpha) * previousNC +
                                alpha * Math.exp(-rho * (et - EventTime)) * maxNC);
                    }
                }
            }
            if (fixedBeliefs){                            // just save the realized beliefs for comparison
                if (convergenceStates.containsKey(code)){
                    tempQs = convergenceStates.get(code);
                    if (tempQs.containsKey(action)){
                        belief = tempQs.get(action);
                    } else {
                        belief = new BeliefQ(1, 1, maxQ, maxQ);
                        tempQs.put(action, belief);
                    }
                } else {
                    tempQs = new HashMap<Integer, BeliefQ>();
                    belief = new BeliefQ(1, 1, maxQ, maxQ);
                    tempQs.put(action, belief);
                    convergenceStates.put(code, tempQs);
                }
            } else {                                      // beliefs saved to states HashMap
                if (tempQs.containsKey(action)){
                    belief = tempQs.get(action); // obtaining the belief-> store as private value
                } else {
                    statesCount++;
                    belief = new BeliefQ((short) 1, maxQ, maxNC);
                    tempQs.put(action, belief); // obtaining the belief-> store as private value
                }
                if (!states.containsKey(code)){
                    states.put(code, tempQs);
                }
            }
        }



        // creating an order
        if (action >= end){
            buyOrder = true;
        }
        pricePosition = (action < end) ? action + LL
                                       : action + LL - end;
        if (action == 2 * end){ // position is Bid
            pricePosition = BookInfo[0];
            buyOrder = false;
        }
        if (action == (2 * end + 1)){pricePosition = BookInfo[1];} // position is Ask
        Order currentOrder = new Order(traderID, et, buyOrder, 1, 0, pricePosition);

        boolean cancelled = false;                                   // used to count cancellations
        if (isReturning){
            if (order != null){
                oldPos = order.getPosition() - book.getPositionShift();
                if (action == (2 * end + 2)){                        // no action
                    order.setCancelled(true);
                    Order orderCancelled = order;
                    orders.add(orderCancelled);
                    order = null;
                    cancelled = true;
                } else if ((oldPos != pricePosition) || (order.isBuyOrder() != buyOrder)){   // different position or different direction
                    order.setCancelled(true);
                    Order orderCancelled = order;
                    orders.add(orderCancelled);
                    orders.add(currentOrder);
                    order = currentOrder;
                    cancelled = true;
                }
            } else if (action != (2 * end + 2)){
                order = currentOrder;
                orders.add(currentOrder);
            }
        } else if (action != (2 * end + 2)){
            order = currentOrder;
            orders.add(currentOrder);
        }
        if (CFEE != 0.0 && (cancelled && oldAction != -1.0)){cancelCount++;}

        if ((action == (2 * end)) || (action == (2 * end + 1))) {
            isTraded = true; // isTraded set to true if submitting MOs
        }
        isReturning = true;
        if (writeDecisions){writeDecision(BookInfo, BookSizes, (short)action, cancelled);} // printing data for output tables
        if (writeHistogram){writeHistogram(BookSizes);}
        if (writeDiagnostics){writeDiagnostics((short)action);}

        EventTime = et;
        return orders;
    }

    // used to update payoff of action taken in a state upon execution
    public void execution(double fundamentalValue, double et){
        int pos = order.getPosition() - book.getPositionShift();
        double payoff;
        tradeCount++;
        if (order.isBuyOrder()){ // buy LO executed
            payoff = (breakPoint - (pos - LL)) * tickSize + privateValue - TTAX;
                    //+ (fundamentalValue - PriceFV);
        } else { // sell LO executed
            payoff = (pos - LL - breakPoint) * tickSize - privateValue - TTAX;
                    //- (fundamentalValue - PriceFV);
        }
        if (fixedBeliefs){
            if (belief.getNe() > 0){
                if(belief.getN() < nResetMax) {
                    belief.increaseN();
                }
                double alpha = (1.0/(1.0 + (belief.getN()))); // updating factor
                double previousQ = belief.getQ();
                belief.setQ((1.0 - alpha) * previousQ +                                 // realized payoff
                        alpha * Math.exp( - rho * (et - EventTime)) * payoff);
                if (!updatedTraders.contains(traderID)){
                    if(belief.getNe() < nResetMax) {
                        belief.increaseNe();
                    }
                    alpha = (1.0/(1.0 + (belief.getNe()))); // updating factor
                    previousQ = belief.getDiff();
                    belief.setDiff((float)((1.0 - alpha) * previousQ +                  // one-step ahead
                            alpha * Math.exp( - rho * (et - EventTime)) * payoff));
                }
            }
        } else {
            if(belief.getN() < nResetMax) {
                belief.increaseN();
            }
            double alpha = (1.0/(1.0 + (belief.getN()))); // updating factor
            double previousQ = belief.getQ();
            belief.setQ((1.0 - alpha) * previousQ +
                    alpha * Math.exp( - rho * (et - EventTime)) * payoff);
            if (CFEE != 0.0){
                double previousNC = belief.getnC();
                belief.setnC((1.0 - alpha) * previousNC +
                        alpha * Math.exp(-rho * (et - EventTime)) * cancelCount);   
         }
            if (writeDiagnostics){writeDiagnostics(belief.getQ() - previousQ);}
        }
        isTraded = true;
        order = null;
    }

    /*public void execution(double fundamentalValue, double et){
        int pos = order.getPosition() - book.getPositionShift();
        double payoff;
        tradeCount++;
        if (order.isBuyOrder()){ // buy LO executed
            payoff = (breakPoint - (pos - LL)) * tickSize + privateValue
                    + (fundamentalValue - PriceFV);
        } else { // sell LO executed
            payoff = (pos - LL - breakPoint) * tickSize - privateValue
                    - (fundamentalValue - PriceFV);
        }
        if (!fixedBeliefs){
            if(belief.getN() < nResetMax) {
                belief.increaseN();
            }
            double alpha = (1.0/(1.0 + (belief.getN()))); // updating factor
            double previousQ = belief.getQ();
            belief.setQ((1.0 - alpha) * previousQ +
                    alpha * Math.exp( - rho * (et - EventTime)) * payoff);
            if (writeDiagnostics){writeDiagnostics(belief.getQ() - previousQ);}
        } else {
            if (belief.getN() == 1){ // completely new belief, falls here until end
                if(belief.getNe() < nResetMax) {
                    belief.increaseNe();
                }
                double alpha = (1.0/(1.0 + (belief.getNe()))); // updating factor
                double previousQ = belief.getQ();
                belief.setQ((1.0 - alpha) * previousQ +
                        alpha * Math.exp( - rho * (et - EventTime)) * payoff);
            } else { // old belief, can be taken into consideration for convergence
                if (belief.getNe() == 0){ // first time updated
                    belief.increaseNe();
                    belief.setDiff(belief.getQ());
                } else if(belief.getNe() < nResetMax) {
                    belief.increaseNe();
                } // continue with normal update
                double alpha = (1.0/(1.0 + (belief.getNe()))); // updating factor
                double previousQ = belief.getDiff();
                belief.setDiff((float)((1.0 - alpha) * previousQ +
                        alpha * Math.exp( - rho * (et - EventTime)) * payoff));
            }
        }
        isTraded = true;
        order = null;
    }*/

    // use to cancel limit order in normal setting
    public void cancel(double et){
        if (fixedBeliefs){
            if (belief.getNe() > 0){
                if (!updatedTraders.contains(traderID)){
                    if(belief.getNe() < nResetMax) {
                        belief.increaseNe();
                    }
                    double alpha = (1.0/(1.0 + (belief.getNe())));          // updating factor
                    double previousQ = belief.getDiff();
                    belief.setDiff((float)((1.0 - alpha) * previousQ));     // payoff is 0.0, so the second part is dropped
                }
            }
        }

        order = null;
    }

    // get belief at similar state AND or OR similar action
    private double getSimilarBelief(long code1b, int ac, int[] bi, int priority, int ownPrice){
        long code2 = code1b;                                 // modified code trying to find similar action-state belief
        //HashMap<Integer, BeliefQ> similarQs;            // HashMap of similar beliefs
        //double similarBelief = -1.0;                    // similar belief, initialized to equal standard -1 in the max choosing for loop
        int i = 13;                                     // number of possible
        //while (similarBelief == -1.0 && i > 0) {
        while ((states.get(code2) == null || states.get(code2).get(ac) == null) && i > 0) { // TODO: test
            if (i == 13 && bi[5] < 2 * maxDepth - 1){       // depth off ask
                code2 = code1b + (1<<19);
            } else if (i == 12 && bi[4] < 2 * maxDepth - 1){ // depth off bid is bigger than 1
                code2 = code1b + (1<<23);
            } else if (i == 11 && bi[5] >= 3){              // depth off ask is bigger than 3
                code2 = code1b - (1<<19);
            } else if (i == 10 && bi[4] >= 3){              // depth off bid is bigger than 3
                code2 = code1b - (1<<23);
            } else if (i == 9 && priority >= 1){            // priority of past own action is higher than 0
                code2 = code1b - (1<<6);
            } else if (i == 8 && bi[6] >= 3){               // past price is higher than 3
                code2 = code1b - (1<<15);
            } else if (i == 7 && ownPrice >= 3){
                code2 = code1b - (1<<10);
            } else if (i == 6 && bi[6] <= 11){              // past price is below 11
                code2 = code1b + (1<<15);
            } else if (i == 5 && ownPrice <= 11){
                code2 = code1b + (1<<10);
            } else if (i == 4 && bi[6] >= 4){               // past price is higher than 4
                code2 = code1b - (2<<15);
            } else if (i == 3 && ownPrice >= 4){
                code2 = code1b - (2<<10);
            } else if (i == 2 && bi[6] <= 10){              // past price is below 10
                code2 = code1b + (2<<15);
            } else if (i == 1 && ownPrice <= 10){
                code2 = code1b + (2<<10);
            }
            /*if (states.containsKey(code2)){
                similarQs = states.get(code2);
                if (similarQs.containsKey(ac)){
                    similarBelief = similarQs.get(ac).getQ();
                }
            }*/
            i--;
        }


        return (states.get(code2) != null && states.get(code2).get(ac) != null) ? states.get(code2).get(ac).getQ()
                                                                                : -1.0;//similarBelief;
    }

    private double[] getSimilarQnC(long code1b, int ac, int[] bi, int priority, int ownPrice){
        long code2 = code1b;                            // modified code trying to find similar action-state belief
        //HashMap<Integer, BeliefQ> similarQs;          // HashMap of similar beliefs
        double[] similarBelief = {-1.0, 0.0};             // similar belief, initialized to equal standard -1 in the max choosing for loop
        int i = 13;                                     // number of possible
        //while (similarBelief == -1.0 && i > 0) {
        while ((states.get(code2) == null || states.get(code2).get(ac) == null) && i > 0) { // TODO: test
            if (i == 13 && bi[5] < 2 * maxDepth - 1){       // depth off ask
                code2 = code1b + (1<<19);
            } else if (i == 12 && bi[4] < 2 * maxDepth - 1){ // depth off bid is bigger than 1
                code2 = code1b + (1<<23);
            } else if (i == 11 && bi[5] >= 3){              // depth off ask is bigger than 3
                code2 = code1b - (1<<19);
            } else if (i == 10 && bi[4] >= 3){              // depth off bid is bigger than 3
                code2 = code1b - (1<<23);
            } else if (i == 9 && priority >= 1){            // priority of past own action is higher than 0
                code2 = code1b - (1<<6);
            } else if (i == 8 && bi[6] >= 3){               // past price is higher than 3
                code2 = code1b - (1<<15);
            } else if (i == 7 && ownPrice >= 3){
                code2 = code1b - (1<<10);
            } else if (i == 6 && bi[6] <= 11){              // past price is below 11
                code2 = code1b + (1<<15);
            } else if (i == 5 && ownPrice <= 11){
                code2 = code1b + (1<<10);
            } else if (i == 4 && bi[6] >= 4){               // past price is higher than 4
                code2 = code1b - (2<<15);
            } else if (i == 3 && ownPrice >= 4){
                code2 = code1b - (2<<10);
            } else if (i == 2 && bi[6] <= 10){              // past price is below 10
                code2 = code1b + (2<<15);
            } else if (i == 1 && ownPrice <= 10){
                code2 = code1b + (2<<10);
            }
            /*if (states.containsKey(code2)){
                similarQs = states.get(code2);
                if (similarQs.containsKey(ac)){
                    similarBelief = similarQs.get(ac).getQ();
                }
            }*/
            i--;
        }

        if (states.get(code2) != null && states.get(code2).get(ac) != null){
            similarBelief[0] = states.get(code2).get(ac).getQ();
            similarBelief[1] = states.get(code2).get(ac).getnC();
        }

        return  similarBelief;
    }

    // writing decisions
    private void writeDecision(int[] BookInfo, int[] BookSizes, Short[] action){
        // tables I, V

        previousTraderAction[0] = decision.addDecision(BookInfo, action, previousTraderAction, isHFT);
        previousTraderAction[1] = BookInfo[0];
        previousTraderAction[2] = BookInfo[1];
    }

    private void writeDecision(int[] BookInfo, int[] BookSizes, Short action, boolean cancelled){
        // tables I, V
        Short[] ac = {action, 127};
        if (isHFT){
            decisionHFT.addDecision(BookInfo, ac, previousTraderAction, !isHFT);
        } else if (pv == 0) {
            decision_4.addDecision(BookInfo, ac, previousTraderAction, isHFT);
        } else if (pv == 1) {
            decision_2.addDecision(BookInfo, ac, previousTraderAction, isHFT);
        } else if (pv == 2) {
            decision0.addDecision(BookInfo, ac, previousTraderAction, isHFT);
        } else if (pv == 3) {
            decision2.addDecision(BookInfo, ac, previousTraderAction, isHFT);
        } else if (pv == 4) {
            decision4.addDecision(BookInfo, ac, previousTraderAction, isHFT);
        }
        previousTraderAction[0] = decision.addDecision(BookInfo, ac, previousTraderAction, isHFT);
        previousTraderAction[1] = BookInfo[0];
        previousTraderAction[2] = BookInfo[1];
        decision.addDecisionLiquidity(action, isHFT, cancelled);
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
    private void writeDiagnostics(Short[] ac, double max){
        diag.addAction(ac, (byte)1, max);
    }
    private void writeDiagnostics(short ac){
        diag.addAction(ac);
    }
    private void writeDiagnostics(double diff){
        diag.addDiff(diff);
    }

    public void computeInitialBeliefs(){
        int[] occurrences = new int[nPayoffs];
        Iterator<Integer> iteratorActions;
        HashMap<Integer, BeliefQ> beliefQs;
        Iterator<Long> iteratorStates = states.keySet().iterator();
        Long code;
        Integer action;
        while (iteratorStates.hasNext()){
            code = iteratorStates.next();
            beliefQs = states.get(code);
            iteratorActions = beliefQs.keySet().iterator();
            while (iteratorActions.hasNext()){
                action = iteratorActions.next();
                numberOfCancels[action] += beliefQs.get(action).getnC();
                occurrences[action]++;
            }
        }
        for (int i = 0; i < nPayoffs; i++){
            numberOfCancels[i] = (double)(numberOfCancels[i] / Math.max(occurrences[i], 1));
        }
    }

    // Hash code computed dependent on various Information Size (2, 4, 6, 7, 8)
    public Long HashCode(int P, int q, int x, int[] BookInfo, int [] BS){

        long code = (long) 0;
        if (infoSize == 2){
            long Bt = BookInfo[0]; // Best Bid position
            long At = BookInfo[1]; // Best Ask position
            int a = pv; // private value zero(0), negative (1), positive (2)
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
}*/ // tests

        } else if (infoSize == 5) { // GPR 2005 state space
            long tempCode;
            int buy;
            for (int i = 1; i < nP - 1; i++){
                buy = (BS[i] > 0) ? 1 : 0;
                tempCode = (code<<5) + (buy<<4) + Math.abs(BS[i]);
                code = tempCode;
            }
            /*long Bt = BookInfo[0]; // Best Bid position
long At = BookInfo[1]; // Best Ask position
long lBt = BookInfo[2]; // depth at best Bid
long lAt = BookInfo[3]; // depth at best Ask
long dBt = (BookInfo[4]); // depth at buy
int dSt = (BookInfo[5]); // depth at sell
int l = (isHFT) ? 1 : 0; // arrival frequency slow (0), fast (1)
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
            long Bt = BookInfo[0]; // Best Bid position
            long At = BookInfo[1]; // Best Ask position
            long lBt = BookInfo[2] / 3; // depth at best Bid
            long lAt = BookInfo[3] / 3; // depth at best Ask
            int a = pv; // private value zero(0), negative (1), positive (2)
            code = (Bt<<21) + (At<<16) + (lBt<<14) + (lAt<<12) + (P<<7) + (q<<4) + (x<<2) + a;

        } else if (infoSize == 7){
            long Bt = BookInfo[0]; // Best Bid position
            long At = BookInfo[1]; // Best Ask position
            long lBt = BookInfo[2] / 2; // depth at best Bid
            long lAt = BookInfo[3] / 2; // depth at best Ask
            long dBt = (BookInfo[4] / maxDepth); // depth at buy
            int dSt = (BookInfo[5] / maxDepth); // depth at sell
            int Pt = BookInfo[6]; // last transaction pricePosition position
            int b = BookInfo[7]; // 1 if last transaction buy, 0 if sell
            int a = pv; // private value zero(0), negative (1), positive (2)
            int h = (isHFT) ? 1 : 0; // arrival frequency slow (0), fast (1)

            /*Long code = (Bt<<50) + (At<<44) + (lBt<<40) + (lAt<<36) + (dBt<<29) + (dSt<<22) + (Pt<<16) + (b<<15) +
+ (P<<9) + (q<<5) + (x<<3) + (a<<1) + l;*/
            code = (Bt<<34) + (At<<29) + (lBt<<27) + (lAt<<25) + (dBt<<22) + (dSt<<19) + (Pt<<14) + (b<<13) +
                    + (P<<8) + (q<<5) + (x<<3) + (a<<1) + h;

        }
        else if (infoSize == 8){
            long Bt = BookInfo[0];      // Best Bid position
            long At = BookInfo[1];      // Best Ask position
            long lBt = BookInfo[2] / 2;     // depth at best Bid
            long lAt = BookInfo[3] / 2;     // depth at best Ask
            long dBt = Math.min(15, BookInfo[4] / 3); // depth off Bid
            long dSt = Math.min(15, BookInfo[5] / 3); // depth off Ask
            int Pt = BookInfo[6];       // last transaction pricePosition position
            int b = BookInfo[7];        // 1 if last transaction buy, 0 if sell
            q = Math.min(15, q);
            int a = pv;                 // private value zero(0), negative (1), positive (2)
            int l = (isHFT) ? 1 : 0;    // arrival frequency slow (0), fast (1)
            //System.out.println(Bt + " : " + lBt + " ; " + At + " : " + lAt);
            /*Long code = (Bt<<50) + (At<<44) + (lBt<<40) + (lAt<<36) + (dBt<<29) + (dSt<<22) + (Pt<<16) + (b<<15) +
+ (P<<9) + (q<<5) + (x<<3) + (a<<1) + l;*/
            code = (Bt<<39) + (At<<35) + (lBt<<31) + (lAt<<27) + (dBt<<23) + (dSt<<19) + (Pt<<15) + (b<<14) +
                    + (P<<10) + (q<<6) + (x<<4) + (a<<1) + l;

            /*long code2 = code;
            boolean [] test = new boolean[14];
            test[0] = ((code2 >> 39) == Bt);
            code2 = code2 - (Bt<<39);

            test[1] = ((code2 >> 35) == At);
            code2 = code2 - (At<<35);

            test[2] = ((code2 >> 31) == lBt);
            code2 = code2 - (lBt<<31);

            test[3] = ((code2 >> 27) == lAt);
            code2 = code2 - (lAt<<27);

            test[4] =((code2 >> 23) == dBt);
            code2 = code2 - (dBt<<23);

            test[5] = ((code2 >> 19) == dSt);
            code2 = code2 - (dSt<<19);

            test[6] = ((code2 >> 15) == Pt);
            code2 = code2 - (Pt<<15);

            test[7] = ((code2 >> 14) == b);
            code2 = code2 - (b<<14);

            test[8] = ((code2 >> 10) == P);
            code2 = code2 - (P<<10);

            test[9] = ((code2 >> 6) == q);
            code2 = code2 - (q<<6);

            test[10] = ((code2 >> 4) == x);
            code2 = code2 - (x<<4);

            test[11] = ((code2 >> 1) == a);
            code2 = code2 - (a<<1);

            test[12] = (code2 == l);
            code2 = code2 - l;

            test[13] = (code2 == 0);
            for (int i = 0; i < 14; i++){
                if (test[i] == false){
                    System.out.println("testing hash code failed");
                }
            } */        // tests
        }

        return code;
    }

    // used to print the data analysed for convergence- occurrences os states, payoffs, time since last hit
    public void printStatesDensity(double et){
        try{
            String outputFileName = folder + "occurrences.csv";
            String outputFileName2 = folder + "payoffs.csv";
            FileWriter writer = new FileWriter(outputFileName, true);
            FileWriter writer2 = new FileWriter(outputFileName2, true);
            Collection<HashMap<Integer, BeliefQ>> colHM = states.values();
            Collection<BeliefQ> colBelief;
            for (HashMap<Integer, BeliefQ> hm : colHM){
                colBelief = hm.values();
                for (BeliefQ beliefQ : colBelief){
                    writer.write( beliefQ.getN() + ";" + beliefQ.getNe() + ";" + beliefQ.getQ() + ";" + "\r");
                }
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
                writer3.write((double)bookSizesHistory[i]/(double)Math.max(1, bookSizesHistory[0]) + ";");
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
        Iterator<Long> iterator = states.keySet().iterator();
        Iterator<Integer> iterator2;
        ArrayList<Long> codes2remove = new ArrayList<Long>();
        ArrayList<Integer> actions2remove = new ArrayList<Integer>();
        rn = Math.random();
        if (rn < 0.25){ // portion of cancelled payoffs
        toDelete.add(code);
        deleted++;
        }
        Collection<BeliefQ> beliefQs;
        Collection<HashMap<Integer, BeliefQ>> collHM = states.values();
        for (HashMap<Integer, BeliefQ> hm : collHM){
            beliefQs = hm.values();
            for (BeliefQ b : beliefQs){
                NN = Math.max(n, b.getN());
                b.setN((Math.min(NN, m)));
            }
        }*/
    }

    // resets n in Payoffs, sets purge indicator to true-> true until next time the state is hit
    public void nReset(byte n, short m, boolean purge){
        Collection<BeliefQ> beliefQs;
        Iterator<Long> iterator = states.keySet().iterator();
        ArrayList<Long> codes2remove = new ArrayList<Long>();
        Long code;
        int NN;
        while (iterator.hasNext()){
            code = iterator.next();
            beliefQs = states.get(code).values();
            for (BeliefQ b : beliefQs){
                if (purge){
                    if (beliefQs.size() == 1 && b.getN() <= n){
                        codes2remove.add(code);
                    }
                }
                NN = Math.max(n, b.getN());
                NN = Math.min(NN, m);
                b.setN(NN);
            }
        }
        for (Long code2delete: codes2remove){
            NN = states.get(code2delete).size();
            statesCount -= NN;
            states.remove(code2delete);
        }
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
            writer.write(diag.printDiagnostics("actions"));
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public double printConvergence(int t2, String convergenceType, boolean write){
        if (convergenceType != "convergenceSecond.csv"){
            return 0.0;                                     // right now, I don't need first convergence
        }
        long code;
        Iterator keys2;
        HashMap<Integer, BeliefQ> currentBeliefs;
        HashMap<Integer, BeliefQ> previousBeliefs;
        int acKey;
        int kqDiff = 0;             // difference in occurences of realized payoff over SingleRun
        int kdDiff = 0;             // difference in occurences of one-step-ahead payoff over SingleRun
        int nSum = 0;
        int nSum2 = 0;
        double qDiff = 0.0;         // difference in realized beliefs
        double dDiff = 0.0;         // difference in one-step-ahead beliefs
        double sumDiff = 0.0;       // average difference in beliefs
        double sumDiff2 = 0.0;      // average difference in beliefs
        double absError = 0.0;      // this goes out to see if converged
        BeliefQ currentBelief;
        BeliefQ previousBelief;
        Iterator keys;
        if (convergenceType == "convergenceSecond.csv"){
            keys = convergenceStates.keySet().iterator();
            previousStates = states;
        } else {
            keys = states.keySet().iterator();
            /*try {
                FileInputStream fileIn = new FileInputStream(folder + "previousStates.ser");
                ObjectInputStream in = new ObjectInputStream(fileIn);
                statesConstructor = (previousStates) in.readObject();
                in.close();
                fileIn.close();
            } catch(IOException i) {
                i.printStackTrace();
            } catch(ClassNotFoundException c) {
                System.out.println("states constructor not found");
                c.printStackTrace();
            }*/
            previousStates = statesConstructor.getTempStates();
        }
        try{
            String outputFileName = folder + convergenceType;
            FileWriter writer = new FileWriter(outputFileName, true);
            while (keys.hasNext()){
                code = (Long) keys.next();
                if (previousStates.containsKey(code)){
                    if (convergenceType == "convergenceSecond.csv"){
                        currentBeliefs = convergenceStates.get(code);
                    } else {
                        currentBeliefs = states.get(code);
                    }
                    previousBeliefs = previousStates.get(code);
                    keys2 = currentBeliefs.keySet().iterator();
                    while (keys2.hasNext()){
                        acKey = (Integer) keys2.next();
                        if (previousBeliefs.containsKey(acKey)){
                            currentBelief = currentBeliefs.get(acKey);
                            previousBelief = previousBeliefs.get(acKey);
                            if (convergenceType == "convergenceSecond.csv"){
                                if (currentBelief.getNe() > t2 && currentBelief.getN() > t2){
                                    qDiff = Math.abs(currentBelief.getQ() - previousBelief.getQ());
                                    dDiff = Math.abs(currentBelief.getDiff() - previousBelief.getQ());
                                    kqDiff = currentBelief.getN();     // no minus, because it starts from 0
                                    kdDiff = currentBelief.getNe();     // no minus, because it starts from 0
                                    if (write){
                                        writer.write(qDiff + ";" + kqDiff + ";" + dDiff + ";" + kdDiff + ";"
                                                + currentBelief.getQ() + ";" + currentBelief.getDiff() + ";" +  previousBelief.getQ() + ";");
                                        writer.write("\r");
                                    }
                                    if (kqDiff > 0){
                                        sumDiff += (qDiff / kqDiff);
                                        nSum++;
                                    }
                                    if (kdDiff > 0){
                                        sumDiff2 += (dDiff / kdDiff);
                                        nSum2++;
                                    }
                                }
                            } else if (convergenceType == "convergence.csv" && (currentBelief.getN() > t2)){
                                qDiff = Math.abs(currentBelief.getQ() - previousBelief.getQ());
                                kqDiff = currentBelief.getN() - previousBelief.getN();
                                //writer.write(qDiff + ";" + kDiff + ";");
                                //writer.write("\r");
                                if (kqDiff > 0){
                                    sumDiff += (qDiff / kqDiff);  // weighted difference
                                    nSum++;                      // number of beliefs in sumDiff
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("convergence? " + (double) sumDiff / Math.max(1, nSum)
                    + " one-step-ahead: " + (double) sumDiff2 / Math.max(1, nSum2)); // average weighted difference
            writer.write("\r");
            writer.close();
        }
        catch (Exception ex){
            ex.printStackTrace();
            System.exit(1);
        }
        if (convergenceType == "convergenceSecond.csv"){
            absError = (double) sumDiff2 / Math.max(1, nSum2);
        } else {
            absError = (double) sumDiff / Math.max(1, nSum);
        }
        convergenceStates = new HashMap<Long, HashMap<Integer, BeliefQ>>();
        statesConstructor.storeStates(states);

        /*try {
            FileOutputStream fileOut = new FileOutputStream(folder + "previousStates.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(statesConstructor);
            out.close();
            fileOut.close();
            statesConstructor = null;
            previousStates = null;
        } catch (Exception ex){
            ex.printStackTrace();
        }*/

        return absError;
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

        try{
            String outputFileName = folder + "decisionsHFT.csv";
            FileWriter writer = new FileWriter(outputFileName, true);
            writer.write(decisionHFT.printDecision());
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        try{
            String outputFileName = folder + "decisions_4.csv";
            FileWriter writer = new FileWriter(outputFileName, true);
            writer.write(decision_4.printDecision());
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        try{
            String outputFileName = folder + "decisions_2.csv";
            FileWriter writer = new FileWriter(outputFileName, true);
            writer.write(decision_2.printDecision());
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        try{
            String outputFileName = folder + "decisions0.csv";
            FileWriter writer = new FileWriter(outputFileName, true);
            writer.write(decision0.printDecision());
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        try{
            String outputFileName = folder + "decisions2.csv";
            FileWriter writer = new FileWriter(outputFileName, true);
            writer.write(decision2.printDecision());
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        try{
            String outputFileName = folder + "decisions4.csv";
            FileWriter writer = new FileWriter(outputFileName, true);
            writer.write(decision4.printDecision());
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        try{
            String outputFileName2 = folder + "decisionsLiquidity.csv";
            FileWriter writer2 = new FileWriter(outputFileName2, true);
            writer2.write(decision.printDecisionLiquidity());
            writer2.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    // resets com.jakubrojcek.Decision history, memory management
    public void resetDecisionHistory(){
        decision = new Decision(nP, fvPos, end, breakPoint, LL);
        decision_4 = new Decision(nP, fvPos, end, breakPoint, LL);
        decision_2 = new Decision(nP, fvPos, end, breakPoint, LL);
        decision0 = new Decision(nP, fvPos, end, breakPoint, LL);
        decision2 = new Decision(nP, fvPos, end, breakPoint, LL);
        decision4 = new Decision(nP, fvPos, end, breakPoint, LL);
        decisionHFT = new Decision(nP, fvPos, end, breakPoint, LL);
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

    public Order getOrder() {
        return order;
    }

    public int getTraderCount() {
        return TraderCount;
    }

    public int getTraderCountNonHFT() {
        return TraderCountNonHFT;
    }

    public int getTraderCountHFT() {
        return TraderCountHFT;
    }

    public static double getTTAX() {
        return TTAX;
    }

    public static double getCFEE() {
        return CFEE;
    }

    public short getCancelCount() {
        return cancelCount;
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

    public void setOnline(boolean o){
        online = o;
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

    public static void setTraderCountHFT(int traderCountHFT) {
        TraderCountHFT = traderCountHFT;
    }

    public static void setTraderCountNonHFT(int traderCountNonHFT) {
        TraderCountNonHFT = traderCountNonHFT;
    }
}
