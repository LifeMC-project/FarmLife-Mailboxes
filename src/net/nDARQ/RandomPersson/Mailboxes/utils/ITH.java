package net.nDARQ.RandomPersson.Mailboxes.utils;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;

public final class ITH
{
	private static Method _00, _01, _02, _03;
	private static Field _04, _05, _06;
	private static Constructor<?> _07;
	private static String _08, _09, _10;

	public static void a(HumanEntity _11, String _12)
	{
		checkNotNull(_11, "player");
		
		try {b(_11, _12);}
		catch (Exception _13) {_13.printStackTrace();}
	}
	private static void b(HumanEntity _11, String _12) throws Exception
	{
		Inventory _13 = _11.getOpenInventory().getTopInventory();
		
		if (_13 == null) return;
		if (_00 == null) _00 = _11.getClass().getMethod("getHandle"); Object _14 = _00.invoke(_11);
		if (_04 == null) _04 = _14.getClass().getField("playerConnection"); Object _15 = _04.get(_14);
		if (_05 == null) _05 = _14.getClass().getField("activeContainer"); Object _16 = _05.get(_14);
		if (_06 == null) _06 = _16.getClass().getField("windowId"); int _17 = _06.getInt(_16);
		
		c(_15, _14, _16, _17, _13, _12);
	}
	
	private static void c(Object _11, Object _12, Object _13, int _14, Inventory _15, String _16) throws Exception
	{
		if (_07 == null) _07 = h("PacketPlayOutOpenWindow").getConstructor(int.class, String.class, h("IChatBaseComponent"), int.class);
		
		String _17;
		int _18 = 0;
		
		switch (_15.getType())
		{
			case ANVIL: _17="minecraft:anvil"; break;
			case BEACON: _17="minecraft:beacon"; break;
			case BREWING: _17="minecraft:brewing_stand"; break;
			case CRAFTING: return;
			case CREATIVE: return;
			case DISPENSER: _17="minecraft:dispenser"; break;
			case DROPPER: _17="minecraft:dropper"; break;
			case ENCHANTING: _17="minecraft:enchanting_table"; break;
			case ENDER_CHEST:
			case CHEST: _17="minecraft:chest"; _18=_15.getSize(); break;
			case FURNACE: _17="minecraft:furnace"; break;
			case HOPPER: _17="minecraft:hopper"; break;
			case MERCHANT: _17="minecraft:villager"; _18=3; break;
			case PLAYER: return;
			case WORKBENCH: _17="minecraft:crafting_table"; break;
			default: return;
		}
		
		if (_02 == null) _02 = Class.forName(g() + ".util.CraftChatMessage").getMethod("fromString", String.class);
		if (_03 == null) _03 = _12.getClass().getMethod("updateInventory", h("Container"));
		
		Object _19 = ((Object[]) _02.invoke(null, _16))[0];
		Object _20 = _07.newInstance(_14, _17, _19, _18);
		d(_11, _20);
		
		_03.invoke(_12, _13);
	}
	private static void d(Object _11, Object _12) throws Exception
	{
		if (_01 == null) _01 = _11.getClass().getMethod("sendPacket", h("Packet"));
		_01.invoke(_11, _12);
	}
	
	private static String e()
	{
		if (_08 == null) _08 = Bukkit.getServer().getClass().getPackage().getName().replace("org.bukkit.craftbukkit.", "");
		return _08;
	}
	private static String f()
	{
		if (_09 == null) _09 = "net.minecraft.server." + e();
		return _09;
	}
	private static String g()
	{
		if (_10 == null) _10 = "org.bukkit.craftbukkit." + e();
		return _10;
	}
	
	private static Class<?> h(String _11) throws Exception
		{return Class.forName(f() + "." + _11);}
}