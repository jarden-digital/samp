package nz.co.fnzc.samp;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.function.Supplier;

public class SampInstance {

    private static final Pattern introPattern = Pattern.compile("SAMP\\/([0-9\\.]+)\\s+([a-zA-Z]+)((?:\\/[-\\w]+)?)\\s+(.*)");
    private static final Pattern headerPattern = Pattern.compile("([^:]+):\\s+(.*)");

    public String version = "1.0";
    public String systemName = "?";
    public Supplier<Optional<String>> defaultCorrelationIdSupplier =
        () -> Optional.of(UUID.randomUUID().toString());
    public Supplier<Optional<String>> defaultDateSupplier =
        () -> Optional.of(Samp.formatDate(new Date()));

    public SampInstance() {
    }

    /** Parse a string to MessageI */
    public MessageI parse(String message) throws IOException {
        return parse(new ByteArrayInputStream(message.getBytes()));
    }

    /** Parse a byte stream to MessageI */
    public MessageI parse(ByteArrayInputStream is) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        final String intro = reader.readLine();
        final Matcher m = introPattern.matcher(intro);
        if (!m.matches()) throw new IOException("Invalid SAMP Frame");
        final String version = m.group(1);
        final String kind = m.group(2);
        final Optional<String> status = Optional.of(m.group(3)).filter(s -> s.length() > 1).map(s -> s.substring(1));
        final String action = m.group(4);

        Map<String, String> headers = new HashMap<>();
        String header = reader.readLine();
        while (header != null && header.length() > 0) {
            final Matcher hm = headerPattern.matcher(header);
            if (hm.matches()) {
                headers.put(hm.group(1), hm.group(2));
            }
            header = reader.readLine();
        }

        String line = reader.readLine();
        Optional<String> body = Optional.empty();
        while (line != null) {
            final String s = line + System.getProperty("line.separator");
            if (body.isPresent()) {
                body = body.map(b -> b + s);
            } else {
                body = Optional.of(s);
            }
            line = reader.readLine();
        }

        return new Message(version, kind, status, action, headers, body.map(s -> s.getBytes()));
    }

    /** Create a new MessageBuilder */
    public MessageBuilder message() {
        return new MessageBuilder(this);
    }

    /** Create a new MessageBuilder, copying the action and relevant headers of the source message */
    public MessageBuilder response(MessageI message) {
        return response(message, Optional.empty());
    }

    /** Create a new MessageBuilder, copying the action and relevant headers of the source message, and adding/appending the tracePath */
    public MessageBuilder response(MessageI message, String tracePath) {
        return response(message, Optional.of(tracePath));
    }

    private MessageBuilder response(MessageI message, Optional<String> tracePath) {
        final MessageBuilder builder = new MessageBuilder(this);
        builder.event();
        builder.withAction(message.action());

        final String correlationId = message.headers().get(Samp.CorrelationId);
        final String from = message.headers().get(Samp.From);
        final String trace = message.headers().get(Samp.Trace);

        if (correlationId != null) {
            builder.withHeader(Samp.CorrelationId, correlationId);
        }
        if (from != null) {
            builder.withHeader(Samp.From, from);
        }
        if (trace != null) {
            builder.withHeader(Samp.Trace, Samp.appendTracePath(trace, tracePath.orElse("?")));
        } else if (tracePath.isPresent()) {
            builder.withHeader(Samp.Trace, tracePath.get());
        }
        return builder;
    }

    /** Format a SAMP message as bytes from the given message */
    public byte[] format(MessageI message) {
        return format(message.kind(),
                      message.status(),
                      message.action(),
                      message.headers(),
                      message.body());
    }

    /** Format a SAMP message as bytes from the given parameters */
    public byte[] format(String kind, Optional<String> status, String action, Map<String, String> headers, Optional<byte[]> body) {
        // TODO should use byte buffer here instead of string builder...
        Map<String, String> sortedHeaders = new TreeMap<>(headers);
        Optional<String> defaultCorrelationId = defaultCorrelationIdSupplier.get();
        if (defaultCorrelationId.isPresent()) {
            sortedHeaders.putIfAbsent(Samp.CorrelationId, defaultCorrelationId.get());
        }
        Optional<String> defaultDate = defaultDateSupplier.get();
        if (defaultDate.isPresent()) {
            sortedHeaders.putIfAbsent(Samp.Date, defaultDate.get());
        }
        final StringBuilder sb = new StringBuilder("SAMP/");
        sb.append(version);
        sb.append(" ");
        sb.append(kind);
        if (status.isPresent()) {
            sb.append("/");
            sb.append(status.get());
        }
        sb.append(" ");
        sb.append(action);
        sb.append("\n");
        sortedHeaders.forEach((k, v) -> {
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
