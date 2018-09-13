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
		Utils.cout("&eLoading mailbox for " + uuid.toString() + "...");
		YamlConfiguration conf = Config.getPlayerConfig(uuid);
		Utils.cout("&eOpened config");
		Mailbox mailbox = new Mailbox(uuid);
		Utils.cout("&eCreated mailbox");
		mailbox.setTexture(Texture.value(conf.getString("texture")));
		Utils.cout("&eLoaded texture");
		conf.getMapList("mail").stream().forEach(map -> {
			Utils.cout("&aLoading a mail");
			Utils.cout(map.get("senderUUID") + " " + map.get("message") + " " + map.get("senderName") + " " + map.get("storagePointer") + " " + map.get("sentDate") + " " + map.get("expDate"));
			mailbox.addMail(new CustomMail(UUID.fromString((String)map.get("senderUUID")),
					(String)map.get("senderName"),
					(String)map.get("message"),
					((Number)map.get("storagePointer")).longValue(),
					((Number)map.get("sentDate")).longValue(),
					((Number)map.get("expDate")).longValue()));
			Utils.cout(map.get("&aCreated a CustomMail"));
		});
		
		loadedMailboxes.put(uuid, mailbox);
		Utils.cout("&eMailbox loaded.");
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
		Utils.cout("&aSaving all mailboxes..");
		saveAllMailboxes();
		Utils.cout("&aCreating iterator..");
		Iterator<UUID> it = loadedMailboxes.keySet().stream().iterator();
		Utils.cout("&aClearing loaded mailboxes list");
		while (it.hasNext()) {
			loadedMailboxes.remove(it.next());
		}
		
		final CompletableFuture<Void> cf = CompletableFuture.completedFuture(null);
		Utils.cout("&aLoading mailboxes back..");
		Bukkit.getOnlinePlayers().stream().map(p -> p.getUniqueId()).forEach(uuid -> cf.thenRunAsync(new Runnable() {
			public void run() {
				Utils.cout("&eLoading mailbox " + uuid.toString());
				loadMailbox(uuid);
				Utils.cout("&aLoaded!");
			}
		}));
		Utils.cout("reloadAllMailboxes() finished!");
	}
	
	// MAIL
	public static boolean sendMail(Mail mail, UUID recipientUUID) {
		Mailbox mb = getMailbox(recipientUUID);
		boolean mailboxUnloaded = mb == null;
		if (mailboxUnloaded) {
			loadMailbox(recipientUUID);
			mb = getMailbox(recipientUUID);
		}
		if (!mb.addMail(mail.getItemCount() > 0 ? mail.lock(StorageManager.getNextStoragePointer()) : mail.lock(-1L))) {
			return false;
		}
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
