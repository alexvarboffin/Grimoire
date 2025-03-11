package data.adb

class AdbClientService {
    companion object {
        const val MSG_GET_APP_INFO = 1
        const val MSG_GET_ALL_APPS_INFO = 2
        const val MSG_GET_APP_ICON = 3

        const val EXTRA_PACKAGE_NAME = "package_name"
        const val EXTRA_RESPONSE = "response"
    }
}
