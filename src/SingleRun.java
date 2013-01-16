import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Jakub
 * Date: 15.10.12
 * Time: 11:45
 * To change this template use File | Settings | File Templates.
 */
public class SingleRun {

    double lambdaArrival;
    double ReturnFrequencyHFT;
    double ReturnFrequencyNonHFT;
    double sigma;
    float tickSize;
    float[] FprivateValues;
    double FVplus;
    HashMap<Integer, Trader> traders;
    History h;
    Trader trader;
    LOB_LinkedHashMap book;

    boolean header = false;
    String outputNameTransactions;  // output file name
    String outputNameBookData;      // output file name
    String outputNameStatsData;     // output file name

    boolean write = false;          // writeDecisions output in this SingleRun?
    boolean writeDiagnostics = false;// write diagnostics
    boolean purge = false;          // purge in this SingleRun?
    boolean nReset = false;         // reset n in this SingleRun?

    double Lambda;


    public SingleRun(double lambdaArrival, double ReturnFrequencyHFT, double ReturnFrequencyNonHFT,
                     float[] FprivateValues, double sigma, float tickSize, double FVplus,
                     boolean head, LOB_LinkedHashMap b,
                     HashMap<Integer, Trader> ts, History his, Trader TR,  String stats,
                     String trans, String bookd){
        this.lambdaArrival = lambdaArrival;
        this.ReturnFrequencyHFT = ReturnFrequencyHFT;
        this.ReturnFrequencyNonHFT = ReturnFrequencyNonHFT;
        this.FprivateValues = FprivateValues;
        this.sigma = sigma;
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
    }

    public double[] run(int nEvents, int nHFT, int NewNonHFT,
                      double EventTime, double FV,
                      boolean w, boolean p, boolean n, boolean wd){
        write = w;
        writeDiagnostics = wd;
        purge = p;
        nReset = n;
        int lastEvent = 0;                         // TODO: delete afterwards
        byte lastAction = 0;                        // TODO: delete afterwards
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

            double prob1 = (double) nHFT / nAll * 0.92;
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
                tr = new Trader(true, 0);
                ID = tr.getTraderID();
                /* if(book.removedTraders.contains(ID)){
                     System.out.println("new Zombie HFT");
                 }*/
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
                /* if(book.removedTraders.contains(ID)){
                     System.out.println("new zombie nonHFT");
                 }*/

                //System.out.println("New arrival nonHFT ID: " + ID);
                traders.put(ID, tr);
                PriceOrder PO = tr.decision(book.getRank(ID), book.getBookSizes(), book.getBookInfo(),
                        EventTime, FV);

                if (PO != null){
                    book.transactionRule(PO.getPrice() , PO.getCurrentOrder());
                } else {book.addTrader(ID);}
                lastEvent = 2;
                lastAction = tr.getOldAction();
            } else if (rn < x3){                   // Returning HFT
                ID = book.randomHFTtraderID();
                //System.out.println("Returning HFT ID: " + ID);
                /* if(book.removedTraders.contains(ID)){
                     System.out.println("old Zombie HFT");
                 }*/
                PriceOrder PO = traders.get(ID).decision(book.getRank(ID), book.getBookSizes(), book.getBookInfo(),
                        EventTime, FV);
                if (PO != null){
                    book.transactionRule(PO.getPrice() , PO.getCurrentOrder());
                } else {
                    book.tryCancel(ID);
                }

            } else if (rn < x4){                   // Returning nonHFT
                ID = book.randomNonHFTtraderID();
                /*if(book.removedTraders.contains(ID)){
                    System.out.println("old Zombie nonHFT");
                }*/
                //System.out.println("Returning nonHFT ID: " + ID);
                boolean test;
                test = traders.get(ID).getIsTraded();
                LinkedHashMap<Integer, Order>[] book2 = book.book;
                int[] bs = book.getBookSizes();
                int[] bi = book.getBookInfo();
                int[] bi2 = getBookInfo(bs);

                int At;            //TODO: delete afterwards
                int Bt;
                int j = 0;
                while (bs[j] >= 0 && j < 8){
                    j++;
                }
                At = j;
                j = 8;
                while (bs[j] <= 0 && j > 0){
                    j--;
                }
                Bt = j;
                System.out.println("Best bid: " + Bt + " Best ask: " + At);
                PriceOrder PO = traders.get(ID).decision(book.getRank(ID), book.getBookSizes(), book.getBookInfo(),
                        EventTime, FV);

                if (PO != null){
                    book.transactionRule(PO.getPrice() , PO.getCurrentOrder());
                } else {
                    book.tryCancel(ID);
                }

                int[] bi3 = getBookInfo(bs);

                if (traders.containsKey(ID)){
                    if(book.book[PO.getPrice()].containsKey(ID) && traders.get(ID).getIsTraded()){
                        System.out.println("problem");
                    }
                }
                lastEvent = 4;
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
                lastEvent = 5;
            }
            h.addOrderData(book.getBestBid(), book.getBestAsk());
            if (i % 100000 == 0) {
                h.addStatisticsData(i, trader.getStatesCount());   // multiple payoffs count
                if (writeDiagnostics){
                    trader.printDiagnostics();
                    trader.resetDiagnostics();
                }
                if (write){
                    h.printTransactions(header, outputNameTransactions);
                    h.printBookData(header, outputNameBookData);
                    trader.printDecisions();
                    trader.printHistogram();
                    //trader.printDiagnostics();
                    trader.resetDecisionHistory();
                    trader.resetHistogram();
                    //trader.resetDiagnostics();
                }
                h.printStatisticsData(header, outputNameStatsData);
                h.resetHistory();

            }   //where events happen
            if (i % 300000000 == 0) {
                if (purge){
                    trader.purge();
                }
                if (nReset){
                    trader.nReset((byte)3, (short) 10);
                }
            }
        }
        return new double[]{EventTime, FV};
    }

    public int[] getBookInfo(int[] BookSizes){     //TODO:delete afterwards
        /* (best Bid, Ask), (depth at B, A), (depth Buy, Sell),
        (last price, WasBuy) */
        int[] BookInfo = new int[8];
        int nPoints = 9;
        int Bt;         // best bid position in Prices
        int At;         // best ask position in Prices

        int j = 0;
        while (BookSizes[j] >= 0 && j < 8){
            j++;
        }
        At = j;
        j = 8;
        while (BookSizes[j] <= 0 && j > 0){
            j--;
        }
        Bt = j;

        BookInfo[0] = Bt;                 // best bid position
        BookInfo[1] = At;                 // best ask position

        return BookInfo;
    }
}
