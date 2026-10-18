package org.jeecg.modules.airag.llm.splitter;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.airag.llm.consts.LLMConsts;

import java.util.ArrayList;
import java.util.List;

/**
* @Description: 自定义分段器
*
* @author: wangshuai
* @date: 2026/2/6 15:47
*/
public class CustomDocumentSplitter implements DocumentSplitter {
    /**
     * 规则
     */
    private final String textRules;
    
    /**
     * 分段标识符
     */
    private final String separator;

    /**
     * 分度长度
     */
    private final int segmentSize;

    /**
     * 重叠长度
     */
    private final int overlapSize;

    public CustomDocumentSplitter(String textRules, String separator, int segmentSize, int overlapSize) {
        this.textRules = textRules;
        this.separator = separator;
        this.segmentSize = segmentSize;
        this.overlapSize = overlapSize;
    }

    @Override
    public List<TextSegment> split(Document document) {
        String text = document.text();

        //过滤掉规则
        if (oConvertUtils.isNotEmpty(textRules)) {
            //处理连续的空格、换行符、制表符
            if (textRules.contains(LLMConsts.TEXT_RULES_CLEAN_SPACES)) {
                text = text.replaceAll("\\s+", " ");
            }
            //URL和电子邮箱地址
            if (textRules.contains(LLMConsts.TEXT_RULES_REMOVE_URLS_EMAILS)) {
                String urlRegex = "http[s]?://\\S+";
                String emailRegex = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}";
                text = text.replaceAll(urlRegex, "").replaceAll(emailRegex, "");
            }
        }
        if (oConvertUtils.isEmpty(text)) {
            return new ArrayList<>();
        }
        //根据定义的分词进行分割
        String[] parts = text.split(java.util.regex.Pattern.quote(separator));
        //存放TextSegment的集合
        List<TextSegment> segments = new ArrayList<>();
        //存放文本的集合
        List<String> currentBuffer = new ArrayList<>();
        int currentLength = 0;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (oConvertUtils.isEmpty(part)) {
                continue;
            }
            // 如果不是第一部分（根据索引判断），说明之前有分隔符，补到当前part开头
            if (i > 0) {
                part = separator + part;
            }

            //文本长度
            int partLen = part.length();
            // 预计长度 = 当前长度 + 文本长度 (分隔符已包含在part中)
            int projectedLen = currentLength + partLen;

            //判断分隔长度
            if (projectedLen <= segmentSize) {
                //分隔长度小于自定义的分割长度
                currentBuffer.add(part);
                currentLength = projectedLen;
            } else {
                // 1. 保存当前分段
                if (!currentBuffer.isEmpty()) {
                    flushAndOverlap(segments, currentBuffer, document, true);
                    // 分隔符已包含在元素中，直接求和
                    currentLength = currentBuffer.stream().mapToInt(String::length).sum();
                }

                // 3. 处理当前part
                // 检查加上当前part是否超过限制
                int newProjectedLen = currentLength + partLen;
                if (newProjectedLen <= segmentSize) {
                    currentBuffer.add(part);
                    currentLength = newProjectedLen;
                } else {
                    // part太长，需要切分
                    int offset = 0;
                    //截取的长度小于文本长度，跳出循环
                    while (offset < partLen) {
                        // 计算当前分段剩余可用空间
                        // space = 最大分段长度 - 当前已用长度
                        int space = segmentSize - currentLength;
                        if (space <= 0) {
                            // Buffer满（可能是重叠导致的），强制刷新
                            flushAndOverlap(segments, currentBuffer, document, true);
                            currentLength = currentBuffer.stream().mapToInt(String::length).sum();

                            // 刷新后重新计算剩余空间
                            space = segmentSize - currentLength;
                            // 如果重叠本身就超长（即space <= 0），则清空Buffer以避免死循环，并重置space为整个分段长度
                            if (space <= 0) {
                                currentBuffer.clear();
                                currentLength = 0;
                                space = segmentSize;
                            }
                        }

                        // 计算本次能截取的长度：取剩余空间和剩余part长度的较小值
                        int take = Math.min(space, partLen - offset);
                        String chunk = part.substring(offset, offset + take);
                        
                        currentBuffer.add(chunk);
                        currentLength += take;
                        offset += take;

                        // 如果还没处理完part，说明填满了buffer，需要flush
                        if (offset < partLen) {
                            flushAndOverlap(segments, currentBuffer, document, false);
                            currentLength = currentBuffer.stream().mapToInt(String::length).sum();
                        }
                    }
                }
            }
        }

        // 处理剩余部分
        if (!currentBuffer.isEmpty()) {
            String segmentText = String.join("", currentBuffer).trim();
            if (oConvertUtils.isNotEmpty(segmentText)) {
                segments.add(TextSegment.from(segmentText, document.metadata()));
            }
        }
        return segments;
    }

    /**
     * 将当前buffer内容保存为segment，并处理重叠部分
     * @param segments 结果集合
     * @param buffer 当前文本buffer
     * @param document 原始文档（用于元数据）
     */
    private void flushAndOverlap(List<TextSegment> segments, List<String> buffer, Document document, boolean enableOverlap) {
        if (buffer.isEmpty()) {
            return;
        }
        // 保存当前分段
        String segmentText = String.join("", buffer).trim();
        if (oConvertUtils.isEmpty(segmentText)) {
            buffer.clear();
            return;
        }
        segments.add(TextSegment.from(segmentText, document.metadata()));

        if (!enableOverlap) {
            buffer.clear();
            return;
        }

        // 处理重叠 (保留buffer末尾部分)
        List<String> newBuffer = new ArrayList<>();
        int newLen = 0;

        // 倒序遍历查找可保留的末尾部分
        for (int j = buffer.size() - 1; j >= 0; j--) {
            String p = buffer.get(j);
            int pLen = p.length();
            //update-begin---author:wangshuai ---date:2026-04-09  for：【issue/9418】修复重叠率失效问题：当某个part本身超过overlapSize时，取其尾部子串保证重叠不为0-----------
            if (newLen + pLen <= overlapSize) {
                // 整段可以放入重叠区
                newBuffer.add(0, p);
                newLen += pLen;
            } else {
                // 剩余可用空间
                int remaining = overlapSize - newLen;
                if (remaining > 0) {
                    // 取该元素的尾部子串，保证重叠区不为空
                    newBuffer.add(0, p.substring(pLen - remaining));
                }
                // 已填满重叠区，停止
                //update-end---author:wangshuai ---date:2026-04-09  for：【issue/9418】修复重叠率失效问题：当某个part本身超过overlapSize时，取其尾部子串保证重叠不为0-----------
                break;
            }
        }
        // 更新buffer为仅包含重叠部分
        buffer.clear();
        buffer.addAll(newBuffer);
    }
}
