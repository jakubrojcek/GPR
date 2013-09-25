package com.jakubrojcek;

/**
 * Created with IntelliJ IDEA.
 * User: rojcek
 * Date: 21.02.13
 * Time: 19:34
 * To change this template use File | Settings | File Templates.
 */
public class Belief {
    private int n = 0;                // number of times updated
    private int ne;               // number of times executed
    private double mu;               // execution probability
    private double deltaV;           // expected change in FV upon execution
    private double diff;             // difference of new vs old execution probability

    public Belief(short n, double mu, double deltaV) {
        this.n = n;
        this.mu = mu;
        this.deltaV = deltaV;
    }

    public int getN() {
        return n;
    }

    public double getMu() {
        return mu;
    }

    public double getDeltaV() {
        return deltaV;
    }

    public void setN(short n) {
        this.n = n;
    }

    public void setMu(float mu) {
        this.mu = mu;
    }

    public void setDeltaV(float deltaV) {
        this.deltaV = deltaV;
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
