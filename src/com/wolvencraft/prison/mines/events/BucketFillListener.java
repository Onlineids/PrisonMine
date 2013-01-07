package com.wolvencraft.prison.mines.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.material.MaterialData;

import com.wolvencraft.prison.mines.PrisonMine;
import com.wolvencraft.prison.mines.mine.Mine;
import com.wolvencraft.prison.mines.mine.Protection;
import com.wolvencraft.prison.mines.util.Message;

public class BucketFillListener implements Listener {
	public BucketFillListener(PrisonMine plugin) {
        Message.debug("Initiating BucketFillListener");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
	
	@EventHandler
	public void onBucketFill(PlayerBucketFillEvent event) {
		if(event.isCancelled()) return;
        Message.debug("BucketFillEvent caught");
        
        Player player = event.getPlayer();
        
        if(player.hasPermission("prison.mine.bypass.break")) {
            Message.debug("The player has a permission to bypass the protection. Aborting . . .");
			return;
		}

        Message.debug("Retrieving the region list...");
        
        for (Mine mine : PrisonMine.getLocalMines()) {
			Message.debug("Checking mine " + mine.getId());
            
        	if(!mine.getProtectionRegion().isLocationInRegion(event.getBlockClicked().getRelative(event.getBlockFace()).getLocation())) continue;
        	
	        if(!player.hasPermission("prison.mine.protection.break." + mine.getId()) && !player.hasPermission("prison.mine.protection.break")) {
	        	Message.debug("Player " + event.getPlayer().getName() + " does not have permission to fill buckets in the mine");
	        	Message.sendError(player, "You are not allowed to fill buckets in this area");
	        	event.setCancelled(true);
	          	return;
	        }
            
            if (!mine.getProtection().contains(Protection.BLOCK_BREAK)) {
                Message.debug("The mine doesn't have breaking protection enabled, skipping rest of check...");
                continue;
            }

            Message.debug("Mine has a block breaking protection enabled");
			if(mine.getBreakBlacklist().getEnabled()) {
				Message.debug("Block breaking blacklist detected");
				boolean found = false;
				for(MaterialData block : mine.getBreakBlacklist().getBlocks()) {
					if(block.getItemType().equals(event.getBucket())) {
						found = true;
						break;
					}
				}
				
				if((mine.getBreakBlacklist().getWhitelist() && !found) || (!mine.getBreakBlacklist().getWhitelist() && found)) {
					Message.debug("Player " + player.getName() + " broke a black/whitelisted block in the mine!");
					Message.sendError(player, "You are not allowed to fill buckets in the mine");
					event.setCancelled(true);
					return;
				}
			}
			else {
				Message.debug("No block breaking blacklist detected");
				Message.sendError(player, "You are not allowed to fill buckets in the mine");
				event.setCancelled(true);
			}
        }
    }
}