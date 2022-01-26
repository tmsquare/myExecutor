package org.crucial.executor.k8s;

import org.crucial.executor.IterativeRunnable;
import org.crucial.executor.ServerlessExecutorService;
import org.testng.annotations.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

public class AdvancedTest {

    @Test
    public void testSubmit() throws ExecutionException, InterruptedException {

        final String ret = "test";
        ServerlessExecutorService esK8s1 = new KubernetesExecutorService("job1", "tmsquare/executor-img");
        esK8s1.setLocal(false);

        Future<String> future1 = esK8s1.submitListener((Serializable  & Callable<String>) () -> {
            try {
                int serverPort = Integer.parseInt(System.getenv("PORT"));
                ServerSocket serverSocket = new ServerSocket(serverPort);
                serverSocket.setSoTimeout(1000000);
                while(true) {
                    System.out.println("Yup! Waiting for client on port " + serverSocket.getLocalPort() + "...");

                    Socket server = serverSocket.accept();
                    System.out.println("Just connected to " + server.getRemoteSocketAddress());

                    PrintWriter toClient =
                            new PrintWriter(server.getOutputStream(),true);
                    BufferedReader fromClient =
                            new BufferedReader(
                                    new InputStreamReader(server.getInputStream()));
                    String line = fromClient.readLine();
                    System.out.println("Server received: " + line);
                    toClient.println("Thank you for connecting to " + server.getLocalSocketAddress() + "\nGoodbye!");
                }
            }
            catch(UnknownHostException ex) {
                ex.printStackTrace();
            }
            catch(IOException e){
                e.printStackTrace();
            }
            return ret;
        });


        String IP = future1.get();
        System.out.println(IP);

        ServerlessExecutorService esK8s2 = new KubernetesExecutorService("job1", "tmsquare/executor-img");
        esK8s2.setLocal(false);
        Future<String> future2 = esK8s2.submit((Serializable & Callable<String>) () -> {
            try {
                int serverPort = Integer.parseInt(System.getenv("PORT"));
                String host = IP;
                System.out.println("Connecting to server on port " + serverPort);

                //Socket socket = new Socket(host,serverPort);
                Socket socket = new Socket(host, serverPort);
                System.out.println("Just connected to " + socket.getRemoteSocketAddress());
                PrintWriter toServer =
                        new PrintWriter(socket.getOutputStream(),true);
                BufferedReader fromServer =
                        new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));
                toServer.println("Hello from " + socket.getLocalSocketAddress());
                String line = fromServer.readLine();
                System.out.println("Client received: " + line + " from Server");
                toServer.close();
                fromServer.close();
                socket.close();
            }
            catch(UnknownHostException ex) {
                ex.printStackTrace();
            }
            catch(IOException e){
                e.printStackTrace();
            }
            return ret;
        });

        System.out.println(future2.get());

        esK8s2.deleteAllJobs();

    }

    @org.testng.annotations.Test
    public void testInvokeAll() throws ExecutionException, InterruptedException {

        final String ret = "test";
        ServerlessExecutorService esK8s = new KubernetesExecutorService("job1", "tmsquare/executor-img");
        esK8s.setLocal(false);

        List<Callable<String>> myTasks = Collections.synchronizedList(new ArrayList<>());
        IntStream.range(0, 4).forEach(i ->
                myTasks.add((Serializable & Callable<String>) () -> {
                    System.out.println("Run" + i);
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
        ServerlessExecutorService esK8s =  new KubernetesExecutorService("job1", "tmsquare/executor-img");
        esK8s.setLocal(false);

        System.out.println("Executor:");
        try {
            esK8s.invokeIterativeTask(
                    (IterativeRunnable) index -> System.out.println("Index " + index),
                    1, 0, 10,
                    (Serializable & Runnable) () -> System.out.println("Hello"));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("With finalize:");
        try {
            esK8s.invokeIterativeTask(
                    (IterativeRunnable) index -> System.out.println("Index " + index),
                    1, 0, 10,
                    (Serializable & Runnable) () -> System.out.println("Over"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
