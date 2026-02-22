package shared;

/**
 * This class defines the "Language" of the game.
 * It formats data into Strings so it can travel over the network.
 */
public class PacketProtocol {

    // 1. FORMATTING METHODS (Used by the Sender)

    // Formats: MOVE|PLAYER_ID|X|Y
    public static String formatMove(int id, int x, int y) {
        return "MOVE|" + id + "|" + x + "|" + y;
    }

    // Formats: KEY_FOUND|PLAYER_ID|REAL_DOOR_INDEX
    public static String formatKeyFound(int id, int doorIndex) {
        return "KEY_FOUND|" + id + "|" + doorIndex;
    }

    // Formats: GHOST_SCARE|PLAYER_ID
    public static String formatGhostScare(int id) {
        return "GHOST_SCARE|" + id;
    }

    // 2. PARSING METHOD (Used by the Receiver)

    /**
     * Takes a String like "MOVE|1|120|200" and splits it
     * into an array: ["MOVE", "1", "120", "200"]
     */
    public static String[] parse(String packet) {
        if (packet == null || packet.isEmpty()) {
            return new String[0];
        }
        // We use \\| because the pipe symbol is a special character in Java Regex
        return packet.split("\\|");
    }
}