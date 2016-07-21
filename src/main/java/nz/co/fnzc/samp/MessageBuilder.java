package nz.co.fnzc.samp;

import java.util.*;
import java.util.stream.Collectors;

public class MessageBuilder {

    private final SampInstance samp;
    private String kind = "EVENT";
    private Optional<String> status = Optional.empty();
    private String action = "";
    private Map<String, String> headers = new HashMap<>();
    private Optional<byte[]> body = Optional.empty();

    public MessageBuilder(SampInstance samp) {
        this.samp = samp;
    }

    /** Set message kind to EVENT with no status */
    public MessageBuilder event() {
        this.kind = Samp.Event;
        this.status = Optional.empty();
        return this;
    }

    /** Set message kind to EVENT with given status */
    public MessageBuilder event(String eventStatus) {
        this.kind = Samp.Event;
        this.status = Optional.of(eventStatus);
        return this;
    }

    /** Set message kind to FAILURE with no status */
    public MessageBuilder failure() {
        this.kind = Samp.Failure;
        this.status = Optional.empty();
        return this;
    }

    /** Set message kind to FAILURE with given failure status */
    public MessageBuilder failure(String failureStatus) {
        this.kind = Samp.Failure;
        this.status = Optional.of(failureStatus);
        return this;
    }

    /** Set message action */
    public MessageBuilder withAction(String action) {
        this.action = action;
        return this;
    }

    /** Add message header key/value */
    public MessageBuilder withHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    /** Add all message headers in given map */
    public MessageBuilder withHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    /** Set empty body */
    public MessageBuilder withEmptyBody() {
        this.body = Optional.empty();
        return this;
    }

    /** Set body bytes */
    public MessageBuilder withBody(byte[] body) {
        this.body = Optional.of(body);
        return this;
    }

    /** Set body bytes from given String */
    public MessageBuilder withBody(String body) {
        this.body = Optional.of(body.getBytes());
        return this;
    }

    /** Format a SAMP message as bytes from this builder state */
    public byte[] format() {
        return samp.format(kind, status, action, headers, body);
    }

    /** Generate a SAMP message from this builder state */
    public MessageI message() {
        return new Message(samp.version, kind, status, action, headers, body);
    }

}
