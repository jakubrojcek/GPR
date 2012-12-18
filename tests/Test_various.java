import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;


import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jakub
 * Date: 26.6.12
 * Time: 20:26
 * To change this template use File | Settings | File Templates.
 */
public class Test_various {
    public static void main(String[] args) {
        /*float[] f = {4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f,
                4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f,
                4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f};*//*
        *//*float[] f = {5.267f, 5.267f, 5.267f, 5.267f, 5.267f, 5.267f, 5.267f, 5.267f, 5.267f, 5.267f,
                4.333f, 4.333f, 4.333f, 4.333f, 4.333f, 4.333f, 4.333f, 4.333f, 4.333f, 4.333f,
                4.333f, 4.333f, 4.333f, 4.333f, 4.333f, 4.333f, 4.333f, 4.333f, 4.333f};*//*
        //float[] f = {4.3f};
        //float f = 4.3f;
        //Float[] f = {4.3f, 4.3f};
        testPayoff[] x = new testPayoff[30000000];
        for (int i = 0; i < 30000000; i++){
            //Float f[] = {i*0.000001f};
            //Float f = i*0.000001f;
             Float [] f = {i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f,
                     i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f,
                     i*0.000002f, i*0.000002f, i*0.000002f, i*0.000002f, i*0.000002f, i*0.000002f, i*0.000002f, i*0.000002f, i*0.000002f};
            x[i] = new testPayoff(f);
        }*/

//        double timeStamp1 = System.currentTimeMillis();
//        int nHFT = 5;                       // # of HFT's fast traders, fixed
//        int nPositiveNonHFT = 10;           // # of positive PV slow traders
//        int nZeroNonHFT = 10;               // # of zero PV slow traders
//        int nNegativeNonHFT = 10;           // # of negative PV slow traders
//        double lambdaArrival = 0.1;             // arrival frequency, same for all
//        double ReturnFrequencyHFT = 1;       // returning frequency of HFT
//        double ReturnFrequencyNonHFT = 0.1;     // returning frequency of NonHFT
//        double[] FprivateValues = {-4, 0, 4}; // distribution over private values
//        double FV = 31;                       // Fundamental value
//        double sigma = 1.0;                   // volatility of FV
//        double tickSize = 1.0;                // size of one tick
//        double FVplus = 0.5;                  // probability f FV going up
//
//        double[] tauB = new double[13]; /* expected time until the arrival of a new buyer for whom trading on
//        the LO yields non-negative payoff */
//        double[] tauS = new double[13]; /* expected time until the arrival of a new seller for whom picking up
//        the LO yields non-negative payoff */
//        for (int i = 0; i < 13; i++){ //
//            int denomB = 0; // denominator buyers
//            int denomS = 0; // denominator sellers
//            // buyers
//            if(i < 7 + FprivateValues[0]){  // add negative value traders
//                denomB += nNegativeNonHFT;
//            }
//            if (i < 7 + FprivateValues[1]){ // add zero value traders
//                denomB += nZeroNonHFT;
//                denomB += nHFT;
//            }
//            if(i < 7 + FprivateValues[2]){  // add positive value traders
//                denomB += nPositiveNonHFT;
//            }
//            // sellers
//            if(i > 7 + FprivateValues[0] - 2){
//                denomS += nNegativeNonHFT;
//            }
//            if(i > 7 + FprivateValues[1] - 2){
//                denomS += nZeroNonHFT;
//                denomS += nHFT;
//            }
//            if(i > 7 + FprivateValues[2] - 2){
//                denomS += nPositiveNonHFT;
//            }
//            // computing tauB and tauS
//            if (denomB == 0){// no buyers interested
//                tauB[i] = 1000000000;
//            } else {
//                tauB[i] = 1 / (denomB * lambdaArrival);
//            }
//            if (denomS == 0){// no sellers interested
//                tauS[i] = 1000000000;
//            } else {
//                tauS[i] = 1 / (denomS * lambdaArrival);
//            }
//        }
//
        //HashMap<Integer, Double> traders = new HashMap<Integer, Double>();
     /*   IdentityHashMap<Integer, Double> traders2 = new IdentityHashMap<Integer, Double>();
        for (int j = 0; j < 1000000000; j++){
            //traders.put(j, Math.sqrt(j));
            traders2.put(j, Math.sqrt(j));
        }*/


//
//        int nP = 63; // number of prices tracked by the book
//        double [] Prices = new double[nP]; // creates vector of the prices, not carrying about ticks now
//        for (int i = 0 ; i < nP ; i++){
//            Prices[i] = i;
//        }
//        History h = new History(); // create history
//        // create map of traders
//        HashMap<Integer, Trader> traders = new HashMap<Integer, Trader>();
//
//        new Trader(tauB, tauS, nP, tickSize, ReturnFrequencyHFT, ReturnFrequencyNonHFT, Prices);
//        LOB_LinkedHashMap book = new LOB_LinkedHashMap(FV, tickSize, nP ,h, traders);
//        // create book
//        book.makeBook(Prices);
//
//        double EventTime = 0.0;
//
//        System.out.println("New arrival HFT");
//        Trader tr = new Trader(true, 0);
//        int ID = tr.getTraderID();
//        traders.put(ID, tr);
//        PriceOrder PO = tr.decision(book.getRank(ID), book.getBookSizes(), book.getBookInfo(), EventTime);
//        if (PO != null){
//            book.transactionRule(PO.getPrice() , PO.getCurrentOrder());
//        } else {book.addTrader(ID);}
//
//
//        book.FVup(32);
//        System.out.println("Change in the fundamental value to " + FV);
//
//        System.out.println("New arrival nonHFT");
//        tr = new Trader(false, 4);
//        ID = tr.getTraderID();
//        traders.put(ID, tr);
//        PO = tr.decision(book.getRank(ID), book.getBookSizes(), book.getBookInfo(), EventTime);
//        if (PO != null){
//            book.transactionRule(PO.getPrice() , PO.getCurrentOrder());
//        } else {book.addTrader(ID);}
//
//        book.FVup(33);
//        System.out.println("Change in the fundamental value to " + FV);
//
//        ID = book.randomNonHFTtraderID();
//        System.out.println("Returning nonHFT ID: " + ID);
//        PO = traders.get(ID).decision(book.getRank(ID), book.getBookSizes(), book.getBookInfo(), EventTime);
//        if (PO != null){
//            book.transactionRule(PO.getPrice() , PO.getCurrentOrder());
//        }
//
//        System.out.println("Seller Market Order new NonHFT arrival");
//        tr = new Trader(false, -4);
//        ID = tr.getTraderID();
//        traders.put(ID, tr);
//        PO = new PriceOrder(25 , new Order(3, 1.0, false));
//        book.transactionRule(PO.getPrice(), PO.getCurrentOrder());
//
//        book.FVup(34);
//        System.out.println("Change in the fundamental value to " + FV);
//        ID = 2;
//        System.out.println("Returning nonHFT ID: " + ID);
//        PO = traders.get(ID).decision(book.getRank(ID), book.getBookSizes(), book.getBookInfo(), EventTime);
//        if (PO != null){
//            book.transactionRule(PO.getPrice() , PO.getCurrentOrder());
//        }
//
//
//        double timeStamp2 = System.currentTimeMillis();
//        book.printBook();
//        System.out.println("running time = " + (timeStamp2 - timeStamp1));

        /*long A;
        A = -4/3;
        System.out.println(A);*/

        ArrayList<Byte> IDs = new ArrayList<Byte>(1);
        Byte a = 5;
        Byte b = 3;
        IDs.add(a);
        IDs.add(b);
        System.out.println(IDs.indexOf(a));


/*        FileWriter fw;
        try{
            String outputFileName =  "D:\\_paper1 HFT, MM, rebates and market quality\\Matlab Analysis\\blabla.csv";
            fw = new FileWriter(outputFileName);
            for (int j = 0; j < 3; j++){
                float [] p = {1.5f, 2.5f, 3.5f};
                short[] n = {(short)j, (short) (2*j), (short) (3*j)};
                int sz = n.length;
                String s = new String();
                for (int i = 0; i < sz; i++){
                    s = s + n[i] + ";"  + p[i] + ";";
                }
                fw.writeDecisions(0 + "\r");
                fw.writeDecisions(s + "\r");
            }
            fw.writeDecisions(1 + ";" + 1.5f + ";" + 1 + ";" + 1.5f + ";" + 1 + ";" + 1.5f + ";" +
                    1 + ";" + 1.5f + ";" + "\r");
            fw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }*/

        /*
        // order 1
        Order o = new Order(0, System.currentTimeMillis(), true);
        book.transactionRule(1, o, tb);
        book.decreasenBuyNew();
        book.printBook(tb);
        // order 2
        Order o2 = new Order(3, System.currentTimeMillis(), true);
        book.transactionRule(1, o2, tb);
        book.decreasenBuyNew();
        book.printBook(tb);
        // order 3
        Order o3 = new Order(12, System.currentTimeMillis(), false);
        book.transactionRule(1, o3, tb);
        book.decreasenSellNew();
        book.printBook(tb);
        // order 4
        Order o4 = new Order(13, System.currentTimeMillis(), false);
        book.transactionRule(5, o4, tb);
        book.decreasenSellNew();
        // print the book
        book.printBook(tb);
        System.out.println(book.randomNonHFTtraderID()+ " random guy");
        */

    }
}
