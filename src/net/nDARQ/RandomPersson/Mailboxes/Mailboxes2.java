package net.nDARQ.RandomPersson.Mailboxes;

import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.nDARQ.RandomPersson.Mailboxes.menu.MenuHandler;
import net.nDARQ.RandomPersson.Mailboxes.utils.Utils;

public class Mailboxes2 extends JavaPlugin implements CommandExecutor {
	private static JavaPlugin instance;
	
	public void onEnable() {
		instance = this;
		Config.createMailboxFolder();
		getConfig().options().copyDefaults(true);
		Utils.registerListener(new MailboxManager());
		
		String storageWorldName = getConfig().getString("storageWorld");
		World storageWorld = Bukkit.getWorld(storageWorldName);
		if (storageWorld == null) {
			Utils.cout("&4Disabling Mailboxes - Couldn't find storage world (&c" + storageWorldName + "&4)specified in the config!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		int storageX = getConfig().getInt("storageX"), storageZ = getConfig().getInt("storageZ");
		
		CompletableFuture.supplyAsync(() -> {MailboxManager.createChests((new Location(storageWorld, storageX, 0, storageZ)).getBlock()); return true;});
	}
	
	public void onDisable() {
		MailboxManager.saveAndCloseAllMailboxes();
	}
	
	public static JavaPlugin getInstance() {
		return instance;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player)sender;
			
			if (args.length > 0 && args[0].equalsIgnoreCase("cancel")) {
				MenuHandler.closeMenu(p);
			} else {
				MenuHandler.openMenu(p);
			}
			
//			Block block = ((Player)sender).getLocation().getBlock();
//			Utils.createSkull(block, Mailbox.Texture.DEFAULT.getCode());
		}
		else {
			
		}
		
		return true;
	}
	
}
