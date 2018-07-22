package net.nDARQ.RandomPersson.Mailboxes.mail;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

import net.nDARQ.RandomPersson.Mailboxes.MailUtils;

public class Mail extends LockedMail {
	protected Mail() {}
	public Mail(HumanEntity p) {
		this.senderUUID = p.getUniqueId();
		this.senderName = p.getName();
		this.items = new ItemStack[5];
		this.storagePointer = -1L;//TODO (?)
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
		this.items[id] = null;
	}
	public void overwriteItems(ItemStack[] items) {
		this.items = items;
	}
}
