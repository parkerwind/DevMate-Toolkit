package com.hope.iconplugin.utils

import com.intellij.openapi.util.IconLoader
import java.awt.Image
import javax.swing.Icon
import javax.swing.ImageIcon

// 提取字符串中的数字
fun extractFirstNumber(input: String?): String {
    val regex = "\\d+".toRegex()
    // 使用 find 而不是 findAll 来获取第一个匹配项
    val matchResult = regex.find(input ?: return "")
    // 如果找到了匹配，转换为 Int，否则返回 null
    return matchResult?.value ?: ""
}

// 获取渠道信息配置
fun getChannelInfo(channels: Set<String>, number: String): String {
    val channelInfo = StringBuilder()
    channels.forEach {
        if (channelInfo.isNotEmpty()) {
            channelInfo.append(",")
        }
        channelInfo.append(it)
        channelInfo.append(number)
    }
    return channelInfo.toString()
}

// 缩放图标
fun scaledIcon(aClass: Class<*>, iconPath: String, width: Int, height: Int): Icon {
    val originalIcon = IconLoader.getIcon(iconPath, aClass)
    val image = (originalIcon as ImageIcon).image
    val scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH)
    return ImageIcon(scaledImage)
}