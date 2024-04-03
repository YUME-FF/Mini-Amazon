package ece568.amazon;

import ece568.amazon.proto.UpsAmazon.*;

import com.google.protobuf.*;
import java.util.*;
import java.io.*;

public class ToUPS extends MessageHandler {
    private long upsID;

    /**
     * Constructor for ToUPS
     * 
     * @param upsID
     */
    public ToUPS(long upsID) {
        this.upsID = upsID;
    }

    /**
     * set upsID
     */
    public void setUpsID(long upsID, int port) {
        this.upsID = upsID;

    }

    /**
     * get upsID
     * 
     * @return upsID
     */
    public long getUpsID() {
        return upsID;
    }

    /**
     * send ACK to ups server
     * 
     * @param packageID
     * @param output
     * @return
     * @throws IOException
     */
    public void sendACK(UACommands response, OutputStream output) throws IOException {
        ArrayList<Long> seq = new ArrayList<>();
        if (response.getTruckArrivedCount() > 0) {
            for (UATruckArrived truckArrived : response.getTruckArrivedList()) {
                seq.add(truckArrived.getSeqnum());
            }
        }
        if (response.getUpdatePackageStatusCount() > 0) {
            for (UAUpdatePackageStatus packageStatus : response.getUpdatePackageStatusList()) {
                seq.add(packageStatus.getSeqnum());
            }
        }
        if (response.getDeliveredCount() > 0) {
            for (UATruckDeliverMade delivered : response.getDeliveredList()) {
                seq.add(delivered.getSeqnum());
            }
        }
        if (response.getErrorCount() > 0) {
            for (Err error : response.getErrorList()) {
                seq.add(error.getSeqnum());
            }
        }
        if (seq.size() > 0) {
            AUCommands.Builder ab = AUCommands.newBuilder();
            for (long s : seq) {
                ab.addAcks(s);
            }
            synchronized (output) {
                System.out.println("Send ACKS to UPS");
                System.out.println(ab);
                sendMSG(ab, output);
            }
        }
    }
}
