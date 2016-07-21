package nz.co.fnzc.samp;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

    private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
    private static final SampInstance defaultInstance = instance();

    /** Factory method for the SAMP instance for a (sub)system. */
    public static SampInstance instance() {
        return new SampInstance();
    }

    public static SampInstance defaultInstance() {
        return defaultInstance;
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

    /** Format Date to ISO 8601 string */
    public static String formatDate(Date date) {
        return dateFormatter.format(date);
    }

    /** Parse ISO 8601 to date */
    public static Date parseDate(String dateString) throws java.text.ParseException {
        return dateFormatter.parse(dateString);
    }
}
