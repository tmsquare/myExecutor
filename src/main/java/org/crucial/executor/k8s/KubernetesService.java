package org.crucial.executor.k8s;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;


import java.util.Collections;
import java.util.Optional;

public class KubernetesService {

    public static void start() {
        try (KubernetesClient client = new DefaultKubernetesClient()) {
            String namespace = Optional.ofNullable(client.getNamespace()).orElse("default");

            Service service = new ServiceBuilder()
                    .withNewMetadata()
                    .withName("my-service")
                    .endMetadata()
                    .withNewSpec()
                    .withSelector(Collections.singletonMap("app", "MyApp"))
                    .addNewPort()
                    .withName("test-port")
                    .withProtocol("TCP")
                    .withPort(8080)
                    .withTargetPort(new IntOrString(8080))
                    .endPort()
                    .withType("LoadBalancer")
                    .endSpec()
                    .build();

            client.services().inNamespace(namespace).createOrReplace(service);
        }
    }
}
