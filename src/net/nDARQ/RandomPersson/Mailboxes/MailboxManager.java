package net.nDARQ.RandomPersson.Mailboxes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.nDARQ.RandomPersson.Mailboxes.Mailbox.Texture;
import net.nDARQ.RandomPersson.Mailboxes.mail.CustomMail;
import net.nDARQ.RandomPersson.Mailboxes.mail.Mail;

public class MailboxManager implements Listener {
	private static final HashMap<UUID,Mailbox> loadedMailboxes = new HashMap<UUID,Mailbox>();
	private static Block packageStorage;
	private static long currentStoragePointer = 1L;
	
	///////////////
	// MAILBOXES //
	///////////////
	public static Mailbox getMailbox(UUID uuid) {
		return loadedMailboxes.get(uuid);
	}
	
	private static boolean loadMailbox(UUID uuid) {
		YamlConfiguration conf = Config.getPlayerConfig(uuid);
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
	private static void unloadMailbox(UUID uuid) {
		loadedMailboxes.remove(uuid);
	}
	public static void saveAndCloseAllMailboxes() {
		Iterator<UUID> it = loadedMailboxes.keySet().stream().iterator();
		while (it.hasNext()) {
			saveMailbox(it.next());
		}
	}
	
	// MAIL
	public static synchronized boolean sendMail(Mail mail, UUID recipientUUID) {
		Mailbox mb = getMailbox(recipientUUID);
		boolean mailboxUnloaded = mb == null;
		if (mailboxUnloaded) {
			loadMailbox(recipientUUID);
			mb = getMailbox(recipientUUID);
		}
		if (!mb.addMail(mail.getItemCount() > 0 ? mail.lock(getNextStoragePointer()) : mail.lock(-1L))) {
			return false;
		}
		saveMailbox(recipientUUID);
		if (mailboxUnloaded) {
			unloadMailbox(recipientUUID);
		}
		return true;
	}
	
	/////////////
	// STORAGE //
	/////////////
	private static final int bsize=2, sizex=32, sizez=32, sizey=256-bsize*2, perchest=5;
	private static final int storageCapacity = sizex*sizez*sizey*perchest;
	static void createChests(Block zero) {
		zero = zero.getRelative(0, -zero.getY(), 0);
		packageStorage = zero.getRelative(0, bsize, 0);
		for (int y=0; y<256; ++y) {
			for (int x=-bsize; x<sizex+bsize; ++x) {
				for (int z=-bsize; z<sizez+bsize; ++z) {
					Block b = zero.getRelative(x, y, z);
					if (x<0 || z<0 || x>=sizex || z>=sizez || y<bsize || y>=sizey+bsize) {
						b.setType(Material.BARRIER);
					} else if (b.getType() != Material.CHEST) {
						b.setType(Material.CHEST);
					}
				}
			}
		}
	}
	
	// ITEMS
	public static synchronized void setItems(long storagePointer, ItemStack[] items) {
		Inventory container = getContainer(storagePointer).getInventory();
		int slot = getItemSlot(storagePointer);
		for (int i=0; i<5; ++i) {
			container.setItem(slot+i, items[i]);
		}
	}
	public static synchronized ItemStack[] getItems(long storagePointer) {
		ItemStack[] items = new ItemStack[5];
		Inventory container = getContainer(storagePointer).getInventory();
		int slot = getItemSlot(storagePointer);
		for (int i=0; i<5; ++i) {
			items[i] = container.getItem(slot+i);
		}
		return items;
	}
	public static synchronized void removeItems(long storagePointer) {
		Inventory container = getContainer(storagePointer).getInventory();
		int slot = getItemSlot(storagePointer);
		for (int i=0; i<5; ++i) {
			container.setItem(slot+i, null);
		}
		if (storagePointer < currentStoragePointer) {
			currentStoragePointer = storagePointer;
		}
	}
	
	// POINTERS
	private static synchronized long getNextStoragePointer() {
		while (!isPointerFree(currentStoragePointer)) ++currentStoragePointer;
		return currentStoragePointer>storageCapacity ? -1L : currentStoragePointer;
	}
	private static boolean isPointerFree(long storagePointer) {
		Inventory inv = getContainer(storagePointer).getInventory();
		int slot = getItemSlot(storagePointer);
		for (int i=0; i<5; ++i) {
			if (inv.getItem(slot+i) != null) return false;
		}
		return true;
	}
	private static Chest getContainer(long storagePointer) {
		storagePointer = storagePointer/perchest;
		long x = storagePointer%sizex;
		storagePointer = storagePointer/sizex;
		long z = storagePointer%sizez;
		storagePointer = storagePointer/sizez;
		long y = storagePointer%sizey;
		storagePointer = storagePointer/sizey;
		
		Chest c = (Chest)packageStorage.getRelative((int)x, (int)y, (int)z).getState();
		c.update(true);// loads the chunk
		return c;
	}
	private static int getItemSlot(long storagePointer) {
		return (int)(storagePointer%perchest)*perchest;
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
		CompletableFuture.supplyAsync(() -> saveMailbox(e.getPlayer().getUniqueId())).thenRun(() -> unloadMailbox(e.getPlayer().getUniqueId()));
	}
}
