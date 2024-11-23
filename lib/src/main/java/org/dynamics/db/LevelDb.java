package org.dynamics.db;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;

import java.io.*;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

public class LevelDb implements Db {
    private Options options = new Options();
    private DB db ;
    public LevelDb() throws IOException {
        File file = new File("./db");
        if(!file.exists()) file.mkdir();
        options.createIfMissing(true);
        db = factory.open(file,options);
    }
    public <T> byte[] serialize(T list) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(list);
        objectOutputStream.flush();
        objectOutputStream.close();
        return byteArrayOutputStream.toByteArray();
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
    public <T> T deserializeObject(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        T op = (T) objectInputStream.readObject();
        return op;
    }

    @Override
    public <T> void insert(String key, List<T> list) throws IOException {
        db.put(key.getBytes(),serialize(list));
    }

    @Override
    public <T> void insert(String key, T list) throws IOException {
        db.put(key.getBytes(),serialize(list));
    }

    @Override
    public <T> List<T> find(String key) throws IOException, ClassNotFoundException {
        byte[] result = db.get(key.getBytes());
        return deserialize(result);
    }

    @Override
    public <T> T findObject(String key) throws IOException, ClassNotFoundException {
        byte[] result = db.get(key.getBytes());
        return deserializeObject(result);
    }

    @Override
    public void delete(String key) {
         db.delete(key.getBytes());
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

    @Override
    public Vector<String> keyFilterBy(String startsWith){
        return new Vector<>(this.keys().stream().filter(a->a.startsWith(startsWith)).collect(Collectors.toList()));
    }
    @Override
    public Vector<String> keyFilterByNot(String startsWith){
        return new Vector<>(this.keys().stream().filter(a->!a.startsWith(startsWith)).collect(Collectors.toList()));
    }
}
