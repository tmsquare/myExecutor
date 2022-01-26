package org.crucial.executor.k8s;

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.kubernetes.client.proto.V1Batch;
import org.crucial.executor.ByteMarshaller;
import org.crucial.executor.ServerlessExecutorService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;

public class KubernetesExecutorService extends ServerlessExecutorService {

    private KubernetesInvoker invoker;
    private final String jobName;
    private final String image;


    public KubernetesExecutorService(String jobName, String image) {
        this.jobName = jobName;
        this.image = image;
        init();
    }
    private void init() {
        invoker = new KubernetesInvoker(this.jobName, this.image);
    }

    @Override
    protected byte[] invokeExternal(byte[] input)  {

        System.out.println(this.printPrefix() + "Calling K8s Job.");
        String response = invoker.invoke(input, super.getListen());
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

    @Override
    public void deleteAllJobs() {

        try (KubernetesClient client = new DefaultKubernetesClient()) {
            String namespace = Optional.ofNullable(client.getNamespace()).orElse("default");

            // Get All jobs in Namespace
            List<Job> jobList = client.batch().v1().jobs().inNamespace(namespace).list().getItems();

            // Delete job
            client.batch().v1().jobs().inNamespace(namespace).delete(jobList);
        }
    }



}
