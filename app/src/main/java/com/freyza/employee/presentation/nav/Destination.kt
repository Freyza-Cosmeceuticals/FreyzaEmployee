package com.freyza.employee.presentation.nav

import kotlinx.serialization.Serializable

interface Destination {
    val title: String
}

object UnauthenticatedDestination : Destination {
    override val title: String = "Unauthenticated"
}

object LoginDestination : Destination {
    override val title: String = "Login"
}

object RegisterDestination : Destination {
    override val title: String = "Register"
}

object AuthenticatedDestination : Destination {
    override val title: String = "Authenticated"
}

object HomeDestination : Destination {
    override val title: String = "Home"
}

object AddExpenseDestination : Destination {
    override val title: String = "Add Expense"
}

object ExpenseDetailDestination : Destination {
    override val title: String = "Expense Details"
}

sealed class NavigationRoutes {
    sealed class Unauthenticated() : NavigationRoutes() {
        @Serializable
        object NavigationRoute : Unauthenticated()

        @Serializable
        object Login : Unauthenticated()

        @Serializable
        object Register : Unauthenticated()
    }

    sealed class Authenticated() : NavigationRoutes() {
        @Serializable
        object NavigationRoute : Authenticated()

        @Serializable
        object Home : Authenticated()

        @Serializable
        object AddExpense : Authenticated()

        @Serializable
        data class ExpenseDetail(val expenseId: String) : Authenticated()
    }
}
