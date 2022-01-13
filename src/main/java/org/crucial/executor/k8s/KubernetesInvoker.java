package org.crucial.executor.k8s;

import java.util.Random;
import java.util.concurrent.TimeUnit;



import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.crucial.executor.Json;

public class KubernetesInvoker {

    private String jobName;
    private final String image;

    public KubernetesInvoker(String jobName, String image) {
        this.jobName = jobName;
        this.image = image;
    }

    public String invoke(byte[] input) {

        String joblog = null;
        String myName = jobName + "-" + Thread.currentThread().getName();

        String getMyIP = "ifconfig | grep 'inet' | grep -Fv 127.0.0.1 | awk '{print $2}' && nc -lkv 4444 > file1.txt";
        String sendData = "echo \"Hello, this is a file\" > file2.txt && nc 10.88.0.107 4444 < file2.txt";
        String portScan = "var=$(ifconfig | grep 'inet' | grep -Fv 127.0.0.1 | awk '{print $2}') && nc -z -n -v $var 8079-8084";
        String mainClass = "org.crucial.executor.k8s.KubernetesHandler";
        String libs = "/usr/local/executor.jar:/usr/local/executor-tests.jar:/usr/local/lib/*:.";


        // use the Base64 class to encode
        String command = Json.toJson(input);


        ConfigBuilder configBuilder = new ConfigBuilder();
        try (KubernetesClient client = new DefaultKubernetesClient(configBuilder.build())) {
            final String namespace = "default";
            final Job job = new JobBuilder()
                    .withApiVersion("batch/v1")
                    .withNewMetadata()
                    .withName(myName)
                    .addToLabels("name", myName)
                    .endMetadata()
                    .withNewSpec()
                    .withNewTemplate()
                    .withNewMetadata()
                    .addToLabels("app", "MyApp")
                    .endMetadata()
                    .withNewSpec()
                    .addNewContainer()
                    .withName(myName)
                    .withImage(image)
                    .withArgs("java","-classpath", libs, mainClass, command)
                    //.withCommand("sh", "-c" , portScan)
                    //.withCommand("sh", "-c", getMyIP)
                    //.withCommand("sh", "-c", sendData)
                    .endContainer()
                    .withRestartPolicy("Never")
                    .endSpec()
                    .endTemplate()
                    .endSpec()
                    .build();

            System.out.println("Creating job " + myName);
            client.batch().v1().jobs().inNamespace(namespace).createOrReplace(job);

            // Get All pods created by the job
            PodList podList = client.pods().inNamespace(namespace).withLabel("job-name", job.getMetadata().getName()).list();

            // Wait for pod to complete
            client.pods().inNamespace(namespace).withName(podList.getItems().get(0).getMetadata().getName())
                    .waitUntilCondition(pod -> pod.getStatus().getPhase().equals("Succeeded"), 1, TimeUnit.MINUTES);

            // Print Job's log
            joblog = client.pods().inNamespace(namespace).withName(podList.getItems().get(0).getMetadata().getName()).getLog();

            // Delete job
            client.batch().v1().jobs().inNamespace(namespace).delete(job);

        } catch (KubernetesClientException e) {
            System.out.println("Unable to create job");
        }

        return joblog;
    }
}
