package com.hope.iconplugin.icons.utils

import com.hope.iconplugin.icons.data.ConfigBean
import com.hope.iconplugin.utils.extractFirstNumber
import com.hope.iconplugin.utils.getChannelInfo
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.nio.charset.StandardCharsets

/**
 * 解析配置
 */
object BuildConfigUtils {

    // 存储 build.gradle文件的内容
    var buildContent = ""

    // 分析 build.gradle 文件
    fun readAndParseBuildGradle(project: Project?, callback: (Set<ConfigBean>) -> Unit) {
        val buildGradleFile = getBuildFile(project)
        if (buildGradleFile == null) {
            Messages.showErrorDialog("未找到 build.gradle 文件", "错误")
        } else {
            // 读取文件内容
            buildContent = String(buildGradleFile.contentsToByteArray(), StandardCharsets.UTF_8)
            // 解析 build.gradle 文件，获取配置信息
            callback(parseProductFlavors())
        }
    }

    // 解析 build.gradle 文件，获取配置信息
    private fun parseProductFlavors(): Set<ConfigBean> {
        // 存储所有配置相关信息
        val configBeans = mutableSetOf<ConfigBean>()
        // 正则表达式匹配 productFlavors 下的块
        val pattern = """automatic_\w+""".toRegex()
        // 当前文本块
        var currentBlock: StringBuilder? = null
        var configName: String? = null

        // 将字符串 content 分割成单独的行，并对每一行执行一个操作
        buildContent.lines().forEach { line ->
            if (currentBlock != null) {
                // 将当前行添加到当前块中
                currentBlock!!.append(line).append('\n')
                // 判断当前行是否是块的开始或结束
                if (line.trim().endsWith("}")) {
                    // 当前块结束
                    val appNameRegex = """resValue\s+"string",\s+"app_name",\s+"(.+?)"""".toRegex()
                    // 获取 app_name 的值
                    val matches = appNameRegex.find(currentBlock.toString())
                    val appName = matches?.groups?.get(1)?.value
                    // 获取渠道值
                    val regex = """CHANNEL_VALUE:\s*"([^"]*)"""".toRegex()
                    val matchResult = regex.find(currentBlock.toString())
                    val channelValue = matchResult?.groups?.get(1)?.value
                    // 添加配置信息到列表中
                    configBeans.add(ConfigBean(configName, appName, channelValue))
                    // 重置当前块和配置名称
                    currentBlock = null
                    configName = null
                }
            } else {
                // 匹配已 automatic_xxx 格式开头的文本块
                val match = pattern.find(line)
                if (match != null) {
                    // 添加配置名称
                    configName = match.value
                    // 创建 StringBuilder 对象，用于存储当前块的内容
                    currentBlock = StringBuilder()
                    // 将当前行 (automatic_xxx {) 添加到 StringBuilder 中
                    currentBlock!!.append(line).append('\n')
                }
            }
        }
        return configBeans
    }

    // 添加 productFlavors 配置
    fun addProductFlavor(
        project: Project?,
        configList: Set<ConfigBean>,
        channels: Set<String>,
    ) {
        // 在 WriteCommandAction 中执行修改操作
        WriteCommandAction.runWriteCommandAction(project) {
            val buildGradleFile = getBuildFile(project) ?: return@runWriteCommandAction
            // 返回指定虚拟文件的文档
            val documentManager = FileDocumentManager.getInstance()
            val document = documentManager.getDocument(buildGradleFile)
            // 查找 productFlavors 块的结束位置
            var lastIndex = findEndOfProductFlavors(document?.text ?: return@runWriteCommandAction)
            // 循环添加所有配置
            if (lastIndex != -1) {
                configList.forEach {
                    // 马甲标识
                    val number = extractFirstNumber(it.channelValue)
                    // 渠道信息配置
                    val channelsInfo = getChannelInfo(channels, number)
                    // 新增 productFlavors 配置
                    val newFlavorConfig = """
                    |
                    |        ${it.configName} {
                    |            manifestPlaceholders = [CHANNEL_VALUE: "text${number}"]
                    |            resValue "string", "app_name", "${it.appName}"
                    |            resValue "string", "${it.configName}_build_channels", "$channelsInfo"
                    |        }
                    |
                    """.trimMargin()
                    // 在指定位置插入新的 productFlavors 配置
                    document.insertString(lastIndex, newFlavorConfig)
                    // 更新 lastIndex
                    lastIndex += newFlavorConfig.length
                }
                // 保存信息
                documentManager.saveDocument(document)
            }
        }
    }

    // 定位 build.gradle 文件
    private fun getBuildFile(project: Project?): VirtualFile? {
        // 定位 build.gradle 文件
        val appDir = LocalFileSystem.getInstance().findFileByPath(
            project?.basePath + "/app"
        )
        // 获取 app 目录下的 build.gradle 文件
        return appDir?.findChild("build.gradle")
    }

    // 查找 productFlavors 块的结束位置
    private fun findEndOfProductFlavors(text: String): Int {
        // 正则表达式，匹配 productFlavors { 中间可兼容多个空格和无空格
        val regex = """productFlavors\s*\{""".toRegex()
        // 查找 productFlavors 块的开始位置
        val matchResult = regex.find(text) ?: return -1
        // 统计左大括号数量，右大括号数量
        var bracesCount = 1
        var i = matchResult.range.last + 1
        // 如果在文件范围内，bracesCount小于0说明多了一个 '}' 则表示 productFlavors 块的结束位置
        while (i < text.length && bracesCount > 0) {
            when (text[i]) {
                '{' -> bracesCount++
                '}' -> bracesCount--
            }
            i++
        }
        // 如果 bracesCount 为 0，则表示 productFlavors 块的结束位置 ， 否则未找到结束位置
        return if (bracesCount == 0) i - 1 else -1
    }
}