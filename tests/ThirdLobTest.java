import com.jakubrojcek.Order;
import com.jakubrojcek.hftRegulation.History;
import com.jakubrojcek.hftRegulation.LOB_LinkedHashMap;
import com.jakubrojcek.hftRegulation.Trader;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jakub
 * Date: 15.3.12
 * Time: 21:23
 * To change this template use File | Settings | File Templates.
 */
public class ThirdLobTest {
    public static void main(String[] args) {
        double timeStamp1 = System.nanoTime();
        String model = "returning";
        String folder = "D:\\_paper1 HFT, MM, rebates and market quality\\Matlab Analysis\\";
        int hti = 5000000;                      // initial capacity for Payoffs HashTable
        int infoSize = 5;                       // 2-bid, ask, 5- GPR 2005, 6-depth at bid,ask, 8-depth off bid,ask
        double prTremble = 0.0;                 // probability of trembling
        byte nP = 9;                            // number of prices tracked by the book, 8 in the base case, 6/11 in tick size experiment
        double lambdaArrival = 1;               // arrival frequency, same for all
        double ReturnFrequencyHFT = 1;          // returning frequency of HFT
        double ReturnFrequencyNonHFT = 0.1;     // returning frequency of NonHFT
        int maxDepth = 15;                      // 0 to 7 which matter
        int FVpos = nP/2;                          // position of the fundamental value
        int HL = FVpos + 3; //                  // Lowest  allowed limit order price.  LL + HL = nP-1 for allowed orders centered around E(v)
        int LL = FVpos - 3; //                  // Highest allowed limit order price
        float tickSize = 0.125f;                // size of one tick
        int end = HL - LL + 1;                  // number of position on the grid for submitting LOs
        int breakPoint = FVpos - LL; //end / 2; // breaking point for positive, negative, represents FV position on the LO grid
        double FV;
        double [] Prices = new double[nP]; // creates vector of the prices, not carrying about ticks now
        for (int i = 0 ; i < nP ; i++){
            Prices[i] = i * tickSize;
        }
        double[] tauB = new double[end];
        /* expected time until the arrival of a new buyer for whom trading on
        the LO yields non-negative payoff */

        double[] tauS = new double[end];
        /* expected time until the arrival of a new seller for whom picking up
        the LO yields non-negative payoff */

        FV = Prices[FVpos];        // TODO: have also FV when lying not on a tick

        HashMap<Integer, Trader> traders = new HashMap<Integer, Trader>(); //trader ID, trader object
        History h = new History(traders, folder); // create history
        LOB_LinkedHashMap book = new LOB_LinkedHashMap(model, FV, FVpos, maxDepth, end, tickSize, nP, h, traders);
        Trader trader = new Trader(infoSize, tauB, tauS, nP, FVpos, tickSize, ReturnFrequencyHFT,
                ReturnFrequencyNonHFT, LL, HL, end, maxDepth, breakPoint, hti, prTremble, folder, book);
        book.makeBook(Prices);

        // testing book sizes
        ArrayList<Order> orders = new ArrayList<Order>();
        Order o1 = new Order(1, 0.1, true, 1, 0, 2);
        Order o2 = new Order(1, 0.1, true, 1, 0, 3);
        Order o3 = new Order(2, 0.1, false, 1, 1, 5);
        Order o4 = new Order(2, 0.1, false, 1, 1, 5);
        orders.add(o1); orders.add(o2);
        book.transactionRule(1, orders);
        orders = new ArrayList<Order>();
        orders.add(o3); orders.add(o4);
        book.transactionRule(2, orders);
        int[] bookSizes;
        bookSizes = book.getBookSizes();
        orders = new ArrayList<Order>();
        orders.add(new Order(3, 0.1, false, 1, 0, 3));
        book.transactionRule(3, orders);
        bookSizes = book.getBookSizes();

        // testing if trader holds belief and order reference
        Trader tr = new Trader(false, 0.0f);
        o1.setPosition(11);
        /* categories of traders: fast, slow, private value: negative, zero, positive
  that means 4 categories of traders. Fast have zero PV */
        double timeStamp2 = System.nanoTime();
        System.out.println(timeStamp2 - timeStamp1);
        // market parameters

    }
}
