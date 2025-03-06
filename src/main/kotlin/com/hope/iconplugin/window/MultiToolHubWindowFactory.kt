package com.hope.iconplugin.window

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory


/**
 * 多功能合集工具窗口工厂
 */
class MultiToolHubWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        // 创建工具窗口内容
        val hubToolWindow = HubToolWindow(toolWindow)
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(hubToolWindow.content, "", false)
        toolWindow.contentManager.addContent(content)
    }
}