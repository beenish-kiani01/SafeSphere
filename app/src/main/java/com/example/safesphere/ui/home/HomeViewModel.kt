package com.example.safesphere.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class HomeFeature(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val isPinned: Boolean = false,
    val isVisible: Boolean = true
)

class HomeViewModel : ViewModel() {
    private val _features = MutableStateFlow(listOf(
        HomeFeature("sos", "SOS Emergency", Icons.Default.Warning, isPinned = true),
        HomeFeature("contacts", "Emergency Contacts", Icons.Default.Contacts),
        HomeFeature("route", "Find Safe Route", Icons.Default.Map),
        HomeFeature("report", "Report Unsafe Area", Icons.Default.AddLocation),
        HomeFeature("fake_call", "Fake Call", Icons.Default.Call),
        HomeFeature("share", "Share Location", Icons.Default.Share)
    ))
    val features: StateFlow<List<HomeFeature>> = _features

    fun getSubtitle(id: String): String {
        return when (id) {
            "sos" -> "Immediate emergency assistance"
            "contacts" -> "Quick access to trusted people"
            "route" -> "Find safer paths nearby"
            "report" -> "Help others stay aware"
            "fake_call" -> "Exit uncomfortable situations"
            "share" -> "Send live location instantly"
            else -> ""
        }
    }

    fun togglePin(id: String) {
        _features.value = _features.value.map {
            if (it.id == id) it.copy(isPinned = !it.isPinned) else it
        }.sortedByDescending { it.isPinned }
    }

    fun toggleVisibility(id: String) {
        _features.value = _features.value.map {
            if (it.id == id) it.copy(isVisible = !it.isVisible) else it
        }
    }
    
    fun reorder(fromIndex: Int, toIndex: Int) {
        val list = _features.value.toMutableList()
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        _features.value = list
    }
}
