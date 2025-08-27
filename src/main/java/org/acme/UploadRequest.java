package org.acme;

public class UploadRequest {
    String host;
    String user;
    String privateKey;
    String remotePath;
    String localFile;
    int port;
    String knownHosts;


    @Override
    public String toString() {
        return "UploadRequest{" +
                "host='" + host + '\'' +
                ", user='" + user + '\'' +
                ", privateKey='" + privateKey + '\'' +
                ", remotePath='" + remotePath + '\'' +
                ", localFile='" + localFile + '\'' +
                ", port=" + port +
                ", knownHosts='" + knownHosts + '\'' +
                '}';
    }

    public String getKnownHosts() {
        return knownHosts;
    }

    public void setKnownHosts(String knownHosts) {
        this.knownHosts = knownHosts;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getLocalFile() {
        return localFile;
    }

    public void setLocalFile(String localFile) {
        this.localFile = localFile;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
