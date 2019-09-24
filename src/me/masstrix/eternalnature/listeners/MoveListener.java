/*
 * Copyright 2019 Matthew Denton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.masstrix.eternalnature.listeners;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.core.world.ChunkData;
import me.masstrix.eternalnature.core.world.WorldData;
import me.masstrix.eternalnature.core.world.WorldProvider;
import me.masstrix.eternalnature.data.UserData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {

    private EternalNature plugin;

    public MoveListener(EternalNature plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        Player player = event.getPlayer();
        double distance = from.distanceSquared(to);
        if (distance == 0) return;

        // Does the players chunk section change
        if (from.getChunk() != to.getChunk() || ChunkData.getSection(from.getY())
                != ChunkData.getSection(to.getY())) {
            // Load nearby sections
            WorldProvider provider = plugin.getEngine().getWorldProvider();
            WorldData worldData = provider.getWorld(player.getWorld());
            if (worldData != null) {
                worldData.loadNearby(to.toVector());
            }
        }

        // Ignore movement if player is a passenger.
        if (!player.isInsideVehicle() && !player.isGliding() && !player.isRiptiding()) {
            UserData user = plugin.getEngine().getUserData(player.getUniqueId());
            if (user != null) user.addWalkDistance((float) distance, player.isSprinting());
        }
    }
}
