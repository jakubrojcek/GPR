import com.jakubrojcek.Order;
import com.jakubrojcek.hftRegulation.History;
import com.jakubrojcek.hftRegulation.LOB_LinkedHashMap;
import com.jakubrojcek.hftRegulation.*;
import com.jakubrojcek.hftRegulation.SingleRun;
import com.jakubrojcek.hftRegulation.Trader;

import java.io.File;
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
        String[] params = new String[21];

        /*SingleCase case2 = new SingleCase();
        params[0] = "_2baseCaseHFT190b\\";   // folder
        *//*params[1] = "1.0";            // % of HFTs from 0 PV traders
        params[2] = "1.0";            // % of |0 PV| traders
        params[3]= "2.0";             // % of |2 PV| traders
        params[4] = "1.0";            // % of |4 PV| traders*//*
        params[1] = "0.454545454545";            // % of HFTs from 0 PV traders
        params[2] = "0.44";            // % of |0 PV| traders
        params[3]= "0.32";             // % of |2 PV| traders
        params[4] = "0.24";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.2";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        case2.main(params);
        case2 = null;*/

        /*SingleCase case3 = new SingleCase();
        params[0] = "_2baseCaseHFT185b\\";   // folder
        params[1] = "0.454545454545";            // % of HFTs from 0 PV traders
        params[2] = "0.44";            // % of |0 PV| traders
        params[3]= "0.32";             // % of |2 PV| traders
        params[4] = "0.24";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.04";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        case3.main(params);
        case3 = null;*/

        /*SingleCase case2a = new SingleCase();
        params[0] = "_2baseCaseHFT224a\\";   // folder
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.375";            // % of |0 PV| traders
        params[3]= "0.3571429";             // % of |2 PV| traders
        params[4] = "0.2678571";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders
        case2a.main(params);
        case2a = null;

        SingleCase case3a = new SingleCase();
        params[0] = "_2baseCaseHFT225a\\";   // folder
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.425";            // % of |0 PV| traders
        params[3]= "0.3285714";             // % of |2 PV| traders
        params[4] = "0.2464286";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders
        case3a.main(params);
        case3a = null;

        SingleCase case1b = new SingleCase();
        params[0] = "_2baseCaseHFT223b\\";   // folder
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.325";            // % of |0 PV| traders
        params[3]= "0.3857143";             // % of |2 PV| traders
        params[4] = "0.2892857";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        case1b.main(params);
        case1b = null;

        SingleCase case2b = new SingleCase();
        params[0] = "_2baseCaseHFT224b\\";   // folder
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.375";            // % of |0 PV| traders
        params[3]= "0.3571429";             // % of |2 PV| traders
        params[4] = "0.2678571";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        case2b.main(params);
        case2b = null;



        SingleCase case3b = new SingleCase();
        params[0] = "_2baseCaseHFT225b\\";   // folder
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.425";            // % of |0 PV| traders
        params[3]= "0.3285714";             // % of |2 PV| traders
        params[4] = "0.2464286";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        case3b.main(params);
        case3b = null;*/

        /*SingleCase case4a = new SingleCase();
        params[0] = "_2baseCaseHFT221aa\\";   // folder
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.45";            // % of |0 PV| traders
        params[3]= "0.3142857";             // % of |2 PV| traders
        params[4] = "0.2357143";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders
        case4a.main(params);
        case4a = null;*/

        /*SingleCase case4b = new SingleCase();
        params[0] = "_2baseCaseHFT221bb\\";   // folder
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.45";            // % of |0 PV| traders
        params[3]= "0.3142857";             // % of |2 PV| traders
        params[4] = "0.2357143";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        case4b.main(params);
        case4b = null;

        SingleCase case5a = new SingleCase();
        params[0] = "_2baseCaseHFT222aa\\";   // folder
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.5";            // % of |0 PV| traders
        params[3]= "0.2857143";             // % of |2 PV| traders
        params[4] = "0.2142857";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders
        case5a.main(params);
        case5a = null;

        SingleCase case5b = new SingleCase();
        params[0] = "_2baseCaseHFT222bb\\";   // folder
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.5";            // % of |0 PV| traders
        params[3]= "0.2857143";             // % of |2 PV| traders
        params[4] = "0.2142857";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        case5b.main(params);
        case5b = null;*/


        /*SingleCase case2c = new SingleCase();
        params[0] = "_2baseCaseHFT215b\\";   // folder
        params[1] = "0.25";            // % of HFTs from 0 PV traders
        params[2] = "0.3";            // % of |0 PV| traders
        params[3]= "0.4";             // % of |2 PV| traders
        params[4] = "0.3";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.2";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        case2c.main(params);
        case2c = null;

        SingleCase case2d = new SingleCase();
        params[0] = "_2baseCaseHFT216b\\";   // folder
        params[1] = "0.75";            // % of HFTs from 0 PV traders
        params[2] = "0.3";            // % of |0 PV| traders
        params[3]= "0.4";             // % of |2 PV| traders
        params[4] = "0.3";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.2";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        case2d.main(params);
        case2d = null;

        SingleCase case3c = new SingleCase();
        params[0] = "_2baseCaseHFT217b\\";   // folder
        params[1] = "0.25";            // % of HFTs from 0 PV traders
        params[2] = "0.3";            // % of |0 PV| traders
        params[3]= "0.4";             // % of |2 PV| traders
        params[4] = "0.3";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        case3c.main(params);
        case3c = null;*/

        /*SingleCase case3d = new SingleCase();
        params[0] = "_2baseCaseHFT218b\\";   // folder
        params[1] = "0.75";            // % of HFTs from 0 PV traders
        params[2] = "0.3";            // % of |0 PV| traders
        params[3]= "0.4";             // % of |2 PV| traders
        params[4] = "0.3";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        case3d.main(params);
        case3d = null;*/

        /*SingleCase case3c = new SingleCase();
        params[0] = "_2baseCaseHFT211a\\";   // folder
        params[1] = "1.0";            // % of HFTs from 0 PV traders
        params[2] = "0.3";            // % of |0 PV| traders
        params[3]= "0.4";             // % of |2 PV| traders
        params[4] = "0.3";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.3";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders
        case3c.main(params);
        case3c = null;*/

        /*SingleCase case22 = new SingleCase();
        params[0] = "_2baseCaseHFT177a\\";   // folder
        params[1] = "0.5";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.04";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders
        case22.main(params);
        case22 = null;

        SingleCase case32 = new SingleCase();
        params[0] = "_2baseCaseHFT177b\\";   // folder
        params[1] = "0.5";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.04";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        case32.main(params);
        case32 = null;

        SingleCase case23 = new SingleCase();
        params[0] = "_2baseCaseHFT178a\\";   // folder
        params[1] = "0.75";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.04";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders
        case23.main(params);
        case23 = null;

        SingleCase case33 = new SingleCase();
        params[0] = "_2baseCaseHFT178b\\";   // folder
        params[1] = "0.75";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.04";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        case33.main(params);
        case33 = null;*/

        /*SingleCase case1 = new SingleCase();
        params[0] = "_1baseCase34a\\";   // folder
        params[1] = "0";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.02";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.03";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders

        case1.main(params);
        case1 = null;*/

        /*SingleCase case1b = new SingleCase();
        params[0] = "_1baseCase29\\";   // folder
        params[1] = "0";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.04";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders

        case1b.main(params);
        case1b = null;*/




        /*SingleCase case4 = new SingleCase();
        params[0] = "_19GPRbc4\\";   // folder
        params[1] = "0";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders
        case4.main(params);
        case4 = null;

        SingleCase case3 = new SingleCase();
        params[0] = "_19GPRbc5\\";   // folder
        params[1] = "4";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.125";        // lambda of new arrivals
        params[11] = "0.05f";       // rho- impatience parameters
        params[12] = "0.125";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "16.0";         // information delay of uninformed traders
        case3.main(params);
        case3 = null;

        SingleCase case2 = new SingleCase();
        params[0] = "_19GPRbc6\\";   // folder
        params[1] = "4";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.0020825";        // lambda of new arrivals
        params[11] = "0.000833f";       // rho- impatience parameters
        params[12] = "0.01667";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
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
        params[0] = "_3baseCaseHFTsameLambda11b\\";
        params[1] = "1";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.04";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders


        case3.main(params);
        case3 = null;*/

        /*SingleCase case4 = new SingleCase();
        params[0] = "_4baseCaseHighVol1ax\\";
        params[1] = "0";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.04";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders

        case4.main(params);
        case4 = null;*/

        /*SingleCase case4b = new SingleCase();
        params[0] = "_4baseCaseHighVol1bx\\";
        params[1] = "0";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.04";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.2";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders

        case4b.main(params);
        case4b = null;*/

        /*SingleCase case5a = new SingleCase();
        params[0] = "_5baseCaseHftHighVol2a\\";
        params[1] = "1";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.02";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.06";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders

        case5a.main(params);
        case5a = null;*/

        /*SingleCase case5 = new SingleCase();
        params[0] = "_5baseCaseHftHighVol1bx\\";
        params[1] = "1";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.04";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.2";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders

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

        /*SingleCase case7a = new SingleCase();
        params[0] = "_7baseCaseHftTIF31aa\\";
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.4";            // % of |0 PV| traders
        params[3]= "0.3428571";             // % of |2 PV| traders
        params[4] = "0.2571429";            // % of |4 PV| traders
        params[5] = "0.2";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders
        case7a.main(params);
        case7a = null;

        SingleCase case7b = new SingleCase();
        params[0] = "_7baseCaseHftTIF31bb\\";
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.4";            // % of |0 PV| traders
        params[3]= "0.3428571";             // % of |2 PV| traders
        params[4] = "0.2571429";            // % of |4 PV| traders
        params[5] = "0.2";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        case7b.main(params);
        case7b = null;

        SingleCase case7c = new SingleCase();
        params[0] = "_7baseCaseHftTIF34aa\\";
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.4";            // % of |0 PV| traders
        params[3]= "0.3428571";             // % of |2 PV| traders
        params[4] = "0.2571429";            // % of |4 PV| traders
        params[5] = "0.5";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders
        case7c.main(params);
        case7c = null;

        SingleCase case7d = new SingleCase();
        params[0] = "_7baseCaseHftTIF34bb\\";
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.4";            // % of |0 PV| traders
        params[3]= "0.3428571";             // % of |2 PV| traders
        params[4] = "0.2571429";            // % of |4 PV| traders
        params[5] = "0.5";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        case7d.main(params);
        case7d = null;

        SingleCase case7e = new SingleCase();
        params[0] = "_7baseCaseHftTIF36aa\\";
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.4";            // % of |0 PV| traders
        params[3]= "0.3428571";             // % of |2 PV| traders
        params[4] = "0.2571429";            // % of |4 PV| traders
        params[5] = "1.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders
        case7e.main(params);
        case7e = null;

        SingleCase case7f = new SingleCase();
        params[0] = "_7baseCaseHftTIF36bb\\";
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.4";            // % of |0 PV| traders
        params[3]= "0.3428571";             // % of |2 PV| traders
        params[4] = "0.2571429";            // % of |4 PV| traders
        params[5] = "1.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        case7f.main(params);
        case7f = null;*/

        /*SingleCase case8a = new SingleCase();
        params[0] = "_8baseCaseHftCFEE52aa\\";
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.4";            // % of |0 PV| traders
        params[3]= "0.3428571";             // % of |2 PV| traders
        params[4] = "0.2571429";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.001";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders
        case8a.main(params);
        case8a = null;

        SingleCase case8b = new SingleCase();
        params[0] = "_8baseCaseHftCFEE52bb\\";
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.4";            // % of |0 PV| traders
        params[3]= "0.3428571";             // % of |2 PV| traders
        params[4] = "0.2571429";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.001";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        case8b.main(params);
        case8b = null;

        SingleCase case8c = new SingleCase();
        params[0] = "_8baseCaseHftCFEE51aa\\";
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.4";            // % of |0 PV| traders
        params[3]= "0.3428571";             // % of |2 PV| traders
        params[4] = "0.2571429";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.01";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders
        case8c.main(params);
        case8c = null;

        SingleCase case8d = new SingleCase();
        params[0] = "_8baseCaseHftCFEE51bb\\";
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.4";            // % of |0 PV| traders
        params[3]= "0.3428571";             // % of |2 PV| traders
        params[4] = "0.2571429";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.01";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        case8d.main(params);
        case8d = null;

        SingleCase case8e = new SingleCase();
        params[0] = "_8baseCaseHftCFEE54aa\\";
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.4";            // % of |0 PV| traders
        params[3]= "0.3428571";             // % of |2 PV| traders
        params[4] = "0.2571429";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.02";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders
        case8e.main(params);
        case8e = null;

        SingleCase case8f = new SingleCase();
        params[0] = "_8baseCaseHftCFEE54bb\\";
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.4";            // % of |0 PV| traders
        params[3]= "0.3428571";             // % of |2 PV| traders
        params[4] = "0.2571429";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.02";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        case8f.main(params);
        case8f = null;*/

        /*SingleCase case9 = new SingleCase();
        params[0] = "_9baseCaseHftTTAX2b\\";
        params[1] = "1";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.001945";      // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.02";          // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.03";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders


        case9.main(params);
        case9 = null;*/

        SingleCase case10a = new SingleCase();
        params[0] = "_10baseCaseMT24aaa"  + File.separator;
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.4";            // % of |0 PV| traders
        params[3]= "0.3428571";             // % of |2 PV| traders
        params[4] = "0.2571429";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.006";          // LO make fee
        params[9] = "0.009";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders
        params[20] = "1000";        // 1000-> 15bn, 100-> 1.5bn, 10-> 150m, 1->15m events
        case10a.main(params);
        case10a = null;

        /*SingleCase case10b = new SingleCase();
        params[0] = "_10baseCaseMT24bb\\";
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.4";            // % of |0 PV| traders
        params[3]= "0.3428571";             // % of |2 PV| traders
        params[4] = "0.2571429";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.006";          // LO make fee
        params[9] = "0.009";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        params[20] = "1";        // 1000-> 15bn, 100-> 1.5bn, 10-> 150m, 1->15m events
        case10b.main(params);
        case10b = null;*/

        /*SingleCase case10c = new SingleCase();
        params[0] = "_10baseCaseMT21aa\\";
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.4";            // % of |0 PV| traders
        params[3]= "0.3428571";             // % of |2 PV| traders
        params[4] = "0.2571429";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.008";          // LO make fee
        params[9] = "0.012";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders
        case10c.main(params);
        case10c = null;

        SingleCase case10d = new SingleCase();
        params[0] = "_10baseCaseMT21bb\\";
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.4";            // % of |0 PV| traders
        params[3]= "0.3428571";             // % of |2 PV| traders
        params[4] = "0.2571429";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.008";          // LO make fee
        params[9] = "0.012";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        case10d.main(params);
        case10d = null;

        SingleCase case10e = new SingleCase();
        params[0] = "_10baseCaseMT22aa\\";
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.4";            // % of |0 PV| traders
        params[3]= "0.3428571";             // % of |2 PV| traders
        params[4] = "0.2571429";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.012";          // LO make fee
        params[9] = "0.018";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders
        case10e.main(params);
        case10e = null;

        SingleCase case10f = new SingleCase();
        params[0] = "_10baseCaseMT22bb\\";
        params[1] = "0.5";            // % of HFTs from 0 PV traders
        params[2] = "0.4";            // % of |0 PV| traders
        params[3]= "0.3428571";             // % of |2 PV| traders
        params[4] = "0.2571429";            // % of |4 PV| traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.012";          // LO make fee
        params[9] = "0.018";          // MO take fee
        params[10] = "0.25";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "returning";   // model to use
        params[15] = "0.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders
        case10f.main(params);
        case10f = null;*/

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
        params[0] = "_11speedBump23a\\";   // folder
        params[1] = "1.0";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.02";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.03";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "speedBump";   // model to use
        params[15] = "0.2";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders

        case12.main(params);
        case12 = null;*/

        /*SingleCase case12a = new SingleCase();
        params[0] = "_11speedBump26a\\";   // folder
        params[1] = "1.0";            // new HFTs
        params[2] = "1";            // # of positive PV slow traders
        params[3]= "2";             // # of zero PV slow traders
        params[4] = "1";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.02";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.03";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "speedBump";   // model to use
        params[15] = "0.1";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders

        case12a.main(params);
        case12a = null;*/


        /*SingleCase case11a = new SingleCase();
        params[0] = "_11speedBump28a\\";   // folder
        params[1] = "1.0";            // new HFTs
        params[2] = "1.0";            // # of positive PV slow traders
        params[3]= "2.0";             // # of zero PV slow traders
        params[4] = "1.0";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.04";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "speedBump";   // model to use
        params[15] = "0.3";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders


        case11a.main(params);
        case11a = null;

        SingleCase case11b = new SingleCase();
        params[0] = "_11speedBump28b\\";   // folder
        params[1] = "1.0";            // new HFTs
        params[2] = "1.0";            // # of positive PV slow traders
        params[3]= "2.0";             // # of zero PV slow traders
        params[4] = "1.0";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.04";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "speedBump";   // model to use
        params[15] = "0.3";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders


        case11b.main(params);
        case11b = null;

        SingleCase case11c = new SingleCase();
        params[0] = "_11speedBump30a\\";   // folder
        params[1] = "1.0";            // new HFTs
        params[2] = "1.0";            // # of positive PV slow traders
        params[3]= "2.0";             // # of zero PV slow traders
        params[4] = "1.0";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.04";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "speedBump";   // model to use
        params[15] = "2.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "0.0";         // information delay of uninformed traders


        case11c.main(params);
        case11c = null;

        SingleCase case11d = new SingleCase();
        params[0] = "_11speedBump30b\\";   // folder
        params[1] = "1.0";            // new HFTs
        params[2] = "1.0";            // # of positive PV slow traders
        params[3]= "2.0";             // # of zero PV slow traders
        params[4] = "1.0";            // # of negative PV slow traders
        params[5] = "0.0";          // time in force
        params[6] = "0.0";          // TTAX
        params[7] = "0.0";          // CFEE
        params[8] = "0.0";          // LO make fee
        params[9] = "0.0";          // MO take fee
        params[10] = "0.04";        // lambda of new arrivals
        params[11] = "0.03f";       // rho- impatience parameters
        params[12] = "0.1";        // arrival of changes in FV
        params[13] = "30";          // max depth
        params[14] = "speedBump";   // model to use
        params[15] = "2.0";         // speed bump length
        params[16] = "15";          // number of ticks
        params[17] = "8";           // infoSize
        params[18] = "2";           // private value volatility {4-> {-8,-4,0,4,8}, 2-> {-4,-2,0,2,4}}
        params[19] = "2.0";         // information delay of uninformed traders


        case11d.main(params);
        case11d = null;*/

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
