import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jakub
 * Date: 15.3.12
 * Time: 21:23
 * To change this template use File | Settings | File Templates.
 */
public class ThirdLobTest {
    public static void main(String[] args) {
        /* categories of traders: fast, slow, private value: negative, zero, positive
  that means 4 categories of traders. Fast have zero PV */

        // market parameters
        double timeStamp1 = System.nanoTime();
        int nHFT = 5;                           // # of HFT's fast traders, fixed
        int nPositiveNonHFT = 10;               // # of positive PV slow traders
        int nZeroNonHFT = 10;                   // # of zero PV slow traders
        int nNegativeNonHFT = 10;               // # of negative PV slow traders
        double lambdaArrival = 0.1;             // arrival frequency, same for all
        double ReturnFrequencyHFT = 1;          // returning frequency of HFT
        double ReturnFrequencyNonHFT = 0.1;     // returning frequency of NonHFT
        String folder = "C:\\Users\\Jakub\\Documents\\School\\SFI\\_paper1 HFT, MM, rebates and market quality\\Matlab Analysis\\";


        int infoSize = 8;                       // size of the BookInfo in LOB
        byte nP = 63;                           // number of prices tracked by the book
        int maxDepth = 15;                      // 0 to 15 which matter
        int FVpos = (int) nP/2;

        int HL = 34; //37                       // Lowest  allowed limit order price.  LL + HL = nP-1 for allowed orders centered around E(v)
        int LL = 28; //25                       // Highest allowed limit order price
        float tickSize = 0.125f;//0.125;        // size of one tick
        int PVsigma = 2;//4                     // # of ticks for negative and positive PVs
        String outputNameTransactions = "Transactions8.csv";  // output file name
        String outputNameBookData = "BookData8.csv";   // output file name
        String outputNameStatsData = "stats8.csv";   // output file name

        /*int HL = 37; //37                     // Lowest  allowed limit order price.  LL + HL = nP-1 for allowed orders centered around E(v)
        int LL = 25; //25                       // Highest allowed limit order price
        float tickSize = 0.0625f;//0.125;       // size of one tick
        int PVsigma = 4;//4                     // # of ticks for negative and positive PVs
        String outputNameTransactions = "Transactions16.csv";  // output file name
        String outputNameBookData = "BookData16.csv";*/  // output file name

        int end = HL - LL + 1;                  // number of position on the grid for submitting LOs
        int breakPoint = end / 2;               // breaking point for positive, negative, represents FV position on the LO grid
        double FV;                              // Fundamental value-> not position
        double sigma = 1.0;                     // volatility of FV
        double prTremble = 0.0;                 // probability of trembling

        boolean header = false;                 // header yes ?
        int hti = 5000000;                      // initial capacity for Payoffs HashTable
        double FVplus = 0.5;                    // probability f FV going up
        float [] FprivateValues = {- PVsigma * tickSize, 0, PVsigma * tickSize};// distribution over private values
        double EventTime = 0.0;                 // captures time

        double [] Prices = new double[nP]; // creates vector of the prices, not carrying about ticks now
        for (int i = 0 ; i < nP ; i++){
            Prices[i] = i * tickSize;
        }
        FV = Prices[31];

        double[] tauB = new double[end];
        /* expected time until the arrival of a new buyer for whom trading on
        the LO yields non-negative payoff */

        double[] tauS = new double[end];
        /* expected time until the arrival of a new seller for whom picking up
        the LO yields non-negative payoff */


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
            }
        }

       /* for (int j = 0; j < 13; j ++){
            System.out.println(tauB[j]);
            System.out.println(tauS[12-j]);
        }*/



        HashMap<Integer, Trader> traders = new HashMap<Integer, Trader>();
        History h = new History(traders, folder); // create history
        // create map of traders

        Trader trader = new Trader(infoSize, tauB, tauS, nP, FVpos, tickSize, ReturnFrequencyHFT,
                ReturnFrequencyNonHFT, LL, HL, end, maxDepth, breakPoint, hti, prTremble, folder, h);
        LOB_LinkedHashMap book = new LOB_LinkedHashMap(FV, FVpos, maxDepth, end, tickSize, nP ,h, traders);
        // create book
        book.makeBook(Prices);

        int NewNonHFT = nNegativeNonHFT + nPositiveNonHFT + nZeroNonHFT;

        SingleRun sr = new SingleRun(lambdaArrival, ReturnFrequencyHFT, ReturnFrequencyNonHFT,
                 FprivateValues, sigma, tickSize, FVplus, header, book, traders, h, trader, outputNameStatsData,
                outputNameTransactions, outputNameBookData);

        int nEvents = 1750000000;                 // number of events
        boolean write = false;          // write output in this SingleRun?
        boolean purge = true;          // purge in this SingleRun?
        boolean nReset = true;         // reset n in this SingleRun?

        double[] RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, EventTime, FV, write,
                        purge, nReset);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];

         // all potential new traders
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

             double Lambda = (nHFT + NewNonHFT) * lambdaArrival + ReturningHFT * ReturnFrequencyHFT +
                     + ReturningNonHFT * ReturnFrequencyNonHFT;
             EventTime += - Math.log(1.0- Math.random()) / Lambda; // random exponential time

             // number of all agents to trade

             double prob1 = (double) nHFT / nAll * 0.8;
             double prob2 = (double) NewNonHFT / nAll * 0.8;
             double prob3 = (double) ReturningHFT / nAll * 0.8;
             double prob4 = (double) ReturningNonHFT / nAll * 0.8;
             double prob5 = 0.2;

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
             if (rn < x1){                          // New arrival HFT
                 tr = new Trader(true, 0);
                 ID = tr.getTraderID();
                *//* if(book.removedTraders.contains(ID)){
                     System.out.println("new Zombie HFT");
                 }*//*
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
                *//* if(book.removedTraders.contains(ID)){
                     System.out.println("new zombie nonHFT");
                 }*//*

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
                *//* if(book.removedTraders.contains(ID)){
                     System.out.println("old Zombie HFT");
                 }*//*

                 PriceOrder PO = traders.get(ID).decision(book.getRank(ID), book.getBookSizes(), book.getBookInfo(),
                         EventTime, FV);
                 if (PO != null){
                     book.transactionRule(PO.getPrice() , PO.getCurrentOrder());
                 } else {
                     book.tryCancel(ID);    //TODO: test
                 }

             } else if (rn < x4){                   // Returning nonHFT
                 ID = book.randomNonHFTtraderID();
                 *//*if(book.removedTraders.contains(ID)){
                     System.out.println("old Zombie nonHFT");
                 }*//*
                 //System.out.println("Returning nonHFT ID: " + ID);
                 PriceOrder PO = traders.get(ID).decision(book.getRank(ID), book.getBookSizes(), book.getBookInfo(),
                         EventTime, FV);

                 if (PO != null){
                     book.transactionRule(PO.getPrice() , PO.getCurrentOrder());
                 } else {
                     book.tryCancel(ID);
                 }

             } else{                                // Change in FV
                 double rn3 = Math.random();
                 if (rn3 < FVplus){
                    FV = FV + sigma * tickSize;
                    book.FVup(FV, EventTime);
                 } else {
                     FV = FV - sigma * tickSize;
                     book.FVdown(FV, EventTime);
                 }
             }
             h.addOrderData(book.getBestBid(), book.getBestAsk());
             if (i % 100000 == 0) {
                 h.addStatisticsData(i, trader.getStatesCount());
                 //h.printTransactions(header, outputNameTransactions);
                 //h.printBookData(header, outputNameBookData);
                 //h.printStatisticsData(header, outputNameStatsData);
                 //h.resetHistory();
             }    //where events happen
             if (i % 20000000 == 0) {
                 trader.purge();
             }
        }*/
        trader.printStatesDensity(EventTime); // occurrences of MPs now
        book.printBook();
        System.out.println("Traders count in Trader " + Trader.TraderCount
         +  " trade count " + Trader.tradeCount + " states count " + Trader.statesCount);
        double timeStamp2 = System.nanoTime();
        System.out.println("running time = " + (timeStamp2 - timeStamp1));

    }
}
