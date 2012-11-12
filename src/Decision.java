/**
 * Created with IntelliJ IDEA.
 * User: Jakub
 * Date: 12.11.12
 * Time: 17:38
 * To change this template use File | Settings | File Templates.
 */
public class Decision {
    private int Bt;
    private int lBt;
    private int spread;
    private int action;

    public Decision(int b, int lb, int s, int a){
        this.Bt = b;
        this.lBt = lb;
        this.spread = s;
        this.action = a;
    }

    public String printDecision(){
        return (Bt + ";" + lBt + ";" + spread + ";" + action + ";" + "\r");
    }
}
