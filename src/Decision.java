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
        /*
        Table V
        depth at ask 1-2, spread 1, [41] count, [1] count of BMOs, [2] count of BLOs
        depth at ask 1-2, spread > 1, [42] count, [3] count of BMOs, [4] count of BLOs
        depth at ask > 2, spread 1, [43] count, [5] count of BMOs, [6] count of BLOs
        depth at ask > 2, spread > 1, [44] count, [7] count of BMOs, [8] count of BLOs

        Table I: conditional frequencies of buy orders at t+1 given order at t
        BMO ->      [9] BMO  [10] AggBLO [11] AtBLO [12] BelowBLO [45] count
        SMO ->      [13] BMO [14] AggBLO [15] AtBLO [16] BelowBLO [46] count
        AggBLO ->   [17] BMO [18] AggBLO [19] AtBLO [20] BelowBLO [47] count
        AggSLO ->   [21] BMO [22] AggBLO [23] AtBLO [24] BelowBLO [48] count
        AtBLO ->    [25] BMO [26] AggBLO [27] AtBLO [28] BelowBLO [49] count
        AtSLO ->    [29] BMO [30] AggBLO [31] AtBLO [32] BelowBLO [50] count
        BelowBLO -> [33] BMO [34] AggBLO [35] AtBLO [36] BelowBLO [51] count
        AboveSLO -> [37] BMO [38] AggBLO [39] AtBLO [40] BelowBLO [52] count

        Table III
        depth at bid 1-2, spread 1, [53] count, [54] count of BMOs, [55] count of BLOs
        depth at bid 1-2, spread > 1, [56] count, [57] count of BMOs, [58] count of BLOs
        depth at bid > 2, spread 1, [59] count, [60] count of BMOs, [61] count of BLOs
        depth at bid > 2, spread > 1, [62] count, [63] count of BMOs, [64] count of BLOs

        Diagnostics: too many sellers
        [65] count of sell orders
        */

    // constructor for Decision
    public Decision(byte numberPrices, int FVpos, int e, int bp, int ll){
        this.nP = numberPrices;
        this.FVpos = FVpos;
        this.e = e;
        this.breakPoint = bp;
        this.LL = ll;
        counts = new int[66];
    }

    // adds information to the decision
    public void addDecision(int[] bi, int ac , int[] prevTrAc){              //TODO: repair this piece
        counts[0]++;
        // table III & V
        int spread = bi[1] - bi[0];
        if (bi[2] > 0 && bi[3] > 0){
            if(spread == 1){                                // spread = 1
                if (bi[3] < 3){                             // depth at ask is 1-2
                    counts[41]++;
                    if (ac == (2 * e + 1)){                 // Buy market order
                        counts[1]++;
                    } else if (ac >= e && ac < 2 * e){      // Buy Limit order
                        counts[2]++;
                    }
                } else{                                     // depth at ask > 1-2
                    counts[43]++;
                    if (ac == (2 * e + 1)){                 // Buy market order
                        counts[5]++;
                    } else if (ac >= e && ac < 2 * e){      // Buy Limit order
                        counts[6]++;
                    }
                }
                if (bi[2] < 3){                             // depth at bid is 1-2
                    counts[53]++;
                    if (ac == (2 * e + 1)){                 // Buy market order
                        counts[54]++;
                    } else if (ac >= e && ac < 2 * e){      // Buy Limit order
                        counts[55]++;
                    }
                } else{                                     // depth at ask > 1-2
                    counts[59]++;
                    if (ac == (2 * e + 1)){                 // Buy market order
                        counts[60]++;
                    } else if (ac >= e && ac < 2 * e){      // Buy Limit order
                        counts[61]++;
                    }
                }
            } else {                                        // spread > 1
                if (bi[3] < 3){                             // depth at ask is 1-2
                    counts[42]++;
                    if (ac == (2 * e + 1)){                 // Buy market order
                        counts[3]++;
                    } else if (ac >= e && ac < 2 * e){      // Buy Limit order
                        counts[4]++;
                    }
                } else{                                     // depth at ask > 1-2
                    counts[44]++;
                    if (ac == (2 * e + 1)){                 // Buy market order
                        counts[7]++;
                    } else if (ac >= e && ac < 2 * e){      // Buy Limit order
                        counts[8]++;
                    }
                }
                if (bi[2] < 3){                             // depth at bid is 1-2
                    counts[56]++;
                    if (ac == (2 * e + 1)){                 // Buy market order
                        counts[57]++;
                    } else if (ac >= e && ac < 2 * e){      // Buy Limit order
                        counts[58]++;
                    }
                } else{                                     // depth at ask > 1-2
                    counts[62]++;
                    if (ac == (2 * e + 1)){                 // Buy market order
                        counts[63]++;
                    } else if (ac >= e && ac < 2 * e){      // Buy Limit order
                        counts[64]++;
                    }
                }
            }
        }

        // table I
        if (prevTrAc[0] == 2 * e + 1){                              // BMO past event
            counts[45]++;
            if (ac == 2 * e + 1){                                   // BMO
                counts[9]++;
            }
            if (ac >= e && ac < 2 * e){
                if ((ac - e) < (bi[0] - LL)){                       // BelowBLO
                    counts[12]++;
                } else if ((ac - e) == bi[0] - LL){                 // AtBLO
                    counts[11]++;
                } else if ((ac - e) > (bi[0] - LL)){                // AggBLO
                    counts[10]++;
                }
            }
        } else if (prevTrAc[0] == 2 * e){                           // SMO past event
            counts[46]++;
            if (ac == 2 * e + 1){                                   // BMO
                counts[13]++;
            }
            if (ac >= e && ac < 2 * e){
                if ((ac - e) < (bi[0] - LL)){                       // BelowBLO
                    counts[16]++;
                } else if ((ac - e) == bi[0] - LL){                 // AtBLO
                    counts[15]++;
                } else if ((ac - e) > (bi[0] - LL)){                // AggBLO
                    counts[14]++;
                }
            }
        } else if (prevTrAc[0] >= e && prevTrAc[0] < (2 * e)){      // BLO previously submitted
            if ((prevTrAc[0] - e) < prevTrAc[1] - LL){              // BelowBLO previously submitted
                counts[51]++;
                if (ac == 2 * e + 1){                               // BMO
                    counts[33]++;
                }
                if (ac >= e && ac < 2 * e){
                    if ((ac - e) < (bi[0] - LL)){                   // BelowBLO
                        counts[36]++;
                    } else if ((ac - e) == bi[0] - LL){             // AtBLO
                        counts[35]++;
                    } else if ((ac - e) > (bi[0] - LL)){            // AggBLO
                        counts[34]++;
                    }
                }

            } else if ((prevTrAc[0] - e) == prevTrAc[1] - LL) {     // AtBLO previously submitted
                counts[49]++;
                if (ac == 2 * e + 1){                               // BMO
                    counts[25]++;
                }
                if (ac >= e && ac < 2 * e){
                    if ((ac - e) < (bi[0] - LL)){                   // BelowBLO
                        counts[28]++;
                    } else if ((ac - e) == bi[0] - LL){             // AtBLO
                        counts[27]++;
                    } else if ((ac - e) > (bi[0] - LL)){            // AggBLO
                        counts[26]++;
                    }
                }
            } else {                                                // AggBLO previously submitted
                counts[47]++;
                if (ac == 2 * e + 1){                               // BMO
                    counts[17]++;
                }
                if (ac >= e && ac < 2 * e){
                    if ((ac - e) < (bi[0] - LL)){                   // BelowBLO
                        counts[20]++;
                    } else if ((ac - e) == bi[0] - LL){             // AtBLO
                        counts[19]++;
                    } else if ((ac - e) > (bi[0] - LL)){            // AggBLO
                        counts[18]++;
                    }
                }
            }
        } else if (prevTrAc[0] < e){                                // SLO previously submitted
            if (prevTrAc[0] > (prevTrAc[2] - LL)){                  // AboveSLO previously submitted
                counts[52]++;
                if (ac == 2 * e + 1){                               // BMO
                    counts[37]++;
                }
                if (ac >= e && ac < 2 * e){
                    if ((ac - e) < (bi[0] - LL)){                   // BelowBLO
                        counts[40]++;
                    } else if ((ac - e) == bi[0] - LL){             // AtBLO
                        counts[39]++;
                    } else if ((ac - e) > (bi[0] - LL)){            // AggBLO
                        counts[38]++;
                    }
                }
            } else if (prevTrAc[0] == (prevTrAc[2] - LL)){          // AtSLO previously submitted
                counts[50]++;
                if (ac == 2 * e + 1){                               // BMO
                    counts[29]++;
                }
                if (ac >= e && ac < 2 * e){
                    if ((ac - e) < (bi[0] - LL)){                   // BelowBLO
                        counts[32]++;
                    } else if ((ac - e) == bi[0] - LL){             // AtBLO
                        counts[31]++;
                    } else if ((ac - e) > (bi[0] - LL)){            // AggBLO
                        counts[30]++;
                    }
                }
            } else {                                                // AggSLO previously submitted
                counts[48]++;
                if (ac == 2 * e + 1){                               // BMO
                    counts[21]++;
                }
                if (ac >= e && ac < 2 * e){
                    if ((ac - e) < (bi[0] - LL)){                   // BelowBLO
                        counts[24]++;
                    } else if ((ac - e) == bi[0] - LL){             // AtBLO
                        counts[23]++;
                    } else if ((ac - e) > (bi[0] - LL)){            // AggBLO
                        counts[22]++;
                    }
                }
            }
        }
        if (ac == 2 * e){                                           // SMO counts
            counts[65]++;
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
}
