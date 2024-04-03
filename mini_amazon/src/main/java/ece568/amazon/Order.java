package ece568.amazon;

import ece568.amazon.proto.WorldAmazon.*;
import javax.persistence.*;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipID")
    private Long shipID; // no need for initialize shipID, it will be auto generated

    @Column(name = "whID")
    private Integer whID;

    @Column(name = "truckID")
    private Integer truckID;

    @Column(name = "locationX")
    private Integer locationX;

    @Column(name = "locationY")
    private Integer locationY;

    // // attributes of the AProduct
    @Column(name = "productID")
    private Integer productID;

    @Column(name = "productDescription")
    private String productDescription;

    @Column(name = "productCount")
    private Integer productCount;

    @Column(name = "status")
    private String status;

    @Column(name = "userID")
    private Integer userID;

    public Order() {
    }

    public Long getShipID() {
        return shipID;
    }

    public Integer getWhID() {
        return whID;
    }

    public int getProductID() {
        return productID;
    }

    public Integer getUserID() {
        return userID;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public int getProductCount() {
        return productCount;
    }


    // write get truckID
    public int getTruckID() {
        return truckID;
    }

    public int getLocationX() {
        return locationX;
    }

    public int getLocationY() {
        return locationY;
    }

    public Integer getUser() {
        return userID;
    }

    public Location getDeliverAddress() {
        Location deliverAddress = new Location(locationX, locationY);
        return deliverAddress;
    }

    public APack getPack() {
        // randomly generate seqnum
        // int seq = getSeqnum();
        APack pack = APack.newBuilder()
                .setWhnum(whID)
                .setShipid(shipID)
                .addThings(AProduct.newBuilder()
                        .setId(productID)
                        .setDescription(productDescription)
                        .setCount(productCount)
                        .build())
                .build();
        return pack;
    }

    public String getStatus() {
        return status;
    }

    public void setTruckid(int truckID) {
        this.truckID = truckID;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setWhID(int whID) {
        this.whID = whID;
    }

}
