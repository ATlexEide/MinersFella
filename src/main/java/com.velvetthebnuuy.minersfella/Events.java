package com.velvetthebnuuy.minersfella;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class Events implements Listener {

	int sum = 0;

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		sum = 0;
		String itemInHand = Objects
			.requireNonNull(event.getPlayer().getEquipment())
			.getItemInMainHand()
			.getType()
			.toString();
		List<String> ALLOWED_TOOLS = Arrays.asList(
			"NETHERITE_PICKAXE",
			"STONE_PICKAXE",
			"IRON_PICKAXE",
			"GOLDEN_PICKAXE",
			"WOODEN_PICKAXE",
			"DIAMOND_PICKAXE"
		);
		if (!ALLOWED_TOOLS.contains(itemInHand)) {
			return;
		}

		Block block = event.getBlock();

		removeBlock(block);
	}

	public void removeBlock(Block block) {
		String type = block.getType().name();

		List<String> visited = new ArrayList<>();
		Queue<Block> queue = new ArrayDeque<>();
		queue.offer(block);

		int blockCount = 0;
		int blockLimit = 10000;
		while (!queue.isEmpty()) {
			try {
				Block curr = queue.peek();
				Location loc = curr.getLocation();
				String currLocation = Double.toString(loc.getX()) + loc.getY() + loc.getZ();
				visited.add(currLocation);

				Block[] neighbours = {
					curr.getRelative(BlockFace.UP),
					curr.getRelative(BlockFace.DOWN),
					curr.getRelative(BlockFace.NORTH),
					curr.getRelative(BlockFace.SOUTH),
					curr.getRelative(BlockFace.WEST),
					curr.getRelative(BlockFace.EAST)
				};

				if (blockCount < blockLimit) {
					for (Block neighbouringBlock : neighbours) {
						loc = neighbouringBlock.getLocation();
						currLocation = Double.toString(loc.getX()) + loc.getY() + loc.getZ();

						boolean isSameType =
							neighbouringBlock.getType().toString().equals(type) && !visited.contains(currLocation);
						if (isSameType) {
							visited.add(currLocation);
							queue.offer(neighbouringBlock);
							blockCount++;
						}
					}
				}
				curr.breakNaturally();
				queue.remove();
			} catch (Exception e) {
				Bukkit.getLogger().warning(e.getMessage());
			}
		}
	}
}
