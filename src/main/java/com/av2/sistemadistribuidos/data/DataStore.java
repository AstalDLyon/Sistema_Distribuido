package com.av2.sistemadistribuidos.data;

public class DataStore {
    private final HostsDAO hostsDAO;
    private final UsersDAO usersDAO;

    private DataStore(Builder builder) {
        this.hostsDAO = builder.hostsDAO;
        this.usersDAO = builder.usersDAO;
    }

    public HostsDAO hosts() {
        return hostsDAO;
    }

    public UsersDAO users() {
        return usersDAO;
    }

    public static class Builder {
        private HostsDAO hostsDAO;
        private UsersDAO usersDAO;

        public Builder withHostsDAO(String path) {
            this.hostsDAO = new HostsDAO(path);
            return this;
        }

        public Builder withUsersDAO(String path) {
            this.usersDAO = new UsersDAO(path);
            return this;
        }

        public DataStore build() {
            if (hostsDAO == null) hostsDAO = new HostsDAO("data/hosts.properties");
            if (usersDAO == null) usersDAO = new UsersDAO("data/users.properties");
            return new DataStore(this);
        }
    }
}