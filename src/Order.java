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
    private short action;
    private int position;

    public Order(int traderID, double timeStamp, boolean buyOrder, short action, int position){
        this.traderID = traderID;
        this.timeStamp = timeStamp;
        this.buyOrder = buyOrder;
        this.action = action;
        this.position = position;
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

    public short getAction() {
        return action;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {   // used in the book, like CurrentPosition
        this.position = position;
    }
}
