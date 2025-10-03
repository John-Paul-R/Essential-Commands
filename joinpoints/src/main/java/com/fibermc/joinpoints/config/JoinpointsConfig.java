package com.fibermc.joinpoints.config;

import java.nio.file.Path;

import com.fibermc.joinpoints.JoinpointsPerms;
import com.fibermc.joinpoints.types.JoinpointLimit;
import dev.jpcode.eccore.config.Config;
import dev.jpcode.eccore.config.ConfigOption;
import dev.jpcode.eccore.config.Option;

@SuppressWarnings("checkstyle:all")
public final class JoinpointsConfig extends Config<JoinpointsConfig> {

    @ConfigOption
    public final Option<Boolean> ENABLE_JOINPOINT = new Option<>("enable_joinpoint", true, Boolean::parseBoolean);

    @ConfigOption
    public final Option<JoinpointLimit> JOINPOINT_LIMIT = new Option<>(
        "joinpoint_limit",
        JoinpointLimit.any(3, 5, 10),
        JoinpointLimit::parse,
        JoinpointLimit::serialize
    );

    public JoinpointsConfig(Path savePath, String displayName, String documentationLink) {
        super(savePath, displayName, documentationLink);

        JOINPOINT_LIMIT.changeEvent.register(joinpointLimit -> {
            JoinpointsPerms.Registry.Group.joinpoint_limit_groups.clear();
            for (var limitGroup : joinpointLimit.getLimits().entrySet()) {
                var key = limitGroup.getKey();
                var limitNums = limitGroup.getValue();

                JoinpointsPerms.Registry.Group.joinpoint_limit_groups.put(
                    key,
                    JoinpointsPerms.makeNumericPermissionGroup(
                        "joinpoints.limit." + key.name().toLowerCase(),
                        limitNums
                    )
                );
            }
        });
    }
}
