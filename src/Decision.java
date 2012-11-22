/**
 * Created with IntelliJ IDEA.
 * User: Jakub
 * Date: 12.11.12
 * Time: 17:38
 * To change this template use File | Settings | File Templates.
 */
public class Decision {
    private int Bt;
    private int At;
    private int lBt;
    private int lAt;
    private int action;
    private int previousAction;
    private int previousBt;
    private int previousAt;

    public Decision(int[] bi, int ac, int[] prevTrAc){
        this.Bt = bi[0];
        this.At = bi[1];
        this.lBt = bi[2];
        this.lAt = bi[3];
        this.action = ac;
        this.previousAction = prevTrAc[0];
        this.previousBt = prevTrAc[1];
        this.previousAt = prevTrAc[2];
    }

    public String printDecision(){
        return (Bt + ";" + At + ";" + lBt + ";" + lAt + ";" + action + ";" +
                + previousAction + ";" + previousBt + ";" + previousAt + ";" + "\r");
    }
}
