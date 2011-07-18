package remote;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.command.*;
import org.bukkit.Location;

public class Remote extends JavaPlugin implements CommandExecutor, Runnable{
	public Logger log = Logger.getLogger("Minecraft");
	/* The links HashMap stores which torches are associated with each remote.
	 * It's loaded from disk on startup, and saved every so often.
	 */
	public HashMap<Short, TorchList> links;
	/* placing is used to determine if a link must be added when a player
	 * places a torch. Each array contains the remotes that that player
	 * is currently placing with.
	 */
	public HashMap<Player, ArrayList<Short>> placing;
	/* inUse is part of the plugin's attempt to determine when it can recycle remote
	 * IDs. It stores RemoteStates for allocated remotes, which can be used to find
	 * out whether that remote is dead.
	 */
	public HashMap<Short, RemoteState> inUse;
	/* chain helmets are used for remotes.
	 */
	public static int usedID = 302;
	private boolean shouldSave = false;
	private File saveFile = new File("remote.dat");
	private Thread saveThread;
	
	public void onDisable() {
		saveThread.interrupt();
		saveNow();
		log.info("Remote plugin disabled");
	}
	
	/* In addition to initializing everything, loading from the file,
	 * and registering events,
	 * onEnable spawns the saving thread and stores a reference to it
	 * in saveThread.
	 */
	public void onEnable() {
		log.info("Remote plugin enabled");
		PluginManager pm = this.getServer().getPluginManager();
		placing = new HashMap<Player, ArrayList<Short>>();
		inUse = new HashMap<Short, RemoteState>();
		load();
		RemoteBlockListener bl = new RemoteBlockListener(this);
		RemotePlayerListener pl = new RemotePlayerListener(this);
		RemoteEntityListener el = new RemoteEntityListener();
		pm.registerEvent(Event.Type.BLOCK_PLACE, bl, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.REDSTONE_CHANGE, bl, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, bl, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_ANIMATION, pl, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, pl, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, pl, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, pl, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, el, Event.Priority.Normal, this);
		getCommand("remote").setExecutor(this);
		saveThread = new Thread(this);
		saveThread.start();
	}
	
	/* When this thread is started, it checks to see if data needs saving and does so
	 * every 15 seconds.
	 */
	public void run(){
		while(true){
			try{
				Thread.sleep(15000);
			}
			catch(InterruptedException ex){
				return;
			}
			saveNow();
		}
	}
	
	/* save is called by methods that modify state to indicate that saving is necessary.
	 * Actual work is done by saveNow, which is run every 15 seconds and checks shouldSave
	 * to see if anything's changed.
	 */
	public void save(){
		shouldSave = true;
	}
	private void saveNow(){
		if(!shouldSave){
			return;
		}
		shouldSave = false;
		log.info("Saving remote state");
		saveFile.delete();
		BufferedWriter w;
		try{
			saveFile.createNewFile();
			w = new BufferedWriter(new FileWriter(saveFile));
		}
		catch(IOException e){
			log.warning("Could not write to remote save file, aborting save");
			return;
		}
		Set<Short> keys = links.keySet();
		for(Short s : keys){
			TorchList list = links.get(s);
			String outLine = s.toString();
			if(list.isOn()){
				outLine += " 1";
			}
			else{
				outLine += " 0";
			}
			synchronized(list){
				for(Location loc : list){
					outLine += " " + loc.getWorld().getName();
					outLine += " " + Double.toString(loc.getX());
					outLine += " " + Double.toString(loc.getY());
					outLine += " " + Double.toString(loc.getZ());
				}
			}
			outLine += "\n";
			//log.info("Writing line for remote " + s.toString());
			//log.info("Line is " + outLine);
			try{
				w.write(outLine);
			}
			catch(IOException e){
				log.warning("Could not write to remote save file, aborting save");
				return;
			}
		}
		try{
			w.write("ids:\n");
		}
		catch(IOException e){
			log.warning("Could not write to remote save file, aborting save");
		}
		for(Short s : inUse.keySet()){
			try{
				w.write(s.toString() + "\n");
			}
			catch(IOException e){
				log.warning("Could not write to remote save file, aborting save");
			}
		}
		try{
			w.flush();
		}
		catch(IOException e){
			log.warning("Could not write to remote save file");
			return;
		}
	}
	
	/* loads remote/torch links from flat file, reinitializing the links hashmap.
	 * should only be called once during onEnable, after links is initialized.
	 */
	private void load(){
		log.info("Loading remote state");
		links = new HashMap<Short, TorchList>();
		BufferedReader r;
		try{
			r = new BufferedReader(new FileReader(saveFile));
		}
		catch(FileNotFoundException e){
			log.warning("File " + saveFile.getAbsolutePath() + " could not be found, creating it.");
			return;
		}
		String currLine;
		try{
			currLine = r.readLine();
		}
		catch(IOException e){
			e.printStackTrace();
			return;
		}
		while(currLine != null && !currLine.equals("ids:")){
			String[] subs = currLine.split(" ", 2);
			short remote;
			try{
				remote = Short.parseShort(subs[0]);
			}
			catch(NumberFormatException e){
				log.warning("Couldn't parse save file, loading aborted.");
				return;
			}
			//log.info("Loading line for remote " + Short.toString(remote));
			TorchList list = new TorchList();
			if(subs[1].startsWith("0")){
				list.toggle();
			}
			subs = subs[1].split(" ");
			for(int i = 1; i < subs.length;i += 4){
				Location l = new Location(getServer().getWorld(subs[i]), Double.parseDouble(subs[i+1]),
											Double.parseDouble(subs[i+2]), Double.parseDouble(subs[i+3]));
				list.add(l);
			}
			links.put(remote, list);
			try{
				currLine = r.readLine();
			}
			catch(IOException e){
				e.printStackTrace();
				return;
			}
		}
		try{
			currLine = r.readLine();
		}
		catch(IOException ex){
			ex.printStackTrace();
			return;
		}
		while(currLine != null){
			short remote;
			try{
				remote = Short.parseShort(currLine);
			}
			catch(NumberFormatException e){
				log.warning("could not parse remote save file, aborting load");
				return;
			}
			inUse.put(remote, new RemoteState());
			try{
				currLine = r.readLine();
			}
			catch(IOException e){
				e.printStackTrace();
				return;
			}
		}
		log.info("Done");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args){
		int numToGive = 1;
		if(args.length > 0){
			try{
				numToGive = Integer.parseInt(args[0]);
			}
			catch(NumberFormatException e){
				return false;
			}
		}
		if(!(sender instanceof Player)){
			return false;
		}
		Player s = (Player)sender;
		giveloop:
		for(int i = 0; i < numToGive; i++){
			ItemStack r = new ItemStack(usedID);
			r.setAmount(1);
			for(int j = 1; j < Short.MAX_VALUE; j++){
				if(inUse.get((short)j) == null || (!inUse.get((short)j).held && inUse.get((short)j).i.isDead())){
					//ID is available.
					r.setDurability((short)j);
					if(s.getInventory().addItem(r).isEmpty()){
						log.info("Giving player "+ s.getDisplayName() + " remote " + j);
						inUse.put((short)j, new RemoteState());
						links.put((short)j, new TorchList());
					}
					else break giveloop; //inventory is full.
					continue giveloop;
				}
			}
			log.warning("Remote plugin is out of remote data values to use!");
			sender.sendMessage("No more data values! Can't give you any more remotes, sorry.");
			break;
		}
		save();
		return true;
	}
}
