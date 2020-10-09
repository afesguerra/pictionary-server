package com.afesguerra.pictionary.server.model;

import lombok.Data;

import java.net.InetAddress;

@Data
public class Player {
    private final String name;
    private final InetAddress ip;
    private final int puerto;
}
