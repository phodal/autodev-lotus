package com.phodal.lotus.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.phodal.lotus.CoroutineScopeHolder
import com.phodal.lotus.chat.ChatApp
import com.phodal.lotus.chat.repository.ChatRepository
import com.phodal.lotus.chat.viewmodel.ChatViewModel
import org.jetbrains.jewel.bridge.addComposeTab


class CodeLotusWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        chatApp(project, toolWindow)
    }

    override fun shouldBeAvailable(project: Project) = true

    private fun chatApp(project: Project, toolWindow: ToolWindow) {
        val viewModel = ChatViewModel(
            project.service<CoroutineScopeHolder>()
                .createScope(ChatViewModel::class.java.simpleName),
            service<ChatRepository>()
        )
        Disposer.register(toolWindow.disposable, viewModel)

        toolWindow.addComposeTab("Chat App") { ChatApp(viewModel) }
    }
}
