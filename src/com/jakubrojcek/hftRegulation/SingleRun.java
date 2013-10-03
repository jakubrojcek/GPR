package com.jakubrojcek.hftRegulation;

import com.jakubrojcek.Order;
import java.util.ArrayList;
import java.util.HashMap;

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

    String model;

    double lambdaArrival;
    double lambdaFVchange;
    double ReturnFrequencyHFT;
    double ReturnFrequencyNonHFT;
    double sigma;
    float tickSize;
    float[] FprivateValues;
    double[] DistributionPV;
    double FVplus;
    int ReturningHFT = 0;
    int ReturningNonHFT = 0;
    HashMap<Integer, Trader> traders;
    History h;
    Trader trader;
    LOB_LinkedHashMap book;

    boolean header = false;
    String outputNameTransactions;      // output file name
    String outputNameBookData;          // output file name
    String outputNameStatsData;         // output file name

    boolean write = false;              // writeDecisions output in this SingleRun?
    boolean writeDiagnostics = false;   // write diagnostics
    boolean writeHistogram = false;     // write histogram
    boolean purge = false;              // purge SingleRun?
    boolean nReset = false;             // reset SingleRun?

    double Lambda;


    public SingleRun(String m, double lambdaArrival, double lambdaFV, double ReturnFrequencyHFT, double ReturnFrequencyNonHFT,
                     float[] FprivateValues, double[] PVdist, double sigma, float tickSize, double FVplus, boolean head,
                     LOB_LinkedHashMap b, HashMap<Integer, Trader> ts, History his, Trader TR, String stats,
                     String trans, String bookd){
        this.model = m;
        this.lambdaArrival = lambdaArrival;
        this.lambdaFVchange = lambdaFV;
        this.ReturnFrequencyHFT = ReturnFrequencyHFT;
        this.ReturnFrequencyNonHFT = ReturnFrequencyNonHFT;
        this.FprivateValues = FprivateValues;
        this.DistributionPV = PVdist;
        this.sigma = sigma;
        this.tickSize = tickSize;
        this.FVplus = FVplus;
        traders = ts;
        h = his;
        trader = TR;
        book = b;
        outputNameStatsData = stats;
        outputNameTransactions = trans;
        outputNameBookData = bookd;
        header = head;
    }

    public double[] run(int nEvents, int nHFT, int NewNonHFT,
                      int rHFT, int rNonHFT, double EventTime, double FV,
                      boolean w, boolean p, boolean n, boolean wd, boolean wh){
        ReturningHFT = rHFT;
        ReturningNonHFT = rNonHFT;
        write = w;
        writeDiagnostics = wd;
        writeHistogram = wh;
        purge = p;
        nReset = n;
        ArrayList<Integer> traderIDsHFT = new ArrayList<Integer>();        // holder for IDs of HFT traders
        ArrayList<Integer> traderIDsNonHFT = new ArrayList<Integer>();     // holder for IDs of nonHFT traders
        if (purge){
            trader.purge();
        }
        if (nReset){
            trader.nReset((byte)1, (short) 1);
        }

        if (model == "returning"){
            double prob1, prob2, prob3, prob4, prob5;
            double x1, x2, x3, x4, x5;
            for (int i = 0; i < nEvents; i ++){
                // LAMBDA -> overall event frequency
                Lambda = (nHFT + NewNonHFT) * lambdaArrival + ReturningHFT * ReturnFrequencyHFT +
                        + ReturningNonHFT * ReturnFrequencyNonHFT + lambdaFVchange;
                EventTime += - Math.log(1.0 - Math.random()) / Lambda; // random exponential time
                // number of all agents to trade
                prob1 = (double) (nHFT) * lambdaArrival / Lambda;
                prob2 = (double) (NewNonHFT) * lambdaArrival / Lambda;
                prob3 = (double) ReturningHFT * ReturnFrequencyHFT / Lambda;
                prob4 = (double) ReturningNonHFT * ReturnFrequencyNonHFT / Lambda;
                prob5 = lambdaFVchange / Lambda;
                // for now prob of change in FV is equal to 0
                x1 = prob1;                            // 1. new HFT
                x2 = x1 + prob2;                       // 2. new nonHFT
                x3 = x2 + prob3;                       // 3. reentry HFT
                x4 = x3 + prob4;                       // 4. reentry nonHFT
                x5 = x4 + prob5;                       // 5. Change in fundamental value
                if (Math.abs(x5 - 1.0) > 0.00001){
                    System.out.println("Probabilities do not sum to 1.");
                }

                double rn = Math.random();             // to determine event
                Trader tr;
                Integer ID;
                float FVrealization;
                if (rn < x1){                          // New arrival HFT
                    tr = new Trader(true, 0.0f);
                    ID = tr.getTraderID();
                    ArrayList<Order> orders = tr.decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV);
                    if (!orders.isEmpty()){
                        Integer IDr = book.transactionRule(ID , orders);
                        if (IDr == null){              // order put to the book
                            traders.put(IDr, tr);
                            traderIDsHFT.add(IDr);
                            ReturningHFT++;
                        } else if (ID != IDr) {        // returns another trader-> executed counterparty
                            if (traders.get(IDr).getIsHFT()){
                                ReturningHFT--;
                                traderIDsHFT.remove(IDr);
                            } else {
                                ReturningNonHFT--;
                                traderIDsNonHFT.remove(IDr);
                            }
                            traders.remove(IDr);
                        }
                    } else {
                        traders.put(ID, tr);
                        traderIDsHFT.add(ID);
                        ReturningHFT++;
                    }
                } else if (rn < x2){                   // New arrival nonHFT
                    double rn2 = Math.random();
                    if (rn2 < DistributionPV[0]){
                        FVrealization = FprivateValues[0];
                    } else if (rn2 < DistributionPV[1]){
                        FVrealization = FprivateValues[1];
                    } else {
                        FVrealization = FprivateValues[2];
                    }
                    tr = new Trader(false, FVrealization);
                    ID = tr.getTraderID();
                    traders.put(ID, tr);
                    ArrayList<Order> orders = tr.decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV);
                    if (!orders.isEmpty()){
                        Integer IDr = book.transactionRule(ID , orders);
                        if (IDr == null){              // order put to the book
                            traders.put(ID, tr);
                            traderIDsNonHFT.add(ID);
                            ReturningNonHFT++;
                        } else if (ID != IDr) {        // returns another trader-> executed counterparty
                            if (traders.get(IDr).getIsHFT()){
                                ReturningHFT--;
                                traderIDsHFT.remove(IDr);
                            } else {
                                ReturningNonHFT--;
                                traderIDsNonHFT.remove(IDr);
                            }
                            traders.remove(IDr);
                        }
                    } else {
                        traders.put(ID, tr);
                        traderIDsNonHFT.add(ID);
                        ReturningNonHFT++;
                    }
                } else if (rn < x3){                   // Returning HFT
                    ID = traderIDsHFT.get((int) (Math.random() * traderIDsHFT.size()));
                    ArrayList<Order> orders = traders.get(ID).decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV);
                    if (!orders.isEmpty()){
                        ID = book.transactionRule(ID , orders);
                        if (ID != null){
                            traders.remove(ID);        // returning trader has executed, remove him
                            ReturningHFT--;
                            traderIDsHFT.remove(ID);
                        }
                    }
                    // TODO: I have null for both the same order and for cancellation, reconcile
                } else if (rn < x4){                   // Returning nonHFT
                    ID = traderIDsNonHFT.get((int) (Math.random() * traderIDsNonHFT.size()));
                    ArrayList<Order> orders = traders.get(ID).decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV);
                    if (!orders.isEmpty()){
                        ID = book.transactionRule(ID , orders);
                        if (ID != null){
                            traders.remove(ID);        // returning trader has executed, remove him
                            ReturningNonHFT--;
                            traderIDsNonHFT.remove(ID);
                        }
                    }
                } else{                                // Change in FV
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
                            traders.remove(trID);
                        } else {
                            ReturningNonHFT--;
                            traderIDsNonHFT.remove(trID);
                            traders.remove(trID);
                        }
                    }
                }
                if (ReturningHFT != traderIDsHFT.size() || ReturningNonHFT != traderIDsNonHFT.size()){
                    System.out.println("error");
                }

                if (i % 100000 == 0) {
                    System.out.println(i + " events");
                }

                if (i % 10000 == 0) {
                    writePrint(i);
                }
                /*if (i % 10000000 == 0) {              // TODO: put this printing outside the loop
                    h.addStatisticsData(i, trader.getStatesCount());   // multiple payoffs count
                    if (writeDiagnostics){
                        trader.printDiagnostics();
                        trader.resetDiagnostics();
                        //trader.printConvergence(100);
                    }
                    if (write){
                        h.printTransactions(header, outputNameTransactions);
                        h.printBookData(header, outputNameBookData);
                        trader.printDecisions();
                        trader.printHistogram();
                        trader.printDiagnostics();
                        trader.resetDecisionHistory();
                        trader.resetHistogram();
                        trader.resetDiagnostics();
                    }
                    h.printStatisticsData(header, outputNameStatsData);
                    h.resetHistory();

                }*/   //where events happen
               /* if (i % 100000000 == 0) {
                    if (purge){
                        trader.purge();
                    }
                    if (nReset){
                        trader.nReset((byte)3, (short) 10000);
                    }
                }*/
            }
        }
        if (write){
            trader.printConvergence(100);  // TODO: print out loud here :) not before, because it's with fixed beliefs
        }
        return new double[]{EventTime, FV, ReturningHFT, ReturningNonHFT};
    }

    private void writePrint (int i){
        h.addStatisticsData(i, trader.getStatesCount());   // multiple payoffs count
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
        }
        //h.printStatisticsData(header, outputNameStatsData);
        h.resetHistory();

         /*if (i % 5000000 == 0) {
            if (purge){
                trader.purge();
            }
            if (nReset){
                trader.nReset((byte)3, (short) 10000);
            }
        }*/
    }

}
