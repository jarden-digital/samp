package nz.co.fnzc.samp;

import java.util.*;
import java.util.stream.Collectors;

public final class Message implements MessageI {

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
        this.headers = Collections.unmodifiableMap(headers);
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

    @Override
	public String toString() {
        final StringBuilder sb = new StringBuilder("SAMP/" + version);
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
        return sb.toString();
	}

}
