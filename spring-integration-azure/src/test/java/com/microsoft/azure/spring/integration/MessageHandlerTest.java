/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.AzureMessageHandler;
import com.microsoft.azure.spring.integration.core.api.PartitionSupplier;
import com.microsoft.azure.spring.integration.core.api.SendOperation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.expression.Expression;
import org.springframework.integration.MessageTimeoutException;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public abstract class MessageHandlerTest<O extends SendOperation> {

    protected O sendOperation;

    protected AzureMessageHandler handler;
    protected String destination = "dest";
    protected CompletableFuture<Void> future = new CompletableFuture<>();
    private Message<?> message =
            new GenericMessage<>("testPayload", ImmutableMap.of("key1", "value1", "key2", "value2"));
    private String payload = "payload";

    public abstract void setUp();

    @Test
    public void testSend() {
        this.handler.handleMessage(this.message);
        verify(this.sendOperation, times(1))
                .sendAsync(eq(destination), isA(Message.class), isA(PartitionSupplier.class));
    }

    @Test
    public void testSendDynamicTopic() {
        String dynamicEventHubName = "dynamicName";
        Message<?> dynamicMessage =
                new GenericMessage<>(payload, ImmutableMap.of(AzureHeaders.NAME, dynamicEventHubName));
        this.handler.handleMessage(dynamicMessage);
        verify(this.sendOperation, times(1))
                .sendAsync(eq(dynamicEventHubName), isA(Message.class), isA(PartitionSupplier.class));
    }

    @Test
    public void testSendSync() {
        this.handler.setSync(true);
        Expression timeout = spy(this.handler.getSendTimeoutExpression());
        this.handler.setSendTimeoutExpression(timeout);

        this.handler.handleMessage(this.message);
        verify(timeout, times(1)).getValue(eq(null), eq(this.message), eq(Long.class));
    }

    @Test(expected = MessageTimeoutException.class)
    public void testSendTimeout() {
        when(this.sendOperation.sendAsync(eq(this.destination), isA(Message.class), isA(PartitionSupplier.class)))
                .thenReturn(new CompletableFuture<>());
        this.handler.setSync(true);
        this.handler.setSendTimeout(1);

        this.handler.handleMessage(this.message);
    }

    @Test
    public void testSendCallback() {
        ListenableFutureCallback<Void> callbackSpy = spy(new ListenableFutureCallback<Void>() {
            @Override
            public void onFailure(Throwable ex) {

            }

            @Override
            public void onSuccess(Void v) {

            }
        });

        this.handler.setSendCallback(callbackSpy);

        this.handler.handleMessage(this.message);

        verify(callbackSpy, times(1)).onSuccess(eq(null));
    }
}
