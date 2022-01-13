package org.crucial.executor.k8s;

import org.crucial.executor.ByteMarshaller;
import org.crucial.executor.ServerlessExecutorService;

import java.io.IOException;
import java.util.Base64;
import java.util.Scanner;

public class KubernetesExecutorService extends ServerlessExecutorService {

    private KubernetesInvoker invoker;
    private KubernetesService service;
    private final String jobName;
    private final String image;


    public KubernetesExecutorService(String jobName, String image) {
        this.jobName = jobName;
        this.image = image;
        init();
    }
    private void init() {
        invoker = new KubernetesInvoker(this.jobName, this.image);
        service = new KubernetesService();
        //service.start();
    }

    @Override
    protected byte[] invokeExternal(byte[] input)  {


        System.out.println(this.printPrefix() + "Calling K8s Job.");

        String response = invoker.invoke(input);
        assert response != null;
        System.out.println(this.printPrefix() + "K8s call completed.");

        try {
            byte[] ret = ByteMarshaller.toBytes(response);
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void closeInvoker() {

    }

}
