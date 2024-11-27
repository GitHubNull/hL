package top.oxff;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.logging.Logging;
import top.oxff.ui.PopMenu;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HighLighter  implements BurpExtension {
    private static final String EXTENSION_NAME = "HighLighter";
    private static final String EXTENSION_VERSION = "1.0";
    private static final String EXTENSION_DESCRIPTION = "high light the request";
    private static final String EXTENSION_AUTHOR = "oxff";
    private static final String EXTENSION_LICENSE = "MIT";

    public static MontoyaApi api;
    public static Logging logger;

    /**
     * Enum Class HighlightColor
     *
     * BLUE
     * CYAN
     * GRAY
     * GREEN
     * MAGENTA
     * NONE
     * ORANGE
     * PINK
     * RED
     * YELLOW
     *
     */

    public final static Map<Color, String> COLOR_STRING_MAP = Map.ofEntries(
            Map.entry(Color.BLUE, "BLUE"),
            Map.entry(Color.CYAN, "CYAN"),
            Map.entry(Color.GRAY, "GRAY"),
            Map.entry(Color.GREEN, "GREEN"),
            Map.entry(Color.MAGENTA, "MAGENTA"),
            Map.entry(Color.WHITE, "NONE"),
            Map.entry(Color.ORANGE, "ORANGE"),
            Map.entry(Color.PINK, "PINK"),
            Map.entry(Color.RED, "RED"),
            Map.entry(Color.YELLOW, "YELLOW")
    );

    public final static Map<Color, HighlightColor> COLOR_TO_HIGHLIGHT_COLOR_MAP = Map.ofEntries(
            Map.entry(Color.BLUE, HighlightColor.BLUE),
            Map.entry(Color.CYAN, HighlightColor.CYAN),
            Map.entry(Color.GRAY, HighlightColor.GRAY),
            Map.entry(Color.GREEN, HighlightColor.GREEN),
            Map.entry(Color.MAGENTA, HighlightColor.MAGENTA),
            Map.entry(Color.WHITE, HighlightColor.NONE),
            Map.entry(Color.ORANGE, HighlightColor.ORANGE),
            Map.entry(Color.PINK, HighlightColor.PINK),
            Map.entry(Color.RED, HighlightColor.RED),
            Map.entry(Color.YELLOW, HighlightColor.YELLOW)
    );
    public static Map<HighlightColor, Color> HIGHLIGHT_COLOR_TO_COLOR_MAP = Map.ofEntries(
            Map.entry(HighlightColor.BLUE, Color.BLUE),
            Map.entry(HighlightColor.CYAN, Color.CYAN),
            Map.entry(HighlightColor.GRAY, Color.GRAY),
            Map.entry(HighlightColor.GREEN, Color.GREEN),
            Map.entry(HighlightColor.MAGENTA, Color.MAGENTA),
            Map.entry(HighlightColor.NONE, Color.WHITE),
            Map.entry(HighlightColor.ORANGE, Color.ORANGE),
            Map.entry(HighlightColor.PINK, Color.PINK),
            Map.entry(HighlightColor.RED, Color.RED),
            Map.entry(HighlightColor.YELLOW, Color.YELLOW)
    );
    public static Set<String> requestHighlightKey;
    public static Map<String, HighlightColor> requestHighlightColor;

    @Override
    public void initialize(MontoyaApi montoyaApi) {
        api = montoyaApi;
        logger = montoyaApi.logging();
        montoyaApi.extension().setName(EXTENSION_NAME);
        requestHighlightKey = new HashSet<>();
        requestHighlightColor = new HashMap<>();

        logger.logToOutput("HttpMocker loaded");
        logger.logToOutput("Version: " + EXTENSION_VERSION);
        logger.logToOutput("Author: " + EXTENSION_AUTHOR);
        logger.logToOutput("License: " + EXTENSION_LICENSE);
        logger.logToOutput("Description: " + EXTENSION_DESCRIPTION);

        api.userInterface().registerContextMenuItemsProvider(new PopMenu(api));
    }

    public static void main(String[] args) {
        System.out.printf("color: " + COLOR_TO_HIGHLIGHT_COLOR_MAP.get(Color.RED));
    }
}
