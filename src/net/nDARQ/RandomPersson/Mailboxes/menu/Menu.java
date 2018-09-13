package net.nDARQ.RandomPersson.Mailboxes.menu;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import net.nDARQ.RandomPersson.Mailboxes.Mailbox;
import net.nDARQ.RandomPersson.Mailboxes.MailboxManager;
import net.nDARQ.RandomPersson.Mailboxes.Mailboxes2;
import net.nDARQ.RandomPersson.Mailboxes.mail.LockedMail;
import net.nDARQ.RandomPersson.Mailboxes.mail.Mail;
import net.nDARQ.RandomPersson.Mailboxes.utils.Utils;

public class Menu implements Listener {
	public enum InputType {
		MESSAGE, RECIPIENT, NONE;
	}
	public enum MenuType {
		MAIN_MENU, SEND_MAIL, MY_MAIL, SETTINGS;
	}
	
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
			item_EmptySlot = Utils.newItem(Material.STAINED_GLASS_PANE, 1, " ", "", (byte)8),
			item_Background = Utils.newItem(Material.STAINED_GLASS_PANE, 1, " ", "", (byte)7),
			item_NULL = Utils.newItem(Material.LAPIS_BLOCK, 1, "", "");
	
	final Player p;
	final Mailbox mailbox;
	final Inventory inv_MainMenu, inv_SendMail, inv_MyMail, inv_Settings;
	final ItemStack item_MainMenu_MyMailEmpty, item_MyMail_Info;
	Mail mail;
	OfflinePlayer recipient = null;
	InputType inputType;
	MenuType currentMenu;
	boolean closeInventoryMessage;
	BukkitTask closeTask;
	
	public Menu(Player p) {
		this.p = p;
		inputType = InputType.NONE;
		closeInventoryMessage = true;
		mailbox = MailboxManager.getMailbox(p.getUniqueId());
		mail = new Mail(p);
		
		item_MainMenu_MyMailEmpty = Utils.editHead(item_MainMenu_MyMailEmpty_Template, -1, null, null, p.getName());
		item_MyMail_Info = Utils.editHead(item_MyMail_Info_Template, -1, null, null, p.getName());
		
		inv_MainMenu = Bukkit.createInventory(p, 3*9, "Mailbox");
		inv_SendMail = Bukkit.createInventory(p, 5*9, "Send Mail");
		inv_MyMail = Bukkit.createInventory(p, 6*9, "My Mail");
		inv_Settings = Bukkit.createInventory(p, 5*9, "Settings");
		
		prepareMenus();
		Utils.registerListener(this);
		
		openInventory(MenuType.MAIN_MENU);
	}
	private void prepareMenus() {
		// Main Menu - "Mailbox"
		inv_MainMenu.setItem(11, item_MainMenu_SendMail);
		inv_MainMenu.setItem(13, item_MainMenu_MyMailEmpty);
		inv_MainMenu.setItem(15, item_MainMenu_Settings);
		// Send Mail Menu - "Send Mail"
		inv_SendMail.setItem(4, item_SendMail_Info);
		inv_SendMail.setItem(28, item_SendMail_AddMessage);
		inv_SendMail.setItem(30, item_SendMail_SpecifyRecipient_Template);
		inv_SendMail.setItem(33, item_SendMail_Red);
		inv_SendMail.setItem(44, item_Exit);
		for (int i=11; i<16; ++i) {
			inv_SendMail.setItem(i, item_NULL);
		}
		// My Mail Menu - "My Mail"
		inv_MyMail.setItem(4, item_MyMail_Info);
		for (int i=10; i<35; ++i) {
			if ((i+1)%9 == 0) {
				i+=2;
			}
			inv_MyMail.setItem(i, item_EmptySlot);
		}
		inv_MyMail.setItem(53, item_Exit);
		// Settings Menu - "Settings"
		inv_Settings.setItem(4, item_Settings_Info);
		inv_Settings.setItem(10, item_Settings_DefaultSkin);
		inv_Settings.setItem(44, item_Exit);
		
		// Background filling
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
	
	public void openInventory(MenuType menuType) {
		closeInventoryMessage = false;
		switch (menuType) {
			case MAIN_MENU:
				if (mailbox == null) {
					Utils.cout("&4Error while loading player's mailbox - &c" + p.getName() + "&4 (&c" + p.getUniqueId().toString() + "&4).");
					p.sendMessage(Utils.colorize("&4There was an error while loading your mailbox. Please try again after a few seconds or, if the problem persists, try rejoining the server."));
					CompletableFuture.supplyAsync(() -> {MenuHandler.closeMenu(p); return true;});
					return;
				}
				inv_MainMenu.setItem(13, mailbox.getMailAmount() > 0 ?
						item_MainMenu_MyMailEmpty :
						Utils.editHead(item_MainMenu_MyMailEmpty, -1, "&b&lMy Mail", "&7Click to view your mail.~~&7You have " + mailbox.getMailAmount() + " piece" + (mailbox.getMailAmount()==1 ? "" : "s") + " of mail~~&e> Click to open menu!", null));
				p.openInventory(inv_MainMenu);
				break;
			case SEND_MAIL:
				if (mail.isSendable() && recipient != null) {
					inv_SendMail.setItem(33, item_SendMail_Green);
				} else {
					inv_SendMail.setItem(33, item_SendMail_Red);
				}
				p.openInventory(inv_SendMail);
				break;
			case MY_MAIL:
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
			case SETTINGS:
				//TODO current skin
				switch (mailbox.getTexture()) {
					case DEFAULT:
						break;
					case BLUE:
						break;
					case GREEN:
						break;
					case RED:
						break;
					case WHITE:
						break;
					default: {}
				}
				
				p.openInventory(inv_Settings);
				break;
			default: {
				Utils.cout("&c[Mailboxes] Menu with the name &4" + menuType.name() + "&c does not exist!");
				return;
			}
		}
		currentMenu = menuType;
		if (closeTask != null) {
			closeTask.cancel();
			closeTask = null;
			Utils.cout("&aClose Task cancelled.");//TODO remove debug
		}
	}
	
	public void reopenMenu() {
		openInventory(currentMenu);
	}
	public void closeMenu() {
		Utils.unregisterListener(this);
		if (closeTask != null) {
			closeTask.cancel();
		}
		p.sendMessage(Utils.colorize("&eMailboxes menu has been closed."));
		
		boolean showMessage = true;
		for (int i=0; i<5; ++i) {
			if (inv_SendMail.getItem(11+i) != null && inv_SendMail.getItem(11+i).getType() != Material.AIR) {
				if (showMessage) {
					p.sendMessage(Utils.colorize("&aYou received back items you stored in the menu:"));
					showMessage = false;
				}
				ItemStack item = inv_SendMail.getItem(11+i);
				p.sendMessage(Utils.colorize("&e" + item.getType().name() + "&a x&b" + item.getAmount()));
				HashMap<Integer,ItemStack> items = p.getInventory().addItem(item);
				if (!items.isEmpty()) {
					p.getWorld().dropItem(p.getLocation(), items.values().iterator().next());//TODO dont drop items
				}
			}
		}
	}
	private static int slotToMailId(int slot) {
		return slot-8-slot/9*2;
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
							openInventory(MenuType.SEND_MAIL);
							break;
						case 13:
							openInventory(MenuType.MY_MAIL);
							break;
						case 15:
							openInventory(MenuType.SETTINGS);
							break;
						default: {}
					}
					break;
				case "Send Mail":
					switch (e.getSlot()) {
						case 11:case 12:case 13:case 14:case 15:
							e.setCancelled(false);
							break;
						case 28:
							inputType = InputType.MESSAGE;
							closeInventoryMessage = false;
							p.closeInventory();
							p.sendMessage(Utils.colorize("&ePlease enter the mail's message."));
							break;
						case 30:
							inputType = InputType.RECIPIENT;
							closeInventoryMessage = false;
							p.closeInventory();
							p.sendMessage(Utils.colorize("&ePlease enter the mail's recipient."));
							break;
						case 33:
							if (e.getCurrentItem().equals(item_SendMail_Green)) {
								for (int i=0; i<5; ++i) {
									mail.setItem(i, e.getClickedInventory().getItem(11+i));
									e.getClickedInventory().setItem(11+i, null);
								}
								MailboxManager.sendMail(mail, recipient.getUniqueId());
								p.sendMessage(Utils.colorize("&eThe mail has been sent!"));
								closeInventoryMessage = false;
								p.closeInventory();
								MenuHandler.closeMenu(p);
							}
							break;
						case 44:
							openInventory(MenuType.MAIN_MENU);
							break;
						default: {}
					}	
					break;
				case "My Mail":
					switch (e.getSlot()) {
						case 53:
							openInventory(MenuType.MAIN_MENU);
							break;
						default: {
							if (e.getSlot()>=10 && e.getSlot()<=34 && e.getSlot()%9!=0 && e.getSlot()%9!=8 && e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
								LockedMail m;
								switch (e.getCurrentItem().getType()) {
									case PAPER:
										m = mailbox.getMailList().get(slotToMailId(e.getSlot()));
										p.sendMessage(Utils.colorize("&aYou opened mail from &b" + m.getSenderName() + "&a. It said:"));
										p.sendMessage(Utils.colorize("&e" + m.getMessage()));
										p.closeInventory();
										break;
									case STORAGE_MINECART:
										m = mailbox.getMailList().get(slotToMailId(e.getSlot()));
										p.sendMessage(Utils.colorize("&aYou opened mail from &b" + m.getSenderName() + "&a. It said:"));
										p.sendMessage(Utils.colorize("&e" + m.getMessage()));
										p.sendMessage(Utils.colorize("&aAnd contained:"));
										for (int i=0; i<5; ++i) {
											if (m.getItems()[i] != null) {
												ItemStack item = m.getItems()[i];
												p.sendMessage(Utils.colorize("&e" + item.getType().name() + "&a x&b" + item.getAmount()));
												HashMap<Integer,ItemStack> items = p.getInventory().addItem(item);
												if (!items.isEmpty()) {
													p.getWorld().dropItem(p.getLocation(), items.values().iterator().next());//TODO dont drop items
												}
											}
										}
										p.closeInventory();
										break;
									default: {}
								}
							}
						}
					}
					break;
				case "Settings":
					switch (e.getSlot()) {
						case 44:
							openInventory(MenuType.MAIN_MENU);
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
					e.setCancelled(false);
				}
			}
		}
	}
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		if (e.getPlayer().getUniqueId().equals(p.getUniqueId())) {
			e.setCancelled(true);//TODO test changing menus when holding item
		}
	}
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		if (e.getPlayer().getUniqueId().equals(p.getUniqueId())) {
			if (closeInventoryMessage) {
				p.sendMessage(Utils.colorize("&eType &6/mb&e to reopen the Mailbox GUI or type &6/mb close&e to close the menu. It will close automatically in 3 minutes."));
				closeTask = Bukkit.getScheduler().runTaskLater(Mailboxes2.getInstance(), new Runnable() {
					public void run() {
						MenuHandler.closeMenu(p);
					}}, 20*180L);
			} else {
				closeInventoryMessage = true;
			}
		}
	}
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		if (e.getPlayer().getUniqueId().equals(p.getUniqueId())) {
			e.setCancelled(true);
			switch (inputType) {
				case MESSAGE:
					mail.setMessage(e.getMessage());
					inv_SendMail.setItem(28, Utils.editItem(item_SendMail_AddMessage, null, -1, "", "&7" + e.getMessage() + "~~&e> Click to edit text!", -1));
					inputType = InputType.NONE;
					closeInventoryMessage = true;
					openInventory(MenuType.SEND_MAIL);
					break;
				case RECIPIENT:
					OfflinePlayer op = Bukkit.getOfflinePlayer(e.getMessage());
					if (op == null) {
						p.sendMessage(Utils.colorize("&4Specified player doesn't exist! Please enter a correct playername or type /cancel to cancel."));
					} else {
						recipient = op;
						inv_SendMail.setItem(30, Utils.editHead(item_SendMail_SpecifyRecipient_Template, -1, "", "&7Recipient:&f " + op.getName() + "~~&e> Click to edit recipient!", op.getName()));
						inputType = InputType.NONE;
						closeInventoryMessage = true;
						openInventory(MenuType.SEND_MAIL);
					}
					break;
				default: {}
			}
		}
	}
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		if (e.getPlayer().getUniqueId().equals(p.getUniqueId())) {
			switch (e.getMessage()) {
				case "/cancel":
					e.setCancelled(true);
					inputType = InputType.NONE;
					closeInventoryMessage = false;
					p.openInventory(inv_SendMail);
					break;
				default: {}
			}
		}
	}
}
