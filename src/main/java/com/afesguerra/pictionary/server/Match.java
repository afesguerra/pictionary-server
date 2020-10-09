package com.afesguerra.pictionary.server;

import com.afesguerra.pictionary.server.model.Player;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Collection;
import java.util.Random;

@Log4j2
public class Match extends Thread {
    private static final String SEPARATOR = ";";
    private final Collection<Player> players;
    private final String palabra;

    private final DatagramSocket sck;

    public Match(Collection<Player> players, String palabra) {
        this.players = players;
        this.palabra = palabra;

        try {
            this.sck = new DatagramSocket();
        } catch (SocketException e) {
            final String message = String.format("Exception for %s", players);
            throw new UncheckedIOException(message, e);
        }
    }

    /**
     * Se encarga de recibir mensajes de los clientes
     */
    public void run() {

        final int random = new Random().nextInt(players.size());
        final Player drawer = players.stream()
                .skip(random)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find any players"));

        final String mensaje = String.format("%s;%s", drawer.getName(), palabra);
        sendMessage(mensaje);

        while (!isInterrupted()) {
            try {
                byte[] a = new byte[10000];
                final DatagramPacket pck = new DatagramPacket(a, a.length);
                sck.receive(pck);
                descifrar(pck);
            } catch (IOException e) {
                log.warn("Error receiving packet for {}", players, e);
            }
        }
    }

    /**
     * Se encarga de enviar los mensajes a todos los usuarios
     */
    private void sendMessage(final String message) {
        for (Player player : players) {
            final DatagramPacket packet = new DatagramPacket(
                    message.getBytes(),
                    message.getBytes().length,
                    player.getIp(),
                    player.getPuerto()
            );

            try {
                sck.send(packet);
            } catch (IOException e) {
                log.error("Error sending packet {}", packet, e);
            }
        }
    }

    /**
     * Se encarga de verificar si la palabra enviada por el cliente es la palabra correcta
     *
     * @return Si la palabra era o no correcta
     */
    public boolean verificarWord(final String message) {
        final String guessWord = message.split(SEPARATOR)[2];
        return palabra.equalsIgnoreCase(guessWord);
    }

    /**
     * Se encarga de interpretar los mensajes recibidos de parte de los clientes, y enviar la informacion pertinente a todos
     */
    public void descifrar(DatagramPacket pck) {
        final String message = new String(pck.getData(), 0, pck.getLength());
        log.trace("pck: {}", message);
        switch (Integer.parseInt(message.split(SEPARATOR)[0])) {
            case 1:
            case 4:
                // coordenadas
                sendMessage(message);
                break;
            case 2:
                // palabra
                sendMessage(message);
                if (verificarWord(message)) {
                    final String playerName = message.split(SEPARATOR)[1];
                    final String response = String.format("0; ;El jugador %s fue quien adivinó la palabra: %s", playerName, palabra);
                    sendMessage(response);
                    sck.close();
                    this.interrupt();
                }
                break;
            case 3:
                final String response = "3;;Error de conectividad, el concursante " + message.split(SEPARATOR)[1] + " se ha desconectado";
                sendMessage(response);
                interrupt();
                break;
            default:
                log.warn("Unexpected message {}", message);
                break;
        }
    }
}
