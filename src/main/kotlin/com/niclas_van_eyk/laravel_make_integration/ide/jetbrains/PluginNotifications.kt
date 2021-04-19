package com.niclas_van_eyk.laravel_make_integration.ide.jetbrains

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType

class PluginNotifications {
    companion object {
        const val GROUP_ID = "laravel_make_integration_notification"

        fun info(message: String, title: String = ""): Notification {
            return notification(title, message, NotificationType.INFORMATION)
        }

        fun error(message: String, title: String = ""): Notification {
            return notification(title, message, NotificationType.ERROR)
        }

        fun warning(message: String, title: String = ""): Notification {
            return notification(title, message, NotificationType.WARNING)
        }

        private fun notification(title: String, message: String, type: NotificationType): Notification {
            return NotificationGroupManager
                    .getInstance()
                    .getNotificationGroup(GROUP_ID)
                    .createNotification(title, message, type)
        }
    }
}