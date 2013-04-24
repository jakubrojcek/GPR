/**
 * Created by IntelliJ IDEA.
 * User: Jakub
 * Date: 16.3.12
 * Time: 10:50
 * To change this template use File | Settings | File Templates.
 */
public class Trade {
    private int buyerID;        // trader ID
    private double buyerPV;     // private values
    private boolean buyerIsHFT; // isHFT
    private int sellerID;
    private boolean sellerIsHFT;
    private double sellerPV;
    private double timeSeller;
    private double timeBuyer;
    private double timeTrade;
    private double price;
    private double FV;          // fundamental value

    public Trade(int buyerID, double bPV, boolean bHFT, int sellerID, double sPV, boolean sHFT,
                 double timeBuyer, double timeSeller, double timeTrade, double price,
                 double fv){
        this.buyerID = buyerID;
        this.buyerPV = bPV;
        this.buyerIsHFT = bHFT;
        this.sellerID = sellerID;
        this.sellerPV = sPV;
        this.sellerIsHFT = sHFT;
        this.timeBuyer = timeBuyer;
        this.timeSeller = timeSeller;
        this.timeTrade = timeTrade;
        this.price = price;
        this.FV = fv;
    }

    public String printTrade(){
        return (buyerID + ";" + buyerPV + ";" + (buyerIsHFT ? 1:0) + ";" + sellerID + ";"  + sellerPV + ";"
                + (sellerIsHFT ? 1:0) + ";" + timeBuyer + ";" + timeSeller + ";" + timeTrade + ";"
                + price + ";" + FV + ";" + "\r");
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
}

