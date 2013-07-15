package com.jakubrojcek;

/**
 * Created with IntelliJ IDEA.
 * User: rojcek
 * Date: 09.04.13
 * Time: 18:28
 * To change this template use File | Settings | File Templates.
 */
public class Order_test {
    private int traderID;
    private double timeStamp;
    private boolean buyOrder;
    private short action;
    private int position;

    public Order_test(int traderID, double timeStamp, boolean buyOrder, short action, int position){
        this.traderID = traderID;
        this.timeStamp = timeStamp;
        this.buyOrder = buyOrder;
        this.action = action;
        this.position = position;
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

    public short getAction() {
        return action;
    }

    public int getPosition() {
        return position;
    }

}
