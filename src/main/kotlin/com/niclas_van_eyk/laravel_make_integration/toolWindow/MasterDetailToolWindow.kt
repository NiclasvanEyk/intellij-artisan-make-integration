package com.niclas_van_eyk.laravel_make_integration.toolWindow

import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.ui.Splitter
import com.intellij.ui.JBColor
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.SideBorder
import com.intellij.ui.components.JBScrollPane
import javax.swing.JComponent

class MasterDetailToolWindow(leftChild: JComponent, rightChild: JComponent): SimpleToolWindowPanel(false) {
    val component = OnePixelSplitter()

    init {
        add(component.apply {
            firstComponent = JBScrollPane(leftChild).apply {
                border = SideBorder(JBColor.border(), SideBorder.LEFT)
            }

            secondComponent = rightChild

            lackOfSpaceStrategy = Splitter.LackOfSpaceStrategy.HONOR_THE_FIRST_MIN_SIZE
            setResizeEnabled(true)
        })
    }
}