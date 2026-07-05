package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.YhwhViewModel
import com.example.ui.viewmodel.YhwhViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as YhwhApplication
        val viewModel = ViewModelProvider(
            this,
            YhwhViewModelFactory(app, app.repository)
        )[YhwhViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: YhwhViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide Bottom Navigation on Auth screen and Invoice Receipt screen for complete focus
    val showBottomBar = currentRoute != "auth" && currentRoute != "receipt"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier.testTag("app_bottom_bar")
                ) {
                    val items = listOf(
                        BottomNavItem("Home", "dashboard", Icons.Default.Home),
                        BottomNavItem("Billing", "billing", Icons.Default.PointOfSale),
                        BottomNavItem("Catalog", "products", Icons.Default.Inventory),
                        BottomNavItem("Audit Log", "stock", Icons.Default.Tune),
                        BottomNavItem("Configuration", "settings", Icons.Default.Settings)
                    )

                    items.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        // Pop up to the start destination of the graph to
                                        // avoid building up a large stack of destinations
                                        popUpTo("dashboard") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.testTag("bottom_tab_${item.route}")
                                )
                            },
                            label = { Text(item.label, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "auth",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("auth") {
                AuthScreen(
                    viewModel = viewModel,
                    onNavigateToDashboard = {
                        navController.navigate("dashboard") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }

            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToBilling = { navController.navigate("billing") },
                    onNavigateToPurchase = { navController.navigate("purchase") },
                    onNavigateToProducts = { navController.navigate("products") },
                    onNavigateToStock = { navController.navigate("stock") },
                    onNavigateToReports = { navController.navigate("reports") }
                )
            }

            composable("billing") {
                BillingScreen(
                    viewModel = viewModel,
                    onNavigateToReceipt = { navController.navigate("receipt") },
                    onNavigateToAddCustomer = { navController.navigate("customers") }
                )
            }

            composable("receipt") {
                ReceiptScreen(
                    viewModel = viewModel,
                    onNavigateBackToBilling = {
                        navController.navigate("billing") {
                            popUpTo("receipt") { inclusive = true }
                        }
                    },
                    onNavigateToDashboard = {
                        navController.navigate("dashboard") {
                            popUpTo("receipt") { inclusive = true }
                        }
                    }
                )
            }

            composable("purchase") {
                PurchasesScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddSupplier = { navController.navigate("suppliers") }
                )
            }

            composable("products") {
                ProductsScreen(viewModel = viewModel)
            }

            composable("stock") {
                StockScreen(viewModel = viewModel)
            }

            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToAuth = {
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable("suppliers") {
                SuppliersScreen(viewModel = viewModel)
            }

            composable("customers") {
                CustomersScreen(viewModel = viewModel)
            }

            composable("reports") {
                ReportsScreen(viewModel = viewModel)
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)
