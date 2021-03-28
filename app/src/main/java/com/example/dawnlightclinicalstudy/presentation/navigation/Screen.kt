package com.example.dawnlightclinicalstudy.presentation.navigation

sealed class Screen(val route: String) {
    object SubjectIdInput : Screen("subject_id_input")
    object HotspotConnection : Screen("hotspot_connection")
    object GraphMonitor : Screen("graph_monitor")
    object UsbTransfer : Screen("usb_transfer")
}