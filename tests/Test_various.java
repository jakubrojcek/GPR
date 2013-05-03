import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import org.apache.commons.math3.distribution.NormalDistribution;


import java.io.*;
import java.util.*;   import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Created by IntelliJ IDEA.
 * User: Jakub
 * Date: 26.6.12
 * Time: 20:26
 * To change this template use File | Settings | File Templates.
 */
public class Test_various {
    public static void main(String[] args) {

        HashMap<Short, Float[]> hm = new HashMap<Short, Float[]>();
        Random random = new Random();
        Float[] f = {0.0f, 1.0f};
        hm.put((short)2, f);
        hm.put((short)0, f);
        hm.put((short)5, f);
        hm.put((short)0, f);
        hm.put((short)19, f);
        int maxIndex;
        for (int i = 0; i < 100; i ++){
            List<Short> keys = new ArrayList<Short>(hm.keySet());
            maxIndex = keys.get(random.nextInt(keys.size()));        // TODO: make sure
            System.out.println(maxIndex);
        }



        short action1 = 15;
        short action2 = 15;
        short sh = (short)((action1<<7) + action2);
        action1 = (short)(sh>>7);
        action2 = (short)(sh - (action1<<7));
        System.out.println(action1 + " " + action2);

        IdentityHashMap<Short, Belief[]> x = new IdentityHashMap<Short, Belief[]>();
        Belief[] beliefs = new Belief[2];
        beliefs[0] = new Belief((short) 1, 0.5f, 0.5f);
        beliefs[1] = new Belief((short) 1, 0.6f, 0.6f);
        x.put((short)1, beliefs);
        short oldAction = (short) 1;
        System.out.println(beliefs[0].getMu());
        Belief[] b = x.get(oldAction);
        System.out.println((x.get(oldAction))[1].getMu());
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
        /*NormalDistribution nd = new NormalDistribution(0.0, 0.35);
        for (int i = 0; i < 1000; i++){
            System.out.println((float) nd.sample());
        }
        double sigma = 2.0;
        System.out.println(sigma == 2.0);*/



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
       /* HashMap<Integer, Trader> traders = new HashMap<Integer, Trader>(); //trader ID, trader object
        LinkedHashMap<Integer, Order_test>[] book = new LinkedHashMap[8];
        book[1] = new LinkedHashMap<Integer, Order_test>();
        book[2] = new LinkedHashMap<Integer, Order_test>();

        Trader tr1 = new Trader(false, 0.0f);   // buyer
        Trader tr2 = new Trader(false, 0.5f);   // buyer
        Trader tr3 = new Trader(false, -0.5f);  // seller

        traders.put(tr1.getTraderID(), tr1);
        traders.put(tr2.getTraderID(), tr2);

        // ID, time, buy, action, position
        Order_test o1 = new Order_test(tr1.getTraderID(), 0.0, true, (short) 7, 1);
        Order_test o2 = new Order_test(tr2.getTraderID(), 0.0, true, (short) 7, 1);
        Order_test o3 = new Order_test(tr1.getTraderID(), 0.0, true, (short) 8, 2);
        Order_test o4 = new Order_test(tr3.getTraderID(), 0.0, false, (short) 2, 1);
        Order_test o5 = new Order_test(tr3.getTraderID(), 0.0, false, (short) 2, 1); // Big SMO


        // book and currentPosition insertion
        Hashtable<Integer, HashMap<Integer, Integer>> CurrentPosition1 =
                new Hashtable<Integer, HashMap<Integer, Integer>>();
        book[1].put(1, o1);
        book[1].put(2, o2);
        book[2].put(3, o3);
        Integer tempSize1 = 0;
        HashMap<Integer, Integer> tempHM1 = new HashMap<Integer, Integer>();
        tempSize1 = o1.isBuyOrder() ? ++tempSize1 : --tempSize1;
        tempHM1.put(1, tempSize1);
        Integer tempSize2 = 0; tempSize2 = o3.isBuyOrder() ? ++tempSize2 : --tempSize2;
        tempHM1.put(2, tempSize2);
        CurrentPosition1.put(1, tempHM1);
        tempHM1 = new HashMap<Integer, Integer>(); tempSize2 = 0;
        tempSize2 = o2.isBuyOrder() ? ++tempSize2 : --tempSize2; tempHM1.put(1, tempSize2);
        CurrentPosition1.put(2, tempHM1);
        System.out.println("Order identification: " + (o1 == book[1].get(book[1].keySet().iterator().next())));

        // execution and remove from book and currentPosition
        Integer pos = 1;
        if (book[pos].size() > 0 && book[pos].get(book[pos].keySet().iterator().next()).isBuyOrder()){
            Order_test cp = book[pos].remove(book[1].keySet().iterator().next());
            Integer id = cp.getTraderID();
            System.out.println("Correct size at pos 1 for trader 1: " + (1 == CurrentPosition1.get(id).get(pos)));
            Integer tempInt = CurrentPosition1.get(id).get(pos);
            CurrentPosition1.get(id).put(pos, --tempInt);
            System.out.println("Executed order removed: " + (0 == CurrentPosition1.get(id).get(pos)));
        }




        // comparing orders of returning traders

        // random order for execution //TODO: will I need a list of Active orders here? No, over traders.keySet

        // Transaction Rule complete with 1 order

        // FVup/FVdown with 1 order


        // Transaction Rule in general

        // Transaction Rule, new trader
        book[1] = new LinkedHashMap<Integer, Order_test>();
        book[2] = new LinkedHashMap<Integer, Order_test>();
        Hashtable<Integer, HashMap<Integer, Integer>> CurrentPosition =
                new Hashtable<Integer, HashMap<Integer, Integer>>();

        Order_test[] orders = {o1, o3};

        Integer id = 1;                   // TODO: add trader ID to the transaction rule
        HashMap<Integer, Integer> tempHM = new HashMap<Integer, Integer>();
        Integer tempSize = 0;
        pos = null;

        for (Order_test o:orders){
            if (pos == null || pos != o.getPosition()){
                // put here tempSize to tempHM if not empty and position not null
                if (pos != null && tempSize != 0) {tempHM.put(pos, tempSize);}
                tempSize = 0;
                pos = o.getPosition();
            }
            if (book[pos].size() > 0 && !book[pos].get(book[pos].keySet().iterator().next()).isBuyOrder()){
                Order_test cp = book[pos].remove(book[1].keySet().iterator().next());
                id = cp.getTraderID();
                Integer tempSizeCP = CurrentPosition.get(id).get(pos);
                if (++tempSizeCP == 7){                               // TODO: check if this works
                    CurrentPosition.get(id).remove(pos);
                } else {
                    CurrentPosition.get(id).put(pos, tempSizeCP);
                }
                //System.out.println("Executed order removed: " + !CurrentPosition1.get(id).get(pos).contains(cp));
            } else if (pos == 0){

            } else {
                tempSize++;
                book[pos].put(o.getTraderID(),o);   // put some key number here
            }
        }
        if (tempSize != 0){tempHM.put(pos, tempSize);}
        if (!tempHM.isEmpty()){CurrentPosition.put(id, tempHM);}
        CurrentPosition.get(id).get(1);
        //System.out.println("Order 1 removed : " + !CurrentPosition.get(id).get(1).contains(o1));
        //System.out.println("Order 3 inside: " + CurrentPosition.get(id).get(pos).contains(o3));
        // expected outcome o1, o3 at pos = 1, CurrentPosition.put(trader), Traders.put(trader)

        // Transaction rule, execution and remove from book and currentPosition, as well as traders
        orders = new Order_test[1];
        orders[0] = o2;

        id = 2;                   // TODO: add trader ID to the transaction rule
        tempHM = new HashMap<Integer, Integer>();
        tempSize = 0;
        pos = null;

        for (Order_test o:orders){
            if (pos == null || pos != o.getPosition()){
                // put here tempSize to tempHM if not empty and position not null
                if (pos != null && tempSize != 0) {tempHM.put(pos, tempSize);}
                tempSize = 0;
                pos = o.getPosition();
            }
            if (book[pos].size() > 0 && !book[pos].get(book[pos].keySet().iterator().next()).isBuyOrder()){
                Order_test cp = book[pos].remove(book[1].keySet().iterator().next());
                id = cp.getTraderID();
                Integer tempSizeCP = CurrentPosition.get(id).get(pos);
                if (++tempSizeCP == 0){                               // TODO: check if this works
                    CurrentPosition.get(id).remove(pos);
                } else {
                    CurrentPosition.get(id).put(pos, tempSizeCP);
                }
                //System.out.println("Executed order removed: " + !CurrentPosition1.get(id).get(pos).contains(cp));
            } else if (pos == 7){

            } else {
                tempSize++;
                book[pos].put(o.getTraderID(),o);   // put some key number here
            }
        }
        if (tempSize != 0){tempHM.put(pos, tempSize);}
        if (!tempHM.isEmpty()){CurrentPosition.put(id, tempHM);}
        System.out.println("Order 2 inside: " + (CurrentPosition.get(id).get(pos) == 1));

        // the execution of a large market order
        orders = new Order_test[2];
        orders[0] = o4; orders[1] = o5;
        id = 3;
        tempHM = new HashMap<Integer, Integer>();
        tempSize = 0;
        pos = null;

        for (Order_test o:orders){
            if (pos == null || pos != o.getPosition()){
                // put here tempSize to tempHM if not empty and position not null
                if (pos != null && tempSize != 0) {tempHM.put(pos, tempSize);}
                tempSize = 0;
                pos = o.getPosition();
            }
            if (book[pos].size() > 0 && book[pos].get(book[pos].keySet().iterator().next()).isBuyOrder()){
                Order_test cp = book[pos].remove(book[pos].keySet().iterator().next());
                Integer CPid = cp.getTraderID();
                Integer tempSizeCP = CurrentPosition.get(CPid).get(pos);
                if (--tempSizeCP == 0){                               // TODO: check if this works
                    CurrentPosition.get(CPid).remove(pos);
                    if (CurrentPosition.get(CPid).isEmpty()){CurrentPosition.remove(CPid);}
                } else {
                    CurrentPosition.get(CPid).put(pos, tempSizeCP);
                }
                // TODO: history, trader.execution, Pt and b store
            } else if (pos == 0){      // if SMO executed against fringe, just continue

            }
            else {
                tempSize--;
                book[pos].put(o.getTraderID(),o);   // put some key number here
            }
        }
        if (tempSize != 0){tempHM.put(pos, tempSize);}
        if (!tempHM.isEmpty()){CurrentPosition.put(id, tempHM);}



        // TODO: remove LO counterparty trader if execution returns true (true if traded_shares == shares2trade)
        // TODO  remove LO MO current trader if traders.get(id).isTraded == true at the end of transactionRule, also delete his CurrentPosition entry (test: check if CP.get(trader).isEmpty)

        // Returning trader, has one buy order, wants to at the same pos
        Order_test o6 = new Order_test(tr1.getTraderID(), 0.0, true, (short) 8, 2);
        Order_test o7 = new Order_test(tr1.getTraderID(), 0.0, true, (short) 8, 2);
        ArrayList<Order_test> Orders = new ArrayList<Order_test>();
        Orders.add(o6); Orders.add(o7);

        Integer OrderID = 1;
        id = o6.getTraderID();                   // TODO: add trader ID to the transaction rule
        pos = null;
        ArrayList<Order_test> Orders2Remove = new ArrayList<Order_test>();
        if (CurrentPosition.containsKey(id)){                   // returning trader
            Integer sizeLeft = null;
            for (Order_test o : Orders){
                if (pos == null || pos != o.getPosition()){     // position changes here
                    pos = o.getPosition();
                    sizeLeft = null;
                }
                if (CurrentPosition.get(id).containsKey(pos)){  // TODO: pos - positionShift
                    boolean buy = false;
                    if (sizeLeft == null){                      // changed position
                        sizeLeft = CurrentPosition.get(id).get(pos);
                        buy = (sizeLeft > 0);                   // TODO: what if he wants b/s at the same position????
                    }
                    if (buy == o.isBuyOrder() || sizeLeft == 0){// sizeLeft == 0 bcz buy is false then
                        if (sizeLeft != 0){
                            sizeLeft = buy ? --sizeLeft : ++sizeLeft;
                            Orders2Remove.add(o);
                        }
                    } else {
                        CurrentPosition.get(id).remove(pos);    // TODO: you also need to remove orders at the position from the book
                        if (CurrentPosition.get(id).isEmpty()){
                            CurrentPosition.remove(id);
                        }
                    }
                }
                // not in CurrentPosition, don't touch orders
            }
            for (Order_test o : Orders2Remove){
                Orders.remove(o);
            }
        }
        // TODO: in "GPR2009" have active traders (in traders), in "GPR2005" have active orders (values at all book positions)

        if (CurrentPosition.containsKey(id)){
            tempHM = CurrentPosition.get(id);
        } else {
            tempHM = new HashMap<Integer, Integer>();
        }
        tempSize = 0;
        pos = null;

        for (Order_test o:Orders){
            if (pos == null || pos != o.getPosition()){
                // put here tempSize to tempHM if not empty and position not null
                if (pos != null && tempSize != 0) {tempHM.put(pos, tempSize);}
                pos = o.getPosition();
                tempSize = tempHM.containsKey(pos) ? tempHM.get(pos)        // TODO: pos - positionShift
                                                   : 0;
            }
            if (book[pos].size() > 0 && !book[pos].get(book[pos].keySet().iterator().next()).isBuyOrder()){
                Order_test cp = book[pos].remove(book[pos].keySet().iterator().next());
                Integer CPid = cp.getTraderID();
                Integer tempSizeCP = CurrentPosition.get(CPid).get(pos);
                if (++tempSizeCP == 0){                               // TODO: check if this works
                    CurrentPosition.get(CPid).remove(pos);
                    if (CurrentPosition.get(CPid).isEmpty()){CurrentPosition.remove(CPid);}
                } else {
                    CurrentPosition.get(CPid).put(pos, tempSizeCP);
                }
                // TODO: history, trader.execution, Pt and b store
            } else if (pos == 7){      // if SMO executed against fringe, just continue

            }
            else {
                tempSize++;
                OrderID++;
                book[pos].put(OrderID,o);   // put some key number here
            }
        }
        if (tempSize != 0){tempHM.put(pos, tempSize);}                 // TODO: + positionShift
        if (!tempHM.isEmpty()){CurrentPosition.put(id, tempHM);}
        // Returning trader, has 2 buy orders, wants one
        Order_test o8 = new Order_test(tr1.getTraderID(), 0.0, true, (short) 8, 2);
        Orders = new ArrayList<Order_test>();
        Orders.add(o8);

        id = o8.getTraderID(); // TODO: add trader ID to the transaction rule
        pos = null;
        Orders2Remove = new ArrayList<Order_test>();    // these orders are not necessary in transaction rule
        if (CurrentPosition.containsKey(id)){           // returning trader
            Integer sizeLeft = null;
            boolean buy = false;
            for (Order_test o : Orders){
                if (pos == null || pos != o.getPosition()){ // position changes here
                    if (sizeLeft != null && Math.abs(sizeLeft) > 0){ // deleting unnecessary current orders
                        CurrentPosition.get(id).put(pos, CurrentPosition.get(id).get(pos) - sizeLeft); // TODO: take this outside with number of orders to delete at each position, for opposite direction- the same = outside
                        int sz = Math.abs(sizeLeft);
                        for (int i = 0; i < sz; i++){
                            ListIterator<Integer> iter =
                                    new ArrayList(book[pos].keySet()).listIterator(book[pos].size());
                            while (iter.hasPrevious()) {
                                Integer key = iter.previous();
                                System.out.println(key);
                            }
                        }
                    }
                    pos = o.getPosition();
                    sizeLeft = null;
                }
                if (CurrentPosition.get(id).containsKey(pos)){ // TODO: pos - positionShift
                    buy = false;
                    if (sizeLeft == null){ // changed position
                        sizeLeft = CurrentPosition.get(id).get(pos);
                        buy = (sizeLeft > 0); // TODO: what if he wants b/s at the same position????
                    }
                    if (buy == o.isBuyOrder() || sizeLeft == 0){// sizeLeft == 0 bcz buy is false then
                        if (sizeLeft != 0){
                            sizeLeft = buy ? --sizeLeft : ++sizeLeft;
                            Orders2Remove.add(o);
                        }
                    } else {
                        CurrentPosition.get(id).remove(pos); // TODO: you also need to remove orders at the position from the book
                        if (CurrentPosition.get(id).isEmpty()){
                            CurrentPosition.remove(id);
                        }
                    }
                }
                // not in CurrentPosition, don't touch orders
            }
            for (Order_test o : Orders2Remove){
                Orders.remove(o);
            }
        }
        // TODO: in "GPR2009" have active traders (in traders), in "GPR2005" have active orders (values at all book positions)

        if (CurrentPosition.containsKey(id)){
            tempHM = CurrentPosition.get(id);
        } else {
            tempHM = new HashMap<Integer, Integer>();
        }
        tempSize = 0;
        pos = null;

        for (Order_test o:Orders){
            if (pos == null || pos != o.getPosition()){
                // put here tempSize to tempHM if not empty and position not null
                if (pos != null && tempSize != 0) {tempHM.put(pos, tempSize);}
                pos = o.getPosition();
                tempSize = tempHM.containsKey(pos) ? tempHM.get(pos)        // TODO: pos - positionShift
                        : 0;
            }
            if (book[pos].size() > 0 && !book[pos].get(book[pos].keySet().iterator().next()).isBuyOrder()){
                Order_test cp = book[pos].remove(book[pos].keySet().iterator().next());
                Integer CPid = cp.getTraderID();
                Integer tempSizeCP = CurrentPosition.get(CPid).get(pos);
                if (++tempSizeCP == 0){                               // TODO: check if this works
                    CurrentPosition.get(CPid).remove(pos);
                    if (CurrentPosition.get(CPid).isEmpty()){CurrentPosition.remove(CPid);}
                } else {
                    CurrentPosition.get(CPid).put(pos, tempSizeCP);
                }
                // TODO: history, trader.execution, Pt and b store
            } else if (pos == 7){      // if SMO executed against fringe, just continue

            }
            else {
                tempSize++;
                OrderID++;
                book[pos].put(OrderID,o);   // put some key number here
            }
        }
        if (tempSize != 0){tempHM.put(pos, tempSize);}                 // TODO: + positionShift
        if (!tempHM.isEmpty()){CurrentPosition.put(id, tempHM);}


        Iterator it = CurrentPosition.keySet().iterator();
        while (it.hasNext()){
            Integer s = (Integer) it.next();
            int sz = CurrentPosition.get(s).size();
            for (int i = 0; i < sz; i++){
                System.out.println(CurrentPosition.get(s));
            }
            System.out.println(sz);
        } */


    }

}
