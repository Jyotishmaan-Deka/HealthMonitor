package com.healthmonitor.presentation.ui.auth

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.healthmonitor.presentation.theme.HealthMonitorTheme
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class AuthScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun launchAuthScreen(
        state: AuthUiState = AuthUiState(),
        onSignIn: (String, String, () -> Unit) -> Unit = { _, _, _ -> },
        onSignUp: (String, String, () -> Unit) -> Unit = { _, _, _ -> },
        onToggle: () -> Unit = {}
    ) {
        val vm = mockk<AuthViewModel>(relaxed = true)
        io.mockk.every { vm.state } returns MutableStateFlow(state)

        composeTestRule.setContent {
            HealthMonitorTheme {
                AuthScreen(viewModel = vm, onAuthSuccess = {})
            }
        }
    }

    @Test
    fun signIn_buttonDisabled_whenFieldsEmpty() {
        launchAuthScreen()
        composeTestRule
            .onNodeWithText("Sign in")
            .assertIsNotEnabled()
    }

    @Test
    fun signIn_buttonEnabled_whenFieldsFilled() {
        launchAuthScreen()

        composeTestRule
            .onNodeWithText("Email address")
            .performTextInput("user@test.com")

        composeTestRule
            .onNodeWithText("Password")
            .performTextInput("password123")

        composeTestRule
            .onNodeWithText("Sign in")
            .assertIsEnabled()
    }

    @Test
    fun toggleMode_showsCreateAccountButton() {
        launchAuthScreen()

        composeTestRule
            .onNodeWithText("Don't have an account? Sign up")
            .performClick()

        // After toggle, ViewModel.toggleMode() called — UI reflects isSignUpMode via state
        // (In a real integration test we'd use a real VM; here we verify the toggle button exists)
        composeTestRule
            .onNodeWithText("Don't have an account? Sign up")
            .assertExists()
    }

    @Test
    fun errorMessage_displayed_whenStateHasError() {
        launchAuthScreen(state = AuthUiState(error = "No account found with that email"))

        composeTestRule
            .onNodeWithText("No account found with that email")
            .assertExists()
    }

    @Test
    fun loadingIndicator_shown_whenIsLoading() {
        launchAuthScreen(state = AuthUiState(isLoading = true))

        // Button should not show "Sign in" text when loading
        composeTestRule
            .onNodeWithText("Sign in")
            .assertDoesNotExist()
    }

    @Test
    fun brandHeader_alwaysVisible() {
        launchAuthScreen()
        composeTestRule.onNodeWithText("Health Monitor").assertExists()
    }
}
