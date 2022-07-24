package com.fibermc.essentialcommands;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.function.UnaryOperator;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.ModMetadata;

public final class Updater {
    private Updater() {}

    public static void checkForUpdates() {
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(1500))
            .build();
        client.sendAsync(
            HttpRequest.newBuilder()
                .uri(URI.create("https://www.jpcode.dev/essentialcommands/latest_version"))
                .version(HttpClient.Version.HTTP_2)
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString()
        ).thenAcceptAsync((HttpResponse<String> response) -> {
            String latestVersionStr = response.body();

            ModMetadata modMetadata = FabricLoader.getInstance().getModContainer("essential_commands").get().getMetadata();
            if (Objects.isNull(modMetadata)) {
                EssentialCommands.LOGGER.warn("Failed to check for Essential Commands updates.");
                return;
            }

            UnaryOperator<String> stripMinecraftVersion = (String versionStr) -> versionStr.substring(0, versionStr.indexOf("-mc"));
            String currentVersionStr = modMetadata.getVersion().getFriendlyString();
            try {
                Version currentVers = Version.parse(stripMinecraftVersion.apply(currentVersionStr));//VersionDeserializer.deserializeSemantic(stripMinecraftVersion.apply(currentVersionStr));
                Version latestVers = Version.parse(stripMinecraftVersion.apply(latestVersionStr));
                if (latestVers.compareTo(currentVers) > 0) {
                    String updateMessage = String.format(
                            "A new version of Essential Commands is available. Current: '%s' Latest: '%s'. Get the new version at %s",
                            currentVersionStr,
                            latestVersionStr,
                            "https://modrinth.com/mod/essential-commands"
                        );
                    EssentialCommands.LOGGER.info(updateMessage);
                    ServerLifecycleEvents.SERVER_STARTED.register((server) -> EssentialCommands.LOGGER.info(updateMessage));
                } else {
                    EssentialCommands.LOGGER.info("Essential Commands is up to date!");
                }
            } catch (VersionParsingException e) {
                e.printStackTrace();
            }
        });
    }
}
