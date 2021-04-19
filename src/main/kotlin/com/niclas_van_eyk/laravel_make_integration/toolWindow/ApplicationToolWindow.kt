package com.niclas_van_eyk.laravel_make_integration.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.JBSplitter
import com.intellij.ui.treeStructure.Tree
import com.niclas_van_eyk.laravel_make_integration.services.LaravelMakeIntegrationProjectService
import javax.swing.JPanel

class ApplicationToolWindow(
    val toolwindow: ToolWindow,
    val project: Project
): JPanel() {
    // TODO: The ProfilerToolWindow can be used as an
    //       inspiration for typography / layout
}