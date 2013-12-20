import com.jakubrojcek.Order;
import com.jakubrojcek.gpr2005a.*;
import com.jakubrojcek.hftRegulation.History;
import com.jakubrojcek.hftRegulation.LOB_LinkedHashMap;
import com.jakubrojcek.hftRegulation.*;
import com.jakubrojcek.hftRegulation.SingleRun;
import com.jakubrojcek.hftRegulation.Trader;

import java.io.FileWriter;
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
        double timeStamp1 = System.currentTimeMillis();
        SingleCase case1 = new SingleCase();
        String[] params = new String[10];
        params[0] = "_baseCase\\";   // folder
        params[1] = "0";            // new HFTs
        params[2] = "2";            // # of positive PV slow traders
        params[3]= "4";             // # of zero PV slow traders
        params[4] = "2";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.5";          // lambda of new arrivals
        params[9] = "0.125";        // arrival of changes in FV

        case1.main(params);
        case1 = null;

        SingleCase case2 = new SingleCase();
        params[0] = "_2baseCaseHFT\\";
        params[1] = "1";            // new HFTs
        params[2] = "2";            // # of positive PV slow traders
        params[3]= "4";             // # of zero PV slow traders
        params[4] = "2";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.5";          // lambda of new arrivals
        params[9] = "0.125";        // arrival of changes in FV
        case2.main(params);
        case2 = null;

        SingleCase case3 = new SingleCase();
        params[0] = "_3baseCaseHFTsameLambda\\";
        params[1] = "1";            // new HFTs
        params[2] = "2";            // # of positive PV slow traders
        params[3]= "4";             // # of zero PV slow traders
        params[4] = "2";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.25";          // lambda of new arrivals
        params[9] = "0.125";        // arrival of changes in FV
        case3.main(params);
        case3 = null;

        SingleCase case4 = new SingleCase();
        params[0] = "_4baseCaseHighVol\\";
        params[1] = "0";            // new HFTs
        params[2] = "2";            // # of positive PV slow traders
        params[3]= "4";             // # of zero PV slow traders
        params[4] = "2";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.5";          // lambda of new arrivals
        params[9] = "0.25";         // arrival of changes in FV
        case4.main(params);
        case4 = null;

        /*SingleCase case5 = new SingleCase();
        params[0] = "_5baseCaseHftHighVol\\";
        params[1] = "1";            // new HFTs
        params[2] = "2";            // # of positive PV slow traders
        params[3]= "4";             // # of zero PV slow traders
        params[4] = "2";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.5";          // lambda of new arrivals
        params[9] = "0.25";         // arrival of changes in FV
        case5.main(params);
        case5 = null;

        SingleCase case6 = new SingleCase();
        params[0] = "_6baseCaseHftTIF1\\";
        params[1] = "1";            // new HFTs
        params[2] = "2";            // # of positive PV slow traders
        params[3]= "4";             // # of zero PV slow traders
        params[4] = "2";            // # of negative PV slow traders
        params[5] = "0.1";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.5";          // lambda of new arrivals
        params[9] = "0.125";        // arrival of changes in FV
        case6.main(params);
        case6 = null;

        SingleCase case7 = new SingleCase();
        params[0] = "_7baseCaseHftTIF2\\";
        params[1] = "1";            // new HFTs
        params[2] = "2";            // # of positive PV slow traders
        params[3]= "4";             // # of zero PV slow traders
        params[4] = "2";            // # of negative PV slow traders
        params[5] = "0.2";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.5";          // lambda of new arrivals
        params[9] = "0.125";        // arrival of changes in FV
        case7.main(params);
        case7 = null;

        SingleCase case9 = new SingleCase();
        params[0] = "_9baseCaseHftTTAX\\";
        params[1] = "1";            // new HFTs
        params[2] = "2";            // # of positive PV slow traders
        params[3]= "4";             // # of zero PV slow traders
        params[4] = "2";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.00375";      // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.5";          // lambda of new arrivals
        params[9] = "0.125";        // arrival of changes in FV
        case9.main(params);
        case9 = null;*/


        double timeStamp2 = System.currentTimeMillis();
        System.out.println("It took: " + (timeStamp2 - timeStamp1));
    }
}
