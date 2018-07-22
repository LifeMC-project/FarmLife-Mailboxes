package net.nDARQ.RandomPersson.Mailboxes.mail;

import java.util.UUID;

import org.bukkit.entity.HumanEntity;

public class CustomMail extends Mail {

	public CustomMail(HumanEntity sender, String message, long storagePointer, long sentDate, long expDate) {
		this(sender.getUniqueId(), sender.getName(), message, storagePointer, sentDate, expDate);
	}
	public CustomMail(UUID senderUUID, String senderName, String message, long storagePointer, long sentDate, long expDate) {
		this.senderUUID = senderUUID;
		this.senderName = senderName;
		this.message = message;
		this.items = items;//TODO get items from storage
		this.storagePointer = storagePointer;
		this.sentDate = sentDate;
		this.expDate = expDate;
	}
	public CustomMail(HumanEntity p) {
		super(p);
	}
	
	/////////////
	// SETTERS //
	/////////////
	public void setSender(HumanEntity sender) {
		this.senderUUID = sender.getUniqueId();
		this.senderName = sender.getName();
	}
	public void setStoragePointer(long storagePointer) {
		this.storagePointer = storagePointer;
	}
	public void setSentDate(long sentDate) {
		this.sentDate = sentDate;
	}
	public void setExpDate(long expDate) {
		this.expDate = expDate;
	}
}
