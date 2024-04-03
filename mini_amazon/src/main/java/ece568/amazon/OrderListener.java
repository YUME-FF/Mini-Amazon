package ece568.amazon;

public interface OrderListener {
    /**
     * notify when order is received
     * 
     * @param packageID
     */
    void onOrder(long packageID);
}
