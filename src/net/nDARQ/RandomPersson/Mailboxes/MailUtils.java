package net.nDARQ.RandomPersson.Mailboxes;

import org.bukkit.inventory.ItemStack;

import net.nDARQ.RandomPersson.Mailboxes.mail.CustomMail;
import net.nDARQ.RandomPersson.Mailboxes.mail.LockedMail;

public class MailUtils {
	public static CustomMail customize(LockedMail mail) {
		return new CustomMail(mail.getSenderUUID(), mail.getSenderName(), mail.getMessage(), mail.getStoragePointer(), mail.getSentDate(), mail.getExpDate());
	}
	
	public static long getStandardExpDate(long sentDate) {
		return Config.mailExpTimeInMilis+sentDate-System.currentTimeMillis();
	}
	public static ItemStack[] getItems(long storagePointer) {
		//TODO
		return null;
	}
}
