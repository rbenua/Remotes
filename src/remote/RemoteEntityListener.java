package remote;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.inventory.ItemStack;

public class RemoteEntityListener extends EntityListener {

	
	/* If a player is wearing a chain helmet (a remote) and takes damage,
	 * this handler resets the durability on the helmet to its original value,
	 * preventing the remote id from changing.
	 */
	public void onEntityDamage(EntityDamageEvent e){
		if(e.getEntity() instanceof Player){
			ItemStack h = ((Player)e.getEntity()).getInventory().getHelmet();
			if(h.getTypeId() == Remote.usedID){
				h.setDurability((short)(h.getDurability() - e.getDamage()));
			}
		}
	}
}
