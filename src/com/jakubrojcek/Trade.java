package com.jakubrojcek;

/**
 * Created by IntelliJ IDEA.
 * User: Jakub
 * Date: 16.3.12
 * Time: 10:50
 * To change this template use File | Settings | File Templates.
 */
public class Trade {
    private int buyerID;            // trader ID
    private double buyerPV;         // private values
    private boolean buyerIsHFT;     // isHFT
    private int sellerID;
    private boolean sellerIsHFT;
    private double sellerPV;
    private double timeSeller;
    private double timeBuyer;
    private double timeTrade;
    private double FVbeforeBuyer;
    private double FVbeforeSeller;
    private double price;
    private double FV;              // fundamental value
    private float trCostsBuyer = 0.0f;    // buyer's transaction costs
    private float trCostsSeller = 0.0f;   // seller's transaction costs

    public Trade(int buyerID, double bPV, boolean bHFT, int sellerID, double sPV, boolean sHFT,
                 double timeBuyer, double timeSeller, double FVb, double FVs,
                 double timeTrade, double price, double fv){
        this.buyerID = buyerID;
        this.buyerPV = bPV;
        this.buyerIsHFT = bHFT;
        this.sellerID = sellerID;
        this.sellerPV = sPV;
        this.sellerIsHFT = sHFT;
        this.timeBuyer = timeBuyer;
        this.timeSeller = timeSeller;
        this.timeTrade = timeTrade;
        this.FVbeforeBuyer = FVb;
        this.FVbeforeSeller = FVs;
        this.price = price;
        this.FV = fv;
    }

    public Trade(int buyerID, double bPV, boolean bHFT, int sellerID, double sPV, boolean sHFT,
                 double timeBuyer, double timeSeller, double FVb, double FVs,
                 double timeTrade, double price, double fv, float tcBuyer, float tcSeller){
        this.buyerID = buyerID;
        this.buyerPV = bPV;
        this.buyerIsHFT = bHFT;
        this.sellerID = sellerID;
        this.sellerPV = sPV;
        this.sellerIsHFT = sHFT;
        this.timeBuyer = timeBuyer;
        this.timeSeller = timeSeller;
        this.timeTrade = timeTrade;
        this.FVbeforeBuyer = FVb;
        this.FVbeforeSeller = FVs;
        this.price = price;
        this.FV = fv;
        this.trCostsBuyer = tcBuyer;
        this.trCostsSeller = tcSeller;
    }

    public String printTrade(){
        return (buyerID + ";" + buyerPV + ";" + (buyerIsHFT ? 1:0) + ";" + sellerID + ";"  + sellerPV + ";"
                + (sellerIsHFT ? 1:0) + ";" + timeBuyer + ";" + timeSeller + ";" + timeTrade + ";"
                + price + ";" + FV + ";" + trCostsBuyer + ";" + trCostsSeller + ";" + "\r");
    }

    public double getTimeSeller() {
        return timeSeller;
    }

    public double getTimeBuyer() {
        return timeBuyer;
    }

    public double getBuyerPV() {
        return buyerPV;
    }

    public double getSellerPV() {
        return sellerPV;
    }

    public double getFV() {
        return FV;
    }

    public double getPrice() {
        return price;
    }

    public boolean isSellerIsHFT() {
        return sellerIsHFT;
    }

    public boolean isBuyerIsHFT() {
        return buyerIsHFT;
    }

    public double getTimeTrade() {
        return timeTrade;
    }

    public double getFVbeforeBuyer() {
        return FVbeforeBuyer;
    }

    public double getFVbeforeSeller() {
        return FVbeforeSeller;
    }

    public float getTrCostsBuyer() {
        return trCostsBuyer;
    }

    public float getTrCostsSeller() {
        return trCostsSeller;
    }
}



