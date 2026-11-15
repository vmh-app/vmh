package eu.kanade.presentation.more.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import soup.compose.material.motion.animation.materialSharedAxisX
import soup.compose.material.motion.animation.rememberSlideDistance
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onRestoreBackup: () -> Unit,
) {
    val slideDistance = rememberSlideDistance()

    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    val steps = remember {
        listOf(
            ThemeStep(),
            StorageStep(),
            PermissionStep(),
            GuidesStep(onRestoreBackup = onRestoreBackup),
        )
    }
    val stepLabels = listOf(
        MR.strings.onboarding_eyebrow_theme,
        MR.strings.onboarding_eyebrow_storage,
        MR.strings.onboarding_eyebrow_permissions,
        MR.strings.onboarding_eyebrow_guides,
    )
    val isLastStep = currentStep == steps.lastIndex

    BackHandler(enabled = currentStep != 0) {
        currentStep--
    }

    VmhOnboardingScaffold(
        stepIndex = currentStep,
        stepCount = steps.size,
        eyebrow = stringResource(stepLabels[currentStep]),
        acceptText = stringResource(
            if (isLastStep) {
                MR.strings.onboarding_action_finish
            } else {
                MR.strings.onboarding_action_next
            },
        ),
        canAccept = steps[currentStep].isComplete,
        onAcceptClick = {
            if (isLastStep) {
                onComplete()
            } else {
                currentStep++
            }
        },
    ) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                materialSharedAxisX(
                    forward = targetState > initialState,
                    slideDistance = slideDistance,
                )
            },
            label = "stepContent",
        ) {
            steps[it].Content()
        }
    }
}
