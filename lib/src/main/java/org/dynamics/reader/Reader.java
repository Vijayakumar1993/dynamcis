package org.dynamics.reader;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

public interface Reader<T> {
    Logger logger = LogManager.getLogger(Reader.class);
    List<T> read() throws IOException;
}
