package net.nDARQ.RandomPersson.Mailboxes.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.PluginManager;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.nDARQ.RandomPersson.Mailboxes.Mailboxes2;

public class Utils {
	public static final String _21 = "§";
	private static final PluginManager pluginManager = Bukkit.getServer().getPluginManager();
	
	//////////////////
	// STRING UTILS //
	//////////////////
	public static String colorize(String s) {
		return s.replaceAll("&", _21);
	}
	public static String getStringFromArray(String[] array, int startIndex, String separator) {
		String opt = array[startIndex++];
		
		while (startIndex<array.length)
			opt += separator + array[startIndex++];
		
		return opt;
	}
	public static List<String> stringDivider(String s, String seperator) {
		return stringDivider(s, seperator, 32);
	}
	public static List<String> stringDivider(String s, String seperator, int linelength) {
		List<String> lines = new ArrayList<String>();
		if (s == null) return lines;
		int i=-1, count=linelength+1;
		
		for (String ss : s.split(seperator)) {
			count+=ss.length()+seperator.length();
			if (count > linelength) {
				lines.add(ss);
				count=ss.length();
				i++;
			}
			else {
				lines.set(i, lines.get(i) + seperator + ss);
			}
		}
		
		return lines;
	}
	public static boolean isEmpty(Object[] array) {
		for (Object obj : array) {
			if (obj != null) {
				return false;
			}
		}
		return true;
	}
	
	//////////////
	// NEW ITEM //
	//////////////
	/**
	 * 
	 * examples:
	 *   create a new item:
	 *     ItemStack item = Utils.newItem(Material.WOOL, 10, "Cool Wool", "This is a block of wool~for cool people.", (byte)15);
	 *   edit an item:
	 *     item = Utils.newItem(item, null, -1, null, null, (byte)5); //changed wool color to green leaving previous material, amount, name and lore
	 * 
	 * @param material default: null
	 * @param count default: -1
	 * @param name default: null
	 * @param lore default: null
	 * @return A fresh ItemStack.
	 */
	public static ItemStack newItem(Material material, int count, String name, String lore) {
		return newItem(material, count, name, lore, (byte)0);
	}
	public static ItemStack newItem(Material material, int count, String name, String lore, int color) {
		return newItem(material, count, name, lore, color, -1, -1);
	}
	public static ItemStack newItem(Material material, int count, String name, String lore, int red, int green, int blue) {
		return newItem(material, count, name.replaceAll("&", _21), lore=="" ? null : Arrays.asList(lore.replaceAll("&", _21).split("~")), red, green, blue);
	}
	public static ItemStack newItem(Material material, int count, String name, List<String> lore, int red, int green, int blue) {
		ItemStack item;
		if (count < 0) count = 1;
		if (red > 0 && green < 0 && blue < 0) item = new ItemStack(material, count, (byte)red);
		else item = new ItemStack(material, count);
		
		ItemMeta meta = item.getItemMeta();
		if (name != null) meta.setDisplayName(name);
		if (lore != null) meta.setLore(lore);
		if (meta instanceof LeatherArmorMeta && red >= 0 && green >= 0 && blue >= 0) {
			((LeatherArmorMeta)meta).setDisplayName(name);
			((LeatherArmorMeta)meta).setColor(Color.fromRGB(red, green, blue));
		}
		item.setItemMeta(meta);
		
		return item;
	}
	public static ItemStack newHead(int count, String name, String lore, String skin) {
		ItemStack item = newItem(Material.SKULL_ITEM, count, name, lore, 3);
		
		ItemMeta meta = item.getItemMeta();
		SkullMeta sm = (SkullMeta)meta;
		if (skin.startsWith("eyJ0ZXh0dXJlcyI6")) {
			GameProfile gp = getNonPlayerProfileFromString(skin);
			try {
				Field profileField = sm.getClass().getDeclaredField("profile");
				profileField.setAccessible(true);
				profileField.set(sm, gp);
			}
			catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException exc) {
				exc.printStackTrace();
			}
		}
		else {
			sm.setOwner(skin);
		}
		item.setItemMeta(meta);
		
		return item;
	}
	// EDIT ITEM //
	public static ItemStack editItem(ItemStack item, Material material, int count, String name, String lore, int color) {
		return editItem(item, material, count, name==null||name==""?null:colorize(name), lore=="" ? null : Arrays.asList(colorize(lore).split("~")), color);
	}
	@SuppressWarnings("deprecation")
	public static ItemStack editItem(ItemStack item, Material material, int count, String name, List<String> lore, int color) {
		ItemStack newItem;
		if (color > 0) newItem = new ItemStack(material==null ? item.getType() : material, count<0 ? item.getAmount() : count, (byte)color);
		else newItem = new ItemStack(material==null ? item.getType() : material, count<0 ? item.getAmount() : count, item.getData().getData());
		ItemMeta meta = item.getItemMeta();
		if (name != null) meta.setDisplayName(name);
		if (lore != null) meta.setLore(lore);
		newItem.setItemMeta(meta);
		
		return newItem;
	}
	public static ItemStack editHead(ItemStack item, int count, String name, String lore, String skin) {
		ItemStack newItem = editItem(item, null, count, name, lore, -1);
		
		ItemMeta meta = newItem.getItemMeta();
		if (meta instanceof SkullMeta) {
			SkullMeta sm = (SkullMeta)meta;
			if (skin.startsWith("eyJ0ZXh0dXJlcyI6")) {
				GameProfile gp = getNonPlayerProfileFromString(skin);
				try {
					Field profileField = sm.getClass().getDeclaredField("profile");
					profileField.setAccessible(true);
					profileField.set(sm, gp);
				} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException exc) {
					exc.printStackTrace();
				}
			}
			else {
				sm.setOwner(skin);
			}
			newItem.setItemMeta(meta);
		}
		
		return newItem;
	}
	
	////////////////////
	// PLUGIN MANAGER //
	////////////////////
	public static PluginManager getPluginManager() {
		return pluginManager;
	}
	public static void registerListener(Listener l) {
		getPluginManager().registerEvents(l, Mailboxes2.getInstance());
	}
	public static void unregisterListener(Listener l) {
		HandlerList.unregisterAll(l);
	}
	
	//////////////////
	// PACKET UTILS //
	//////////////////
	private static Object respawnPacket = null;
	public static void sendRespawnPacket(Player p) throws Exception {
		Object nmsPlayer = Player.class.getMethod("getHandle").invoke(p);
		
		if (respawnPacket == null) {
			Class<?> enumClasses = Class.forName(nmsPlayer.getClass().getPackage().getName() + ".EnumClientCommand");
			
			for(Object ob : enumClasses.getEnumConstants()) {
				if(ob.toString().equals("PERFORM_RESPAWN")) {
					respawnPacket = Class.forName(nmsPlayer.getClass().getPackage().getName() + ".PacketPlayInClientCommand").getConstructor(enumClasses).newInstance(ob);
				}
			}
		}

		Object con = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
		con.getClass().getMethod("a", respawnPacket.getClass()).invoke(con, respawnPacket);
	}
	
	/////////
	// NMS //
	/////////
	private static final String version = Bukkit.getServer().getClass().getPackage().getName().substring(23);
	public static Class<?> getNMSClass(String name) {
		Class<?> clazz = null;
		try {
			clazz = Class.forName("net.minecraft.server." + version + "." + name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return clazz;
	}
	public static Class<?> getCBClass(String name) {
		Class<?> clazz = null;
		try {
			clazz = Class.forName("org.bukkit.craftbukkit." + version + "." + name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return clazz;
	}
	private static Class<?> blockPosition;
	private static Constructor<?> blockPositionConstructor;
	private static Method getWorldHandle, getTileEntity, setGameProfile;
	public static void init() {//TODO
		try {
			blockPosition = getNMSClass("BlockPosition");
			blockPositionConstructor = blockPosition.getConstructor(int.class, int.class, int.class);
			getWorldHandle = getCBClass("CraftWorld").getMethod("getHandle");
			getTileEntity = getNMSClass("WorldServer").getMethod("getTileEntity", blockPosition);
			setGameProfile = getNMSClass("TileEntitySkull").getMethod("setGameProfile", GameProfile.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	// SKULL TEXTURES //
	public static void setSkullTexture(String texture, Block block) {
		block.setType(Material.SKULL);
		Skull skullData = (Skull)block.getState();
		skullData.setSkullType(SkullType.PLAYER);
		
		try {
			Object world = getWorldHandle.invoke(block.getWorld());
			Object tile = getTileEntity.invoke(world, blockPositionConstructor.newInstance(block.getX(), block.getY(), block.getZ()));
			setGameProfile.invoke(tile, getNonPlayerProfileFromString(texture));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException | SecurityException e) {
			e.printStackTrace();
		}
//		TileEntitySkull skullTile = (TileEntitySkull)((CraftWorld)block.getWorld()).getHandle().getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));
//		skullTile.setGameProfile(getNonPlayerProfileFromTexture(texture));		
		
		block.getState().update(true);
	}
	
	public static GameProfile getNonPlayerProfileFromString(String texture) {
		GameProfile profile = new GameProfile(UUID.randomUUID(), "Mailbox");
		profile.getProperties().put("textures", new Property("textures", texture.startsWith("eyJ0ZXh0dXJlcyI6") ? texture : Base64Coder.encodeString("{textures:{SKIN:{url:\"" + texture + "\"}}}")));
		return profile;
	}
}
