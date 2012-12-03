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

    public Diagnostics(byte numberPrices,int e){
        this.nP = numberPrices;
        this.e = e;
        diff = 0.0;
        count = 0;
    }

    public void addDiff(double d){
        diff = diff + d;
        count++;
    }




    // printing diagnostics here
    public String printDiagnostics(){
        String s = new String();
        s = count + ";";
        s = s + diff + "\r";
        return s;
    }
}
