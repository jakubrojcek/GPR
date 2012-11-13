import java.util.*;
import java.lang.Math;


/**
 * Created by IntelliJ IDEA.
 * User: Jakub
 * Date: 6.3.12
 * Time: 11:28
 * To change this template use File | Settings | File Templates.
 */
public class LOB_LinkedHashMap {
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
    Hashtable<Byte, Byte> priorities = new Hashtable<Byte, Byte>();
    // priorities HashTable <position, priority>
    ArrayList<Integer> traderIDsHFT = new ArrayList<Integer>();
    ArrayList<Integer> traderIDsNonHFT = new ArrayList<Integer>();
    //vectors holding traderIDs, HFT or nonHFT, traderID and position, price and position
    Hashtable<Integer, Integer> currentPosition = new Hashtable<Integer, Integer>();
    // traderID, position of price + positionShift


    LinkedHashMap<Integer, Order>[] book; // price position and orders // TODO: to byte later
    History hist;
    HashMap<Integer, Trader> traders;


    public LOB_LinkedHashMap(double fv, int FVpos, int md, int e, double ts, byte nP, History h, HashMap<Integer, Trader> t){
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

    public void FVup(double fv, double et){
        // (1) book shift, (2) 31+1, (3) currentPosition, (4) pricePosition
        positionShift++;
        //System.out.println("Keys to delete are " + keys);
        Set keys = book[1].keySet();
        while (! keys.isEmpty()){          // this part executes the SLOs against fringe
            int id = (Integer) keys.iterator().next();
            if (! book[1].get(id).isBuyOrder()){
                System.out.println("Seller executed when FV moved up"); // TODO: check if OK, too often
                traders.get(id).execution(fv, et);
                if (traderIDsNonHFT.contains(id)){
                    traderIDsNonHFT.remove(traderIDsNonHFT.indexOf(id));
                } else if (traderIDsHFT.contains(id)){
                    traderIDsHFT.remove(traderIDsHFT.indexOf(id));
                }
                currentPosition.remove(id);
                keys.remove(keys.iterator().next());
            }
        }

        keys = book[0].keySet();
        while (! keys.isEmpty()){                        // removing BLOs from the zero position
            int id = (Integer) keys.iterator().next();
            currentPosition.remove(id);
            keys.remove(keys.iterator().next());
        }

        for (int i = 0; i < nPoints - 1; i++){
            book[i] = book[i + 1];
        }
        book[nPoints - 1] = new LinkedHashMap();
        System.arraycopy(Prices, 1, Prices, 0, nPoints - 1);
        Prices[nPoints - 1] = Prices[nPoints - 2] + tickSize;
        Pt = Math.max(Pt--, 0);                               // not to fall of the grid
        FV = fv;      
    }

    public void FVdown(double fv, double et){
        // (1) book shift, (2) 31-1, (3) currentPosition, (4) pricePosition
        positionShift--;

        Set keys = book[nPoints - 2].keySet();
        //System.out.println("Keys to delete are " + keys);
        while (! keys.isEmpty()){
            int id = (Integer) keys.iterator().next();
            if (book[nPoints - 2].get(id).isBuyOrder()){ // Buyer executed when FV moved down
                System.out.println("Buyer executed when FV moved down");
                traders.get(id).execution(fv, et);
                if (traderIDsNonHFT.contains(id)){
                    traderIDsNonHFT.remove(traderIDsNonHFT.indexOf(id));
                } else if (traderIDsHFT.contains(id)){
                    traderIDsHFT.remove(traderIDsHFT.indexOf(id));
                }
                currentPosition.remove(keys.iterator().next());
                keys.remove(keys.iterator().next());
            }
        }

        keys = book[nPoints - 1].keySet();
        while (! keys.isEmpty()){                        // removing SLOs from the zero position
            int id = (Integer) keys.iterator().next();
            currentPosition.remove(id);
            keys.remove(keys.iterator().next());
        }

        for (int i = nPoints - 1; i > 0; i--){
            book[i] = book[i - 1];
        }
        book[0] = new LinkedHashMap();
        System.arraycopy(Prices, 0, Prices, 1, nPoints - 1);
        Prices[0] = Prices[1] - tickSize;
        Pt = Math.min(Pt++, nPoints - 1);                        // not to fall of the grid
        FV = fv;
    }
    
    public Hashtable<Byte, Byte> getRank (int traderID){

        priorities.put(nPoints, (byte) 0); // trader's position in previous action
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
            //System.out.println("keySet iterator " + book[pos].keySet());
            while (! it.next().equals(traderID)){
                rank ++;
            }
            priorities.put((byte) pos, (byte)Math.min(rank,maxDepth));
        }
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
        Ds = Math.max(Ds, - maxSumDepth); // limited to 90

        BookInfo[0] = Bt;                 // best bid position
        BookInfo[1] = At;                 // best ask position
        BookInfo[2] = BookSizes[Bt];      // depth at best bid
        BookInfo[3] = - BookSizes[At];    // depth at best ask
        BookInfo[4] = Db;                 // depth buys
        BookInfo[5] = - Ds;               // depth sells
        BookInfo[6] = Pt;                 // last transaction position
        BookInfo[7] = b;                  // 1 if last transaction buy, 0 if sell
        if (BookInfo[2] < 0){
            //System.out.println("ou shit, negative depth at the best bid");
        }

        return BookInfo;
    }
       
     public void transactionRule(int pos, Order o){
         int oID = o.getTraderID();
         Integer oldPos = null;
         // cancel previous LO unless not retained
         if(currentPosition.containsKey(oID)){
             oldPos = currentPosition.get(oID) - positionShift;
             if (oldPos != pos){
                 book[oldPos].remove(oID);
                 currentPosition.remove(oID);
             } else {
                 return;
             }
         }

         if (traderIDsHFT.contains(oID)){
            traderIDsHFT.remove(traderIDsHFT.indexOf(oID));
         } else if (traderIDsNonHFT.contains(oID)){
            traderIDsNonHFT.remove(traderIDsNonHFT.indexOf(oID));
         }

         if (o.isBuyOrder()){
            if (book[pos].size() > 0 && !book[pos].get(book[pos].keySet().iterator().next()).isBuyOrder()){
                Order cp = book[pos].remove(book[pos].keySet().iterator().next());
                Integer cpID = cp.getTraderID();
                // enters transaction history
                Pt = pos; // sets last transaction position
                b = 1;            // sets last transaction direction, buy = 1
                traders.get(cpID).execution(FV, cp.getTimeStamp());
                hist.addTrade(o, cp, o.getTimeStamp(), Prices[pos], FV);
                currentPosition.remove(cpID);
                if (traders.get(cpID).getIsHFT()){
                    traderIDsHFT.remove(traderIDsHFT.indexOf(cpID)); // removes from the list of HFT traders
                }else {
                    traderIDsNonHFT.remove(traderIDsNonHFT.indexOf(cpID));
                }
                traders.remove(oID);            //garbage collecting
                traders.remove(cpID);
            } else if (pos == nPoints - 1){     // if BMO executed against fringe
                return;
            }
            else{
                book[pos].put(oID, o);          // put some number here
                currentPosition.put(oID, pos + positionShift);
                if (traders.get(oID).getIsHFT()){
                    traderIDsHFT.add(oID);
                } else{
                    traderIDsNonHFT.add(oID);
                }
            }

         } else {
            if (book[pos].size() > 0 && book[pos].get(book[pos].keySet().iterator().next()).isBuyOrder()){
                Order cp = book[pos].remove(book[pos].keySet().iterator().next());
                Integer cpID = cp.getTraderID();
                Pt = pos; // set last transaction price
                b = 0;            // set last transaction direction, 0=sell
                traders.get(cpID).execution(FV, cp.getTimeStamp());
                hist.addTrade(cp, o, o.getTimeStamp(), Prices[pos], FV);// enters transaction history
                currentPosition.remove(cpID);
                if (traders.get(cpID).getIsHFT()){
                    traderIDsHFT.remove(traderIDsHFT.indexOf(cpID)); // removes from the list of HFT traders
                }else {
                    traderIDsNonHFT.remove(traderIDsNonHFT.indexOf(cpID));
                }
                traders.remove(oID);   // garbage collecting
                traders.remove(cpID);
            } else if (pos == 0){      // if SMO executed against fringe
                return;
            }
            else{
                book[pos].put(oID,o);// put some key number here
                currentPosition.put(oID, pos + positionShift);
                if (traders.get(oID).getIsHFT()){
                    traderIDsHFT.add(oID);
                } else{
                    traderIDsNonHFT.add(oID);
                }
            }
        }
         for (int i = 0; i < nPoints; i++){
             int size = book[i].size();
             if (size != 0){
                 boolean buy = book[i].get(book[i].keySet().iterator().next()).isBuyOrder();  // buy orders at book[i]?
                 BookSizes[i] = buy ? Math.min(size, maxDepth) : - Math.min(size, maxDepth);   // max size at each tick is 15
             } else BookSizes[i] = 0;
         }
    }

    public void tryCancel(int id){   // if order is null after returning, see if there's sth to cancel
        if(currentPosition.containsKey(id)){
            Integer oldPos = currentPosition.remove(id) - positionShift;
            book[oldPos].remove(id);
        }
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
        System.out.println("non HFTs " + traderIDsNonHFT);
        System.out.println("HFTs " + traderIDsHFT);
        System.out.println(BookInfo[0] + " best bid @ position = " + BookInfo[0] + " shift " + positionShift);
        System.out.println(BookInfo[1] + " best ask @ position = " + BookInfo[1] + " shift " + positionShift);
        System.out.println(BookInfo[2] + " depth at best bid");
        System.out.println(BookInfo[3] + " depth at best ask");
        System.out.println(BookInfo[4] + " depth buys");
        System.out.println(BookInfo[5] + " depth sells");
        System.out.println(BookInfo[6] + " last transaction price");
        System.out.println(BookInfo[7] + " 1 if last transaction buy, 0 if sell");
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

    public int getBestBid(){
        return BookInfo[0];
    }

    public int getBestAsk(){
        return BookInfo[1];
    }
}
