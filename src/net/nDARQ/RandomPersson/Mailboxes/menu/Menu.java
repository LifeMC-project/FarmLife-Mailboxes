package net.nDARQ.RandomPersson.Mailboxes.menu;

import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.PacketDataSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload;
import net.nDARQ.RandomPersson.Mailboxes.Mailbox;
import net.nDARQ.RandomPersson.Mailboxes.MailboxManager;
import net.nDARQ.RandomPersson.Mailboxes.mail.LockedMail;
import net.nDARQ.RandomPersson.Mailboxes.mail.Mail;
import net.nDARQ.RandomPersson.Mailboxes.utils.Utils;

public class Menu implements Listener {
	private static final ItemStack
			item_MainMenu_SendMail = Utils.newItem(Material.MAP, 1, "&b&lSend Mail", "&7Send a letter or package to~&7another player on FarmLife!~~&e> Click to open menu!"),
			item_MainMenu_MyMailEmpty_Template = Utils.newHead(1, "&cMy Mail", "&7You don't have any mail.~&7Try sending some, you~&7might get mail in return!", "Traeton"),
			item_MainMenu_Settings = Utils.newItem(Material.REDSTONE_COMPARATOR, 1, "&b&lSettings", "&7Adjust settings and change~&7the skin for your mailbox.~~&e> Click to open menu!"),
			//Send Mail
			item_SendMail_Info = Utils.newItem(Material.MAP, 1, "&bSend Mail", "&7To send mail specify a player~&7and add a letter by clicking~&7the paper or add a package by~&7adding items into the slots."),
			item_SendMail_AddMessage = Utils.newItem(Material.PAPER, 1, "&bAdd a Message", "~&e> Click to add text!"),
			item_SendMail_SpecifyRecipient_Template = Utils.newHead(1, "&bSpecify Recipient", "~&e> Click to set recipient!", "Steve"),
			item_SendMail_Green = Utils.newItem(Material.FEATHER, 1, "&bSend Mail!", "~&e> Click to send!"),
			item_SendMail_Red = Utils.newItem(Material.FEATHER, 1, "&cSend Mail", "&7You must specify a recipient~&7and add a message or add~&7items to send!"),
			//Read Mail
			item_MyMail_Info_Template = Utils.newHead(1, "&bMy mail", "&7Click the items below to~&7retreive mail that you've~&7received!", "Traeton"),
			item_MyMail_Letter_Template = Utils.newItem(Material.PAPER, 1, "&bLetter", "&7From: &fNoone~~&e> Click to read!"),
			item_MyMail_Package_Template = Utils.newItem(Material.STORAGE_MINECART, 1, "&bPackage", "&7From: &fNoone~~&e> Click to claim items!"),
			//Settings
			item_Settings_Info = Utils.newItem(Material.REDSTONE_COMPARATOR, 1, "&bSettings", "&7Change your mailbox skin by~&7selecting one of the skins below!"),
			item_Settings_DefaultSkin = Utils.newHead(1, "&aDefault", "&7Just your typical plain jane~&7default texture!~~&a&l✓&f Unlocked!~~&e> Click to select!", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjZhNDllZmFhYWI1MzI1NTlmZmY5YWY3NWRhNmFjNGRkNzlkMTk5ZGNmMmZkNDk3Yzg1NDM4MDM4NTY0In19fQ=="),
			//Misc
			item_Exit = Utils.newItem(Material.COMPASS, 1, "&cExit to menu!", ""),
			item_Background = Utils.newItem(Material.STAINED_GLASS_PANE, 1, " ", "", (byte)7),
			item_NULL = Utils.newItem(Material.LAPIS_BLOCK, 1, "", "");
	
	final Player p;
	final Mailbox mailbox;
	final Inventory inv_MainMenu, inv_SendMail, inv_MyMail, inv_Settings;
	final ItemStack item_MainMenu_MyMailEmpty, item_MyMail_Info;
	Mail mail = null;
	
	public Menu(Player p) {
		this.p = p;
		mailbox = MailboxManager.getMailbox(p.getUniqueId());
		Utils.cout(String.valueOf(mailbox == null));
		
		item_MainMenu_MyMailEmpty = Utils.editHead(item_MainMenu_MyMailEmpty_Template, -1, null, null, p.getName());
		item_MyMail_Info = Utils.editHead(item_MyMail_Info_Template, -1, null, null, p.getName());
		
		inv_MainMenu = Bukkit.createInventory(p, 3*9, "Mailbox");
		inv_SendMail = Bukkit.createInventory(p, 5*9, "Send Mail");
		inv_MyMail = Bukkit.createInventory(p, 6*9, "My Mail");
		inv_Settings = Bukkit.createInventory(p, 5*9, "Settings");
		
		prepareMenus();
		Utils.registerListener(this);
		
		openMenu("Mailbox");
	}
	private void prepareMenus() {
		inv_MainMenu.setItem(11, item_MainMenu_SendMail);
		inv_MainMenu.setItem(13, item_MainMenu_MyMailEmpty);
		inv_MainMenu.setItem(15, item_MainMenu_Settings);
		
		inv_SendMail.setItem(4, item_SendMail_Info);
		inv_SendMail.setItem(28, item_SendMail_AddMessage);
		inv_SendMail.setItem(30, item_SendMail_SpecifyRecipient_Template);
		inv_SendMail.setItem(33, item_SendMail_Red);
		inv_SendMail.setItem(44, item_Exit);
		
		inv_MyMail.setItem(4, item_MyMail_Info);
		for (int i=10; i<35; ++i) {//TODO check
			if ((i+1)%9 == 0) {
				i+=2;
			}
			inv_MyMail.setItem(i, item_NULL);
		}
		inv_MyMail.setItem(53, item_Exit);
		
		inv_Settings.setItem(4, item_Settings_Info);
		inv_Settings.setItem(10, item_Settings_DefaultSkin);
		inv_Settings.setItem(44, item_Exit);
		
		for (int i=0; i<3*9; ++i) {
			if (inv_MainMenu.getItem(i) == null)
				inv_MainMenu.setItem(i, item_Background);
			else if (inv_MainMenu.getItem(i).equals(item_NULL))
				inv_MainMenu.setItem(i, null);
		}
		for (int i=0; i<5*9; ++i) {
			if (inv_SendMail.getItem(i) == null)
				inv_SendMail.setItem(i, item_Background);
			else if (inv_SendMail.getItem(i).equals(item_NULL))
				inv_SendMail.setItem(i, null);
			if (inv_Settings.getItem(i) == null)
				inv_Settings.setItem(i, item_Background);
			else if (inv_Settings.getItem(i).equals(item_NULL))
				inv_Settings.setItem(i, null);
		}
		for (int i=0; i<6*9; ++i) {
			if (inv_MyMail.getItem(i) == null)
				inv_MyMail.setItem(i, item_Background);
			else if (inv_MyMail.getItem(i).equals(item_NULL))
				inv_MyMail.setItem(i, null);
		}
	}
	
	public void openMenu(String title) {
		p.closeInventory();
		switch (title) {
			case "Mailbox":
				inv_MainMenu.setItem(13, mailbox.getMailList().size() > 0 ?
						item_MainMenu_MyMailEmpty :
						Utils.editHead(item_MainMenu_MyMailEmpty, -1, "&b&lMy Mail", "&7Click to view your mail.~~&7You have " + mailbox.getMailList().size() + " piece" + (mailbox.getMailList().size()==1 ? "" : "s") + " of mail~~&e> Click to open menu!", null));
				p.openInventory(inv_MainMenu);
				break;
			case "Send Mail":
				p.openInventory(inv_SendMail);
				break;
			case "My Mail":
				int i=10;
				for (LockedMail mail : mailbox.getMailList()) {
					if ((i+1)%9 == 0) {
						i+=2;
					}
					inv_MyMail.setItem(i, mail.isLetter() ?
							Utils.editItem(item_MyMail_Letter_Template, null, -1, null, "&a▶&7 From:&f " +  mail.getSenderName() + "~&a▶&7 Date:&f " + mail.getSentDateString() + "~&a▶&7 Expires:&f " + mail.getExpDateString() + "~~&e> Click to open!", -1) :
							Utils.editItem(item_MyMail_Package_Template, null, -1, null, "&a▶&7 From:&f " + mail.getSenderName() + "~&a▶&7 Date:&f " + mail.getSentDateString() + "~&a▶&7 Expires:&f " + mail.getExpDateString() + "~~&e> Click to open!", -1));
					++i;
				}
				p.openInventory(inv_MyMail);
				break;
			case "Settings":
				p.openInventory(inv_Settings);
				//TODO current skin
				break;
			default: {
				Bukkit.getConsoleSender().sendMessage(Utils.colorize("&c[Mailboxes] Menu with the name &4" + title + "&c does not exist!"));
			}
		}
//		currMenu = title;
	}
	
	private static int slotToMailId(int slot) {
		return slot - 8 - slot/9*2;
	}
	
	////////////
	// EVENTS //
	////////////
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getWhoClicked().equals(p)) {
			e.setCancelled(true);
			if (e.getClickedInventory() == null) {
				return;
			}
			switch(e.getClickedInventory().getTitle()) {
				case "Mailbox":
					switch (e.getSlot()) {
						case 11:
							mail = new Mail(p);
							openMenu("Send Mail");
							break;
						case 13:
							openMenu("My Mail");
							break;
						case 15:
							openMenu("Settings");
							break;
						default: {}
					}
					break;
				case "Send Mail":
					switch (e.getSlot()) {
						case 28:
							//TODO open chat - add message
							CraftPlayer cp = (CraftPlayer)p;
							EntityPlayer ep = cp.getHandle();
							PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload("MC|BOpen", new PacketDataSerializer(Unpooled.EMPTY_BUFFER));
							ep.playerConnection.sendPacket(packet);
//							ep.openBook(CraftItemStack.asNMSCopy(Utils.newItem(Material.BOOK_AND_QUILL, 1, "", "")));
							
							
							break;
						case 30:
							//TODO open chat - check offline player - add recipient
							break;
						case 33:
							//TODO check
							// recipient set?
							// message or items set?
							
							break;
						case 44:
							mail = null;//TODO reset storage pointer?
							openMenu("Mailbox");
							break;
						default: {}
					}	
					break;
				case "My Mail":
					switch (e.getSlot()) {
						case 53:
							openMenu("Mailbox");
							break;
						default: {
							if (e.getSlot()>=10 && e.getSlot()<=34 && e.getSlot()%9!=0 && e.getSlot()%9!=8) {
								//TODO open the mail
							}
						}
					}
					break;
				case "Settings":
					switch (e.getSlot()) {
						case 44:
							openMenu("Mailbox");
							break;
						default: {
							switch (e.getSlot()) {//TODO skins
								case 0:
									
									break;
								default: {}
							}
						}
					}
					break;
				default: {
					
				}
			}
		}
	}
	@EventHandler
	public void onPlayerEditBook(PlayerEditBookEvent e) {
		Utils.cout("&aPlayerEditBookEvent triggered!");
		Utils.cout(e.getNewBookMeta().getPage(0));
		System.out.println(e.isSigning());
	}
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		if (e.getPlayer().equals(p)) {
			PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload("MC|BOpen", new PacketDataSerializer(Unpooled.EMPTY_BUFFER));
			((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
		}
	}
}
