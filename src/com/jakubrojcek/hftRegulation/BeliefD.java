package com.jakubrojcek.hftRegulation;

/**
 * Created by rojcek on 19.06.14.
 */
public class BeliefD {
    private short n = 0;                  // number of times updated
    private float d;                   // execution probability or Q factor in continuous time

    public BeliefD(short n, float d) {
        this.n = n;
        this.d = d;
    }

    public void setN(short n) {
        this.n = n;
    }

    public void increaseN(){
        n++;
    }

    public void setD(float d) {
        this.d = d;
    }

    public short getN() {
        return n;
    }

    public float getD() {
        return d;
    }
}
