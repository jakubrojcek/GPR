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
         // market parameters
        double timeStamp1 = System.nanoTime();
        int nHFT = 0;                           // # of HFT's fast traders, fixed
        int nPositiveNonHFT = 10;               // # of positive PV slow traders
        int nZeroNonHFT = 10;                   // # of zero PV slow traders
        int nNegativeNonHFT = 10;               // # of negative PV slow traders
        double lambdaArrival = 0.1;             // arrival frequency, same for all
        double ReturnFrequencyHFT = 1;          // returning frequency of HFT
        double ReturnFrequencyNonHFT = 0.1;     // returning frequency of NonHFT
        String folder = "D:\\_paper1 HFT, MM, rebates and market quality\\Matlab Analysis\\";



        int infoSize = 6;                       // 2-bid, ask, 4-last price, direction, 6-depth at bid,ask, 8-depth off bid,ask
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

        int HL = FVpos + 3; // + 6              // Lowest  allowed limit order price.  LL + HL = nP-1 for allowed orders centered around E(v)
        int LL = FVpos - 3; //              // Highest allowed limit order price
        float tickSize = 0.125f;//0.125;        // size of one tick
        int PVsigma = 2;//4                     // # of ticks for negative and positive PVs
        String outputNameTransactions = "Transactions8.csv";  // output file name
        String outputNameBookData = "BookData8.csv";   // output file name
        String outputNameStatsData = "stats8.csv";   // output file name

        int end = HL - LL + 1;                  // number of position on the grid for submitting LOs
        int breakPoint = end / 2;               // breaking point for positive, negative, represents FV position on the LO grid
        double FV;                              // Fundamental value-> not position
        double sigma = 1.0;                     // volatility of FV

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
            }      // computing taus
        }

        /* for (int j = 0; j < 13; j ++){
            System.out.println(tauB[j]);
            System.out.println(tauS[12-j]);
        } */      //computing taus



        HashMap<Integer, Trader> traders = new HashMap<Integer, Trader>(); //trader ID, trader object
        History h = new History(traders, folder); // create history
        // create map of traders

        Trader trader = new Trader(infoSize, tauB, tauS, nP, FVpos, tickSize, ReturnFrequencyHFT,
                ReturnFrequencyNonHFT, LL, HL, end, maxDepth, breakPoint, hti, prTremble, folder);
        LOB_LinkedHashMap book = new LOB_LinkedHashMap(FV, FVpos, maxDepth, end, tickSize, nP ,h, traders);
        // create book
        book.makeBook(Prices);
        Hashtable<Byte, Byte> priorities = new Hashtable<Byte, Byte>();
        int[] BookSizes = new int[nP];
        int[] BookInfo = new int[8];
        BookInfo[0] = 2;                 // best bid position
        BookInfo[1] = 6;                 // best ask position
        BookInfo[2] = 1;                 // depth at best bid
        BookInfo[3] = 1;                 // depth at best ask
        BookInfo[4] = 4;                 // depth buys
        BookInfo[5] = 4;                 // depth sells
        BookInfo[6] = 4;                 // last transaction position
        BookInfo[7] = 0;                 // 1 if last transaction buy, 0 if sell
        for (int i = 0; i < nP; i++){
            priorities.put((byte) i, (byte) 1);
            BookSizes[i] = 1;
        }
        priorities.put(nP, (byte)1);

        boolean[] test = new boolean[10];

        traders.put(1, new Trader(false, 0.0f));
        traders.put(2, new Trader(false, 0.0f));

        Trader tr = new Trader(false, 0.0f);
        traders.put(3, tr);
        book.transactionRule(2, new Order(1, 0.001, true));
        book.transactionRule(6, new Order(2, 0.002, false));
        book.transactionRule(2, new Order(3, 0.003, false));
        test[0] = !book.traderIDsNonHFT.contains(3);


        Trader tr2 = new Trader(false, 0.0f);
        traders.put(4, tr2);
        book.transactionRule(3, new Order(4, 0.004, true));
        test[1] = book.traderIDsNonHFT.contains(4);

        Trader tr3 = new Trader(false, 0.0f);
        traders.put(5, tr3);
        book.addTrader(5);
        test[2] = book.traderIDsNonHFT.contains(5);

        book.transactionRule(3, new Order(5, 0.005, false));
        test[3] = !book.traderIDsNonHFT.contains(4);
        test[7] = !book.traderIDsNonHFT.contains(5);

        Trader tr4 = new Trader(false, 0.0f);
        traders.put(6, tr4);
        book.transactionRule(3, new Order(6, 0.006, true));
        book.transactionRule(6, new Order(6, 0.007, true));
        test[4] = !book.traderIDsNonHFT.contains(6);

        Trader tr5 = new Trader(false, 0.0f);
        traders.put(7, tr5);
        book.transactionRule(3, new Order(7, 0.008, true));
        book.transactionRule(3, new Order(7, 0.008, true));
        boolean part1 = book.traderIDsNonHFT.contains(7);
        book.transactionRule(4, new Order(7, 0.009, true));
        boolean part2 = book.traderIDsNonHFT.contains(7);
        test[5] = part1&&part2;

        book.tryCancel(7);
        part1 = book.traderIDsNonHFT.contains(7);
        part2 = !book.book[4].containsKey(7);
        test[6] = part1&&part2;

        Trader tr6 = new Trader(false, 0.0f);
        traders.put(8, tr6);
        book.addTrader(8);
        book.transactionRule(7, new Order(8, 0.010, false));
        test[8] = book.traderIDsNonHFT.contains(8);

        Trader tr7 = new Trader(false, 0.0f);
        traders.put(9, tr7);
        book.addTrader(9);
        book.tryCancel(9);
        test[9] = book.traderIDsNonHFT.contains(9);

        for (int i = 0; i < 10; i++){
            System.out.println("test" + (i + 1) + ": " + test[i]);
        }

/*        int nEvents = 30;
        for (int i = 0; i < nEvents; i ++){
            EventTime = i * 0.05;
            tr.decision(priorities, BookSizes, BookInfo, EventTime, FV);
            tr.execution(Prices[2], EventTime);
        }
        //trader.nReset((byte)1, (byte)10);
        for (int i = 0; i < nEvents; i ++){
            EventTime = i * 0.05;
            tr.decision(priorities, BookSizes, BookInfo, EventTime, FV);
            tr.execution(Prices[2], EventTime);
        }*/

        double timeStamp2 = System.nanoTime();
        System.out.println("running time = " + (timeStamp2 - timeStamp1));

    }

}
