package com.kurodai0715.autoemergencycall.ui.screen

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.kurodai0715.autoemergencycall.BuildConfig
import com.kurodai0715.autoemergencycall.R
import com.kurodai0715.autoemergencycall.ui.navigation.AppNavGraph
import com.kurodai0715.autoemergencycall.ui.navigation.Config
import com.kurodai0715.autoemergencycall.ui.navigation.Contact
import com.kurodai0715.autoemergencycall.ui.navigation.Developer
import com.kurodai0715.autoemergencycall.ui.navigation.Home
import com.kurodai0715.autoemergencycall.ui.navigation.NavDestination
import com.kurodai0715.directdebitmanager.ui.theme.ICON_DEF_SIZE
import com.kurodai0715.directdebitmanager.ui.theme.LayoutTokens
import com.kurodai0715.directdebitmanager.ui.util.debouncedClick
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBaseScreen() {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var navDest: NavDestination by remember { mutableStateOf(Home) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                currentDest = navDest,
                onClickItem = { navDestination ->
                    // この警告 [Assigned value is never read] は無視して OK。
                    // 正しいのにコンパイラが正しく認識できていないため。
                    navDest = navDestination
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        },
    ) {
        @StringRes var screenHeaderTitle by remember { mutableIntStateOf(R.string.no_title) }

        Scaffold(
            topBar = {
                AppTopBar(
                    onClickMenu = {
                        scope.launch {
                            drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    },
                    title = stringResource(screenHeaderTitle),
                )
            }
        ) { contentPadding ->
            AppNavGraph(
                modifier = Modifier
                    .padding(contentPadding)
                    .consumeWindowInsets(contentPadding),
                onChangeTitle = {
                    // この警告 [Assigned value is never read] は無視して OK。
                    // 正しいのにコンパイラが正しく認識できていないため。
                    screenHeaderTitle = it
                },
                startDestination = navDest,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    onClickMenu: () -> Unit,
    title: String,
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        title = {
            AnimatedContent(targetState = title) { targetState ->
                Text(
                    text = targetState,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { debouncedClick(onClickMenu) }) {
                Icon(
                    painter = painterResource(R.drawable.baseline_menu_24),
                    contentDescription = stringResource(R.string.menu_icon_description)
                )
            }
        },
        scrollBehavior = null,
    )
}

@Composable
fun AppDrawerContent(
    currentDest: NavDestination,
    onClickItem: (NavDestination) -> Unit
) {
    ModalDrawerSheet {

        val uriHandler = LocalUriHandler.current

        Text(
            text = stringResource(R.string.header_label),
            modifier = Modifier.padding(LayoutTokens.screenPaddingHalf)
        )

        HorizontalDivider()

        NavigationDrawerItem(
            label = { Text(text = stringResource(R.string.home_screen_title)) },
            selected = currentDest is Home,
            onClick = { debouncedClick { onClickItem(Home) } }
        )

        // 💡 デバッグビルドの場合のみ表示
        if (BuildConfig.DEBUG) {
            NavigationDrawerItem(
                label = { Text(text = stringResource(R.string.developer)) },
                selected = currentDest is Developer,
                onClick = { debouncedClick { onClickItem(Developer) } }
            )
        }

        NavigationDrawerItem(
            label = { Text(text = stringResource(R.string.privacy_policy)) },
            selected = false,
            onClick = {
                debouncedClick {
                    uriHandler.openUri("https://sites.google.com/view/mamorukun/%E3%83%9B%E3%83%BC%E3%83%A0")
                }
            },
            badge = {
                Icon(
                    painter = painterResource(R.drawable.baseline_arrow_outward_24),
                    contentDescription = stringResource(R.string.open_in_outer_browser_icon_description),
                    modifier = Modifier.size(ICON_DEF_SIZE)
                )
            }
        )
    }
}

@Preview
@Composable
fun Preview() {
    AppBaseScreen()
}

@Preview
@Composable
fun PreviewOpenedAppDrawer() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Open) // ドロワーを開いた状態に設定

    ModalNavigationDrawer(
        drawerContent = {
            AppDrawerContent(
                currentDest = Home,
                onClickItem = {},
            )
        },
        drawerState = drawerState
    ) {}
}
