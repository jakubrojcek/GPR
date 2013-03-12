/**
 * Created with IntelliJ IDEA.
 * User: rojcek
 * Date: 21.02.13
 * Time: 19:34
 * To change this template use File | Settings | File Templates.
 */
public class Belief {
    private short n;
    private float mu;
    private float deltaV;

    public Belief(short n, float mu, float deltaV) {
        this.n = n;
        this.mu = mu;
        this.deltaV = deltaV;
    }

    public int getN() {
        return n;
    }

    public float getMu() {
        return mu;
    }

    public float getDeltaV() {
        return deltaV;
    }

    public void setN(short n) {
        this.n = n;
    }

    public void setMu(float mu) {
        this.mu = mu;
    }

    public void setDeltaV(float deltaV) {
        this.deltaV = deltaV;
    }

    public void increaseN(){
        n++;
    }
}
