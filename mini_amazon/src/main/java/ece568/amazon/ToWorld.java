package ece568.amazon;

import ece568.amazon.proto.WorldAmazon.*;
import com.google.protobuf.*;
import java.io.*;
import java.util.*;

public class ToWorld extends MessageHandler {
    private long worldID;

    /**
     * Constructor for ToWorld
     * 
     * @param worldID
     */
    public ToWorld(long worldID) {
        this.worldID = worldID;
    }

    /**
     * set worldID
     */
    public void setWorldID(long worldID) {
        this.worldID = worldID;
    }

    /**
     * get worldID
     * 
     * @return worldID
     */
    public long getWorldID() {
        return worldID;
    }

    /**
     * send ack to world server
     * 
     * @param res
     * @param output
     * @throws IOException
     */
    public void sendACK(AResponses res, OutputStream output) throws IOException {
        ArrayList<Long> seq = new ArrayList<>();
        for (APurchaseMore apc : res.getArrivedList()) {
            seq.add(apc.getSeqnum());
        }
        for (APacked ap : res.getReadyList()) {
            seq.add(ap.getSeqnum());
        }
        for (ALoaded al : res.getLoadedList()) {
            seq.add(al.getSeqnum());
        }
        for (AErr ae : res.getErrorList()) {
            seq.add(ae.getSeqnum());
        }
        for (APackage ap : res.getPackagestatusList()) {
            seq.add(ap.getSeqnum());
        }
        // This means it at least needs to send one ack.
        if (seq.size() > 0) {
            ACommands.Builder ares = ACommands.newBuilder();
            for (Long s : seq) {
                ares.addAcks(s);
            }
            synchronized (output) {
                System.out.println("Now Send Acks to the World.");
                sendMSG(ares, output);
            }
        }
    }
}
