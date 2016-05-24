package samp;

import java.util.*;

public interface MessageI {

    public String version();

    public String kind();

    public Optional<String> status();

    public String action();

    public Map<String, String> headers();

    public Optional<byte[]> body();

}
