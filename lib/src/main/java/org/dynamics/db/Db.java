package org.dynamics.db;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

public interface Db {
    <T> void  insert(String key, List<T> list) throws IOException;
    <T> void  insert(String key, T list) throws IOException;
    <T> List<T> find(String key) throws IOException, ClassNotFoundException;

    <T> T findObject(String key) throws IOException, ClassNotFoundException;

    void delete(String key);
    Vector<String> keys();

    Vector<String> keyFilterBy(String startsWith);

    Vector<String> keyFilterByNot(String startsWith);
}
