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
    private boolean cancelled = false;
    private int action;
    private int q;                      // priority in the queue
    private int size;
    private int position;

    public Order(int traderID, double timeStamp, boolean buyOrder, boolean fs, int action, int q, int position){
        this.traderID = traderID;
        this.timeStamp = timeStamp;
        this.buyOrder = buyOrder;
        this.firstShare = fs;
        this.action = action;
        this.q = q;
        this.position = position;
    }

    public Order(int traderID, double timeStamp, boolean buyOrder, int size, int Q, int position){
        this.traderID = traderID;
        this.timeStamp = timeStamp;
        this.buyOrder = buyOrder;
        this.size = size;
        this.q = Q;
        this.position = position;
    }

    public Order(int traderID, double timeStamp, boolean buyOrder, int size, int Q, int position, int a){
        this.traderID = traderID;
        this.timeStamp = timeStamp;
        this.buyOrder = buyOrder;
        this.size = size;
        this.q = Q;
        this.position = position;
        this.action = a;
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

    public int getAction() {
        return action;
    }

    public int getQ() {
        return q;
    }

    public int getPosition() {
        return position;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
        if (orderID == 0){
            System.out.println("debug");
        }
    }

    public void setPosition(int position) {   // used in the book, like CurrentPosition
        this.position = position;
    }

    public void setTimeStamp(double timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setAction(short action) {
        this.action = action;
    }

    public void setQ(int q) {
        this.q = q;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isFirstShare() {
        return firstShare;
    }

    public void increasePriority(int i){
        q -= i;
        if (q < 0){
            System.out.println("debug");
        }
    }
}
