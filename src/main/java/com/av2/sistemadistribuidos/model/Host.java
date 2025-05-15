package com.av2.sistemadistribuidos.model;

import com.av2.sistemadistribuidos.data.HostsDAO;

public class Host {
    String hostname;
    String ip;
    private final HostsDAO hostsDAO;

    public Host(String hostnamne, String ip, HostsDAO hostsDAO){
        this.hostname = hostnamne;
        this.ip = ip;
        this.hostsDAO = hostsDAO;
    }

    public boolean registerHost(String hostname, String ip) {
        return hostsDAO.append(hostname, ip);
    }

    public String lookupHost(String hostname) {
        return hostsDAO.get(hostname);
    }
}
