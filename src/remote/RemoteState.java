package remote;
import org.bukkit.entity.Item;

/* A RemoteState represents what the plugin
 * knows about an individual remote - whether
 * it's in someone's hand or, if it's been dropped,
 * the Item associated with it. This is needed
 * so that we can recycle remote IDs if players
 * destroy remotes (by lava or timeout or such).
 */
public class RemoteState{
	public Item i;
	public boolean held;
	public RemoteState(Item i){
		held = false;
		this.i = i;
	}
	public RemoteState(){
		held = true;
		i = null;
	}
}
