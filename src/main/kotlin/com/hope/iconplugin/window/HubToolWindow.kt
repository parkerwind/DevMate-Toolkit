package com.hope.iconplugin.window

import com.hope.iconplugin.icons.dialog.IconDialogWrapper
import com.hope.iconplugin.rewrite.MouseListenerKt
import com.hope.iconplugin.icons.view.IconToTextView
import com.intellij.openapi.wm.ToolWindow
import java.awt.GridLayout
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JPanel

// 工具窗口
class HubToolWindow(toolWindow: ToolWindow?) {

    // 工具窗口内容
    private val hubToolWindowContent = JPanel()

    // view
    private val iconToTextView = IconToTextView()

    // 内容列表
    private val iconsList: List<JPanel> by lazy {
        listOf(
            iconToTextView.getBoxView("/icons/icon_icons.svg", "图标替换"),
            iconToTextView.getBoxView("/icons/icon_def.svg", "待解锁"),
            iconToTextView.getBoxView("/icons/icon_def.svg", "待解锁"),
            iconToTextView.getBoxView("/icons/icon_def.svg", "待解锁"),
        )
    }

    init {
        // 设置排版
        hubToolWindowContent.layout = GridLayout(1, 4)
        hubToolWindowContent.border = BorderFactory.createEmptyBorder(0, 5, 0, 5)
        // 循环添加组件
        iconsList.forEach { hubToolWindowContent.add(it) }
        // 点击事件
        setupClickEvent(toolWindow)
    }

    // 设置点击事件处理
    private fun setupClickEvent(toolWindow: ToolWindow?) {
        iconsList[0].addMouseListener(object : MouseListenerKt() {
            override fun mouseClicked(e: MouseEvent?) {
                IconDialogWrapper(toolWindow?.project ?: return).show()
            }
        })
    }

    // 提供工具窗口内容引用
    val content get() = hubToolWindowContent
}
