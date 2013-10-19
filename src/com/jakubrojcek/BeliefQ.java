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
    private int ne = 0;               // number of times executed
    private double q;                 // execution probability or Q factor in continuous time
    private double diff;              // difference of new vs old execution probability

    public BeliefQ(int n, double q) {
        this.n = n;
        this.q = q;
    }

    public int getN() {
        return n;
    }

    public double getQ() {
        return q;
    }

    public void setN(int n) {
        this.n = n;
    }

    public void setQ(double q) {
        this.q = q;
    }

    public void setDiff(double diff) {
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
