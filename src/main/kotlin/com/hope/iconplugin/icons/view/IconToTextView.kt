package com.hope.iconplugin.icons.view

import com.intellij.openapi.util.IconLoader
import java.awt.Component
import javax.swing.*

// 创建上图下文字视图
class IconToTextView {

    // 添加视图
    fun getBoxView(iconPath: String, text: String): JPanel {
        val panel = JPanel() // 创建一个新的 JPanel
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS) // 设置布局为 BoxLayout，方向为 Y_AXIS (垂直)

        // 加载图标
        val originalIcon = IconLoader.getIcon(iconPath, this::class.java)
        val iconLabel = JLabel(originalIcon)
        iconLabel.alignmentX = Component.CENTER_ALIGNMENT // 设置为中心对齐

        // 创建文本标签并设置字体大小
        val textLabel = JLabel(text)
        textLabel.alignmentX = Component.CENTER_ALIGNMENT // 同样设置为中心对齐
        textLabel.font = textLabel.font.deriveFont(12f)

        // 将标签添加到面板
        panel.add(iconLabel)
        panel.add(textLabel)

        panel.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        return panel
    }
}