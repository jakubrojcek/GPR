package com.jakubrojcek.hftRegulation;

import com.jakubrojcek.Order;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Jakub
 * Date: 15.10.12
 * Time: 11:45
 * To change this template use File | Settings | File Templates.
 * Class SingleRun is operating (1) new trader (2) & (3) decision and transaction rule (4) cancellations
 * (5) innovation of fundamental value for the "normal", "GPR2005" and "GPR2005C" model based on current
 * specification of run parameters for specific number of iterations (events).
 * I, main class loads parameters at initialization of SingleRun
 * II, run specific parameters are load together with number of events
 * III, iterations are computed
 * IV, EventTime and FundamentalValue are returned
 */
public class SingleRun {

    int model;                          // model of simulation, e.g. "returning" = 0, "speedBump" = 1

    HashMap<Integer, Integer> tifCounts = new HashMap<Integer, Integer>(6);     // 5- HFT, rest 0, 1, etc is according to trader.getPv
    HashMap<Integer, Double> tifTimes = new HashMap<Integer, Double>(6);
    HashMap<Integer, Integer> population = new HashMap<Integer, Integer>(6);


    double lambdaArrival;
    double lambdaFVchange;
    double ReturnFrequencyHFT;
    double ReturnFrequencyNonHFT;
    double tif;                                 // time in force
    double speedBump;                           // speed bump length
    double infoDelay;                           // information delay of uninformed traders
    double transparencyPeriod;                  // period for transparency
    double lastUpdateTime;                      // last time the BookInfo and BookSize were updated
    double sigma;
    float tickSize;
    double[] FprivateValues;
    double[] DistributionPV;
    double FVplus;
    double FvLag;                               // lagged fundamental value, knowledge of uninformed traders
    int ReturningHFT = 0;
    int ReturningNonHFT = 0;
    double convergenceStat = 0.0;
    ArrayList<Integer> traderIDsHFT;            // holder for IDs of HFT traders
    ArrayList<Integer> traderIDsNonHFT;         // holder for IDs of nonHFT traders
    TreeMap<Double, Integer> waitingTraders;    // waiting traders, key is time, value is trader's ID
    TreeMap<Double, Double> fundamentalValue;   // lagged fundamental value, key is the change in FV time
    HashMap<Integer, Trader> traders;
    History h;
    Trader trader;
    LOB_LinkedHashMap book;
    Order heldOrder;                            // held market order in speedBump case
    int[] bi;                                   // book info holder
    int[] BookSizes;                            // signed sizes of the book
    int[] BookInfo;                             // info used in decision making
    int end;                                    // number of actions

    boolean header = false;
    String outputNameTransactions; // output file name
    String outputNameBookData; // output file name
    String outputNameStatsData; // output file name

    boolean write = false; // writeDecisions output in this SingleRun?
    boolean writeDiagnostics = false; // write diagnostics
    boolean writeHistogram = false; // write histogram
    boolean purge = false; // purge SingleRun?
    boolean nReset = false; // reset SingleRun?

    double Lambda;


    public SingleRun(int m, double t,double lambdaArrival, double lambdaFV, double ReturnFrequencyHFT, double ReturnFrequencyNonHFT,
                     double [] FprivateValues, double[] PVdist, double sigma, float tickSize, double FVplus, boolean head,
                     LOB_LinkedHashMap b, HashMap<Integer, Trader> ts, History his, Trader TR, String stats,
                     String trans, String bookd, double sb, int e, double d, double pt){
        for (int i = 0; i < 6; i++){
            tifCounts.put(i, 0);
            tifTimes.put(i,0.0);
            population.put(i, 0);
        }
        this.model = m;
        this.tif = t;
        this.speedBump = sb;
        this.lambdaArrival = lambdaArrival;
        this.lambdaFVchange = lambdaFV;
        this.ReturnFrequencyHFT = ReturnFrequencyHFT;
        this.ReturnFrequencyNonHFT = ReturnFrequencyNonHFT;
        this.FprivateValues = FprivateValues;
        this.DistributionPV = PVdist;
        this.sigma = sigma;
        this.tickSize = tickSize;
        this.FVplus = FVplus;
        this.end = e;
        this.infoDelay = d;
        this.transparencyPeriod = pt;
        traders = ts;
        h = his;
        trader = TR;
        book = b;
        outputNameStatsData = stats;
        outputNameTransactions = trans;
        outputNameBookData = bookd;
        header = head;
        traderIDsHFT = new ArrayList<Integer>();
        traderIDsNonHFT = new ArrayList<Integer>();
        waitingTraders = new TreeMap<Double, Integer>();
        fundamentalValue = new TreeMap<Double, Double>();
    }

    public double[] run(int nEvents, double nHFT, double NewNonHFT, int rHFT,
                        int rNonHFT, double EventTime, double FV, boolean w,
                        boolean p, boolean n, boolean wd, boolean wh, String convergence){
        ReturningHFT = rHFT;
        ReturningNonHFT = rNonHFT;
        write = w;
        writeDiagnostics = wd;
        writeHistogram = wh;
        purge = p;
        nReset = n;
        FvLag = FV;

        if (model == 0){        // "returning" model
            if (EventTime < 0.0){
                System.out.println("negative event time, debug");
            }
            if (nReset){
                trader.nReset((byte) 2, (short) 50, purge);
            }
            double prob1, prob2, prob3, prob4, prob5;
            double x1, x2, x3, x4, x5;
            for (int i = 0; i < nEvents; i ++){
                // LAMBDA -> overall event frequency
                Lambda = (nHFT + NewNonHFT) * lambdaArrival + ReturningHFT * ReturnFrequencyHFT +
                        + ReturningNonHFT * ReturnFrequencyNonHFT + lambdaFVchange;  // TODO: kill (nHFT + NewNonHFT)
                EventTime += - Math.log(1.0 - Math.random()) / Lambda; // random exponential time

                // updating the lagged fundamental value
                while (!fundamentalValue.isEmpty() && EventTime > (fundamentalValue.firstKey() + infoDelay)){
                    FvLag = fundamentalValue.remove(fundamentalValue.firstKey());
                }
                // number of all agents to trade
                if (!waitingTraders.isEmpty() && (EventTime > waitingTraders.firstKey())){
                    Integer ID;
                    //while (!waitingTraders.isEmpty() && EventTime > waitingTraders.firstKey()){
                    EventTime = waitingTraders.firstKey();
                    ID = waitingTraders.remove(waitingTraders.firstKey());
                    if (traders.containsKey(ID)){
                        boolean isHFT = traders.get(ID).getIsHFT();
                        ArrayList<Order> orders = traders.get(ID).decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV, FvLag, 0.0);
                        if (!orders.isEmpty()){
                            Integer IDr = book.transactionRule(ID , orders);
                            if (ID.equals(IDr)) { // executed against fringe, remove ID trader
                                traders.remove(ID);
                                if (isHFT){
                                    ReturningHFT--;
                                    traderIDsHFT.remove(ID);
                                } else {
                                    ReturningNonHFT--;
                                    traderIDsNonHFT.remove(ID);
                                }
                            } else if (IDr != null){ // returning trader has executed, remove him and the counterparty as well
                                if (isHFT){
                                    ReturningHFT--;
                                    traderIDsHFT.remove(ID);
                                } else {
                                    ReturningNonHFT--;
                                    traderIDsNonHFT.remove(ID);
                                }
                                if (traders.get(IDr).getIsHFT()){
                                    ReturningHFT--;
                                    traderIDsHFT.remove(IDr);
                                } else {
                                    ReturningNonHFT--;
                                    traderIDsNonHFT.remove(IDr);
                                }
                                traders.remove(ID);
                                traders.remove(IDr);
                            }
                        }
                        //break;
                    }
                    //}
                } else {
                    prob1 = (double) (nHFT) * lambdaArrival / Lambda;
                    prob2 = (double) (NewNonHFT) * lambdaArrival / Lambda;
                    prob3 = (double) ReturningHFT * ReturnFrequencyHFT / Lambda;
                    prob4 = (double) ReturningNonHFT * ReturnFrequencyNonHFT / Lambda;
                    prob5 = lambdaFVchange / Lambda;
                    // for now prob of change in FV is equal to 0
                    x1 = prob1; // 1. new HFT
                    x2 = x1 + prob2; // 2. new nonHFT
                    x3 = x2 + prob3; // 3. reentry HFT
                    x4 = x3 + prob4; // 4. reentry nonHFT
                    x5 = x4 + prob5; // 5. Change in fundamental value
                    if (Math.abs(x5 - 1.0) > 0.00001){
                        System.out.println("Probabilities do not sum to 1.");
                    }

                    double rn = Math.random(); // to determine event
                    Trader tr;
                    Integer ID;
                    double FVrealization;
                    if (rn < x1){ // New arrival HFT
                        FVrealization = 0.0;
                        /*if (rn2 < DistributionPV[0]){
                            FVrealization = FprivateValues[0];
                        } else if (rn2 < DistributionPV[1]){
                            FVrealization = FprivateValues[1];
                        } else {
                            FVrealization = FprivateValues[2];
                        }*/
                        /*double rn2 = Math.random();
                        if (rn2 < DistributionPV[0]){FVrealization = FprivateValues[0];}
                        else if (rn2 < DistributionPV[1]){FVrealization = FprivateValues[1];}
                        else if (rn2 < DistributionPV[2]){FVrealization = FprivateValues[2];}
                        else if (rn2 < DistributionPV[3]){FVrealization = FprivateValues[3];}
                        else {FVrealization = FprivateValues[4];}*/
                        tr = new Trader(true, true, FVrealization);     // TODO: make a separate variable to fork here later
                        ID = tr.getTraderID();
                        traders.put(ID, tr);
                        ArrayList<Order> orders = tr.decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV, FvLag, 0.0);
                        if (!orders.isEmpty()){
                            Integer IDr = book.transactionRule(ID , orders);
                            if (IDr == null){ // order put to the book
                                traderIDsHFT.add(ID);
                                ReturningHFT++;
                            } else if (!ID.equals(IDr)) { // returns another trader-> executed counterparty
                                if (traders.get(IDr).getIsHFT()){
                                    ReturningHFT--;
                                    traderIDsHFT.remove(IDr);
                                } else {
                                    ReturningNonHFT--;
                                    traderIDsNonHFT.remove(IDr);
                                }
                                traders.remove(IDr);
                                traders.remove(ID);
                            } else { // MO against fringe
                                traders.remove(ID);
                            }
                        } else {
                            traderIDsHFT.add(ID);
                            ReturningHFT++;
                        }
                    } else if (rn < x2){ // New arrival nonHFT
                        double rn2 = Math.random();
                        /*if (rn2 < DistributionPV[0]){
                            FVrealization = FprivateValues[0];
                        } else if (rn2 < DistributionPV[1]){
                            FVrealization = FprivateValues[1];
                        } else {
                            FVrealization = FprivateValues[2];
                        }*/
                        if (rn2 < DistributionPV[0]){FVrealization = FprivateValues[0];}
                        else if (rn2 < DistributionPV[1]){FVrealization = FprivateValues[1];}
                        else if (rn2 < DistributionPV[2]){FVrealization = FprivateValues[2];}
                        else if (rn2 < DistributionPV[3]){FVrealization = FprivateValues[3];}
                        else {FVrealization = FprivateValues[4];}

                        tr = new Trader(false, false, FVrealization);
                        ID = tr.getTraderID();
                        traders.put(ID, tr);
                        ArrayList<Order> orders = tr.decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV, FvLag, 0.0);
                        if (!orders.isEmpty()){
                            Integer IDr = book.transactionRule(ID , orders);
                            if (IDr == null){ // order put to the book
                                traderIDsNonHFT.add(ID);
                                ReturningNonHFT++;
                            } else if (!ID.equals(IDr)) { // returns another trader-> executed counterparty
                                if (traders.get(IDr).getIsHFT()){
                                    ReturningHFT--;
                                    traderIDsHFT.remove(IDr);
                                } else {
                                    traderIDsNonHFT.remove(IDr);
                                    ReturningNonHFT--;
                                }
                                traders.remove(IDr);
                                traders.remove(ID);
                            } else { // MO against fringe
                                traders.remove(ID);
                            }
                        } else {
                            traderIDsNonHFT.add(ID);
                            ReturningNonHFT++;
                        }
                    } else if (rn < x3){ // Returning HFT
                        ID = traderIDsHFT.get((int) (Math.random() * traderIDsHFT.size()));
                        if (traders.get(ID).getOrder() != null &&
                                ((EventTime - traders.get(ID).getOrder().getTimeStamp()) < tif)){
                            waitingTraders.put(traders.get(ID).getOrder().getTimeStamp() + tif, ID);
                        } else {
                            ArrayList<Order> orders = traders.get(ID).decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV, FvLag, 0.0);
                            if (!orders.isEmpty()){
                                Integer IDr = book.transactionRule(ID , orders);
                                if (ID.equals(IDr)) { // executed against fringe, remove ID trader
                                    traders.remove(ID);
                                    ReturningHFT--;
                                    traderIDsHFT.remove(ID);
                                } else if (IDr != null){ // returning trader has executed, remove him and the counterparty as well
                                    ReturningHFT--;
                                    traderIDsHFT.remove(ID);
                                    if (traders.get(IDr).getIsHFT()){
                                        ReturningHFT--;
                                        traderIDsHFT.remove(IDr);
                                    } else {
                                        ReturningNonHFT--;
                                        traderIDsNonHFT.remove(IDr);
                                    }
                                    traders.remove(ID);
                                    traders.remove(IDr);
                                }
                            }
                        }
                    } else if (rn < x4){ // Returning nonHFT
                        ID = traderIDsNonHFT.get((int) (Math.random() * traderIDsNonHFT.size()));
                        if (traders.get(ID).getOrder() != null &&
                                ((EventTime - traders.get(ID).getOrder().getTimeStamp()) < tif)){
                            waitingTraders.put(traders.get(ID).getOrder().getTimeStamp() + tif, ID);
                        } else {
                            ArrayList<Order> orders = traders.get(ID).decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV, FvLag, 0.0);
                            if (!orders.isEmpty()){
                                Integer IDr = book.transactionRule(ID , orders);
                                if (ID.equals(IDr)){ // executed against fringe, remove ID trader
                                    traders.remove(ID);
                                    ReturningNonHFT--;
                                    traderIDsNonHFT.remove(ID);
                                } else if (IDr != null){ // returning trader has executed, remove him and the counterparty as well
                                    ReturningNonHFT--;
                                    traderIDsNonHFT.remove(ID);
                                    if (traders.get(IDr).getIsHFT()){
                                        ReturningHFT--;
                                        traderIDsHFT.remove(IDr);
                                    } else {
                                        ReturningNonHFT--;
                                        traderIDsNonHFT.remove(IDr);
                                    }
                                    traders.remove(ID);
                                    traders.remove(IDr);
                                }
                            }
                        }
                    } else { // Change in FV
                        double rn3 = Math.random();
                        ArrayList<Integer> tradersExecuted = new ArrayList<Integer>();
                        if (rn3 < FVplus){
                            FV = FV + sigma * tickSize;
                            tradersExecuted = book.FVup(FV, EventTime, (int) sigma);
                        } else {
                            FV = FV - sigma * tickSize;
                            tradersExecuted = book.FVdown(FV, EventTime, (int) sigma);
                        }
                        for (Integer trID : tradersExecuted){
                            if (traders.get(trID).getIsHFT()){
                                ReturningHFT--;
                                traderIDsHFT.remove(trID);
                            } else {
                                ReturningNonHFT--;
                                traderIDsNonHFT.remove(trID);
                            }
                            traders.remove(trID);
                        }
                        fundamentalValue.put(EventTime, FV);
                    }
                }

                if (ReturningHFT != traderIDsHFT.size() || ReturningNonHFT != traderIDsNonHFT.size()){
                    System.out.println("error, number of traders not equal");
                }
                if (i % 10000000 == 0) {
                    System.out.println(i + " events");
                }
                if (i % 10000 == 0){
                    writeWrite();
                }
                if (i % 1000000 == 0) {
                    writePrint(i);
                }
            }
        } else if (model == 1){     // "speedBump" model
            if (EventTime < 0.0){
                System.out.println("negative event time, debug");
            }
            if (nReset){
                trader.nReset((byte)2, (short) 50, purge);
            }
            double prob1, prob2, prob3, prob4, prob5;
            double x1, x2, x3, x4, x5;
            for (int i = 0; i < nEvents; i ++){
                // LAMBDA -> overall event frequency
                Lambda = (nHFT + NewNonHFT) * lambdaArrival + ReturningHFT * ReturnFrequencyHFT +
                        + ReturningNonHFT * ReturnFrequencyNonHFT + lambdaFVchange;
                EventTime += - Math.log(1.0 - Math.random()) / Lambda; // random exponential time

                // updating the lagged fundamental value
                while (!fundamentalValue.isEmpty() && EventTime > (fundamentalValue.firstKey() + infoDelay)){
                    FvLag = fundamentalValue.remove(fundamentalValue.firstKey());
                }
                // number of all agents to trade
                if (!waitingTraders.isEmpty() && (EventTime > waitingTraders.firstKey())){
                    Integer ID;
                    EventTime = waitingTraders.firstKey();
                    ID = waitingTraders.remove(waitingTraders.firstKey());

                    if (traders.containsKey(ID)){
                        //boolean isHFT = traders.get(ID).getIsHFT();
                        heldOrder = traders.get(ID).getOrder();
                        bi = book.getBookInfo();
                        if (heldOrder.isBuyOrder()){
                            /*if (write && (heldOrder.getPosition() != bi[1])){
                                System.out.println("different position");
                            }*/
                            if (heldOrder.getPosition() >= bi[1]){      // price, would pay more  // TODO: have to test new speedBump
                                heldOrder.setPosition(bi[1]);   // buy MO sets the position to ask
                                heldOrder.setTimeStamp(EventTime);
                            } else {                                    // price-> wouldn't submit MO
                                heldOrder = null;
                                traders.get(ID).setOrder(null);
                                if (traders.get(ID).getIsHFT()){
                                    ReturningHFT++;
                                    traderIDsHFT.add(ID);
                                } else {
                                    ReturningNonHFT++;
                                    traderIDsNonHFT.add(ID);
                                }
                            }
                        } else {
                            /*if (write && (heldOrder.getPosition() != bi[0])){
                                System.out.println("different position");
                            }*/
                            if (heldOrder.getPosition() <= bi[0]){  // price, could get less      // TODO: can change back afterwards
                                heldOrder.setPosition(bi[0]);   // buy MO sets the position to ask
                                heldOrder.setTimeStamp(EventTime);
                            } else {
                                heldOrder = null;
                                traders.get(ID).setOrder(null);
                                if (traders.get(ID).getIsHFT()){
                                    ReturningHFT++;
                                    traderIDsHFT.add(ID);
                                } else {
                                    ReturningNonHFT++;
                                    traderIDsNonHFT.add(ID);
                                }
                            }
                        }
                        ArrayList<Order> orders = new ArrayList<Order>();
                        if (heldOrder != null){orders.add(heldOrder);}

                        if (!orders.isEmpty()){
                            Integer IDr = book.transactionRule(ID , orders);
                            if (ID.equals(IDr)) { // executed against fringe, remove ID trader
                                traders.remove(ID);
                            } else if (IDr != null){ // returning trader has executed, remove him and the counterparty as well
                                if (traders.get(IDr).getIsHFT()){
                                    ReturningHFT--;
                                    traderIDsHFT.remove(IDr);
                                } else {
                                    ReturningNonHFT--;
                                    traderIDsNonHFT.remove(IDr);
                                }
                                traders.remove(ID);
                                traders.remove(IDr);
                            }           // else, he hasn't executed, leave him in the traders arrays
                        } /*else {
                            System.out.println("MO not there");
                        }*/
                    } /*else {
                        System.out.println("trader not there");
                    }*/
                } else {
                    prob1 = (double) (nHFT) * lambdaArrival / Lambda;
                    prob2 = (double) (NewNonHFT) * lambdaArrival / Lambda;
                    prob3 = (double) ReturningHFT * ReturnFrequencyHFT / Lambda;
                    prob4 = (double) ReturningNonHFT * ReturnFrequencyNonHFT / Lambda;
                    prob5 = lambdaFVchange / Lambda;
                    // for now prob of change in FV is equal to 0
                    x1 = prob1; // 1. new HFT
                    x2 = x1 + prob2; // 2. new nonHFT
                    x3 = x2 + prob3; // 3. reentry HFT
                    x4 = x3 + prob4; // 4. reentry nonHFT
                    x5 = x4 + prob5; // 5. Change in fundamental value
                    if (Math.abs(x5 - 1.0) > 0.00001){
                        System.out.println("Probabilities do not sum to 1.");
                    }

                    double rn = Math.random(); // to determine event
                    Trader tr;
                    Integer ID;
                    double FVrealization;
                    if (rn < x1){ // New arrival HFT
                        tr = new Trader(true, true, 0.0f);
                        ID = tr.getTraderID();
                        traders.put(ID, tr);
                        ArrayList<Order> orders = tr.decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV, FvLag, 0.0);
                        if (!orders.isEmpty()){
                            if ((orders.get(0).getAction() == 2 * end) || (orders.get(0).getAction() == 2 * end + 1)){
                                waitingTraders.put((EventTime + speedBump), ID);
                                continue;
                            }
                            Integer IDr = book.transactionRule(ID , orders);
                            if (IDr == null){ // order put to the book
                                traderIDsHFT.add(ID);
                                ReturningHFT++;
                            }
                        } else {    // no order, put the trader to the returning lists
                            traderIDsHFT.add(ID);
                            ReturningHFT++;
                        }
                    } else if (rn < x2){ // New arrival nonHFT
                        double rn2 = Math.random();
                        /*if (rn2 < DistributionPV[0]){
                            FVrealization = FprivateValues[0];
                        } else if (rn2 < DistributionPV[1]){
                            FVrealization = FprivateValues[1];
                        } else {
                            FVrealization = FprivateValues[2];
                        }*/
                        if (rn2 < DistributionPV[0]){FVrealization = FprivateValues[0];}
                        else if (rn2 < DistributionPV[1]){FVrealization = FprivateValues[1];}
                        else if (rn2 < DistributionPV[2]){FVrealization = FprivateValues[2];}
                        else if (rn2 < DistributionPV[3]){FVrealization = FprivateValues[3];}
                        else {FVrealization = FprivateValues[4];}

                        tr = new Trader(false, false, FVrealization);
                        ID = tr.getTraderID();
                        traders.put(ID, tr);
                        ArrayList<Order> orders = tr.decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV, FvLag, 0.0);
                        if (!orders.isEmpty()){
                            if ((orders.get(0).getAction() == 2 * end) || (orders.get(0).getAction() == 2 * end + 1)){
                                waitingTraders.put((EventTime + speedBump), ID);
                                continue;
                            }
                            Integer IDr = book.transactionRule(ID , orders);
                            if (IDr == null){ // order put to the book
                                traderIDsNonHFT.add(ID);
                                ReturningNonHFT++;
                            }
                        } else {    // no order, put the trader to the returning lists
                            traderIDsNonHFT.add(ID);
                            ReturningNonHFT++;
                        }
                    } else if (rn < x3){ // Returning HFT
                        ID = traderIDsHFT.get((int) (Math.random() * traderIDsHFT.size()));
                        ArrayList<Order> orders = traders.get(ID).decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV, FvLag, 0.0);
                        if (!orders.isEmpty()){
                            ArrayList<Order> orders2hold = new ArrayList<Order>();
                            for (Order o : orders){
                                if (o.getAction() == (2 * end) || (o.getAction() == 2 * end + 1)){
                                    orders2hold.add(o);
                                    waitingTraders.put((EventTime + speedBump), ID);
                                }
                            }
                            for (Order o : orders2hold){
                                orders.remove(o);
                                ReturningHFT--;
                                traderIDsHFT.remove(ID);
                            }
                            book.transactionRule(ID , orders);
                        }
                    } else if (rn < x4){ // Returning nonHFT
                        ID = traderIDsNonHFT.get((int)(Math.random() * traderIDsNonHFT.size()));
                        ArrayList<Order> orders = traders.get(ID).decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV, FvLag, 0.0);
                        if (!orders.isEmpty()){
                            ArrayList<Order> orders2hold = new ArrayList<Order>();
                            for (Order o : orders){
                                if (o.getAction() == (2 * end) || (o.getAction() == 2 * end + 1)){
                                    orders2hold.add(o);
                                    waitingTraders.put((EventTime + speedBump), ID);
                                }
                            }
                            for (Order o : orders2hold){
                                orders.remove(o);
                                ReturningNonHFT--;
                                traderIDsNonHFT.remove(ID);
                            }
                            book.transactionRule(ID , orders);
                        }
                    } else { // Change in FV
                        double rn3 = Math.random();
                        ArrayList<Integer> tradersExecuted = new ArrayList<Integer>();
                        if (rn3 < FVplus){
                            FV = FV + sigma * tickSize;
                            tradersExecuted = book.FVup(FV, EventTime, (int) sigma);
                        } else {
                            FV = FV - sigma * tickSize;
                            tradersExecuted = book.FVdown(FV, EventTime, (int) sigma);
                        }
                        for (Integer trID : tradersExecuted){
                            if (traders.get(trID).getIsHFT()){
                                if (traderIDsHFT.remove(trID)){
                                    ReturningHFT--;
                                }
                            } else {
                                if (traderIDsNonHFT.remove(trID)){
                                    ReturningNonHFT--;
                                }
                            }
                            traders.remove(trID);
                        }
                        fundamentalValue.put(EventTime, FV);
                    }
                }

                if (ReturningHFT != traderIDsHFT.size() || ReturningNonHFT != traderIDsNonHFT.size()){
                    System.out.println("error, number of traders not equal");
                }
                if (i % 10000000 == 0) {
                    System.out.println(i + " events");
                }

                if (i % 10000 == 0){
                    writeWrite();
                }
                if (i % 1000000 == 0) {
                    writePrint(i);
                }
            }

        } else if (model == 2){     // "transparency" model
            if (EventTime < 0.0){
                System.out.println("negative event time, debug");
            }
            if (nReset){
                trader.nReset((byte)2, (short) 50, purge);
            }
            double prob1, prob2, prob3, prob4, prob5;
            double x1, x2, x3, x4, x5;
            BookInfo = book.getBookInfo();
            BookSizes = book.getBookSizes();
            for (int i = 0; i < nEvents; i ++){
                // LAMBDA -> overall event frequency
                Lambda = (nHFT + NewNonHFT) * lambdaArrival + ReturningHFT * ReturnFrequencyHFT +
                        + ReturningNonHFT * ReturnFrequencyNonHFT + lambdaFVchange;
                EventTime += - Math.log(1.0 - Math.random()) / Lambda; // random exponential time

                // updating the lagged fundamental value
                /*while (!fundamentalValue.isEmpty() && EventTime > (fundamentalValue.firstKey() + infoDelay)){
                    FvLag = fundamentalValue.remove(fundamentalValue.firstKey());
                }*/
                // TODO: uncomment here and at the end where the fundamental value changes

                if (EventTime > lastUpdateTime + transparencyPeriod){
                    BookInfo = book.getBookInfo();
                    BookSizes = book.getBookSizes();
                    lastUpdateTime = ((int) (EventTime / transparencyPeriod)) * transparencyPeriod;
                    FvLag = FV; // TODO: change the first FvLag to FV after tested for all trader events, make slow uninformed again
                }
                if (FvLag != FV){
                    System.out.println("not equal");
                }
                // number of all agents to trade
                prob1 = (double) (nHFT) * lambdaArrival / Lambda;
                prob2 = (double) (NewNonHFT) * lambdaArrival / Lambda;
                prob3 = (double) ReturningHFT * ReturnFrequencyHFT / Lambda;
                prob4 = (double) ReturningNonHFT * ReturnFrequencyNonHFT / Lambda;
                prob5 = lambdaFVchange / Lambda;
                // for now prob of change in FV is equal to 0
                x1 = prob1; // 1. new HFT
                x2 = x1 + prob2; // 2. new nonHFT
                x3 = x2 + prob3; // 3. reentry HFT
                x4 = x3 + prob4; // 4. reentry nonHFT
                x5 = x4 + prob5; // 5. Change in fundamental value
                if (Math.abs(x5 - 1.0) > 0.00001){
                    System.out.println("Probabilities do not sum to 1.");
                }

                double rn = Math.random(); // to determine event
                Trader tr;
                Integer ID;
                double FVrealization;
                if (rn < x1){ // New arrival HFT
                    tr = new Trader(true, false, 0.0f);
                    ID = tr.getTraderID();
                    traders.put(ID, tr);
                    ArrayList<Order> orders = tr.decision(BookSizes, BookInfo, EventTime, FV, FvLag, lastUpdateTime);
                    if (!orders.isEmpty()){
                        for (Order o : orders) {
                            bi = book.getBookInfo();
                            if (o.isBuyOrder()) {
                                if (o.getPosition() > bi[1]) {      // price, would pay more
                                    o.setPosition(bi[1]);       // buy sets the position to ask
                                }
                            } else {
                                if (o.getPosition() < bi[0]) {  // price, could get less
                                    o.setPosition(bi[0]);       // sell MO sets the position to bid
                                }
                            }
                        }
                        Integer IDr = book.transactionRule(ID , orders);
                        if (IDr == null){ // order put to the book
                            traderIDsHFT.add(ID);
                            ReturningHFT++;
                        } else if (!ID.equals(IDr)) { // returns another trader-> executed counterparty
                            if (traders.get(IDr).getIsHFT()){
                                ReturningHFT--;
                                traderIDsHFT.remove(IDr);
                            } else {
                                ReturningNonHFT--;
                                traderIDsNonHFT.remove(IDr);
                            }
                            traders.remove(IDr);
                            traders.remove(ID);
                        } else { // MO against fringe
                            traders.remove(ID);
                        }
                    } else {    // no order, put the trader to the returning lists
                        traderIDsHFT.add(ID);
                        ReturningHFT++;
                    }
                } else if (rn < x2){ // New arrival nonHFT
                    double rn2 = Math.random();
                        /*if (rn2 < DistributionPV[0]){
                            FVrealization = FprivateValues[0];
                        } else if (rn2 < DistributionPV[1]){
                            FVrealization = FprivateValues[1];
                        } else {
                            FVrealization = FprivateValues[2];
                        }*/
                    if (rn2 < DistributionPV[0]){FVrealization = FprivateValues[0];}
                    else if (rn2 < DistributionPV[1]){FVrealization = FprivateValues[1];}
                    else if (rn2 < DistributionPV[2]){FVrealization = FprivateValues[2];}
                    else if (rn2 < DistributionPV[3]){FVrealization = FprivateValues[3];}
                    else {FVrealization = FprivateValues[4];}

                    tr = new Trader(false, false, FVrealization);
                    ID = tr.getTraderID();
                    traders.put(ID, tr);
                    ArrayList<Order> orders = tr.decision(BookSizes, BookInfo, EventTime, FV, FvLag, lastUpdateTime);
                    if (!orders.isEmpty()){
                        for (Order o : orders) {
                            bi = book.getBookInfo();
                            if (o.isBuyOrder()) {
                                if (o.getPosition() > bi[1]) {      // price, would pay more
                                    o.setPosition(bi[1]);       // buy sets the position to ask
                                }
                            } else {
                                if (o.getPosition() < bi[0]) {  // price, could get less
                                    o.setPosition(bi[0]);       // sell MO sets the position to bid
                                }
                            }
                        }
                        Integer IDr = book.transactionRule(ID , orders);
                        if (IDr == null){ // order put to the book
                            traderIDsNonHFT.add(ID);
                            ReturningNonHFT++;
                        } else if (!ID.equals(IDr)) { // returns another trader-> executed counterparty
                            if (traders.get(IDr).getIsHFT()){
                                ReturningHFT--;
                                traderIDsHFT.remove(IDr);
                            } else {
                                traderIDsNonHFT.remove(IDr);
                                ReturningNonHFT--;
                            }
                            traders.remove(IDr);
                            traders.remove(ID);
                        } else { // MO against fringe
                            traders.remove(ID);
                        }
                    } else {    // no order, put the trader to the returning lists
                        traderIDsNonHFT.add(ID);
                        ReturningNonHFT++;
                    }
                } else if (rn < x3){ // Returning HFT
                    ID = traderIDsHFT.get((int) (Math.random() * traderIDsHFT.size()));
                    ArrayList<Order> orders = traders.get(ID).decision(BookSizes, BookInfo, EventTime, FV, FvLag, lastUpdateTime);
                    if (!orders.isEmpty()){
                        for (Order o : orders) {
                            if (!o.isCancelled()){
                                bi = book.getBookInfo();
                                if (o.isBuyOrder()) {
                                    if (o.getPosition() > bi[1]) {      // price, would pay more
                                        o.setPosition(bi[1]);       // buy sets the position to ask
                                    }
                                } else {
                                    if (o.getPosition() < bi[0]) {  // price, could get less
                                        o.setPosition(bi[0]);       // sell MO sets the position to bid
                                    }
                                }
                            }
                        }
                        Integer IDr = book.transactionRule(ID , orders);
                        if (ID.equals(IDr)) { // executed against fringe, remove ID trader
                            traders.remove(ID);
                            ReturningHFT--;
                            traderIDsHFT.remove(ID);
                        } else if (IDr != null){ // returning trader has executed, remove him and the counterparty as well
                            ReturningHFT--;
                            traderIDsHFT.remove(ID);
                            if (traders.get(IDr).getIsHFT()){
                                ReturningHFT--;
                                traderIDsHFT.remove(IDr);
                            } else {
                                ReturningNonHFT--;
                                traderIDsNonHFT.remove(IDr);
                            }
                            traders.remove(ID);
                            traders.remove(IDr);
                        }
                    }
                } else if (rn < x4){ // Returning nonHFT
                    ID = traderIDsNonHFT.get((int)(Math.random() * traderIDsNonHFT.size()));
                    ArrayList<Order> orders = traders.get(ID).decision(BookSizes,BookInfo, EventTime, FV, FvLag, lastUpdateTime);
                    if (!orders.isEmpty()){
                        for (Order o : orders) {
                            if (!o.isCancelled()){
                                bi = book.getBookInfo();
                                if (o.isBuyOrder()) {
                                    if (o.getPosition() > bi[1]) {      // price, would pay more
                                        o.setPosition(bi[1]);       // buy sets the position to ask
                                    }
                                } else {
                                    if (o.getPosition() < bi[0]) {  // price, could get less
                                        o.setPosition(bi[0]);       // sell MO sets the position to bid
                                    }
                                }
                            }
                        }
                        Integer IDr = book.transactionRule(ID , orders);
                        if (ID.equals(IDr)){ // executed against fringe, remove ID trader
                            traders.remove(ID);
                            ReturningNonHFT--;
                            traderIDsNonHFT.remove(ID);
                        } else if (IDr != null){ // returning trader has executed, remove him and the counterparty as well
                            ReturningNonHFT--;
                            traderIDsNonHFT.remove(ID);
                            if (traders.get(IDr).getIsHFT()){
                                ReturningHFT--;
                                traderIDsHFT.remove(IDr);
                            } else {
                                ReturningNonHFT--;
                                traderIDsNonHFT.remove(IDr);
                            }
                            traders.remove(ID);
                            traders.remove(IDr);
                        }
                    }
                } else { // Change in FV
                    double rn3 = Math.random();
                    ArrayList<Integer> tradersExecuted = new ArrayList<Integer>();
                    if (rn3 < FVplus){
                        FV = FV + sigma * tickSize;
                        tradersExecuted = book.FVup(FV, EventTime, (int) sigma);
                    } else {
                        FV = FV - sigma * tickSize;
                        tradersExecuted = book.FVdown(FV, EventTime, (int) sigma);
                    }
                    for (Integer trID : tradersExecuted){
                        if (traders.get(trID).getIsHFT()){
                            ReturningHFT--;
                            traderIDsHFT.remove(trID);
                        } else {
                            ReturningNonHFT--;
                            traderIDsNonHFT.remove(trID);
                        }
                        traders.remove(trID);
                    }
                    //fundamentalValue.put(EventTime, FV);
                }

                if (ReturningHFT != traderIDsHFT.size() || ReturningNonHFT != traderIDsNonHFT.size()){
                    System.out.println("error, number of traders not equal");
                }
                if (i % 10000000 == 0) {
                    System.out.println(i + " events");
                }

                if (i % 10000 == 0){
                    writeWrite();
                }
                if (i % 1000000 == 0) {
                    writePrint(i);
                }
            }

        }


        if (convergence != "none"){
            convergenceStat = trader.printConvergence(20, convergence, write);
        }
        return new double[]{EventTime, FV, ReturningHFT, ReturningNonHFT, convergenceStat};
    }

    private void writeWrite (){
        if (writeHistogram){
            trader.writeHistogram(book.getBookSizes());
        }
        bi = book.getBookInfo();
        h.addDepth(bi);
        h.addQuotedSpread(bi[1] - bi[0]);
        if (write){
            Iterator keys = waitingTraders.values().iterator();         // counting guys in the waiting queue
            Integer key;
            while (keys.hasNext()){
                key = (Integer) keys.next();
                if (traders.containsKey(key)){
                    if (traders.get(key).getIsHFT()){
                        int x = tifCounts.get(5);
                        tifCounts.put(5, x + 1);
                    } else {
                        int pv = traders.get(key).getPv();
                        int x = tifCounts.get(pv);
                        tifCounts.put(pv, x + 1);
                    }
                }
            }
            int y = population.get(5);
            population.put(5, y + ReturningHFT);
            keys = traderIDsNonHFT.iterator();
            while (keys.hasNext()){
                key = (Integer) keys.next();
                int pv = traders.get(key).getPv();
                int x = population.get(pv);
                population.put(pv, x + 1);
            }

        }
    }

    private void writePrint (int i){
        h.addStatisticsData(i, trader.getStatesCount()); // multiple payoffs count
        if (writeDiagnostics){
            if (i != 0){trader.printDiagnostics();}
            trader.resetDiagnostics();
        }
        if (writeHistogram){
            if (i != 0){trader.printHistogram();}
            trader.resetHistogram();
        }
        if (write){
            if (i != 0){
                h.printTransactions(header, outputNameTransactions);
                h.printBookData(header, outputNameBookData);
                trader.printDecisions();
                trader.printTif(tifTimes, tifCounts, population);
            }
            trader.resetDecisionHistory();
            for (int j = 0; j < 6; j++){
                tifCounts.put(j, 0);
                tifTimes.put(j, 0.0);
                population.put(j, 0);
            }
        }
        h.printStatisticsData(header, outputNameStatsData);
        h.resetHistory();

        /*if (i % 5000000 == 0) {
        if (purge){
        trader.purge();
        }*/

    }

}
