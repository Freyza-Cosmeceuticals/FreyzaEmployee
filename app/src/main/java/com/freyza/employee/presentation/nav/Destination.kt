package com.freyza.employee.presentation.nav

interface Destination {
    val route: String
    val title: String
}

object UnauthenticatedDestination : Destination {
    override val route: String = "unauthenticated"
    override val title: String = "Unauthenticated"
}

object LoginDestination : Destination {
    override val route: String = "login"
    override val title: String = "Login"
}

object RegisterDestination : Destination {
    override val route: String = "register"
    override val title: String = "Register"
}

object AuthenticatedDestination : Destination {
    override val route: String = "authenticated"
    override val title: String = "Authenticated"
}

object HomeDestination : Destination {
    override val route: String = "home"
    override val title: String = "Home"
}

object AddExpenseDestination : Destination {
    override val route: String = "add_expense"
    override val title: String = "Add Expense"
}

object ExpenseDetailDestination : Destination {
    override val route: String = "expense_detail"
    override val title: String = "Expense Details"
}

sealed class NavigationRoutes {
    sealed class Unauthenticated(val route: String) : NavigationRoutes() {
        object NavigationRoute : Unauthenticated(route = UnauthenticatedDestination.route)
        object Login : Unauthenticated(route = LoginDestination.route)
        object Register : Unauthenticated(route = RegisterDestination.route)
    }

    sealed class Authenticated(val route: String) : NavigationRoutes() {
        object NavigationRoute : Authenticated(route = AuthenticatedDestination.route)
        object Home : Authenticated(route = HomeDestination.route)
        object AddExpense : Authenticated(route = AddExpenseDestination.route)
        object ExpenseDetail : Authenticated(route = ExpenseDetailDestination.route)
    }
}
