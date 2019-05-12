package bot.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;

import bot.main.AnnouncerBot;
import bot.main.TelegramList;

public class UserManager {
	private HashMap<Long, User> users;
	private HashMap<Long, User> admins;

	public UserManager() {
		this.users = new HashMap<Long, User>();
		this.admins = new HashMap<Long, User>();
	}

	public synchronized boolean saveUsers(File file) {
		if (!AnnouncerBot.blockedSaving) {
			return save(file, users);
		}
		return false;
	}

	public synchronized boolean saveAdmins(File file) {
		if (!AnnouncerBot.blockedSaving) {
			return save(file, admins);
		}
		return false;
	}

	public void setAdmins(HashMap<Long, User> data) {
		this.admins = data;
	}

	public void setUsers(HashMap<Long, User> data) {
		this.users = data;
	}

	private boolean save(File file, HashMap<Long, User> toSave) {
		boolean successful = false;
		try (FileOutputStream fw = new FileOutputStream(file, false);
				ObjectOutputStream oos = new ObjectOutputStream(fw);) {

			oos.writeObject(toSave);
			successful = true;
		} catch (IOException e) {

		}
		return successful;
	}

	public HashMap<Long, User> load(File file) {

		HashMap<Long, User> outputMap = null;

		try (FileInputStream fw = new FileInputStream(file); ObjectInputStream oos = new ObjectInputStream(fw);) {

			outputMap = (HashMap<Long, User>) oos.readObject();

		} catch (IOException | ClassNotFoundException e) {
			if (!(e instanceof FileNotFoundException)) {
				AnnouncerBot.blockedSaving = true;
			}
		}
		return outputMap;
	}

	public void makeAdmin(User user) {
		admins.put(user.id, user);
		saveAdmins(new File(AnnouncerBot.ADMIN_FILE));
	}

	public void removeAdmin(long userID) {
		admins.remove(userID);
		saveAdmins(new File(AnnouncerBot.ADMIN_FILE));
	}

	public HashSet<User> usersOnList(TelegramList... lists) {

		HashSet<User> output = new HashSet<User>();

		for (User u : users.values()) {

			for (TelegramList telList : lists) {
				if (u.lists.contains(telList)) {
					output.add(u);
				}
			}
		}

		return output;
	}

	public boolean isUserOnList(long id, TelegramList... lists) {

		if (!users.containsKey(id)) {
			return false;
		}

		User u = users.get(id);

		for (TelegramList telList : lists) {
			if (u.lists.contains(telList)) {
				return true;
			}
		}

		return false;
	}

	public void subUser(long userID, String message) {
		message = message.toLowerCase();
		message = message.replace("/sub", "");

		TelegramList tl = TelegramList.valueOf(message.toUpperCase());
		if (users.containsKey(userID)) {
			this.users.get(userID).lists.add(tl);
		} else {
			User usr = new User();
			usr.id = userID;
			usr.lists.add(tl);
			users.put(userID, usr);
		}
		saveUsers(new File(AnnouncerBot.USER_FILE));
	}

	public void unsubUser(long user, String message) {
		message = message.toLowerCase();
		message = message.replaceAll("/unsub", "");

		TelegramList tl = TelegramList.valueOf(message.toUpperCase());
		if (this.users.containsKey(user)) {
			this.users.get(user).lists.remove(tl);
		}
		saveUsers(new File(AnnouncerBot.USER_FILE));
	}

	public HashMap<Long, User> getAdmins() {
		return (HashMap<Long, User>) this.admins.clone();
	}

	public boolean isAdmin(Long chatId) {

		return admins.containsKey(chatId) || chatId == AnnouncerBot.SUPER_ADMIN;
	}

	public User getUserByID(long chatID) {
		return users.get(chatID);
		
	}
}
