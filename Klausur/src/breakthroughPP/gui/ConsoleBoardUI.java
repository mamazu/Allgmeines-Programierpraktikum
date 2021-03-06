package breakthroughPP.gui;

import breakthroughPP.SplinterTheOmniscientRat;
import breakthroughPP.map.Board;
import breakthroughPP.preset.*;

import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Basic IO for console
 *
 * @author faulersack
 */
public class ConsoleBoardUI implements Requestable {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static final String ANSI38_GRAY = "\u001B[38;5;237m";
    public static final String ANSI38_RED = "\u001B[38;5;196m";
    private static final char[] alphabet;
    private static final boolean toupper, NWIN;
    private static final Pattern pat;

    static {
        //Not windows
        NWIN = !System.getProperty("os.name").toLowerCase().contains("win");
        //expecting alphabet to be sorted
        alphabet = Position.getAlphabet().toCharArray();
        toupper = isUpperCase(alphabet);
        //String posp = "(["+Pattern.quote(Position.getAlphabet())+"])\\s?([0-9]{1,"+(int)Math.ceil(Math.log10(alphabet.length))+"})";
        String posp = "([" + Pattern.quote(Position.getAlphabet()) + "])\\s?([0-9]{1,2})";
        pat = Pattern.compile("\\A" + posp + "(?:\\s+(?:TO|to)\\s+||\\s+|\\s?>\\s?)" + posp + "\\z");
        //System.out.println("\\A"+posp+"(?: to | |\\s?>\\s?)"+posp+"\\z");
    }

    private Viewer viewer;
    private int width, height;
    private boolean sep;
    private String newline;

    /**
     * Initialize ConsoleBoardUI
     *
     * @param viewer Viewer Object to use
     */
    public ConsoleBoardUI(Viewer viewer) {
        this(viewer, false);
    }

    /**
     * Initialize ConsoleBoardUI
     *
     * @param viewer     Viewer Object to use
     * @param doubleLine Whether or not to separate the boards lines with an additional one
     */
    public ConsoleBoardUI(Viewer viewer, boolean doubleLine) {
        this.viewer = viewer;
        sep = doubleLine;
        width = viewer.getDimX();
        height = viewer.getDimY();
        if (sep) newline = "---" + String.format("%0" + width + "d", 0).replace("0", "--");
        else newline = null;
    }

    /**
     * Check if all characters are uppercase
     *
     * @param s Char-Array to validate
     * @return whether is completely uppercase
     */
    public static boolean isUpperCase(char[] s) {
        for (char c : s) {
            if (!Character.isUpperCase(c))
                return false;
        }
        return true;
    }

    /**
     * Request viewer-supplieds current user to make move via console
     *
     * @return Move the user chose, null when aborted
     */
    public Move deliver() throws PresetException {
        return deliver(viewer.turn());
    }

    /**
     * Request user to make move via console
     *
     * @param playerColor The color of the player to make move
     * @return Move the user chose, null when aborted
     */
    public Move deliver(int playerColor) throws PresetException {
        draw();
        {//check status
            int status;
            try {
                status = viewer.getStatus().getStatus();
            } catch (Exception e) {
                status = Setting.ILLEGAL;
            }
            if (status != Setting.OK) {
                System.out.println("No stones can be moved on this board");
                return null;
            }
        }
        if (playerColor != Setting.RED && playerColor != Setting.BLUE)
            throw new PresetException("playerColor was neither RED nor BLUE");
        Scanner scanner = new Scanner(System.in);
        char[] alphabet = Position.getAlphabet().toCharArray();
        if (NWIN) System.out.print("Enter positions to make a " + (playerColor == Setting.RED ?
                ANSI38_RED + "RED" + ANSI_RESET : ANSI_BLUE + "BLUE" + ANSI_RESET) + " move (eg 'A1 B2', 'Z 3 to Z 2')\n> ");
        else System.out.print("Enter positions to make a " + (playerColor == Setting.RED ?
                "RED" : "BLUE") + " move (eg 'A1 B2', 'Z 3 to Z 2')\n> ");
        String ms;
        Matcher match;
        int ENEMY = playerColor == Board.MOVESUP ? Board.MOVESDOWN : Board.MOVESUP;
        do {
            try {
                if (toupper)
                    ms = scanner.nextLine().toUpperCase();
                else ms = scanner.nextLine();
            } catch (Exception e) {
                if (SplinterTheOmniscientRat.DEBUG)
                    e.printStackTrace();
                System.err.println("Error: Scanner error. Is STDIN still alive?");
                return null;
            }
            if (ms.equals("") || ms.equals("X") || ms.equals("x"))
                return null;
            match = pat.matcher(ms);
            if (match.find() && match.groupCount() == 4) {
                //WARN binarySearch, alphabet required to be pre-sorted
                int from_l = Arrays.binarySearch(alphabet, match.group(1).toCharArray()[0]) + 1,
                        from_n = Integer.valueOf(match.group(2)),
                        to_l = Arrays.binarySearch(alphabet, match.group(3).toCharArray()[0]) + 1,
                        to_n = Integer.valueOf(match.group(4));
                if (SplinterTheOmniscientRat.DEBUG)
                    System.out.println("DEBUG: " + ms + " ==> " + from_l + "|" + from_n + "->" + to_l + "|" + to_n);
                //check move for validity
                if (viewer.getColorAt(from_l, from_n) == playerColor &&
                        from_l >= 1 && from_l <= width && to_l >= 1 && to_l <= width &&
                        ((playerColor == Board.MOVESUP && (from_n + 1 == to_n &&
                                from_n >= 1 && from_n < height && to_n > 1 && to_n <= height
                        )) ||
                                (playerColor == Board.MOVESDOWN && (from_n - 1 == to_n &&
                                        from_n > 1 && from_n <= height && to_n >= 1 && to_n < height
                                )))
                        && (from_l == to_l || from_l + 1 == to_l || from_l - 1 == to_l)
                        && viewer.getColorAt(from_l, from_n) != viewer.getColorAt(to_l, to_n)
                        && (!(viewer.getColorAt(to_l, to_n) == ENEMY && from_l == to_l)))
                    return new Move(new Position(from_l - 1, from_n - 1), new Position(to_l - 1, to_n - 1));
            }
            System.out.print("Invalid move. Please try again, enter 'x' or '' (without quotes) to abort\n> ");
        } while (true);
    }

    /**
     * Print game board to console
     */
    public void draw() {
        /*if (NWIN) System.out.print(CLEARSCREEN);
        else*/
        System.out.print("\n\n\n");
        System.out.println("   ___     _____                      ___  ______");
        System.out.println("  / _ |   / ___/__ ___ _  ___   ___  / _/ / __/ /____  ___  ___ ___");
        System.out.println(" / __ |  / (_ / _ `/  ' \\/ -_) / _ \\/ _/ _\\ \\/ __/ _ \\/ _ \\/ -_|_-<");
        System.out.println("/_/ |_|  \\___/\\_,_/_/_/_/\\__/  \\___/_/  /___/\\__/\\___/_//_/\\__/___/\n");

        System.out.print("  ");
        int i;
        for (i = 0; i < width; i++)
            System.out.print("|" + alphabet[i]);
        System.out.print("\n");
        for (i = 0; i < height; i++) {
            if (sep)
                System.out.println(newline);
            System.out.printf("%02d", height - i);
            for (int j = 0; j < width; j++) {
                switch (viewer.getRawAtXY(j, i)) {
                    case Setting.RED:
                        if (NWIN) System.out.print("|" + ANSI38_RED + "R" + ANSI_RESET);
                        else System.out.print("|R");
                        break;
                    case Setting.BLUE:
                        if (NWIN) System.out.print("|" + ANSI_BLUE + "B" + ANSI_RESET);
                        else System.out.print("|B");
                        break;
                    case Setting.WHITE:
                        if (NWIN) System.out.print("|" + ANSI_WHITE + "W" + ANSI_RESET);
                        else System.out.print("|W");
                        break;
                    case Setting.GRAY:
                        if (NWIN) System.out.print("|" + ANSI38_GRAY + "G" + ANSI_RESET);
                        else System.out.print("|G");
                        break;
                    default:
                        System.out.print("| ");
                        break;
                }
            }
            System.out.print("\n");
        }
        System.out.print("\n");
        int status;
        try {
            status = viewer.getStatus().getStatus();
        } catch (Exception e) {
            status = Setting.ILLEGAL;
        }
        switch (status) {
            case Setting.OK:
                if (viewer.turn() == Setting.RED) {
                    if (NWIN) System.out.println(ANSI_RED + "RED" + ANSI_RESET + " is next");
                    else System.out.println("RED is next");
                } else {
                    if (NWIN) System.out.println(ANSI_BLUE + "BLUE" + ANSI_RESET + " is next");
                    else System.out.println("BLUE is next");
                }
                break;
            case Setting.RED_WIN:
                if (NWIN) System.out.println(ANSI_RED + "RED" + ANSI_RESET + " won");
                else System.out.println("RED won");
                break;
            case Setting.BLUE_WIN:
                if (NWIN) System.out.println(ANSI_BLUE + "BLUE" + ANSI_RESET + " won");
                else System.out.println("BLUE won");
                break;
            default:
                System.out.println("The game encountered an error: " + Setting.statusString[status]);
        }
        System.out.print("\n");
    }
}
