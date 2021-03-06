package io.rocketchat.common.network;

import com.neovisionaries.ws.client.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sachin on 7/6/17.
 */

public class Socket {

    String url;
    WebSocketFactory factory;
    private WebSocket ws;
    private WebSocketAdapter adapter;
    private ReconnectionStrategy strategy;
    private Timer timer;
    private boolean selfDisconnect;

    protected Socket(String url){
        this.url=url;
        adapter=getAdapter();
        factory=new WebSocketFactory().setConnectionTimeout(5000);
        selfDisconnect=false;
    }

    public void setReconnectionStrategy(ReconnectionStrategy strategy) {
        this.strategy = strategy;
    }

    private WebSocketAdapter getAdapter() {
        return new WebSocketAdapter(){
            @Override
            public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                Socket.this.onConnected();
                super.onConnected(websocket, headers);
            }

            @Override
            public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                Socket.this.onDisconnected(closedByServer);
                super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
            }

            @Override
            public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
                Socket.this.onConnectError(exception);
                super.onConnectError(websocket, exception);
            }

            @Override
            public void onTextMessage(WebSocket websocket, String text) throws Exception {
                Socket.this.onTextMessage(text);
                super.onTextMessage(websocket, text);
            }
        };
    }

    /**
     * Function for connecting to server
     */

    protected void createSocket(){
        // Create a WebSocket with a socket connection timeout value.
        try {
            ws = factory.createSocket(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ws.addExtension("permessage-deflate; client_max_window_bits");
        ws.addHeader("Accept-Encoding","gzip, deflate, sdch");
        ws.addHeader("Accept-Language","en-US,en;q=0.8");
        ws.addHeader("Pragma","no-cache");
        ws.addHeader("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36");

        ws.addListener(adapter);
    }

    protected void connect() {
        try
        {
            // Connect to the server and perform an opening handshake.
            // This method blocks until the opening handshake is finished.
            ws.connect();
        }
        catch (OpeningHandshakeException e)
        {
            // A violation against the WebSocket protocol was detected
            // during the opening handshake.
            StatusLine sl = e.getStatusLine();
            System.out.println("=== Status Line ===");
            System.out.format("HTTP Version  = %s\n", sl.getHttpVersion());
            System.out.format("Status Code   = %d\n", sl.getStatusCode());
            System.out.format("Reason Phrase = %s\n", sl.getReasonPhrase());

            // HTTP headers.
            Map<String, List<String>> headers = e.getHeaders();
            System.out.println("=== HTTP Headers ===");
            for (Map.Entry<String, List<String>> entry : headers.entrySet())
            {
                // Header name.
                String name = entry.getKey();

                // Values of the header.
                List<String> values = entry.getValue();

                if (values == null || values.size() == 0)
                {
                    // Print the name only.
                    System.out.println(name);
                    continue;
                }

                for (String value : values)
                {
                    // Print the name and the value.
                    System.out.format("%s: %s\n", name, value);
                }
            }
        }
        catch (WebSocketException e)
        {
            System.out.println("Got websocket exception "+e.getMessage());
            // Failed to establish a WebSocket connection.
        }
    }

    protected void connectAsync(){
        ws.connectAsynchronously();
    }


    protected void sendData(String message){
        ws.sendText(message);
    }

    protected void sendDataInBackground(final String message){
        EventThread.exec(new Runnable() {
            @Override
            public void run() {
                ws.sendText(message);
            }
        });
    }

    public void reconnect(){
        try {
            ws = ws.recreate(5000).connectAsynchronously();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(){
        ws.disconnect();
        selfDisconnect=true;
    }

    protected void onConnected(){
        strategy.setNumberOfAttempts(0);
        System.out.println("Connected");
    }

    protected void onDisconnected(boolean closedByServer){
        System.out.println("Disconnected");
        if (strategy!=null && !selfDisconnect){
            if (strategy.getNumberOfAttempts()<strategy.getMaxAttempts()){
                timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        reconnect();
                        strategy.processAttempts();
                        timer.cancel();
                        timer.purge();
                    }
                },strategy.getReconnectInterval());

            }else{
                System.out.println("Number of attempts are complete");
            }
        }else{
            selfDisconnect=false;
        }
    }

    protected void onConnectError(Exception websocketException){
        System.out.println("Onconnect Error");
        onDisconnected(true);
    }

    protected void onTextMessage(String text) throws Exception{
        System.out.println("Message is " + text);
    }
}

