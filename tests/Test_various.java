import com.jakubrojcek.Belief;
import com.jakubrojcek.gpr2005a.Payoff;
import com.jakubrojcek.gpr2005a.GPR2005Payoff_test3;
import com.jakubrojcek.gpr2005a.Trader;
import com.jakubrojcek.Choices;


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
        int [] BS = {0,1,1,0,0,0,-3,0};
        int [] BsNew = new int[8];
        System.arraycopy(BS, 0, BsNew, 0, 8);
        for (int i = 1; i < (8 - 1); i++){
            BsNew[i] = -BS[8 - 1 - i];
        }


        /*HashMap<Long, Payoff> Payoffs = new HashMap<Long, Payoff>();
        GPR2005Payoff_test3 pay1 = new GPR2005Payoff_test3();
        Payoffs.put((long) 1, pay1);
        pay1.updateMax((short) 1, (short) 2, 0.5f, -1.0f);
        GPR2005Payoff_test3 pay2temp;
        pay2temp = (GPR2005Payoff_test3)Payoffs.get((long)1);
        System.out.println(pay2temp.getX().size() + ": should be 1");
        pay2temp = new GPR2005Payoff_test3();
        System.out.println("should be 1 as well: " +
                ((GPR2005Payoff_test3) Payoffs.get((long)1)).getX().size()
                + " and this one should be 0: " + pay2temp.getX().size());*/
        /*HashMap<String, ArrayList<Boolean>> TestResults = new HashMap<String,ArrayList<Boolean>>();
        ArrayList<Boolean> FirstShare = new ArrayList<Boolean>();
        ArrayList<Boolean> SecondShare = new ArrayList<Boolean>();
        ArrayList<Boolean> Max = new ArrayList<Boolean>();

        int [] BookSizes = {0,1,1,0,0,0,-3,0};
        int [] BookInfo = {2, 6, 1, 3, 0, 0, 0, 0};
        float privateValue = -0.0625f;
        float pvGPR = 2.5f;
        boolean u2t = true;
        int units2trade = u2t ? 2 : 1;
        float [] mu = new float[15];
        float [] mu2 = new float[15];
        float [] dV = new float[15];
        float [] dV2 = new float[15];

        HashMap<Integer, Integer> Actions = new HashMap<Integer, Integer>();
        HashMap<Integer, ArrayList<Integer>> ActionsMy;
        HashMap<Integer, ArrayList<Integer>> ActionsGPR;
        // actions translations
        Actions.put(-6, 5);  Actions.put(-5, 4);  Actions.put(-4, 3);  Actions.put(-3, 2);
        Actions.put(-2, 1);  Actions.put(-1, 0);  Actions.put(0, 14);  Actions.put(1, 6);
        Actions.put(2, 7);  Actions.put(3, 8);  Actions.put(4, 9);  Actions.put(5, 10);
        Actions.put(6, 11);
        //Actions.put(-BookInfo[0], 12); Actions.put(BookInfo[1],13);

        // loading execution probabilities and deltas
        HashMap<Integer, Double> muGPR = new HashMap<Integer, Double>();
        HashMap<Integer, Double> mu2GPR = new HashMap<Integer, Double>();
        HashMap<Integer, Double> dVGPR = new HashMap<Integer, Double>();
        HashMap<Integer, Double> dV2GPR = new HashMap<Integer, Double>();
        for(int i = 0; i < 15; i++){
            mu[i] = (float) Math.random();
            dV[i] = (float) Math.random();
            if (i < 6){
                dV[i] *= -1;
                //dV2[i] *= -1;
            }
            mu2[i] = mu[i];
            dV2[i] = dV[i];
        }
        //mu[12] = 1.0f; mu[13] = 1.0f; mu[14] = 1.0f;
        for (int i = -6; i < 7; i++){
            muGPR.put(i, 0.0);
            mu2GPR.put(i, 0.0);
            dVGPR.put(i, 0.0);
            dV2GPR.put(i, 0.0);
        }
        Iterator it = Actions.keySet().iterator();
        Integer actionGPR;
        while (it.hasNext()){
            actionGPR = (Integer) it.next();
            muGPR.put(actionGPR, (double) mu[Actions.get(actionGPR)]);
            dVGPR.put(actionGPR, (double) (dV[Actions.get(actionGPR)] * 8));
            mu2GPR.put(actionGPR, (double) mu[Actions.get(actionGPR)]);
            dV2GPR.put(actionGPR, (double) (dV[Actions.get(actionGPR)] * 8));
        }

        // Running choices and storing the Maximizing Actions
        Choices ch = new Choices();
        short [] MaxAction, MaxActionGPR;
        // Computing best action of our implementation
        MaxAction = ch.decision(BookSizes, BookInfo, mu, mu2, dV, dV2, privateValue, units2trade);
        System.out.println();
        MaxActionGPR = ch.GPRdecision(pvGPR, u2t, BookSizes, BookInfo, muGPR, dVGPR, mu2GPR, dV2GPR);
        System.out.println();

        // running tests

        ActionsMy = ch.getActionsMy();
        ActionsGPR = ch.getActionsGPR();
        Integer actionGPR2;
        ArrayList<Integer> ActionsJ;
        ArrayList<Integer> ActionsGPR2Delete = new ArrayList<Integer>();
        // action set test
        *//*Iterator it2 = ActionsGPR.keySet().iterator();
        while (it2.hasNext()){
            actionGPR = (Integer) it2.next();
            if (ActionsMy.containsKey(Actions.get(actionGPR))){
                ActionsMy.remove(Actions.get(actionGPR));
                ActionsGPR2Delete.add(actionGPR);
            }
        }
        for (Integer ac : ActionsGPR2Delete){
            ActionsGPR.remove(ac);
        }*//*
        //FirstShare.add(ActionsMy.isEmpty() && ActionsGPR.isEmpty());
        // max test
        Max.add((ch.getMaxMy() * 8 < ch.getMaxGPR() + 0.001) && (ch.getMaxMy() * 8 > ch.getMaxGPR() - 0.001));
        // Max actions test
        actionGPR = (int) MaxActionGPR[0];
        FirstShare.add(MaxAction[0] ==  Actions.get(actionGPR));

        // testing Max action for second share
        actionGPR = (int) MaxActionGPR[1];
        SecondShare.add(MaxAction[1] == Actions.get(actionGPR));
        for (int i = 0; i < units2trade; i++){
            System.out.println(i + "'s action is " + MaxAction[i] + " my max: " + ch.getMaxMy());
            System.out.println(i + "'s GPR action is " + MaxActionGPR[i] + " GPR max: " + ch.getMaxGPR());
        }

        TestResults.put("Max", Max);
        TestResults.put("FirstShare", FirstShare);
        TestResults.put("SecondShare", SecondShare);

        Iterator it3 = TestResults.keySet().iterator();
        while (it3.hasNext()){
            String s = (String) it3.next();
            int sz = TestResults.get(s).size();
            for (int i = 0; i < sz; i ++){
                System.out.println(s + " " + TestResults.get(s).get(i).toString());
            }
        }


        *//*double timeStamp1 = System.nanoTime();
        for (int i = 0; i < 1000000; i++ ){
            ch.decision(BookSizes, BookInfo, mu, mu2, dV, dV2, privateValue, units2trade);
        }
        double timeStamp2 = System.nanoTime();
        System.out.println("time to run with my decision =  " + (timeStamp2 - timeStamp1));

        for (int i = 0; i < 1000000; i++ ){
            ch.GPRdecision(pvGPR, u2t, BookSizes, BookInfo, muGPR, dVGPR, mu2GPR, dV2GPR);
        }
        double timeStamp3 = System.nanoTime();
        System.out.println("time to run with GPR decision =  " + (timeStamp3 - timeStamp2));*//*

        ch.printActions();*/
        // Computing best action of GPR2005 implementation

        /*Hashtable<Long, Payoff> payoffs = new Hashtable<Long, Payoff>();
        long rn;
        for (int i = 0; i < 10000000; i++){
            GPR2005Payoff_test3 pay = new GPR2005Payoff_test3();
            rn = (long) Math.random() * 10000000;
            payoffs.put((long) rn, pay);
        }

        for (int i = 1; i < 10000000; i++){
            rn = (long) Math.random() * 10000000;
            System.out.println(payoffs.containsKey(rn));
        }


        System.out.println((5<<7));
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
            maxIndex = keys.get(random.nextInt(keys.size()));
            System.out.println(maxIndex);
        }



        short action1 = (0<<4) + 0;
        short action2 = (8<<4) + 0;
        short[] q = new short[2];
        short sh = (short)((action1<<7) + action2);
        action1 = (short)((sh>>7));
        q[0] = (short) (action1>>4);
        action2 = (short)(sh - (action1<<7));
        q[1] = (short) (action2>>4);
        action1 = (short) (action1 - (q[0]<<4));
        action2 = (short) (action2 - (q[1]<<4));
        System.out.println(action1 + " " + action2);

        IdentityHashMap<Short, Belief[]> x = new IdentityHashMap<Short, Belief[]>();
        Belief[] beliefs = new Belief[2];
        beliefs[0] = new Belief((short) 1, 0.5f, 0.5f);
        beliefs[1] = new Belief((short) 1, 0.6f, 0.6f);
        x.put((short)1, beliefs);
        short oldAction = (short) 1;
        System.out.println(beliefs[0].getMu());
        Belief[] b = x.get(oldAction);
        System.out.println((x.get(oldAction))[1].getMu());*/

        /*float[] f = {4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f,
                4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f,
                4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f};*//*
        *//*float[] f = {5.267f, 5.267f, 5.267f, 5.267f, 5.267f, 5.267f, 5.267f, 5.267f, 5.267f, 5.267f,
                4.333f, 4.333f, 4.333f, 4.333f, 4.333f, 4.333f, 4.333f, 4.333f, 4.333f, 4.333f,
                4.333f, 4.333f, 4.333f, 4.333f, 4.333f, 4.333f, 4.333f, 4.333f, 4.333f};*//*
        //float[] f = {4.3f};
        //float f = 4.3f;
        //Float[] f = {4.3f, 4.3f};
        com.jakubrojcek.gpr2005a.testPayoff[] x = new com.jakubrojcek.gpr2005a.testPayoff[30000000];
        for (int i = 0; i < 30000000; i++){
            //Float f[] = {i*0.000001f};
            //Float f = i*0.000001f;
             Float [] f = {i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f,
                     i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f, i*0.000001f,
                     i*0.000002f, i*0.000002f, i*0.000002f, i*0.000002f, i*0.000002f, i*0.000002f, i*0.000002f, i*0.000002f, i*0.000002f};
            x[i] = new com.jakubrojcek.gpr2005a.testPayoff(f);
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
//        com.jakubrojcek.gpr2005a.History h = new com.jakubrojcek.gpr2005a.History(); // create history
//        // create map of traders
//        HashMap<Integer, com.jakubrojcek.gpr2005a.Trader> traders = new HashMap<Integer, com.jakubrojcek.gpr2005a.Trader>();
//
//        new com.jakubrojcek.gpr2005a.Trader(tauB, tauS, nP, tickSize, ReturnFrequencyHFT, ReturnFrequencyNonHFT, Prices);
//        com.jakubrojcek.gpr2005a.LOB_LinkedHashMap book = new com.jakubrojcek.gpr2005a.LOB_LinkedHashMap(FV, tickSize, nP ,h, traders);
//        // create book
//        book.makeBook(Prices);
//
//        double EventTime = 0.0;
//
//        System.out.println("New arrival HFT");
//        com.jakubrojcek.gpr2005a.Trader tr = new com.jakubrojcek.gpr2005a.Trader(true, 0);
//        int ID = tr.getTraderID();
//        traders.put(ID, tr);
//        com.jakubrojcek.PriceOrder PO = tr.decision(book.getRank(ID), book.getBookSizes(), book.getBookInfo(), EventTime);
//        if (PO != null){
//            book.transactionRule(PO.getPrice() , PO.getCurrentOrder());
//        } else {book.addTrader(ID);}
//
//
//        book.FVup(32);
//        System.out.println("Change in the fundamental value to " + FV);
//
//        System.out.println("New arrival nonHFT");
//        tr = new com.jakubrojcek.gpr2005a.Trader(false, 4);
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
//        System.out.println("Seller Market com.jakubrojcek.Order new NonHFT arrival");
//        tr = new com.jakubrojcek.gpr2005a.Trader(false, -4);
//        ID = tr.getTraderID();
//        traders.put(ID, tr);
//        PO = new com.jakubrojcek.PriceOrder(25 , new com.jakubrojcek.Order(3, 1.0, false));
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
        com.jakubrojcek.Order o = new com.jakubrojcek.Order(0, System.currentTimeMillis(), true);
        book.transactionRule(1, o, tb);
        book.decreasenBuyNew();
        book.printBook(tb);
        // order 2
        com.jakubrojcek.Order o2 = new com.jakubrojcek.Order(3, System.currentTimeMillis(), true);
        book.transactionRule(1, o2, tb);
        book.decreasenBuyNew();
        book.printBook(tb);
        // order 3
        com.jakubrojcek.Order o3 = new com.jakubrojcek.Order(12, System.currentTimeMillis(), false);
        book.transactionRule(1, o3, tb);
        book.decreasenSellNew();
        book.printBook(tb);
        // order 4
        com.jakubrojcek.Order o4 = new com.jakubrojcek.Order(13, System.currentTimeMillis(), false);
        book.transactionRule(5, o4, tb);
        book.decreasenSellNew();
        // print the book
        book.printBook(tb);
        System.out.println(book.randomNonHFTtraderID()+ " random guy");
        */
       /* HashMap<Integer, com.jakubrojcek.gpr2005a.Trader> traders = new HashMap<Integer, com.jakubrojcek.gpr2005a.Trader>(); //trader ID, trader object
        LinkedHashMap<Integer, com.jakubrojcek.Order_test>[] book = new LinkedHashMap[8];
        book[1] = new LinkedHashMap<Integer, com.jakubrojcek.Order_test>();
        book[2] = new LinkedHashMap<Integer, com.jakubrojcek.Order_test>();

        com.jakubrojcek.gpr2005a.Trader tr1 = new com.jakubrojcek.gpr2005a.Trader(false, 0.0f);   // buyer
        com.jakubrojcek.gpr2005a.Trader tr2 = new com.jakubrojcek.gpr2005a.Trader(false, 0.5f);   // buyer
        com.jakubrojcek.gpr2005a.Trader tr3 = new com.jakubrojcek.gpr2005a.Trader(false, -0.5f);  // seller

        traders.put(tr1.getTraderID(), tr1);
        traders.put(tr2.getTraderID(), tr2);

        // ID, time, buy, action, position
        com.jakubrojcek.Order_test o1 = new com.jakubrojcek.Order_test(tr1.getTraderID(), 0.0, true, (short) 7, 1);
        com.jakubrojcek.Order_test o2 = new com.jakubrojcek.Order_test(tr2.getTraderID(), 0.0, true, (short) 7, 1);
        com.jakubrojcek.Order_test o3 = new com.jakubrojcek.Order_test(tr1.getTraderID(), 0.0, true, (short) 8, 2);
        com.jakubrojcek.Order_test o4 = new com.jakubrojcek.Order_test(tr3.getTraderID(), 0.0, false, (short) 2, 1);
        com.jakubrojcek.Order_test o5 = new com.jakubrojcek.Order_test(tr3.getTraderID(), 0.0, false, (short) 2, 1); // Big SMO


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
        System.out.println("com.jakubrojcek.Order identification: " + (o1 == book[1].get(book[1].keySet().iterator().next())));

        // execution and remove from book and currentPosition
        Integer pos = 1;
        if (book[pos].size() > 0 && book[pos].get(book[pos].keySet().iterator().next()).isBuyOrder()){
            com.jakubrojcek.Order_test cp = book[pos].remove(book[1].keySet().iterator().next());
            Integer id = cp.getTraderID();
            System.out.println("Correct size at pos 1 for trader 1: " + (1 == CurrentPosition1.get(id).get(pos)));
            Integer tempInt = CurrentPosition1.get(id).get(pos);
            CurrentPosition1.get(id).put(pos, --tempInt);
            System.out.println("Executed order removed: " + (0 == CurrentPosition1.get(id).get(pos)));
        }




        // comparing orders of returning traders

        // random order for execution

        // Transaction Rule complete with 1 order

        // FVup/FVdown with 1 order


        // Transaction Rule in general

        // Transaction Rule, new trader
        book[1] = new LinkedHashMap<Integer, com.jakubrojcek.Order_test>();
        book[2] = new LinkedHashMap<Integer, com.jakubrojcek.Order_test>();
        Hashtable<Integer, HashMap<Integer, Integer>> CurrentPosition =
                new Hashtable<Integer, HashMap<Integer, Integer>>();

        com.jakubrojcek.Order_test[] orders = {o1, o3};

        Integer id = 1;
        HashMap<Integer, Integer> tempHM = new HashMap<Integer, Integer>();
        Integer tempSize = 0;
        pos = null;

        for (com.jakubrojcek.Order_test o:orders){
            if (pos == null || pos != o.getPosition()){
                // put here tempSize to tempHM if not empty and position not null
                if (pos != null && tempSize != 0) {tempHM.put(pos, tempSize);}
                tempSize = 0;
                pos = o.getPosition();
            }
            if (book[pos].size() > 0 && !book[pos].get(book[pos].keySet().iterator().next()).isBuyOrder()){
                com.jakubrojcek.Order_test cp = book[pos].remove(book[1].keySet().iterator().next());
                id = cp.getTraderID();
                Integer tempSizeCP = CurrentPosition.get(id).get(pos);
                if (++tempSizeCP == 7){
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
        //System.out.println("com.jakubrojcek.Order 1 removed : " + !CurrentPosition.get(id).get(1).contains(o1));
        //System.out.println("com.jakubrojcek.Order 3 inside: " + CurrentPosition.get(id).get(pos).contains(o3));
        // expected outcome o1, o3 at pos = 1, CurrentPosition.put(trader), Traders.put(trader)

        // Transaction rule, execution and remove from book and currentPosition, as well as traders
        orders = new com.jakubrojcek.Order_test[1];
        orders[0] = o2;

        id = 2;
        tempHM = new HashMap<Integer, Integer>();
        tempSize = 0;
        pos = null;

        for (com.jakubrojcek.Order_test o:orders){
            if (pos == null || pos != o.getPosition()){
                // put here tempSize to tempHM if not empty and position not null
                if (pos != null && tempSize != 0) {tempHM.put(pos, tempSize);}
                tempSize = 0;
                pos = o.getPosition();
            }
            if (book[pos].size() > 0 && !book[pos].get(book[pos].keySet().iterator().next()).isBuyOrder()){
                com.jakubrojcek.Order_test cp = book[pos].remove(book[1].keySet().iterator().next());
                id = cp.getTraderID();
                Integer tempSizeCP = CurrentPosition.get(id).get(pos);
                if (++tempSizeCP == 0){
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
        System.out.println("com.jakubrojcek.Order 2 inside: " + (CurrentPosition.get(id).get(pos) == 1));

        // the execution of a large market order
        orders = new com.jakubrojcek.Order_test[2];
        orders[0] = o4; orders[1] = o5;
        id = 3;
        tempHM = new HashMap<Integer, Integer>();
        tempSize = 0;
        pos = null;

        for (com.jakubrojcek.Order_test o:orders){
            if (pos == null || pos != o.getPosition()){
                // put here tempSize to tempHM if not empty and position not null
                if (pos != null && tempSize != 0) {tempHM.put(pos, tempSize);}
                tempSize = 0;
                pos = o.getPosition();
            }
            if (book[pos].size() > 0 && book[pos].get(book[pos].keySet().iterator().next()).isBuyOrder()){
                com.jakubrojcek.Order_test cp = book[pos].remove(book[pos].keySet().iterator().next());
                Integer CPid = cp.getTraderID();
                Integer tempSizeCP = CurrentPosition.get(CPid).get(pos);
                if (--tempSizeCP == 0){
                    CurrentPosition.get(CPid).remove(pos);
                    if (CurrentPosition.get(CPid).isEmpty()){CurrentPosition.remove(CPid);}
                } else {
                    CurrentPosition.get(CPid).put(pos, tempSizeCP);
                }
            } else if (pos == 0){      // if SMO executed against fringe, just continue

            }
            else {
                tempSize--;
                book[pos].put(o.getTraderID(),o);   // put some key number here
            }
        }
        if (tempSize != 0){tempHM.put(pos, tempSize);}
        if (!tempHM.isEmpty()){CurrentPosition.put(id, tempHM);}




        // Returning trader, has one buy order, wants to at the same pos
        com.jakubrojcek.Order_test o6 = new com.jakubrojcek.Order_test(tr1.getTraderID(), 0.0, true, (short) 8, 2);
        com.jakubrojcek.Order_test o7 = new com.jakubrojcek.Order_test(tr1.getTraderID(), 0.0, true, (short) 8, 2);
        ArrayList<com.jakubrojcek.Order_test> Orders = new ArrayList<com.jakubrojcek.Order_test>();
        Orders.add(o6); Orders.add(o7);

        Integer OrderID = 1;
        id = o6.getTraderID();
        pos = null;
        ArrayList<com.jakubrojcek.Order_test> Orders2Remove = new ArrayList<com.jakubrojcek.Order_test>();
        if (CurrentPosition.containsKey(id)){                   // returning trader
            Integer sizeLeft = null;
            for (com.jakubrojcek.Order_test o : Orders){
                if (pos == null || pos != o.getPosition()){     // position changes here
                    pos = o.getPosition();
                    sizeLeft = null;
                }
                if (CurrentPosition.get(id).containsKey(pos)){
                    boolean buy = false;
                    if (sizeLeft == null){                      // changed position
                        sizeLeft = CurrentPosition.get(id).get(pos);
                        buy = (sizeLeft > 0);
                    }
                    if (buy == o.isBuyOrder() || sizeLeft == 0){// sizeLeft == 0 bcz buy is false then
                        if (sizeLeft != 0){
                            sizeLeft = buy ? --sizeLeft : ++sizeLeft;
                            Orders2Remove.add(o);
                        }
                    } else {
                        CurrentPosition.get(id).remove(pos);
                        if (CurrentPosition.get(id).isEmpty()){
                            CurrentPosition.remove(id);
                        }
                    }
                }
                // not in CurrentPosition, don't touch orders
            }
            for (com.jakubrojcek.Order_test o : Orders2Remove){
                Orders.remove(o);
            }
        }

        if (CurrentPosition.containsKey(id)){
            tempHM = CurrentPosition.get(id);
        } else {
            tempHM = new HashMap<Integer, Integer>();
        }
        tempSize = 0;
        pos = null;

        for (com.jakubrojcek.Order_test o:Orders){
            if (pos == null || pos != o.getPosition()){
                // put here tempSize to tempHM if not empty and position not null
                if (pos != null && tempSize != 0) {tempHM.put(pos, tempSize);}
                pos = o.getPosition();
                tempSize = tempHM.containsKey(pos) ? tempHM.get(pos)
                                                   : 0;
            }
            if (book[pos].size() > 0 && !book[pos].get(book[pos].keySet().iterator().next()).isBuyOrder()){
                com.jakubrojcek.Order_test cp = book[pos].remove(book[pos].keySet().iterator().next());
                Integer CPid = cp.getTraderID();
                Integer tempSizeCP = CurrentPosition.get(CPid).get(pos);
                if (++tempSizeCP == 0){
                    CurrentPosition.get(CPid).remove(pos);
                    if (CurrentPosition.get(CPid).isEmpty()){CurrentPosition.remove(CPid);}
                } else {
                    CurrentPosition.get(CPid).put(pos, tempSizeCP);
                }

            } else if (pos == 7){      // if SMO executed against fringe, just continue

            }
            else {
                tempSize++;
                OrderID++;
                book[pos].put(OrderID,o);   // put some key number here
            }
        }
        if (tempSize != 0){tempHM.put(pos, tempSize);}
        if (!tempHM.isEmpty()){CurrentPosition.put(id, tempHM);}
        // Returning trader, has 2 buy orders, wants one
        com.jakubrojcek.Order_test o8 = new com.jakubrojcek.Order_test(tr1.getTraderID(), 0.0, true, (short) 8, 2);
        Orders = new ArrayList<com.jakubrojcek.Order_test>();
        Orders.add(o8);

        id = o8.getTraderID();
        pos = null;
        Orders2Remove = new ArrayList<com.jakubrojcek.Order_test>();    // these orders are not necessary in transaction rule
        if (CurrentPosition.containsKey(id)){           // returning trader
            Integer sizeLeft = null;
            boolean buy = false;
            for (com.jakubrojcek.Order_test o : Orders){
                if (pos == null || pos != o.getPosition()){ // position changes here
                    if (sizeLeft != null && Math.abs(sizeLeft) > 0){ // deleting unnecessary current orders
                        CurrentPosition.get(id).put(pos, CurrentPosition.get(id).get(pos) - sizeLeft);
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
                if (CurrentPosition.get(id).containsKey(pos)){ /
                    buy = false;
                    if (sizeLeft == null){ // changed position
                        sizeLeft = CurrentPosition.get(id).get(pos);
                        buy = (sizeLeft > 0);
                    }
                    if (buy == o.isBuyOrder() || sizeLeft == 0){// sizeLeft == 0 bcz buy is false then
                        if (sizeLeft != 0){
                            sizeLeft = buy ? --sizeLeft : ++sizeLeft;
                            Orders2Remove.add(o);
                        }
                    } else {
                        CurrentPosition.get(id).remove(pos);
                        if (CurrentPosition.get(id).isEmpty()){
                            CurrentPosition.remove(id);
                        }
                    }
                }
                // not in CurrentPosition, don't touch orders
            }
            for (com.jakubrojcek.Order_test o : Orders2Remove){
                Orders.remove(o);
            }
        }

        if (CurrentPosition.containsKey(id)){
            tempHM = CurrentPosition.get(id);
        } else {
            tempHM = new HashMap<Integer, Integer>();
        }
        tempSize = 0;
        pos = null;

        for (com.jakubrojcek.Order_test o:Orders){
            if (pos == null || pos != o.getPosition()){
                // put here tempSize to tempHM if not empty and position not null
                if (pos != null && tempSize != 0) {tempHM.put(pos, tempSize);}
                pos = o.getPosition();
                tempSize = tempHM.containsKey(pos) ? tempHM.get(pos)
                        : 0;
            }
            if (book[pos].size() > 0 && !book[pos].get(book[pos].keySet().iterator().next()).isBuyOrder()){
                com.jakubrojcek.Order_test cp = book[pos].remove(book[pos].keySet().iterator().next());
                Integer CPid = cp.getTraderID();
                Integer tempSizeCP = CurrentPosition.get(CPid).get(pos);
                if (++tempSizeCP == 0){
                    CurrentPosition.get(CPid).remove(pos);
                    if (CurrentPosition.get(CPid).isEmpty()){CurrentPosition.remove(CPid);}
                } else {
                    CurrentPosition.get(CPid).put(pos, tempSizeCP);
                }

            } else if (pos == 7){      // if SMO executed against fringe, just continue

            }
            else {
                tempSize++;
                OrderID++;
                book[pos].put(OrderID,o);   // put some key number here
            }
        }
        if (tempSize != 0){tempHM.put(pos, tempSize);}
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
