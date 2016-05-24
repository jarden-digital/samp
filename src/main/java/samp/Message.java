package samp;

import java.util.Map;
import java.util.HashMap;

class Message implements MessageI {

    private final String action;
    private final Map<String, String> headers;
    private final byte[] body;

    public Message(String action, Map<String, String> headers, byte[] body) {
        this.action = action;
        this.headers = headers;
        this.body = body;
    }

    public String action() {
        return this.action;
    }

    public Map<String, String> headers() {
        return this.headers;
    }

    public byte[] body() {
        return this.body;
    }

}
