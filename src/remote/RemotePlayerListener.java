package remote;

import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;

public class RemotePlayerListener extends PlayerListener {
	private Remote plugin;
	public RemotePlayerListener(Remote r){
		super();
		plugin = r;
	}
	
	/* If a player right-clicks while holding a remote, then toggle placing-mode on that remote
	 * for that player. Any redstone torches placed in placing mode are registered to the remote.
	 * If a player left-clicks while holding a remote, toggle all torches registered to that remote.
	 */
	public void onPlayerInteract(PlayerInteractEvent e){
		if(e.getItem() != null && e.getItem().getTypeId() == Remote.usedID){
			short d = e.getItem().getDurability();
			if(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR){
				ArrayList<Short> remotes = plugin.placing.get(e.getPlayer());
				if(remotes == null){
					remotes = new ArrayList<Short>();
					plugin.placing.put(e.getPlayer(), remotes);
				}
				if(!remotes.remove(new Short(d))){
					remotes.add(new Short(d));
					e.getPlayer().sendMessage("Now placing with remote " + d);
				}
				else{
					e.getPlayer().sendMessage("No longer placing with remote " + d);
				}
			}
			else if(e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK){
				TorchList t = plugin.links.get(d);
				if(t != null){
					if(t.toggle()){
						e.getPlayer().sendMessage("Turning torches on with remote " + (int)d);
						synchronized(t){
							for(Location l : t){
								l.getWorld().getBlockAt(l).setType(Material.REDSTONE_TORCH_ON);
							}
						}
					}
					else{
						e.getPlayer().sendMessage("Turning torches off with remote " + (int)d);
						synchronized(t){
							for(Location l : t){
								l.getWorld().getBlockAt(l).setType(Material.REDSTONE_TORCH_OFF);
							}
						}
					}
				}
				else{
					t = new TorchList();
					t.toggle();
				}
			}
		}
		
	}
	
	
	/* This is used to keep track of what remote ids are available.
	 */
	public void onPlayerDropitem(PlayerDropItemEvent e){
		if(e.getItemDrop().getItemStack().getTypeId() == Remote.usedID){
			plugin.inUse.put(e.getItemDrop().getItemStack().getDurability(), new RemoteState(e.getItemDrop()));
		}
	}
	public void onPlayerPickupItem(PlayerPickupItemEvent e){
		if(e.getItem().getItemStack().getTypeId() == Remote.usedID){
			plugin.inUse.put(e.getItem().getItemStack().getDurability(), new RemoteState());
		}
	}
}
