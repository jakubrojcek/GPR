/**
 * Created by IntelliJ IDEA.
 * User: Jakub
 * Date: 9.3.12
 * Time: 11:38
 * To change this template use File | Settings | File Templates.
 */
public class Order {
    private int traderID;
    private double timeStamp;
    private boolean buyOrder;

    public Order(int traderID, double timeStamp, boolean buyOrder){
        this.traderID = traderID;
        this.timeStamp = timeStamp;
        this.buyOrder = buyOrder;
    }
    
    public void setTraderID(int ID){
        traderID = ID;
    }
    
    public int getTraderID(){
        return traderID;
    }
    public double getTimeStamp(){
        return timeStamp;
    }
    public boolean isBuyOrder(){
        return buyOrder;
    }


            
    
}
