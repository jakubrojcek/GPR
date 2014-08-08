import com.jakubrojcek.Order;
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
        String[] params = new String[20];

        SingleCase case1 = new SingleCase();
        params[0] = "_1baseCase18\\";   // folder
        params[1] = "0";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.075";        // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "1.0";         // information delay of uninformed traders

        case1.main(params);
        case1 = null;

        /*SingleCase case2 = new SingleCase();
        params[0] = "_2baseCaseHFT153\\";   // folder
        params[1] = "4";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.075";        // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "1.0";         // information delay of uninformed traders
        case2.main(params);
        case2 = null;*/




        /*SingleCase case4 = new SingleCase();
        params[0] = "_19GPRbc1\\";   // folder
        params[1] = "0";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "1.0";        // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "30";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders
        case4.main(params);
        case4 = null;

        SingleCase case3 = new SingleCase();
        params[0] = "_19GPRbc2\\";   // folder
        params[1] = "1";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "1.0";        // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "30";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "16.0";         // information delay of uninformed traders
        case3.main(params);
        case3 = null;

        SingleCase case2 = new SingleCase();
        params[0] = "_19GPRbc3\\";   // folder
        params[1] = "0";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.01667";        // lambda of new arrivals
        params[11] = "0.000833f";       // rho- impatience parameters
        params[12] = "0.01667";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "30";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "960.0";         // information delay of uninformed traders
        case2.main(params);
        case2 = null;*/

        /*SingleCase case2 = new SingleCase();
        params[0] = "_2baseCaseHFT95\\";   // folder
        params[1] = "0";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "25.0";        // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "31";          // number of ticks
        params[17] = "9";           // infoSize
        params[18] = "4";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders
        case2.main(params);
        case2 = null;*/

        /*SingleCase case2 = new SingleCase();
        params[0] = "_2baseCaseHFT150\\";   // folder
        params[1] = "1";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.075";        // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "1.0";         // information delay of uninformed traders
        case2.main(params);
        case2 = null;*/

        /*SingleCase case3 = new SingleCase();
        params[0] = "_3baseCaseHFTsameLambda10\\";
        params[1] = "4";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.075";        // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "1.0";         // information delay of uninformed traders


        case3.main(params);
        case3 = null;*/

        SingleCase case4 = new SingleCase();
        params[0] = "_4baseCaseHighVol12\\";
        params[1] = "0";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.075";        // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "1.0";         // information delay of uninformed traders

        case4.main(params);
        case4 = null;

        SingleCase case5 = new SingleCase();
        params[0] = "_5baseCaseHftHighVol12\\";
        params[1] = "1";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.075";        // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.25";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "1.0";         // information delay of uninformed traders

        case5.main(params);
        case5 = null;

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

        /*SingleCase case7 = new SingleCase();
        params[0] = "_7baseCaseHftTIF29\\";
        params[1] = "1";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.2";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.075";        // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        case7.main(params);
        case7 = null;*/

        /*SingleCase case8 = new SingleCase();
        params[0] = "_8baseCaseHftCFEE45\\";
        params[1] = "1";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.001";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.075";        // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}

        case8.main(params);
        case8 = null;*/

        /*SingleCase case8 = new SingleCase();
        params[0] = "_8baseCaseHftCFEE42\\";
        params[1] = "1";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.01";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.075";        // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}

        case8.main(params);
        case8 = null;

        SingleCase case9 = new SingleCase();
        params[0] = "_8baseCaseHftCFEE41\\";
        params[1] = "1";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0000001";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.075";        // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}

        case9.main(params);
        case9 = null;*/



        /*SingleCase case9 = new SingleCase();
        params[0] = "_8baseCaseHftCFEE32\\";
        params[1] = "1";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0000001";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.1";        // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}

        case9.main(params);
        case9 = null;*/
        /*case8 = new SingleCase();
        params[0] = "_8baseCaseHftCFEE20\\";
        params[7] = "0.1";          // CFEE

        case8.main(params);
        case8 = null;

        case8 = new SingleCase();
        params[0] = "_8baseCaseHftCFEE21\\";
        params[7] = "0.05";          // CFEE

        case8.main(params);
        case8 = null;*/

        /*SingleCase case9 = new SingleCase();
        params[0] = "_9baseCaseHftTTAX9\\";
        params[1] = "1";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.001875";      // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.075";          // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}

        case9.main(params);
        case9 = null;*/

        /*SingleCase case9 = new SingleCase();
        params[0] = "_10baseCaseMT18\\";
        params[1] = "1";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.008";          // LO make fee
        params[9] = "0.012";         // MO take fee
        params[10] = "0.075";          // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";       // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}

        case9.main(params);
        case9 = null;*/

        /*SingleCase case10 = new SingleCase();
        params[0] = "_10baseCaseMT20\\";
        params[1] = "1";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.012";          // LO make fee
        params[9] = "0.018";         // MO take fee
        params[10] = "0.1";          // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";       // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}

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
        params[0] = "_11speedBump20\\";   // folder
        params[1] = "1";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.075";        // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "speedBump";   // model to use
        params[15] = "0.2";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders

        case12.main(params);
        case12 = null;*/


        /*SingleCase case11 = new SingleCase();
        params[0] = "_11speedBump20\\";   // folder
        params[1] = "1";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.075";        // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "speedBump";   // model to use
        params[15] = "0.5";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "1.0";         // information delay of uninformed traders


        case11.main(params);
        case11 = null;*/

        /*SingleCase case11 = new SingleCase();

        params[0] = "_11speedBump10\\";   // folder
        params[1] = "1";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.1";        // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "speedBump";   // model to use
        params[15] = "0.05";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize

        case11.main(params);
        case11 = null;*/

        /*SingleCase case10 = new SingleCase();
        params[0] = "_10baseCaseMTtrial3\\";
        params[1] = "2";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.004";          // LO make fee
        params[9] = "0.006";          // MO take fee
        params[10] = "0.25";         // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";       // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        case10.main(params);
        case10 = null;*/

        /*SingleCase case12 = new SingleCase();
        params[0] = "_18baseCaseMTh\\";   // folder
        params[1] = "4";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";         // lambda of new arrivals
        params[10] = "0.0625";         // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "1.0";       // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}

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
