package net.nDARQ.RandomPersson.Mailboxes.mail;

import java.util.UUID;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

import net.nDARQ.RandomPersson.Mailboxes.MailUtils;

public class Mail extends LockedMail {
	public Mail(HumanEntity sender) {
		this(sender.getUniqueId(), sender.getName());
	}
	public Mail(UUID senderUUID, String senderName) {
		this.senderUUID = senderUUID;
		this.senderName = senderName;
		this.storagePointer = -1L;//TODO (?)
		this.items = new ItemStack[6];
		this.sentDate = System.currentTimeMillis();
		this.expDate = MailUtils.getStandardExpDate(sentDate);
	}
	public boolean isSendable() {
		return this.message != null;
	}
	public LockedMail lock(long storagePointer) {
		this.storagePointer = storagePointer;
		return (LockedMail)this;
	}
	
	/////////////
	// SETTERS //
	/////////////
	public void setMessage(String message) {
		this.message = message;
	}
	public boolean addItem(ItemStack item) {
		for (int i=0; i<5; ++i) {
			if (this.items[i] == null) {
				this.items[i] = item;
				return true;
			}
		}
		return false;
	}
	public void removeItem(int id) {
		for (int i=id; i<5; ++i) {
			
		}
		this.items[id] = null;
	}
	public void overwriteItems(ItemStack[] items) {
		this.items = items;
	}
}
