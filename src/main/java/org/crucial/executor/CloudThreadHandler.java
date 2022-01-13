package org.crucial.executor;


import java.io.*;

import java.util.Base64;
import java.util.concurrent.Callable;


public class CloudThreadHandler {

    protected byte[] handle(byte[] input) {
        Object result = null;

        try {
            ThreadCall call = ByteMarshaller.fromBytes(input);
            Callable c = call.getTarget();
            result = c.call();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            result = e;
        }
        try {
            byte[] ret = ByteMarshaller.toBytes(result);
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
