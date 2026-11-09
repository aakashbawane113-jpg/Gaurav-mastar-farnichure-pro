package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import com.example.util.AppLanguage
import com.example.util.Localization
import com.example.viewmodel.Screen

@Composable
fun AppBottomBar(
    currentScreen: Screen,
    hasActiveProject: Boolean,
    language: AppLanguage,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        if (!hasActiveProject || currentScreen == Screen.Dashboard || currentScreen == Screen.Customers) {
            NavigationBarItem(
                selected = currentScreen == Screen.Dashboard,
                onClick = { onNavigate(Screen.Dashboard) },
                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                label = { Text(Localization.get("dashboard", language), fontSize = 11.sp) },
                modifier = Modifier.testTag("nav_dashboard")
            )
            NavigationBarItem(
                selected = currentScreen == Screen.Customers,
                onClick = { onNavigate(Screen.Customers) },
                icon = { Icon(Icons.Default.Groups, contentDescription = "Customers") },
                label = { Text(Localization.get("customers", language), fontSize = 11.sp) },
                modifier = Modifier.testTag("nav_customers")
            )
        } else {
            // Project tabs
            NavigationBarItem(
                selected = currentScreen == Screen.Design3D,
                onClick = { onNavigate(Screen.Design3D) },
                icon = { Icon(Icons.Default.ViewInAr, contentDescription = "3D Design") },
                label = { Text(Localization.get("design_3d", language), fontSize = 10.sp) },
                modifier = Modifier.testTag("nav_3d_design")
            )
            NavigationBarItem(
                selected = currentScreen == Screen.CuttingList,
                onClick = { onNavigate(Screen.CuttingList) },
                icon = { Icon(Icons.Default.ContentCut, contentDescription = "Cutting List") },
                label = { Text(Localization.get("cutting_list", language), fontSize = 10.sp) },
                modifier = Modifier.testTag("nav_cutting_list")
            )
            NavigationBarItem(
                selected = currentScreen == Screen.SheetOptimizer,
                onClick = { onNavigate(Screen.SheetOptimizer) },
                icon = { Icon(Icons.Default.GridView, contentDescription = "Optimizer") },
                label = { Text(Localization.get("sheet_optimizer", language), fontSize = 10.sp) },
                modifier = Modifier.testTag("nav_sheet_optimizer")
            )
            NavigationBarItem(
                selected = currentScreen == Screen.EstimationView,
                onClick = { onNavigate(Screen.EstimationView) },
                icon = { Icon(Icons.Default.Paid, contentDescription = "Estimation") },
                label = { Text(Localization.get("estimation", language), fontSize = 10.sp) },
                modifier = Modifier.testTag("nav_estimation")
            )
            NavigationBarItem(
                selected = currentScreen == Screen.QuotationPreview,
                onClick = { onNavigate(Screen.QuotationPreview) },
                icon = { Icon(Icons.Default.PictureAsPdf, contentDescription = "Quotation") },
                label = { Text(Localization.get("quotation", language), fontSize = 10.sp) },
                modifier = Modifier.testTag("nav_quotation")
            )
        }
    }
}
