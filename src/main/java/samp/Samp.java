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

    private static Pattern introPattern = Pattern.compile("SAMP\\/([0-9\\.]+)\\s+([a-zA-Z]+)\\s+(.*)");
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
        final String type = m.group(2);
        final String action = m.group(3);

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
        String body = "";
        while (line != null) {
            body += line + System.getProperty("line.separator");
            line = reader.readLine();
        }

        return new Message(action, headers, body.getBytes());
    }

}
