package com.jakubrojcek; /**
 * Created by IntelliJ IDEA.
 * User: Jakub
 * Date: 5.3.12
 * Time: 16:37
 * To change this template use File | Settings | File Templates.
 */
import java.util.Arrays;
public class LimitOrderBook {
    int[] LOB; //vector of outstanding signed orders- +buy, -sell

    public void setLOB(int[] book) {
        LOB = book;
    }
    public int[] TransactionRule(int[] LimitOrder){ //LimitOrder = [signed order flow, price/tick]
        LOB[LimitOrder[1]] = LOB[LimitOrder[1]] + LimitOrder[0];
        System.out.println(Arrays.toString(LOB));
        return LOB;
        /*if (LimitOrder[0]<=0) {
            System.out.print("Buy order at price " + LimitOrder[1]);
        }
        else {
            System.out.print("Sell order at price " + LimitOrder[1]);
        } */

    }

}
