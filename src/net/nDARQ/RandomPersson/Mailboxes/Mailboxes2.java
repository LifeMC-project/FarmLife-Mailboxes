package net.nDARQ.RandomPersson.Mailboxes;

import org.bukkit.plugin.java.JavaPlugin;

import net.nDARQ.RandomPersson.Mailboxes.utils.Utils;

public class Mailboxes2 extends JavaPlugin {
	private static JavaPlugin instance;
	
	
	
	public void onEnable() {
		instance = this;
		getConfig().options().copyDefaults(true);
		Utils.init();//TODO (?)
		Utils.registerListener(new MailboxManager());
		
		//create chests (async)
	}
	
	public static JavaPlugin getInstance() {
		return instance;
		//save all mailboxes
	}
	
	
}

/*
public static long storeItems(ItemStack[] items) {
	if (Utils.isEmpty(items)) {
		return -1;
	}
	
	for (long i=0; !isPointerFree(currentStoragePointer); ++i) {
		if (i >= storageCapacity) {
			return -2;
		}
		++currentStoragePointer;
	}
	
	Inventory inv = getContainer(currentStoragePointer);
	int slot0 = getItemSlot(currentStoragePointer);
	for (int i=0; i<5; ++i) {
		inv.setItem(slot0 + i, items[i]);
	}
	
	return currentStoragePointer;
}
public static ItemStack[] getStorage(long storagePointer) {
	ItemStack[] items = new ItemStack[5];
	if (storagePointer<0) {
		return items;
	}
	
	Inventory inv = getContainer(storagePointer);
	int slot0 = getItemSlot(storagePointer);
	for (int i=0; i<5; ++i) {
		items[i] = inv.getItem(slot0 + i);
	}
	
	return items;
}
public static void clearStorage(long storagePointer) {
	if (storagePointer<0 || storagePointer>storageCapacity) {
		return;
	}
	
	Inventory inv = getContainer(storagePointer);
	int slot0 = getItemSlot(storagePointer);
	for (int i=0; i<5; ++i) {
		inv.setItem(slot0 + i, null);
	}
}
public static boolean isPointerFree(long storagePointer) {
	return getContainer(storagePointer).getItem(getItemSlot(storagePointer)) == null;
}
public static long getPointer(Block chest, int packageID) {
	return packageID + chest.getY()*5 + chest.getX()*5*256 + chest.getZ()*5*256*10;
	//             5 +          256*5 +           10*5*256 +           10*5*256*10
	//5pc
	//256y
	//10x
	//10z
}
private static Inventory getContainer(long storagePointer) {
	Chest c = (Chest)chest0.getRelative((int)(storagePointer%(5*256*10)/(5*256)), (int)(storagePointer%(5*256)/5), (int)(storagePointer/(5*256*10))).getState();
	c.update(true);
	return c.getInventory();
}
private static int getItemSlot(long storagePointer) {
	return (int)(storagePointer%5)*5;
}
*/