package com.jakubrojcek;

/**
 * Created with IntelliJ IDEA.
 * User: rojcek
 * Date: 27.09.13
 * Time: 17:56
 * To change this template use File | Settings | File Templates.
 */
public class BeliefQ implements java.io.Serializable {
    private int n = 0;                  // number of times updated
    private int ne = 0;                 // number of times executed
    private double q;                   // execution probability or Q factor in continuous time
    private double diff;                // difference of new vs old execution probability
    private double nC;                  // expected number of cancellations

    public BeliefQ(int n, double q) {
        this.n = n;
        this.q = q;
    }

    public BeliefQ(int n, double q, double nc) {
        this.n = n;
        this.q = q;
        this.nC = nc;
    }

    public BeliefQ(int n, int ne, double q, double diff) {
        this.n = n;
        this.ne = ne;
        this.q = q;
        this.diff = diff;
    }



    public int getN() {
        return n;
    }

    public double getQ() {
        return q;
    }

    public double getnC() {
        return nC;
    }

    public void setN(int n) {
        this.n = n;
    }

    public void setQ(double q) {
        this.q = q;
    }

    public void setnC(double nc) {
        this.nC = nc;
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
