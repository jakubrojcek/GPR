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
        String[] params = new String[14];

        /*SingleCase case1 = new SingleCase();
        params[0] = "_1baseCase9\\";   // folder
        params[1] = "0";            // new HFTs
        params[2] = "0";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "0";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.5";          // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth

        case1.main(params);
        case1 = null;*/

        /*SingleCase case2 = new SingleCase();
        params[0] = "_2baseCaseHFT10\\";   // folder
        params[1] = "2";            // new HFTs
        params[2] = "0";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "0";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.5";          // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        case2.main(params);
        case2 = null;*/

        /*SingleCase case3 = new SingleCase();
        params[0] = "_3baseCaseHFTsameLambda2\\";
        params[1] = "1";            // new HFTs
        params[2] = "0";            // # of positive PV slow traders
        params[3]= "1";             // # of zero PV slow traders
        params[4] = "0";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.1";          // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        case3.main(params);
        case3 = null;

        SingleCase case4 = new SingleCase();
        params[0] = "_4baseCaseHighVol2\\";
        params[1] = "0";            // new HFTs
        params[2] = "0";            // # of positive PV slow traders
        params[3]= "1";             // # of zero PV slow traders
        params[4] = "0";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.2";          // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.25";        // arrival of changes in FV
        params[13] = "30";          // max depth
        case4.main(params);
        case4 = null;

        SingleCase case5 = new SingleCase();
        params[0] = "_5baseCaseHftHighVol2\\";
        params[1] = "1";            // new HFTs
        params[2] = "0";            // # of positive PV slow traders
        params[3]= "1";             // # of zero PV slow traders
        params[4] = "0";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.2";          // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.25";        // arrival of changes in FV
        params[13] = "30";          // max depth
        case5.main(params);
        case5 = null;*/

        /*SingleCase case6 = new SingleCase();
        params[0] = "_6baseCaseHftTIF12\\";
        params[1] = "1";            // new HFTs
        params[2] = "0";            // # of positive PV slow traders
        params[3]= "1";             // # of zero PV slow traders
        params[4] = "0";            // # of negative PV slow traders
        params[5] = "0.1";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.2";          // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        case6.main(params);
        case6 = null;*/

        SingleCase case7 = new SingleCase();
        params[0] = "_7baseCaseHftTIF23\\";
        params[1] = "1";            // new HFTs
        params[2] = "0";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "0";            // # of negative PV slow traders
        params[5] = "0.2";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.5";          // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        case7.main(params);
        case7 = null;

        /*SingleCase case8 = new SingleCase();
        params[0] = "_8baseCaseHftCFEE2\\";
        params[1] = "1";            // new HFTs
        params[2] = "0";            // # of positive PV slow traders
        params[3]= "1";             // # of zero PV slow traders
        params[4] = "0";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.01";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.2";          // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth

        SingleCase case9 = new SingleCase();
        params[0] = "_9baseCaseHftTTAX2\\";
        params[1] = "1";            // new HFTs
        params[2] = "0";            // # of positive PV slow traders
        params[3]= "1";             // # of zero PV slow traders
        params[4] = "0";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.00375";      // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.2";          // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        case9.main(params);
        case9 = null;*/

        /*SingleCase case10 = new SingleCase();
        params[0] = "_10baseCaseMT6\\";
        params[1] = "1";            // new HFTs
        params[2] = "0";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "0";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.012";          // LO make fee
        params[9] = "0.018";          // MO take fee
        params[10] = "0.5";         // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";       // arrival of changes in FV
        params[13] = "30";          // max depth
        case10.main(params);
        case10 = null;*/

        /*SingleCase case10 = new SingleCase();
        params[0] = "_8baseCaseHftCFEEnew\\";
        params[1] = "1";            // new HFTs
        params[2] = "2";            // # of positive PV slow traders
        params[3]= "4";             // # of zero PV slow traders
        params[4] = "2";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.01";         // CFEE
        params[8] = "0.5";          // lambda of new arrivals
        params[9] = "0.125";        // arrival of changes in FV
        params[10] = "30";          // max depth
        case10.main(params);
        case10 = null;*//*SingleCase case10 = new SingleCase();
        params[0] = "_8baseCaseHftCFEEnew\\";
        params[1] = "1";            // new HFTs
        params[2] = "2";            // # of positive PV slow traders
        params[3]= "4";             // # of zero PV slow traders
        params[4] = "2";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.01";         // CFEE
        params[8] = "0.5";          // lambda of new arrivals
        params[9] = "0.125";        // arrival of changes in FV
        params[10] = "30";          // max depth
        case10.main(params);
        case10 = null;*/

        /*SingleCase case11 = new SingleCase();
        params[0] = "_8baseCaseHftCFEElow\\";
        params[1] = "1";            // new HFTs
        params[2] = "2";            // # of positive PV slow traders
        params[3]= "4";             // # of zero PV slow traders
        params[4] = "2";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.00375";         // CFEE
        params[8] = "0.5";          // lambda of new arrivals
        params[9] = "0.125";        // arrival of changes in FV
        params[10] = "30";          // max depth
        case11.main(params);
        case11 = null;*/

        /*SingleCase case12 = new SingleCase();
        params[0] = "_11trial\\";
        params[1] = "1";            // new HFTs
        params[2] = "2";            // # of positive PV slow traders
        params[3]= "4";             // # of zero PV slow traders
        params[4] = "2";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.5";          // lambda of new arrivals
        params[9] = "0.125";        // arrival of changes in FV
        params[10] = "30";          // max depth
        case12.main(params);
        case12 = null;*/

        /*SingleCase case13 = new SingleCase();
        params[0] = "_12baseCaseHighActivity\\";
        params[1] = "0";            // new HFTs
        params[2] = "4";            // # of positive PV slow traders
        params[3]= "8";             // # of zero PV slow traders
        params[4] = "4";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.5";          // lambda of new arrivals
        params[9] = "0.125";        // arrival of changes in FV
        params[10] = "30";          // max depth
        case13.main(params);
        case13 = null;*/

        /*SingleCase case13 = new SingleCase();
        params[0] = "_13baseCaseMoreSlow\\";
        params[1] = "0";            // new HFTs
        params[2] = "2";            // # of positive PV slow traders
        params[3]= "5";             // # of zero PV slow traders
        params[4] = "2";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.5";          // lambda of new arrivals
        params[9] = "0.125";        // arrival of changes in FV
        params[10] = "30";          // max depth
        case13.main(params);
        case13 = null;*/

        /*SingleCase case14 = new SingleCase();
        params[0] = "_14baseCaseMoreSlowSpec\\";
        params[1] = "0";            // new HFTs
        params[2] = "2";            // # of positive PV slow traders
        params[3]= "5";             // # of zero PV slow traders
        params[4] = "2";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.5";          // lambda of new arrivals
        params[9] = "0.125";        // arrival of changes in FV
        params[10] = "30";          // max depth
        case14.main(params);
        case14 = null;*/

        /*SingleCase case15 = new SingleCase();
        params[0] = "_15baseCaseHFT50a\\";
        params[1] = "8";            // new HFTs
        params[2] = "2";            // # of positive PV slow traders
        params[3]= "4";             // # of zero PV slow traders
        params[4] = "2";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.5";          // lambda of new arrivals
        params[9] = "0.125";        // arrival of changes in FV
        params[10] = "30";          // max depth
        case15.main(params);
        case15 = null;*/

        /*SingleCase case16 = new SingleCase();
        params[0] = "_16baseCaseHFT50aHighVol\\";
        params[1] = "8";            // new HFTs
        params[2] = "2";            // # of positive PV slow traders
        params[3]= "4";             // # of zero PV slow traders
        params[4] = "2";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.5";          // lambda of new arrivals
        params[9] = "0.25";        // arrival of changes in FV
        params[10] = "30";          // max depth
        case16.main(params);
        case16 = null;*/

        /*SingleCase case17 = new SingleCase();
        params[0] = "_17baseCaseHFT50k\\";
        params[1] = "1";            // new HFTs
        params[2] = "0";            // # of positive PV slow traders
        params[3]= "1";             // # of zero PV slow traders
        params[4] = "0";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.5";         // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";       // arrival of changes in FV
        params[13] = "30";          // max depth
        case17.main(params);
        case17 = null;*/

        /*SingleCase case18 = new SingleCase();
        params[0] = "_18baseCaseMTg\\";
        params[1] = "1";            // new HFTs
        params[2] = "2";            // # of positive PV slow traders
        params[3]= "4";             // # of zero PV slow traders
        params[4] = "2";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.012";          // LO make fee
        params[9] = "0.018";          // MO take fee
        params[10] = "0.5";         // lambda of new arrivals
        params[11] = "0.15f";       // rho- impatience parameters
        params[12] = "0.125";       // arrival of changes in FV
        params[13] = "30";          // max depth
        case18.main(params);
        case18 = null;*/

        /*SingleCase case18 = new SingleCase();
        params[0] = "TIF\\";
        params[1] = "1";            // new HFTs
        params[2] = "2";            // # of positive PV slow traders
        params[3]= "4";             // # of zero PV slow traders
        params[4] = "2";            // # of negative PV slow traders
        params[5] = "0.2";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.5";         // lambda of new arrivals
        params[11] = "0.125";       // arrival of changes in FV
        params[12] = "30";          // max depth
        case18.main(params);
        case18 = null;*/

        double timeStamp2 = System.currentTimeMillis();
        System.out.println("It took: " + (timeStamp2 - timeStamp1));


    }
}
