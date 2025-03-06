package com.hope.iconplugin.icons.utils

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicReference

/**
 * 默认app目录下icon替换
 */
object IconDefaultChange {
    fun changeIcon(project: Project?, iconFolderPath: String?) {
        // 传入的路径是否为空
        if (iconFolderPath.isNullOrEmpty() || project == null) {
            Messages.showInfoMessage("$iconFolderPath 路径文件夹不存在", "错误")
            return
        }

        // 检查 Android 资源目录
        val resDir = LocalFileSystem.getInstance().findFileByPath(
            project.basePath + "/app/src/main/res"
        )
        // 资源目录是否正确
        if (resDir == null || !resDir.exists()) {
            Messages.showErrorDialog(project, "未找到路径: app/src/main/res", "错误")
            return
        }

        // 检查和创建 mipmap 文件夹，替换图标
        val success = replaceCreateIcons(project, resDir, iconFolderPath)
        if (success) {
            Messages.showInfoMessage("所有图标已成功替换。", "替换完成")
        } else {
            Messages.showInfoMessage("部分图标替换失败", "替换完成")
        }
    }

    // 图标替换创建功能
    fun replaceCreateIcons(project: Project?, resDir: VirtualFile, iconFolderPath: String): Boolean {
        // 资源文件尺寸
        val densities = arrayOf("hdpi", "mdpi", "xhdpi", "xxhdpi", "xxxhdpi")
        val sizes = intArrayOf(144, 192, 256, 384, 512)
        // 返回状态
        var allSuccessful = true

        for (i in densities.indices) {
            val density = densities[i]
            val size = sizes[i]
            val mipmapFolderName = "mipmap-$density"

            // 获取资源文件下对应尺寸的icon文件夹
            val mipmapDir = AtomicReference(resDir.findChild(mipmapFolderName))
            if (mipmapDir.get() == null) {
                // 创建文件夹如果不存在
                WriteCommandAction.runWriteCommandAction(project) {
                    try {
                        mipmapDir.set(resDir.createChildDirectory(this, mipmapFolderName))
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                    }
                }
            }

            // 替换图标文件
            val sourceIconPath = Paths.get(iconFolderPath, "$size.png")
            if (Files.exists(sourceIconPath)) {
                val finalMipmapDir = mipmapDir.get()
                WriteCommandAction.runWriteCommandAction(project) {
                    try {
                        val iconFile =
                            finalMipmapDir!!.findOrCreateChildData(this, "ic_launcher.png")
                        iconFile.setBinaryContent(Files.readAllBytes(sourceIconPath))
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                    }
                }
            } else {
                allSuccessful = false
                Messages.showErrorDialog(project, "缺少尺寸 $size 的图标", "图标缺失")
            }
        }
        return allSuccessful
    }
}
