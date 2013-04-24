import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Jakub
 * Date: 23.8.12
 * Time: 19:06
 * To change this template use File | Settings | File Templates.
 */
public class TestComplete {
    public static void main(String[] args) {
        // holds results of tests, key is part name, value is list of test results for that part
        HashMap<String, ArrayList<Boolean>> TestResults = new HashMap<String,ArrayList<Boolean>>();

         // market parameters
        double timeStamp1 = System.nanoTime();
        int nHFT = 0;                           // # of HFT's fast traders, fixed
        int nPositiveNonHFT = 1;               // # of positive PV slow traders
        int nZeroNonHFT = 2;                   // # of zero PV slow traders
        int nNegativeNonHFT = 1;               // # of negative PV slow traders
        double lambdaArrival = 0.1;             // arrival frequency, same for all
        double ReturnFrequencyHFT = 1;          // returning frequency of HFT
        double ReturnFrequencyNonHFT = 0.1;     // returning frequency of NonHFT
        double privateValueStdev = 0.35;        // standard deviation of normal distribution of private valus GPR 2005, 0.35 in base case
        float deltaLow = 0.04f;                 // minimum cancellation probability GPR 2005
        String folder = "D:\\_paper1 HFT, MM, rebates and market quality\\Matlab Analysis\\";



        int infoSize = 5;                       // 2-bid, ask, 4-last price, direction, 6-depth at bid,ask, 8-depth off bid,ask
        byte nP = 9;                           // number of prices tracked by the book
        int maxDepth = 7;                       // 0 to 7 which matter
        int FVpos = (int) nP/2;                 // position of the fundamental value
        double prTremble = 0.0;                 // probability of trembling

        /*int HL = FVpos + 6;                     // Lowest  allowed limit order price.  LL + HL = nP-1 for allowed orders centered around E(v)
        int LL = FVpos - 6;                    // Highest allowed limit order price
        float tickSize = 0.0625f;//0.125;       // size of one tick
        int PVsigma = 4;//4                     // # of ticks for negative and positive PVs
        String outputNameTransactions = "Transactions16.csv";  // output file name
        String outputNameBookData = "BookData16.csv";  // output file name
        String outputNameStatsData = "stats16.csv";   // output file name*/

        double privateValueMean = 0.0;      // mean of normal distribution of private values GPR 2005
        int HL = FVpos + 3; //                  // Lowest  allowed limit order price.  LL + HL = nP-1 for allowed orders centered around E(v)
        int LL = FVpos - 3; //                  // Highest allowed limit order price
        float tickSize = 0.125f;                // size of one tick
        int PVsigma = 2;//4                     // # of ticks for negative and positive PVs
        String outputNameTransactions = "Transactions8.csv";  // output file name
        String outputNameBookData = "BookData8.csv";   // output file name
        String outputNameStatsData = "stats8.csv";   // output file name
        double sigma = 1.0;                     // volatility of FV   1/8th 1.0 and 1/16th 2.0

        int end = HL - LL + 1;                  // number of position on the grid for submitting LOs
        int breakPoint = end / 2;               // breaking point for positive, negative, represents FV position on the LO grid
        double FV;                              // Fundamental value-> not position

        boolean header = false;                 // header yes ?
        int hti = 5000000;                      // initial capacity for Payoffs HashTable

        double FVplus = 0.5;                    // probability f FV going up
        float [] FprivateValues = {- PVsigma * tickSize, 0, PVsigma * tickSize};// distribution over private values
        double EventTime = 0.0;                 // captures time

        double [] Prices = new double[nP]; // creates vector of the prices, not carrying about ticks now
        for (int i = 0 ; i < nP ; i++){
            Prices[i] = i * tickSize;
        }
        FV = Prices[FVpos];

        double[] tauB = new double[end]; //   expected time until the arrival of a new buyer for whom trading on the LO yields non-negative payoff

        double[] tauS = new double[end]; //   expected time until the arrival of a new seller for whom picking up the LO yields non-negative payoff


        for (int i = 0; i < end; i++){
            int denomB = 0; // denominator buyers
            int denomS = 0; // denominator sellers
            // buyers
            if (0 <= breakPoint - i - PVsigma){  // add negative value traders
                denomB += nNegativeNonHFT;
            }
            if (0 <= breakPoint - i){ // add zero value traders
                denomB += nZeroNonHFT;
                denomB += nHFT;
            }
            if (0 <= breakPoint - i + PVsigma){  // add positive value traders
                denomB += nPositiveNonHFT;
            }
            // sellers
            if (i - breakPoint + PVsigma >= 0){
                denomS += nNegativeNonHFT;
            }
            if (i - breakPoint >= 0){
                denomS += nZeroNonHFT;
                denomS += nHFT;
            }
            if (i - breakPoint - PVsigma >= 0){
                denomS += nPositiveNonHFT;
            }
            // computing tauB and tauS
            if (denomB == 0){// no buyers interested
                tauB[i] = 1000000000;
            } else {
                tauB[i] = 1 / (denomB * lambdaArrival);
            }
            if (denomS == 0){// no sellers interested
                tauS[i] = 1000000000;
            } else {
                tauS[i] = 1 / (denomS * lambdaArrival);
            }      // computing taus
        }   // computing taus

        HashMap<Integer, Trader> traders = new HashMap<Integer, Trader>(); //trader ID, trader object
        History h = new History(traders, folder); // create history
        // create map of traders

        Trader trader = new Trader(infoSize, tauB, tauS, nP, FVpos, tickSize, ReturnFrequencyHFT,
                ReturnFrequencyNonHFT, LL, HL, end, maxDepth, breakPoint, hti, prTremble, folder);
        LOB_LinkedHashMap book = new LOB_LinkedHashMap("GPR2005", FV, FVpos, maxDepth, end, tickSize, nP ,h, traders);
        // create book
        book.makeBook(Prices);
        Hashtable<Byte, Byte> priorities = new Hashtable<Byte, Byte>();

        ArrayList<Boolean> Initials = new ArrayList<Boolean>();
        ArrayList<Boolean> InitialPayoffs = new ArrayList<Boolean>();
        ArrayList<Boolean> Updated = new ArrayList<Boolean>();
        ArrayList<Boolean> HashCode = new ArrayList<Boolean>();
        ArrayList<Boolean> ChooseMaxIndex = new ArrayList<Boolean>();
        ArrayList<Boolean> IDsWorkflow = new ArrayList<Boolean>();
        ArrayList<Boolean> LOB_test = new ArrayList<Boolean>();
        ArrayList<Boolean> FVmovesTest = new ArrayList<Boolean>();
        ArrayList<Boolean> NewPayoffTest = new ArrayList<Boolean>();
        // initial mu and deltaV tests
        HashMap<String, Float[]> tempIB = trader.computeInitialBeliefs(deltaLow, privateValueMean, privateValueStdev);
        Initials.add((tempIB.get("mu0")[4] < 0.8964 && 0.8963 < tempIB.get("mu0")[4]));
        Initials.add((tempIB.get("mu0")[9] < 0.8964 && 0.8963 < tempIB.get("mu0")[9]));
        Initials.add((tempIB.get("mu0")[2 * end] == 1.0 && 1.0 == tempIB.get("mu0")[2 * end + 1]));
        Initials.add((tempIB.get("deltaV0")[2 * end] == 0.0 && 0.0 == tempIB.get("deltaV0")[2 * end + 1]));
        Initials.add((tempIB.get("deltaV0")[4] == -0.5 && 0.5 == tempIB.get("deltaV0")[9]));

        TestResults.put("Initials", Initials);

        // action creation and GPR2005Payoff constructor test
        byte units2trade = 2;
        HashMap<Short, Float> p = new HashMap<Short, Float>();
        short action1 = 5;
        short action2 = 8;
        short sh = (short)((action2<<7) + action1);
        p.put(sh, 0.6f);
        action1 = 7; action2 = 6; sh = (short)((action1<<7) + action2);
        p.put(sh, 0.5f);
        GPR2005Payoff_test payoff_test = new GPR2005Payoff_test(p, (byte)2);

        p.put(sh, 0.65f);
        payoff_test.updateMax(p, (byte)2, false);
        //payoff_test.update((short)1029, 0.125f, false, 1);




        // initial payoffs
        int[] BookInfo = {3,6,1,1,4,5,3,0};
        int[] BookSizes = {1,1,1,1,-1,-1,-1,-1,-1};
        priorities.put(nP, (byte) (nP + 1)); // trader's position in previous action
        // positions , priorities table & last item is isReturning? 1 if yes, Price in previous action
        for (int i = 0; i < nP; i++){
            priorities.put((byte) i, (byte) 1);
        }
        Trader tr1minus = new Trader(false, -1.0f, (byte)1); traders.put(tr1minus.getTraderID(), tr1minus);
        Trader tr1 = new Trader(false, 1.0f, (byte)1);       traders.put(tr1.getTraderID(), tr1);
        Trader tr0 = new Trader(false, 0.0f, (byte)1);       traders.put(tr0.getTraderID(), tr0);
        Trader tr4 = new Trader(false, 1.0f, (byte)1);       traders.put(tr4.getTraderID(), tr4);
        ArrayList orders = tr0.decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV);
        /*InitialPayoffs.add(PO.getPrice() == 7 && !PO.getCurrentOrder().isBuyOrder());
        if(PO.getPrice() != 7){
            System.out.println(PO.getPrice() + " is the price position, not 7");
        }
        PO = tr1minus.decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV);
        InitialPayoffs.add(PO.getPrice() == 7 && !PO.getCurrentOrder().isBuyOrder());
        if(PO.getPrice() != 7){
            System.out.println(PO.getPrice() + " is the price position, not 7");
        }
        PO = tr1.decision(book.getBookSizes(), book.getBookInfo(), EventTime, FV);
        InitialPayoffs.add(PO.getPrice() == 1 && PO.getCurrentOrder().isBuyOrder());
        if(PO.getPrice() != 1){
            System.out.println(PO.getPrice() + " is the price position, not 1");
        }*/

        TestResults.put("InitialPayoffs", InitialPayoffs);

        // new Trader features
        float[] pay = {4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f,
                4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f, 4.3f};
        short b = (short) Math.max(BookInfo[0] - LL + 1, 0);             // + 1 in order to start from one above B
        b = (short) Math.min(end, b); // TODO: test this change
        short a = (short) Math.min(BookInfo[1] - LL + end, 2 * end);
        a = (short) Math.max(end, a);
        for(short i = b; i < a; i++){            // searching for best payoff and not marketable LO
            p.put((short)(i<<7), pay[i]);
        }
        p.put((short)((2 * end)<<7),     pay[2 * end]);             // SMO
        p.put((short)((2 * end + 1)<<7), pay[2 * end + 1]);         // BMO
        p.put((short)((2 * end + 2)<<7), pay[2 * end + 2]);         // NO



        Boolean buyOrder = false;
        int pricePosition;
        short[] action = new short[units2trade];
        short maxIndex;
        maxIndex = payoff_test.getMaxIndex();
        action[0] = (short)(maxIndex>>7);
        action[1] = (short) (maxIndex - (action[0]<<7));
        System.out.println(action[0] + " " + action[1]);
        ArrayList<Order> orders2 = new ArrayList<Order>();
        for (int i = 0; i < units2trade; i++){
            if ((action[i] >= end) && (action[i] != 2 * end)){
                buyOrder = true;
            }
            pricePosition = (action[i] < end) ? LL + action[i]
                    : LL + action[i] - end;
            if (action[i] == 2 * end || (!buyOrder && pricePosition < BookInfo[0])){pricePosition = BookInfo[0];}       // position is Bid
            if (action[i] == 2 * end + 1 || (buyOrder && pricePosition > BookInfo[1])){pricePosition = BookInfo[1];}
            Order CurrentOrder = new Order(1, EventTime, buyOrder, action[i], pricePosition);
            orders2.add(CurrentOrder);
        }


        // update tests
        BookInfo[0] = 2;
        //tr1.cancel();
        Updated.add(0.3865<((GPR2005Payoff) trader.getPayoffs().get((long) 16777216)).getX1().get((short) 7).getMu() &&
        0.3866 > ((GPR2005Payoff) trader.getPayoffs().get((long) 16777216)).getX1().get((short) 7).getMu());
        /*PO = tr4.decision(book.getBookSizes(), BookInfo, EventTime, FV);
        tr4.execution(FV + 2 * tickSize);

        Updated.add(0.8865<((GPR2005Payoff) trader.getPayoffs().get((long) 151134730)).getX1().get((short) 7).getMu() &&
                ((GPR2005Payoff) trader.getPayoffs().get((long) 151134730)).getX1().get((short) 7).getMu() < 0.8866);
        Updated.add(((GPR2005Payoff) trader.getPayoffs().get((long) 151134730)).getX1().get((short) 7).getDeltaV() == 0.5);
        PO = tr4.decision(book.getBookSizes(), BookInfo, EventTime, FV);
        Updated.add(PO.getPrice() == 1 && PO.getCurrentOrder().isBuyOrder());
        TestResults.put("Updated", Updated);*/

        // HashCode tests
        BookInfo[1] = 7; BookInfo[2] = 2; BookInfo[3] = 3; BookInfo[4] = 5; BookInfo[5] = 3; int l = 0;
        Long code = trader.HashCode(0, 0, 0, BookInfo);
        Boolean testHashCode;
        long code2 = code;
        testHashCode = (code2>>26 == BookInfo[0]);
        code2 = code - (BookInfo[0]<<26);
        testHashCode = (testHashCode && (code2>>21 == BookInfo[1]));
        code2 = code2 - (BookInfo[1]<<21);
        testHashCode = (testHashCode &&(code2>>17 == BookInfo[2]));
        code2 = code2 - (BookInfo[2]<<17);
        testHashCode = (testHashCode &&(code2>>13 == BookInfo[3]));
        code2 = code2 - (BookInfo[3]<<13);
        testHashCode = (testHashCode &&(code2>>7 == BookInfo[4]));
        code2 = code2 - (BookInfo[4]<<7);
        testHashCode = (testHashCode &&(code2>>1 == BookInfo[5]));
        code2 = code2 - (BookInfo[5]<<1);
        testHashCode = (testHashCode &&(code2 == l));
        code2 = code2 - l;
        if (code2 !=0){testHashCode = false;}     // tests
        HashCode.add(testHashCode);
        TestResults.put("HashCode", HashCode);

        // Choosing MaxIndex testing
        float[] payoffs = new float[17];
        payoffs[1] = 1.0f; payoffs[2] = 0.56f; payoffs[12] = 0.6f; payoffs[13] = 0.9f;
        payoffs[16] = 0.55f;
        GPR2005Payoff payoff = new GPR2005Payoff(payoffs, -2, 7);
        ChooseMaxIndex.add(payoff.getMaxIndex() == (short)1);
        payoff = new GPR2005Payoff(payoffs, 1, 7);
        ChooseMaxIndex.add(payoff.getMaxIndex() == (short)13);
        payoff = new GPR2005Payoff(payoffs, 1, 6);
        ChooseMaxIndex.add(payoff.getMaxIndex() == (short)12);
        payoff = new GPR2005Payoff(payoffs, 1, 5);
        ChooseMaxIndex.add(payoff.getMaxIndex() == (short)2);
        payoff = new GPR2005Payoff(payoffs, 2, 5);
        ChooseMaxIndex.add(payoff.getMaxIndex() == (short)16);
        payoff = new GPR2005Payoff(payoffs, 7, 6);
        ChooseMaxIndex.add(payoff.getMaxIndex() == (short)12);
        payoff = new GPR2005Payoff(payoffs, 1, -1);
        ChooseMaxIndex.add(payoff.getMaxIndex() == (short)2);

        TestResults.put("ChooseMaxIndex", ChooseMaxIndex);

        // IDs workflow test
        /*int NewNonHFT = nNegativeNonHFT + nPositiveNonHFT + nZeroNonHFT;
        double Lambda;
        int nEvents = 10000000;
        trader.setWriteDiag(true);

        SingleRun sr = new SingleRun("GPR2005", lambdaArrival, ReturnFrequencyHFT, ReturnFrequencyNonHFT,
                FprivateValues, privateValueMean, privateValueStdev, deltaLow, sigma, tickSize, FVplus, header, book, traders, h, trader, outputNameStatsData,
                outputNameTransactions, outputNameBookData);

        nEvents = 1;                    // number of events
        boolean write = false;          // writeDecisions output in this SingleRun?
        boolean writeDiagnostics = true;// write diagnostics controls diagnostics
        boolean writeHistogram = false; // write histogram
        boolean purge = false;          // purge in this SingleRun?
        boolean nReset = false;         // reset n in this SingleRun?
        //trader.setPrTremble(0.1);
        //trader.setWriteDec(true);
        trader.setWriteDiag(writeDiagnostics);
        //trader.setWriteHist(writeHistogram);

        double[] RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, EventTime, FV, write,
                        purge, nReset, writeDiagnostics, writeHistogram);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];

        IDsWorkflow.add(traders.size() == 1);
        sr.run(1000, nHFT, NewNonHFT, EventTime, FV, write,
                purge, nReset, writeDiagnostics, writeHistogram);
        IDsWorkflow.add(traders.size() == book.traderIDsNonHFT.size());*/
        TestResults.put("IDsWorkflow", IDsWorkflow);

        // testing LOB_test
        LOB_test book2 = new LOB_test("GPR2005", FV, FVpos, maxDepth, end, tickSize, nP ,h, traders);
        // create book
        book2.makeBook(Prices);
        Order_test o1 = new Order_test(tr1.getTraderID(), 0.0, false, (short) 2, 3);
        Order_test o2 = new Order_test(tr1.getTraderID(), 0.0, false, (short) 2, 3);
        Order_test o3 = new Order_test(tr1.getTraderID(), 0.0, false, (short) 1, 2);
        orders = new ArrayList<Order_test>();
        orders.add(o1); orders.add(o2); orders.add(o3);
        book2.transactionRule(tr1.getTraderID(), orders);
        LOB_test.add(book2.CurrentPosition.get(tr1.getTraderID()).get(3) == -2);
        LOB_test.add(book2.book[3].get(book2.book[3].keySet().iterator().next()) == o1);
        LOB_test.add(book2.book[2].get(book2.book[2].keySet().iterator().next()) == o3);

        Order_test o4 = new Order_test(tr0.getTraderID(), 0.0, true, (short) 8, 3);
        Order_test o5 = new Order_test(tr0.getTraderID(), 0.0, true, (short) 8, 3);
        orders = new ArrayList<Order_test>(); orders.add(o4); orders.add(o5);
        book2.transactionRule(tr0.getTraderID(), orders);
        LOB_test.add(!book2.CurrentPosition.get(tr1.getTraderID()).containsKey(3));
        LOB_test.add(book2.book[3].isEmpty());
        LOB_test.add(!book2.CurrentPosition.containsKey(tr0.getTraderID()));

        Order_test o6 = new Order_test(tr1minus.getTraderID(), 0.0, true, (short) 7, 2);
        Order_test o7 = new Order_test(tr1minus.getTraderID(), 0.0, true, (short) 10, 3);
        Order_test o8 = new Order_test(tr1minus.getTraderID(), 0.0, true, (short) 8, 1);
        Order_test o9 = new Order_test(tr1minus.getTraderID(), 0.0, true, (short) 7, 0);
        Order_test o10 = new Order_test(tr1minus.getTraderID(), 0.0, true, (short) 7, 0);
        orders = new ArrayList<Order_test>(); orders.add(o6); orders.add(o7); orders.add(o8);
        orders.add(o9); orders.add(o10);
        book2.transactionRule(tr1minus.getTraderID(), orders);
        LOB_test.add(!book2.CurrentPosition.containsKey(tr1.getTraderID()));
        LOB_test.add(book2.CurrentPosition.get(tr1minus.getTraderID()).get(3) == 1);
        LOB_test.add(book2.book[3].get(book2.book[3].keySet().iterator().next()) == o7);

        TestResults.put("LOB_test", LOB_test);

        // FVup, FVdown LOB_test testing
        FV = FV + 1 * tickSize;                 book2.FVup(FV, 0.0, 1);
        FVmovesTest.add(book2.book[0].get(book2.book[0].keySet().iterator().next()) == o8);
        FVmovesTest.add(book2.getBookSizes()[2] == 1);

        Order_test o11 = new Order_test(tr0.getTraderID(), 0.0, false, (short) 0, 1);
        orders = new ArrayList<Order_test>();           orders.add(o11);
        traders.put(tr0.getTraderID(), tr0);
        book2.transactionRule(tr0.getTraderID(), orders);
        FV = FV + 1 * tickSize;                 book2.FVup(FV, 0.0, 1);
        FVmovesTest.add(!book2.CurrentPosition.containsKey(tr0.getTraderID()));
        FVmovesTest.add(!traders.containsKey(tr0.getTraderID()));

        Order_test o12 = new Order_test(tr4.getTraderID(), 0.0, false, (short) 1, 2);
        Order_test o13 = new Order_test(tr4.getTraderID(), 0.0, false, (short) 1, 2);
        orders = new ArrayList<Order_test>();           orders.add(o12); orders.add(o13);
        book2.transactionRule(tr4.getTraderID(), orders);
        FV = FV + 2 * tickSize;                 book2.FVup(FV, 0.0, 2);
        FVmovesTest.add(!book2.CurrentPosition.containsKey(tr1minus.getTraderID()));
        FVmovesTest.add(!book2.CurrentPosition.containsKey(tr4.getTraderID()));
        FVmovesTest.add(!traders.containsKey(tr4.getTraderID()));

        Order_test o14 = new Order_test(tr4.getTraderID(), 0.0, false, (short) 6, 8);
        Order_test o15 = new Order_test(tr4.getTraderID(), 0.0, false, (short) 6, 8);
        Order_test o16 = new Order_test(tr4.getTraderID(), 0.0, true, (short) 13, 7);
        Order_test o17 = new Order_test(tr4.getTraderID(), 0.0, true, (short) 12, 6);
        Order_test o18 = new Order_test(tr4.getTraderID(), 0.0, true, (short) 12, 6);
        orders = new ArrayList<Order_test>();           orders.add(o14); orders.add(o15);
        orders.add(o16); orders.add(o17);orders.add(o18);
        traders.put(tr4.getTraderID(), tr4);
        book2.transactionRule(tr4.getTraderID(), orders);
        FV = FV - 2 * tickSize;                 book2.FVdown(FV, 0.0, 2);
        FVmovesTest.add(!book2.CurrentPosition.containsKey(tr4.getTraderID()));
        FVmovesTest.add(!traders.containsKey(tr4.getTraderID()));
        FVmovesTest.add(book2.getPositionShift() == 2);
        FVmovesTest.add(book2.Prices[2] == 4 * tickSize);

        TestResults.put("FVmovesTests", FVmovesTest);
        // printing results of tests
        Iterator it = TestResults.keySet().iterator();
        while (it.hasNext()){
            String s = (String) it.next();
            int sz = TestResults.get(s).size();
            for (int i = 0; i < sz; i ++){
                System.out.println(s + " " + TestResults.get(s).get(i).toString());
            }
        }
        book2.printBook();

        //Trader.Payoffs.put(0, new GPR2005Payoff())

        //tr1.cancel();
        tr1.decision(BookSizes, BookInfo, EventTime, FV);

        /*for (int i = 0; i < nEvents; i ++){

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

            double prob1 = (double) nHFT / nAll;
            double prob2 = (double) NewNonHFT / nAll;
            double prob3 = (double) ReturningHFT / nAll;
            double prob4 = (double) ReturningNonHFT / nAll;
            double prob5 = 0.0;

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
            int ID;
            float FVrealization;
            *//*if (rn < x1){                          // New arrival HFT
                tr = new Trader(true, 0);
                ID = tr.getTraderID();
                *//**//* if(book.removedTraders.contains(ID)){
                     System.out.println("new Zombie HFT");
                 }*//**//*
                //System.out.println("New arrival HFT ID: " + ID);
                traders.put(ID, tr);
                PriceOrder PO = tr.decision(book.getRank(ID), book.getBookSizes(), book.getBookInfo(),
                        EventTime, FV);
                if (PO != null){
                    book.transactionRule(PO.getPrice() , PO.getCurrentOrder());
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
                tr = new Trader(false, FVrealization);
                ID = tr.getTraderID();
                *//**//* if(book.removedTraders.contains(ID)){
                     System.out.println("new zombie nonHFT");
                 }*//**//*

                //System.out.println("New arrival nonHFT ID: " + ID);
                traders.put(ID, tr);
                PriceOrder PO = tr.decision(book.getRank(ID), book.getBookSizes(), book.getBookInfo(),
                        EventTime, FV);

                if (PO != null){
                    book.transactionRule(PO.getPrice() , PO.getCurrentOrder());
                } else {book.addTrader(ID);}
            } else if (rn < x3){                   // Returning HFT
                ID = book.randomHFTtraderID();
                //System.out.println("Returning HFT ID: " + ID);
                *//**//* if(book.removedTraders.contains(ID)){
                     System.out.println("old Zombie HFT");
                 }*//**//*
                PriceOrder PO = traders.get(ID).decision(book.getRank(ID), book.getBookSizes(), book.getBookInfo(),
                        EventTime, FV);
                if (PO != null){
                    book.transactionRule(PO.getPrice() , PO.getCurrentOrder());
                } else {
                    book.tryCancel(ID);
                }

            } else if (rn < x4){                   // Returning nonHFT
                ID = book.randomNonHFTtraderID();
                *//**//*if(book.removedTraders.contains(ID)){
                    System.out.println("old Zombie nonHFT");
                }*//**//*
                //System.out.println("Returning nonHFT ID: " + ID);
                PriceOrder PO = traders.get(ID).decision(book.getRank(ID), book.getBookSizes(), book.getBookInfo(),
                        EventTime, FV);
                if (PO != null){
                    book.transactionRule(PO.getPrice() , PO.getCurrentOrder());
                } else {
                    book.tryCancel(ID);
                }

                *//**//* if (traders.containsKey(ID)){
                    if(book.book[PO.getPrice()].containsKey(ID) && traders.get(ID).getIsTraded()){
                        System.out.println("problem");
                    }
                }*//**//*
            } else{                                // Change in FV
                double rn3 = Math.random();
                if (rn3 < FVplus){
                    FV = FV + sigma * tickSize;
                    book.FVup(FV, EventTime);
                    //System.out.println("up" + FV);
                } else {
                    FV = FV - sigma * tickSize;
                    book.FVdown(FV, EventTime);
                    //System.out.println("down" + FV);
                }
            }*//*
            *//*if (i % 1000 == 0) {
                h.addStatisticsData(i, trader.getStatesCount());   // multiple payoffs count
                trader.printDiagnostics();
                trader.resetDiagnostics();
                h.printStatisticsData(header, outputNameStatsData);
                h.resetHistory();
             }*//*
        } // random events*/          // events workflow




        double timeStamp2 = System.nanoTime();
        System.out.println("running time = " + (timeStamp2 - timeStamp1));

    }

}
