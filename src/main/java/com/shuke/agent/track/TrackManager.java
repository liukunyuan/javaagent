package com.shuke.agent.track;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Stack;


public class TrackManager {

    //    private static final ThreadLocal<Stack<String>> track = new ThreadLocal<Stack<String>>();
    private static final TransmittableThreadLocal<Stack<String>> track = new TransmittableThreadLocal<Stack<String>>();

    private static String createSpan() {
        Stack<String> stack = track.get();
        if (stack == null) {
            stack = new Stack<>();
            track.set(stack);
        }
        String linkId;
        if (stack.isEmpty()) {
            linkId = TrackContext.getLinkId();
            if (linkId == null) {
                linkId = "nvl";
                TrackContext.setLinkId(linkId);
            }
        } else {
            linkId = stack.peek();
            TrackContext.setLinkId(linkId);
        }
        return linkId;
    }

    public static String createEntrySpan() {
        String span = createSpan();
        Stack<String> stack = track.get();
        stack.push(span);
        return span;
    }


    public static String getExitSpan() {
        Stack<String> stack = track.get();
        if (stack == null || stack.isEmpty()) {
            TrackContext.clear();
            return null;
        }
        try {
            return stack.pop();
        } catch (Exception e) {
            TrackContext.clear();
            return null;
        }
    }

    public static String getCurrentSpan() {
        Stack<String> stack = track.get();
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return stack.peek();
    }


}