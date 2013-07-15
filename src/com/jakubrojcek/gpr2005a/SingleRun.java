package com.jakubrojcek.gpr2005a;

import java.util.*;

import com.jakubrojcek.Order;
import com.jakubrojcek.PriceOrder;
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Created with IntelliJ IDEA.
 * User: Jakub
 * Date: 15.10.12
 * Time: 11:45
 * To change this template use File | Settings | File Templates.
 */
public class SingleRun {

    String model;

    double lambdaArrival;
    double ReturnFrequencyHFT;
    double ReturnFrequencyNonHFT;
    double sigma;
    float tickSize;
    float[] FprivateValues;
    NormalDistribution nd;
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
                     HashMap<Integer, Trader> ts, History his, Trader TR,  String stats, String trans,
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
        nd = new NormalDistribution(pvMean, pvStdev);
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
            trader.nReset((byte)1, (short) 100);
        }


        if (model == "GPR2005"){
            float privateValue;
            Trader tr;
            int ID;
            byte u2t;
            for (int i = 0; i < nEvents; i ++){
                EventTime += 0.000000001;
                // 1. new trader
                double rnHFT = 0.0;//Math.random();
                double rnU2T = 0.0;//Math.random();
                privateValue = 0.0f;
                boolean HFT = true;
                u2t = 1;
                if (rnHFT < 0.6){                         // proportion of nonHFT traders
                    privateValue = (float) nd.sample();
                    HFT = false;
                }

                if (rnU2T < 0.5){
                    u2t = 2;
                }

                tr = new Trader(HFT, privateValue, u2t);
                ID = tr.getTraderID();
                traders.put(ID, tr);
                // 2. & 3. decision and transaction rule
                ArrayList<Order> orders = tr.decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV);
                if (!orders.isEmpty()){
                    book.transactionRule(ID, orders);
                } else traders.remove(ID);
                // 4. Cancellations
                ArrayList<Order> ActiveOrders = book.getActiveOrders();
                ArrayList<Order> orders2remove = new ArrayList<Order>();
                for (Order ao : ActiveOrders){
                    int aoID = ao.getTraderID();
                    double FVbefore = traders.get(aoID).getPriceFV();         // TODO: separate for each order
                    boolean isBuy = ao.isBuyOrder();
                    boolean isHFT = traders.get(aoID).getIsHFT();
                    double delta = 0.0;
                    if (isHFT){
                        double deltaHat = isBuy ? 7.2 * Math.max(FVbefore - FV, 0)    // 0.2 * in base case
                                                : 7.2 * Math.max(FV - FVbefore, 0);
                        delta = Math.min(deltaLow + deltaHat, 0.99);
                    } else {
                        double deltaHat = isBuy ? 1.6 * Math.max(FVbefore - FV, 0)    // 0.2 * in base case
                                                : 1.6 * Math.max(FV - FVbefore, 0);
                        delta = Math.min(deltaLow + deltaHat, 0.64);
                    }
                    double rn = Math.random();             // to determine cancellation
                    if (rn < delta){
                        traders.get(aoID).cancel(ao);
                        book.tryCancel(ao); // it's trying to remove the worst order of the trader at that position
                        orders2remove.add(ao);
                    }
                }
                book.removeOrders(orders2remove);
                // 5. innovation of fundamental value
                double rn3 = 1.0;//Math.random();
                if (rn3 < FVplus * 0.08){
                    FV = FV + sigma * tickSize;
                    book.FVup(FV, EventTime, (int) sigma);
                    //book.FVup(FV, EventTime); //TODO: use twice for 1/16 ts
                    //System.out.println("up" + FV);
                } else if (rn3 < 0.08) {
                    FV = FV - sigma * tickSize;
                    book.FVdown(FV, EventTime, (int) sigma);
                    //book.FVdown(FV, EventTime); //TODO: use twice for 1/16 ts
                    //System.out.println("down" + FV);
                }

                // TODO: 6. Update rest of the traders from previous state = oldCode of current trader

                if (i % 100000 == 0) {
                    System.out.println(i + " events");
                }

                if (i % 100000 == 0) {
                    writePrint(i);
                }
            }

        } else if (model == "normal"){
            for (int i = 0; i < nEvents; i ++){

                // 1. new HFT
                // 2. new nonHFT
                // 3. reentry HFT
                // 4. reentry nonHFT
                // 5. Change in fundamental value


                int ReturningHFT = book.getnReturningHFT();
                // all HFT already in the book

                int ReturningNonHFT = book.getnReturningNonHFT();
                // all NonHFT already in the book

                int nAll = NewNonHFT + nHFT + ReturningHFT + ReturningNonHFT;

                // LAMBDA -> overall event frequency

                Lambda = (nHFT + NewNonHFT) * lambdaArrival + ReturningHFT * ReturnFrequencyHFT +
                        + ReturningNonHFT * ReturnFrequencyNonHFT;
                EventTime += - Math.log(1.0- Math.random()) / Lambda; // random exponential time

                // number of all agents to trade

                double prob1 = (double) nHFT / nAll * 0.92;    //TODO: times 0.92
                double prob2 = (double) NewNonHFT / nAll * 0.92;
                double prob3 = (double) ReturningHFT / nAll * 0.92;
                double prob4 = (double) ReturningNonHFT / nAll * 0.92;
                double prob5 = 0.08;

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
                int ID;
                float FVrealization;
                if (rn < x1){                          // New arrival HFT
                    tr = new Trader(true, 0, (byte) 1);
                    ID = tr.getTraderID();
                    /* if(book.removedTraders.contains(ID)){
                         System.out.println("new Zombie HFT");
                     }*/
                    //System.out.println("New arrival HFT ID: " + ID);
                    traders.put(ID, tr);
                    PriceOrder PO = tr.decision(book.getRank(ID), book.getBookSizes(), book.getBookInfo(),
                            EventTime, FV);
                    if (PO != null){
                        //book.transactionRule(PO.getPrice() , PO.getCurrentOrder());
                    } else {book.addTrader(ID);}

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
                    tr = new Trader(false, FVrealization, (byte)1);
                    ID = tr.getTraderID();
                    /* if(book.removedTraders.contains(ID)){
                         System.out.println("new zombie nonHFT");
                     }*/

                    //System.out.println("New arrival nonHFT ID: " + ID);
                    traders.put(ID, tr);
                    PriceOrder PO = tr.decision(book.getRank(ID), book.getBookSizes(), book.getBookInfo(),
                            EventTime, FV);

                    if (PO != null){
                        //book.transactionRule(PO.getPrice() , PO.getCurrentOrder());
                    } else {book.addTrader(ID);}
                } else if (rn < x3){                   // Returning HFT
                    ID = book.randomHFTtraderID();
                    //System.out.println("Returning HFT ID: " + ID);
                    /* if(book.removedTraders.contains(ID)){
                         System.out.println("old Zombie HFT");
                     }*/
                    PriceOrder PO = traders.get(ID).decision(book.getRank(ID), book.getBookSizes(), book.getBookInfo(),
                            EventTime, FV);
                    if (PO != null){
                        //book.transactionRule(PO.getPrice() , PO.getCurrentOrder());
                    } else {
                        //book.tryCancel(ID);                     // TODO: pass order here
                    }

                } else if (rn < x4){                   // Returning nonHFT
                    ID = book.randomNonHFTtraderID();
                    //System.out.println("Returning nonHFT ID: " + ID);
                    /*com.jakubrojcek.PriceOrder PO = traders.get(ID).decision(book.getRank(ID), book.getBookSizes(), book.getBookInfo(),
                            EventTime, FV);
                    if (PO != null){
                        book.transactionRule(PO.getPrice() , PO.getCurrentOrder());
                    } else {
                        book.tryCancel(ID);
                    }*/
                    //traders.get(ID).cancel(EventTime);          // TODO: cancel and uncomment above
                    //book.tryCancel(ID);                         // TODO: pass order here
                    book.traderIDsNonHFT.remove(book.traderIDsNonHFT.indexOf(ID));
                    traders.remove(ID);

                } else{                                // Change in FV
                    double rn3 = Math.random();
                    if (rn3 < FVplus){
                        //FV = FV + sigma * tickSize;
                        //book.FVup(FV, EventTime);
                        //book.FVup(FV, EventTime);    //TODO: use twice for 1/16 ts
                        //System.out.println("up" + FV);
                    } else {
                        //FV = FV - sigma * tickSize;
                        //book.FVdown(FV, EventTime);
                        //book.FVdown(FV, EventTime); //TODO: use twice for 1/16 ts
                        //System.out.println("down" + FV);
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
        return new double[]{EventTime, FV};
    }

    private void writePrint (int i){
        h.addStatisticsData(i, trader.getStatesCount());   // multiple payoffs count
        if (writeDiagnostics){
            trader.printDiagnostics();
            trader.resetDiagnostics();
            //trader.printConvergence(100);
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
