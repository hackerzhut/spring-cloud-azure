/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core.converter;

import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;

/**
 * A converter to turn the payload of a {@link Message} from serialized form to a typed
 * Object and vice versa.
 *
 * @param <T> Azure message type
 * @author Warren Zhu
 */
public interface AzureMessageConverter<T> {

    /**
     * Convert the payload of a {@link Message} from a serialized form to a typed Object
     * of the specified target class.
     *
     * @param message     the input message
     * @param targetClass the target class for the conversion
     * @return the result of the conversion, or {@code null} if the converter cannot
     * perform the conversion
     */
    @Nullable
    T fromMessage(Message<?> message, Class<T> targetClass);

    /**
     * Create a {@link Message} whose payload is the result of converting the given
     * payload Object to serialized form. The optional {@link MessageHeaders} parameter
     * may contain additional headers to be added to the message.
     *
     * @param azureMessage       the Object to convert
     * @param headers            optional headers for the message (may be {@code null})
     * @param targetPayloadClass the target payload class for the conversion
     * @return the new message, or {@code null} if the converter does not support the
     * Object type or the target media type
     */
    @Nullable
    <U> Message<U> toMessage(T azureMessage, MessageHeaders headers, Class<U> targetPayloadClass);

    @Nullable
    default <U> Message<U> toMessage(T azureMessage, Class<U> targetPayloadClass) {
        return this.toMessage(azureMessage, new MessageHeaders(new HashMap<>()), targetPayloadClass);
    }
}
