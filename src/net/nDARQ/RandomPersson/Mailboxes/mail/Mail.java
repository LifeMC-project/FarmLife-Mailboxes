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
		this.storagePointer = -1L;
		this.items = new ItemStack[5];
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
	public boolean setItem(int slot, ItemStack item) {
		boolean ret = this.items[slot] != null;
		this.items[slot] = item;
		return ret;
	}
	public void removeItem(int id) {
		this.items[id] = null;
	}
	public void overwriteItems(ItemStack[] items) {
		this.items = items;
	}
}
