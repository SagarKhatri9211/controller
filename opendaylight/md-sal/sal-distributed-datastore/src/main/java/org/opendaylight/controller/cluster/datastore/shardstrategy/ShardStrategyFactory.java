/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.cluster.datastore.shardstrategy;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import org.opendaylight.controller.cluster.datastore.config.Configuration;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class ShardStrategyFactory {
    private static final String UNKNOWN_MODULE_NAME = "unknown";

    private final Configuration configuration;
    private final LogicalDatastoreType logicalStoreType;

    public ShardStrategyFactory(final Configuration configuration, final LogicalDatastoreType logicalStoreType) {
        checkState(configuration != null, "configuration should not be missing");
        this.configuration = configuration;
        this.logicalStoreType = requireNonNull(logicalStoreType);
    }

    public ShardStrategy getStrategy(final YangInstanceIdentifier path) {
        // try with the legacy module based shard mapping
        final String moduleName = getModuleName(requireNonNull(path, "path should not be null"));
        final ShardStrategy shardStrategy = configuration.getStrategyForModule(moduleName);
        if (shardStrategy == null) {
            // retry with prefix based sharding
            final ShardStrategy strategyForPrefix =
                    configuration.getStrategyForPrefix(new DOMDataTreeIdentifier(logicalStoreType, path));
            if (strategyForPrefix == null) {
                return DefaultShardStrategy.getInstance();
            }
            return strategyForPrefix;
        }

        return shardStrategy;
    }

    public static ShardStrategy newShardStrategyInstance(final String moduleName, final String strategyName,
            final Configuration configuration) {
        if (ModuleShardStrategy.NAME.equals(strategyName)) {
            return new ModuleShardStrategy(moduleName, configuration);
        }

        return DefaultShardStrategy.getInstance();
    }

    private String getModuleName(final YangInstanceIdentifier path) {
        if (path.isEmpty()) {
            return UNKNOWN_MODULE_NAME;
        }

        String namespace = path.getPathArguments().get(0).getNodeType().getNamespace().toString();
        String moduleName = configuration.getModuleNameFromNameSpace(namespace);
        return moduleName != null ? moduleName : UNKNOWN_MODULE_NAME;
    }
}
