package bot.main;

import java.util.ArrayList;
import java.util.HashMap;

public enum TelegramList {
    SPIELE, FIUSINTERN, ALLGEMEIN, SIFF;

    public static ArrayList<String> getAllStrings(String appending) {
        ArrayList<String> output = new ArrayList<String>();
        for (TelegramList temp : TelegramList.values()) {
            output.add("/" + appending + temp);
        }

        return output;
    }

    public static final HashMap<TelegramList, String> groupInfo = new HashMap<TelegramList, String>();
}
