package com.afesguerra.pictionary.server;

import com.afesguerra.pictionary.server.model.Player;
import com.afesguerra.pictionary.server.model.WordGenerator;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class PictionaryServer {
    private static final int MAX_PLAYERS = 3;
    private static final int LISTENING_PORT = 1337;
    private static final int BUFFER_SIZE = 99999;
    static WordGenerator palabras = new WordGenerator();

    public static void main(String[] args) throws IOException, InterruptedException {
        logHostAddress();

        final Collection<Player> players = waitForPlayers();

        final Match match = new Match(players, palabras.getWord());
        match.start();
        match.join();
    }

    private static Collection<Player> waitForPlayers() throws IOException {
        final Map<String, Player> players = new HashMap<>();
        final DatagramSocket sck = new DatagramSocket(LISTENING_PORT);
        while (players.size() < MAX_PLAYERS) {
            final DatagramPacket pck = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
            sck.receive(pck);
            final String newPlayerName = new String(pck.getData(), 0, pck.getLength());
            log.trace("pck: {}", newPlayerName);
            final int newPlayerPort = pck.getPort();
            final InetAddress newPlayerAddress = pck.getAddress();
            if (players.containsKey(newPlayerName)) {
                final String errorMessage = "Sorry, name is already in use, choose a different one";
                final DatagramPacket response = new DatagramPacket(
                        errorMessage.getBytes(),
                        errorMessage.getBytes().length,
                        newPlayerAddress,
                        newPlayerPort
                );
                sck.send(response);
            } else {
                final Player newPlayer = new Player(newPlayerName, newPlayerAddress, newPlayerPort);
                players.put(newPlayerName, newPlayer);
            }
        }
        return players.values();
    }

    private static void logHostAddress() {
        try {
            final String hostAddress = InetAddress.getLocalHost().getHostAddress();
            log.info("Host address {}", hostAddress);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Cannot obtain host address", e);
        }
    }
}
