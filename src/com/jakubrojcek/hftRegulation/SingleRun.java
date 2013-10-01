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
    double ReturnFrequencyHFT;
    double ReturnFrequencyNonHFT;
    double sigma;
    float tickSize;
    float[] FprivateValues;
    float deltaLow;
    double FVplus;
    HashMap<Integer, Trader> traders;
    History h;
    Trader trader;
    LOB_LinkedHashMap book;

    boolean header = false;
    String outputNameTransactions;      // output file name
    String outputNameBookData;          // output file name
    String outputNameStatsData;         // output file name

    boolean write = false;              // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
    boolean writeDiagnostics = false;   // write diagnostics
    boolean writeHistogram = false;     // write histogram
    boolean purge = false;              // purge in this com.jakubrojcek.gpr2005a.SingleRun?
    boolean nReset = false;             // reset n in this com.jakubrojcek.gpr2005a.SingleRun?

    double Lambda;


    public SingleRun(String m, double lambdaArrival, double ReturnFrequencyHFT, double ReturnFrequencyNonHFT,
                     float[] FprivateValues, double pvMean, double pvStdev, float dl, double sigma,
                     float tickSize, double FVplus, boolean head, LOB_LinkedHashMap b,
                     HashMap<Integer, Trader> ts, History his, Trader TR, String stats, String trans,
                     String bookd){
        this.model = m;
        this.lambdaArrival = lambdaArrival;
        this.ReturnFrequencyHFT = ReturnFrequencyHFT;
        this.ReturnFrequencyNonHFT = ReturnFrequencyNonHFT;
        this.FprivateValues = FprivateValues;
        this.sigma = sigma;
        this.deltaLow = dl;
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
                      double EventTime, double FV,
                      boolean w, boolean p, boolean n, boolean wd, boolean wh){
        write = w;
        writeDiagnostics = wd;
        writeHistogram = wh;
        purge = p;
        nReset = n;
        if (purge){
            trader.purge();
        }
        if (nReset){
            trader.nReset((byte)1, (short) 1);
        }

        if (model == "returning"){
            int ReturningHFT;
            int ReturningNonHFT;
            double prob1;
            double prob2;
            double prob3;
            double prob4;
            double prob5;
            for (int i = 0; i < nEvents; i ++){
                // 1. new HFT
                // 2. new nonHFT
                // 3. reentry HFT
                // 4. reentry nonHFT
                // 5. Change in fundamental value
                ReturningHFT = book.getnReturningHFT();
                // all HFT already in the book
                ReturningNonHFT = book.getnReturningNonHFT();
                // all NonHFT already in the book
                int nAll = NewNonHFT + nHFT + ReturningHFT + ReturningNonHFT;
                // LAMBDA -> overall event frequency
                Lambda = (nHFT + NewNonHFT) * lambdaArrival + ReturningHFT * ReturnFrequencyHFT +
                        + ReturningNonHFT * ReturnFrequencyNonHFT;
                EventTime += - Math.log(1.0 - Math.random()) / Lambda; // random exponential time
                // number of all agents to trade
                prob1 = (double) nHFT / nAll * 0.92;
                prob2 = (double) NewNonHFT / nAll * 0.92;
                prob3 = (double) ReturningHFT / nAll * 0.92;
                prob4 = (double) ReturningNonHFT / nAll * 0.92;
                prob5 = 0.08;

                // for now prob of change in FV is equal to 0
                double x1 = prob1;
                double x2 = x1 + prob2;
                double x3 = x2 + prob3;
                double x4 = x3 + prob4;
                double x5 = x4 + prob5;


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
                    //System.out.println("New arrival HFT ID: " + ID);
                    ArrayList<Order> orders = tr.decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV);
                    if (orders != null){
                        ID = book.transactionRule(ID , orders);
                        if (ID != null){
                            traders.put(ID, tr);
                        }
                    } else {traders.put(ID, tr);}

                } else if (rn < x2){                   // New arrival nonHFT
                    double rn2 = Math.random();
                    if (rn2 < 0.3334){
                        FVrealization = FprivateValues[0];
                    } else if (rn2 < 0.6667){
                        FVrealization = FprivateValues[1];
                    } else {
                        FVrealization = FprivateValues[2];
                    }
                    //System.out.println("FV realization = " + FVrealization);
                    tr = new Trader(false, FVrealization);
                    ID = tr.getTraderID();
                    //System.out.println("New arrival nonHFT ID: " + ID);
                    traders.put(ID, tr);
                    ArrayList<Order> orders = tr.decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV);
                    if (orders != null){
                        ID = book.transactionRule(ID , orders);
                        if (ID != null){
                            traders.put(ID, tr);
                        }
                    } else {traders.put(ID, tr);}
                } else if (rn < x3){                   // Returning HFT
                    ID = book.randomHFTtraderID();
                    ArrayList<Order> orders = traders.get(ID).decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV);
                    if (orders != null){
                        ID = book.transactionRule(ID , orders);
                        if (ID == null){
                            traders.remove(ID);        // returning trader has executed, remove him
                        }
                    }
                    // TODO: I have null for both the same order and for cancellation, reconcile
                } else if (rn < x4){                   // Returning nonHFT
                    ID = book.randomNonHFTtraderID();
                    ArrayList<Order> orders = traders.get(ID).decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV);
                    if (orders != null){
                        ID = book.transactionRule(ID , orders);
                        if (ID == null){
                            traders.remove(ID);        // returning trader has executed, remove him
                        }
                    }
                    book.traderIDsNonHFT.remove(book.traderIDsNonHFT.indexOf(ID));
                    traders.remove(ID);

                } else{                                // Change in FV
                    double rn3 = Math.random();
                    if (rn3 < FVplus){
                        FV = FV + sigma * tickSize;
                        book.FVup(FV, EventTime, (int) sigma);
                    } else {
                        FV = FV - sigma * tickSize;
                        book.FVdown(FV, EventTime, (int) sigma);
                    }
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
        return new double[]{EventTime, FV};
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
        h.printStatisticsData(header, outputNameStatsData);
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
