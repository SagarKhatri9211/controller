/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.clustering.it.karaf.cli;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.karaf.shell.api.action.Action;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;

public abstract class AbstractDOMRpcAction implements Action {
    @Override
    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    public final Object execute() throws InterruptedException, ExecutionException {
        final DOMRpcResult result = invokeRpc().get();
        if (!result.getErrors().isEmpty()) {
            // FIXME: is there a better way to report errors?
            System.out.println("Invocation failed: " + result.getErrors());
            return null;
        } else {
            return result.getResult().prettyTree().get();
        }
    }

    protected abstract ListenableFuture<? extends DOMRpcResult> invokeRpc();
}
