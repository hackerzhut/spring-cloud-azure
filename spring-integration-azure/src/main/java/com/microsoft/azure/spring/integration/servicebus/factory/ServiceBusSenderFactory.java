/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.factory;

import com.microsoft.azure.servicebus.IMessageSender;

import java.util.function.Function;

/**
 * Factory to return functional creator of service bus sender
 *
 * @author Warren Zhu
 */
public interface ServiceBusSenderFactory {
    /**
     * Return a function which accepts service bus topic or queue name, then returns {@link IMessageSender}
     */
    Function<String, ? extends IMessageSender> getSenderCreator();
}
