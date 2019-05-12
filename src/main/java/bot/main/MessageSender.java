/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bot.main;

import static java.lang.Math.toIntExact;


import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 *
 * This class handles the sending of all messages
 *
 */
public class MessageSender {

    private final TelegramLongPollingBot bot;

    public MessageSender(TelegramLongPollingBot bot) {
        this.bot = bot;
    }

    /**
     *
     * Replaces buttons with specified text
     *
     * @param chat_id The ID of the chat where the button should be replaced
     * @param message_id The ID of the message from the buttons
     * @param answer The message you want to replace the buttons with
     */
    public void sendEditedMessage(long chat_id, long message_id, String answer) {
        EditMessageText new_message = new EditMessageText().setChatId(chat_id).setMessageId(toIntExact(message_id))
                .setText(answer);
        new_message.setParseMode("markdown");

        try {
            bot.execute(new_message);
        } catch (TelegramApiException e) {
            //logUnableToSendMessage(chat_id);
        }
    }

    /**
     *
     * Sends a Message to the user who is contained in the update
     *
     * @param message The message to deliver
     * @param update The idientification of the user
     */
    public void sendMessage(String message, Update update) {
        sendMessage(message, update.getMessage().getChatId());
    }

    /**
     *
     * Sends a Message to the user whos chat id is passed
     *
     * @param message The message to deliver
     * @param chat_id The idientification of the user
     */
    public void sendMessage(String message, long chat_id) {
        SendMessage sm = new SendMessage() // Create a SendMessage object with mandatory fields
                .setChatId(chat_id).setText(message);
        sm.setParseMode("markdown");
        try {
            bot.execute(sm); // Call method to send the message
        } catch (TelegramApiException e) {
            //logUnableToSendMessage(chat_id);
        }
    }

    /**
     *
     * Add a log the the logfile saying: Unable to send a message (ChatID =
     * <chatID>)")
     *
     * @param chatID The chatID to which the message could ne be delivered
     */
    public static void logUnableToSendMessage(long chatID) {
        //Logging.log(LogType.ERROR, "Unable to send a message (ChatID = " + chatID + ")");
    }
}