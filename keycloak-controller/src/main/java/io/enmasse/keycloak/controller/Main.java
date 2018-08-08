/*
 * Copyright 2017-2018, EnMasse authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.enmasse.keycloak.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.enmasse.address.model.AddressSpace;
import io.enmasse.k8s.api.AddressSpaceApi;
import io.enmasse.k8s.api.ConfigMapAddressSpaceApi;
import io.enmasse.k8s.api.ResourceChecker;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.User;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Security;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public class Main {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String [] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        Map<String, String> env = System.getenv();

        String openshiftUri = String.format("https://%s:%s", getEnvOrThrow(env, "KUBERNETES_SERVICE_HOST"), getEnvOrThrow(env, "KUBERNETES_SERVICE_PORT"));

        Config config = new ConfigBuilder().withMasterUrl(openshiftUri).withOauthToken(getAuthenticationToken()).withNamespace(getNamespace()).build();
        NamespacedOpenShiftClient client = new DefaultOpenShiftClient(config);
        AddressSpaceApi addressSpaceApi = new ConfigMapAddressSpaceApi(client);
        KeycloakParams params = KeycloakParams.fromEnv(System.getenv());
        if (params.getIdentityProviderUrl() == null || params.getIdentityProviderUrl().isEmpty()) {
            params.setIdentityProviderUrl(openshiftUri);
        }

        log.info("Started with params: {}", params);

        KubeApi kubeApi = userName -> {
            if (client.supportsOpenShiftAPIGroup("user.openshift.io")) {
                if (userName == null || userName.isEmpty() || userName.contains(":")) {
                    return "";
                }
                try {
                    User user = client.users().withName(userName).get();
                    if (user == null) {
                        return "";
                    }
                    return user.getMetadata().getUid();
                } catch (KubernetesClientException e) {
                    return "";
                }
            } else {
                return "";
            }
        };

        KeycloakManager keycloakManager = new KeycloakManager(new Keycloak(params), kubeApi);

        Duration resyncInterval = getEnv(env, "RESYNC_INTERVAL")
                .map(i -> Duration.ofSeconds(Long.parseLong(i)))
                .orElse(Duration.ofMinutes(5));

        Duration checkInterval = getEnv(env, "CHECK_INTERVAL")
                .map(i -> Duration.ofSeconds(Long.parseLong(i)))
                .orElse(Duration.ofSeconds(30));

        ResourceChecker<AddressSpace> resourceChecker = new ResourceChecker<>(keycloakManager, checkInterval);
        resourceChecker.start();
        addressSpaceApi.watchAddressSpaces(resourceChecker, resyncInterval);
    }

    private static String getEnvOrThrow(Map<String, String> env, String envVar) {
        String var = env.get(envVar);
        if (var == null) {
            throw new IllegalArgumentException(String.format("Unable to find value for required environment var '%s'", envVar));
        }
        return var;
    }

    private static Optional<String> getEnv(Map<String, String> env, String envVar) {
        return Optional.ofNullable(env.get(envVar));
    }

    private static final String SERVICEACCOUNT_PATH = "/var/run/secrets/kubernetes.io/serviceaccount";

    private static String getNamespace() throws IOException {
        String namespace = System.getenv("KUBERNETES_NAMESPACE");
        if (namespace == null) {
            return readFile(new File(SERVICEACCOUNT_PATH, "namespace"));
        } else {
            return namespace;
        }
    }

    private static String getAuthenticationToken() throws IOException {
        String token = System.getenv("KUBERNETES_TOKEN");
        if (token == null) {
            return readFile(new File(SERVICEACCOUNT_PATH, "token"));
        } else {
            return token;
        }
    }

    private static String readFile(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }
}
