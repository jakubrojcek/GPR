
import com.jakubrojcek.gpr2005a.History;
import com.jakubrojcek.gpr2005a.LOB_LinkedHashMap;
import com.jakubrojcek.gpr2005a.SingleRun;
import com.jakubrojcek.gpr2005a.Trader;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Jakub
 * Date: 7.9.12
 * Time: 10:48
 * To change this template use File | Settings | File Templates.
 */
public class ReducedStatesTest {
    public static void main(String[] args) {
        /* categories of traders: fast, slow, private value: negative, zero, positive
  that means 4 categories of traders. Fast have zero PV */

        // market parameters
        double timeStamp1 = System.nanoTime();
        int nHFT = 0;                           // # of HFT's fast traders, fixed
        int nPositiveNonHFT = 10;               // # of positive PV slow traders
        int nZeroNonHFT = 20;                   // # of zero PV slow traders
        int nNegativeNonHFT = 10;               // # of negative PV slow traders
        double lambdaArrival = 1;               // arrival frequency, same for all
        double ReturnFrequencyHFT = 1;          // returning frequency of HFT
        double ReturnFrequencyNonHFT = 0.1;     // returning frequency of NonHFT

        double privateValueStdev = 0.35;        // standard deviation of normal distribution of private valus GPR 2005, 0.35 in base case
        float deltaLow = 0.04f;                 // minimum cancellation probability GPR 2005
        String folder = "D:\\_paper1 HFT, MM, rebates and market quality\\Matlab Analysis\\";
        String model = "GPR2005";



        int infoSize = 5;                       // 2-bid, ask, 5- GPR 2005, 6-depth at bid,ask, 8-depth off bid,ask
        byte nP = 8;                           // number of prices tracked by the book, 8 in the base case, 6/11 in tick size experiment
        int maxDepth = 15;                       // 0 to 7 which matter
        int FVpos = (int) nP/2;                 // position of the fundamental value
        double prTremble = 0.0;                 // probability of trembling

        /*double privateValueMean = 0.0;          // mean of normal distribution of private values GPR 2005
        int HL = FVpos + 3;                     // Lowest  allowed limit order price.  LL + HL = nP-1 for allowed orders centered around E(v)
        int LL = FVpos - 3;                     // Highest allowed limit order price
        float tickSize = 0.0625f;               // size of one tick
        int PVsigma = 4;                        // # of ticks for negative and positive PVs
        String outputNameTransactions = "Transactions16.csv";  // output file name
        String outputNameBookData = "BookData16.csv";  // output file name
        String outputNameStatsData = "stats16.csv";   // output file name
        double sigma = 2.0;                     // volatility of FV   1/8th 1.0 and 1/16th 2.0*/

        double privateValueMean = -0.0625;      // mean of normal distribution of private values GPR 2005
        int HL = FVpos + 2; //                  // Lowest  allowed limit order price.  LL + HL = nP-1 for allowed orders centered around E(v)
        int LL = FVpos - 3; //                  // Highest allowed limit order price
        float tickSize = 0.125f;                // size of one tick
        int PVsigma = 2;//4                     // # of ticks for negative and positive PVs
        String outputNameTransactions = "Transactions8.csv";  // output file name
        String outputNameBookData = "BookData8.csv";   // output file name
        String outputNameStatsData = "stats8.csv";   // output file name
        double sigma = 1.0;                     // volatility of FV   1/8th 1.0 and 1/16th 2.0

        int end = HL - LL + 1;                  // number of position on the grid for submitting LOs
        int breakPoint = FVpos - LL; //end / 2; // breaking point for positive, negative, represents FV position on the LO grid
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

        LOB_LinkedHashMap book = new LOB_LinkedHashMap(model, FV, FVpos, maxDepth, end, tickSize, nP, h, traders);
        Trader trader = new Trader(infoSize, tauB, tauS, nP, FVpos, tickSize, ReturnFrequencyHFT,
                ReturnFrequencyNonHFT, LL, HL, end, maxDepth, breakPoint, hti, prTremble, folder);
        trader.computeInitialBeliefs(deltaLow, privateValueMean, privateValueStdev);
        // create book
        book.makeBook(Prices);

        int NewNonHFT = nNegativeNonHFT + nPositiveNonHFT + nZeroNonHFT;

        SingleRun sr = new SingleRun(model, lambdaArrival, ReturnFrequencyHFT, ReturnFrequencyNonHFT,
                FprivateValues, privateValueMean, privateValueStdev, deltaLow, sigma, tickSize, FVplus, header, book, traders, h, trader, outputNameStatsData,
                outputNameTransactions, outputNameBookData);
        trader.setSimilar(true);
        trader.setSymm(true);
        // getting to equilibrium ballpark
        int nEvents = 300000000;         // number of events
        boolean write = false;          // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
        boolean writeDiagnostics = true;// write diagnostics controls diagnostics
        boolean writeHistogram = false; // write histogram
        boolean purge = false;          // purge in this com.jakubrojcek.gpr2005a.SingleRun?
        boolean nReset = false;         // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
        //trader.setPrTremble(0.1);
        //trader.setWriteDec(true);
        trader.setWriteDiag(writeDiagnostics);
        //trader.setWriteHist(writeHistogram);

        double[] RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, EventTime, FV, write,
                        purge, nReset, writeDiagnostics, writeHistogram);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];

        //trader.printStatesDensity(EventTime); // occurrences of MPs now
        //trader.printHistogram();

        for (int k = 0; k < 3; k++){
            trader.setFixedBeliefs(false);
            nEvents = 20000000;         // number of events
            write = false;              // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
            writeDiagnostics = true;    // write diagnostics controls diagnostics
            writeHistogram = false;     // write histogram
            purge = true;              // purge in this com.jakubrojcek.gpr2005a.SingleRun?
            nReset = true;              // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
            trader.setPrTremble(0.15);
            //trader.setWriteDec(write);
            trader.setWriteDiag(writeDiagnostics);
            //trader.setWriteHist(writeHistogram);


            RunOutcome =
                    sr.run(nEvents, nHFT, NewNonHFT, EventTime, FV, write,
                            purge, nReset, writeDiagnostics, writeHistogram);
            EventTime = RunOutcome[0];
            FV = RunOutcome[1];

            nEvents = 20000000;         // number of events
            write = false;              // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
            writeDiagnostics = true;    // write diagnostics controls diagnostics
            writeHistogram = false;     // write histogram
            purge = false;              // purge in this com.jakubrojcek.gpr2005a.SingleRun?
            nReset = true;              // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
            trader.setPrTremble(0.05);
            //trader.setWriteDec(write);
            trader.setWriteDiag(writeDiagnostics);
            //trader.setWriteHist(writeHistogram);

            RunOutcome =
                    sr.run(nEvents, nHFT, NewNonHFT, EventTime, FV, write,
                            purge, nReset, writeDiagnostics, writeHistogram);
            EventTime = RunOutcome[0];
            FV = RunOutcome[1];

            nEvents = 20000000;         // number of events
            write = false;              // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
            writeDiagnostics = true;    // write diagnostics controls diagnostics
            writeHistogram = false;     // write histogram
            purge = false;              // purge in this com.jakubrojcek.gpr2005a.SingleRun?
            nReset = true;              // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
            trader.setPrTremble(0.025);
            //trader.setWriteDec(write);
            trader.setWriteDiag(writeDiagnostics);
            //trader.setWriteHist(writeHistogram);

            RunOutcome =
                    sr.run(nEvents, nHFT, NewNonHFT, EventTime, FV, write,
                            purge, nReset, writeDiagnostics, writeHistogram);
            EventTime = RunOutcome[0];
            FV = RunOutcome[1];

            //trader.setFixedBeliefs(true);
            nEvents = 20000000;         // number of events
            write = false;              // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
            writeDiagnostics = true;    // write diagnostics controls diagnostics
            writeHistogram = false;     // write histogram
            purge = false;              // purge in this com.jakubrojcek.gpr2005a.SingleRun?
            nReset = true;              // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
            trader.setPrTremble(0.0);
            trader.setWriteDec(write);
            trader.setWriteDiag(writeDiagnostics);
            //trader.setWriteHist(writeHistogram);

            RunOutcome =
                    sr.run(nEvents, nHFT, NewNonHFT, EventTime, FV, write,
                            purge, nReset, writeDiagnostics, writeHistogram);
            EventTime = RunOutcome[0];
            FV = RunOutcome[1];
        }

        trader.setFixedBeliefs(true);
        nEvents = 100000000;        // number of events
        write = true;               // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
        writeDiagnostics = true;    // write diagnostics controls diagnostics
        writeHistogram = true;      // write histogram
        purge = false;              // purge in this com.jakubrojcek.gpr2005a.SingleRun?
        nReset = true;             // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
        //trader.setPrTremble(0.0);
        trader.setWriteDec(write);
        trader.setWriteDiag(writeDiagnostics);
        trader.setWriteHist(writeHistogram);
        trader.setTradeCount(0);
        trader.setStatesCount(0);
        trader.setTraderCount(book.getnReturningHFT() + book.getnReturningNonHFT());

        RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, EventTime, FV, write,
                        purge, nReset, writeDiagnostics, writeHistogram);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];

        //trader.printStatesDensity(EventTime); // occurrences of MPs now
        book.printBook();

        /*System.out.println("Traders count in com.jakubrojcek.gpr2005a.Trader " + Trader.TraderCount
                +  " trade count " + Trader.tradeCount + " states count " + Trader.statesCount);*/
        double timeStamp2 = System.nanoTime();
        System.out.println("running time = " + (timeStamp2 - timeStamp1));

    }
}
