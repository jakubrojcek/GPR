package com.jakubrojcek.hftRegulation;

import com.jakubrojcek.Order;
import com.jakubrojcek.Trade;

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
    int[] depths;                       // count, lBt, lAt, dBt, dSt
    Vector<Trade> history;
    String folder;
    double TTAX = 0.0f;
    double CFEE = 0.0f;
    double MFEE = 0.0f;
    double TFEE = 0.0f;

    public History(HashMap<Integer, Trader> t, String f){
        traders = t;
        Asks = new ArrayList<Integer>();
        Bids = new ArrayList<Integer>();
        EffSpread = new ArrayList<Double>();
        QuotedSpread = new ArrayList<Integer>();
        depths = new int[5];
        Events = new ArrayList<Integer>();
        States = new ArrayList<Integer>();
        history = new Vector<Trade>();
        folder = f;
    }

    public History(HashMap<Integer, Trader> t, String f, double tt, double cf, double mf, double tf){
        traders = t;
        Asks = new ArrayList<Integer>();
        Bids = new ArrayList<Integer>();
        EffSpread = new ArrayList<Double>();
        QuotedSpread = new ArrayList<Integer>();
        depths = new int[5];
        Events = new ArrayList<Integer>();
        States = new ArrayList<Integer>();
        history = new Vector<Trade>();
        folder = f;
        TTAX = tt;
        CFEE = cf;
        MFEE = mf;
        TFEE = tf;
    }
    
    public void addTrade(Order buy, Order sell, double timeTrade, double price, double fv){
        int bID = buy.getTraderID();
        int sID = sell.getTraderID();

        if (TTAX != 0.0 || CFEE != 0.0){
            history.add(new Trade(bID, traders.get(bID).getPrivateValue(), traders.get(bID).getIsHFT(),
                    sID, traders.get(sID).getPrivateValue(), traders.get(sID).getIsHFT(),
                    buy.getTimeStamp(), sell.getTimeStamp(),
                    traders.get(bID).getPriceFV(), traders.get(sID).getPriceFV(), timeTrade, price, fv,
                    traders.get(bID).getCancelCount() * CFEE + TTAX, traders.get(sID).getCancelCount() * CFEE + TTAX));
        } else if (MFEE != 0.0){
            if (buy.getTimeStamp() < sell.getTimeStamp()){   // sell MO
                history.add(new Trade(bID, traders.get(bID).getPrivateValue(), traders.get(bID).getIsHFT(),
                        sID, traders.get(sID).getPrivateValue(), traders.get(sID).getIsHFT(),
                        buy.getTimeStamp(), sell.getTimeStamp(),
                        traders.get(bID).getPriceFV(), traders.get(sID).getPriceFV(), timeTrade, price, fv,
                        traders.get(bID).getCancelCount() * CFEE + TTAX - MFEE,
                        traders.get(sID).getCancelCount() * CFEE + TTAX + TFEE));
            } else {                                        // buy MO
                history.add(new Trade(bID, traders.get(bID).getPrivateValue(), traders.get(bID).getIsHFT(),
                        sID, traders.get(sID).getPrivateValue(), traders.get(sID).getIsHFT(),
                        buy.getTimeStamp(), sell.getTimeStamp(),
                        traders.get(bID).getPriceFV(), traders.get(sID).getPriceFV(), timeTrade, price, fv,
                        traders.get(bID).getCancelCount() * CFEE + TTAX + TFEE,
                        traders.get(sID).getCancelCount() * CFEE + TTAX - MFEE));
            }
        } else {
            history.add(new Trade(bID, traders.get(bID).getPrivateValue(), traders.get(bID).getIsHFT(),
                    sID, traders.get(sID).getPrivateValue(), traders.get(sID).getIsHFT(),
                    buy.getTimeStamp(), sell.getTimeStamp(),
                    traders.get(bID).getPriceFV(), traders.get(sID).getPriceFV(), timeTrade, price, fv));
        }
    }

    public void addEffSpread(Double spread){
        EffSpread.add(spread);
    }

    public void addQuotedSpread(Integer qspread){
        QuotedSpread.add(qspread);
    }

    public void addDepth(int[] bi){
        depths[0]++;
        depths[1] += bi[2];             // lBt
        depths[2] += bi[3];             // lAt
        depths[3] += bi[4];             // dBt
        depths[4] += bi[5];             // dSt
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
                writer.write("moHFT;mo;loHFT;lo;tradeTime;FV;Price;buyerPV;sellerPV;buyMO;buyerCosts;sellerCosts;timeLO;" + "\r");
            }
            int sz = history.size();
            double mo;                               // payoff from market order
            double lo;                               // payoff from limit order
            double loTime;                           // time of limit order submission
            int moHFT;                               // is the MO submitter HFT
            int loHFT;                               // is the LO submitter HFT
            int buyMO;
            for (int i = 0; i < sz; i++){
                Trade t = history.get(i);
                if (t.getTimeBuyer() < t.getTimeSeller()){           // distinguishing if seller initiated
                    mo = t.getPrice() - t.getSellerPV() - t.getFV();
                    moHFT = t.isSellerIsHFT() ? 1 : 0;
                    lo = t.getFV() + t.getBuyerPV() - t.getPrice();
                    loHFT = t.isBuyerIsHFT() ? 1 : 0;
                    buyMO = 0;                                      // seller initiated order
                    loTime = t.getTimeBuyer();
                } else {
                    mo = t.getFV() + t.getBuyerPV() - t.getPrice();
                    moHFT = t.isBuyerIsHFT() ? 1 : 0;
                    lo = t.getPrice() - t.getSellerPV() - t.getFV();
                    loHFT = t.isSellerIsHFT() ? 1 : 0;
                    buyMO = 1;                                      // buyer initiated order
                    loTime = t.getTimeSeller();
                }
                //writer.writeDecisions(history.get(i).printTrade());
                writer.write(moHFT + ";" + mo + ";" + loHFT + ";" + lo + ";"  + t.getTimeTrade() +  ";"
                        + t.getFV() + ";" + t.getPrice() + ";" + t.getBuyerPV() + ";" +
                        t.getSellerPV() + ";" + buyMO + ";"
                        + t.getTrCostsBuyer() + ";" + t.getTrCostsSeller() + ";" + loTime + ";" +  "\r");
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
            double sum = 0.0;
            for (int i = 0; i < sz; i++){
                sum += QuotedSpread.get(i);
            }
            writer.write((double) (sum / sz) + ";" + "\r");
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        try{
            String outputFileName = folder + "depth.csv";
            FileWriter writer = new FileWriter(outputFileName, true);
            if (writeHeader){
                writer.write("depth;");
            }
            writer.write(((double) depths[1] / depths[0]) + ";" +
                     ((double) depths[2] / depths[0]) + ";" +
                     ((double) depths[3] / depths[0]) + ";" +
                     ((double) depths[4] / depths[0]) + ";" + "\r");
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
        depths = new int[5];
        history = new Vector<Trade>();
    }




}
