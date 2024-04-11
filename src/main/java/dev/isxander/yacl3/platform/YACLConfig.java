package dev.isxander.yacl3.platform;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;

public class YACLConfig {
    public static final ConfigClassHandler<YACLConfig> HANDLER = ConfigClassHandler.createBuilder(YACLConfig.class)
            .id(YACLPlatform.rl("config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(YACLPlatform.getConfigDir().resolve("yacl.json5"))
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry
    public boolean showColorPickerIndicator = true;
}
