package com.jakubrojcek.gpr2005a;

import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: Jakub
 * Date: 29.5.12
 * Time: 22:48
 * To change this template use File | Settings | File Templates.
 */
public class Payoff {
    static float rho;                      // discount rate
    static int nPayoffs;                   // size of payoff vectors
    static byte nReset;                    // nReset-> resets n to specific value for "forced learning"
    static short nResetMax = 32767;        // nReset-> resets n to specific value for "forced learning" 32767 is max short
    static int dof = 0;                    // degrees of freedom for chi^2 TODO: delete afterwards
    static Float[][] mu0;
    static Float[][] deltaV0;
    static int end;
    static int LL;
    static int maxDepth;
    static int nP;
    static Random random = new Random();
    static short[] ac =  new short[2];
    static short[] q =  new short[2];

    // generic constructor
    public Payoff(){}

    // constructor
    public Payoff(float r, int nPs, int e, int ll, int md, int nPrices){
        rho = r;
        nPayoffs = nPs;
        end = e;
        LL = ll;
        maxDepth = md;
        nP = nPrices;
    }

    public void update(){

    }

    public boolean canBeDeleted(){
        return false;
    }

    public void setnReset(byte nSet, short mSet){
        nReset = nSet;
        nResetMax = mSet;
    }
    public void nReset(){}

    public void setDof(int i){
        dof = i;
    }

    public void setInitialBeliefs(Float[][] mu, Float[][] deltaV){
        mu0 = mu;
        deltaV0 = deltaV;
    }

    /*public float[] getP() {
        return p;
    }*/

    /*public void setP(float[] p) {
        this.p = p;
    }*/

    /*public void setMax(float max) {
        this.max = max;
    }*/

   /* public void setMaxIndex(byte maxIndex) {
        this.maxIndex = maxIndex;
    }

    public int getRecursiveStatesCount(){
        return recursiveStatesCount;
    }*/
}
