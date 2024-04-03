package ece568.amazon;

public class Main {
    // private static final String WORLD_IP = "vcm-32430.vm.duke.edu";
    // private static final String WORLD_IP = "65.75.220.176";
    private static final String WORLD_IP = "vcm-30598.vm.duke.edu";
    private static final int AMAZON_PORT = 23456;
    // QILAOSHI PORT
    // private static final int UPS_PORT = 7474;
    private static final int UPS_PORT = 22222;

    public static void main(String[] args) {

        System.out.println("Initializing the AmazonServer: " + WORLD_IP);
        System.out.println("Amazon PORT is : " + AMAZON_PORT);
        System.out.println("UPS PORT is : " + UPS_PORT);

        AmazonServer amazonServer = new AmazonServer(WORLD_IP, AMAZON_PORT,
                UPS_PORT);
        // while (true) {
        //     try {
        //         amazonServer.init();
        //         break;
        //     } catch (Exception e) {
        //         System.out.println(e.getMessage());
        //     }
        // }
        amazonServer.run();
    }
}