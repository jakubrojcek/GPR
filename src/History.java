import java.io.FileWriter;
import java.io.BufferedWriter;
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
    Vector<Trade> history;
    Vector<Decision> decisions;
    String folder;

    public History(HashMap<Integer, Trader> t, String f){
        traders = t;
        Asks = new ArrayList<Integer>();
        Bids = new ArrayList<Integer>();
        Events = new ArrayList<Integer>();
        States = new ArrayList<Integer>();
        history = new Vector<Trade>();
        decisions = new Vector<Decision>();
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

    public void addOrderData(int BestBid, int BestAsk){
        Bids.add(BestBid);
        Asks.add(BestAsk);
    }

    public void addStatisticsData(int eventNum, int statesNum){
        Events.add(eventNum);
        States.add(statesNum);
    }

    public void addDecisions(int Bt, int lBt, int spread, int action){
        decisions.add(new Decision(Bt, lBt, spread, action));
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
            for (int i = 0; i < sz; i++){
                Trade t = history.get(i);
                if (t.getTimeBuyer() < t.getTimeSeller()){           // distinguishing if seller initiated
                    mo = t.getPrice() - t.getSellerPV() - t.getFV();
                    lo = t.getFV() + t.getBuyerPV() - t.getPrice();
                } else {
                    mo = t.getFV() + t.getBuyerPV() - t.getPrice();
                    lo = t.getPrice() - t.getSellerPV() - t.getFV();
                }
                //writer.write(history.get(i).printTrade());
                writer.write(mo + ";" + lo + ";" + "\r");
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
                writer.write("BestBid;BestAsk;");
            }
            int sz = Bids.size();
            int sumB = 0;
            int sumA = 0;
            for (int i = 0; i < sz; i++){
                sumB += Bids.get(i);
                sumA += Asks.get(i);
            }
            writer.write((double)sumB/sz + ";" + (double)sumA/sz + ";" + "\r");
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

    public void printDepthFrequency(){
        try{
            String outputFileName = folder + "frequency.csv";
            FileWriter writer = new FileWriter(outputFileName, true);
            for (Decision d:decisions){
                writer.write(d.printDecision());
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
        history = new Vector<Trade>();
        decisions = new Vector<Decision>();
    }




}
