package net.nDARQ.RandomPersson.Mailboxes;

import java.util.ArrayList;
import java.util.UUID;

import net.nDARQ.RandomPersson.Mailboxes.mail.LockedMail;

public class Mailbox {
	
	public static enum Texture {
		DEFAULT("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjZhNDllZmFhYWI1MzI1NTlmZmY5YWY3NWRhNmFjNGRkNzlkMTk5ZGNmMmZkNDk3Yzg1NDM4MDM4NTY0In19fQ=="),
		BLUE("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjZhNDllZmFhYWI1MzI1NTlmZmY5YWY3NWRhNmFjNGRkNzlkMTk5ZGNmMmZkNDk3Yzg1NDM4MDM4NTY0In19fQ=="),
		WHITE("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTM5ZTE5NzFjYmMzYzZmZWFhYjlkMWY4NWZjOWQ5YmYwODY3NjgzZjQxMjk1NWI5NjExMTdmZTY2ZTIifX19"),
		RED("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGZhODljZTg1OTMyYmVjMWExYzNmMzFjYjdjMDg1YTViZmIyYWM3ZTQwNDA5NDIwOGMzYWQxMjM4NzlkYTZkYSJ9fX0="),
		GREEN("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzJiY2NiNTI0MDg4NWNhNjRlNDI0YTBjMTY4YTc4YzY3NmI4Yzg0N2QxODdmNmZiZjYwMjdhMWZlODZlZSJ9fX0=");
		
		private final String code;
		
		Texture (String code) {
			this.code = code;
		}
		
		public String getCode() {
			return code;
		}
		public static Texture value(String arg0) {
			return (arg0==null ? DEFAULT : valueOf(arg0));
		}
	}
	
	private final UUID uuid;
	private final ArrayList<LockedMail> mailList = new ArrayList<LockedMail>();
	private Texture texture = Texture.DEFAULT;
	private int capacity = 27;
	
	public Mailbox(UUID uuid) {
		this(uuid, 27);
	}
	public Mailbox(UUID uuid, int capacity) {
		this.uuid = uuid;
		this.capacity = capacity;
	}
	
	/////////////
	// GETTERS //
	/////////////
	public UUID getUUID() {
		return this.uuid;
	}
	@SuppressWarnings("unchecked")
	public ArrayList<LockedMail> getMailList() {
		return (ArrayList<LockedMail>)mailList.clone();
	}
	public int getMailAmount() {
		return mailList.size();
	}
	public int getCapacity() {
		return capacity;
	}
	public Texture getTexture() {
		return texture;
	}
	
	/////////////
	// SETTERS //
	////////////
	public boolean addMail(LockedMail mail) {
		if (mailList.size() < capacity) {
			return this.mailList.add(mail);
		}
		
		return false;
	}
	public boolean removeMail(int id) {
		return this.mailList.remove(id) != null;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	public void setTexture(Texture texture) {
		this.texture = texture;
	}
	
	//////////
	// UTIL //
	//////////
	public void save() {
		//TODO or not //TODO
	}
}
