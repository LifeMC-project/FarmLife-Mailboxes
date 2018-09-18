package net.nDARQ.RandomPersson.Mailboxes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
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
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		instance = this;
		Config.createMailboxFolder();
		Utils.registerListener(new MailboxManager());
		
		String storageWorldName = getConfig().getString("storageWorld");
		final World storageWorld = Bukkit.getWorld(storageWorldName);
		if (storageWorld == null) {
			Utils.cout("&4Disabling Mailboxes - Couldn't find storage world (&c" + storageWorldName + "&4) specified in the config!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		final int storageX = getConfig().getInt("storageX"), storageZ = getConfig().getInt("storageZ");
		Utils.cout("&a" + storageWorldName + ": " + storageX + " " + storageZ);
		
		StorageManager.createStorage((new Location(storageWorld, storageX, 0, storageZ)).getBlock());
		
		Utils.cout("&aReloading mailboxes..");
		MailboxManager.reloadAllMailboxes();
		Utils.cout("&aMailboxes reloaded!");
	}
	
	public void onDisable() {
		MailboxManager.saveAllMailboxes();
	}
	
	public static JavaPlugin getInstance() {
		return instance;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player)sender;
			if (args.length > 0) {
				switch (args[0].toLowerCase()) {
					case "close":
						MenuHandler.closeMenu(p);
						break;
					case "test":
						break;
					case "storagedump":
						StorageManager.dumpStorage();
						p.sendMessage(Utils.colorize("&aStorage dumped into console."));
						break;
					case "getzero":
						Block b = StorageManager.getZero();
						p.sendMessage(Utils.colorize("&5Storage block zero is at &d" + b.getX() + " " + b.getY() + " " + b.getZ()));
						break;
					default: {}
				}
				if (args[0].equalsIgnoreCase("close")) {
					MenuHandler.closeMenu(p);
				} else if (args[0].equalsIgnoreCase("test")) {
				}
			} else {
				MenuHandler.openMenu(p);
			}
		}
		else {
			if (args.length > 0 && args[0].equalsIgnoreCase("storagedump")) {
				StorageManager.dumpStorage();
			} else {
				Utils.cout("&cThis command can only be used from in-game!");
			}
		}
		return true;
	}
}
