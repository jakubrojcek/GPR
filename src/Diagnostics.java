import java.util.HashMap;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: rojcek
 * Date: 30.11.12
 * Time: 17:55
 * To change this template use File | Settings | File Templates.
 */
public class Diagnostics {

    private byte nP;
    private int e;
    double diff;
    int count;
    HashMap<Short, Integer> actions;

    public Diagnostics(byte numberPrices,int e){
        this.nP = numberPrices;
        this.e = e;
        diff = 0.0;
        count = 0;
        actions = new HashMap<Short, Integer>(2 * e + 3);
    }

    public void addDiff(double d){
        diff = diff + d;
        count++;
    }

    public void addAction(short ac){
        if (!actions.containsKey(ac)){
            actions.put(ac, 1);
        } else {
            int n = actions.get(ac);
            actions.put(ac, n + 1);
        }
    }

    // printing diagnostics here
   public String printDiagnostics(String version){
        String s = new String();
        if (version == "diffs"){
            s = count + ";";
            s = s + diff + "\r";
        } else if (version == "actions"){
            int sz = actions.size();
            for (short i = 0; i < sz; i++){
                s = s + actions.get(i) + ";";
            }
            s = s + "\r";
        }
        return s;
    }
}
