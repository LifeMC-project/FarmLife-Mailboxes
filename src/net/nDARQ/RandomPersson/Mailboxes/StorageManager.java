package net.nDARQ.RandomPersson.Mailboxes;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.nDARQ.RandomPersson.Mailboxes.utils.Utils;

public class StorageManager {
	private static Block packageStorage;
	private static long currentStoragePointer = 1L;
	
	private static final int bsize=2, sizex=8, sizez=8, sizey=256-bsize*2, percont=5;
	private static final int storageCapacity = sizex*sizez*sizey*percont;
	static void createStorage(Block zero) {
		Utils.cout("&aCreating storage at " + zero.getX() + " " + zero.getZ() + " in " + zero.getWorld().getName());
		final Block zero2 = zero.getRelative(0, -zero.getY(), 0);
		packageStorage = zero2.getRelative(0, bsize, 0);
		for (int y=0; y<256; ++y) {
			for (int x=-bsize; x<sizex+bsize; ++x) {
				for (int z=-bsize; z<sizez+bsize; ++z) {
					Block b = zero2.getRelative(x, y, z);
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
		if (storagePointer < 1) {
			return items;
		}
		Container container = getContainer(storagePointer);
		int slot = getItemSlot(storagePointer);
		return getItems(container, slot);
	}
	private static ItemStack[] getItems(Container container, int slot) {
		ItemStack[] items = new ItemStack[5];
		Inventory inv = container.getInventory();
		for (int i=0; i<5; ++i) {
			items[i] = inv.getItem(slot+i);
		}
		return items;
	}
	public static synchronized void removeItems(long storagePointer) {
		if (storagePointer < 0) {
			return;
		}
		Utils.cout("&eRemoving items at " + storagePointer + " ..");
		Inventory container = getContainer(storagePointer).getInventory();
		int slot = getItemSlot(storagePointer);
		for (int i=0; i<5; ++i) {
			container.setItem(slot+i, null);
		}
		if (storagePointer < currentStoragePointer) {
			Utils.cout("&eChanging currentStoragePointer to " + storagePointer + " ..");
			currentStoragePointer = storagePointer;
		}
	}
	
	// POINTERS
	static synchronized long getNextStoragePointer() {
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
	private static Container getContainer(long storagePointer) {
		storagePointer = storagePointer/percont;
		long x = storagePointer%sizex;
		storagePointer = storagePointer/sizex;
		long z = storagePointer%sizez;
		storagePointer = storagePointer/sizez;
		long y = storagePointer%sizey;
		storagePointer = storagePointer/sizey;
		
		Container container = (Container)packageStorage.getRelative((int)x, (int)y, (int)z).getState();
		container.update(true);// loads the chunk
		return container;
	}
	private static int getItemSlot(long storagePointer) {
		return (int)(storagePointer%percont)*percont;
	}
	
	//DEBUG
	static Block getZero() {
		return packageStorage;
	}
	static void dumpStorage() {
		Utils.cout("&5Dumping storage into console..");
		for (long p=0; p<storageCapacity; ++p) {
			if (!isPointerFree(p)) {
				p = p/percont;
				long x = p%sizex;
				p = p/sizex;
				long z = p%sizez;
				p = p/sizez;
				long y = p%sizey;
				p = p/sizey;
				
				Container container = (Container)packageStorage.getRelative((int)x, (int)y, (int)z).getState();
				container.update(true);
				
				Utils.cout("&d" + p + "&5 (&d" + x + " " + y + " " + z + "&5):");
				Utils.cout("&d" + getItems(container, (int)p%5).toString());
			}
		}
		Utils.cout("&5Done!");
	}
}
