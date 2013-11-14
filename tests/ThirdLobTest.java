import com.jakubrojcek.Order;
import com.jakubrojcek.gpr2005a.*;
import com.jakubrojcek.hftRegulation.History;
import com.jakubrojcek.hftRegulation.LOB_LinkedHashMap;
import com.jakubrojcek.hftRegulation.*;
import com.jakubrojcek.hftRegulation.SingleRun;
import com.jakubrojcek.hftRegulation.Trader;

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
        double timeStamp1 = System.nanoTime();
        String model = "returning";
        String folder = "D:\\_paper1 HFT, MM, rebates and market quality\\Matlab Analysis\\Profiling14112013\\";
        String outputNameTransactions = "Transactions8.csv";  // output file name
        String outputNameBookData = "effSpread.csv";   // output file name
        String outputNameStatsData = "stats8.csv";   // output file name
        boolean header = false;                 // header yes ?
        int hti = 5000000;                      // initial capacity for Payoffs HashTable
        int infoSize = 8;                       // 2-bid, ask, 5- GPR 2005, 6-depth at bid,ask, 8-depth off bid,ask
        double prTremble = 0.0;                 // probability of trembling
        byte nP = 11;                            // number of prices tracked by the book, 8 in the base case, 6/11 in tick size experiment
        int nHFT = 0;                           // # of HFT's fast traders, fixed
        int nPositiveNonHFT = 1;                // # of positive PV slow traders
        int nZeroNonHFT = 2;                    // # of zero PV slow traders
        int nNegativeNonHFT = 1;                // # of negative PV slow traders
        double tif = 0.0;                       // time if force
        int NewNonHFT = nNegativeNonHFT + nPositiveNonHFT + nZeroNonHFT;
        double lambdaArrival = 0.1;               // arrival frequency, same for all
        double lambdaFV = 0.125;                // frequency of FV changes
        double ReturnFrequencyHFT = 10;          // returning frequency of HFT
        double ReturnFrequencyNonHFT = 1;     // returning frequency of NonHFT
        int maxDepth = 15;                      // 0 to 7 which matter
        int FVpos = nP/2;                          // position of the fundamental value
        int HL = FVpos + 3; //                  // Lowest  allowed limit order price.  LL + HL = nP-1 for allowed orders centered around E(v)
        int LL = FVpos - 3; //                  // Highest allowed limit order price
        float tickSize = 0.125f;                // size of one tick
        int end = HL - LL + 1;                  // number of position on the grid for submitting LOs
        int breakPoint = FVpos - LL; //end / 2; // breaking point for positive, negative, represents FV position on the LO grid
        double FV;
        int PVsigma = 2;                        // # of ticks for negative and positive PVs
        double sigma = 1.0;                     // volatility of FV   1/8th 1.0 and 1/16th 2.0
        double [] Prices = new double[nP]; // creates vector of the prices, not carrying about ticks now
        for (int i = 0 ; i < nP ; i++){
            Prices[i] = i * tickSize;
        }
        FV = Prices[FVpos];        // TODO: have also FV when lying not on a tick
        double FVplus = 0.5;                    // probability f FV going up
        float PVmean = 0.0f;//-0.0625f;
        float [] FprivateValues = {- PVsigma * tickSize, 0, PVsigma * tickSize};// distribution over private values
        double [] PVdistrb = new double[3];
        PVdistrb[0] = (double)nNegativeNonHFT / NewNonHFT;
        PVdistrb[1] = PVdistrb[0] + (double) nZeroNonHFT / NewNonHFT;
        PVdistrb[2] = PVdistrb[1] + (double) nPositiveNonHFT / NewNonHFT;
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
        HashMap<Integer, Trader> traders = new HashMap<Integer, Trader>(); //trader ID, trader object
        History h = new History(traders, folder); // create history
        LOB_LinkedHashMap book = new LOB_LinkedHashMap(model, FV, FVpos, maxDepth, end, tickSize, nP, h, traders);
        Trader trader = new Trader(infoSize, tauB, tauS, nP, FVpos, tickSize, ReturnFrequencyHFT,
                ReturnFrequencyNonHFT, LL, HL, end, maxDepth, breakPoint, hti, prTremble, folder, book);
        book.makeBook(Prices);
        SingleRun sr = new SingleRun(model, tif, lambdaArrival, lambdaFV, ReturnFrequencyHFT, ReturnFrequencyNonHFT,
                FprivateValues, PVdistrb, sigma, tickSize, FVplus, header, book, traders, h, trader, outputNameStatsData,
                outputNameTransactions, outputNameBookData);

        int nEvents = 500000000;         // number of events
        int ReturningHFT = 0;           // # of returning HFT traders in the book
        int ReturningNonHFT = 0;        // # of returning nonHFT traders in the book
        boolean write = false;          // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
        boolean writeDiagnostics = true;// write diagnostics controls diagnostics
        boolean writeHistogram = false; // write histogram
        boolean purge = false;          // purge in this SingleRun?
        boolean nReset = false;         // reset n in this SingleRun?
        trader.setPrTremble(0.02);
        trader.setWriteDec(false);
        trader.setWriteDiag(writeDiagnostics);
        trader.setWriteHist(writeHistogram);
        trader.setOnline(true);         // controls updating for returning trader
        trader.setFixedBeliefs(false);  // controls updating. if fixed beliefs => no updating
        double EventTime = 0.0;                 // captures time
        double[] RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                        write, purge, nReset, writeDiagnostics, writeHistogram);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];
        ReturningHFT = (int) RunOutcome[2];
        ReturningNonHFT = (int) RunOutcome[3];

        nEvents = 600000000;         // number of events
        write = false;          // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
        writeDiagnostics = true;// write diagnostics controls diagnostics
        writeHistogram = false; // write histogram
        purge = false;          // purge in this com.jakubrojcek.gpr2005a.SingleRun?
        nReset = false;         // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
        trader.setPrTremble(0.1);
        //trader.setWriteDec(false);
        trader.setWriteDiag(writeDiagnostics);
        //trader.setWriteHist(writeHistogram);
        RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                        write, purge, nReset, writeDiagnostics, writeHistogram);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];
        ReturningHFT = (int) RunOutcome[2];
        ReturningNonHFT = (int) RunOutcome[3];

        nEvents = 600000000;         // number of events
        write = false;          // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
        writeDiagnostics = true;// write diagnostics controls diagnostics
        writeHistogram = false; // write histogram
        purge = false;          // purge in this com.jakubrojcek.gpr2005a.SingleRun?
        nReset = true;         // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
        trader.setPrTremble(0.05);
        //trader.setWriteDec(false);
        trader.setWriteDiag(writeDiagnostics);
        //trader.setWriteHist(writeHistogram);
        RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                        write, purge, nReset, writeDiagnostics, writeHistogram);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];
        ReturningHFT = (int) RunOutcome[2];
        ReturningNonHFT = (int) RunOutcome[3];

        for (int i = 0; i < 25; i++){
            nEvents = 600000000;         // number of events
            write = false;          // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
            writeDiagnostics = true;// write diagnostics controls diagnostics
            writeHistogram = false; // write histogram
            purge = false;          // purge in this com.jakubrojcek.gpr2005a.SingleRun?
            nReset = true;         // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
            trader.setPrTremble(0.02);
            //trader.setWriteDec(false);
            trader.setWriteDiag(writeDiagnostics);
            //trader.setWriteHist(writeHistogram);
            //trader.setOnline(true);
            RunOutcome =
                    sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                            write, purge, nReset, writeDiagnostics, writeHistogram);
            EventTime = RunOutcome[0];
            FV = RunOutcome[1];
            ReturningHFT = (int) RunOutcome[2];
            ReturningNonHFT = (int) RunOutcome[3];
        }

        nEvents = 500000000;         // number of events
        write = true;          // writeDecisions output in this SingleRun?
        writeDiagnostics = true;// write diagnostics controls diagnostics
        writeHistogram = true; // write histogram
        purge = false;          // purge in this SingleRun?
        nReset = false;         // reset n in this SingleRun?
        trader.setPrTremble(0.0);
        trader.setWriteDec(true);
        trader.setWriteDiag(writeDiagnostics);
        trader.setWriteHist(writeHistogram);
        trader.setOnline(true);
        trader.setFixedBeliefs(true);
        RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                        write, purge, nReset, writeDiagnostics, writeHistogram);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];
        ReturningHFT = (int) RunOutcome[2];
        ReturningNonHFT = (int) RunOutcome[3];
        // occurrences Beliefs
        trader.printStatesDensity(EventTime);
        /* categories of traders: fast, slow, private value: negative, zero, positive
  that means 4 categories of traders. Fast have zero PV */
        double timeStamp2 = System.nanoTime();
        System.out.println(timeStamp2 - timeStamp1);
        // market parameters

    }
}
