package com.fibermc.essentialcommands;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.ModMetadata;

public final class Updater {
    private Updater() {}

    // TODO @jp: 1.0.0 - move this to eccore, use GH releases for identifying latest versions _per MC vers_
    //  (or, ig, add something to build process to update the listings on jpcode.dev, or compute at jpcode.dev from GH, not here)
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

            ModMetadata modMetadata = EssentialCommands.MOD_METADATA;
            if (modMetadata == null) {
                EssentialCommands.LOGGER.warn("Failed to check for Essential Commands updates.");
                return;
            }

            String currentVersionStr = modMetadata.getVersion().getFriendlyString();
            try {
                Version currentVers = Version.parse(stripMinecraftVersion(currentVersionStr));//VersionDeserializer.deserializeSemantic(stripMinecraftVersion.apply(currentVersionStr));
                Version latestVers = Version.parse(stripMinecraftVersion(latestVersionStr));
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

    private static String stripMinecraftVersion(String versionStr) {
        return versionStr.substring(0, versionStr.indexOf("-mc"));
    }
}
