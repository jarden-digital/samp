package nz.co.fnzc.samp;

import java.io.IOException;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

public class SampTest {

    static final String sampleMessage1 = "SAMP/1.0 EVENT /make/lunch\nFrom: bob@someplace.com\nCorrelation-Id: a54d3200-d8c5-4ef2-8514-0e3f9e0533e9\nDate: 2016-05-12T04:03:39.668Z\nContent-Type: application/json\nTrace: api-gateway...menud...order-placement\n\n{\"orderNumber\":\"542523\",\"placed\":true,\"product\":\"burger\",\"quantity\":1}";

    static final String sampleMessage2 = "SAMP/1.0 EVENT /make/lunch\n\n";

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
    public void testFormatHeadersInAlphaOrder() {
        Map<String, String> headers = new HashMap<>();
        headers.put(Samp.From, "bob");
        headers.put(Samp.CorrelationId, "12345678");
        headers.put(Samp.Date, "1");
        String x = new String(Samp.format(Samp.Event,
                                   Optional.of(Samp.Ok),
                                   "/foo/bar",
                                   headers,
                                   Optional.of("baz".getBytes())));
        assertEquals("SAMP/1.0 EVENT/Ok /foo/bar\nCorrelation-Id: 12345678\nDate: 1\nFrom: bob\n\nbaz", x);
    }

    @Test
    public void testFormatParse() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(Samp.From, "bob");
        headers.put(Samp.CorrelationId, "a54d3200-d8c5-4ef2-8514-0e3f9e0533e8");
        String s = new String(Samp.format(Samp.Event,
                                          Optional.of(Samp.Ok),
                                          "/foo/bar",
                                          headers,
                                          Optional.of("baz".getBytes())));
        MessageI x = Samp.parse(s);
        assertEquals("1.0", x.version());
        assertEquals("EVENT", x.kind());
        assertEquals("Ok", x.status().get());
        assertEquals("/foo/bar", x.action());
        assertEquals(3, x.headers().size());
        assertEquals("a54d3200-d8c5-4ef2-8514-0e3f9e0533e8", headers.get(Samp.CorrelationId));
        assertEquals("bob", headers.get(Samp.From));
        // TODO the body is changed to have \n... whereas should be taken raw from input
        assertEquals("baz\n", new String(x.body().get()));
    }

    @Test
    public void testResponseBuilder() throws Exception {
        final MessageI m = Samp.parse(sampleMessage1);
        final MessageBuilder builder = Samp.response(m);
        builder.withHeader(Samp.Date, "1");
        final String x = new String(builder.format());
        assertEquals("SAMP/1.0 EVENT /make/lunch\nCorrelation-Id: a54d3200-d8c5-4ef2-8514-0e3f9e0533e9\nDate: 1\nFrom: bob@someplace.com\nTrace: api-gateway...menud...order-placement...?\n\n", x);
    }

    @Test
    public void testResponseBuilderWithTraceAppend() throws Exception {
        final MessageI m = Samp.parse(sampleMessage1);
        final MessageBuilder builder = Samp.response(m, "delivery");
        builder.withHeader(Samp.Date, "1");
        final String x = new String(builder.format());
        assertEquals("SAMP/1.0 EVENT /make/lunch\nCorrelation-Id: a54d3200-d8c5-4ef2-8514-0e3f9e0533e9\nDate: 1\nFrom: bob@someplace.com\nTrace: api-gateway...menud...order-placement...delivery\n\n", x);
    }

    @Test
    public void testResponseBuilderWithTraceButNonePrior() throws Exception {
        final MessageI m = Samp.parse(sampleMessage2);
        final MessageBuilder builder = Samp.response(m, "end");
        builder.withHeader(Samp.Date, "1");
        final String x = new String(builder.format());
        assertEquals("SAMP/1.0 EVENT /make/lunch\nDate: 1\nTrace: end\n\n", x);
    }

    @Test
    public void testBuilderNoBody() {
        final MessageBuilder x =
            Samp.message()
            .event()
            .withHeader(Samp.Date, "1")
            .withAction("/a/b/c/d");
        assertEquals("SAMP/1.0 EVENT /a/b/c/d\nDate: 1\n\n",
                     new String(x.format()));
    }

    @Test
    public void testBuilderHeaderNoBody() {
        final MessageBuilder x =
            Samp.message()
            .withAction("/a/b/c/d")
            .withHeader(Samp.Date, "1")
            .withHeader(Samp.Trace, Samp.formatTracePaths("a", "b", "c", "z"));
        assertEquals("SAMP/1.0 EVENT /a/b/c/d\nDate: 1\nTrace: a...b...c...z\n\n",
                     new String(x.format()));
    }

    @Test
    public void testBuilder() {
        final MessageBuilder x =
            Samp.message()
            .withAction("/a/b/c/d")
            .withHeader(Samp.Date, "1")
            .withBody("{\"foo\":9}");
        assertEquals("SAMP/1.0 EVENT /a/b/c/d\nDate: 1\n\n{\"foo\":9}",
                     new String(x.format()));
    }

    @Test
    public void testBuilderBadRequest() {
        final MessageBuilder x =
            Samp.message()
            .failure(Samp.BadRequest)
            .withAction("/foo/bar")
            .withHeader(Samp.Date, "1")
            .withBody("baz");
        assertEquals("SAMP/1.0 FAILURE/Bad-Request /foo/bar\nDate: 1\n\nbaz",
                     new String(x.format()));
    }

    @Test
    public void testBuilderHeadersInAlphaOrder() {
        Map<String, String> headers = new HashMap<>();
        headers.put(Samp.From, "bob");
        headers.put(Samp.CorrelationId, "12345678");
        headers.put(Samp.Date, "1");
        final MessageBuilder x =
            Samp.message()
            .event(Samp.Ok)
            .withAction("/foo/bar")
            .withHeaders(headers)
            .withBody("baz");
        assertEquals("SAMP/1.0 EVENT/Ok /foo/bar\nCorrelation-Id: 12345678\nDate: 1\nFrom: bob\n\nbaz", new String(x.format()));
    }

    @Test
    public void testBuilderParse() throws IOException {
        final MessageBuilder m =
            Samp.message()
            .event(Samp.Ok)
            .withAction("/foo/bar")
            .withHeader(Samp.From, "bob")
            .withHeader(Samp.CorrelationId, "a54d3200-d8c5-4ef2-8514-0e3f9e0533e8")
            .withBody("baz");
        String s = new String(m.format());
        MessageI x = Samp.parse(s);
        assertEquals("1.0", x.version());
        assertEquals("EVENT", x.kind());
        assertEquals("Ok", x.status().get());
        assertEquals("/foo/bar", x.action());
        assertEquals(3, x.headers().size());
        assertEquals("a54d3200-d8c5-4ef2-8514-0e3f9e0533e8", x.headers().get(Samp.CorrelationId));
        assertEquals("bob", x.headers().get(Samp.From));
        // TODO the body is changed to have \n... whereas should be taken raw from input
        assertEquals("baz\n", new String(x.body().get()));
    }

    @Test
    public void testDefaultDateHeader() throws IOException {
        final MessageBuilder m =
            Samp.message()
            .event(Samp.Ok)
            .withAction("/foo/bar");
        String s = new String(m.format());
        MessageI x = Samp.parse(s);
        assertTrue(x.headers().containsKey(Samp.Date));
    }

    @Test
    public void testDateFormatters() throws java.text.ParseException {
        Date date = new Date(1468981423459L);
        String isoDate = "2016-07-20T14:23:43.459+1200";
        Date parsedDate = Samp.parseDate(isoDate);
        String formattedIso = Samp.formatDate(date);
        assertEquals(date, parsedDate);
        assertEquals(isoDate, formattedIso);
    }
}
