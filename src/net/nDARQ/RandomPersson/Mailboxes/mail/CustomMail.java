package net.nDARQ.RandomPersson.Mailboxes.mail;

import java.util.UUID;

import org.bukkit.entity.HumanEntity;

import net.nDARQ.RandomPersson.Mailboxes.MailboxManager;

public class CustomMail extends Mail {

	public CustomMail(HumanEntity sender, String message, long storagePointer, long sentDate, long expDate) {
		this(sender.getUniqueId(), sender.getName(), message, storagePointer, sentDate, expDate);
	}
	public CustomMail(UUID senderUUID, String senderName, String message, long storagePointer, long sentDate, long expDate) {
		super(senderUUID, senderName);
		this.message = message;
		if (storagePointer > 0) {
			this.storagePointer = storagePointer;
			this.items = MailboxManager.getItems(storagePointer);
		}
		if (sentDate >= 0) this.sentDate = sentDate;
		if (expDate >= 0) this.expDate = expDate;
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
	public void setSenderUUID(UUID senderUUID) {
		this.senderUUID = senderUUID;
	}
	public void setSenderName(String senderName) {
		this.senderName = senderName;
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
