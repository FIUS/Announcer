package bot.main;

import bot.dish.DishTimer;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import bot.user.User;
import bot.user.UserManager;

public class BotDataProcessor {

	private UserManager userManager;
	private MessageSender sender;
	private AnnouncerBot bot;
	private DishTimer timer;

	private HashMap<String, Integer> washerStates;

	public BotDataProcessor(AnnouncerBot bot, UserManager userManager, MessageSender sender) {
		this.userManager = userManager;
		this.sender = sender;
		this.bot = bot;
		this.timer = new DishTimer(sender, userManager);

		washerStates = new HashMap<String, Integer>();
		washerStates.put("asterix", 3);
		washerStates.put("obelix", 2);
		washerStates.put("idefix", 1);
		washerStates.put("miraculix", 0);

		washerStates.put("tick", 7);
		washerStates.put("trick", 6);
		washerStates.put("track", 5);
		washerStates.put("donald", 4);
	}

	public void reactOnSubChange(Update update, String message, String command) {
		int i = 0;
		boolean groupFound = false;
		for (String compare : TelegramList.getAllStrings(command)) {
			if (message.replaceAll(" ", "").toLowerCase().contains(compare.toLowerCase())) {
				if (command.equals("sub")) {
					userManager.subUser(update.getMessage().getChatId(), message);
					sender.sendMessage("Subscribed to " + TelegramList.values()[i], update);
					groupFound = true;
					break;
				} else {
					userManager.unsubUser(update.getMessage().getChatId(), message);
					sender.sendMessage("Unbscribed from " + TelegramList.values()[i], update);
					groupFound = true;
					break;
				}
			}
			i++;
		}
		if (!groupFound && message.toLowerCase().startsWith("/" + command.toLowerCase())) {
			sendAvailableGroups(update, message.replaceAll(command, ""));
		}
	}

	public void processAdminRequest(Update update) {
		displayAdminRequestMenu(update.getMessage().getFrom().getFirstName(), update.getMessage().getChatId());
		sender.sendMessage("A request has been send", update);
	}

	public void buttonHandler(Update update) {

		String buttonName = update.getCallbackQuery().getData();
		long message_id = update.getCallbackQuery().getMessage().getMessageId();
		long chat_id = update.getCallbackQuery().getMessage().getChatId();

		String message = "";
		String editedManaged = "";
		String nameOfUser = "";
		long userChatID = -1;
		boolean adminButton = false;

		if (buttonName.startsWith("btn_admin_Yes_")) {
			String[] splittedRequest = buttonName.split("_");
			userChatID = Long.parseLong(splittedRequest[3]);
			nameOfUser = splittedRequest[4];
			message = "Your request has been accepted";
			editedManaged = "The request has been accepted";
			adminButton = true;

			userManager.makeAdmin(new User(nameOfUser, userChatID));

		} else if (buttonName.startsWith("btn_admin_No_")) {
			userChatID = Long.parseLong(buttonName.split("_")[3]);
			message = "Your request has been declined";
			editedManaged = "The request has been declined";
			adminButton = true;
		} else if (buttonName.startsWith("btn_save_Yes")) {
			AnnouncerBot.blockedSaving = false;
			sender.sendEditedMessage(chat_id, message_id, "*WARNING:* Saving is enabled");
		} else if (buttonName.startsWith("btn_save_No")) {

			sender.sendEditedMessage(chat_id, message_id, "Saving stays locked");
		} else if (buttonName.startsWith("btn_admin_remove_")) {
			userManager.removeAdmin(Long.parseLong(buttonName.split("_")[3]));
			sender.sendEditedMessage(chat_id, message_id, "Admin removed");
		} else if (buttonName.startsWith("btn_abort")) {
			sender.sendEditedMessage(chat_id, message_id, "Aborted");
		} else if (buttonName.startsWith("btn_dish_")) {
			String washer = buttonName.replace("btn_dish_", "");

			String response=timer.toggleTimer(washer, washerStates.get(washer));

			sender.sendEditedMessage(chat_id, message_id, response);
		}
		if (adminButton) {
			sender.sendMessage(message, userChatID);
			sender.sendEditedMessage(chat_id, message_id, editedManaged);
		}
	}

	public void displayRemoveAdmin(Update update, HashMap<Long, User> admins) {

		SendMessage message = new SendMessage() // Create a message object object
				.setChatId(update.getMessage().getChatId()).setText("Who you want to remove as Admin?");
		message.setParseMode("markdown");
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<List<InlineKeyboardButton>>();
		List<InlineKeyboardButton> rowInline = new ArrayList<InlineKeyboardButton>();

		int counter = 0;

		for (User u : admins.values()) {

			rowInline.add(new InlineKeyboardButton().setText(u.name).setCallbackData("btn_admin_remove_" + u.id));

			counter++;
			if (counter > 1) {
				rowsInline.add(rowInline);
				rowInline = new ArrayList<InlineKeyboardButton>();
				counter = 0;
			}
		}

		if (admins.values().size() % 2 == 1) {

			rowInline.add(new InlineKeyboardButton().setText("-").setCallbackData("btn_abort"));
			rowsInline.add(rowInline);
			rowInline = new ArrayList<InlineKeyboardButton>();
			rowInline.add(new InlineKeyboardButton().setText("Abort").setCallbackData("btn_abort"));

			rowsInline.add(rowInline);

		} else {
			rowInline = new ArrayList<InlineKeyboardButton>();
			rowInline.add(new InlineKeyboardButton().setText("Abort").setCallbackData("btn_abort"));

			rowsInline.add(rowInline);

		}
		// Add it to the message
		markupInline.setKeyboard(rowsInline);
		message.setReplyMarkup(markupInline);
		try {
			bot.execute(message); // Sending our message object to user
		} catch (TelegramApiException e) {
		}

	}

	public void displayAdminRequestMenu(String nameOfUser, long chatIDofUser) {

		SendMessage message = new SendMessage() // Create a message object object
				.setChatId(AnnouncerBot.SUPER_ADMIN).setText(nameOfUser + " wants to be admin. Accept the request?");
		message.setParseMode("markdown");
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<List<InlineKeyboardButton>>();
		List<InlineKeyboardButton> rowInline = new ArrayList<InlineKeyboardButton>();

		rowInline.add(new InlineKeyboardButton().setText("Yes")
				.setCallbackData("btn_admin_Yes_" + chatIDofUser + "_" + nameOfUser));
		rowInline.add(new InlineKeyboardButton().setText("No").setCallbackData("btn_admin_No_" + chatIDofUser));

		// Set the keyboard to the markup
		rowsInline.add(rowInline);
		// Add it to the message
		markupInline.setKeyboard(rowsInline);
		message.setReplyMarkup(markupInline);
		try {
			bot.execute(message); // Sending our message object to user
		} catch (TelegramApiException e) {
			MessageSender.logUnableToSendMessage(chatIDofUser);
		}
	}

	public void sendBroadcastPhoto(Update update) {

		String caption = update.getMessage().getCaption();
		if (caption != null && caption.startsWith("@")) {

			try {
				downloadAndSendPhoto(update, caption);

			} catch (IllegalArgumentException e) {
				sendAvailableGroups(update, caption);

			}
		} else {
			sender.sendMessage("The Photo had no or malformed destiny", update);
		}
	}

	public void sendAvailableGroups(Update update, String wrongGroup) {
		StringBuilder messageString = new StringBuilder();
		wrongGroup = wrongGroup.replaceAll("/", "").trim();
		sender.sendMessage("The group _" + wrongGroup + "_ does not exist\n", update);

		messageString.append("You may use one of these:\n");
		for (TelegramList tl : TelegramList.values()) {
			messageString.append(tl + "\n");
		}
		sender.sendMessage(messageString.toString(), update);

	}

	private void downloadAndSendPhoto(Update update, String caption) {
		File downloadedFile;
		caption = caption.substring(1);
		HashSet<User> users = userManager.usersOnList(TelegramList.valueOf(caption.toUpperCase()));

		sender.sendMessage("Photo downloading to server", update);
		downloadedFile = downloadPhoto(update);
		sender.sendMessage("Done loading", update);
		for (User u : users) {
			sendPhoto(u.id, downloadedFile);
		}

		sender.sendMessage("Photo was sent to " + users.size() + " person(s)", update);
	}

	private void sendPhoto(long chatID, File file) {
		// Send
		SendPhoto sendPhotoRequest = new SendPhoto();
		sendPhotoRequest.setChatId(chatID);

		// path: String, photoName: String
		sendPhotoRequest.setPhoto(file); //
		try {
			bot.execute(sendPhotoRequest);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public File downloadPhoto(Update update) {
		PhotoSize photoSize = getPhoto(update);

		String path = getFilePath(photoSize);

		File file = downloadPhotoByFilePath(path);
		return file;
	}

	public PhotoSize getPhoto(Update update) {
		if (update.hasMessage() && update.getMessage().hasPhoto()) {
			List<PhotoSize> photos = update.getMessage().getPhoto();

			return photos.stream().max(Comparator.comparing(PhotoSize::getFileSize)).orElse(null);
		}

		return null;
	}

	public String getFilePath(PhotoSize photo) {
		Objects.requireNonNull(photo);

		if (photo.hasFilePath()) {
			return photo.getFilePath();
		} else {
			GetFile getFileMethod = new GetFile();
			getFileMethod.setFileId(photo.getFileId());

			try {
				org.telegram.telegrambots.meta.api.objects.File file = bot.execute(getFileMethod);
				return file.getFilePath();
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public java.io.File downloadPhotoByFilePath(String filePath) {
		try {
			return bot.downloadFile(filePath);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void displaySaveConfirm() {

		SendMessage message = new SendMessage() // Create a message object object
				.setChatId(AnnouncerBot.SUPER_ADMIN).setText("Failed to load users, enable writing anyway?");
		message.setParseMode("markdown");
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<List<InlineKeyboardButton>>();
		List<InlineKeyboardButton> rowInline = new ArrayList<InlineKeyboardButton>();

		rowInline.add(new InlineKeyboardButton().setText("Yes").setCallbackData("btn_save_Yes"));
		rowInline.add(new InlineKeyboardButton().setText("No").setCallbackData("btn_save_No"));

		// Set the keyboard to the markup
		rowsInline.add(rowInline);
		// Add it to the message
		markupInline.setKeyboard(rowsInline);
		message.setReplyMarkup(markupInline);
		try {
			bot.execute(message); // Sending our message object to user
		} catch (TelegramApiException e) {

		}
	}

	public void displayDishwasher(long chatID) {

		SendMessage message = new SendMessage() // Create a message object object
				.setChatId(chatID).setText("Welcher Geschirrreinigungsapparat?");
		message.setParseMode("markdown");
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<List<InlineKeyboardButton>>();
		List<InlineKeyboardButton> rowInline = new ArrayList<InlineKeyboardButton>();

		rowInline.add(new InlineKeyboardButton().setText("Asterix").setCallbackData("btn_dish_asterix"));
		rowInline.add(new InlineKeyboardButton().setText("Obelix").setCallbackData("btn_dish_obelix"));
		rowInline.add(new InlineKeyboardButton().setText("Idefix").setCallbackData("btn_dish_idefix"));
		rowInline.add(new InlineKeyboardButton().setText("Miraculix").setCallbackData("btn_dish_miraculix"));
		// Set the keyboard to the markup
		rowsInline.add(rowInline);
		
		rowInline = new ArrayList<InlineKeyboardButton>();
		
		rowInline.add(new InlineKeyboardButton().setText("Tick").setCallbackData("btn_dish_tick"));
		rowInline.add(new InlineKeyboardButton().setText("Trick").setCallbackData("btn_dish_trick"));
		rowInline.add(new InlineKeyboardButton().setText("Track").setCallbackData("btn_dish_track"));
		rowInline.add(new InlineKeyboardButton().setText("Donald").setCallbackData("btn_dish_donald"));

		// Set the keyboard to the markup
		rowsInline.add(rowInline);

		// Add it to the message
		markupInline.setKeyboard(rowsInline);
		message.setReplyMarkup(markupInline);
		try {
			bot.execute(message); // Sending our message object to user
		} catch (TelegramApiException e) {

		}
	}

	public void reactOnInfoRequest(Update update, String message) {

		long chatID = update.getMessage().getChatId();

		if (message.toLowerCase().startsWith("/info") || message.equals("ls")) {
			boolean noMessages = true;
			User user = userManager.getUserByID(chatID);
			if (user != null) {
				for (TelegramList list : user.lists) {
					if (TelegramList.groupInfo.containsKey(list)) {
						noMessages = false;
						StringBuilder stringBuilder = new StringBuilder();
						addListInfo(stringBuilder, list);

						sender.sendMessage(stringBuilder.toString(), update);
					}
				}
				if (noMessages) {
					sender.sendMessage("There are currently no news", update);
				}
			} else {
				sender.sendMessage("You not part of any group", update);
			}
		}

	}

	private void addListInfo(StringBuilder stringBuilder, TelegramList list) {
		stringBuilder.append("*@");
		stringBuilder.append(list);
		stringBuilder.append("*	\n");
		stringBuilder.append(TelegramList.groupInfo.get(list));
		stringBuilder.append("\n\n");
	}

	public void addInfo(Update update, String message) {
		try {
			String[] splittedMessage = message.trim().split(" ");
			TelegramList list = TelegramList.valueOf(splittedMessage[0].toUpperCase());
			String newMessage = message.replaceAll("/add", "");
			newMessage = newMessage.trim();
			newMessage = newMessage.substring(list.toString().length());
			newMessage = newMessage.trim();
			String oldValue = TelegramList.groupInfo.put(list, newMessage);
			if (oldValue == null) {
				sender.sendMessage("Message added", update);
			} else {
				sender.sendMessage("Replaced message:\n" + oldValue, update);
			}
		} catch (IllegalArgumentException e) {
			sender.sendMessage("The group does not exist", update);
		}
	}

	public void removeInfo(Update update, String message) {
		try {
			TelegramList list = TelegramList.valueOf(message.trim().toUpperCase());
			TelegramList.groupInfo.remove(list);
			sender.sendMessage("Removed message from: " + list, update);
		} catch (IllegalArgumentException e) {
			sender.sendMessage("The group does not exist", update);

		}
	}

	public void reactOnGroupInfoRequest(Update update, String message) {

		if (message.toLowerCase().startsWith("/groups")) {
			StringBuilder stringBuilder = new StringBuilder();
			TelegramList[] groups = TelegramList.values();
			stringBuilder.append("These groups are available:\n");
			for (TelegramList list : groups) {
				stringBuilder.append(list);
				stringBuilder.append("\n");
			}
			sender.sendMessage(stringBuilder.toString(), update);
		}

	}

	public void sendWelcomeMessage(Update update, String message) {
		if (message.toLowerCase().trim().startsWith("/start")) {
			reactOnCommandsRequest(update, "/commands");
			sender.sendMessage("Your Chat-ID is: " + update.getMessage().getChatId(), update);
		}
	}

	public void reactOnCommandsRequest(Update update, String message) {

		if (message.toLowerCase().startsWith("/commands") || message.toLowerCase().startsWith("/help")) {
			StringBuilder stringBuilder = new StringBuilder();
			getCommands(update, stringBuilder);

			sender.sendMessage(stringBuilder.toString(), update);
		}

	}

	private void getCommands(Update update, StringBuilder stringBuilder) {
		stringBuilder.append("These commands are available:\n");
		stringBuilder.append("/sub<GROUPNAME> - Join group\n");
		stringBuilder.append("/unsub<GROUPNAME> - Leave group\n");
		stringBuilder.append("/commands - Listing the commands\n");
		stringBuilder.append("/info - Shows news of joined groups\n");
		stringBuilder.append("/groups - Shows all available groups\n");

		if (userManager.isAdmin(update.getMessage().getChatId())) {
			stringBuilder.append("\n");
			stringBuilder.append("Admin commands:\n");
			stringBuilder.append("@<GROUPNAME>\n<MESSAGE> - Sends a message to a group\n\n");
			stringBuilder.append("@<GROUPNAME> - As caption of a picture (will send the picture to the group)\n\n");
			stringBuilder.append("/add <GROUPNAME> <INFO> - Adds news to a group\n\n");
			stringBuilder.append("/remove <GROUPNAME> - Removes news from a group\n\n");
			stringBuilder.append("/groupCount <GROUPNAME> - Shows how many members a group has\n\n");
			stringBuilder.append("/chatID - Shows your Chat-ID");
			stringBuilder.append("/removeAdmin - To remove an admin\n");
		}
	}

	public void showChatID(Update update) {
		sender.sendMessage(update.getMessage().getChatId().toString(), update);

	}

	public void reactOnDish(Update update) {
		String message = update.getMessage().getText();
		if (message.toLowerCase().startsWith("/dish")) {
			// message = message.replace("/dish", "");
			// message = message.trim();
			// timer.toggleTimer(message);
			displayDishwasher(update.getMessage().getChatId());
		}
	}

}
