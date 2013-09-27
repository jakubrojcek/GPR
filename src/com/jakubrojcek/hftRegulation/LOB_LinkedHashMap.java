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
    double[] Prices;                           // prices for trading
    private int[] BookSizes;                   // signed sizes of the book
    private int[] BookInfo;                    // info used in decision making
    private int positionShift;                 // ++ if FVup, -- if FVdown
    double FV;                                 // Fundamental value
    double tickSize;                           // size of one tick
    byte nPoints;                              // number of available positions
    int maxDepth;                              // 0 to 7 which matter
    int maxSumDepth;
    private int Pt = 0;                        // last transaction position
    private int b = 0;                         // 1 if last transaction buy, 0 if sell
    private int OrderID = 0;                   // id stamp for orders used as key in the book LHM
    Hashtable<Byte, Byte> priorities    = new Hashtable<Byte, Byte>();
    // priorities HashTable <position, priority>
    ArrayList<Integer> traderIDsHFT     = new ArrayList<Integer>();
    ArrayList<Integer> traderIDsNonHFT  = new ArrayList<Integer>();
    //vectors holding traderIDs, HFT or nonHFT, traderID and position, price and position
    ArrayList<Order> ActiveOrders       = new ArrayList<Order>();
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

    public void FVup(double fv, double et, int tickChange){
        // (1) book shift, (2) 31+1, (3) currentPosition, (4) pricePosition

        Set keys;
        for (int i = tickChange; i > 0; i--){
            keys = book[i].keySet();
            if (!keys.isEmpty()){
                if (! book[i].get(keys.iterator().next()).isBuyOrder()){
                    while (!keys.isEmpty()){ // this part executes the SLOs against fringe
                        int oID = (Integer) keys.iterator().next();
                        Order o = book[i].remove(oID);
                        int traderID = o.getTraderID();
                        traders.get(traderID).execution(fv, et);
                        ActiveOrders.remove(o);
                        int tempSizeCP = CurrentPosition.get(traderID).get(i + positionShift);
                        if (++tempSizeCP == 0){
                            CurrentPosition.get(traderID).remove(i + positionShift);
                            if (CurrentPosition.get(traderID).isEmpty()){
                                CurrentPosition.remove(traderID);
                                traders.remove(traderID);
                            }
                        } else {
                            CurrentPosition.get(traderID).put(i + positionShift, tempSizeCP);
                        }
                        keys.remove(oID);
                    }
                }
            }
        }

        for (int i = tickChange; i >= 0; i--){                        // TODO: >= 0
            keys = book[i].keySet();
            while (! keys.isEmpty()){ // removing BLOs from the zero position
                int oID = (Integer) keys.iterator().next();
                Order o = book[i].remove(oID);
                int traderID = o.getTraderID();
                book[i].remove(oID);
                ActiveOrders.remove(o);
                int tempSizeCP = CurrentPosition.get(traderID).get(i + positionShift);
                if (--tempSizeCP == 0){
                    CurrentPosition.get(traderID).remove(i + positionShift);
                    if (CurrentPosition.get(traderID).isEmpty()){
                        CurrentPosition.remove(traderID);
                        traders.remove(traderID);
                    }
                } else {
                    CurrentPosition.get(traderID).put(i + positionShift, tempSizeCP);
                }
                keys.remove(oID);
            }
        }

        for (int i = 0; i < nPoints - tickChange; i++){
            book[i] = book[i + tickChange];
        }

        for (int i = tickChange; i > 0; i--){
            book[nPoints - i] = new LinkedHashMap();
            System.arraycopy(Prices, 1, Prices, 0, nPoints - 1);
            Prices[nPoints - 1] = Prices[nPoints - 2] + tickSize;
            Pt = Math.max(Pt--, 0); // not to fall of the grid
            positionShift++;
        }
        FV = fv;
        BookSizes();
    }

    public void FVdown(double fv, double et, int tickChange){
        // (1) book shift, (2) 31-1, (3) currentPosition, (4) pricePosition
        Set keys;
        for (int i = nPoints - 1 - tickChange; i < nPoints - 1; i++){
            keys = book[i].keySet();
            if (!keys.isEmpty()){
                if (book[i].get(keys.iterator().next()).isBuyOrder()){
                    while (! keys.isEmpty()){
                        int oID = (Integer) keys.iterator().next();
                        Order o = book[i].remove(oID);
                        int traderID = o.getTraderID();
                        traders.get(traderID).execution(fv, et);
                        ActiveOrders.remove(o);
                        int tempSizeCP = CurrentPosition.get(traderID).get(i + positionShift);
                        if (--tempSizeCP == 0){
                            CurrentPosition.get(traderID).remove(i + positionShift);
                            if (CurrentPosition.get(traderID).isEmpty()){
                                CurrentPosition.remove(traderID);
                                traders.remove(traderID);
                            }
                        } else {
                            CurrentPosition.get(traderID).put(i + positionShift, tempSizeCP);
                        }
                        keys.remove(oID);
                    }
                }
            }
        }

        for (int i = nPoints - 1 - tickChange; i < nPoints; i++){      // TODO: nPoints - tickChange, <=
            keys = book[i].keySet();
            while (!keys.isEmpty()){                        // removing SLOs from the zero position
                int oID = (Integer) keys.iterator().next();
                Order o = book[i].remove(oID);
                int traderID = o.getTraderID();
                book[i].remove(oID);
                ActiveOrders.remove(o);
                int tempSizeCP = CurrentPosition.get(traderID).get(i + positionShift);
                if (++tempSizeCP == 0){
                    CurrentPosition.get(traderID).remove(i + positionShift);
                    if (CurrentPosition.get(traderID).isEmpty()){
                        CurrentPosition.remove(traderID);
                        traders.remove(traderID);
                    }
                } else {
                    CurrentPosition.get(traderID).put(i + positionShift, tempSizeCP);
                }
                keys.remove(oID);
            }
        }

        for (int i = nPoints - 1; i > tickChange - 1; i--){
            book[i] = book[i - tickChange];
        }
        for (int i = 0; i < tickChange; i++){
            book[i] = new LinkedHashMap();
            System.arraycopy(Prices, 0, Prices, 1, nPoints - 1);
            Prices[0] = Prices[1] - tickSize;
            Pt = Math.min(Pt++, nPoints - 1);                        // not to fall of the grid
            positionShift--;
        }
        FV = fv;
        BookSizes();
    }
    
    public int[] getBookInfo(){
        /* (best Bid, Ask), (depth at B, A), (depth Buy, Sell),
        (last price, WasBuy) */

        int Bt;         // best bid position in Prices
        int At;         // best ask position in Prices
        int Db = 0;     // depth buys
        int Ds = 0;     // depth sells


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

        BookInfo[0] = Bt;                 // best bid position
        BookInfo[1] = At;                 // best ask position
        BookInfo[2] = BookSizes[Bt];      // depth at best bid
        BookInfo[3] = - BookSizes[At];    // depth at best ask
        BookInfo[4] = Db;                 // depth buys
        BookInfo[5] = - Ds;               // depth sells
        BookInfo[6] = Pt;                 // last transaction position
        BookInfo[7] = b;                  // 1 if last transaction buy, 0 if sell

        return BookInfo;
    }
       
     public void transactionRule(int oID, ArrayList<Order> orders){
         hist.addOrderData(BookInfo[1] - BookInfo[0]); // quoted spread
         Integer oldPos = null;
         // cancel previous LO unless not retained
         /*if(currentPosition.containsKey(oID)){
             oldPos = currentPosition.get(oID) - positionShift;
             if (oldPos != pos || o.isBuyOrder() != book[oldPos].get(oID).isBuyOrder()){
                 book[oldPos].remove(oID);
                 currentPosition.remove(oID);
             } else {
                 return;
             }
         }*/

        /* if (traderIDsHFT.contains(oID)){
            traderIDsHFT.remove(traderIDsHFT.indexOf(oID));
         } else if (traderIDsNonHFT.contains(oID)){
            traderIDsNonHFT.remove(traderIDsNonHFT.indexOf(oID));
         }*/

         HashMap<Integer, Integer> tempHM = new HashMap<Integer, Integer>();
         if (CurrentPosition.containsKey(oID)){
             tempHM = CurrentPosition.get(oID);
         }
         Integer tempSize = 0;
         Integer pos = null;

         for (Order o : orders){
             /*if (o.getAction() > 15){
                 System.out.println("stop & think");
             }*/
             if (pos == null || pos != o.getPosition()){
                 // put here tempSize to tempHM if not empty and position not null
                 if (pos != null && tempSize != 0) {tempHM.put(pos + positionShift, tempSize);}
                 pos = o.getPosition();
                 tempSize = tempHM.containsKey(pos + positionShift) ? tempHM.get(pos + positionShift)
                                                                    : 0;
             }
             if (o.isBuyOrder()){
                 if (book[pos].size() > 0 && !book[pos].get(book[pos].keySet().iterator().next()).isBuyOrder()){
                     Order cp = book[pos].remove(book[pos].keySet().iterator().next());
                     Integer CPid = cp.getTraderID();

                     traders.get(CPid).execution(FV, o.getTimeStamp());
                     ActiveOrders.remove(cp);
                     Pt = pos;                                       // sets last transaction position
                     b = 1;                                          // sets last transaction direction, buy = 1
                     hist.addTrade(o, cp, o.getTimeStamp(), Prices[pos], FV);
                     hist.addOrderData(pos - (double)(BookInfo[1] + BookInfo[0]) / 2); // effective spread
                     Integer tempSizeCP = CurrentPosition.get(CPid).get(pos + positionShift);
                     if (++tempSizeCP == 0){
                         CurrentPosition.get(CPid).remove(pos + positionShift);
                         if (CurrentPosition.get(CPid).isEmpty()){
                             CurrentPosition.remove(CPid);
                             traders.remove(CPid);
                         }
                     } else {
                         CurrentPosition.get(CPid).put(pos + positionShift, tempSizeCP);
                     }
                 } else if (pos == nPoints - 1){        // if BMO executed against fringe, just continue
                 } else{
                     tempSize++;
                     OrderID++;
                     o.setPosition(pos + positionShift);
                     book[pos].put(OrderID,o);           // put some key number here
                     ActiveOrders.add(o);
                 }

             } else {
                 if (book[pos].size() > 0 && book[pos].get(book[pos].keySet().iterator().next()).isBuyOrder()){
                     Order cp = book[pos].remove(book[pos].keySet().iterator().next());
                     Integer CPid = cp.getTraderID();
                     traders.get(CPid).execution(FV, o.getTimeStamp());
                     ActiveOrders.remove(cp);
                     Pt = pos;           // set last transaction price
                     b = 0;              // set last transaction direction, 0=sell
                     hist.addTrade(cp, o, o.getTimeStamp(), Prices[pos], FV);
                     hist.addOrderData((double)(BookInfo[1] + BookInfo[0]) / 2 - pos);
                     Integer tempSizeCP = CurrentPosition.get(CPid).get(pos + positionShift);
                     if (--tempSizeCP == 0){
                         CurrentPosition.get(CPid).remove(pos + positionShift);
                         if (CurrentPosition.get(CPid).isEmpty()){
                             CurrentPosition.remove(CPid);
                             traders.remove(CPid);
                         }
                     } else {
                         CurrentPosition.get(CPid).put(pos + positionShift, tempSizeCP);
                     }
                 } else if (pos == 0){      // if SMO executed against fringe, just continue
                 } else{
                     tempSize--;
                     OrderID++;
                     o.setPosition(pos + positionShift);
                     book[pos].put(OrderID,o);           // put some key number here
                     ActiveOrders.add(o);
                 }
             }
         }
         if (tempSize != 0){tempHM.put(pos + positionShift, tempSize);}
         if (!tempHM.isEmpty()){
             CurrentPosition.put(oID, tempHM);
         } else {
             traders.remove(oID);
         }
         BookSizes();
    }

    public void BookSizes(){
        int orderNum = 0;
        int sizeNum = 0;
        boolean buy;
        Iterator keysO;
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
                buy = book[i].get(book[i].keySet().iterator().next()).isBuyOrder();  // buy orders at book[i]?
                BookSizes[i] = buy ? Math.min(sizeNum, maxDepth) : - Math.min(sizeNum, maxDepth);   // max size at each tick is maxDepth- 7 or 15
            }
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

    public Order tryCancel(Order o){   // if order is null after returning, see if there's sth to cancel
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
}
