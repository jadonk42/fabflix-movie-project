package edu.uci.ics.fabflixmobile.data;

public class BackendServer {
    private final String host;
    private final String port;
    private final String domain;
    private final String baseURL;

    // Change host to your own AWS machine public IP address
    // DON'T MODIFY THE OTHER VALUES
    public BackendServer() {
        this.host = "52.12.152.71";
        this.port = "8080";
        this.domain = "cs-122b-group-37";
        this.baseURL = "http://" + host + ":" + port + "/" + domain;
    }

    public String getBaseURL() { return baseURL; }
}
