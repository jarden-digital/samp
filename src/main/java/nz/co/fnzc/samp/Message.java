package nz.co.fnzc.samp;

import java.util.*;

class Message implements MessageI {

    private final String version;
    private final String kind;
    private final Optional<String> status;
    private final String action;
    private final Map<String, String> headers;
    private final Optional<byte[]> body;

    public Message(String version,
                   String kind,
                   Optional<String> status,
                   String action,
                   Map<String, String> headers,
                   Optional<byte[]> body) {
        this.version = version;
        this.kind = kind;
        this.status = status;
        this.action = action;
        this.headers = headers;
        this.body = body;
    }

    public String version() {
        return this.version;
    }

    public String kind() {
        return this.kind;
    }

    public Optional<String> status() {
        return this.status;
    }

    public String action() {
        return this.action;
    }

    public Map<String, String> headers() {
        return this.headers;
    }

    public Optional<byte[]> body() {
        return this.body;
    }

}
