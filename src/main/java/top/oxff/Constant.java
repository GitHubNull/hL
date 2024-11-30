package top.oxff;

import burp.api.montoya.core.HighlightColor;

import java.awt.*;
import java.util.Map;

public class Constant {
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

    public final static int MINI_SELECT_STRING_LENGTH = 5;
}
