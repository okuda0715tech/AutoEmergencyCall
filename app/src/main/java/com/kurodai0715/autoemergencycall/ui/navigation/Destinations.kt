package com.kurodai0715.autoemergencycall.ui.navigation

import kotlinx.serialization.Serializable

/**
 * 全ての Destination のベースとなるオブジェクト.
 *
 * このオブジェクトが存在しないと、様々な Destination が渡される可能性がある部分 (例えばメニュー機能など) で、
 * Any 型を使うことになってしまう。
 * Any 型では制約がなさすぎて危険であるため、避ける必要がある。
 */
@Serializable
sealed interface NavDestination

@Serializable
data object Home : NavDestination

@Serializable
data object RegisterContact : NavDestination

@Serializable
sealed interface Contact : NavDestination {
    @Serializable
    data object Root : Contact

    @Serializable
    data object List : Contact

    @Serializable
    data class Edit(val contactId: String? = null) : Contact
}

