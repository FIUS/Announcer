package bot.user;

import java.io.Serializable;
import java.util.HashSet;

import bot.main.TelegramList;

public class User implements Serializable {
	
	private static final long serialVersionUID = -8957524136552453301L;
	
	public long id;
	public String name;
	public HashSet<TelegramList> lists;

	public User(String name, long id) {
		this.name = name;
		this.id = id;
		this.lists = new HashSet<TelegramList>();
	}

	public User(long id) {

		this.id = id;
		this.lists = new HashSet<TelegramList>();
	}

	public User() {
		this.lists = new HashSet<TelegramList>();
	}

	public boolean equals(Object other) {
		if (other instanceof User) {
			return false;
		}

		return ((User) other).id == this.id;
	}

	public boolean hasName() {
		return this.name != null;
	}
}
