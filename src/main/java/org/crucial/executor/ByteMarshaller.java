package org.crucial.executor;

import java.io.*;
import java.util.Base64;

public class ByteMarshaller {
    public static byte[] toBytes(Object o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        return baos.toByteArray();
    }

    public static <T> T fromBytes(byte[] input) throws IOException, ClassNotFoundException {
        return (T) new ObjectInputStream(new ByteArrayInputStream(input)).readObject();
    }

}
