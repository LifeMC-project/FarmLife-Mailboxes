package net.nDARQ.RandomPersson.Mailboxes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.nDARQ.RandomPersson.Mailboxes.Mailbox.Texture;
import net.nDARQ.RandomPersson.Mailboxes.mail.CustomMail;
import net.nDARQ.RandomPersson.Mailboxes.menu.MenuHandler;
import net.nDARQ.RandomPersson.Mailboxes.utils.Utils;

public class MailboxManager implements Listener {
	private static final HashMap<UUID,Mailbox> loadedMailboxes = new HashMap<UUID,Mailbox>();
	
	public static Mailbox getMailbox(UUID uuid) {
		return loadedMailboxes.get(uuid);
	}
	
	private static boolean loadMailbox(UUID uuid) {
		YamlConfiguration conf = Config.getConfig(uuid);
		Mailbox mailbox = new Mailbox(uuid);
		mailbox.setTexture(Texture.value(conf.getString("texture")));
		conf.getMapList("mail").stream().forEach(map -> {
			mailbox.addMail(new CustomMail(UUID.fromString((String)map.get("sender")),
					(String)map.get("senderName"),
					(String)map.get("message"),
					((Number)map.get("storagePointer")).longValue(),
					((Number)map.get("sentDate")).longValue(),
					((Number)map.get("expDate")).longValue()));
		});
//		for (final Map<?, ?> map : conf.getMapList("mail")) {
//			mailbox.addMail(new CustomMail(UUID.fromString((String)map.get("sender")),
//					(String)map.get("senderName"),
//					(String)map.get("message"),
//					((Number)map.get("storagePointer")).longValue(),
//					((Number)map.get("sentDate")).longValue(),
//					((Number)map.get("expDate")).longValue()));
//		}
		
//		System.out.println("Mailbox " + uuid.toString() + " loaded.");
		return true;
	}
	private static boolean saveMailbox(UUID uuid) {
		Mailbox mailbox = loadedMailboxes.get(uuid);
		YamlConfiguration conf = Config.getConfig(mailbox.getUUID());
		
		conf.set("texture", mailbox.getTexture().name());
		ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		mailbox.getMailList().stream().forEach(mail -> {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("senderUUID", mail.getSenderUUID().toString());
			map.put("senderName", mail.getSenderName().toString());
			map.put("message", mail.getMessage());
			map.put("storagePointer", mail.getStoragePointer());
			map.put("sentDate", mail.getSentDate());
			map.put("expDate", mail.getExpDate());
			list.add(map);
		});
		
		conf.set("mail", list);
		
		return Config.saveConfig(mailbox.getUUID(), conf);
	}
	private static void unloadMailbox(UUID uuid) {
		loadedMailboxes.remove(uuid);
	}
	
	@EventHandler//(priority=EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Utils.cout("&ePlayer joined.");
		YamlConfiguration conf = Config.getConfig(e.getPlayer().getUniqueId());
		loadMailbox(e.getPlayer().getUniqueId());
		MenuHandler.openMenu(e.getPlayer());
		//CompletableFuture.supplyAsync(() -> loadMailbox(e.getPlayer().getUniqueId())).thenRun(() -> MenuHandler.openMenu(e.getPlayer()));
		Utils.cout("&aPlayerJoinEvent completed.");
		
	}
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent e) {
		CompletableFuture.supplyAsync(() -> saveMailbox(e.getPlayer().getUniqueId())).thenRun(() -> unloadMailbox(e.getPlayer().getUniqueId()));
	}
}
