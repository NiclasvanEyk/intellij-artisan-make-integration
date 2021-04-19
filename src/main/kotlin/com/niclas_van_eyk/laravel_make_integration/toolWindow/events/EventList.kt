package com.niclas_van_eyk.laravel_make_integration.toolWindow.events

import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.ListSpeedSearch
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.niclas_van_eyk.laravel_make_integration.services.project.EventListenerPair
import javax.swing.DefaultListModel
import javax.swing.JList

class EventList(commands: List<EventListenerPair>): JBList<EventListenerPair>(commands) {
    fun refreshEvents(newCommands: List<EventListenerPair>) {
        (model as DefaultListModel).apply {
            removeAllElements()
            addAll(newCommands)
        }
    }

    init {
        cellRenderer = object: ColoredListCellRenderer<EventListenerPair>() {
            override fun customizeCellRenderer(list: JList<out EventListenerPair>, value: EventListenerPair?, index: Int, selected: Boolean, hasFocus: Boolean) {
                if (value != null) {
                    append(value.event.name, SimpleTextAttributes.REGULAR_ATTRIBUTES, true)
                    append(" ")
                    append(value.listener.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                }
            }
        }

        ListSpeedSearch(this) { "${it.event.name} ${it.listener.name}" }
    }
}