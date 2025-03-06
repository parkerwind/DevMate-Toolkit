package com.hope.iconplugin.icons.dialog

import com.hope.iconplugin.icons.data.ConfigBean
import com.hope.iconplugin.icons.utils.BuildConfigUtils
import com.hope.iconplugin.icons.utils.IconDefaultChange
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import javax.swing.*


class IconDialogWrapper(private val project: Project) : DialogWrapper(true) {

    private var textField: JTextField? = null

    init {
        init()
        title = "图标处理"
    }

    override fun createCenterPanel(): JComponent {
        // 创建一个面板
        val dialogPanel = JPanel(BorderLayout())
        // 创建一个提示信息
        val label = JLabel("请输入新的图标文件夹地址 (确保icon名称分别为:144, 192, 256, 384, 512):")

        // 创建一个输入面板
        val inputPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        // 创建一个文本输入框
        textField = JTextField(45)
        // 创建 JButton
        val button = JButton("select...")
        // 创建 JFileChooser
        val fileChooser = JFileChooser()
        // 设置只能选择目录
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        // 添加按钮的点击事件
        button.addActionListener {
            // 显示打开文件对话框
            val result = fileChooser.showOpenDialog(dialogPanel)
            if (result == JFileChooser.APPROVE_OPTION) {
                val selectedFile = fileChooser.selectedFile
                // 设置文本字段为选中的文件路径
                textField!!.text = selectedFile.absolutePath
            }
        }
        // 将文本输入框添加到面板
        textField?.let { inputPanel.add(it, BorderLayout.CENTER) }
        // 将按钮添加到面板
        inputPanel.add(button, BorderLayout.CENTER);

        // 将标签添加到面板的顶部
        dialogPanel.add(label, BorderLayout.NORTH)
        // 将输入面板添加到面板的底部
        dialogPanel.add(inputPanel, BorderLayout.CENTER)

        // 添加额外的提示信息
        val infoPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        val infoLabel = JLabel("选择替换方式:")
        infoPanel.add(infoLabel)
        dialogPanel.add(infoPanel, BorderLayout.SOUTH)

        return dialogPanel
    }

    override fun createActions(): Array<Action> {
        val defaultAction: Action = object : DialogWrapperAction("默认icon替换") {
            override fun doAction(e: ActionEvent) {
                // 在这里处理点击"默认icon替换"按钮后的动作
                if (textField != null && textField!!.text.isNotEmpty()) {
                    doDefaultIconReplacement()
                    close(OK_EXIT_CODE)
                }
            }
        }

        val channelAction: Action = object : DialogWrapperAction("渠道icon替换") {
            override fun doAction(e: ActionEvent) {
                // 在这里处理点击"渠道icon替换"按钮后的动作
                if (textField != null && textField!!.text.isNotEmpty()) {
                    close(OK_EXIT_CODE)
                    doChannelIconReplacement()
                }
            }
        }

        return arrayOf(defaultAction, channelAction)
    }

    private fun doDefaultIconReplacement() {
        // 处理默认图标替换逻辑
        IconDefaultChange.changeIcon(project, textField!!.text)
    }

    // 进行渠道icon替换
    private fun doChannelIconReplacement() {
        // 读取配置文件
        BuildConfigUtils.readAndParseBuildGradle(project) {
            // 展示配置选择弹窗
            showSelectConfigDialog(it)
        }
    }

    // 展示配置选择弹窗
    private fun showSelectConfigDialog(configList: Set<ConfigBean>?) {
        if (configList.isNullOrEmpty()) {
            Messages.showErrorDialog("未匹配到 automatic_xxx 格式配置", "错误")
        } else {
            val selectDialog = SelectConfigDialogWrapper(project, configList)
            selectDialog.setNewIconPath(textField!!.text)
            selectDialog.show()
        }
    }
}
