package net.nDARQ.RandomPersson.Mailboxes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.nDARQ.RandomPersson.Mailboxes.Mailbox.Texture;
import net.nDARQ.RandomPersson.Mailboxes.mail.CustomMail;
import net.nDARQ.RandomPersson.Mailboxes.mail.Mail;
import net.nDARQ.RandomPersson.Mailboxes.utils.Utils;

public class MailboxManager implements Listener {
	private static final HashMap<UUID,Mailbox> loadedMailboxes = new HashMap<UUID,Mailbox>();
	
	///////////////
	// MAILBOXES //
	///////////////
	public static Mailbox getMailbox(UUID uuid) {
		return loadedMailboxes.get(uuid);
	}
	
	private static synchronized boolean loadMailbox(UUID uuid) {
		Utils.cout("&eLoading mailbox " + uuid.toString() + "...");
		YamlConfiguration conf = Config.getPlayerConfig(uuid);
		Utils.cout("&eOpened config");
		Mailbox mailbox = new Mailbox(uuid);
		Utils.cout("&eCreated mailbox");
		mailbox.setTexture(Texture.value(conf.getString("texture")));
		Utils.cout("&eLoaded texture");
		conf.getMapList("mail").stream().forEach(map -> {
			Utils.cout("&aLoading a mail");
			Utils.cout("&5" + map.get("senderUUID") + " &d" + map.get("message") + " &5" + map.get("senderName") + " &d" + map.get("storagePointer") + " &5" + map.get("sentDate") + " &d" + map.get("expDate"));
			mailbox.addMail(new CustomMail(UUID.fromString((String)map.get("senderUUID")),
					(String)map.get("senderName"),
					(String)map.get("message"),
					((Number)map.get("storagePointer")).longValue(),
					((Number)map.get("sentDate")).longValue(),
					((Number)map.get("expDate")).longValue()));
			Utils.cout("&aCreated a CustomMail");
		});
		
		loadedMailboxes.put(uuid, mailbox);
		Utils.cout("&aMailbox loaded.");
		return true;
	}
	private static synchronized boolean saveMailbox(UUID uuid) {
		Mailbox mailbox = loadedMailboxes.get(uuid);
		YamlConfiguration conf = Config.getPlayerConfig(mailbox.getUUID());
		
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
		
		return Config.savePlayerConfig(mailbox.getUUID(), conf);
	}
	private static synchronized void unloadMailboxWithoutSaving(UUID uuid) {
		loadedMailboxes.remove(uuid);
	}
	public static void saveAllMailboxes() {
		loadedMailboxes.keySet().stream().forEach(mb -> saveMailbox(mb));
	}
	public static void reloadAllMailboxes() {
		saveAllMailboxes();
		Iterator<UUID> it = loadedMailboxes.keySet().stream().iterator();
		while (it.hasNext()) {
			loadedMailboxes.remove(it.next());
		}
		
		final CompletableFuture<Void> cf = CompletableFuture.completedFuture(null);
		Bukkit.getOnlinePlayers().stream().map(p -> p.getUniqueId()).forEach(uuid -> cf.thenRunAsync(new Runnable() {
			public void run() {
				loadMailbox(uuid);
			}
		}));
	}
	
	// MAIL
	public static boolean sendMail(Mail mail, UUID recipientUUID) {
		Mailbox mb = getMailbox(recipientUUID);
		boolean mailboxUnloaded = mb == null;
		if (mailboxUnloaded) {
			loadMailbox(recipientUUID);
			mb = getMailbox(recipientUUID);
		}
		long storagePointer = mail.getItemCount()>0 ? StorageManager.getNextStoragePointer() : -1L;
		if (storagePointer == -2L) {
			return false;
		}
		if (!mb.addMail(mail.lock(StorageManager.getNextStoragePointer()))) {
			return false;
		}
		StorageManager.setItems(storagePointer, mail.getItems());
		saveMailbox(recipientUUID);
		if (mailboxUnloaded) {
			unloadMailboxWithoutSaving(recipientUUID);
		}
		return true;
	}
	
	////////////
	// EVENTS //
	////////////
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		CompletableFuture.supplyAsync(() -> loadMailbox(e.getPlayer().getUniqueId()));
	}
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent e) {
		CompletableFuture.supplyAsync(() -> saveMailbox(e.getPlayer().getUniqueId())).thenRun(() -> unloadMailboxWithoutSaving(e.getPlayer().getUniqueId()));
	}
}
