/**
 * Created by IntelliJ IDEA.
 * User: Jakub
 * Date: 5.3.12
 * Time: 17:30
 * To change this template use File | Settings | File Templates.
 */
public class FirstBookTest {
    public static void main(String[] args) {
        LimitOrderBook fb = new LimitOrderBook();
        int[] outstanding = {-1,-1,-2,0,1,1,2};
        fb.setLOB(outstanding);
        int[] bid = {-1,4};
        int[] result = fb.TransactionRule(bid);
    }
}
