import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

/**
 *
 * @author William
 */


class Connection {
    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    String username;
    
    public Connection(Socket socket, String user){
        try {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.username = user;
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Error creatind connection client.");
        }       
    }
    
    public void write(String msg){
        try {
            this.out.writeUTF(msg);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Error sendind message to client.");
        }
    }
    
    public void welcomeMessage(String msg){
        try {
            TimeUnit.MILLISECONDS.sleep(1500);
            write(msg);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            System.err.println("Error sendind welcome message to client.");
        }
    }
}

public class SocketServer {

    static Socket socket;
    static ServerSocket serverSocket;        
    static Map<String, Connection> listUsers = new HashMap<String, Connection>();
    
    // controllers //
    private int count = 0;
    private String defaultName = "USER_";
    
    public SocketServer() {
        try {            
            serverSocket = new ServerSocket(8084);
            System.out.println("Server On, listenning...");
            while(true){                                                
                Socket newSocket = serverSocket.accept();                
                String user = defaultName + String.valueOf(count++);
                Connection client = new Connection(newSocket, user);
                                
                listUsers.put(user, client);
                System.out.println("New client just connected: " + user);
                
                Thread thread = new Thread(new SocketListenner(client, user));
                thread.setPriority(10);
                thread.setDaemon(true);
                thread.start();
            }            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private class SocketListenner implements Runnable{
        private Connection client;
        private String user = "";
        
        public SocketListenner(Connection socket, String user){
            try {
                this.client = socket;
                this.user = user;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        @Override
        public void run() {
            try {
                // hellow //
                for (Map.Entry<String, Connection> entry : listUsers.entrySet()) {                        
                    String current = entry.getKey();
                    Connection client = entry.getValue();
                    System.out.println("Sending hello to: " + current);
                    client.welcomeMessage(user + " Entrou!!!");
                }    
                // loop response //
                while (!serverSocket.isClosed()){                                                        
                    String message = client.in.readUTF();
                    System.out.println("Data received...: " + message);                    
                    for (Map.Entry<String, Connection> entry : listUsers.entrySet()) {                        
                        String current = entry.getKey();
                        Connection client = entry.getValue();
                        System.out.println("Sending message to: " + current + " | " + message);
                        client.write(user + ": " + message);
                    }    
                }           
                // connection end //
                listUsers.remove(user);
                System.out.println("Client just disconnected: " + user);
            } catch (Exception ex) {
                if (ex instanceof EOFException){
                    listUsers.remove(user);
                    System.out.println("Client just disconnected: " + user);
                }else if (ex instanceof SocketException){                    
                    listUsers.remove(user);
                    System.out.println("Client just disconnected: " + user);
                }else{
                    ex.printStackTrace();
                }
            }
        }
        
    }
    
    public static void main(String[] args) throws IOException {
        new SocketServer();                
    }
}
