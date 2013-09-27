package com.jakubrojcek;

/**
 * Created with IntelliJ IDEA.
 * User: rojcek
 * Date: 27.09.13
 * Time: 17:56
 * To change this template use File | Settings | File Templates.
 */
public class BeliefQ {
    private int n = 0;                // number of times updated
    private int ne;               // number of times executed
    private double q;               // execution probability
    private double diff;             // difference of new vs old execution probability

    public BeliefQ(short n, double q) {
        this.n = n;
        this.q = q;
    }

    public int getN() {
        return n;
    }

    public double getQ() {
        return q;
    }

    public void setN(short n) {
        this.n = n;
    }

    public void setQ(float q) {
        this.q = q;
    }

    public void setDiff(float diff) {
        this.diff = diff;
    }

    public double getDiff() {

        return diff;
    }

    public void setNe(short ne) {
        this.ne = ne;
    }

    public int getNe() {
        return ne;
    }

    public void increaseN(){
        n++;
    }

    public void increaseNe(){
        ne++;
    }
}
