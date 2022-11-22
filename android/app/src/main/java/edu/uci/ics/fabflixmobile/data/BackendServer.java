package edu.uci.ics.fabflixmobile.data;

public class BackendServer {
    private final String host;
    private final String port;
    private final String domain;
    private final String baseURL;

    // Change host to your own AWS machine public IP address
    // DON'T MODIFY THE OTHER VALUES
    public BackendServer() {
        this.host = "54.201.228.84";
        this.port = "8443";
        this.domain = "cs-122b-group-37";
        this.baseURL = "https://" + host + ":" + port + "/" + domain;
    }

    public String getBaseURL() { return baseURL; }
}
