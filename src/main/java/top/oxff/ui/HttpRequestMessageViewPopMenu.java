package top.oxff.ui;

import burp.api.montoya.http.message.HttpRequestResponse;
import top.oxff.model.SelectionPositionType;
import top.oxff.tools.HttpTools;

import javax.swing.*;
import java.util.List;

public class HttpRequestMessageViewPopMenu {
    HttpRequestResponse httpRequestResponse;
    int start;
    int end;

    HttpRequestMessageViewPopMenu(HttpRequestResponse httpRequestResponse, int start, int end) {
        this.httpRequestResponse = httpRequestResponse;
        this.start = start;
        this.end = end;
    }

    public List<JMenu> genMenus() {
        SelectionPositionType selectionPositionType = HttpTools.getSelectHttpRequestTextPositionType(httpRequestResponse, start, end);
        return switch (selectionPositionType) {
            case HEADER_FIRST_LINE, SUB_HEADER_FIRST_LINE -> List.of(new JMenu("Header First Line"));
            case HEADER_OTHER_LINES -> List.of(new JMenu("Header Other Lines"));
            case BODY, IN_BODY -> List.of(new JMenu("Body"));
            default -> List.of(new JMenu("None"));
        };
    }
}
