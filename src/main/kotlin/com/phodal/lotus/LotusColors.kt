package com.phodal.lotus

import com.intellij.ui.IconManager

@Suppress("unused")
object ComposeIcons {
    @JvmField
    val ComposeToolWindow =
        IconManager.getInstance().getIcon("/icons/composeToolWindow.svg", javaClass.getClassLoader())

    @JvmField
    val Lotus =
        IconManager.getInstance().getIcon("/icons/lotus.svg", javaClass.getClassLoader())
}