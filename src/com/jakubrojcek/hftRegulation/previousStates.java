package com.jakubrojcek.hftRegulation;

import com.jakubrojcek.BeliefQ;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: rojcek
 * Date: 15.10.13
 * Time: 19:00
 * To change this template use File | Settings | File Templates.
 */
public class previousStates {
    private HashMap<Long, HashMap<Integer, BeliefQ>> tempStates;

    public void storeStates(HashMap<Long, HashMap<Integer, BeliefQ>> states){
        tempStates = new HashMap<Long, HashMap<Integer, BeliefQ>>();
        long code;
        BeliefQ tempBelief;
        int tempAction;
        HashMap<Integer, BeliefQ> beliefs;
        HashMap<Integer, BeliefQ> tempBeliefs;
        Iterator codes = states.keySet().iterator();
        Iterator actions;
        while (codes.hasNext()){
            code = (Long) codes.next();
            beliefs = states.get(code);
            actions = beliefs.keySet().iterator();
            tempBeliefs = new HashMap<Integer, BeliefQ>();
            while (actions.hasNext()){
                tempAction = (Integer) actions.next();
                tempBelief = beliefs.get(tempAction);
                tempBeliefs.put(tempAction, new BeliefQ(tempBelief.getN(), tempBelief.getQ()));
            }
            tempStates.put(code, tempBeliefs);
        }
    }

    public HashMap<Long, HashMap<Integer, BeliefQ>> getTempStates(){
        return tempStates;
    }
}
