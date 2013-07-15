import com.jakubrojcek.Order;

import java.util.LinkedHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Jakub
 * Date: 9.3.12
 * Time: 8:58
 * To change this template use File | Settings | File Templates.
 */
public class SecondBookTestLHM {
    public static void main(String[] args) {
        int nPoints = 5;
        LinkedHashMap<Integer, Order>[] book = new LinkedHashMap[nPoints];
        for (int i = 0; i < nPoints; i ++){
            book[i] = new LinkedHashMap();
            System.out.println(i + ": " + book[i]);
        }
        for (int j = 0; j < 100; j ++){
            //book[1].put(100 - j, new com.jakubrojcek.Order(j + 5, System.currentTimeMillis(), true));
        }
        Integer firstElement = book[1].keySet().iterator().next();
        System.out.println(firstElement);

        for (int i = 0; i < nPoints; i ++){
            System.out.println(i + ": " + book[i]);
        }
        

        // Remove trade
        Order o =  book[1].remove(book[1].keySet().iterator().next());

        for (int i = 0; i < nPoints; i ++){
            System.out.println(i + ": " + book[i]);
        }

    }
    

}
