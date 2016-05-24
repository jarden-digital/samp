package samp;

import java.util.Map;

public interface MessageI {

    public String action();

    public Map<String, String> headers();

    public byte[] body();

}
