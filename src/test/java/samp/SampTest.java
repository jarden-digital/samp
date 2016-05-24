package samp;

import java.io.IOException;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class SampTest {

    @Test
    public void testEventNoStatus() throws IOException {
        final String message = "SAMP/1.0 EVENT /continent/1234/nuke\n\nHmmm";
        final MessageI x = Samp.parse(message);
        assertEquals("1.0", x.version());
        assertEquals("EVENT", x.kind());
        assertEquals(false, x.status().isPresent());
        assertEquals("/continent/1234/nuke", x.action());
    }

    @Test
    public void testEventWithStatus() throws IOException {
        final String message = "SAMP/1.0 EVENT/Ok /continent/1234/nuke\n\nHmmm";
        final MessageI x = Samp.parse(message);
        assertEquals("1.0", x.version());
        assertEquals("EVENT", x.kind());
        assertEquals("Ok", x.status().get());
        assertEquals("/continent/1234/nuke", x.action());
    }

    @Test
    public void testFailureNoStatus() throws IOException {
        final String message = "SAMP/1.0 FAILURE /continent/1234/nuke\n\nHmmm";
        final MessageI x = Samp.parse(message);
        assertEquals("1.0", x.version());
        assertEquals("FAILURE", x.kind());
        assertEquals(false, x.status().isPresent());
        assertEquals("/continent/1234/nuke", x.action());
    }

    @Test
    public void testFailureWithStatus() throws IOException {
        final String message = "SAMP/1.0 FAILURE/Bad-Request /continent/1234/nuke\n\nHmmm";
        final MessageI x = Samp.parse(message);
        assertEquals("1.0", x.version());
        assertEquals("FAILURE", x.kind());
        assertEquals("Bad-Request", x.status().get());
        assertEquals("/continent/1234/nuke", x.action());
    }

    @Test
    public void testNoHeader() throws IOException {
        final String message = "SAMP/1.0 EVENT /order/532534/items\n\nbananas";
        final MessageI x = Samp.parse(message);
        assertEquals("/order/532534/items", x.action());
        Map<String, String> headers = x.headers();
        assertEquals(0, headers.size());
        assertEquals("bananas\n", new String(x.body().get()));
    }

    @Test
    public void testSingleHeader() throws IOException {
        final String message = "SAMP/1.0 EVENT /order/532534/items\nContent-Type: text/csv\n\n\"foo\",234,1,\"Wholesale\",88.99,,,,2,";
        final MessageI x = Samp.parse(message);
        assertEquals("/order/532534/items", Samp.parse(message).action());
        Map<String, String> headers = x.headers();
        assertEquals("text/csv", headers.get(Samp.ContentType));
        assertEquals(null, headers.get(Samp.CorrelationId));
        assertEquals(1, headers.size());
        assertEquals("\"foo\",234,1,\"Wholesale\",88.99,,,,2,\n", new String(x.body().get()));
    }

    @Test
    public void testAllHeaders() throws IOException {
        final String message = "SAMP/1.0 EVENT /make/lunch\nFrom: bob@someplace.com\nCorrelation-Id: a54d3200-d8c5-4ef2-8514-0e3f9e0533e9\nDate: 2016-05-12T04:03:39.668Z\nContent-Type: application/json\nTrace: api-gateway...menud...order-placement\n\n{\"orderNumber\":\"542523\",\"placed\":true,\"product\":\"burger\",\"quantity\":1}";
        final MessageI x = Samp.parse(message);
        assertEquals("/make/lunch", x.action());
        Map<String, String> headers = x.headers();
        assertEquals("application/json", headers.get(Samp.ContentType));
        assertEquals("a54d3200-d8c5-4ef2-8514-0e3f9e0533e9", headers.get(Samp.CorrelationId));
        assertEquals(5, headers.size());
        assertEquals("{\"orderNumber\":\"542523\",\"placed\":true,\"product\":\"burger\",\"quantity\":1}\n",
                     new String(x.body().get()));
    }

    @Test
    public void testNoTrace() throws IOException {
        final String message = "SAMP/1.0 EVENT /make/lunch\nFrom: bob@someplace.com\nCorrelation-Id: a54d3200-d8c5-4ef2-8514-0e3f9e0533e9\nDate: 2016-05-12T04:03:39.668Z\nContent-Type: application/json\n\n{\"orderNumber\":\"542523\",\"placed\":true,\"product\":\"burger\",\"quantity\":1}";
        final MessageI x = Samp.parse(message);
        assertEquals(Collections.emptyList(), Samp.tracePaths(x));
    }

    @Test
    public void testTrace() throws IOException {
        final String message = "SAMP/1.0 EVENT /make/lunch\nFrom: bob@someplace.com\nCorrelation-Id: a54d3200-d8c5-4ef2-8514-0e3f9e0533e9\nDate: 2016-05-12T04:03:39.668Z\nContent-Type: application/json\nTrace: api-gateway...menud...order-placement\n\n{\"orderNumber\":\"542523\",\"placed\":true,\"product\":\"burger\",\"quantity\":1}";
        final MessageI x = Samp.parse(message);
        assertEquals(Arrays.asList("api-gateway", "menud", "order-placement"), Samp.tracePaths(x));
    }

    @Test
    public void testMultilineBody() throws IOException {
        final String message = "SAMP/1.0 FAILURE /whinge\n\nTHIS\nIS\nCRAP\n";
        final MessageI x = Samp.parse(message);
        assertEquals("/whinge", x.action());
        Map<String, String> headers = x.headers();
        assertEquals(0, headers.size());
        assertEquals("THIS\nIS\nCRAP\n", new String(x.body().get()));
    }

    @Test
    public void testFormatNoBody() {
        assertEquals("SAMP/1.0 EVENT /a/b/c/d\n\n",
                     new String(Samp.format("EVENT", Optional.empty(), "/a/b/c/d", Collections.emptyMap(), Optional.empty())));
    }

    @Test
    public void testFormat() {
        assertEquals("SAMP/1.0 EVENT /a/b/c/d\n\n{\"foo\":9}",
                     new String(Samp.format("EVENT", Optional.empty(), "/a/b/c/d", Collections.emptyMap(), Optional.of("{\"foo\":9}".getBytes()))));
    }

}
