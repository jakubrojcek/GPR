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
    double sigma;
    float tickSize;
    double[] FprivateValues;
    double[] DistributionPV;
    double FVplus;
    int ReturningHFT = 0;
    int ReturningNonHFT = 0;
    double convergenceStat = 0.0;
    ArrayList<Integer> traderIDsHFT;            // holder for IDs of HFT traders
    ArrayList<Integer> traderIDsNonHFT;         // holder for IDs of nonHFT traders
    TreeMap<Double, Integer> waitingTraders;    // waiting traders, key is time, value is trader's ID
    HashMap<Integer, Trader> traders;
    History h;
    Trader trader;
    LOB_LinkedHashMap book;
    Order heldOrder;                            // held market order in speedBump case
    int[] bi;                                   // book info holder
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
                     String trans, String bookd, double sb, int e){
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
    }

    public double[] run(int nEvents, int nHFT, int NewNonHFT, int rHFT,
                        int rNonHFT, double EventTime, double FV, boolean w,
                        boolean p, boolean n, boolean wd, boolean wh, String convergence){
        ReturningHFT = rHFT;
        ReturningNonHFT = rNonHFT;
        write = w;
        writeDiagnostics = wd;
        writeHistogram = wh;
        purge = p;
        nReset = n;

        if (model == 0){        // "returning" model
            if (EventTime < 0.0){
                System.out.println("negative event time, debug");
            }
            if (nReset){
                trader.nReset((byte)3, (short) 100, purge);
            }
            double prob1, prob2, prob3, prob4, prob5;
            double x1, x2, x3, x4, x5;
            for (int i = 0; i < nEvents; i ++){
                // LAMBDA -> overall event frequency
                Lambda = (nHFT + NewNonHFT) * lambdaArrival + ReturningHFT * ReturnFrequencyHFT +
                        + ReturningNonHFT * ReturnFrequencyNonHFT + lambdaFVchange;
                EventTime += - Math.log(1.0 - Math.random()) / Lambda; // random exponential time

                // number of all agents to trade
                if (!waitingTraders.isEmpty() && (EventTime > waitingTraders.firstKey())){
                    Integer ID;
                    //while (!waitingTraders.isEmpty() && EventTime > waitingTraders.firstKey()){
                    EventTime = waitingTraders.firstKey();   // TODO: remove afterwards
                    ID = waitingTraders.remove(waitingTraders.firstKey());
                    if (traders.containsKey(ID)){
                        boolean isHFT = traders.get(ID).getIsHFT();
                        /*if (isHFT){
                            int x = tifCounts.get(5);
                            tifCounts.put(5, x + 1);
                            double y = tifTimes.get(5);
                            tifTimes.put(5, y + tif);
                        } else {
                            int pv = traders.get(ID).getPv();
                            int x = tifCounts.get(pv);
                            tifCounts.put(pv, x + 1);
                            double y = tifTimes.get(pv);
                            tifTimes.put(pv, y + tif);
                        }*/
                        // TODO: collect traders statistics here na remove afterwards
                        ArrayList<Order> orders = traders.get(ID).decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV);
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
                        tr = new Trader(true, 0.0f);
                        ID = tr.getTraderID();
                        traders.put(ID, tr);
                        ArrayList<Order> orders = tr.decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV);
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

                        tr = new Trader(false, FVrealization);
                        ID = tr.getTraderID();
                        traders.put(ID, tr);
                        ArrayList<Order> orders = tr.decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV);
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
                            /*int x = tifCounts.get(5);     // TODO: delete these four lines afterwards
                            tifCounts.put(5, x + 1);
                            double y = tifTimes.get(5);
                            tifTimes.put(5, y + EventTime - traders.get(ID).getOrder().getTimeStamp());*/
                        } else {
                            ArrayList<Order> orders = traders.get(ID).decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV);
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
                        ID = traderIDsNonHFT.get(((int) Math.random() * traderIDsNonHFT.size()));
                        if (traders.get(ID).getOrder() != null &&
                                ((EventTime - traders.get(ID).getOrder().getTimeStamp()) < tif)){
                            waitingTraders.put(traders.get(ID).getOrder().getTimeStamp() + tif, ID);
                            /*int pv = traders.get(ID).getPv();  // TODO: remove this five lines afterwards
                            int x = tifCounts.get(pv);
                            tifCounts.put(pv, x + 1);
                            double y = tifTimes.get(pv);
                            tifTimes.put(pv, y + EventTime - traders.get(ID).getOrder().getTimeStamp());*/
                        } else {
                            ArrayList<Order> orders = traders.get(ID).decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV);
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
                trader.nReset((byte)3, (short) 100, purge);
            }
            double prob1, prob2, prob3, prob4, prob5;
            double x1, x2, x3, x4, x5;
            for (int i = 0; i < nEvents; i ++){
                // LAMBDA -> overall event frequency
                Lambda = (nHFT + NewNonHFT) * lambdaArrival + ReturningHFT * ReturnFrequencyHFT +
                        + ReturningNonHFT * ReturnFrequencyNonHFT + lambdaFVchange;
                EventTime += - Math.log(1.0 - Math.random()) / Lambda; // random exponential time

                // number of all agents to trade
                if (!waitingTraders.isEmpty() && (EventTime > waitingTraders.firstKey())){
                    Integer ID;
                    EventTime = waitingTraders.firstKey();
                    ID = waitingTraders.remove(waitingTraders.firstKey());

                    if (traders.containsKey(ID)){
                        //boolean isHFT = traders.get(ID).getIsHFT();
                        heldOrder = traders.get(ID).getOrder();
                        if (!ID.equals(heldOrder.getTraderID())){
                            System.out.println("debug here: order not from the trader");  // TODO: delete afterwards
                        }
                        bi = book.getBookInfo();
                        if (heldOrder.isBuyOrder()){
                            heldOrder.setPosition(bi[1]);   // buy MO sets the position to ask
                            heldOrder.setTimeStamp(EventTime);
                        } else {
                            heldOrder.setPosition(bi[0]);   // sell MO sets the position to bid
                            heldOrder.setTimeStamp(EventTime);
                        }
                        ArrayList<Order> orders = new ArrayList<Order>();
                        orders.add(heldOrder);

                        if (!orders.isEmpty()){
                            Integer IDr = book.transactionRule(ID , orders);
                            if (ID.equals(IDr)) { // executed against fringe, remove ID trader
                                traders.remove(ID);
                                /*if (isHFT){                   // TODO: not needed, right?
                                    ReturningHFT--;
                                    traderIDsHFT.remove(ID);
                                } else {
                                    ReturningNonHFT--;
                                    traderIDsNonHFT.remove(ID);
                                }*/
                            } else if (IDr != null){ // returning trader has executed, remove him and the counterparty as well
                                /*if (isHFT){                   // TODO: not needed, right?
                                    ReturningHFT--;
                                    traderIDsHFT.remove(ID);
                                } else {
                                    ReturningNonHFT--;
                                    traderIDsNonHFT.remove(ID);
                                }*/
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
                        } else {
                            System.out.println("debug here: orders is empty");  // TODO: delete afterwards
                        }
                    } else {
                        System.out.println("debug here: trader is not here, it's ok if isTraded == true");  // TODO: delete afterwards
                    }
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
                        tr = new Trader(true, 0.0f);
                        ID = tr.getTraderID();
                        traders.put(ID, tr);
                        ArrayList<Order> orders = tr.decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV);
                        if (!orders.isEmpty()){
                            if ((orders.get(0).getAction() == 2 * end) || (orders.get(0).getAction() == 2 * end + 1)){
                                waitingTraders.put(EventTime + speedBump, ID);
                                continue;
                            }  // TODO: test this part more
                            Integer IDr = book.transactionRule(ID , orders);
                            if (IDr == null){ // order put to the book
                                traderIDsHFT.add(ID);
                                ReturningHFT++;
                            }
                            /*if (IDr == null){ // order put to the book    // TODO: is this needed?
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
                            }*/
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

                        tr = new Trader(false, FVrealization);
                        ID = tr.getTraderID();
                        traders.put(ID, tr);
                        ArrayList<Order> orders = tr.decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV);
                        if (!orders.isEmpty()){
                            if ((orders.get(0).getAction() == 2 * end) || (orders.get(0).getAction() == 2 * end + 1)){
                                waitingTraders.put(EventTime + speedBump, ID);
                                continue;
                            }  // TODO: test this part more
                            Integer IDr = book.transactionRule(ID , orders);
                            if (IDr == null){ // order put to the book
                                traderIDsNonHFT.add(ID);
                                ReturningNonHFT++;                          // TODO: is it needed?
                            } /*else if (!ID.equals(IDr)) { // returns another trader-> executed counterparty
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
                            }*/
                        } else {    // no order, put the trader to the returning lists
                            traderIDsNonHFT.add(ID);
                            ReturningNonHFT++;
                        }
                    } else if (rn < x3){ // Returning HFT
                        ID = traderIDsHFT.get((int) (Math.random() * traderIDsHFT.size()));
                        ArrayList<Order> orders = traders.get(ID).decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV);
                        if (!orders.isEmpty()){
                            // TODO: think about IDs handling and deleting
                            ArrayList<Order> orders2hold = new ArrayList<Order>();
                            for (Order o : orders){
                                if (o.getAction() == (2 * end) || (o.getAction() == 2 * end + 1)){
                                    orders2hold.add(o);
                                    waitingTraders.put(EventTime + speedBump, ID);
                                }
                            }
                            for (Order o : orders2hold){
                                orders.remove(o);
                                ReturningHFT--;
                                traderIDsHFT.remove(ID);
                            }
                            book.transactionRule(ID , orders);
                            /*if (ID.equals(IDr)) { // executed against fringe, remove ID trader // TODO: don't need this?
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
                            }*/
                        }
                    } else if (rn < x4){ // Returning nonHFT
                        ID = traderIDsNonHFT.get(((int) Math.random() * traderIDsNonHFT.size()));
                        ArrayList<Order> orders = traders.get(ID).decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV);
                        if (!orders.isEmpty()){
                            // TODO: think about IDs handling and deleting
                            ArrayList<Order> orders2hold = new ArrayList<Order>();
                            for (Order o : orders){
                                if (o.getAction() == (2 * end) || (o.getAction() == 2 * end + 1)){
                                    orders2hold.add(o);
                                    waitingTraders.put(EventTime + speedBump, ID);
                                }
                            }
                            for (Order o : orders2hold){
                                orders.remove(o);
                                ReturningNonHFT--;
                                traderIDsNonHFT.remove(ID);
                            }
                            book.transactionRule(ID , orders);
                            /*if (ID.equals(IDr)){ // executed against fringe, remove ID trader // TODO: don't need this?
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
                            }*/
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
                                }  else {
                                    System.out.println("debug here: trader not found");  // TODO: delete afterwards
                                }
                            } else {
                                if (traderIDsNonHFT.remove(trID)){
                                    ReturningNonHFT--;
                                } else {
                                    System.out.println("debug here: trader not found");  // TODO: delete afterwards
                                }
                            }
                            traders.remove(trID);
                        }
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

        }
        if (convergence != "none"){                                          // TODO: distinguish with separate boolean about second type convergence
            convergenceStat = trader.printConvergence(15, convergence, write);
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
            population.put(5, ReturningHFT);
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
            trader.printDiagnostics();
            trader.resetDiagnostics();
        }
        if (writeHistogram){
            trader.printHistogram();
            trader.resetHistogram();
        }
        if (write){
            h.printTransactions(header, outputNameTransactions);
            h.printBookData(header, outputNameBookData);
            trader.printDecisions();
            trader.resetDecisionHistory();
            trader.printTif(tifTimes, tifCounts, population);
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
