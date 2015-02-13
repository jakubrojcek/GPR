import com.jakubrojcek.hftRegulation.History;
import com.jakubrojcek.hftRegulation.LOB_LinkedHashMap;
import com.jakubrojcek.hftRegulation.SingleRun;
import com.jakubrojcek.hftRegulation.Trader;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Created by rojcek on 20.12.13.
 */
public class SingleCase {

    public boolean main(String[] args) {
        double timeStamp1 = System.nanoTime();
        String mode = args[14];
        int model = 0;
        if (mode.equals("returning")){
            model = 0;
        } else if (mode.equals("speedBump")) {
            model = 1;
        } else if (mode.equals("transparency")) {
            model = 2;
        }

        Path dir = Paths.get("D:\\_paper1 HFT, MM, rebates and market quality\\Matlab Analysis" + File.separator + args[0]);
        //Path dir = Paths.get("." + File.separator + args[0]);
        if (Files.exists(dir)){
            System.out.println("Directory already exists");
            return false;
        } else {
            try {
                Files.createDirectory(dir);
            } catch (Exception e){
                System.out.println("Could not create directory");
            }
        }
        String folder = dir.getParent()  + File.separator + args[0];
        //String folder = "D:\\_paper1 HFT, MM, rebates and market quality\\Matlab Analysis\\" + args[0];

        String outputNameTransactions = "Transactions8.csv";  // output file name
        String outputNameBookData = "effSpread.csv";   // output file name
        String outputNameStatsData = "stats8.csv";   // output file name
        boolean header = false;                 // header yes ?
        int hti = 5000000;                      // initial capacity for Payoffs HashTable
        int infoSize = Integer.parseInt(args[17]); // 2-bid, ask, 5- GPR 2005, 6-depth at bid,ask, 8-depth off bid,ask
        double prTremble = 0.0;                 // probability of trembling
        byte nP = Byte.parseByte(args[16]);     // number of prices tracked by the book, 8 in the base case, 6/11 in tick size experiment

        /*double nHFT = Double.parseDouble(args[1]);   // % of HFTs from 0 PV traders
        double nPositiveNonHFT = Double.parseDouble(args[2]);   // % of |0 PV| traders
        double nZeroNonHFT = Double.parseDouble(args[3]);   // % of |2 PV| traders
        double nNegativeNonHFT = Double.parseDouble(args[4]);   // % of |4 PV| traders
        double [] PVdistrb = {.15, .35, .65, .85, 1.0};*/

        double PercHFT = Double.parseDouble(args[1]);   // % of HFTs from 0 PV traders
        double Perc0PV = Double.parseDouble(args[2]);   // % of |0 PV| traders
        double Perc2PV = Double.parseDouble(args[3]);   // % of |2 PV| traders
        double Perc4PV = Double.parseDouble(args[4]);   // % of |4 PV| traders

        /*double nHFT = 1.0;   // % of HFTs from 0 PV traders
        double nPositiveNonHFT = 1.0;   // % of |0 PV| traders
        double nZeroNonHFT = 2.0;   // % of |2 PV| traders
        double nNegativeNonHFT = 1.0;   // % of |4 PV| traders*/
        double nHFT = Math.round(PercHFT * Perc0PV * 100000000.0)/ 100000000.0;            // # of HFT's fast traders, fixed
        double nPositiveNonHFT = Math.round((Perc2PV + Perc4PV) / 2.0 * 100000000.0)/ 100000000.0;           // # of positive PV slow traders
        double nZeroNonHFT = Math.round(Perc0PV * (1 - PercHFT) * 100000000.0)/ 100000000.0;               // # of zero PV slow traders
        double nNegativeNonHFT = Math.round((Perc2PV + Perc4PV) / 2.0 * 100000000.0)/ 100000000.0;                // # of negative PV slow traders

        double [] PVdistrb = new double[5];
        double xFactor = Math.round(1.0 / (Perc0PV * (1.0 - PercHFT) + Perc2PV + Perc4PV) * 100000000.0)/100000000.0; // brings nonHFT PVdistrib back to 1.0
        PVdistrb[0] = Math.round(xFactor * Perc4PV / 2.0 * 100000000.0)/100000000.0;
        PVdistrb[1] = PVdistrb[0] + Math.round(xFactor * Perc2PV / 2.0 * 100000000.0)/100000000.0;
        PVdistrb[4] = 1.0;
        PVdistrb[3] = 1.0 - PVdistrb[0];
        PVdistrb[2] = 1.0 - PVdistrb[1];

        /*double nHFT = PercHFT * Perc0PV * 5.0;            // # of HFT's fast traders, fixed
        double nPositiveNonHFT = (Perc2PV + Perc4PV) / 2.0 * 5.0;           // # of positive PV slow traders
        double nZeroNonHFT = Perc0PV * (1 - PercHFT) * 5.0;               // # of zero PV slow traders
        double nNegativeNonHFT = (Perc2PV + Perc4PV) / 2.0 * 5.0;                // # of negative PV slow traders

        double [] PVdistrb = new double[5];
        double xFactor = 1.0 / (Perc0PV * (1.0 - PercHFT) + Perc2PV + Perc4PV); // brings nonHFT PVdistrib back to 1.0
        PVdistrb[0] = xFactor * Perc4PV / 2;
        PVdistrb[1] = PVdistrb[0] + xFactor * Perc2PV / 2;
        PVdistrb[2] = PVdistrb[1] + xFactor * Perc0PV * (1 - PercHFT);
        PVdistrb[3] = PVdistrb[2] + xFactor * Perc2PV / 2;
        PVdistrb[4] = PVdistrb[3] + xFactor * Perc4PV / 2;*/

        int eventScale = Integer.parseInt(args[20]);                    // 1000-> 15bn, 100-> 1.5bn, 10-> 150m, 1->15m events
        double tif = Double.parseDouble(args[5]);                       // time if force
        double TTAX = Double.parseDouble(args[6]);                      // transaction tax
        double CFEE = Double.parseDouble(args[7]);                      // cancellation fee
        double MFEE = Double.parseDouble(args[8]);                      // LO make fee
        double TFEE = Double.parseDouble(args[9]);                      // MO take fee
        float rho = Float.parseFloat(args[11]);                         // impatience parameter
        double sb = Double.parseDouble(args[15]);                       // speed bump length
        double NewNonHFT = nNegativeNonHFT + nPositiveNonHFT + nZeroNonHFT;
        double lambdaArrival = Double.parseDouble(args[10]);             // arrival frequency, same for all
        double lambdaFV = Double.parseDouble(args[12]);                  // frequency of FV changes
        double infoDelay = Double.parseDouble(args[19]);                 // information delay of uninformed traders
        double transparencyPeriod = Double.parseDouble(args[21]);        // period for transparency
        double ReturnFrequencyHFT = 4.0;//8.3;          // returning frequency of HFT  //
        double ReturnFrequencyNonHFT = 1.0;//1.67;     // returning frequency of NonHFT //
        int maxDepth = Integer.parseInt(args[13]);// 0 to 7 which matter
        int FVpos = nP/2;                          // position of the fundamental value
        int range = (nP == 31) ?                    // range of prices on the grid +/-
                             6 : 3;
        int HL = FVpos + range; //                 // Lowest  allowed limit order price.  LL + HL = nP-1 for allowed orders centered around E(v)
        int LL = FVpos - range; //                  // Highest allowed limit order price
        float tickSize = 0.125f;                // size of one tick
        int end = HL - LL + 1;                  // number of position on the grid for submitting LOs
        int breakPoint = FVpos - LL; //end / 2; // breaking point for positive, negative, represents FV position on the LO grid
        double FV;
        int PVsigma = Integer.parseInt(args[18]);   // # of ticks for negative and positive PVs
        double sigma = 1.0;                         // volatility of FV   1/8th 1.0 and 1/16th 2.0
        double [] Prices = new double[nP];          // creates vector of the prices, not carrying about ticks now
        for (int i = 0 ; i < nP ; i++){
            Prices[i] = i * tickSize;
        }
        FV = Prices[FVpos];
        double FVplus = 0.5;                    // probability f FV going up
        float PVmean = 0.0f;//-0.0625f;
        //float [] FprivateValues = {- PVsigma * tickSize, 0, PVsigma * tickSize};// distribution over private values
        double  [] FprivateValues = {- 2 * PVsigma * tickSize, - PVsigma * tickSize, 0,
                PVsigma * tickSize, 2 * PVsigma * tickSize};// distribution over private values

        //double [] PVdistrb = {.134, .311, .689, .866, 1.0};
        //double [] PVdistrb = {.0, .0, 1.0, 1.0, 1.0};
        //double [] PVdistrb = {.2143, .5, .5, .7857, 1.0};
        /*double [] PVdistrb = new double[3];
        PVdistrb[0] = (double)nNegativeNonHFT / NewNonHFT;
        PVdistrb[1] = PVdistrb[0] + (double) nZeroNonHFT / NewNonHFT;
        PVdistrb[2] = PVdistrb[1] + (double) nPositiveNonHFT / NewNonHFT;*/
        double[] tauB = new double[end];
        /* expected time until the arrival of a new buyer for whom trading on
        the LO yields non-negative payoff */
        double[] tauS = new double[end];
        /* expected time until the arrival of a new seller for whom picking up
        the LO yields non-negative payoff */
        for (int i = 0; i < end; i++){
            double denomB = 0.0; // denominator buyers
            double denomS = 0.0; // denominator sellers
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
        History h = new History(traders, folder, TTAX, CFEE, MFEE, TFEE); // create history
        LOB_LinkedHashMap book = new LOB_LinkedHashMap(model, FV, FVpos, maxDepth, end, tickSize, nP, h, traders);
        Trader trader = new Trader(infoSize, tauB, tauS, nP, FVpos, tickSize, ReturnFrequencyHFT,
                ReturnFrequencyNonHFT, LL, HL, end, maxDepth, breakPoint, hti, prTremble, folder, book, FprivateValues,
                rho, TTAX, CFEE, MFEE, TFEE, model, sb, transparencyPeriod);
        book.makeBook(Prices);
        SingleRun sr = new SingleRun(model, tif, lambdaArrival, lambdaFV, ReturnFrequencyHFT, ReturnFrequencyNonHFT,
                FprivateValues, PVdistrb, sigma, tickSize, FVplus, header, book, traders, h, trader, outputNameStatsData,
                outputNameTransactions, outputNameBookData, sb, end, infoDelay, transparencyPeriod);

        // phase 1a) initialization
        int nEvents = 500000 * eventScale;         // number of events
        int ReturningHFT = 0;           // # of returning HFT traders in the book
        int ReturningNonHFT = 0;        // # of returning nonHFT traders in the book
        boolean write = false;          // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
        boolean writeDiagnostics = true;// write diagnostics controls diagnostics
        boolean writeHistogram = true; // write histogram
        boolean purge = false;          // purge in this SingleRun?
        boolean nReset = false;         // reset n in this SingleRun?
        String convergence = "none";    // computing convergence, "none", "convergenceSecond.csv", "convergence.csv"?
        trader.setPrTremble(0.005);
        trader.setWriteDec(false);
        trader.setWriteDiag(writeDiagnostics);
        trader.setWriteHist(writeHistogram);
        trader.setOnline(true);         // controls updating for returning trader
        trader.setFixedBeliefs(false);  // controls updating. if fixed beliefs => no updating
        trader.setSimilar(false);       // controls if beliefs for a state not present, looks for similar state belief
        double EventTime = 0.0;                 // captures time
        double[] RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                        write, purge, nReset, writeDiagnostics, writeHistogram, convergence);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];
        ReturningHFT = (int) RunOutcome[2];
        ReturningNonHFT = (int) RunOutcome[3];

        if (CFEE != 0.0 || sb != 0.0 || model == 2){               // collect initial beliefs and restart
            trader.computeInitialBeliefs(CFEE, sb);
            nEvents = 100000 * eventScale;         // number of events
            write = false;          // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
            writeDiagnostics = true;// write diagnostics controls diagnostics
            writeHistogram = true; // write histogram
            purge = false;          // purge in this com.jakubrojcek.gpr2005a.SingleRun?
            nReset = true;         // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
            trader.setPrTremble(0.0025);
            //trader.setWriteDec(false);
            trader.setWriteDiag(writeDiagnostics);
            trader.setWriteHist(writeHistogram);
            RunOutcome =
                    sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                            write, purge, nReset, writeDiagnostics, writeHistogram, convergence);
            EventTime = RunOutcome[0];
            FV = RunOutcome[1];
            ReturningHFT = (int) RunOutcome[2];
            ReturningNonHFT = (int) RunOutcome[3];
        }

        nEvents = 1000000 * eventScale;         // number of events
        write = false;          // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
        writeDiagnostics = true;// write diagnostics controls diagnostics
        writeHistogram = true; // write histogram
        purge = false;          // purge in this com.jakubrojcek.gpr2005a.SingleRun?
        nReset = false;         // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
        trader.setPrTremble(0.0018);
        //trader.setWriteDec(false);
        trader.setWriteDiag(writeDiagnostics);
        trader.setWriteHist(writeHistogram);
        RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                        write, purge, nReset, writeDiagnostics, writeHistogram, convergence);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];
        ReturningHFT = (int) RunOutcome[2];
        ReturningNonHFT = (int) RunOutcome[3];

        nEvents = 1000000 * eventScale;         // number of events
        write = false;          // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
        writeDiagnostics = true;// write diagnostics controls diagnostics
        writeHistogram = true; // write histogram
        purge = false;          // purge in this com.jakubrojcek.gpr2005a.SingleRun?
        nReset = false;         // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
        trader.setPrTremble(0.0015);
        //trader.setWriteDec(false);
        trader.setWriteDiag(writeDiagnostics);
        trader.setWriteHist(writeHistogram);
        RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                        write, purge, nReset, writeDiagnostics, writeHistogram, convergence);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];
        ReturningHFT = (int) RunOutcome[2];
        ReturningNonHFT = (int) RunOutcome[3];

        nEvents = 1000000 * eventScale;         // number of events
        write = false;          // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
        writeDiagnostics = true;// write diagnostics controls diagnostics
        writeHistogram = true; // write histogram
        purge = false;          // purge in this com.jakubrojcek.gpr2005a.SingleRun?
        nReset = true;         // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
        trader.setPrTremble(0.001);
        //trader.setWriteDec(false);
        trader.setWriteDiag(writeDiagnostics);
        //trader.setWriteHist(writeHistogram);
        RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                        write, purge, nReset, writeDiagnostics, writeHistogram, convergence);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];
        ReturningHFT = (int) RunOutcome[2];
        ReturningNonHFT = (int) RunOutcome[3];

        nEvents = 1000000 * eventScale;         // number of events
        write = false;          // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
        writeDiagnostics = true;// write diagnostics controls diagnostics
        writeHistogram = true; // write histogram
        purge = false;          // purge in this com.jakubrojcek.gpr2005a.SingleRun?
        nReset = true;         // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
        trader.setPrTremble(0.0008);
        //trader.setWriteDec(false);
        trader.setWriteDiag(writeDiagnostics);
        //trader.setWriteHist(writeHistogram);
        RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                        write, purge, nReset, writeDiagnostics, writeHistogram, convergence);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];
        ReturningHFT = (int) RunOutcome[2];
        ReturningNonHFT = (int) RunOutcome[3];

        nEvents = 1000000 * eventScale;         // number of events
        write = false;          // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
        writeDiagnostics = true;// write diagnostics controls diagnostics
        writeHistogram = true; // write histogram
        purge = false;          // purge in this com.jakubrojcek.gpr2005a.SingleRun?
        nReset = true;         // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
        trader.setPrTremble(0.0004);
        //trader.setWriteDec(false);
        trader.setWriteDiag(writeDiagnostics);
        //trader.setWriteHist(writeHistogram);
        RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                        write, purge, nReset, writeDiagnostics, writeHistogram, convergence);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];
        ReturningHFT = (int) RunOutcome[2];
        ReturningNonHFT = (int) RunOutcome[3];

        nEvents = 1000000 * eventScale;         // number of events
        write = false;          // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
        writeDiagnostics = true;// write diagnostics controls diagnostics
        writeHistogram = true; // write histogram
        purge = false;          // purge in this com.jakubrojcek.gpr2005a.SingleRun?
        nReset = true;         // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
        trader.setPrTremble(0.0003);
        //trader.setWriteDec(false);
        trader.setWriteDiag(writeDiagnostics);
        //trader.setWriteHist(writeHistogram);
        RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                        write, purge, nReset, writeDiagnostics, writeHistogram, convergence);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];
        ReturningHFT = (int) RunOutcome[2];
        ReturningNonHFT = (int) RunOutcome[3];

        nEvents = 1000000 * eventScale;         // number of events
        write = false;          // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
        writeDiagnostics = true;// write diagnostics controls diagnostics
        writeHistogram = true; // write histogram
        purge = false;          // purge in this com.jakubrojcek.gpr2005a.SingleRun?
        nReset = true;         // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
        trader.setPrTremble(0.0002);
        //trader.setWriteDec(false);
        trader.setWriteDiag(writeDiagnostics);
        //trader.setWriteHist(writeHistogram);
        RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                        write, purge, nReset, writeDiagnostics, writeHistogram, convergence);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];
        ReturningHFT = (int) RunOutcome[2];
        ReturningNonHFT = (int) RunOutcome[3];
        // phase 1b) extensive simulation and learning
        for (int i = 0; i < 3; i++){
            nEvents = 1000000 * eventScale;         // number of events
            write = false;          // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
            writeDiagnostics = true;// write diagnostics controls diagnostics
            writeHistogram = true; // write histogram
            if (i % 5 == 0) {
                purge = true;      // purge occasionally in this phase
            } else {
                purge = false;
            }
            purge = false;
            nReset = true;         // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
            trader.setPrTremble(0.00015);
            //trader.setWriteDec(false);
            trader.setWriteDiag(writeDiagnostics);
            trader.setWriteHist(writeHistogram);
            //trader.setOnline(true);
            RunOutcome =
                    sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                            write, purge, nReset, writeDiagnostics, writeHistogram, convergence);
            EventTime = RunOutcome[0];
            FV = RunOutcome[1];
            ReturningHFT = (int) RunOutcome[2];
            ReturningNonHFT = (int) RunOutcome[3];
        }


        nEvents = 700000 * eventScale;         // number of events
        write = false;          // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
        writeDiagnostics = true;// write diagnostics controls diagnostics
        writeHistogram = true; // write histogram
        purge = false;          // purge in this com.jakubrojcek.gpr2005a.SingleRun?
        nReset = true;         // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
        //trader.setPrTremble(0.0);
        trader.setPrTremble(0.00012);
        //trader.setWriteDec(false);
        trader.setWriteDiag(writeDiagnostics);
        //trader.setWriteHist(writeHistogram);
        RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                        write, purge, nReset, writeDiagnostics, writeHistogram, convergence);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];
        ReturningHFT = (int) RunOutcome[2];
        ReturningNonHFT = (int) RunOutcome[3];

        /*nEvents = 10000000;         // number of events
        write = false;          // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
        writeDiagnostics = true;// write diagnostics controls diagnostics
        writeHistogram = true; // write histogram
        purge = false;          // purge in this com.jakubrojcek.gpr2005a.SingleRun?
        nReset = true;         // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
        trader.setPrTremble(0.0006);
        //trader.setWriteDec(false);
        trader.setWriteDiag(writeDiagnostics);
        //trader.setWriteHist(writeHistogram);
        RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                        write, purge, nReset, writeDiagnostics, writeHistogram, convergence);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];
        ReturningHFT = (int) RunOutcome[2];
        ReturningNonHFT = (int) RunOutcome[3];*/

        // phase 2a) less extensive simulation, checking for convergence of type 1
        for (int i = 0; i < 2; i++){    // outer loop for convergence type 1
            nEvents = 800000 * eventScale;         // number of events
            write = false;          // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
            writeDiagnostics = true;// write diagnostics controls diagnostics
            writeHistogram = true; // write histogram
            purge = false;          // purge in this com.jakubrojcek.gpr2005a.SingleRun?
            nReset = false;         // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
            convergence = "convergence.csv";    // computing convergence, "none", "convergenceSecond.csv", "convergence.csv"?
            trader.setPrTremble(0.0001);
            trader.setWriteDec(true);
            trader.setWriteDiag(writeDiagnostics);
            //trader.setWriteHist(writeHistogram);
            trader.setOnline(true);         // controls updating for returning trader
            trader.setFixedBeliefs(false);  // controls updating. if fixed beliefs => no updating
            trader.setSimilar(false);
            RunOutcome =
                    sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                            write, purge, nReset, writeDiagnostics, writeHistogram, convergence);
            EventTime = RunOutcome[0];
            FV = RunOutcome[1];
            ReturningHFT = (int) RunOutcome[2];
            ReturningNonHFT = (int) RunOutcome[3];

            // phase 2b) checking for convergence of type 2
            if (RunOutcome[4] < 0.01){          // type 1 converged, check for type 2
                nEvents = 30000 * eventScale;         // number of events
                write = false;          // writeDecisions output in this SingleRun?
                writeDiagnostics = true;// write diagnostics controls diagnostics
                writeHistogram = true; // write histogram
                purge = false;          // purge in this SingleRun?
                nReset = false;         // reset n in this SingleRun?
                convergence = "convergenceSecond.csv";    // computing convergence, "none", "convergenceSecond.csv", "convergence.csv"?
                trader.setPrTremble(0.0);
                //trader.setWriteDec(true);
                //trader.setWriteDiag(writeDiagnostics);
                //trader.setWriteHist(writeHistogram);
                trader.setOnline(true);
                trader.setFixedBeliefs(true);
                trader.setSimilar(true);       // controls if beliefs for a state not present, looks for similar state belief

                RunOutcome =
                        sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                                write, purge, nReset, writeDiagnostics, writeHistogram, convergence);
                EventTime = RunOutcome[0];
                FV = RunOutcome[1];
                ReturningHFT = (int) RunOutcome[2];
                ReturningNonHFT = (int) RunOutcome[3];
                if (RunOutcome[4] < 0.01){
                    break;
                }
            }
        }

        nEvents = 1000000 * eventScale;         // number of events
        write = false;          // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
        writeDiagnostics = true;// write diagnostics controls diagnostics
        writeHistogram = true; // write histogram
        purge = false;          // purge in this com.jakubrojcek.gpr2005a.SingleRun?
        nReset = true;         // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
        convergence = "none";    // computing convergence, "none", "convergenceSecond.csv", "convergence.csv"?
        //trader.setPrTremble(0.0);
        trader.setPrTremble(0.0001);
        //trader.setWriteDec(false);
        trader.setWriteDiag(writeDiagnostics);
        //trader.setWriteHist(writeHistogram);
        trader.setOnline(true);         // controls updating for returning trader
        trader.setFixedBeliefs(false);  // controls updating. if fixed beliefs => no updating
        trader.setSimilar(false);
        RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                        write, purge, nReset, writeDiagnostics, writeHistogram, convergence);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];
        ReturningHFT = (int) RunOutcome[2];
        ReturningNonHFT = (int) RunOutcome[3];

        nEvents = 100000 * eventScale;         // number of events
        write = false;          // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
        writeDiagnostics = true;// write diagnostics controls diagnostics
        writeHistogram = true; // write histogram
        purge = false;          // purge in this com.jakubrojcek.gpr2005a.SingleRun?
        nReset = true;         // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
        convergence = "none";    // computing convergence, "none", "convergenceSecond.csv", "convergence.csv"?
        //trader.setPrTremble(0.0);
        trader.setPrTremble(0.00005);
        //trader.setWriteDec(false);
        trader.setWriteDiag(writeDiagnostics);
        //trader.setWriteHist(writeHistogram);
        trader.setOnline(true);         // controls updating for returning trader
        trader.setFixedBeliefs(false);  // controls updating. if fixed beliefs => no updating
        trader.setSimilar(false);
        RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                        write, purge, nReset, writeDiagnostics, writeHistogram, convergence);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];
        ReturningHFT = (int) RunOutcome[2];
        ReturningNonHFT = (int) RunOutcome[3];

        nEvents = 100000 * eventScale;         // number of events
        write = false;          // writeDecisions output in this com.jakubrojcek.gpr2005a.SingleRun?
        writeDiagnostics = true;// write diagnostics controls diagnostics
        writeHistogram = true; // write histogram
        purge = false;          // purge in this com.jakubrojcek.gpr2005a.SingleRun?
        nReset = true;         // reset n in this com.jakubrojcek.gpr2005a.SingleRun?
        convergence = "none";    // computing convergence, "none", "convergenceSecond.csv", "convergence.csv"?
        //trader.setPrTremble(0.0);
        trader.setPrTremble(0.000025);
        //trader.setWriteDec(false);
        trader.setWriteDiag(writeDiagnostics);
        //trader.setWriteHist(writeHistogram);
        trader.setOnline(true);         // controls updating for returning trader
        trader.setFixedBeliefs(false);  // controls updating. if fixed beliefs => no updating
        trader.setSimilar(false);
        RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                        write, purge, nReset, writeDiagnostics, writeHistogram, convergence);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];
        ReturningHFT = (int) RunOutcome[2];
        ReturningNonHFT = (int) RunOutcome[3];

        // phase 3) simulating from the equilibrium
        int traderCountStart = trader.getTraderCount();
        int traderCountHFTstart = trader.getTraderCountHFT();
        int traderCountNonHFTstart = trader.getTraderCountNonHFT();
        nEvents = 30000 * eventScale;         // number of events
        write = true;          // writeDecisions output in this SingleRun?
        writeDiagnostics = true;// write diagnostics controls diagnostics
        writeHistogram = true; // write histogram
        purge = false;          // purge in this SingleRun?
        nReset = false;         // reset n in this SingleRun?
        convergence = "convergenceSecond.csv";    // computing convergence, "none", "convergenceSecond.csv", "convergence.csv"?
        trader.setPrTremble(0.0);
        trader.setWriteDec(true);
        trader.setWriteDiag(writeDiagnostics);
        trader.setWriteHist(writeHistogram);
        trader.setOnline(true);
        trader.setFixedBeliefs(true);
        trader.setSimilar(true);       // controls if beliefs for a state not present, looks for similar state belief

        RunOutcome =
                sr.run(nEvents, nHFT, NewNonHFT, ReturningHFT, ReturningNonHFT, EventTime, FV,
                        write, purge, nReset, writeDiagnostics, writeHistogram, convergence);
        EventTime = RunOutcome[0];
        FV = RunOutcome[1];
        ReturningHFT = (int) RunOutcome[2];
        ReturningNonHFT = (int) RunOutcome[3];
        // occurrences Beliefs
        //trader.printStatesDensity(EventTime);
        int traderCountEnd = trader.getTraderCount();
        int traderCountHFTend = trader.getTraderCountHFT();
        int traderCountNonHFTend = trader.getTraderCountNonHFT();
        /* categories of traders: fast, slow, private value: negative, zero, positive
  that means 4 categories of traders. Fast have zero PV */
        double timeStamp2 = System.nanoTime();
        System.out.println(timeStamp2 - timeStamp1);
        System.out.println(traderCountEnd - traderCountStart);
        System.out.println(folder);
        // market parameters
        try{
            String outputFileName = folder + "_params.csv";
            FileWriter writer = new FileWriter(outputFileName, true);
            writer.write("infoSize:" + ";" + infoSize + ";" + "\r");
            writer.write("nP:" + ";" + nP + ";" + "\r");
            writer.write("gridN:" + ";" + end + ";" + "\r");
            writer.write("nHFT:" + ";" + nHFT + ";" + "\r");
            writer.write("nALL:" + ";" + NewNonHFT + ";" + "\r");
            writer.write("lambdaArrival:" + ";" + lambdaArrival + ";" + "\r");
            writer.write("lambdaFV:" + ";" + lambdaFV + ";" + "\r");
            writer.write("ReturnFrequencyHFT:" + ";" + ReturnFrequencyHFT + ";" + "\r");
            writer.write("ReturnFrequencyNonHFT:" + ";" + ReturnFrequencyNonHFT + ";" + "\r");
            writer.write("TIF:" + ";" + tif + ";" + "\r");
            writer.write("tickSize:" + ";" + tickSize + ";" + "\r");
            writer.write("sigma:" + ";" + sigma + ";" + "\r");
            writer.write("newTraders:" + ";" + (traderCountEnd - traderCountStart) + ";" + "\r");
            writer.write("newHFTtrades:" + ";" + (traderCountHFTend - traderCountHFTstart) + ";" + "\r");
            writer.write("newNonHFTtrades:" + ";" + (traderCountNonHFTend - traderCountNonHFTstart) + ";" + "\r");
            writer.write("timeElapsed:" + ";" + (timeStamp2 - timeStamp1) + ";" + "\r");
            writer.write("MFEE:" + ";" + MFEE + ";" + "\r");
            writer.write("TFEE:" + ";" + TFEE + ";" + "\r");
            writer.write("rho:" + ";" + rho + ";" + "\r");
            writer.write("speedBump:" + ";" + sb + ";" + "\r");
            writer.write("model:" + ";" + model + ";" + "\r");
            writer.write("CFEE:" + ";" + CFEE + ";" + "\r");
            writer.write("pvSigma:" + ";" + PVsigma + ";" + "\r");
            writer.write("delay:" + ";" + infoDelay + ";" + "\r");
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        return true;    // returns true when finished

    }
}
