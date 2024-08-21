package com.dyland.accessibility

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

class ConvertNodeToXml {
    companion object {
        fun convertNodeToXml(node: AccessibilityNodeInfo): String {
            val builder = StringBuilder()
            builder.append("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>\n")
            builder.append("<hierarchy rotation=\"0\">\n")
            convertNodeToXml(node, builder, 1)
            builder.append("</hierarchy>")
            return builder.toString()
        }

        private fun convertNodeToXml(
            node: AccessibilityNodeInfo?,
            builder: StringBuilder,
            indent: Int
        ) {
            if (node == null) return

            val indentStr = "  ".repeat(indent)
            builder.append(indentStr)
                .append("<node")
                .append(" text=\"").append(if (node.text != null) node.text else "").append("\"")
                .append(" resource-id=\"")
                .append(if (node.viewIdResourceName != null) node.viewIdResourceName else "")
                .append("\"")
                .append(" class=\"").append(if (node.className != null) node.className else "")
                .append("\"")
                .append(" package=\"")
                .append(if (node.packageName != null) node.packageName else "")
                .append("\"")
                .append(" content-desc=\"")
                .append(if (node.contentDescription != null) node.contentDescription else "")
                .append("\"")
                .append(" checkable=\"").append(node.isCheckable).append("\"")
                .append(" checked=\"").append(node.isChecked).append("\"")
                .append(" clickable=\"").append(node.isClickable).append("\"")
                .append(" enabled=\"").append(node.isEnabled).append("\"")
                .append(" focusable=\"").append(node.isFocusable).append("\"")
                .append(" focused=\"").append(node.isFocused).append("\"")
                .append(" scrollable=\"").append(node.isScrollable).append("\"")
                .append(" long-clickable=\"").append(node.isLongClickable).append("\"")
                .append(" password=\"").append(node.isPassword).append("\"")
                .append(" selected=\"").append(node.isSelected).append("\"")
                .append(" bounds=\"").append(getBoundsInScreen(node)).append("\"")
                .append(">\n")

            for (i in 0 until node.childCount) {
                convertNodeToXml(node.getChild(i), builder, indent + 1)
            }

            builder.append(indentStr).append("</node>\n")
        }

        private fun getBoundsInScreen(node: AccessibilityNodeInfo): String {
            val bounds: Rect = Rect()
            node.getBoundsInScreen(bounds)
            return (((("[" + bounds.left).toString() + "," + bounds.top).toString() + "][" + bounds.right).toString() + "," + bounds.bottom).toString() + "]"
        }
    }
}