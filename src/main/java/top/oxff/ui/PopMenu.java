package top.oxff.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.core.Range;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.proxy.ProxyHttpRequestResponse;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.MessageEditorHttpRequestResponse;
import top.oxff.HighLighter;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static top.oxff.HighLighter.COLOR_STRING_MAP;

public class PopMenu  implements ContextMenuItemsProvider {
    private final MontoyaApi api;
    private final Logging logger;

    public PopMenu(MontoyaApi api) {
        this.api = api;
        logger = api.logging();

    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        logger.logToOutput("PopMenu");
        if (event.isFromTool(ToolType.PROXY)){
            return proxyMenu(event);
        } else if (event.isFromTool(ToolType.REPEATER)) {
            return repeaterMenu(event);
        } else if (event.isFromTool(ToolType.LOGGER)) {

        } else if (event.isFromTool(ToolType.INTRUDER)) {

        }else {
            return null;
        }
        return null;
    }

    private List<Component> proxyMenu(ContextMenuEvent event) {
        logger.logToOutput("proxyMenu()...");
        List<Component> menuItemList = new ArrayList<>();
        List<HttpRequestResponse> selectededRequestResponseList = event.selectedRequestResponses();
        if (selectededRequestResponseList.isEmpty()){
            logger.logToOutput("未选择");
            JMenuItem removeAllHighLightItem = new JMenuItem("取消所有高亮");
            removeAllHighLightItem.addActionListener(e -> {
                SwingUtilities.invokeLater(() -> {
                    List<ProxyHttpRequestResponse> proxyHttpRequestResponseList = api.proxy().history();
                    for (ProxyHttpRequestResponse proxyHttpRequestResponse : proxyHttpRequestResponseList) {
                        proxyHttpRequestResponse.annotations().setHighlightColor(HighlightColor.NONE);
                    }
                    HighLighter.requestHighlightKey.clear();
                });
            });
            menuItemList.add(removeAllHighLightItem);
            return menuItemList;
        }


        JMenu addHighLightMenu = new JMenu("添加高亮");
        JMenu removeHighLightMenu = new JMenu("取消高亮");

        addHighLight(event, selectededRequestResponseList, addHighLightMenu);
        removeHighLight(event, selectededRequestResponseList, removeHighLightMenu);

        menuItemList.add(addHighLightMenu);
        menuItemList.add(removeHighLightMenu);

        return menuItemList;
    }

    private List<Component> repeaterMenu(ContextMenuEvent event) {
        logger.logToOutput("repeaterMenu()...");
        MessageEditorHttpRequestResponse.SelectionContext selectionContext = event.messageEditorRequestResponse().get().selectionContext();
        if (!(event.messageEditorRequestResponse().isPresent() && event.messageEditorRequestResponse().get().selectionOffsets().isPresent()) ||
        !selectionContext.equals(MessageEditorHttpRequestResponse.SelectionContext.REQUEST)){
            return null;
        }

        List<Component> menuItemList = new ArrayList<>();
        Range selectionOffsets = event.messageEditorRequestResponse().get().selectionOffsets().get();
        int start = selectionOffsets.startIndexInclusive();
        int end = selectionOffsets.endIndexExclusive();
        String path = event.messageEditorRequestResponse().get().requestResponse().request().pathWithoutQuery();
        String selectText;
        if (end > start){
            selectText = event.messageEditorRequestResponse().get().requestResponse().request().toString().substring(start, end);
        } else {
            selectText = null;
        }

        JMenu addSubHighLightMenu = new JMenu("添加高亮");
        JMenu addSelectedHighLightMenu = new JMenu("添加选中高亮");

        HighLighter.COLOR_TO_HIGHLIGHT_COLOR_MAP.forEach((color, highlightColor) -> {
            JMenuItem addHighLightItem = new JMenuItem(COLOR_STRING_MAP.get(color));
            addHighLightItem.setBackground(color);
            addHighLightItem.setOpaque(true);
            addHighLightItem.addActionListener(e -> {
                SwingUtilities.invokeLater(() -> {
                    List<ProxyHttpRequestResponse> proxyHttpRequestResponseList = api.proxy().history();
                    for (ProxyHttpRequestResponse proxyHttpRequestResponse : proxyHttpRequestResponseList) {
                        String requestPath = proxyHttpRequestResponse.request().pathWithoutQuery();
                        if (requestPath.equals(path)){
                            proxyHttpRequestResponse.annotations().setHighlightColor(highlightColor);
                        }
                    }
                    HighLighter.requestHighlightKey.add(path);
                    HighLighter.requestHighlightColor.put(path, highlightColor);
                });
            });
            addSubHighLightMenu.add(addHighLightItem);
        });

        if (selectText != null){
            HighLighter.COLOR_TO_HIGHLIGHT_COLOR_MAP.forEach((color, highlightColor) -> {
                JMenuItem addHighLightItem = new JMenuItem(COLOR_STRING_MAP.get(color));
                addHighLightItem.setBackground(color);
                addHighLightItem.setOpaque(true);
                addHighLightItem.addActionListener(e -> {
                    SwingUtilities.invokeLater(() -> {
                        List<ProxyHttpRequestResponse> proxyHttpRequestResponseList = api.proxy().history();
                        for (ProxyHttpRequestResponse proxyHttpRequestResponse : proxyHttpRequestResponseList) {
                            String requestPath = proxyHttpRequestResponse.request().pathWithoutQuery();
                            if (requestPath.contains(path)){
                                proxyHttpRequestResponse.annotations().setHighlightColor(highlightColor);
                            }
                        }
                        HighLighter.requestHighlightKey.add(selectText);
                        HighLighter.requestHighlightColor.put(selectText, highlightColor);
                    });
                });
                addSelectedHighLightMenu.add(addHighLightItem);
            });
        }

        menuItemList.add(addSubHighLightMenu);
        menuItemList.add(addSelectedHighLightMenu);
        return menuItemList;
    }

    private void addHighLight(ContextMenuEvent event, List<HttpRequestResponse> selectededRequestResponseList, JMenu addHighLightMenu){
        logger.logToOutput("addHighLight()...");
        if (1 == selectededRequestResponseList.size()){
            HttpRequestResponse requestResponse = selectededRequestResponseList.get(0);
            String path = requestResponse.request().pathWithoutQuery();
            if (HighLighter.requestHighlightKey.contains(path)){
                logger.logToOutput("已存在");
                return;
            }
            JMenu addSubHighLightMenu = new JMenu("添加高亮");
            HighLighter.COLOR_TO_HIGHLIGHT_COLOR_MAP.forEach((color, highlightColor) -> {
                JMenuItem addHighLightItem = new JMenuItem(COLOR_STRING_MAP.get(color));
                addHighLightItem.setBackground(color);
                addHighLightItem.setOpaque(true);
                addHighLightItem.addActionListener(e -> {
                    SwingUtilities.invokeLater(() -> {
                        List<ProxyHttpRequestResponse> proxyHttpRequestResponseList = api.proxy().history();
                        for (ProxyHttpRequestResponse proxyHttpRequestResponse : proxyHttpRequestResponseList) {
                            if (proxyHttpRequestResponse.request().pathWithoutQuery().equals(path)){
                                proxyHttpRequestResponse.annotations().setHighlightColor(highlightColor);
                            }
                        }
                    });
                    HighLighter.requestHighlightKey.add(path);
                });
                addSubHighLightMenu.add(addHighLightItem);

                // 如果是请求报文且鼠标选择了某些字符串
//                Optional<Selection> selection = api.montoya.ui.editor.Editor.currentRequest().selection();

            });
            addHighLightMenu.add(addSubHighLightMenu);
        }else {
            JMenuItem addFirstItem = new JMenuItem("第一个高亮");
            JMenuItem addHighLightItem = new JMenuItem("所有高亮");
        }
    }
    private void removeHighLight(ContextMenuEvent event, List<HttpRequestResponse> selectededRequestResponseList, JMenu removeHighLightMenu) {
        logger.logToOutput("removeHighLight()...");
        if (1 == selectededRequestResponseList.size()){
            HttpRequestResponse requestResponse = selectededRequestResponseList.get(0);
            String path = requestResponse.request().pathWithoutQuery();
            JMenuItem removeHighLightItem = new JMenuItem("取消高亮");
            JMenuItem removeAllHighLightItem = new JMenuItem("所有取消高亮");

            removeHighLightItem.addActionListener(e -> {
                if (!HighLighter.requestHighlightKey.contains(path)){
                    logger.logToOutput("不存在");
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    List<ProxyHttpRequestResponse> proxyHttpRequestResponseList = api.proxy().history();
                    for (ProxyHttpRequestResponse proxyHttpRequestResponse : proxyHttpRequestResponseList) {
                        if (proxyHttpRequestResponse.request().pathWithoutQuery().equals(path)){
                            proxyHttpRequestResponse.annotations().setHighlightColor(HighlightColor.NONE);
                        }
                    }
                    HighLighter.requestHighlightKey.remove(path);
                });
            });

            removeAllHighLightItem.addActionListener(e -> {
                SwingUtilities.invokeLater(() -> {
                    List<ProxyHttpRequestResponse> proxyHttpRequestResponseList = api.proxy().history();
                    for (ProxyHttpRequestResponse proxyHttpRequestResponse : proxyHttpRequestResponseList) {
                        proxyHttpRequestResponse.annotations().setHighlightColor(HighlightColor.NONE);
                    }
                    HighLighter.requestHighlightKey.clear();
                });
            });

            removeHighLightMenu.add(removeHighLightItem);
            removeHighLightMenu.add(removeAllHighLightItem);
        }else{
            JMenuItem removeFirstItem = new JMenuItem("第一个取消高亮");
            JMenuItem removeSelectedHighLightItem = new JMenuItem("所有选择的取消高亮");
            JMenuItem removeDialogMenuItem = new JMenuItem("弹窗选择");
            JMenuItem removeAllHighLightItem = new JMenuItem("所有取消高亮");

            removeFirstItem.addActionListener(e -> {
                SwingUtilities.invokeLater(() -> {
                    HttpRequestResponse requestResponse = selectededRequestResponseList.get(0);
                    String selectedPath = requestResponse.request().pathWithoutQuery();
                    List<ProxyHttpRequestResponse> proxyHttpRequestResponseList = api.proxy().history();
                    for (ProxyHttpRequestResponse proxyHttpRequestResponse : proxyHttpRequestResponseList) {
                        String historyPath = proxyHttpRequestResponse.request().pathWithoutQuery();
                        if (historyPath.equals(selectedPath)){
                            proxyHttpRequestResponse.annotations().setHighlightColor(HighlightColor.NONE);
                        }
                    }
                    HighLighter.requestHighlightKey.remove(selectedPath);
                });
            });

            removeSelectedHighLightItem.addActionListener(e -> {
                SwingUtilities.invokeLater(() -> {
                    Set<String> pathSet = new HashSet<>();
                    for (HttpRequestResponse requestResponse : selectededRequestResponseList) {
                        String selectedPath = requestResponse.request().pathWithoutQuery();
                        pathSet.add(selectedPath);
                    }

                    List<ProxyHttpRequestResponse> proxyHttpRequestResponseList = api.proxy().history();
                    for (ProxyHttpRequestResponse proxyHttpRequestResponse : proxyHttpRequestResponseList) {
                        String historyPath = proxyHttpRequestResponse.request().pathWithoutQuery();
                        if (pathSet.contains(historyPath)){
                            proxyHttpRequestResponse.annotations().setHighlightColor(HighlightColor.NONE);
                        }
                    }
                    HighLighter.requestHighlightKey.removeAll(pathSet);
                });
            });

            removeDialogMenuItem.addActionListener(e -> {
                logger.logToOutput("弹窗选择");
            });

            removeAllHighLightItem.addActionListener(e -> {
                SwingUtilities.invokeLater(() -> {
                    List<ProxyHttpRequestResponse> proxyHttpRequestResponseList = api.proxy().history();
                    for (ProxyHttpRequestResponse proxyHttpRequestResponse : proxyHttpRequestResponseList) {
                        proxyHttpRequestResponse.annotations().setHighlightColor(HighlightColor.NONE);
                    }
                    HighLighter.requestHighlightKey.clear();
                });
            });

            removeHighLightMenu.add(removeFirstItem);
            removeHighLightMenu.add(removeSelectedHighLightItem);
            removeHighLightMenu.add(removeDialogMenuItem);
            removeHighLightMenu.add(removeAllHighLightItem);
        }
    }
}