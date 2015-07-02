package com.chesswebsocket.pefi;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * Created by pererikfinstad on 02/07/15.
 */



@ServerEndpoint("/moves")
public class ChessWebsocket {


    private static final Log log = LogFactory.getLog(ChessWebsocket.class);

    private static final String GUEST_PREFIX = "Player ";
    private static final AtomicInteger connectionIds = new AtomicInteger(0);
    private static final Set<ChessWebsocket> connections = new CopyOnWriteArraySet<>();

    private final String nickname;
    private Session session;

    public ChessWebsocket() {
        nickname = GUEST_PREFIX + connectionIds.getAndIncrement();
    }


    @OnOpen
    public void start(Session session) {
        System.out.println(nickname + " joined the game");
        this.session = session;
        connections.add(this);
        String message = String.format("* %s %s", nickname, "has joined the game.");
        broadcast(message);
    }


    @OnClose
    public void end() {
        System.out.println(nickname + " has left the game");
        connections.remove(this);
        String message = String.format("* %s %s",
                nickname, "has left the game.");
        broadcast(message);
    }


    @OnMessage
    public void incoming(String message) {
        System.out.println(message);
        broadcast(nickname + " moved to " + message);
    }




    @OnError
    public void onError(Throwable t) throws Throwable {
        log.error("Error: " + t.toString(), t);
    }


    private static void broadcast(String msg) {
        for (ChessWebsocket client : connections) {
            try {
                synchronized (client) {
                    client.session.getBasicRemote().sendText(msg);
                }
            } catch (IOException e) {
                log.debug("Error: could not make the move", e);
                connections.remove(client);
                try {
                    client.session.close();
                } catch (IOException e1) {
                    // Ignore
                }
                String message = String.format("* %s %s",
                        client.nickname, "has been disconnected.");
                broadcast(message);
            }
        }
    }




} // end class ChessWebSocketServer
