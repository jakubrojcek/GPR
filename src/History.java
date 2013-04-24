import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Jakub
 * Date: 16.3.12
 * Time: 11:16
 * To change this template use File | Settings | File Templates.
 */
public class History {
    HashMap<Integer, Trader> traders;
    ArrayList<Integer> Bids;
    ArrayList<Integer> Asks;
    ArrayList<Integer> Events;
    ArrayList<Integer> States;
    ArrayList<Double> EffSpread;
    ArrayList<Integer> QuotedSpread;
    Vector<Trade> history;
    String folder;

    public History(HashMap<Integer, Trader> t, String f){
        traders = t;
        Asks = new ArrayList<Integer>();
        Bids = new ArrayList<Integer>();
        EffSpread = new ArrayList<Double>();
        QuotedSpread = new ArrayList<Integer>();
        Events = new ArrayList<Integer>();
        States = new ArrayList<Integer>();
        history = new Vector<Trade>();
        folder = f;
    }
    
    public void addTrade(Order buy, Order sell, double timeTrade, double price,
                         double fv){
        int bID = buy.getTraderID();
        int sID = sell.getTraderID();
        history.add(new Trade(bID, traders.get(bID).getPrivateValue(), traders.get(bID).getIsHFT(),
                sID, traders.get(sID).getPrivateValue(), traders.get(sID).getIsHFT(),
                buy.getTimeStamp(), sell.getTimeStamp(), timeTrade, price, fv));
    }

    public void addOrderData(Double spread){
        EffSpread.add(spread);
    }

    public void addOrderData(Integer qspread){
        QuotedSpread.add(qspread);
    }

    public void addStatisticsData(int eventNum, int statesNum){
        Events.add(eventNum);
        States.add(statesNum);
    }


    public void printTransactions(boolean writeHeader, String fileNameTransactions){
        try{
            String outputFileName = folder + fileNameTransactions;
            FileWriter writer = new FileWriter(outputFileName, true);
            if (writeHeader){
                writer.write("buyerID;buyerPV;buyerIsHFT;sellerID;sellerPV;sellerIsHFT;timeBuyer;timeSeller;timeTrade;price;FV;" + "\r");
            }
            int sz = history.size();
            double mo;                               // payoff from market order
            double lo;                               // payoff from limit order
            int moHFT;                               // is the MO submitter HFT
            int loHFT;                               // is the LO submitter HFT
            for (int i = 0; i < sz; i++){
                Trade t = history.get(i);
                if (t.getTimeBuyer() < t.getTimeSeller()){           // distinguishing if seller initiated
                    mo = t.getPrice() - t.getSellerPV() - t.getFV();
                    moHFT = t.isSellerIsHFT() ? 1 : 0;
                    lo = t.getFV() + t.getBuyerPV() - t.getPrice();
                    loHFT = t.isBuyerIsHFT() ? 1 : 0;
                } else {
                    mo = t.getFV() + t.getBuyerPV() - t.getPrice();
                    moHFT = t.isBuyerIsHFT() ? 1 : 0;
                    lo = t.getPrice() - t.getSellerPV() - t.getFV();
                    loHFT = t.isSellerIsHFT() ? 1 : 0;
                }
                //writer.writeDecisions(history.get(i).printTrade());
                writer.write(moHFT + ";" + mo + ";" + loHFT + ";" + lo + ";" + (t.getFV() - t.getPrice()) + ";" + "\r");
            }
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void printBookData(boolean writeHeader, String fileNameBookData){
        try{
            String outputFileName = folder + fileNameBookData;
            FileWriter writer = new FileWriter(outputFileName, true);
            if (writeHeader){
                writer.write("EffSpread;");
            }
            int sz = EffSpread.size();
            for (int i = 0; i < sz; i++){
                writer.write(EffSpread.get(i) + ";" + "\r");
            }
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        try{
            String outputFileName = folder + "QuotedSpread.csv";
            FileWriter writer = new FileWriter(outputFileName, true);
            if (writeHeader){
                writer.write("QuotedSpread;");
            }
            int sz = QuotedSpread.size();
            for (int i = 0; i < sz; i++){
                writer.write(QuotedSpread.get(i) + ";" + "\r");
            }
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void printStatisticsData(boolean writeHeader, String fileNameStatisticsData){
        try{
            String outputFileName = folder + fileNameStatisticsData;
            FileWriter writer = new FileWriter(outputFileName, true);
            if (writeHeader){
                writer.write("Events;States;");
            }
            int sz = Events.size();
            for (int i = 0; i < sz; i++){
                writer.write(Events.get(i) + ";" + States.get(i) + ";" + "\r");
            }
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }



    public int historySize(){
        return history.size();
    }

    public void resetHistory(){
        Asks = new ArrayList<Integer>();
        Bids = new ArrayList<Integer>();
        Events = new ArrayList<Integer>();
        States = new ArrayList<Integer>();
        EffSpread = new ArrayList<Double>();
        QuotedSpread = new ArrayList<Integer>();
        history = new Vector<Trade>();
    }




}
