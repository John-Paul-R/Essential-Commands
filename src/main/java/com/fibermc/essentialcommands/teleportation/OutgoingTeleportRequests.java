package com.fibermc.essentialcommands.teleportation;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

import com.fibermc.essentialcommands.playerdata.PlayerData;

public class OutgoingTeleportRequests {
    private final ArrayList<TeleportRequest> requests = new ArrayList<>();

    public void add(TeleportRequest request) {
        if (request.type == TeleportRequest.Type.TPA_TO) {
            requests.clear();
        }
        requests.add(request);
    }

    public Stream<TeleportRequest> stream() {
        return requests.stream();
    }

    public void remove(TeleportRequest request) {
        requests.remove(request);
    }

    public Optional<TeleportRequest> getRequestToPlayer(PlayerData targetPlayer) {
        return stream()
            .filter(r -> r.getTargetPlayerData() == targetPlayer)
            .findAny();
    }

    public void clear() {
        requests.forEach(TeleportRequest::end);
        requests.clear();
    }

    public int size() {
        return requests.size();
    }
}
