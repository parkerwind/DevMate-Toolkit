package com.hope.iconplugin.icons.utils

import com.hope.iconplugin.icons.data.ConfigBean
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException

object AddConfigInfoUtils {

    private const val ICONS_NAME = "icons"

    // 添加配置信息
    fun addConfigInfo(project: Project?, channels: Set<String>, configList: Set<ConfigBean>, newIconPath: String?) {
        // 获取项目基础路径
        val basePath = project?.basePath ?: return
        // 替换成新配置名称
        val newConfigList = getNewConfigName(configList, channels)
        // 准备创建icons文件夹及其内部icon配置
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "更新图标配置...", true) {
            override fun run(indicator: ProgressIndicator) {
                // 不确定完成时间
                indicator.isIndeterminate = false
                checkIconsDir(project, basePath, newConfigList, channels, newIconPath ?: return, indicator)
            }
        })
    }

    // 检查icons文件夹是否存在，如果不存在则创建，并创建内部配置文件
    private fun checkIconsDir(
        project: Project?,
        basePath: String,
        configList: Set<ConfigBean>,
        channels: Set<String>,
        newIconPath: String,
        indicator: ProgressIndicator
    ) {
        val baseDir = LocalFileSystem.getInstance().findFileByPath(basePath) ?: return
        indicator.text = "检查图标目录..."
        // 如果icons文件夹不存在，则创建icons文件夹
        val iconsDir = baseDir.findChild(ICONS_NAME) ?: run {
            indicator.text = "创建图标目录..."
            var createdDir: VirtualFile? = null
            // 创建icons文件夹
            WriteCommandAction.runWriteCommandAction(project) {
                try {
                    createdDir = baseDir.createChildDirectory(this, ICONS_NAME)
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
            createdDir  // 返回新创建的目录
        }
        // 创建所有配置文件夹
        createAllConfigDir(project, iconsDir ?: return, configList, channels, newIconPath, indicator)
    }

    // 创建所有配置文件夹
    private fun createAllConfigDir(
        project: Project?,
        iconsDir: VirtualFile,
        configList: Set<ConfigBean>,
        channels: Set<String>,
        newIconPath: String,
        indicator: ProgressIndicator
    ) {
        configList.forEachIndexed { index, config ->
            // 更新进度
            indicator.fraction = index.toDouble() / configList.size
            indicator.text = "更新配置: ${config.configName}"

            config.configName?.let {
                val iconDir = iconsDir.findChild(it) ?: run {
                    var newDir: VirtualFile? = null  // 定义一个中间变量来存储新创建的目录
                    WriteCommandAction.runWriteCommandAction(project) {
                        try {
                            newDir = iconsDir.createChildDirectory(this, it)  // 创建目录并赋值给变量
                        } catch (ex: IOException) {
                            ex.printStackTrace()
                        }
                    }
                    newDir  // 返回新创建的目录引用
                } ?: return  // 如果目录仍然为null，则返回并终止执行
                // icon替换创建配置
                IconDefaultChange.replaceCreateIcons(project, iconDir, newIconPath)
            }
        }
        // build添加配置文件信息
        BuildConfigUtils.addProductFlavor(project, configList, channels)
        // 更新进度
        indicator.fraction = 1.0
    }

    // 获取新配置名称
    private fun getNewConfigName(configList: Set<ConfigBean>, channels: Set<String>): Set<ConfigBean> {
        // 如果渠道选择唯一，那么直接拼接渠道名称,否则拼接_new
        val newConfigNameEnd = if (channels.size == 1) "_" + channels.first() else "_new"
        // 返回新的配置名称集合
        return configList.map {
            ConfigBean(it.configName + newConfigNameEnd, it.appName, it.channelValue)
        }.toSet()
    }
}