/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.queue;

import com.google.common.collect.Sets;
import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.MessageHandlerOptions;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusTemplate;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Default implementation of {@link ServiceBusQueueOperation}.
 *
 * @author Warren Zhu
 */
public class ServiceBusQueueTemplate extends ServiceBusTemplate<ServiceBusQueueClientFactory>
        implements ServiceBusQueueOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusQueueTemplate.class);

    private final Set<String> subscribedQueues = Sets.newConcurrentHashSet();

    public ServiceBusQueueTemplate(ServiceBusQueueClientFactory clientFactory) {
        super(clientFactory);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean subscribe(String destination, @NonNull Consumer<Message<?>> consumer, @NonNull Class<?>
            targetPayloadClass) {
        Assert.hasText(destination, "destination can't be null or empty");

        if (subscribedQueues.contains(destination)) {
            return false;
        }

        subscribedQueues.add(destination);

        internalSubscribe(destination, consumer, targetPayloadClass);

        return true;
    }

    @Override
    public boolean unsubscribe(String destination) {

        //TODO: unregister message handler but service bus sdk unsupported

        return subscribedQueues.remove(destination);
    }

    @SuppressWarnings("unchecked")
    protected void internalSubscribe(String name, Consumer<Message<?>> consumer,
            Class<?> payloadType) {

        IQueueClient queueClient = this.senderFactory.getQueueClientCreator().apply(name);

        try {
            queueClient.registerMessageHandler(
                    new QueueMessageHandler(consumer, payloadType, queueClient), options);
        } catch (ServiceBusException | InterruptedException e) {
            LOGGER.error("Failed to register queue message handler", e);
            throw new ServiceBusRuntimeException("Failed to register queue message handler", e);
        }
    }

    protected class QueueMessageHandler<U> extends ServiceBusMessageHandler<U>{
        private final IQueueClient queueClient;

        public QueueMessageHandler(Consumer<Message<U>> consumer, Class<U> payloadType, IQueueClient queueClient) {
            super(consumer, payloadType);
            this.queueClient = queueClient;
        }

        @Override
        protected CompletableFuture<Void> success(UUID uuid) {
            return queueClient.completeAsync(uuid);
        }

        @Override
        protected CompletableFuture<Void> failure(UUID uuid) {
            return queueClient.abandonAsync(uuid);
        }
    }
}
