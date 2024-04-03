package ece568.amazon;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ToFront extends Thread {
    private int port;
    OrderListener orderListener;
    UserInfoListener userInfoListener;

    public ToFront(int port, OrderListener orderListener, UserInfoListener userInfoListener) {
        this.port = port;
        this.orderListener = orderListener;
        this.userInfoListener = userInfoListener;
        this.setDaemon(true);
    }

    /**
     * run thread to listen to front server
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                while (!Thread.currentThread().isInterrupted()) {
                    Socket socket = serverSocket.accept();
                    if (socket != null) {
                        if (orderListener != null) {
                            getPackage(socket);
                        }
                        if (userInfoListener != null) {
                            getUserInfo(socket);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * get package from front server
     * 
     * @param socket
     */
    public void getPackage(Socket socket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            String req = reader.readLine();
            Long packageID = Long.parseLong(req);
            System.out.println("get package " + packageID);
            writer.write(String.format("received %d", packageID));
            writer.flush();
            socket.close();
            if (orderListener != null) {
                orderListener.onOrder(packageID);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * get Username from front server
     * 
     * @param socket
     */
    public void getUserInfo(Socket socket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            String userInfo = reader.readLine();
            writer.write("checking " + userInfo);
            writer.flush();
            socket.close();
            if (userInfoListener != null) {
                userInfoListener.onUserInfo(userInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}