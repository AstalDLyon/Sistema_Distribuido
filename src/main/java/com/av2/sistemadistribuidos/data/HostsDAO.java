package com.av2.sistemadistribuidos.data;

import com.av2.sistemadistribuidos.base.DAO;

public class HostsDAO extends DAO {
    public HostsDAO(String path) {
        super(path, "DNS Hosts");
    }

    public boolean registerHost(String hostname, String ip) {
        return append(hostname, ip);
    }

    public String lookupHost(String hostname) {
        return get(hostname);
    }
}