package net.nDARQ.RandomPersson.Mailboxes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

import net.nDARQ.RandomPersson.Mailboxes.utils.Utils;

public class Config {
	public static final String prefix = Utils.colorize("&b&l[&bMailbox&b&l]&7 ");
	public static final long mailExpTimeInMilis = 7*24*3600*1000;
	//                                            D*H *s   *ms
	private static final File mailboxFolder = Paths.get(Mailboxes2.getInstance().getDataFolder().toPath().toString(), "mailboxes").toFile();
	
	public static YamlConfiguration getDefaultConfig() {
		YamlConfiguration conf = new YamlConfiguration();
		conf.set("texture", "DEFAULT");
		conf.set("mail", new HashMap<String,Object>());
		
		return conf;
	}
	public static YamlConfiguration getConfig(UUID uuid) {
		File mailboxFile = new File(mailboxFolder, uuid.toString()+".mailbox");
		if (!mailboxFile.exists()) {
			try {
				mailboxFile.createNewFile();
				YamlConfiguration conf = getDefaultConfig();
				conf.save(mailboxFile);
				return conf;
			}
			catch (IOException exc) {
				exc.printStackTrace();
			}
		}
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(mailboxFile);		
		return conf;
	}
	public static synchronized boolean saveConfig(UUID uuid, YamlConfiguration conf) {
		File mailboxFile = new File(mailboxFolder, uuid.toString()+".mailbox");
		mailboxFile.getParentFile().mkdirs();
		try {
//			mailboxFile.delete();
//			mailboxFile.createNewFile();
			conf.save(mailboxFile);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
//		Mailboxes2.getInstance().saveConfig();
		return true;
	}
	public static void deleteConfig(UUID uuid) {
		File mailboxFile = new File(mailboxFolder, uuid.toString()+".mailbox");
		
		if (mailboxFile.exists()) mailboxFile.delete();
	}
}
