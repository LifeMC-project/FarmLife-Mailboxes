package net.nDARQ.RandomPersson.Mailboxes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.nDARQ.RandomPersson.Mailboxes.Mailbox.Texture;
import net.nDARQ.RandomPersson.Mailboxes.mail.CustomMail;
import net.nDARQ.RandomPersson.Mailboxes.mail.LockedMail;
import net.nDARQ.RandomPersson.Mailboxes.mail.Mail;

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
		
		loadedMailboxes.put(uuid, mailbox);
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
	
	public static boolean sendMail(Mail mail, UUID recipientUUID) {
		boolean mailboxUnloaded = getMailbox(recipientUUID) == null;
		if (mailboxUnloaded) {
			loadMailbox(recipientUUID);
		}
		Mailbox mb = getMailbox(recipientUUID);
		if (!mb.addMail(mail.getItemCount() > 0 ? mail.lock(getNextStoragePointer()) : mail.lock(0L))) {
			return false;
		}
		saveMailbox(recipientUUID);
		if (mailboxUnloaded) {
			unloadMailbox(recipientUUID);
		}
		return true;
	}
	private static long getNextStoragePointer() {
		
	}
	
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		CompletableFuture.supplyAsync(() -> loadMailbox(e.getPlayer().getUniqueId()));
	}
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent e) {
		CompletableFuture.supplyAsync(() -> saveMailbox(e.getPlayer().getUniqueId())).thenRun(() -> unloadMailbox(e.getPlayer().getUniqueId()));
	}
}
