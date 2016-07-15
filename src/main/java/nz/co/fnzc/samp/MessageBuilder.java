package nz.co.fnzc.samp;

import java.util.*;
import java.util.stream.Collectors;

class MessageBuilder {

    private String kind = "EVENT";
    private Optional<String> status = Optional.empty();
    private String action = "";
    private Map<String, String> headers = new HashMap<>();
    private Optional<byte[]> body = Optional.empty();

    public MessageBuilder event() {
        this.kind = Samp.Event;
        this.status = Optional.empty();
        return this;
    }

    public MessageBuilder event(String eventStatus) {
        this.kind = Samp.Event;
        this.status = Optional.of(eventStatus);
        return this;
    }

    public MessageBuilder failure() {
        this.kind = Samp.Failure;
        this.status = Optional.empty();
        return this;
    }

    public MessageBuilder failure(String failureStatus) {
        this.kind = Samp.Failure;
        this.status = Optional.of(failureStatus);
        return this;
    }

    public MessageBuilder withAction(String action) {
        this.action = action;
        return this;
    }

    public MessageBuilder withHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public MessageBuilder withHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    public MessageBuilder withEmptyBody() {
        this.body = Optional.empty();
        return this;
    }

    public MessageBuilder withBody(byte[] body) {
        this.body = Optional.of(body);
        return this;
    }

    public MessageBuilder withBody(String body) {
        this.body = Optional.of(body.getBytes());
        return this;
    }

    public byte[] format() {
        // TODO should use byte buffer here instead of string builder...
        final StringBuilder sb = new StringBuilder("SAMP/1.0 ");
        sb.append(kind);
        if (status.isPresent()) {
            sb.append("/");
            sb.append(status.get());
        }
        sb.append(" ");
        sb.append(action);
        sb.append("\n");
        new TreeMap(headers).forEach((k, v) -> {
                sb.append(k);
                sb.append(": ");
                sb.append(v);
                sb.append("\n");
        });
        sb.append("\n");
        if (body.isPresent()) {
            sb.append(new String(body.get()));
        }

        return sb.toString().getBytes();
    }

}
