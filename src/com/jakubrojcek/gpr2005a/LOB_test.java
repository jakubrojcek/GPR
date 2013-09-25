package com.jakubrojcek.gpr2005a;

import com.jakubrojcek.Order_test;

import java.util.*;
import java.lang.Math;


/**
 * Created by IntelliJ IDEA.
 * User: Jakub
 * Date: 6.3.12
 * Time: 11:28
 * To change this template use File | Settings | File Templates.
 */
public class LOB_test {
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
    private int Pt;                            // last transaction position
    private int b = 0;                         // 1 if last transaction buy, 0 if sell
    private int OrderID = 0;                   // id stamp for orders used as key in the book LHM
    Hashtable<Byte, Byte> priorities = new Hashtable<Byte, Byte>();
    // priorities HashTable <position, priority>
    ArrayList<Integer> traderIDsHFT = new ArrayList<Integer>();
    ArrayList<Integer> traderIDsNonHFT = new ArrayList<Integer>();
    //vectors holding traderIDs, HFT or nonHFT, traderID and position, price and position
    Hashtable<Integer, HashMap<Integer, Integer>> CurrentPosition =
            new Hashtable<Integer, HashMap<Integer, Integer>>();
    // traderID, position, signed number of orders at position: price + positionShift


    LinkedHashMap<Integer, Order_test>[] book; // price position and orders
    History hist;
    HashMap<Integer, Trader> traders;

    public LOB_test(String m, double fv, int FVpos, int md, int e, double ts, byte nP, History h, HashMap<Integer, Trader> t){
        model = m;
        hist = h;
        traders = t;
        nPoints = nP;
        FV = fv;
        Pt = FVpos;
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
                    while (! keys.isEmpty()){ // this part executes the SLOs against fringe
                        int oID = (Integer) keys.iterator().next();
                        Order_test o = book[i].remove(oID);
                        int traderID = o.getTraderID();
                        if (model == "GPR2005") {
                            //traders.get(traderID).execution(fv);
                        } else {traders.get(traderID).execution(fv, et);}
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
        /*Set keys = book[1].keySet();
        if (!keys.isEmpty()){
            if (! book[1].get(keys.iterator().next()).isBuyOrder()){
                while (! keys.isEmpty()){          // this part executes the SLOs against fringe
                    int id = (Integer) keys.iterator().next();
                    if (model == "GPR2005") {
                        traders.get(id).execution(fv);
                    } else {traders.get(id).execution(fv, et);}
                    if (traderIDsNonHFT.contains(id)){
                        traderIDsNonHFT.remove(traderIDsNonHFT.indexOf(id));
                    } else if (traderIDsHFT.contains(id)){
                        traderIDsHFT.remove(traderIDsHFT.indexOf(id));
                    }
                    currentPosition.remove(id);
                    book[1].remove(id);
                    traders.remove(id);
                    keys.remove(id);
                }
            }
        }*/ // old FVup sellers part
        // removing BLOs from the zero position
        /*keys = book[0].keySet();
        while (! keys.isEmpty()){
            int id = (Integer) keys.iterator().next();
            currentPosition.remove(id);
            if (model == "GPR2005"){
                book[0].remove(id);
                traders.get(id).cancel();
                if (traderIDsNonHFT.contains(id)){
                    traderIDsNonHFT.remove(traderIDsNonHFT.indexOf(id));
                } else if (traderIDsHFT.contains(id)){
                    traderIDsHFT.remove(traderIDsHFT.indexOf(id));
                }
                traders.remove(id);
            }
            keys.remove(id);
        }*/
        for (int i = tickChange - 1; i >= 0; i--){
            keys = book[i].keySet();
            while (! keys.isEmpty()){ // removing BLOs from the zero position
                int oID = (Integer) keys.iterator().next();
                Order_test o = book[i].remove(oID);
                int traderID = o.getTraderID();
                book[i].remove(oID);
                if (model == "GPR2005"){
                    //traders.get(traderID).cancel();
                }
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
        /*for (int i = 0; i < nPoints - 1; i++){
            book[i] = book[i + 1];
        }
        book[nPoints - 1] = new LinkedHashMap();
        System.arraycopy(Prices, 1, Prices, 0, nPoints - 1);
        Prices[nPoints - 1] = Prices[nPoints - 2] + tickSize;
        Pt = Math.max(Pt--, 0);                               // not to fall of the grid
        FV = fv;
        BookSizes();*/     // old ending
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
                if ( book[i].get(keys.iterator().next()).isBuyOrder()){
                    while (! keys.isEmpty()){
                        int oID = (Integer) keys.iterator().next();
                        Order_test o = book[i].remove(oID);
                        int traderID = o.getTraderID();
                        if (model == "GPR2005") {
                            //traders.get(traderID).execution(fv, o);
                        } else {traders.get(traderID).execution(fv, et);}
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

        for (int i = nPoints - tickChange; i <= nPoints - 1; i++){
            keys = book[i].keySet();
            while (! keys.isEmpty()){                        // removing SLOs from the zero position
                int oID = (Integer) keys.iterator().next();
                Order_test o = book[i].remove(oID);
                int traderID = o.getTraderID();
                book[i].remove(oID);
                if (model == "GPR2005"){
                    //traders.get(traderID).cancel();
                }
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

    public Hashtable<Byte, Byte> getRank (int traderID){

        /*priorities.put(nPoints, (byte) (nPoints + 1)); // trader's position in previous action
        // positions , priorities table & last item is isReturning? 1 if yes, Price in previous action
        for (int i = 0; i < nPoints; i++){
            priorities.put((byte) i, (byte) Math.min(book[i].size(), maxDepth));
        }
        if (currentPosition.containsKey(traderID)){
            int pos = currentPosition.get(traderID) - positionShift;
            //System.out.println("getRank position is " + pos + " shift is " + positionShift);
            priorities.put(nPoints, (byte) pos); // trader's position from previous action
            int rank = 0;
            Iterator it = book[pos].keySet().iterator(); //pos != currentPosition
            while (! it.next().equals(traderID)){
                rank ++;
            }
            priorities.put((byte) pos, (byte)Math.min(rank,maxDepth));
        }*/
        return priorities;
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
        j = nPoints - 1;
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

    public void transactionRule(int oID, ArrayList<Order_test> orders){
        hist.addOrderData(BookInfo[1] - BookInfo[0]);          // quoted spread
        // cancel previous LO unless not retained
        /*Integer oldPos = null;
        if(currentPosition.containsKey(oID)){
            oldPos = currentPosition.get(oID) - positionShift;
            if (oldPos != pos || o.isBuyOrder() != book[oldPos].get(oID).isBuyOrder()){
                book[oldPos].remove(oID);
                currentPosition.remove(oID);
            } else {
                return;
            }
        }*/

        HashMap<Integer, Integer> tempHM = new HashMap<Integer, Integer>();
        if (CurrentPosition.containsKey(oID)){
            tempHM = CurrentPosition.get(oID);
        }
        Integer tempSize = 0;
        Integer pos = null;
        for (Order_test o : orders){
            if (pos == null || pos != o.getPosition()){
                // put here tempSize to tempHM if not empty and position not null
                if (pos != null && tempSize != 0) {tempHM.put(pos + positionShift, tempSize);}
                pos = o.getPosition();
                tempSize = tempHM.containsKey(pos + positionShift) ? tempHM.get(pos + positionShift)
                                                                   : 0;
            }
            if (o.isBuyOrder()){
                if (book[pos].size() > 0 && !book[pos].get(book[pos].keySet().iterator().next()).isBuyOrder()){
                    Order_test cp = book[pos].remove(book[pos].keySet().iterator().next());
                    Integer CPid = cp.getTraderID();
                    if (model == "GPR2005") {
                        //traders.get(CPid).execution(FV);
                    } else {traders.get(CPid).execution(FV, o.getTimeStamp());}
                    Pt = pos;                                       // sets last transaction position
                    b = 1;                                          // sets last transaction direction, buy = 1
                    //hist.addTrade(o, cp, o.getTimeStamp(), Prices[pos], FV);
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
                    book[pos].put(OrderID,o);           // put some key number here
                }

            } else {
                if (book[pos].size() > 0 && book[pos].get(book[pos].keySet().iterator().next()).isBuyOrder()){
                    Order_test cp = book[pos].remove(book[pos].keySet().iterator().next());
                    Integer CPid = cp.getTraderID();
                    if (model == "GPR2005") {
                        //traders.get(CPid).execution(FV);
                    } else {traders.get(CPid).execution(FV, o.getTimeStamp());}
                    Pt = pos;           // set last transaction price
                    b = 0;              // set last transaction direction, 0=sell
                    //hist.addTrade(cp, o, o.getTimeStamp(), Prices[pos], FV);
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
                    book[pos].put(OrderID,o);           // put some key number here
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
        for (int i = 0; i < nPoints; i++){
            int size = book[i].size();
            if (size != 0){
                boolean buy = book[i].get(book[i].keySet().iterator().next()).isBuyOrder();  // buy orders at book[i]?
                BookSizes[i] = buy ? Math.min(size, maxDepth) : - Math.min(size, maxDepth);  // max size at each tick is maxDepth- 7 or 15
            } else BookSizes[i] = 0;
        }
    }

    public void tryCancel(int id){   // if order is null after returning, see if there's sth to cancel
        /*if(CurrentPosition.containsKey(id)){
            Integer oldPos = CurrentPosition.get(id) - positionShift;
            book[oldPos].remove(id);
        }*/
        BookSizes();
    }

    public boolean isBuyOrder(int id){
        boolean isBuy = false;
        /*if(CurrentPosition.containsKey(id)){
            Integer oldPos = CurrentPosition.get(id) - positionShift;
            isBuy = book[oldPos].get(id).isBuyOrder();
        }*/
        return isBuy;
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

    public int getPositionShift(){
        return positionShift;
    }
}
