package com.hope.iconplugin.icons.dialog

import com.hope.iconplugin.icons.data.ConfigBean
import com.hope.iconplugin.icons.utils.AddConfigInfoUtils
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.*

class SelectConfigDialogWrapper(private val project: Project, private val configList: Set<ConfigBean>) :
    DialogWrapper(true) {

    // 列表渠道
    private val channels = listOf("vivo", "oppo", "xiaomi", "huawei", "qq", "honor")

    // 新图标地址
    private var newIconPath: String? = null

    // 创建多选框 渠道
    private val channelsCheckBoxes: List<JCheckBox> by lazy {
        channels.map { JCheckBox(it) }
    }

    // 创建多选框 配置
    private val configCheckBoxes: List<JCheckBox> by lazy {
        configList.map {
            JCheckBox(it.configName).apply {
                border = JBUI.Borders.emptyRight(5)
            }
        }
    }

    fun setNewIconPath(newIconPath: String?) {
        this.newIconPath = newIconPath
    }

    init {
        init()
        title = "配置选择"
    }

    override fun createCenterPanel(): JComponent {
        // 创建一个面板
        val dialogPanel = JPanel(BorderLayout())

        // 创建一个渠道选择面板
        val channelsPanel = JPanel(BorderLayout())
        // 创建一个渠道选择提示信息
        val changeLabel = JLabel("请选择需要替换的渠道:")
        channelsPanel.add(changeLabel, BorderLayout.NORTH)
        // 创建渠道多选框
        val changeCheckBoxPanel = JPanel().apply {
            channelsCheckBoxes.forEach { add(it) }
        }
        channelsPanel.add(changeCheckBoxPanel, BorderLayout.LINE_START)
        // 添加渠道选择面板
        dialogPanel.add(channelsPanel, BorderLayout.NORTH)

        // 创建一个配置选择面板
        val configPanel = JPanel(BorderLayout())
        configPanel.setBorder(JBUI.Borders.empty(10, 0))
        // 创建一个配置选择提示信息
        val label = JLabel("请选择需要替换配置的名称:")
        configPanel.add(label, BorderLayout.NORTH)
        // 创建配置多选框
        val checkBoxPanel = JPanel(GridLayout(0, 3)).apply {
            configCheckBoxes.forEach { add(it) }
        }
        // 创建配置多选框面板
        configPanel.add(checkBoxPanel, BorderLayout.CENTER)
        // 添加配置选择面板
        dialogPanel.add(configPanel, BorderLayout.CENTER)
        return dialogPanel
    }

    override fun doOKAction() {
        // 获取选择渠道列表
        val channelsSelectList = getSelectedChannels()
        // 获取选择配置列表
        val configsSelectList = getSelectedConfigs()
        // 判断是否选中至少一个渠道和一个替换配置
        if (channelsSelectList.isNotEmpty() && configsSelectList.isNotEmpty()) {
            AddConfigInfoUtils.addConfigInfo(project, channelsSelectList, configsSelectList, newIconPath)
            super.doOKAction()
        } else {
            Messages.showErrorDialog("请选择至少一个渠道和一个替换配置名称", "错误")
        }
    }

    // 获取选中渠道列表
    private fun getSelectedChannels(): MutableSet<String> {
        return mutableSetOf<String>().apply {
            channelsCheckBoxes.forEachIndexed { _, checkBox ->
                if (checkBox.isSelected) add(checkBox.text)
            }
        }
    }

    // 获取选择配置列表
    private fun getSelectedConfigs(): MutableSet<ConfigBean> {
        return mutableSetOf<ConfigBean>().apply {
            configCheckBoxes.forEachIndexed { index, checkBox ->
                if (checkBox.isSelected) add(configList.elementAt(index))
            }
        }
    }
}