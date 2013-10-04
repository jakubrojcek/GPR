package com.jakubrojcek.hftRegulation;

import com.jakubrojcek.Order;

import java.util.*;


/**
 * Created by IntelliJ IDEA.
 * User: Jakub
 * Date: 6.3.12
 * Time: 11:28
 * To change this template use File | Settings | File Templates.
 */
public class LOB_LinkedHashMap {
    String model;
    double[] Prices; // prices for trading
    private int[] BookSizes; // signed sizes of the book
    private int[] BookInfo; // info used in decision making
    private int positionShift; // ++ if FVup, -- if FVdown
    double FV; // Fundamental value
    double tickSize; // size of one tick
    byte nPoints; // number of available positions
    int maxDepth; // 0 to 7 which matter
    int maxSumDepth;
    private int Pt = 0; // last transaction position
    private int b = 0; // 1 if last transaction buy, 0 if sell
    private int OrderID = 0; // id stamp for orders used as key in the book LHM
    ArrayList<Integer> traderIDsHFT = new ArrayList<Integer>();
    ArrayList<Integer> traderIDsNonHFT = new ArrayList<Integer>();
    //vectors holding traderIDs, HFT or nonHFT, traderID and position, price and position
    ArrayList<Order> ActiveOrders = new ArrayList<Order>();
    // holds orders which are in the book, remove from book: order.getPosition
    Hashtable<Integer, HashMap<Integer, Integer>> CurrentPosition =
            new Hashtable<Integer, HashMap<Integer, Integer>>();
    // traderID, position, signed number of orders at position: price + positionShift


    LinkedHashMap<Integer, Order>[] book; // orderID and orders
    History hist;
    HashMap<Integer, Trader> traders;

    public LOB_LinkedHashMap(String m, double fv, int fvPos, int md, int e, double ts, byte nP, History h, HashMap<Integer, Trader> t){
        model = m;
        hist = h;
        traders = t;
        nPoints = nP;
        FV = fv;
        Pt = fvPos;
        tickSize = ts;
        BookSizes = new int[nPoints];
        maxDepth = md;
        maxSumDepth = (e / 2 + 1) * maxDepth;
    }

    public void makeBook(double [] prices) {//initiates book with int[] prices
        Prices = prices;
        BookSizes = new int[nPoints];
        BookInfo = new int[8];
        book = new LinkedHashMap[nPoints];
        for (int i = 0; i < nPoints; i ++){
            book[i] = new LinkedHashMap();
        }
    }

    public ArrayList<Integer> FVup(double fv, double et, int tickChange){
        // (1) book shift, (2) 31+1, (3) currentPosition, (4) pricePosition
        ArrayList<Integer> tradersExecuted = new ArrayList<Integer>();
        Set keys;
        for (int i = tickChange; i > 0; i--){
            positionShift++;
            keys = book[i].keySet();
            if (!keys.isEmpty()){
                if (!book[i].get(keys.iterator().next()).isBuyOrder()){
                    while (!keys.isEmpty()){ // this part executes the SLOs against fringe
                        int oID = (Integer) keys.iterator().next();
                        Order o = book[i].remove(oID);
                        int traderID = o.getTraderID();
                        traders.get(traderID).execution(fv, et);
                        ActiveOrders.remove(o);
                        tradersExecuted.add(traderID);
                        keys.remove(oID);
                    }
                }
            }
        }

        for (int i = tickChange; i >= 0; i--){
            keys = book[i].keySet();
            while (! keys.isEmpty()){ // removing BLOs from the zero position
                int oID = (Integer) keys.iterator().next();
                Order o = book[i].remove(oID);
                int traderID = o.getTraderID();
                book[i].remove(oID);
                ActiveOrders.remove(o);
                traders.get(traderID).cancel(et);
                keys.remove(oID);
            }
        }

        System.arraycopy(Prices, tickChange, Prices, 0, nPoints - tickChange);
        System.arraycopy(book, tickChange, book, 0, nPoints - tickChange);
        for (int i = tickChange; i > 0; i--){
            book[nPoints - i] = new LinkedHashMap();
            Prices[nPoints - 1] = Prices[nPoints - 2] + tickSize;
            Pt = Math.max(Pt--, 0); // not to fall of the grid
        }
        FV = fv;
        BookSizes();
        return tradersExecuted;
    }

    public ArrayList<Integer> FVdown(double fv, double et, int tickChange){
        // (1) book shift, (2) 31-1, (3) currentPosition, (4) pricePosition
        ArrayList<Integer> tradersExecuted = new ArrayList<Integer>();
        Set keys;
        for (int i = nPoints - 1 - tickChange; i < nPoints - 1; i++){
            positionShift--; // TODO: check if shifts the right number of ticks
            keys = book[i].keySet();
            if (!keys.isEmpty()){
                if (book[i].get(keys.iterator().next()).isBuyOrder()){ // executing buyers at (nP - 2)
                    while (! keys.isEmpty()){
                        int oID = (Integer) keys.iterator().next();
                        Order o = book[i].remove(oID);
                        int traderID = o.getTraderID();
                        traders.get(traderID).execution(fv, et);
                        ActiveOrders.remove(o);
                        tradersExecuted.add(traderID);
                        keys.remove(oID);
                    }
                }
            }
        }

        for (int i = nPoints - 1 - tickChange; i < nPoints; i++){ // TODO: nPoints - tickChange, <=
            keys = book[i].keySet();
            while (!keys.isEmpty()){ // removing SLOs from the (nP - 1) position
                int oID = (Integer) keys.iterator().next();
                Order o = book[i].remove(oID);
                int traderID = o.getTraderID();
                book[i].remove(oID);
                ActiveOrders.remove(o);
                traders.get(traderID).cancel(et);
                keys.remove(oID);
                // TODO: I think I don't remove traders from all places here..
            }
        }

        System.arraycopy(Prices, 0, Prices, tickChange, nPoints - tickChange);
        System.arraycopy(book, 0, book, tickChange, nPoints - tickChange);
        for (int i = 0; i < tickChange; i++){
            book[i] = new LinkedHashMap();
            Prices[0] = Prices[1] - tickSize;
            Pt = Math.min(Pt++, nPoints - 1); // not to fall of the grid
        }
        FV = fv;
        BookSizes();
        return tradersExecuted;
    }

    public int[] getBookInfo(){
        /* (best Bid, Ask), (depth at B, A), (depth Buy, Sell),
(last price, WasBuy) */

        int Bt; // best bid position in Prices
        int At; // best ask position in Prices
        int Db = 0; // depth buys
        int Ds = 0; // depth sells


        int j = 0;
        while (BookSizes[j] >= 0 && j < nPoints - 1){
            j++;
        }
        At = j;
        //j = nPoints - 1;
        while (BookSizes[j] <= 0 && j > 0){
            j--;
        }
        Bt = j;

        j = 0;
        while (j <= Bt){
            Db += BookSizes[j];
            j++;
        }
        Db = Math.min(Db, maxSumDepth); // limited to 90

        j = nPoints - 1;
        while (j >= At){
            Ds += BookSizes[j];
            j--;
        }
        Ds = Math.max(Ds, - maxSumDepth); // limited to half of the LO grid at maxDepth

        BookInfo[0] = Bt; // best bid position
        BookInfo[1] = At; // best ask position
        BookInfo[2] = BookSizes[Bt]; // depth at best bid
        BookInfo[3] = - BookSizes[At]; // depth at best ask
        BookInfo[4] = Db; // depth buys
        BookInfo[5] = - Ds; // depth sells
        BookInfo[6] = Pt; // last transaction position
        BookInfo[7] = b; // 1 if last transaction buy, 0 if sell

        return BookInfo;
    }

    public Integer transactionRule(Integer oID, ArrayList<Order> orders){
        hist.addOrderData(BookInfo[1] - BookInfo[0]); // quoted spread
        int pos, size;
        for (Order o : orders){
            pos = o.getPosition();
            size = o.getSize();
            if (o.isCancelled()){ // TODO: test if this works
                int Q = o.getQ();
                pos = o.getPosition() - positionShift;
                Order removed = book[pos].remove(o.getOrderID());
                if (!ActiveOrders.contains(removed)){
                    System.out.println("debug");
                }
                ActiveOrders.remove(o);
                Collection<Order> collO = book[pos].values();
                for (Order order : collO){ // increase priorities of the remaining orders
                    if (Q < order.getQ()){order.increasePriority(size);}
                    // TODO: problem is that the order has no OrderID..
                }
            } else {
                if (o.isBuyOrder()){
                    if (book[pos].size() > 0 && !book[pos].get(book[pos].keySet().iterator().next()).isBuyOrder()){
                        Order cp = book[pos].remove(book[pos].keySet().iterator().next());
                        Integer CPid = cp.getTraderID();
                        traders.get(CPid).execution(FV, o.getTimeStamp());
                        ActiveOrders.remove(cp);
                        Pt = pos; // sets last transaction position
                        b = 1; // sets last transaction direction, buy = 1
                        hist.addTrade(o, cp, o.getTimeStamp(), Prices[pos], FV);
                        hist.addOrderData(pos - (double)(BookInfo[1] + BookInfo[0]) / 2); // effective spread
                        Collection<Order> collO = book[pos].values();
                        for (Order order : collO){
                            order.increasePriority(size);
                        }
                        oID = CPid;
                    } else if (pos == nPoints - 1){ // if BMO executed against fringe, just continue
                    } else{
                        OrderID++;
                        o.setOrderID(OrderID);
                        o.setPosition(pos + positionShift);
                        o.setQ(book[pos].size());
                        book[pos].put(OrderID,o); // put some key number here
                        ActiveOrders.add(o);
                        oID = null;
                    }

                } else {
                    if (book[pos].size() > 0 && book[pos].get(book[pos].keySet().iterator().next()).isBuyOrder()){
                        Order cp = book[pos].remove(book[pos].keySet().iterator().next());
                        Integer CPid = cp.getTraderID();
                        traders.get(CPid).execution(FV, o.getTimeStamp());
                        ActiveOrders.remove(cp);
                        Pt = pos; // set last transaction price
                        b = 0; // set last transaction direction, 0=sell
                        hist.addTrade(cp, o, o.getTimeStamp(), Prices[pos], FV);
                        hist.addOrderData((double) (BookInfo[1] + BookInfo[0]) / 2 - pos);
                        Collection<Order> collO = book[pos].values();
                        for (Order order : collO){
                            order.increasePriority(size);
                        }
                        oID = CPid;
                    } else if (pos == 0){ // if SMO executed against fringe, just continue
                    } else{
                        OrderID++;
                        o.setOrderID(OrderID);
                        o.setPosition(pos + positionShift);
                        o.setQ(book[pos].size());
                        book[pos].put(OrderID, o); // put some key number here
                        ActiveOrders.add(o);
                        oID = null;
                    }
                }
            }
        }
        BookSizes();
        return oID;
    }

    public void BookSizes(){
        int orderNum = 0;
        int sizeNum = 0;
        boolean buy;
        //Iterator keysO;
        Collection<Order> collO;
        for (int i = 0; i < nPoints; i++){
            BookSizes[i] = 0;
            //keysO = book[i].keySet().iterator();
            collO = book[i].values();
            for (Order o : collO){
                sizeNum += o.getSize();
            }
            /*while (keysO.hasNext()){
sizeNum += book[i].get(keysO.next()).getSize();
}*/
            if (sizeNum != 0){
                buy = book[i].get(book[i].keySet().iterator().next()).isBuyOrder(); // buy orders at book[i]?
                BookSizes[i] = buy ? Math.min(sizeNum, maxDepth) : - Math.min(sizeNum, maxDepth); // max size at each tick is maxDepth- 7 or 15
            }
            orderNum += sizeNum;
            sizeNum = 0;
        }
        if (ActiveOrders.size() != orderNum){
            System.out.println("error");
        }
        /*Iterator trs = traders.keySet().iterator();
while (trs.hasNext()){
CurrentPosition.get(trs.next()).
}*/
    }

    public Order tryCancel(Order o){ // if order is null after returning, see if there's sth to cancel
        int id = o.getTraderID();
        int tempSize = CurrentPosition.get(id).get(o.getPosition());
        boolean buy = o.isBuyOrder();
        tempSize = buy ? --tempSize : ++ tempSize;
        if (tempSize == 0){
            CurrentPosition.get(id).remove(o.getPosition());
            if (CurrentPosition.get(id).isEmpty()){
                CurrentPosition.remove(id);
                traders.remove(id);
            }
        } else {
            CurrentPosition.get(id).put(o.getPosition(), tempSize);
        }
        book[o.getPosition() - positionShift].values().remove(o);

        //BookSizes();

        return o;
    }

    public void removeOrders(ArrayList<Order> o2r){
        for (Order ao : o2r){
            ActiveOrders.remove(ao);
        }
        BookSizes();
    }

    // returning random traderID either HFT or nonHFT
    public int randomHFTtraderID(){
        return traderIDsHFT.get((int) (Math.random() * traderIDsHFT.size()));
    }

    public int randomNonHFTtraderID(){
        //System.out.println("IDs = " + traderIDsNonHFT);
        return traderIDsNonHFT.get((int) (Math.random() * traderIDsNonHFT.size()));
    }

    public void printBook(){
        for (int i = 0; i < nPoints; i ++){
            System.out.println(Prices[i] + ": " + book[i].keySet());
        }
        /*System.out.println("non HFTs " + traderIDsNonHFT);
System.out.println("HFTs " + traderIDsHFT);
System.out.println(BookInfo[0] + " best bid @ position = " + BookInfo[0] + " shift " + positionShift);
System.out.println(BookInfo[1] + " best ask @ position = " + BookInfo[1] + " shift " + positionShift);
System.out.println(BookInfo[2] + " depth at best bid");
System.out.println(BookInfo[3] + " depth at best ask");
System.out.println(BookInfo[4] + " depth buys");
System.out.println(BookInfo[5] + " depth sells");
System.out.println(BookInfo[6] + " last transaction price");
System.out.println(BookInfo[7] + " 1 if last transaction buy, 0 if sell");*/
    }


    public void addTrader(int traderID){
        if (traders.get(traderID).getIsHFT()){
            traderIDsHFT.add(traderID);
        } else{
            traderIDsNonHFT.add(traderID);
        }
    }
    public int[] getBookSizes(){
        // signed sizes
        return BookSizes;
    }

    public int getnReturningHFT(){
        return traderIDsHFT.size();
    }

    public int getnReturningNonHFT(){
        return traderIDsNonHFT.size();
    }

    public LinkedHashMap<Integer, Order>[] getBook() {
        return book;
    }

    public ArrayList<Order> getActiveOrders() {
        return ActiveOrders;
    }

    public int getPositionShift() {
        return positionShift;
    }
}
