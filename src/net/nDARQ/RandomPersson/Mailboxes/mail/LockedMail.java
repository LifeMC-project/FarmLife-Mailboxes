package net.nDARQ.RandomPersson.Mailboxes.mail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

public class LockedMail {
	public static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm a"), expFormat = new SimpleDateFormat("dd'd', hh'h', mm'm'");
	private static final Date date = new Date();
	protected UUID senderUUID;
	protected String senderName, message;
	protected ItemStack[] items;
	protected long storagePointer, sentDate, expDate;
	
	/////////////
	// GETTERS //
	/////////////
	public UUID getSenderUUID() {
		return senderUUID;
	}
	public String getSenderName() {
		return senderName;
	}
	public String getMessage() {
		return message;
	}
	public ItemStack[] getItems() {
		return items.clone();
	}
	public int getItemCount() {
		int count = 0;
		for (int i=0; i<5; ++i) {
			if (items[i] != null)
				++count;
		}
		return count;
	}
	public long getStoragePointer() {
		return storagePointer;
	}
	public boolean isLetter() {
		for (int i=0; i<5; ++i) {
			if (items[i] != null)
				return false;
		}
		return true;
	}
	public long getSentDate() {
		return sentDate;
	}
	public long getExpDate() {
		return expDate;
	}
	public String getSentDateString() {
		date.setTime(sentDate);
		return dateFormat.format(date);
	}
	public String getExpDateString() {
		date.setTime(expDate);
		return expFormat.format(date);
	}
}
