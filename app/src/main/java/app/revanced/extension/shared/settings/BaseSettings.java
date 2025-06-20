package app.revanced.extension.shared.settings;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static app.revanced.extension.shared.settings.Setting.parent;

import io.github.chsbuffer.revancedxposed.BuildConfig;

/**
 * Settings shared across multiple apps.
 * <p>
 * To ensure this class is loaded when the UI is created, app specific setting bundles should extend
 * or reference this class.
 */
public class BaseSettings {
    public static final BooleanSetting DEBUG = new BooleanSetting("revanced_debug", BuildConfig.DEBUG);
    public static final BooleanSetting DEBUG_STACKTRACE = new BooleanSetting("revanced_debug_stacktrace", FALSE, parent(DEBUG));
    public static final BooleanSetting DEBUG_TOAST_ON_ERROR = new BooleanSetting("revanced_debug_toast_on_error", TRUE, "revanced_debug_toast_on_error_user_dialog_message");

    public static final IntegerSetting CHECK_ENVIRONMENT_WARNINGS_ISSUED = new IntegerSetting("revanced_check_environment_warnings_issued", 0, true, false);

    public static final EnumSetting<AppLanguage> REVANCED_LANGUAGE = new EnumSetting<>("revanced_language", AppLanguage.DEFAULT, true, "revanced_language_user_dialog_message");

    /**
     * Use the icons declared in the preferences created during patching. If no icons or styles are declared then this setting does nothing.
     */
    public static final BooleanSetting SHOW_MENU_ICONS = new BooleanSetting("revanced_show_menu_icons", TRUE, true);

    public static final BooleanSetting SPOOF_VIDEO_STREAMS = new BooleanSetting("revanced_spoof_video_streams", TRUE, true, "revanced_spoof_video_streams_user_dialog_message");

}
