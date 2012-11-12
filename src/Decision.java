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

    public void Decision(int b, int lb, int s, int a){
        Bt = b;
        lBt = lb;
        spread = s;
        action = a;
    }

    public String printDecision(){
        return (Bt + ";" + lBt + ";" + spread + ";" + action + ";" + "\r");
    }
}
