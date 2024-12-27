package top.oxff.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.logging.Logging;
import top.oxff.model.SelectionPositionType;
import top.oxff.tools.HttpTools;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class HttpRequestMessageViewPopMenu {
    HttpRequestResponse httpRequestResponse;
    int start;
    int end;
    private final MontoyaApi api;
    private final Logging logger;

    HttpRequestMessageViewPopMenu(MontoyaApi api, HttpRequestResponse httpRequestResponse, int start, int end) {
        this.httpRequestResponse = httpRequestResponse;
        this.start = start;
        this.end = end;
        this.api = api;
        this.logger = api.logging();
    }

    public List<JMenu> genMenus(List<Component> menuItemList) {
        SelectionPositionType selectionPositionType = HttpTools.getSelectHttpRequestTextPositionType(httpRequestResponse, start, end);
        return switch (selectionPositionType) {
            case HEADER_FIRST_LINE, SUB_HEADER_FIRST_LINE -> List.of(new JMenu("Header First Line"));
            case HEADER_OTHER_LINES -> List.of(new JMenu("Header Other Lines"));
            case BODY, IN_BODY -> List.of(new JMenu("Body"));
            default -> List.of(new JMenu("None"));
        };
    }
}
