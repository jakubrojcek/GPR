package com.jakubrojcek;

import java.util.IdentityHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Jakub
 * Date: 12.11.12
 * Time: 17:38
 * To change this template use File | Settings | File Templates.
 */
public class Decision {
    private byte nP;
    private int LL;
    private int FVpos;
    private int e;
    private int breakPoint;
    private int[] counts;           // holds counts of decisions. [0] overall count
    private int[] countsLiquidity;  // holds counts of LOs vs MOs vs NOs
        /*
        Table V, DA = depth at ask
        DA 1-2, spread 1,   [41] count, [1] SBMOs, [86] LBO, [2] AggBLO [94] AtBLO [95] BelowBLO
        DA 1-2, spread > 1, [42] count, [3] SBMOs, [87] LBO, [4] AggBLO [96] AtBLO [97] BelowBLO
        DA > 2, spread 1,   [43] count, [5] SBMOs, [88] LBO, [6] AggBLO [98] AtBLO [99] BelowBLO
        DA > 2, spread > 1, [44] count, [7] SBMOs, [89] LBO, [8] AggBLO [100] AtBLO [101] BelowBLO

        Table I: conditional frequencies of buy orders at t+1 given order at t
        BMO ->      [66] LBM [9]  BMO [10] AggBLO [11] AtBLO [12] BelowBLO [45] count
        LBM ->      [67] LBM [68] BMO [69] AggBLO [70] AtBLO [71] BelowBLO [72] count
        SMO ->      [73] LBM [13] BMO [14] AggBLO [15] AtBLO [16] BelowBLO [46] count
        LSO->       [74] LBM [75] BMO [76] AggBLO [77] AtBLO [78] BelowBLO [79] count
        AggBLO ->   [80] LBM [17] BMO [18] AggBLO [19] AtBLO [20] BelowBLO [47] count
        AggSLO ->   [81] LBM [21] BMO [22] AggBLO [23] AtBLO [24] BelowBLO [48] count
        AtBLO ->    [82] LBM [25] BMO [26] AggBLO [27] AtBLO [28] BelowBLO [49] count
        AtSLO ->    [83] LBM [29] BMO [30] AggBLO [31] AtBLO [32] BelowBLO [50] count
        BelowBLO -> [84] LBM [33] BMO [34] AggBLO [35] AtBLO [36] BelowBLO [51] count
        AboveSLO -> [85] LBM [37] BMO [38] AggBLO [39] AtBLO [40] BelowBLO [52] count

        Table III, DB = depth at bid
        DB 1-2, spread 1,   [53] count, [54] BMOs, [90] LBO, [55] AggBLO [102] AtBLO [103] BelowBLO
        DB 1-2, spread > 1, [56] count, [57] BMOs, [91] LBO, [58] AggBLO [104] AtBLO [105] BelowBLO
        DB > 2, spread 1,   [59] count, [60] BMOs, [92] LBO, [61] AggBLO [106] AtBLO [107] BelowBLO
        DB > 2, spread > 1, [62] count, [63] BMOs, [93] LBO, [64] AggBLO [108] AtBLO [109] BelowBLO

        Table IV, DB = depth below bid
        DB 0, spread 1,   [110] count, [111] BMOs, [112] LBO, [113] AggBLO [114] AtBLO [115] BelowBLO
        DB > 0, spread 1, [116] count, [117] BMOs, [118] LBO, [119] AggBLO [120] AtBLO [121] BelowBLO
        DB 0, spread > 1,   [122] count, [123] BMOs, [124] LBO, [125] AggBLO [126] AtBLO [127] BelowBLO
        DB > 0, spread > 1, [128] count, [129] BMOs, [130] LBO, [131] AggBLO [132] AtBLO [133] BelowBLO

        com.jakubrojcek.Diagnostics: too many sellers
        [65] count of sell orders
        */

    // constructor for com.jakubrojcek.Decision
    public Decision(byte numberPrices, int FVpos, int e, int bp, int ll){
        this.nP = numberPrices;
        this.FVpos = FVpos;
        this.e = e;
        this.breakPoint = bp;
        this.LL = ll;
        counts = new int[134];
        countsLiquidity = new int[8];   //  MO nonHFT, LO nonHFT, NO nonHFT; MO HFT, LO HFT, NO HFT
    }
    // TODO: make decisions also separately for HFT
    // adds information to the decision
    public int addDecision(int[] bi, Short [] ac , int[] prevTrAc, boolean isHFT){
        byte t = 0;                                         // designates which action matters
        if (ac[1] == 2 * e + 3){
            t = 1;}                     // large sell order
        else if (ac[1] == 2 * e + 4){
            t = 1;}                // large buy order
        else if (ac[1] != 127 && ac[0] < (2 * e)){          // two units to trade && both are LO
            double rn = Math.random();                      // else take first action
            if (rn < 0.5){
                t = 1;
            }
        }
        if ((ac[0] == (short) (2 * e + 1)) && (ac[1] == (short) (2 * e))){
            double rn = Math.random();                      // else take first action
            if (rn < 0.5){
                t = 1;
            }
        }
        if ((ac[0] == (2 * e + 2)) && (ac[1] != 127)){t = 1;}   // no order, take second action

        if (isHFT){
            return ac[t];
        }
        counts[0]++;
        // table III & V
        int spread = bi[1] - bi[0];
        if (bi[2] > 0 && bi[3] > 0){
            if(spread == 1){                                // spread = 1
                if (bi[3] < 3){                             // depth at ask is 1-2
                    counts[41]++;
                    if (ac[t] == (2 * e + 1)){        // Buy market order
                        counts[1]++;
                    } else if (ac[t] == (2 * e + 4)){
                        counts[86]++;
                    } else if (ac[t] >= e && ac[t] < 2 * e){      // Buy Limit order
                        if ((ac[t] - e) < (bi[0] - LL)){                   // BelowBLO
                            counts[95]++;
                        } else if ((ac[t] - e) == (bi[0] - LL)){             // AtBLO
                            counts[94]++;
                        } else if ((ac[t] - e) > (bi[0] - LL)){            // AggBLO
                            counts[2]++;
                        }
                    }
                } else{                                     // depth at ask > 1-2
                    counts[43]++;
                    if (ac[t] == (2 * e + 1)){                 // Buy market order
                        counts[5]++;
                    } else if (ac[t] == (2 * e + 4)){
                        counts[88]++;
                    } else if (ac[t] >= e && ac[t] < 2 * e){      // Buy Limit order
                        if ((ac[t] - e) < (bi[0] - LL)){                   // BelowBLO
                            counts[99]++;
                        } else if ((ac[t] - e) == (bi[0] - LL)){             // AtBLO
                            counts[98]++;
                        } else if ((ac[t] - e) > (bi[0] - LL)){            // AggBLO
                            counts[6]++;
                        }
                    }
                }
                if (bi[2] < 3){                             // depth at bid is 1-2
                    counts[53]++;
                    if (ac[t] == (2 * e + 1)){                 // Buy market order
                        counts[54]++;
                    } else if (ac[t] == (2 * e + 4)){
                        counts[90]++;
                    } else if (ac[t] >= e && ac[t] < 2 * e){      // Buy Limit order
                        if ((ac[t] - e) < (bi[0] - LL)){                   // BelowBLO
                            counts[103]++;
                        } else if ((ac[t] - e) == (bi[0] - LL)){             // AtBLO
                            counts[102]++;
                        } else if ((ac[t] - e) > (bi[0] - LL)){            // AggBLO
                            counts[55]++;
                        }
                    }
                } else{                                     // depth at bid > 1-2
                    counts[59]++;
                    if (ac[t] == (2 * e + 1)){                 // Buy market order
                        counts[60]++;
                    } else if (ac[t] == (2 * e + 4)){
                        counts[92]++;
                    } else if (ac[t] >= e && ac[t] < 2 * e){      // Buy Limit order
                        if ((ac[t] - e) < (bi[0] - LL)){                   // BelowBLO
                            counts[107]++;
                        } else if ((ac[t] - e) == (bi[0] - LL)){             // AtBLO
                            counts[106]++;
                        } else if ((ac[t] - e) > (bi[0] - LL)){            // AggBLO
                            counts[61]++;
                        }
                    }
                }
            } else {                                        // spread > 1
                if (bi[3] < 3){                             // depth at ask is 1-2
                    counts[42]++;
                    if (ac[t] == (2 * e + 1)){                 // Buy market order
                        counts[3]++;
                    } else if (ac[t] == (2 * e + 4)){
                        counts[87]++;
                    } else if (ac[t] >= e && ac[t] < 2 * e){      // Buy Limit order
                        if ((ac[t] - e) < (bi[0] - LL)){                   // BelowBLO
                            counts[97]++;
                        } else if ((ac[t] - e) == (bi[0] - LL)){             // AtBLO
                            counts[96]++;
                        } else if ((ac[t] - e) > (bi[0] - LL)){            // AggBLO
                            counts[4]++;
                        }
                    }
                } else{                                     // depth at ask > 1-2
                    counts[44]++;
                    if (ac[t] == (2 * e + 1)){                 // Buy market order
                        counts[7]++;
                    } else if (ac[t] == (2 * e + 4)){
                        counts[89]++;
                    } else if (ac[t] >= e && ac[t] < 2 * e){      // Buy Limit order
                        if ((ac[t] - e) < (bi[0] - LL)){                   // BelowBLO
                            counts[101]++;
                        } else if ((ac[t] - e) == (bi[0] - LL)){             // AtBLO
                            counts[100]++;
                        } else if ((ac[t] - e) > (bi[0] - LL)){            // AggBLO
                            counts[8]++;
                        }
                    }
                }
                if (bi[2] < 3){                             // depth at bid is 1-2
                    counts[56]++;
                    if (ac[t] == (2 * e + 1)){                 // Buy market order
                        counts[57]++;
                    } else if (ac[t] == (2 * e + 4)){
                        counts[91]++;
                    } else if (ac[t] >= e && ac[t] < 2 * e){      // Buy Limit order
                        if ((ac[t] - e) < (bi[0] - LL)){                   // BelowBLO
                            counts[105]++;
                        } else if ((ac[t] - e) == (bi[0] - LL)){             // AtBLO
                            counts[104]++;
                        } else if ((ac[t] - e) > (bi[0] - LL)){            // AggBLO
                            counts[58]++;
                        }
                    }
                } else{                                     // depth at ask > 1-2
                    counts[62]++;
                    if (ac[t] == (2 * e + 1)){                 // Buy market order
                        counts[63]++;
                    } else if (ac[t] == (2 * e + 4)){
                        counts[93]++;
                    } else if (ac[t] >= e && ac[t] < 2 * e){      // Buy Limit order
                        if ((ac[t] - e) < (bi[0] - LL)){                   // BelowBLO
                            counts[109]++;
                        } else if ((ac[t] - e) == (bi[0] - LL)){             // AtBLO
                            counts[108]++;
                        } else if ((ac[t] - e) > (bi[0] - LL)){            // AggBLO
                            counts[64]++;
                        }
                    }
                }
            }
        }
        // table IV
        if (bi[2] > 0 && bi[3] > 0){
            if(spread == 1){                                // spread = 1
                if ((bi[4] - bi[2]) == 0){                  // depth below bid is 0
                    counts[110]++;
                    if (ac[t] == (2 * e + 1)){                 // Buy market order
                        counts[111]++;
                    } else if (ac[t] == (2 * e + 4)){
                        counts[112]++;
                    } else if (ac[t] >= e && ac[t] < 2 * e){      // Buy Limit order
                        if ((ac[t] - e) < (bi[0] - LL)){                   // BelowBLO
                            counts[115]++;
                        } else if ((ac[t] - e) == (bi[0] - LL)){             // AtBLO
                            counts[114]++;
                        } else if ((ac[t] - e) > (bi[0] - LL)){            // AggBLO
                            counts[113]++;
                        }
                    }
                } else{                                     // depth at bid > 1-2
                    counts[116]++;
                    if (ac[t] == (2 * e + 1)){                 // Buy market order
                        counts[117]++;
                    } else if (ac[t] == (2 * e + 4)){
                        counts[118]++;
                    } else if (ac[t] >= e && ac[t] < 2 * e){      // Buy Limit order
                        if ((ac[t] - e) < (bi[0] - LL)){                   // BelowBLO
                            counts[121]++;
                        } else if ((ac[t] - e) == (bi[0] - LL)){             // AtBLO
                            counts[120]++;
                        } else if ((ac[t] - e) > (bi[0] - LL)){            // AggBLO
                            counts[119]++;
                        }
                    }
                }
            } else {                                        // spread > 1
                if ((bi[4] - bi[2]) == 0){                  // depth below bid is 0
                    counts[122]++;
                    if (ac[t] == (2 * e + 1)){                 // Buy market order
                        counts[123]++;
                    } else if (ac[t] == (2 * e + 4)){
                        counts[124]++;
                    } else if (ac[t] >= e && ac[t] < 2 * e){      // Buy Limit order
                        if ((ac[t] - e) < (bi[0] - LL)){                   // BelowBLO
                            counts[127]++;
                        } else if ((ac[t] - e) == (bi[0] - LL)){             // AtBLO
                            counts[126]++;
                        } else if ((ac[t] - e) > (bi[0] - LL)){            // AggBLO
                            counts[125]++;
                        }
                    }
                } else{                                     // depth at ask > 1-2
                    counts[128]++;
                    if (ac[t] == (2 * e + 1)){                 // Buy market order
                        counts[129]++;
                    } else if (ac[t] == (2 * e + 4)){
                        counts[130]++;
                    } else if (ac[t] >= e && ac[t] < 2 * e){      // Buy Limit order
                        if ((ac[t] - e) < (bi[0] - LL)){                   // BelowBLO
                            counts[133]++;
                        } else if ((ac[t] - e) == (bi[0] - LL)){             // AtBLO
                            counts[132]++;
                        } else if ((ac[t] - e) > (bi[0] - LL)){            // AggBLO
                            counts[131]++;
                        }
                    }
                }
            }
        }
        // table I
        if (prevTrAc[0] == (2 * e + 1)){                              // BMO past event
            counts[45]++;
            if (ac[t] == 2 * e + 1){                                   // Small BMO
                counts[9]++;
            } else if (ac[t] == 2 * e + 4){                            // Large BMO
                counts[66]++;
            }
            if (ac[t] >= e && ac[t] < 2 * e){
                if ((ac[t] - e) < (bi[0] - LL)){                       // BelowBLO
                    counts[12]++;
                } else if ((ac[t] - e) == (bi[0] - LL)){                 // AtBLO
                    counts[11]++;
                } else if ((ac[t] - e) > (bi[0] - LL)){                // AggBLO
                    counts[10]++;
                }
            }
        } else if (prevTrAc[0] == (2 * e + 4)){                              // Large BMO past event
            counts[72]++;
            if (ac[t] == 2 * e + 1){                                   // Small BMO
                counts[68]++;
            } else if (ac[t] == 2 * e + 4){                            // Large BMO
                counts[67]++;
            }
            if (ac[t] >= e && ac[t] < 2 * e){
                if ((ac[t] - e) < (bi[0] - LL)){                       // BelowBLO
                    counts[71]++;
                } else if ((ac[t] - e) == (bi[0] - LL)){                 // AtBLO
                    counts[70]++;
                } else if ((ac[t] - e) > (bi[0] - LL)){                // AggBLO
                    counts[69]++;
                }
            }
        } else if (prevTrAc[0] == (2 * e)){                           // SMO past event
            counts[46]++;
            if (ac[t] == 2 * e + 1){                                   // BMO
                counts[13]++;
            } else if (ac[t] == 2 * e + 4){                            // Large BMO
                counts[73]++;
            }

            if (ac[t] >= e && ac[t] < 2 * e){
                if ((ac[t] - e) < (bi[0] - LL)){                       // BelowBLO
                    counts[16]++;
                } else if ((ac[t] - e) == (bi[0] - LL)){                 // AtBLO
                    counts[15]++;
                } else if ((ac[t] - e) > (bi[0] - LL)){                // AggBLO
                    counts[14]++;
                }
            }
        } else if (prevTrAc[0] == (2 * e + 3)){                          // Large SMO past event
            counts[79]++;
            if (ac[t] == 2 * e + 1){                                   // Small BMO
                counts[75]++;
            } else if (ac[t] == 2 * e + 4){                            // Large BMO
                counts[74]++;
            }
            if (ac[t] >= e && ac[t] < 2 * e){
                if ((ac[t] - e) < (bi[0] - LL)){                       // BelowBLO
                    counts[78]++;
                } else if ((ac[t] - e) == (bi[0] - LL)){                 // AtBLO
                    counts[77]++;
                } else if ((ac[t] - e) > (bi[0] - LL)){                // AggBLO
                    counts[76]++;
                }
            }
        } else if (prevTrAc[0] >= e && prevTrAc[0] < (2 * e)){      // BLO previously submitted
            if ((prevTrAc[0] - e) < prevTrAc[1] - LL){              // BelowBLO previously submitted
                counts[51]++;
                if (ac[t] == 2 * e + 1){                               // BMO
                    counts[33]++;
                } else if (ac[t] == 2 * e + 4){                            // Large BMO
                    counts[84]++;
                }
                if (ac[t] >= e && ac[t] < 2 * e){
                    if ((ac[t] - e) < (bi[0] - LL)){                   // BelowBLO
                        counts[36]++;
                    } else if ((ac[t] - e) == (bi[0] - LL)){             // AtBLO
                        counts[35]++;
                    } else if ((ac[t] - e) > (bi[0] - LL)){            // AggBLO
                        counts[34]++;
                    }
                }

            } else if ((prevTrAc[0] - e) == prevTrAc[1] - LL) {     // AtBLO previously submitted
                counts[49]++;
                if (ac[t] == 2 * e + 1){                               // BMO
                    counts[25]++;
                } else if (ac[t] == 2 * e + 4){                            // Large BMO
                    counts[82]++;
                }
                if (ac[t] >= e && ac[t] < 2 * e){
                    if ((ac[t] - e) < (bi[0] - LL)){                   // BelowBLO
                        counts[28]++;
                    } else if ((ac[t] - e) == (bi[0] - LL)){             // AtBLO
                        counts[27]++;
                    } else if ((ac[t] - e) > (bi[0] - LL)){            // AggBLO
                        counts[26]++;
                    }
                }
            } else {                                                // AggBLO previously submitted
                counts[47]++;
                if (ac[t] == 2 * e + 1){                               // BMO
                    counts[17]++;
                } else if (ac[t] == 2 * e + 4){                            // Large BMO
                    counts[80]++;
                }
                if (ac[t] >= e && ac[t] < 2 * e){
                    if ((ac[t] - e) < (bi[0] - LL)){                   // BelowBLO
                        counts[20]++;
                    } else if ((ac[t] - e) == (bi[0] - LL)){             // AtBLO
                        counts[19]++;
                    } else if ((ac[t] - e) > (bi[0] - LL)){            // AggBLO
                        counts[18]++;
                    }
                }
            }
        } else if (prevTrAc[0] < e){                                // SLO previously submitted
            if (prevTrAc[0] > (prevTrAc[2] - LL)){                  // AboveSLO previously submitted
                counts[52]++;
                if (ac[t] == 2 * e + 1){                               // BMO
                    counts[37]++;
                } else if (ac[t] == 2 * e + 4){                            // Large BMO
                    counts[85]++;
                }
                if (ac[t] >= e && ac[t] < 2 * e){
                    if ((ac[t] - e) < (bi[0] - LL)){                   // BelowBLO
                        counts[40]++;
                    } else if ((ac[t] - e) == (bi[0] - LL)){             // AtBLO
                        counts[39]++;
                    } else if ((ac[t] - e) > (bi[0] - LL)){            // AggBLO
                        counts[38]++;
                    }
                }
            } else if (prevTrAc[0] == (prevTrAc[2] - LL)){          // AtSLO previously submitted
                counts[50]++;
                if (ac[t] == 2 * e + 1){                               // BMO
                    counts[29]++;
                } else if (ac[t] == 2 * e + 4){                            // Large BMO
                    counts[83]++;
                }
                if (ac[t] >= e && ac[t] < 2 * e){
                    if ((ac[t] - e) < (bi[0] - LL)){                   // BelowBLO
                        counts[32]++;
                    } else if ((ac[t] - e) == (bi[0] - LL)){             // AtBLO
                        counts[31]++;
                    } else if ((ac[t] - e) > (bi[0] - LL)){            // AggBLO
                        counts[30]++;
                    }
                }
            } else {                                                // AggSLO previously submitted
                counts[48]++;
                if (ac[t] == 2 * e + 1){                               // BMO
                    counts[21]++;
                } else if (ac[t] == 2 * e + 4){                            // Large BMO
                    counts[81]++;
                }
                if (ac[t] >= e && ac[t] < 2 * e){
                    if ((ac[t] - e) < (bi[0] - LL)){                   // BelowBLO
                        counts[24]++;
                    } else if ((ac[t] - e) == (bi[0] - LL)){             // AtBLO
                        counts[23]++;
                    } else if ((ac[t] - e) > (bi[0] - LL)){            // AggBLO
                        counts[22]++;
                    }
                }
            }
        }
        if (ac[t] == 2 * e){                                           // SMO counts
            counts[65]++;
        }
        return ac[t];
    }

    public void addDecisionLiquidity(int ac, boolean hft, boolean cancelled){
        if (hft){
            if (ac < (2 * e)){
                countsLiquidity[4]++;
            } else if (ac == (2 * e + 2)){
                countsLiquidity[5]++;
            } else {
                countsLiquidity[3]++;
            }
            if (cancelled){
                countsLiquidity[7]++;
            }
        } else {
            if (ac < (2 * e)){
                countsLiquidity[1]++;
            } else if (ac == (2 * e + 2)){
                countsLiquidity[2]++;
            } else {
                countsLiquidity[0]++;
            }
            if (cancelled){
                countsLiquidity[6]++;
            }
        }
    }

    // printing decisions here
    public String printDecision(){
        int sz = counts.length;
        String s = new String();
        for (int i = 0; i < sz; i++){
            s = s + counts[i] + ";";
        }
        s = s + "\r";
        return s;
    }

    public String printDecisionLiquidity(){
        int sz = countsLiquidity.length;
        String s = new String();
        for (int i = 0; i < sz; i++){
            s = s + countsLiquidity[i] + ";";
        }
        s = s + "\r";
        return s;
    }
}
