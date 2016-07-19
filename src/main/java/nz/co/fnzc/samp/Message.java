package nz.co.fnzc.samp;

import java.util.*;
import java.util.stream.Collectors;

public class Message implements MessageI {

    protected String version = "1.0";
    protected String kind = "";
    protected Optional<String> status = Optional.empty();
    protected String action = "";
    protected Map<String, String> headers = new HashMap<String, String>();
    protected Optional<byte[]> body = Optional.empty();
    public Message(){}
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
    public void setStatus(Optional<String> status) {
		this.status = status;
	}

	public void setBody(Optional<byte[]> body) {
		this.body = body;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String correlationId() {
		return this.headers.get(Samp.CorrelationId);
	}
  @Override
	public String toString() {
    String correlationId = this.headers.get(Samp.CorrelationId);

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
    return sb.toString();
	}

}
