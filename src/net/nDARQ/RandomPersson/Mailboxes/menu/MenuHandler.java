package net.nDARQ.RandomPersson.Mailboxes.menu;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;

public class MenuHandler {
	private static HashMap<UUID,Menu> menus = new HashMap<UUID,Menu>();
	
	public static void openMenu(Player p) {
		if (menus.containsKey(p.getUniqueId())) {
			menus.get(p.getUniqueId()).reopenMenu();;
		} else {
			Menu menu = new Menu(p);
			menus.put(p.getUniqueId(), menu);
		}
	}
	public static void closeMenu(Player p) {
		if (menus.containsKey(p.getUniqueId())) {
			menus.remove(p.getUniqueId()).closeMenu();
		}
	}
}
