package nz.co.fnzc.samp;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.*;

public class Samp {

    // Kind options:
    public static final String Event = "EVENT";
    public static final String Failure = "FAILURE";

    // Event status options:
    public static final String Ok = "Ok";
    public static final String Accepted = "Accepted";
    public static final String NoContent = "No-Content";

    // Failure status options:
    public static final String BadRequest = "Bad-Request";
    public static final String InternalError = "Internal-Error";
    public static final String NotFound = "Not-Found";
    public static final String Forbidded = "Forbidded";
    public static final String Unauthorized = "Unauthorized";
    public static final String Timeout = "Timeout";
    // Header options:
    public static final String ContentType = "Content-Type";
    public static final String CorrelationId = "Correlation-Id";
    public static final String Date = "Date";
    public static final String From = "From";
    public static final String Trace = "Trace";
    public static final String Payload = "Payload";

    private static final Pattern introPattern = Pattern.compile("SAMP\\/([0-9\\.]+)\\s+([a-zA-Z]+)((?:\\/[-\\w]+)?)\\s+(.*)");
    private static final Pattern headerPattern = Pattern.compile("([^:]+):\\s+(.*)");

    /** Parse a string to MessageI */
    public static MessageI parse(String message) throws IOException {
        return parse(new ByteArrayInputStream(message.getBytes()));
    }

    /** Parse a byte stream to MessageI */
    public static MessageI parse(ByteArrayInputStream is) throws IOException {
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
    public static MessageBuilder message() {
        return new MessageBuilder();
    }

    /** Create a new MessageBuilder, copying the action and relevant headers of the source message */
    public static MessageBuilder response(MessageI message) {
        return response(message, Optional.empty());
    }

    /** Create a new MessageBuilder, copying the action and relevant headers of the source message, and adding/appending the tracePath */
    public static MessageBuilder response(MessageI message, String tracePath) {
        return response(message, Optional.of(tracePath));
    }

    private static MessageBuilder response(MessageI message, Optional<String> tracePath) {
        final MessageBuilder builder = new MessageBuilder();
        builder.event();
        builder.withAction(message.action());
        final List<String> copiedHeaders = Arrays.asList(Samp.CorrelationId, Samp.From, Samp.Trace);

        final String correlationId = message.headers().get(Samp.CorrelationId);
        final String from = message.headers().get(Samp.From);
        final String trace = message.headers().get(Samp.Trace);
        final String payload = message.headers().get(Samp.Payload);
        final String contentType = message.headers().get(Samp.ContentType);
        final String date = message.headers().get(Samp.Date);

        if (correlationId != null) {
            builder.withHeader(Samp.CorrelationId, correlationId);
        }
        if (from != null) {
            builder.withHeader(Samp.From, from);
        }
        if (trace != null) {
            builder.withHeader(Samp.Trace, appendTracePath(trace, tracePath.orElse("?")));
        } else if (tracePath.isPresent()) {
            builder.withHeader(Samp.Trace, tracePath.get());
        }
        if(payload != null) {
          builder.withHeader(Samp.Payload, payload);
        }
        if(contentType != null) {
          builder.withHeader(Samp.ContentType, contentType);
        }
        if(date != null) {
          builder.withHeader(Samp.Date, date);
        }
        return builder;
    }

    /** @deprecated Please use message or response instead */
    public static byte[] format(String kind, Optional<String> status, String action, Map<String, String> headers, Optional<byte[]> body) {
        final MessageBuilder builder = message();
        if (kind == Samp.Failure && status.isPresent()) {
            builder.failure(status.get());
        } else if (kind == Samp.Failure) {
            builder.failure();
        } else if (kind == Samp.Event && status.isPresent()) {
            builder.event(status.get());
        } else {
            builder.event();
        }
        builder.withAction(action);
        headers.forEach( (k,v) -> builder.withHeader(k, v) );
        if (body.isPresent()) {
            builder.withBody(body.get());
        } else {
            builder.withEmptyBody();
        }
        return builder.format();
    }

    /** Correctly format tracePaths for the Trace header value */
    public static String formatTracePaths(String... tracePaths) {
        return Arrays.stream(tracePaths)
            .collect(Collectors.joining("..."));
    }

    /** Correctly format tracePaths for the Trace header value */
    public static String appendTracePath(String tracePaths, String tracePath) {
        return tracePaths + "..." + tracePath;
    }

    /** Parse the trace path elements in the Trace header value, if present */
    public static List<String> tracePaths(MessageI message) {
        String trace = message.headers().get(Samp.Trace);
        if (trace != null) {
            return Arrays.asList(trace.split("\\.\\.\\."));
        } else {
            return Collections.emptyList();
        }
    }

}
