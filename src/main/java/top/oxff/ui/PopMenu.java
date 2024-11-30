package top.oxff.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.core.Range;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.proxy.ProxyHttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.MessageEditorHttpRequestResponse;
import top.oxff.GlobalVar;
import top.oxff.tools.HttpTools;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

import static top.oxff.Constant.*;

public class PopMenu implements ContextMenuItemsProvider {
    private final MontoyaApi api;
    private final Logging logger;

    public PopMenu(MontoyaApi api) {
        this.api = api;
        logger = api.logging();

    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        logger.logToOutput("PopMenu");
        if (event.isFromTool(ToolType.PROXY)) {
            return proxyMenu(event);
        } else if (event.isFromTool(ToolType.REPEATER)) {
            return repeaterMenu(event);
        } else if (event.isFromTool(ToolType.LOGGER)) {
            // TODO
            /**
             * 1. 获取所有请求
             */
        } else if (event.isFromTool(ToolType.INTRUDER)) {
            // TODO
        } else {
            return null;
        }
        return null;
    }

    private List<Component> proxyMenu(ContextMenuEvent event) {
        logger.logToOutput("proxyMenu()...");

        List<HttpRequestResponse> selectededRequestResponseList = event.selectedRequestResponses();
        if (selectededRequestResponseList.isEmpty()) {
            return getComponentListIfSelectNoRequestOnHistoryBoard(event);
        }

        List<Component> menuItemList = new ArrayList<>();
        JMenu addHighLightMenu = new JMenu("添加高亮");
        JMenu removeHighLightMenu = new JMenu("取消高亮");

        addHighLight(event, selectededRequestResponseList, addHighLightMenu);
        removeHighLight(event, selectededRequestResponseList, removeHighLightMenu);

        menuItemList.add(addHighLightMenu);
        menuItemList.add(removeHighLightMenu);

        return menuItemList;
    }

    private List<Component> getComponentListIfSelectNoRequestOnHistoryBoard(ContextMenuEvent event) {
        logger.logToOutput("getComponentListIfSelectNoRequestOnHistoryBoard()...");
        List<Component> menuItemList = new ArrayList<>();
        JMenuItem removeAllHighLightItem = new JMenuItem("取消所有高亮");
        removeAllHighLightItem.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            List<ProxyHttpRequestResponse> proxyHttpRequestResponseList = api.proxy().history();
            for (ProxyHttpRequestResponse proxyHttpRequestResponse : proxyHttpRequestResponseList) {
                proxyHttpRequestResponse.annotations().setHighlightColor(HighlightColor.NONE);
            }
            GlobalVar.highLightPathSet.clear();
        }));
        menuItemList.add(removeAllHighLightItem);

        if (event.messageEditorRequestResponse().isPresent()) {
            MessageEditorHttpRequestResponse messageEditorHttpRequestResponse = event.messageEditorRequestResponse()
                                                                                     .get();
            if (messageEditorHttpRequestResponse.selectionOffsets().isPresent()) {
                Range selectionOffsets = messageEditorHttpRequestResponse.selectionOffsets().get();
                int start = selectionOffsets.startIndexInclusive();
                int end = selectionOffsets.endIndexExclusive();
                int selectLength = end - start;
                HttpRequestResponse httpRequestResponse = messageEditorHttpRequestResponse.requestResponse();
                HttpRequest httpRequest = httpRequestResponse.request();
                byte[] requestBytes = httpRequest.toByteArray().getBytes();
                byte[] selectedBytes = Arrays.copyOfRange(requestBytes, start, end);
                boolean isPrintable = HttpTools.isBytesPrintable(selectedBytes);
                if (isPrintable && MINI_SELECT_STRING_LENGTH <= selectLength) {
                    String selectString = event.messageEditorRequestResponse().get().requestResponse().request()
                                               .toString().substring(start, end);
                    JMenu addHighLightMenu = new JMenu("添加选择字符串高亮");
                    COLOR_TO_HIGHLIGHT_COLOR_MAP.forEach((color, highlightColor) -> {
                        JMenuItem addHighLightItem = new JMenuItem(COLOR_STRING_MAP.get(color));
                        addHighLightItem.setBackground(color);
                        addHighLightItem.setOpaque(true);
                        addHighLightItem.addActionListener(e -> {
                            GlobalVar.highLightSelectStringSet.add(selectString);
                            GlobalVar.selectStringHighlightColor.put(selectString, highlightColor);
                            for (ProxyHttpRequestResponse proxyHttpRequestResponse : api.proxy().history()) {
                                if (HttpTools.isProxyHttpRequestResponsePrintable(proxyHttpRequestResponse)) {

                                    String requestString = proxyHttpRequestResponse.request().toString();
                                    if (GlobalVar.isHighLightSelectString(requestString)){
                                        proxyHttpRequestResponse.annotations().setHighlightColor(highlightColor);
                                    }
                                }
                            }
                            httpRequestResponse.annotations().setHighlightColor(highlightColor);
                        });
                        addHighLightMenu.add(addHighLightItem);
                    });
                    menuItemList.add(addHighLightMenu);
                }
            }

        }

        return menuItemList;
    }

    private List<Component> repeaterMenu(ContextMenuEvent event) {
        logger.logToOutput("repeaterMenu()...");
        MessageEditorHttpRequestResponse.SelectionContext selectionContext = event.messageEditorRequestResponse().get()
                                                                                  .selectionContext();
        if (!(event.messageEditorRequestResponse().isPresent() && event.messageEditorRequestResponse().get()
                                                                       .selectionOffsets().isPresent()) ||
                !selectionContext.equals(MessageEditorHttpRequestResponse.SelectionContext.REQUEST)) {
            return null;
        }

        List<Component> menuItemList = new ArrayList<>();
        Range selectionOffsets = event.messageEditorRequestResponse().get().selectionOffsets().get();
        int start = selectionOffsets.startIndexInclusive();
        int end = selectionOffsets.endIndexExclusive();
        String path = event.messageEditorRequestResponse().get().requestResponse().request().pathWithoutQuery();
        String selectText;
        if (end > start) {
            selectText = event.messageEditorRequestResponse().get().requestResponse().request().toString()
                              .substring(start, end);
        } else {
            selectText = null;
        }

        JMenu addSubHighLightMenu = new JMenu("添加高亮");
        JMenu addSelectedHighLightMenu = new JMenu("添加选中高亮");

        COLOR_TO_HIGHLIGHT_COLOR_MAP.forEach((color, highlightColor) -> {
            JMenuItem addHighLightItem = new JMenuItem(COLOR_STRING_MAP.get(color));
            addHighLightItem.setBackground(color);
            addHighLightItem.setOpaque(true);
            addHighLightItem.addActionListener(e -> SwingUtilities.invokeLater(() -> {
                List<ProxyHttpRequestResponse> proxyHttpRequestResponseList = api.proxy().history();
                for (ProxyHttpRequestResponse proxyHttpRequestResponse : proxyHttpRequestResponseList) {
                    String requestPath = proxyHttpRequestResponse.request().pathWithoutQuery();
                    if (requestPath.equals(path)) {
                        proxyHttpRequestResponse.annotations().setHighlightColor(highlightColor);
                    }
                }
                GlobalVar.highLightPathSet.add(path);
                GlobalVar.requestPathHighlightColor.put(path, highlightColor);
            }));
            addSubHighLightMenu.add(addHighLightItem);
        });

        if (selectText != null) {
            COLOR_TO_HIGHLIGHT_COLOR_MAP.forEach((color, highlightColor) -> {
                JMenuItem addHighLightItem = new JMenuItem(COLOR_STRING_MAP.get(color));
                addHighLightItem.setBackground(color);
                addHighLightItem.setOpaque(true);
                addHighLightItem.addActionListener(e -> {
                    SwingUtilities.invokeLater(() -> {
                        List<ProxyHttpRequestResponse> proxyHttpRequestResponseList = api.proxy().history();
                        for (ProxyHttpRequestResponse proxyHttpRequestResponse : proxyHttpRequestResponseList) {
                            String requestPath = proxyHttpRequestResponse.request().pathWithoutQuery();
                            if (requestPath.contains(path)) {
                                proxyHttpRequestResponse.annotations().setHighlightColor(highlightColor);
                            }
                        }
                        GlobalVar.highLightPathSet.add(selectText);
                        GlobalVar.requestPathHighlightColor.put(selectText, highlightColor);
                    });
                });
                addSelectedHighLightMenu.add(addHighLightItem);
            });
        }

        menuItemList.add(addSubHighLightMenu);
        menuItemList.add(addSelectedHighLightMenu);
        return menuItemList;
    }

    private void addHighLight(ContextMenuEvent event, List<HttpRequestResponse> selectededRequestResponseList, JMenu addHighLightMenu) {
        logger.logToOutput("addHighLight()...");
        JMenu addHighLightSubMenu = new JMenu("添加高亮");
        COLOR_TO_HIGHLIGHT_COLOR_MAP.forEach((color, highlightColor) -> {
            JMenuItem addHighLightItem = new JMenuItem(COLOR_STRING_MAP.get(color));
            addHighLightItem.setBackground(color);
            addHighLightItem.setOpaque(true);
            addHighLightItem.addActionListener(e -> {
                SwingUtilities.invokeLater(() -> {
                    Set<String> pathSet = new HashSet<>();
                    for (HttpRequestResponse httpRequestResponse : selectededRequestResponseList) {
                        String path = httpRequestResponse.request().pathWithoutQuery();
                        if (!GlobalVar.isHighLightPath(path)){
                            GlobalVar.highLightPathSet.add(path);
                            GlobalVar.addHighLightPath(path, highlightColor);
                            pathSet.add(path);
                        }
                    }
                    api.proxy().history().forEach(proxyHttpRequestResponse -> {
                        String path = proxyHttpRequestResponse.request().pathWithoutQuery();
                        if (pathSet.contains(path)){
                            proxyHttpRequestResponse.annotations().setHighlightColor(highlightColor);
                        }
                    });

                });
            });
            addHighLightSubMenu.add(addHighLightItem);
        });
        addHighLightMenu.add(addHighLightSubMenu);
    }

    private void removeHighLight(ContextMenuEvent event, List<HttpRequestResponse> selectededRequestResponseList,
                                 JMenu removeHighLightMenu) {
        logger.logToOutput("removeHighLight()...");
        JMenu removeHighLightSubMenu = new JMenu("移除高亮");
        COLOR_TO_HIGHLIGHT_COLOR_MAP.forEach((color, highlightColor) -> {
            JMenuItem removeHighLightItem = new JMenuItem(COLOR_STRING_MAP.get(color));
            removeHighLightItem.setBackground(color);
            removeHighLightItem.setOpaque(true);
            removeHighLightItem.addActionListener(e -> {
                SwingUtilities.invokeLater(() -> {
                    Set<String> pathSet = new HashSet<>();
                    for (HttpRequestResponse httpRequestResponse : selectededRequestResponseList) {
                        String path = httpRequestResponse.request().pathWithoutQuery();
                        if (GlobalVar.isHighLightPath(path)){
                            GlobalVar.removeHighLightPath(path);
                            pathSet.add(path);
                        }
                   }
                    api.proxy().history().forEach(proxyHttpRequestResponse -> {
                        String path = proxyHttpRequestResponse.request().pathWithoutQuery();
                        if (pathSet.contains(path)){
                            proxyHttpRequestResponse.annotations().setHighlightColor(HighlightColor.NONE);
                        }
                    });
                });
            });
            removeHighLightSubMenu.add(removeHighLightItem);
        });
        removeHighLightMenu.add(removeHighLightSubMenu);

        JMenuItem removeAllHighLightSubMenu = new JMenuItem("移除所有高亮");
        removeAllHighLightSubMenu.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                GlobalVar.clearHighLightPath();
                GlobalVar.clearHighLightSelectString();
                api.proxy().history().forEach(proxyHttpRequestResponse -> {
                    proxyHttpRequestResponse.annotations().setHighlightColor(HighlightColor.NONE);
                });
            });
        });
        removeHighLightMenu.add(removeAllHighLightSubMenu);

        JMenuItem removeHighLightPath = new JMenuItem("移除高亮路径");
        removeHighLightPath.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                GlobalVar.clearHighLightPath();
                api.proxy().history().forEach(proxyHttpRequestResponse -> {
                    String path = proxyHttpRequestResponse.request().pathWithoutQuery();
                    if (GlobalVar.isHighLightPath(path)){
                        proxyHttpRequestResponse.annotations().setHighlightColor(HighlightColor.NONE);
                    }
                });
            });
        });
        removeHighLightMenu.add(removeHighLightPath);


        JMenuItem removeHighLightSelectString = new JMenuItem("移除选中字符串高亮");
        removeHighLightSelectString.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                GlobalVar.clearHighLightSelectString();
                api.proxy().history().forEach(proxyHttpRequestResponse -> {
                    if (HttpTools.isProxyHttpRequestResponsePrintable(proxyHttpRequestResponse)){
                        String requestString = proxyHttpRequestResponse.request().toString();
                        if (GlobalVar.isHighLightSelectString(requestString)){
                            proxyHttpRequestResponse.annotations().setHighlightColor(HighlightColor.NONE);
                        }
                    }
                });
            });
        });
        removeHighLightMenu.add(removeHighLightSelectString);

    }
}