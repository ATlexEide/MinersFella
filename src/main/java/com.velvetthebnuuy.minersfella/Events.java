package com.velvetthebnuuy.minersfella;

import java.util.*;
import org.bukkit.Bukkit;
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

		Dictionary<String, Boolean> visited = new Hashtable<>();

		Queue<Block> queue = new ArrayDeque<>();
		queue.offer(block);

		int blockCount = 0;
		int blockLimit = 10000;
		while (!queue.isEmpty()) {
			try {
				Block curr = queue.peek();

				String currBlock = curr.toString();
				visited.put(currBlock, true);

				boolean isLog = curr.getType().toString().contains("LOG");
				Block[] neighbours = new Block[isLog ? 22 : 6];
				neighbours[0] = curr.getRelative(BlockFace.UP);
				neighbours[1] = curr.getRelative(BlockFace.DOWN);
				neighbours[2] = curr.getRelative(BlockFace.NORTH);
				neighbours[3] = curr.getRelative(BlockFace.SOUTH);
				neighbours[4] = curr.getRelative(BlockFace.WEST);
				neighbours[5] = curr.getRelative(BlockFace.EAST);
				if (isLog) {
					//					Check for logs where the sides are not facing each other
					//					(Offset by 1 in the middle, up and down)
					//
					neighbours[6] = curr.getRelative(1, 1, 0);
					neighbours[7] = curr.getRelative(0, 1, 1);
					neighbours[8] = curr.getRelative(-1, 1, 0);
					neighbours[9] = curr.getRelative(0, 1, -1);

					neighbours[10] = curr.getRelative(1, 1, 1);
					neighbours[11] = curr.getRelative(-1, 1, 1);
					neighbours[12] = curr.getRelative(1, 1, -1);
					neighbours[13] = curr.getRelative(-1, 1, -1);

					neighbours[14] = curr.getRelative(1, 0, 1);
					neighbours[15] = curr.getRelative(-1, 0, 1);
					neighbours[16] = curr.getRelative(1, 0, -1);
					neighbours[17] = curr.getRelative(-1, 0, -1);

					neighbours[18] = curr.getRelative(1, -1, 1);
					neighbours[19] = curr.getRelative(-1, -1, 1);
					neighbours[20] = curr.getRelative(1, -1, -1);
					neighbours[21] = curr.getRelative(-1, -1, -1);
				}

				if (blockCount < blockLimit) {
					for (Block neighbouringBlock : neighbours) {
						currBlock = neighbouringBlock.toString();

						boolean isSameType = neighbouringBlock.getType().toString().equals(type);
						boolean isVisited = (visited.get(currBlock)) != null;
						if (isSameType && !isVisited) {
							visited.put(currBlock, true);
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
