package remote;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

public class RemoteBlockListener extends BlockListener implements Listener {
	static Remote plugin;
	public RemoteBlockListener(Remote r){
		super();
		plugin = r;
	}
	
	/*
	 * If a redstone torch is destroyed, remove it from any remotes.
	 */
	public void onBlockBreak(BlockBreakEvent e){
		Block b = e.getBlock();
		if(b.getType() == Material.REDSTONE_TORCH_OFF || b.getType() == Material.REDSTONE_TORCH_ON){
			for(TorchList l : plugin.links.values()){
				if(l != null){
					l.remove(b.getLocation());
				}
			}
		}
	}
	
	/*
	 * If a player places a redstone torch while holding a remote in placing-mode, 
	 * register that torch under that remote.
	 */
	public void onBlockPlace(BlockPlaceEvent e){
		if(e.getBlock().getType() == Material.REDSTONE_TORCH_ON
				|| e.getBlock().getType() == Material.REDSTONE_TORCH_OFF){
			ArrayList<Short> controllers = plugin.placing.get(e.getPlayer());
			if(controllers != null && !controllers.isEmpty()){
				//plugin.log.info("Player " + e.getPlayer().getDisplayName() + " placed a redstone torch at " + e.getBlock().getLocation().toString());
				for(Short c : controllers){
					if(plugin.links.get(c) == null){
						plugin.links.put(c, new TorchList());
					}
					plugin.links.get(c).add(e.getBlock().getLocation());
					plugin.log.info("Torch placed using remote " + c);
				}
				plugin.save();
			}
		}
	}
	/*
	 * If a redstone torch changes value and it's under the control of a remote, prevent the change.
	 */
	public void onBlockRedstoneChange(BlockRedstoneEvent e){
		if(e.getBlock().getType() == Material.REDSTONE_TORCH_OFF
				|| e.getBlock().getType() == Material.REDSTONE_TORCH_ON){
			for(TorchList tl : plugin.links.values()){
				if(tl != null && !tl.isEmpty()){
					synchronized(tl){
						for(Location l : tl){
							if(e.getBlock().getLocation().equals(l)){
								//plugin.log.info("Prevented a redstone change");
								e.setNewCurrent(e.getOldCurrent());
								return;
							}
						}
					}
				}
			}
		}
	}
}
