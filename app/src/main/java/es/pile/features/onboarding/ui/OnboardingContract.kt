package es.pile.features.onboarding.ui

data class OnboardingState(
    val currentPage: Int = 0
)

sealed interface OnboardingEvent {
    data object OnNextClicked : OnboardingEvent
    data object OnBackClicked : OnboardingEvent
    data object OnFinished : OnboardingEvent
}