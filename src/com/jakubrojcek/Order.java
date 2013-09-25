package com.jakubrojcek;

/**
 * Created by IntelliJ IDEA.
 * User: Jakub
 * Date: 9.3.12
 * Time: 11:38
 * To change this template use File | Settings | File Templates.
 */
public class Order {
    private int orderID;
    private int traderID;
    private double timeStamp;
    private boolean buyOrder;
    private boolean firstShare;
    private short action;
    private short q;
    private int size;
    private int position;

    public Order(int traderID, double timeStamp, boolean buyOrder, boolean fs, short action, short q, int position){
        this.traderID = traderID;
        this.timeStamp = timeStamp;
        this.buyOrder = buyOrder;
        this.firstShare = fs;
        this.action = action;
        this.q = q;
        this.position = position;
    }

    public Order(int traderID, double timeStamp, boolean buyOrder, int size, int position){
        this.traderID = traderID;
        this.timeStamp = timeStamp;
        this.buyOrder = buyOrder;
        this.size = size;
        this.position = position;
    }
    
    public int getTraderID(){
        return traderID;
    }

    public int getOrderID() {
        return orderID;
    }

    public int getSize() {
        return size;
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

    public short getQ() {
        return q;
    }

    public int getPosition() {
        return position;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public void setPosition(int position) {   // used in the book, like CurrentPosition
        this.position = position;
    }

    public void setAction(short action) {
        this.action = action;
    }

    public void setQ(short q) {
        this.q = q;
    }

    public boolean isFirstShare() {
        return firstShare;
    }
}
