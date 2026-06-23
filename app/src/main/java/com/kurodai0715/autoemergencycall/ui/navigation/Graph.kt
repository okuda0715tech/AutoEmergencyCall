package com.kurodai0715.autoemergencycall.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.kurodai0715.autoemergencycall.R
import com.kurodai0715.autoemergencycall.ui.screen.contact_edit.ContactEditScreen
import com.kurodai0715.autoemergencycall.ui.screen.contact_list.ContactListScreen
import com.kurodai0715.autoemergencycall.ui.screen.contact_list.ContactViewModel
import com.kurodai0715.autoemergencycall.ui.screen.home.Screen as HomeScreen
import com.kurodai0715.autoemergencycall.ui.screen.register_contact.Screen as RegisterContactScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier,
    onChangeTitle: (Int) -> Unit,
    startDestination: Any,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<Home> {
            HomeScreen()

            onChangeTitle(R.string.home_screen_title)
        }

        composable<RegisterContact> {
            RegisterContactScreen()
        }

        navigation<Contact.Root>(startDestination = Contact.List) {
            composable<Contact.List> { backStackEntry ->
                // 「Contact」のバックスタックエントリーを安全に検索して取得
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry<Contact.Root>()
                }
                // 親のライフサイクルに紐づいた ViewModel を取得
                val viewModel: ContactViewModel = hiltViewModel(parentEntry)

                ContactListScreen(
                    viewModel = viewModel,
                    onNavigateToEdit = { id ->
                        navController.navigate(Contact.Edit(contactId = id))
                    }
                )

                onChangeTitle(R.string.contact_list_screen_title)
            }

            composable<Contact.Edit> { backStackEntry ->
                val editRoute: Contact.Edit = backStackEntry.toRoute()

                // 編集画面でも、全く同じ「Contact」の親エントリーを指定して取得
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry<Contact.Root>()
                }
                // これにより、一覧画面と100%同一のインスタンスが保証される
                val viewModel: ContactViewModel = hiltViewModel(parentEntry)

                ContactEditScreen(
                    contactId = editRoute.contactId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )

                onChangeTitle(R.string.contact_edit_screen_title)
            }
        }
    }
}
