package com.devcli.config;

import com.devcli.cli.*;
import com.devcli.model.*;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(NativeRuntimeHints.Registrar.class)
public class NativeRuntimeHints {

    public static class Registrar implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // Model classes requiring Jackson reflection/serialization
            Class<?>[] modelClasses = new Class<?>[] {
                Achievement.class,
                ActivityEvent.class,
                Bug.class,
                Commit.class,
                Goal.class,
                HistoricalSnapshot.class,
                Insight.class,
                Issue.class,
                Learning.class,
                ProjectHealth.class,
                PullRequest.class,
                Repository.class,
                UserProfile.class,
                XPEvent.class
            };

            for (Class<?> clazz : modelClasses) {
                hints.reflection().registerType(clazz, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_METHODS,
                        MemberCategory.DECLARED_FIELDS);
            }

            // Picocli command classes requiring annotation introspection
            Class<?>[] commandClasses = new Class<?>[] {
                DevCliCommand.class,
                StatusCommand.class,
                DnaCommand.class,
                LearnCommand.class,
                ProjectsCommand.class,
                GoalCommand.class,
                XpCommand.class,
                RadarCommand.class,
                ExportCommand.class,
                LoginCommand.class,
                LogoutCommand.class,
                SyncCommand.class,
                HelpCommand.class,
                WelcomeCommand.class,
                AchievementsCommand.class,
                ActivityCommand.class,
                BugsCommand.class,
                CalendarCommand.class,
                DiffCommand.class,
                FocusCommand.class,
                GraphCommand.class,
                HistoryCommand.class,
                InsightCommand.class,
                OpenSourceCommand.class,
                PortfolioCommand.class,
                PromptCommand.class,
                ReportCommand.class,
                SnapshotCommand.class,
                StatsCommand.class,
                TimelineCommand.class,
                TrendsCommand.class,
                WrappedCommand.class
            };

            for (Class<?> clazz : commandClasses) {
                hints.reflection().registerType(clazz, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_METHODS,
                        MemberCategory.DECLARED_FIELDS);
            }

            // Resource patterns for OpenPDF & assets
            hints.resources().registerPattern("com/lowagie/text/*");
            hints.resources().registerPattern("assets/*");
        }
    }
}
