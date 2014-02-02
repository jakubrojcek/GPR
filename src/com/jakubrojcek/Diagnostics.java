package com.jakubrojcek;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: rojcek
 * Date: 30.11.12
 * Time: 17:55
 * To change this template use File | Settings | File Templates.
 */
public class Diagnostics {

    private byte nP;
    private int e;
    double diff;
    int[] cancelCount;
    int[] countC;
    int count;
    HashMap<Short, Integer> actions;
    HashMap<Short, Double> payoffs;

    public Diagnostics(byte numberPrices,int e){
        this.nP = numberPrices;
        this.e = e;
        diff = 0.0;
        count = 0;
        cancelCount = new int[6];
        countC = new int[6];
        actions = new HashMap<Short, Integer>(2 * e + 5);
        payoffs = new HashMap<Short, Double>(2 * e + 5);
        for (short i = 0; i < (2 * e + 5); i++){
            actions.put(i, 0);
            payoffs.put(i, 0.0);
        }
    }

    public void addDiff(double d){
        diff = diff + d;
        count++;
    }

    public void addCancelCount(int cc, int pv, boolean hft){
        if (hft){
            cancelCount[5] += cc;
            countC[5]++;
        } else {
            cancelCount[pv] += cc;
            countC[pv]++;
        }
        countC[pv]++;
    }

    public void addAction(Short[] ac, byte u2t, double max){
        for (int i = 0; i < u2t; i++){
            if (!actions.containsKey(ac[i])){
                actions.put(ac[i], 1);
            } else {
                int n = actions.get(ac[i]);
                actions.put(ac[i], n + 1);
            }
            if (!payoffs.containsKey(ac[i])){
                payoffs.put(ac[i], max);
            } else {
                double f = payoffs.get(ac[i]);
                payoffs.put(ac[i], f + max);
            }
        }
    }
    public void addAction(short ac){
        if (!actions.containsKey(ac)){
            actions.put(ac, 1);
        } else {
            int n = actions.get(ac);
            actions.put(ac, n + 1);
        }
        if (!payoffs.containsKey(ac)){
            payoffs.put(ac, 0.0);
        } else {
            double f = payoffs.get(ac);
            payoffs.put(ac, f + 0.0);
        }
    }

    // printing diagnostics here
   public String printDiagnostics(String version){
        String s = new String();
        if (version == "diffs"){
            s = count + ";";
            s = s + diff + "\r";
        } else if (version == "actions"){
            int sz = actions.size();
            for (short i = 0; i < sz; i++){
                s = s + actions.get(i) + ";";
            }
            s = s + "\r";
        } else if (version == "payoffs"){
            int sz = payoffs.size();
            for (short i = 0; i < sz; i++){
                s = s + (double)(payoffs.get(i) / actions.get(i)) + ";";
            }
            s = s + "\r";
        } else if (version == "cancellations"){
            for (int i = 0; i < 6; i++){
                s = s + ((double)cancelCount[i] / Math.max(countC[i],1)) + ";";
            }
            s = s + "\r";
        }
        return s;
    }
}
