package com.velvetthebnuuy.minersfella;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class Events implements Listener {

	private final Main plugin;

	public Events(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Collection<ItemStack> drops = event.getBlock().getDrops(event.getPlayer().getInventory().getItemInMainHand());
		if (drops.isEmpty()) {
			return;
		}
		FileConfiguration cfg = this.plugin.getConfig();

		ItemStack itemInHand = Objects.requireNonNull(event.getPlayer().getEquipment()).getItemInMainHand();
		String itemInHandName = itemInHand.getType().toString();
		int durability = itemInHand.getType().getMaxDurability();

		Damageable meta = (Damageable) itemInHand.getItemMeta();
		assert meta != null;
		int currDamage = meta.getDamage();

		boolean isDurabilityBurnEnabled = cfg.getBoolean("durability_burn");
		int cfg_block_limit = cfg.getInt("block_limit");
		int BLOCK_LIMIT = isDurabilityBurnEnabled
			? Math.min(durability - currDamage, cfg_block_limit)
			: cfg_block_limit;
		List<String> MINABLE_BLOCKS = cfg.getStringList("minable");
		List<String> CHOPPABLE_BLOCKS = cfg.getStringList("choppable");
		List<String> ALLOWED_PICKAXES = cfg.getStringList("allowed_pickaxes");
		List<String> ALLOWED_AXES = cfg.getStringList("allowed_axes");
		Block block = event.getBlock();
		String type = block.getType().toString();

		Dictionary<String, String> ORE_TABLE = getStringOreTable();

		boolean isVeinMinerEnabled = cfg.getBoolean("vein_miner");
		boolean isTreeFellerEnabled = cfg.getBoolean("tree_feller");
		boolean isLog = block.getType().toString().contains("LOG");
		boolean isAllowedOre = MINABLE_BLOCKS.contains(block.getType().toString());
		boolean isAllowedLog = CHOPPABLE_BLOCKS.contains(block.getType().toString());
		boolean isAllowedPickaxe = ALLOWED_PICKAXES.contains(itemInHandName);
		boolean isAllowedAxe = ALLOWED_AXES.contains(itemInHandName);
		if ((!isLog && !isAllowedOre) || (!isLog && !isVeinMinerEnabled)) {
			return;
		}

		if ((isLog && !isAllowedLog) || (isLog && !isTreeFellerEnabled)) {
			return;
		}
		if ((!isLog && !isAllowedPickaxe) || (isLog && !isAllowedAxe)) {
			return;
		}

		if (isLog && !checkIsTree(block, 100)) {
			return;
		}

		Dictionary<String, Integer> removalResult = removeBlock(block, isLog, BLOCK_LIMIT, type, itemInHand);
		int totalBlocks = removalResult.get("totalBlocks");
		int totalDrops = removalResult.get("totalDrops");

		int newDamage = currDamage + totalBlocks;
		if (isDurabilityBurnEnabled) {
			if (newDamage >= durability) {
				Player player = event.getPlayer();
				player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1F);
				player.getInventory().removeItem(itemInHand);
			} else {
				meta.setDamage(newDamage);
				itemInHand.setItemMeta(meta);
			}
		} else {
			meta.setDamage(currDamage + 1);
			itemInHand.setItemMeta(meta);
		}

		ItemStack item = type.contains("ORE")
			? new ItemStack(Objects.requireNonNull(Material.getMaterial(ORE_TABLE.get(type))), totalDrops)
			: new ItemStack(Objects.requireNonNull(Material.getMaterial(type)), totalDrops);

		event.getPlayer().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
	}

	private static Dictionary<String, String> getStringOreTable() {
		Dictionary<String, String> ORE_TABLE = new Hashtable<>();
		ORE_TABLE.put("IRON_ORE", "RAW_IRON");
		ORE_TABLE.put("DEEPSLATE_IRON_ORE", "RAW_IRON");
		ORE_TABLE.put("GOLD_ORE", "RAW_GOLD");
		ORE_TABLE.put("NETHER_GOLD_ORE", "GOLD_NUGGET");
		ORE_TABLE.put("DEEPSLATE_GOLD_ORE", "RAW_GOLD");
		ORE_TABLE.put("COPPER_ORE", "RAW_COPPER");
		ORE_TABLE.put("DEEPSLATE_COPPER_ORE", "RAW_COPPER");
		ORE_TABLE.put("DIAMOND_ORE", "DIAMOND");
		ORE_TABLE.put("DEEPSLATE_DIAMOND_ORE", "DIAMOND");
		ORE_TABLE.put("COAL_ORE", "COAL");
		ORE_TABLE.put("DEEPSLATE_COAL_ORE", "COAL");
		ORE_TABLE.put("REDSTONE_ORE", "REDSTONE_DUST");
		ORE_TABLE.put("DEEPSLATE_REDSTONE_ORE", "REDSTONE_DUST");
		ORE_TABLE.put("EMERALD_ORE", "EMERALD");
		ORE_TABLE.put("DEEPSLATE_EMERALD_ORE", "EMERALD");
		ORE_TABLE.put("LAPIS_ORE", "LAPIS_LAZULI");
		ORE_TABLE.put("DEEPSLATE_LAPIS_ORE", "LAPIS_LAZULI");
		ORE_TABLE.put("NETHER_QUARTZ_ORE", "QUARTZ");
		return ORE_TABLE;
	}

	public Dictionary<String, Integer> removeBlock(
		Block block,
		boolean isLog,
		int blockLimit,
		String type,
		ItemStack itemInHand
	) {
		Set<String> visited = new HashSet<>();
		Queue<Block> queue = new ArrayDeque<>();
		queue.offer(block);

		int blockCount = 1;
		int totalDrop = 0;
		while (!queue.isEmpty()) {
			try {
				Block curr = queue.peek();
				Collection<ItemStack> drop = curr.getDrops(itemInHand);
				ItemStack[] dropArr = new ItemStack[1];
				drop.toArray(dropArr);
				int dropAmount = dropArr[0].getAmount();
				totalDrop += dropAmount;

				String currBlock = curr.toString();
				visited.add(currBlock);
				//				visited.add(currBlock);

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
						boolean isVisited = (visited.contains(currBlock));

						if (isSameType && !isVisited) {
							visited.add(currBlock);

							queue.offer(neighbouringBlock);
							blockCount++;
						}
					}
				}

				curr.setType(Material.AIR);
				queue.remove();
			} catch (Exception e) {
				Bukkit.getLogger().warning(e.getMessage());
			}
		}

		Dictionary<String, Integer> result = new Hashtable<>();
		result.put("totalBlocks", blockCount);
		result.put("totalDrops", totalDrop);
		return result;
	}

	public boolean checkIsTree(Block block, int maxSize) {
		boolean isTree = false;
		int airCount = 0;
		for (int i = 1; i < maxSize; i++) {
			String currBlockType = block.getRelative(0, i, 0).getType().toString();
			Bukkit.getServer().broadcastMessage(String.valueOf(i));
			if (currBlockType.contains("AIR")) {
				airCount++;
			}
			if (airCount > 5) {
				break;
			}
			if (currBlockType.contains("LEAVES")) {
				isTree = true;
				break;
			}
		}
		return isTree;
	}
}
