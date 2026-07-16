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
import com.kurodai0715.autoemergencycall.ui.screen.alert_config.ConfigEditScreen
import com.kurodai0715.autoemergencycall.ui.screen.alert_config.ConfigListScreen
import com.kurodai0715.autoemergencycall.ui.screen.alert_config.ConfigViewModel
import com.kurodai0715.autoemergencycall.ui.screen.app_info.AppInfoScreen
import com.kurodai0715.autoemergencycall.ui.screen.contact.ContactEditScreen
import com.kurodai0715.autoemergencycall.ui.screen.contact.ContactListScreen
import com.kurodai0715.autoemergencycall.ui.screen.contact.ContactViewModel
import com.kurodai0715.autoemergencycall.ui.screen.developer_only.DeveloperScreen
import com.kurodai0715.autoemergencycall.ui.screen.sms_test.TestSmsScreen
import com.kurodai0715.autoemergencycall.ui.screen.user_settings.UserNameRegistrationScreen

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
            com.kurodai0715.autoemergencycall.ui.screen.home.HomeScreen(
                onNavigateToProfile = { navController.navigate(Profile) },
                onNavigateToContacts = { navController.navigate(Contact.Root) },
                onNavigateToConfigs = { navController.navigate(Alart.Root) },
                onNavigateToTest = { navController.navigate(Test) },
            )

            onChangeTitle(R.string.home_screen_title)
        }

        // 💡 【追加】Profile（利用者の名前登録）画面のデスティネーション
        composable<Profile> {
            // 先ほど作成した Compose 画面を呼び出します（パッケージ名はプロジェクト構造に合わせて適宜補完してください）
            UserNameRegistrationScreen(
                onSaveSuccess = {
                    navController.popBackStack() // 保存できたらホーム画面に戻る
                }
            )

            // アプリバーのタイトルを「利用者の名前設定」に変更
            onChangeTitle(R.string.profile_screen_title)
        }

        composable<Test> {
            TestSmsScreen {
                navController.popBackStack()
            }

            onChangeTitle(R.string.sms_send_test_title)
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
                    },
                    onNavigateBack = { navController.popBackStack() }
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

        navigation<Alart.Root>(startDestination = Alart.List) {
            composable<Alart.List> { backStackEntry ->
                // 「Contact」のバックスタックエントリーを安全に検索して取得
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry<Alart.Root>()
                }
                // 親のライフサイクルに紐づいた ViewModel を取得
                val viewModel: ConfigViewModel = hiltViewModel(parentEntry)

                ConfigListScreen(
                    viewModel = viewModel,
                    onNavigateToEdit = { id ->
                        navController.navigate(Alart.Edit(configId = id))
                    },
                    onNavigateBack = { navController.popBackStack() },
                )

                onChangeTitle(R.string.alert_config_list_screen_title)
            }

            composable<Alart.Edit> { backStackEntry ->
                val editRoute: Alart.Edit = backStackEntry.toRoute()

                // 編集画面でも、全く同じ「Config」の親エントリーを指定して取得
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry<Alart.Root>()
                }
                // これにより、一覧画面と100%同一のインスタンスが保証される
                val viewModel: ConfigViewModel = hiltViewModel(parentEntry)

                ConfigEditScreen(
                    configId = editRoute.configId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )

                onChangeTitle(R.string.alert_config_edit_screen_title)
            }
        }

        composable<Developer> {
            DeveloperScreen()

            onChangeTitle(R.string.developer_screen_title)
        }

        composable<AppInfo> {
            AppInfoScreen()

            onChangeTitle(R.string.app_info_screen_title)
        }
    }
}
