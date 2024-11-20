package org.dynamics.reader;

import org.dynamics.model.Person;

import java.io.IOException;
import java.util.List;

public interface Reader<T> {
    List<T> read() throws IOException;
}
