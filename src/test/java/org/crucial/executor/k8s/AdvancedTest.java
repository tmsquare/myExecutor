package org.crucial.executor.k8s;

import org.crucial.executor.IterativeRunnable;
import org.crucial.executor.ServerlessExecutorService;
import org.crucial.executor.aws.AWSLambdaExecutorService;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

public class AdvancedTest {

    @org.testng.annotations.Test
    public void testSubmit() throws ExecutionException, InterruptedException {

         final String ret = "test";
         ServerlessExecutorService esK8s = new KubernetesExecutorService("job2", "tmsquare/executor-image");
         esK8s.setLocal(false);

         Future<String> future = esK8s.submit((Serializable & Callable<String>) () -> {
             System.out.println("Hello from remote");
             return ret;
         });

         System.out.println(future.get());
    }

    @org.testng.annotations.Test
    public void testInvokeAll() throws ExecutionException, InterruptedException {

        final String ret = "test";
        ServerlessExecutorService esK8s = new KubernetesExecutorService("job1", "tmsquare/executor-image");
        esK8s.setLocal(false);

        List<Callable<String>> myTasks = Collections.synchronizedList(new ArrayList<>());
        IntStream.range(0, 4).forEach(i ->
                myTasks.add((Serializable & Callable<String>) () -> {
                    System.out.println("Run." + i);
                    return ret;
                }));
        List<Future<String>> futures = esK8s.invokeAll(myTasks);
        for (Future<String> future : futures) {
            System.out.println(future.get());
            //assert future.get().equals(ret);
        }

    }

    @Test
    public void testInvokeIterativeTask() {
        ServerlessExecutorService esK8s =  new KubernetesExecutorService("job1", "tmsquare/executor-image");
        esK8s.setLocal(false);

        System.out.println("Executor:");
        try {
            esK8s.invokeIterativeTask((IterativeRunnable) index -> System.out.println("Index " + index),
                    2, 0, 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("With finalize:");
        try {
            esK8s.invokeIterativeTask(
                    (IterativeRunnable) index -> System.out.println("Index " + index),
                    2, 0, 10,
                    (Serializable & Runnable) () -> System.out.println("Over"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
