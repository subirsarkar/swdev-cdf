package config.util;

public class SvtCommand {
  public static void main(String [] argv) {
    if (argv.length < 2 || argv[0].equals("-h") || argv[0].equals("-help")) {
      System.out.println("Usage: svtcom <destination> <command> [<flag>]");
    }
    else {
      String command = argv[0];
      String dest    = argv[1];
      if (argv.length > 2) {
        int flag = Integer.parseInt(argv[2]);
        Tools.sendCommand(command, dest, flag);
      }
      else {
        Tools.sendCommand(command, dest);
      }
    }
  }
}
