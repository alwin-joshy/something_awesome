public class Common {

        // Erases the terminal
    public static void clearTerminal(){
        System.out.print("\033[H\033[2J"); //Found here https://www.reddit.com/r/java/comments/1uuvqo/clear_terminal_screen_linux/
    }

    // Makes a nice looking banner
    public static void fancyBanner(String msg) {
        for (int i = 0; i < msg.length() + 4; i++) {
            System.out.print("~");
        }
        System.out.println();
        System.out.println("~ " + msg + " ~");
        for (int i = 0; i < msg.length() + 4; i++) {
            System.out.print("~");
        }
        System.out.println("\n");

    }

    public static void printDivider() {
        System.out.println("----------------------------------");
    }
    
}
