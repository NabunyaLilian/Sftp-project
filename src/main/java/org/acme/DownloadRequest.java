package org.acme;

public class DownloadRequest {
    public String privateKey;
    public String user;
    public String host;
    public int port;
    public String localDir;    // e.g. "/Users/lilian/Downloads"
    public String remotePath;
    public String knownHosts;

    @Override
    public String toString() {
        return "DownloadRequest{" +
                "privateKey='" + privateKey + '\'' +
                ", user='" + user + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", localDir='" + localDir + '\'' +
                ", remotePath='" + remotePath + '\'' +
                ", knownHosts='" + knownHosts + '\'' +
                '}';
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getKnownHosts() {
        return knownHosts;
    }

    public void setKnownHosts(String knownHosts) {
        this.knownHosts = knownHosts;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getLocalDir() {
        return localDir;
    }

    public void setLocalDir(String localDir) {
        this.localDir = localDir;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }
}
