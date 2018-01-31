/*
 * Copyright 2016 Red Hat Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.enmasse.controller.standard;

import io.enmasse.address.model.Address;
import io.enmasse.address.model.AddressResolver;
import io.enmasse.address.model.Status;
import io.enmasse.config.AnnotationKeys;
import io.enmasse.k8s.api.AddressApi;
import io.enmasse.k8s.api.EventLogger;
import io.enmasse.k8s.api.TestSchemaApi;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.client.OpenShiftClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.util.collections.Sets;
import org.mockito.internal.verification.VerificationModeFactory;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AddressControllerTest {
    private Kubernetes mockHelper;
    private AddressApi mockApi;
    private AddressController controller;
    private OpenShiftClient mockClient;
    private BrokerSetGenerator mockGenerator;

    @Before
    public void setUp() {
        mockHelper = mock(Kubernetes.class);
        mockGenerator = mock(BrokerSetGenerator.class);
        mockApi = mock(AddressApi.class);
        mockClient = mock(OpenShiftClient.class);
        EventLogger eventLogger = mock(EventLogger.class);
        StandardControllerSchema standardControllerSchema = new StandardControllerSchema();

        controller = new AddressController("me", mockApi, mockHelper, mockGenerator, null, eventLogger, standardControllerSchema::getSchema);
    }

    @Test
    public void testAddressGarbageCollection() throws Exception {
        Address alive = new Address.Builder()
                .setName("q1")
                .setType("queue")
                .setPlan("small-queue")
                .setStatus(new Status(true).setPhase(Status.Phase.Active))
                .build();
        Address terminating = new Address.Builder()
                .setName("q2")
                .setType("queue")
                .setPlan("small-queue")
                .setStatus(new Status(false).setPhase(Status.Phase.Terminating))
                .build();
        controller.resourcesUpdated(Sets.newSet(alive, terminating));
        verify(mockApi).deleteAddress(any());
        verify(mockApi).deleteAddress(eq(terminating));
    }

    @Test
    public void testDeleteUnusedClusters() throws Exception {
        Address alive = new Address.Builder()
                .setName("q1")
                .setType("queue")
                .setPlan("small-queue")
                .putAnnotation(AnnotationKeys.BROKER_ID, "broker-0")
                .putAnnotation(AnnotationKeys.CLUSTER_ID, "broker")
                .setStatus(new Status(true).setPhase(Status.Phase.Active))
                .build();

        KubernetesList oldList = new KubernetesListBuilder()
                .addToConfigMapItems(new ConfigMapBuilder()
                        .withNewMetadata()
                        .withName("mymap")
                        .endMetadata()
                        .build())
                .build();
        when(mockHelper.listClusters()).thenReturn(Arrays.asList(
                new AddressCluster("broker", new KubernetesList()),
                new AddressCluster("unused", oldList)));

        controller.resourcesUpdated(Sets.newSet(alive));

        verify(mockHelper).delete(any());
        verify(mockHelper).delete(eq(oldList));
    }
}
