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

package me.masstrix.eternalnature.core.world;

import me.masstrix.eternalnature.events.ItemPlantEvent;
import me.masstrix.eternalnature.util.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a plant. Contains an Item entity that will be planted by {@link AutoPlanter}
 * in the future.
 */
public class Plant {

    public final static byte GROUND_INVALID = -1;
    public final static byte GROUND_VALID = 1;
    public final static byte GROUND_NONE = 0;

    private final AutoPlanter PLANTER;
    private Item item;
    private Sound sound;
    private PlantType plantType;
    private int ticks;
    private int plantTime;

    public Plant(AutoPlanter planter, Item linked) {
        this(planter, linked, PlantType.fromMaterial(linked.getItemStack().getType()));
    }

    /**
     * Creates a new plant instance with a linked item to plant.
     *
     * @param linked item to be planted.
     * @param plantType the plant type. This determines what block it can be placed on.
     */
    public Plant(AutoPlanter planter, Item linked, PlantType plantType) {
        this.PLANTER = planter;
        this.item = linked;
        this.plantType = plantType;
        this.plantTime = MathUtil.randomInt(20, 120);
        this.sound = plantPlaceSound(linked.getItemStack().getType());
    }

    /**
     * @return the time when to plant.
     */
    public int getPlantTime() {
        return plantTime;
    }

    /**
     * @return the linked item to plant.
     */
    public Item getItem() {
        return item;
    }

    /**
     * @return the sound played when the plant is placed.
     */
    public Sound getPlaceSound() {
        return sound;
    }

    /**
     * Returns if the plant is still valid. For a plant to be valid it's bound item has to
     * be alive and not removed as well as not having been planted.
     *
     * @return if the plant is still valid.
     */
    public boolean isValid() {
        return item.isValid() && plantType != null;
    }

    /**
     * Returns if the ground is a valid block type for this plant to be grown on.
     * If the item is not on the ground 0 will bre returned.
     *
     * @return 0 if item is not on the ground. If the block is valid returns 1 otherwise -1
     *         if the block type is invalid.
     */
    public byte isGroundTypeValid() {
        if (!item.isOnGround()) return GROUND_NONE;
        Block block = item.getLocation().clone().add(0, -0.3, 0).getBlock();
        if (block.isLiquid() || block.isPassable() || block.isEmpty()) return GROUND_INVALID;
        return plantType.getSoilType().isValidBlock(block.getType()) ? GROUND_VALID : GROUND_INVALID;
    }

    /**
     * Attempts to place the plant. If the plant has not been ticked long enough for it
     * to be planted then it will return false and not be placed.
     *
     * @return true if the plant was placed.
     */
    public boolean plant() {
        if (isGroundTypeValid() != GROUND_VALID) return false;
        if (++ticks >= plantTime) {
            ItemStack stack = item.getItemStack();

            ItemPlantEvent event = new ItemPlantEvent(item.getLocation(), plantType, stack.getType());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }

            boolean remove = true;
            if (stack.getAmount() > 1) {
                remove = false;
                stack.setAmount(stack.getAmount() - 1);
                item.setItemStack(stack);
            }
            Material block = convertFromItemToBlock(item.getItemStack().getType());
            if (block != null) {
                int y = (int) Math.ceil(item.getLocation().getY());
                Location place = item.getLocation().clone();
                place.setY(y);
                Block replace = place.getBlock();
                if (isBlockReplaceable(replace.getType())) {
                    place.getBlock().setType(block);
                    if (PLANTER.getPlaySounds())
                        place.getWorld().playSound(place, getPlaceSound(), 1, 1);
                }
            }
            if (remove)
                item.remove();
            return true;
        }
        return false;
    }

    /**
     * Returns if a block is replaceable when planting. Blocks such as grass, ferns and dead
     * bushes are apart of this group that can be replaced when a plant is being planted.
     *
     * @param type type to check if it is replaceable.
     * @return true if material is ok to be replaced.
     */
    private boolean isBlockReplaceable(Material type) {
        switch (type) {
            case AIR:
            case CAVE_AIR:
            case GRASS:
            case FERN:
            case DEAD_BUSH:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns the sound for when that type of material is placed.
     *
     * @param type material type to get the sound for.
     * @return the sound for that block.
     */
    private Sound plantPlaceSound(Material type) {
        if (plantType == PlantType.SAPLING || plantType == PlantType.FLOWER)
            return Sound.BLOCK_GRASS_PLACE;
        switch (type) {
            case SWEET_BERRIES:
            case SWEET_BERRY_BUSH:
                return Sound.BLOCK_SWEET_BERRY_BUSH_PLACE;
            default: return Sound.ITEM_CROP_PLANT;
        }
    }

    /**
     * Converts a material to it's block form material. Some crops and plants do not
     * have the same material type for when they are blocks or items like such as
     * melon seeds.
     *
     * @param type material to convert.
     * @return it's block form or it's self if no conversion is defined.
     */
    public static Material convertFromItemToBlock(Material type) {
        switch (type) {
            case MELON_SEEDS: return Material.MELON_STEM;
            case PUMPKIN_SEEDS: return Material.PUMPKIN_STEM;
            case BEETROOT_SEEDS: return Material.BEETROOTS;
            case WHEAT_SEEDS: return Material.WHEAT;
            case SWEET_BERRIES: return Material.SWEET_BERRY_BUSH;
            case CARROT: return Material.CARROTS;
            case POTATO: return Material.POTATOES;
            default: return type;
        }
    }

    /**
     * Gets the config section for a plant from it's material.
     *
     * @param type type to get config section for.
     * @return the config option or null if not valid.
     */
    public static String configPathFromPlant(Material type) {
        if (PlantType.isFlower(type)) return plantPath("flowers");
        if (PlantType.isSapling(type)) return plantPath("saplings");
        switch (type) {
            case CARROT:
            case CARROTS:
                return plantPath("carrot");
            case POTATO:
            case POTATOES:
                return plantPath("potato");
            case WHEAT:
            case WHEAT_SEEDS:
                return plantPath("wheat");
            case BEETROOT_SEEDS:
            case BEETROOTS:
                return plantPath("beetroot");
            case MELON_SEEDS:
            case MELON_STEM:
                return plantPath("melon");
            case PUMPKIN_SEEDS:
            case  PUMPKIN_STEM:
                return plantPath("pumpkin");
            case SWEET_BERRIES:
            case SWEET_BERRY_BUSH:
                return plantPath("sweet-berry");
            default: return null;
        }
    }

    private static String plantPath(String name) {
        return "global.auto-plant." + name;
    }
}
