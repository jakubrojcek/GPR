/**
 * Created by IntelliJ IDEA.
 * User: Jakub
 * Date: 24.5.12
 * Time: 20:45
 * To change this template use File | Settings | File Templates.
 * stores price and order from traders decision
 */
public class PriceOrder { // <Integer, Order>{
    private Order currentOrder;
    private int pricePosition;

    public PriceOrder(int p, Order o){
        this.pricePosition = p;
        this.currentOrder = o;
    }
    public int getPrice(){
        return pricePosition;
    }
    public Order getCurrentOrder(){
        return currentOrder;
    }


}
