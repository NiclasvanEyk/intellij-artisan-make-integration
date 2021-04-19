package com.niclas_van_eyk.laravel_make_integration.toolWindow.commands

import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.ListSpeedSearch
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.niclas_van_eyk.laravel_make_integration.services.project.Command
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.SwingConstants

class CommandList(commands: List<Command>): JBList<Command>(commands) {
    fun refreshCommands(newCommands: List<Command>) {
        (model as DefaultListModel).apply {
            removeAllElements()
            addAll(newCommands)
        }
    }

    init {
        cellRenderer = object: ColoredListCellRenderer<Command>() {
            override fun customizeCellRenderer(list: JList<out Command>, value: Command?, index: Int, selected: Boolean, hasFocus: Boolean) {
                if (value != null) {
                    append(value.name, SimpleTextAttributes.REGULAR_ATTRIBUTES, true)
                    append(" ")
                    append(value.description, SimpleTextAttributes.GRAYED_ATTRIBUTES, SwingConstants.RIGHT)
                }
            }
        }

        ListSpeedSearch(this) { "${it.name} ${it.description}" }
    }
}