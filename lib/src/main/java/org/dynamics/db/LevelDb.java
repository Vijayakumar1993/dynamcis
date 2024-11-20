package org.dynamics.db;

import org.dynamics.model.Person;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import static org.iq80.leveldb.impl.Iq80DBFactory.*;

public class LevelDb implements Db<Person> {
    private Options options = new Options();
    private DB db ;
    public LevelDb() throws IOException {
        File file = new File("./db");
        if(!file.exists()) file.mkdir();
        options.createIfMissing(true);
        db = factory.open(file,options);
    }

    public <T> byte[] serialize(List<T> list) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(list);
        objectOutputStream.flush();
        objectOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public <T> List<T> deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        List<T> op = (List<T>) objectInputStream.readObject();
        return op;
    }

    @Override
    public void insert(String key, List<Person> list) throws IOException {
        db.put(key.getBytes(),serialize(list));
    }

    @Override
    public List<Person> find(String key) throws IOException, ClassNotFoundException {
        byte[] result = db.get(key.getBytes());
        return deserialize(result);
    }

    @Override
    public Vector<String> keys() {
        Vector<String> keys = new Vector<>();
        DBIterator dbIterator = db.iterator();
        dbIterator.seekToFirst();
        while(dbIterator.hasNext())
        {
            keys.add(asString(dbIterator.next().getKey()));
        }
        return keys;
    }
}
