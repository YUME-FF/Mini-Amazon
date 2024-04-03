package ece568.amazon;

import java.util.concurrent.ConcurrentHashMap;

import ece568.amazon.proto.UpsAmazon.*;
import ece568.amazon.proto.WorldAmazon.*;
import com.google.protobuf.*;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class AmazonServer {
    private String WORLD_IP;
    private int AMAZON_PORT;
    private int UPS_PORT;
    private ToWorld toWorld;
    private ToUPS toUPS;
    private ToFront toFront_Userinfo;
    private ToFront toFront_Order;
    private static final int packagePort = 11111;
    private static final int userinfoPort = 22222;

    private InputStream input; // Amazon receive msg from this.
    private OutputStream output; // Amazon Send msg by this.

    private final ThreadPoolExecutor threadPool;

    private Map<Long, Timer> worldRequestMap;
    private Map<Long, Timer> UPSRequestMap;

    Socket UPSsocket;

    public AmazonServer(String worldIP, int amazonPort, int upsPort) {
        this.WORLD_IP = worldIP;
        this.AMAZON_PORT = amazonPort;
        this.UPS_PORT = upsPort;
        this.toWorld = new ToWorld(-1);
        this.toUPS = new ToUPS(-1);
        this.threadPool = new ThreadPoolExecutor(20, 80, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        this.worldRequestMap = new ConcurrentHashMap<>();
        this.UPSRequestMap = new ConcurrentHashMap<>();
        this.processingOrderMap = new ConcurrentHashMap<>();
        this.wareHouseList = new ArrayList<>();
        this.seqNumber = 0L;
        this.finishedOrderMap = new ConcurrentHashMap<>();
    }

    private ConcurrentHashMap<Long, Order> processingOrderMap;
    private ConcurrentHashMap<Long, Order> finishedOrderMap;
    private List<AInitWarehouse> wareHouseList;
    private Long seqNumber;

    /**
     * run the amazon server
     */
    public void run() {
        threadPool.prestartAllCoreThreads();
        listenPackage();
        // listenUserinfo();
        // runWorldServer();
        // runUPSServer();
    }

    /**
     * initialize the amazon server, connect to world server and ups server
     */
    public void init() throws IOException {
        System.out.println("Starting connecting to world server...");
        System.out.println("Connecting to UPS server...");
        while (true) {
            try {
                UPSsocket = new Socket(WORLD_IP, UPS_PORT);
            } catch (IOException e) {
                System.out.println("Failed to connect to UPS server, retrying...");
                // set timeout for 10s
                // UPSsocket.setSoTimeout(10000);
                continue;
            }
            if (UPSsocket != null) {
                CodedInputStream codedInputStream = CodedInputStream.newInstance(UPSsocket.getInputStream());
                UAConnect fromUPS = UAConnect.parseFrom(codedInputStream.readByteArray());
                AUConnected.Builder builderToUPS = AUConnected.newBuilder();
                builderToUPS.setWorldid(fromUPS.getWorldid());
                builderToUPS.setResult("connected!");

                toUPS.sendMSG(builderToUPS, UPSsocket.getOutputStream());
                System.out.println("UAConnect: " + fromUPS);
                if (fromUPS.hasWorldid()) {
                    try {
                        toWorld.setWorldID(fromUPS.getWorldid());
                        boolean result = connectToWorld(fromUPS.getWorldid(), false);
                        System.out.println("result is " + result);
                        if (result) {
                            System.out.println("Connected to world server!");
                            break;
                        }
                    } catch (IOException e) {
                        System.out.println("Failed to connect to world server, retrying...");
                    }
                }
            }
        }
    }

    /**
     * connect to world server, send the warehouse information
     * 
     * @param worldId
     * @param reconnect
     * @return true if connected successfully
     * @throws IOException
     */
    public boolean connectToWorld(long worldId, boolean reconnect) throws IOException {
        Socket socket = new Socket(WORLD_IP, AMAZON_PORT);
        input = socket.getInputStream();
        output = socket.getOutputStream();
        AConnect.Builder builder = AConnect.newBuilder();
        builder.setIsAmazon(true);
        if (!reconnect) {
            setWarehouse();
            System.out.println(wareHouseList);
        }
        builder.setWorldid(worldId);
        builder.addAllInitwh(wareHouseList);
        System.out.println(builder);

        // Send Msg to the World
        toWorld.sendMSG(builder, output);
        CodedInputStream codedInputStream = CodedInputStream.newInstance(input);
        AConnected acc = AConnected.parseFrom(codedInputStream.readByteArray());
        System.out.println(acc);
        System.out.println("WorldID: " + acc.getWorldid());
        System.out.println("Result: " + acc.getResult());
        return acc.getResult().equals("connected!");
    }

    /**
     * run world server, handle message from world server
     */
    public void runWorldServer() {
        Thread worldServer = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    AResponses.Builder ab = AResponses.newBuilder();
                    toWorld.recvMSG(ab, this.input); // receive message from world server
                    handleWorldMessage(ab.build()); // handle the message from world server
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        worldServer.start();
    }

    /**
     * run ups server, handle message from ups server
     */
    public void runUPSServer() {
        Thread upsServer = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (UPSsocket != null) {
                    try {
                        InputStream UpsInput = UPSsocket.getInputStream();
                        if (UpsInput.available() == 0) {
                            continue;
                        }
                        UACommands.Builder fromUPS = UACommands.newBuilder();
                        toUPS.recvMSG(fromUPS, UpsInput);
                        handleUPSMessage(fromUPS.build());
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        upsServer.start();
    }

    /**
     * listen from the front to get package
     */
    void listenPackage() {
        toFront_Order = new ToFront(packagePort, packageID -> {
            System.out.println("get package ID: " + packageID);
            buyOrder(packageID);
        }, null);
        toFront_Order.start();
    }

    /**
     * listen from the front to get username
     */
    void listenUserinfo() {
        toFront_Userinfo = new ToFront(userinfoPort, null, userinfo -> {
            System.out.println("get userinfo: " + userinfo);
            sendUserInfoToUPS(userinfo);
        });
        toFront_Userinfo.start();
    }

    void sendUserInfoToUPS(String userinfo) {
        String[] info = userinfo.split(" ");
        String userID = info[0];
        String username = info[1];
        String password = info[2];
        threadPool.execute(() -> {
            Long seq = getSeqnum();
            AURequestSendUserInfo.Builder requestSendUserInfo = AURequestSendUserInfo.newBuilder();
            requestSendUserInfo.setSeqnum(seq);
            requestSendUserInfo.setUserid(Integer.parseInt(userID));
            requestSendUserInfo.setUsername(username);
            requestSendUserInfo.setPassword(password);
            System.out.println("Checking Userinfo:");
            System.out.println("userID: " + userID);
            System.out.println("username: " + username);
            System.out.println("password: " + password);
            AUCommands.Builder ab = AUCommands.newBuilder();
            ab.addUserInfo(requestSendUserInfo);
            sendToUPS(seq, ab);
        });
    }

    /**
     * generate and keep count of the seqnum(for socket communication)
     * increase by 1 each time
     * 
     * @return the seqnum
     */
    private synchronized Long getSeqnum() {
        Long val = seqNumber;
        seqNumber++;
        return val;
    }

    /**
     * initialize the warehouse location
     */
    public void setWarehouse() {
        // each warehouse is of type AInitWarehouse
        // set 2 warehouse, whose locaton is (0,0) and (10,10)
        AInitWarehouse warehouse1 = AInitWarehouse.newBuilder().setX(0).setY(0).setId(0).build();
        AInitWarehouse warehouse2 = AInitWarehouse.newBuilder().setX(10).setY(10).setId(1).build();
        wareHouseList.add(warehouse1);
        wareHouseList.add(warehouse2);
    }

    // get the warehouse location X index
    public Integer getWarehouseX(int whID) {
        Integer warehouseX = wareHouseList.get(whID).getX();
        return warehouseX;
    }

    // get the warehouse location Y index
    public Integer getWarehouseY(int whID) {
        Integer warehouseY = wareHouseList.get(whID).getY();
        return warehouseY;
    }

    /**
     * dispatch an order to the closest warehouse
     * 
     * @param shipID the shipID of the order
     */
    public void dispatchOrder(long shipID) {
        // 1. get the order from the processingOrderMap
        // 2. calculate the distance between the order and the warehouse
        // 3. find the closest warehouse
        // 4. set order's whID to the closest warehouse
        Order order = processingOrderMap.get(shipID);
        System.out.println("dispatch order: " + order.getShipID());
        int orderX = order.getLocationX();
        int orderY = order.getLocationY();
        System.out.println("order location: " + orderX + " " + orderY);
        int minDistance = Integer.MAX_VALUE;
        int closestWarehouse = 0;
        for (int i = 0; i < wareHouseList.size(); i++) {
            int warehouseX = wareHouseList.get(i).getX();
            int warehouseY = wareHouseList.get(i).getY();
            int distance = (int) Math.sqrt(Math.pow(orderX - warehouseX, 2) + Math.pow(orderY - warehouseY, 2));
            if (distance < minDistance) {
                minDistance = distance;
                closestWarehouse = i;
            }
        }
        order.setWhID(closestWarehouse);
    }

    /**
     * send ACommnads to the world
     * 
     * @param seq     the seqnum of the command
     * @param command the command to be sent
     */
    public void sendToWorld(Long seq, ACommands.Builder command) {
        System.out.println("Sending Command to World ...");
        command.setSimspeed(300);
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (output) {
                    try {
                        toWorld.sendMSG(command, output);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, 5000);
        worldRequestMap.put(seq, t); // store the timer in the map
    }

    /**
     * send AUCommands to the UPS
     * 
     * @param seq     the seqnum of the command
     * @param command
     */
    public void sendToUPS(Long seq, AUCommands.Builder command) {
        System.out.println("Sending Command to UPS ...");
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                OutputStream out = null;
                try {
                    out = UPSsocket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (out) {
                    try {
                        toUPS.sendMSG(command, out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, 5000);
        UPSRequestMap.put(seq, t); // store the timer in the map
    }

    /**
     * find the order in the processingOrderMap that is same as purchasedmore from
     * world
     * 
     */
    public Order findOrder(APurchaseMore purchased) {
        AProduct aproduct = purchased.getThings(0);
        for (Order order : processingOrderMap.values()) {
            if (order.getWhID() != purchased.getWhnum()) {
                continue;
            }
            if (order.getProductID() != aproduct.getId()) {
                continue;
            }
            if (order.getProductCount() != aproduct.getCount()) {
                continue;
            }
            return order;
        }
        return null;
    }

    /**
     * handle the message from world server
     * 
     * @param response the message from world server
     */
    public void handleWorldMessage(AResponses response) throws IOException {
        System.out.println("Received world message:");
        System.out.println(response.toString());
        toWorld.sendACK(response, output); // why ?
        if (response.getArrivedCount() > 0) {
            for (APurchaseMore purchased : response.getArrivedList()) {
                Order order = findOrder(purchased);
                if (order == null) {
                    continue;
                }
                packOrder(order.getShipID());
            }
        }
        if (response.getReadyCount() > 0) {
            for (APacked packed : response.getReadyList()) {
                Long shipID = packed.getShipid();
                callTruck(shipID);
            }
        }
        if (response.getLoadedCount() > 0) {
            for (ALoaded loaded : response.getLoadedList()) {
                Long shipID = loaded.getShipid();
                truckGoDeliver(shipID);
            }
        }
        if (response.getErrorCount() > 0) {
            for (AErr ae : response.getErrorList()) {
                System.err.println(ae.getErr());
            }
        }
        if (response.getPackagestatusCount() > 0) {
            // *********** 目前只是打印出来status
            for (APackage ap : response.getPackagestatusList()) {
                System.out.println("shipID: " + ap.getPackageid() + " status: " + ap.getStatus());
            }
        }
        if (response.getAcksCount() > 0) {
            for (long ack : response.getAcksList()) {
                if (worldRequestMap.containsKey(ack)) {
                    worldRequestMap.get(ack).cancel();
                    worldRequestMap.remove(ack);
                }
            }
        }
        if (response.hasFinished()) {
            System.out.println("Amazon Disconnected from the World!");
            connectToWorld(toWorld.getWorldID(), true); // reconnected to world
        }
    }

    public Integer getWarehouseID(Integer X, Integer Y) {
        for (int i = 0; i < wareHouseList.size(); i++) {
            if (wareHouseList.get(i).getX() == X && wareHouseList.get(i).getY() == Y) {
                return i;
            }
        }
        return null;
    }

    /**
     * handle the message from UPS
     * 
     * @param response
     */
    public void handleUPSMessage(UACommands response) throws IOException {
        System.out.println("Received UPSMessage:");
        System.out.println(response.toString());
        // send ACK to UPS
        toUPS.sendACK(response, UPSsocket.getOutputStream());
        for (Long ack : response.getAcksList()) { // check if there is any ACK
            System.out.println("UPS ACK is :" + ack);
            if (UPSRequestMap.containsKey(ack)) {
                /** ***********还不确定cancel的作用 */
                UPSRequestMap.get(ack).cancel(); // when sychronized, cancel the request
                UPSRequestMap.remove(ack);
                System.out.println("Canceled UPS ACK is :" + ack);
            }
        }
        if (response.getTruckArrivedCount() > 0) {
            for (UATruckArrived truckArrived : response.getTruckArrivedList()) {
                int truckID = truckArrived.getTruckid();
                int warehouseX = truckArrived.getX();
                int warehouseY = truckArrived.getY();
                Integer wareHouseID = getWarehouseID(warehouseX, warehouseY);
                // Integer warehouseID = truckArrived.getWhid();
                for (Order order : processingOrderMap.values()) {
                    if (order.getStatus().equals("packed") && order.getWhID().equals(wareHouseID)) {
                        order.setTruckid(truckID);
                    }
                }
                List<Long> shipIDList = getShipID(truckID);
                for (Long shipID : shipIDList) {
                    truckArrived(shipID, truckID); // send to UPS we are loading
                    putOnTruck(shipID); // send to world we are loading
                }
            }
        }
        // **************好像用不到？
        if (response.getUpdatePackageStatusCount() > 0) {
            for (UAUpdatePackageStatus packageStatus : response.getUpdatePackageStatusList()) {
                Long shipID = packageStatus.getPackageid();
                String status = packageStatus.getStatus();
                int packageLocX = packageStatus.getX();
                int packageLocY = packageStatus.getY();

                // TODO: updatePackageStatus(shipID, status);
            }
        }
        if (response.getDeliveredCount() > 0) {
            for (UATruckDeliverMade deliverMade : response.getDeliveredList()) {
                Long shipID = deliverMade.getPackageid();
                Integer truckID = deliverMade.getTruckid();
                // List<Long> shipIDList = getShipID(truckID);
                // TODO: updatePackageStatus(shipID, "delivered"); and remove the order from
                // processingOrderMap
                // remove the order using shipID from processingOrderMap

                for (Order order : processingOrderMap.values()) {
                    if (order.getShipID().equals(shipID)) {
                        OrderDAO orderDAO = new OrderDAO();
                        order.setStatus("delivered");
                        orderDAO.updateOrder(order);
                        processingOrderMap.remove(order.getShipID());
                        finishedOrderMap.put(shipID, order);
                    }
                }
            }
        }
        if (response.getErrorCount() > 0) {
            for (Err error : response.getErrorList()) {
                String errDescription = error.getErr();
                long originseqNum = error.getOriginseqnum();
                System.out.println("error: " + errDescription + " \noriginseqNum: " + originseqNum);
            }
        }
        if (response.getFinished()) {
            System.out.println("UPS Disconnected!");
        }
    }

    /**
     * get all the shipID in processingOrderMap that is same as truckID
     */
    public List<Long> getShipID(int truckID) {
        List<Long> shipIDList = new ArrayList<>();
        for (Order order : processingOrderMap.values()) {
            if (order.getTruckID() == truckID && order.getStatus().equals("packed")) {
                shipIDList.add(order.getShipID());
            }
        }
        return shipIDList;
    }

    /**
     * pack the order, and send the order status to the UPS (AUTruckGoLoad)
     * 
     * @param order
     * @return
     */
    public void truckArrived(long shipID, int truckID) throws IOException {
        if (!processingOrderMap.containsKey(shipID)) {
            System.out.println("truckArrived failed. We do not have that Order.");
            return;
        }
        System.out.println("Loading Order: " + shipID);
        OrderDAO orderDAO = new OrderDAO();
        Order order = processingOrderMap.get(shipID);
        // TruckID is already set at this step
        order.setStatus("loading");
        orderDAO.updateOrder(order);
        threadPool.execute(() -> {
            AUCommands.Builder load = AUCommands.newBuilder();
            AUTruckGoLoad.Builder truckGoLoad = AUTruckGoLoad.newBuilder();
            Long seq = getSeqnum();
            truckGoLoad.setTruckid(truckID);
            truckGoLoad.setSeqnum(seq);
            load.addLoading(truckGoLoad.build());
            sendToUPS(seq, load);
        });
    }

    /**
     * Query for the buyorder (stored in database by frontend when an order is
     * placed)
     * Updates the order status to "preparing", save back to database
     * Adds order to the processing order map, and send it to world(warehouse)
     * 
     * @param shipid the shipid of the order (automated generated by frontend)
     */
    public void buyOrder(Long shipid) {
        System.out.println("buyOrder: " + shipid);
        threadPool.execute(() -> {
            // 1. query for the buyorder
            OrderDAO orderDAO = new OrderDAO();
            Order order = orderDAO.getOrder(shipid);
            order.setStatus("preparing");
            orderDAO.updateOrder(order);
            // 2. add order to the processing order map
            processingOrderMap.put(shipid, order);

            dispatchOrder(shipid);

            /* this is for testing if writing back to DB works --> it works */
            // try {
            //     Thread.sleep(5000);
            //     truckArrived(shipid, 3);
            // } catch (Exception e) {
            //     e.printStackTrace();
            // }

            // 3. send the order to the world(warehouse)
            APurchaseMore newOrder = convertOrderToAPurchaseMore(order);
            ACommands.Builder commands = ACommands.newBuilder();
            commands.addBuy(newOrder);
            Long seq = newOrder.getSeqnum();
            sendToWorld(seq, commands);
        });
    }

    /**
     * check the username, if the username is valid, then send the order to the
     * world(warehouse)
     * 
     * @param username
     */
    void checkOrder(String username) {
        threadPool.execute(() -> {

        });
    }

    /**
     * convert the order to APurchaseMore
     * 
     * @param order the order to be converted
     * @return the converted APurchaseMore
     */
    public APurchaseMore convertOrderToAPurchaseMore(Order order) {
        APurchaseMore.Builder newOrder = APurchaseMore.newBuilder();

        Long seq = getSeqnum();

        AProduct product = AProduct.newBuilder()
                .setId(order.getProductID())
                .setDescription(order.getProductDescription())
                .setCount(order.getProductCount())
                .build();
        newOrder.addThings(product);
        newOrder.setWhnum(order.getWhID());
        newOrder.setSeqnum(seq);
        return newOrder.build();
    }

    /**
     * pack the order, and send the order status to the world
     * *** step: APack ***
     * 
     * @param order
     * @return
     */
    public void packOrder(long shipID) {

        threadPool.execute(() -> {
            if (!processingOrderMap.containsKey(shipID)) {
                System.out.println("PackOrder failed. We do not have that Order.");
                return;
            }
            System.out.println("Packing Order: " + shipID);
            OrderDAO orderDAO = new OrderDAO();
            Order order = processingOrderMap.get(shipID);
            order.setStatus("packing");
            orderDAO.updateOrder(order);
            ACommands.Builder pack = ACommands.newBuilder();
            APack topack = convertOrdertoPack(order);
            pack.addTopack(topack);
            // Long seq = pack.getTopackList().get(0).getSeqnum();
            Long seq = topack.getSeqnum();
            sendToWorld(seq, pack);
        });
    }

    /**
     * convert the order to APack
     * 
     * @param order the order to be converted
     * @return the converted APack
     */
    public APack convertOrdertoPack(Order order) {
        APack.Builder newOrder = APack.newBuilder();
        Long seq = getSeqnum();
        AProduct product = AProduct.newBuilder()
                .setId(order.getProductID())
                .setDescription(order.getProductDescription())
                .setCount(order.getProductCount())
                .build();
        newOrder.addThings(product);
        newOrder.setShipid(order.getShipID());
        newOrder.setWhnum(order.getWhID());
        newOrder.setSeqnum(seq);
        return newOrder.build();
    }

    /**
     * send command to UPS to pick up the order (this is executed when the order is
     * packed)
     * 
     * @param shipID the shipID of the order
     */
    public void callTruck(long shipID) {
        if (!processingOrderMap.containsKey(shipID)) {
            System.out.println("CallTruck failed. We do not have that Order.");
            return;
        }
        OrderDAO orderDAO = new OrderDAO();
        Order order = processingOrderMap.get(shipID);
        order.setStatus("packed");
        orderDAO.updateOrder(order);
        threadPool.execute(() -> {
            AUCommands.Builder commands = AUCommands.newBuilder();
            AUCallTruck callTruck = convertOrdertoCallTruck(order);
            commands.addCallTruck(callTruck);
            Long seq = callTruck.getSeqnum();
            sendToUPS(seq, commands);
        });
    }

    /**
     * convert order to AUCallTruck
     */
    public AUCallTruck convertOrdertoCallTruck(Order order) {
        AUCallTruck.Builder newOrder = AUCallTruck.newBuilder();
        long seq = getSeqnum();
        newOrder.addThings(AUProduct.newBuilder()
                .setId(order.getShipID())
                .setDescription(order.getProductDescription())
                .setCount(order.getProductCount())
                .setDestX(order.getLocationX())
                .setDestY(order.getLocationX())
                .setUserid(order.getUserID())
                .build());
        newOrder.setWhid(order.getWhID());
        newOrder.setSeqnum(seq);
        newOrder.setWhX(getWarehouseX(order.getWhID()));
        newOrder.setWhY(getWarehouseY(order.getWhID()));

        return newOrder.build();
    }

    /**
     * send command to world to put the order on the truck
     * 
     * @param shipID
     */
    public void putOnTruck(long shipID) {
        if (!processingOrderMap.containsKey(shipID)) {
            System.out.println("PutOnTruck failed. We do not have that Order.");
            return;
        }
        Order order = processingOrderMap.get(shipID);
        // order.setStatus("loading");
        threadPool.execute(() -> {
            ACommands.Builder commands = ACommands.newBuilder();
            APutOnTruck putOnTruck = convertOrdertoPutOnTruck(order);
            commands.addLoad(putOnTruck);
            // long seq = commands.getLoadList().get(0).getSeqnum();
            Long seq = putOnTruck.getSeqnum();
            sendToWorld(seq, commands);
        });
    }

    /**
     * convert order to APutOnTruck
     * 
     * @param order the order to be converted
     */
    public APutOnTruck convertOrdertoPutOnTruck(Order order) {
        APutOnTruck.Builder newOrder = APutOnTruck.newBuilder();
        Long seq = getSeqnum();
        newOrder.setShipid(order.getShipID());
        newOrder.setTruckid(order.getTruckID());
        newOrder.setWhnum(order.getWhID());
        newOrder.setSeqnum(seq);
        return newOrder.build();
    }

    /**
     * send command to UPS to deliver the order
     */
    public void truckGoDeliver(long shipID) {
        if (!processingOrderMap.containsKey(shipID)) {
            System.out.println("TruckGoDeliver failed. We do not have that Order.");
            return;
        }
        OrderDAO orderDAO = new OrderDAO();
        Order order = processingOrderMap.get(shipID);
        order.setStatus("delivering");
        orderDAO.updateOrder(order);
        threadPool.execute(() -> {
            AUCommands.Builder commands = AUCommands.newBuilder();
            AUTruckGoDeliver truckGoDeliver = convertOrdertoTruckGoDeliver(order);
            commands.addTruckGoDeliver(truckGoDeliver);
            // Long seq = commands.getTruckGoDeliverList().get(0).getSeqnum();
            Long seq = truckGoDeliver.getSeqnum();
            sendToUPS(seq, commands);
        });
    }

    /**
     * convert order to TruckGoDeliver
     * 
     * @param order the order to be converted
     */
    public AUTruckGoDeliver convertOrdertoTruckGoDeliver(Order order) {
        AUTruckGoDeliver.Builder newOrder = AUTruckGoDeliver.newBuilder();
        Long seq = getSeqnum();
        newOrder.setTruckid(order.getTruckID());
        newOrder.setSeqnum(seq);
        return newOrder.build();
    }

}
