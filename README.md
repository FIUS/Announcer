# Telegram-Announcer
## What is this repository about?
The repository provides you with the ability to host a group broadcast telegram service.
You can create groups which users can subscribe and admins can send messages or pictures to all user who subscribed the group.

## Installation
  1. Clone git repository
  2. Add all groups to the `TelegramList.java` file, (by adding Enum values, **values needs to be in caps**)
  2. Build a docker image by running `sudo docker build -t <name> .`
  3. Create `credential.conf` by writing in the first line the telegram bot name, in the second line the bot token and in the
  third line the chatID of the person who will be SUPER_ADMIN. You can find out your chatID as described below in the section usage.
  For first usage type 0 in the third line. You can run the bot and build it again with the chatID after you find out.
  4. Store `credential.conf` file in the directory which will be used as volume. The config file has to has the path `/data/credential.conf` in the container itself (which will be achieved when following step 5).
  5. Store a `config.conf` file in the same directory as `credential.conf` and instert in the first line an integer representing the time in minutes
  6. Start the docker container from the just created image. A volume is required for storing the user data permanently
  and loading the telegram api token, username and SUPER_ADMIN.
  The data in the container needs to be in the directory `/data/`. To achive this run `sudo docker run -i -t -v <PathInOs>:/data <name>`

## Usage
### Commands
- User commands:
  - `/sub<GROUPNAME>` - Join group
  - `/unsub<GROUPNAME>` - Leave group
  - `/commands` - Listing the commands
  - `/info` - Shows news of joined groups
  - `/groups` - Shows all available groups
- Admin commands:
  - `@<GROUPNAME>\n<MESSAGE>` - Sends a message to a group
  - `@<GROUPNAME>` - As caption of a picture (will send the picture to the group)
  - `/add <GROUPNAME> <INFO>` - Adds news to a group
  - `/remove <GROUPNAME>` - Removes news from a group
  - `/groupCount <GROUPNAME>` - Shows how many members a group has
  - `/chatID` - Shows your Chat-ID
  - `/removeAdmin` - To remove an admin
	
### Admins
To become an admin a user can type `/adminRequest`, then the SUPER_ADMIN needs to accept them.
Admins can remove other admins by typing `/removeAdmin`(Note: The SUPER_ADMIN is always implicit admin, but can also be added
to the admin list and also be remove from there).

### Sending broadcasts to groups
To send a **message** to all members of a group an admin just needs to type:

`@<groupname>` in the first line and the acutal message is starting from the second line (Note: The command will only be recognized
if in the first line are no other character than the command `@<groupname>` itself).

To send a **photo** to all members of a group an admin just needs to do:

Send a picture with caption `@<groupname>` to the bot.

### Libraries used
[rubenlagus/TelegramBots](https://github.com/rubenlagus/TelegramBots)
