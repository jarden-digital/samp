package samp;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Samp {

    public static String ContentType = "Content-Type";
    public static String CorrelationId = "Correlation-Id";
    public static String Date = "Date";
    public static String From = "From";
    public static String Trace = "Trace";

    private static Pattern introPattern = Pattern.compile("SAMP\\/([0-9\\.]+)\\s+([a-zA-Z]+)((?:\\/[-\\w]+)?)\\s+(.*)");
    private static Pattern headerPattern = Pattern.compile("([^:]+):\\s+(.*)");

    public static MessageI parse(String message) throws IOException {
        return parse(new ByteArrayInputStream(message.getBytes()));
    }

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

    public static byte[] format(String kind, Optional<String> status, String action, Map<String, String> headers, Optional<byte[]> body) {
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

    public static List<String> tracePaths(MessageI message) {
        String trace = message.headers().get(Samp.Trace);
        if (trace != null) {
            return Arrays.asList(trace.split("\\.\\.\\."));
        } else {
            return Collections.emptyList();
        }
    }

}
