package io.github.chsbuffer.revancedxposed.youtube.layout.hide.general

import android.view.View
import app.revanced.extension.shared.Utils
import app.revanced.extension.youtube.patches.components.CommentsFilter
import app.revanced.extension.youtube.patches.components.CustomFilter
import app.revanced.extension.youtube.patches.components.DescriptionComponentsFilter
import app.revanced.extension.youtube.patches.components.KeywordContentFilter
import app.revanced.extension.youtube.patches.components.LayoutComponentsFilter
import io.github.chsbuffer.revancedxposed.new
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.scopedHook
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.InputType
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.NonInteractivePreference
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.PreferenceScreenPreference
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.TextPreference
import io.github.chsbuffer.revancedxposed.youtube.misc.litho.filter.LithoFilter
import io.github.chsbuffer.revancedxposed.youtube.misc.litho.filter.addLithoFilter
import io.github.chsbuffer.revancedxposed.youtube.misc.litho.filter.emptyComponentClass
import io.github.chsbuffer.revancedxposed.youtube.misc.navigation.NavigationBarHook
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceScreen
import org.luckypray.dexkit.wrap.DexMethod

val HideLayoutComponents = patch(
    name = "Hide layout components",
    description = "Adds options to hide general layout components.",
) {
    dependsOn(LithoFilter, NavigationBarHook)

    PreferenceScreen.PLAYER.addPreferences(
        PreferenceScreenPreference(
            key = "revanced_hide_description_components_screen",
            preferences = setOf(
                SwitchPreference("revanced_hide_ai_generated_video_summary_section"),
                SwitchPreference("revanced_hide_ask_section"),
                SwitchPreference("revanced_hide_attributes_section"),
                SwitchPreference("revanced_hide_chapters_section"),
                SwitchPreference("revanced_hide_featured_links_section"),
                SwitchPreference("revanced_hide_featured_videos_section"),
                SwitchPreference("revanced_hide_info_cards_section"),
                SwitchPreference("revanced_hide_how_this_was_made_section"),
                SwitchPreference("revanced_hide_hype_points"),
                SwitchPreference("revanced_hide_key_concepts_section"),
                SwitchPreference("revanced_hide_podcast_section"),
                SwitchPreference("revanced_hide_subscribe_button"),
                SwitchPreference("revanced_hide_transcript_section"),
            ),
        ),
        PreferenceScreenPreference(
            "revanced_comments_screen",
            preferences = setOf(
                SwitchPreference("revanced_hide_comments_ai_chat_summary"),
                SwitchPreference("revanced_hide_comments_ai_summary"),
                SwitchPreference("revanced_hide_comments_channel_guidelines"),
                SwitchPreference("revanced_hide_comments_by_members_header"),
                SwitchPreference("revanced_hide_comments_section"),
                SwitchPreference("revanced_hide_comments_community_guidelines"),
                SwitchPreference("revanced_hide_comments_create_a_short_button"),
                SwitchPreference("revanced_hide_comments_emoji_and_timestamp_buttons"),
                SwitchPreference("revanced_hide_comments_preview_comment"),
                SwitchPreference("revanced_hide_comments_thanks_button"),
            ),
            sorting = PreferenceScreenPreference.Sorting.UNSORTED,
        ),
        SwitchPreference("revanced_hide_channel_bar"),
        SwitchPreference("revanced_hide_channel_watermark"),
        SwitchPreference("revanced_hide_emergency_box"),
        SwitchPreference("revanced_hide_info_panels"),
        SwitchPreference("revanced_hide_join_membership_button"),
        SwitchPreference("revanced_hide_medical_panels"),
        SwitchPreference("revanced_hide_quick_actions"),
        SwitchPreference("revanced_hide_related_videos"),
        SwitchPreference("revanced_hide_subscribers_community_guidelines"),
        SwitchPreference("revanced_hide_timed_reactions"),
    )

    PreferenceScreen.FEED.addPreferences(
        PreferenceScreenPreference(
            key = "revanced_hide_keyword_content_screen",
            sorting = PreferenceScreenPreference.Sorting.UNSORTED,
            preferences = setOf(
                SwitchPreference("revanced_hide_keyword_content_home"),
                SwitchPreference("revanced_hide_keyword_content_subscriptions"),
                SwitchPreference("revanced_hide_keyword_content_search"),
                TextPreference(
                    "revanced_hide_keyword_content_phrases", inputType = InputType.TEXT_MULTI_LINE
                ),
                NonInteractivePreference(
                    key = "revanced_hide_keyword_content_about",
                    tag = app.revanced.extension.shared.settings.preference.BulletPointPreference::class.java
                ),
                NonInteractivePreference(
                    key = "revanced_hide_keyword_content_about_whole_words",
                    tag = app.revanced.extension.youtube.settings.preference.HtmlPreference::class.java
                ),
            ),
        ),
        PreferenceScreenPreference(
            key = "revanced_hide_filter_bar_screen",
            preferences = setOf(
                SwitchPreference("revanced_hide_filter_bar_feed_in_feed"),
                SwitchPreference("revanced_hide_filter_bar_feed_in_related_videos"),
                SwitchPreference("revanced_hide_filter_bar_feed_in_search"),
                SwitchPreference("revanced_hide_filter_bar_feed_in_history"),
            ),
        ),
        PreferenceScreenPreference(
            key = "revanced_channel_screen",
            preferences = setOf(
                SwitchPreference("revanced_hide_community_button"),
                SwitchPreference("revanced_hide_for_you_shelf"),
                SwitchPreference("revanced_hide_join_button"),
                SwitchPreference("revanced_hide_links_preview"),
                SwitchPreference("revanced_hide_members_shelf"),
                SwitchPreference("revanced_hide_store_button"),
                SwitchPreference("revanced_hide_subscribe_button_in_channel_page"),
            ),
        ),
        SwitchPreference("revanced_hide_album_cards"),
        SwitchPreference("revanced_hide_artist_cards"),
        SwitchPreference("revanced_hide_chips_shelf"),
        SwitchPreference("revanced_hide_community_posts"),
        SwitchPreference("revanced_hide_compact_banner"),
        SwitchPreference("revanced_hide_crowdfunding_box"),
        SwitchPreference("revanced_hide_expandable_card"),
//        SwitchPreference("revanced_hide_floating_microphone_button"),
        SwitchPreference(
            key = "revanced_hide_horizontal_shelves",
            tag = app.revanced.extension.shared.settings.preference.BulletPointSwitchPreference::class.java
        ),
        SwitchPreference("revanced_hide_image_shelf"),
        SwitchPreference("revanced_hide_latest_posts"),
        SwitchPreference("revanced_hide_mix_playlists"),
        SwitchPreference("revanced_hide_movies_section"),
        SwitchPreference("revanced_hide_notify_me_button"),
        SwitchPreference("revanced_hide_playables"),
        SwitchPreference("revanced_hide_show_more_button"),
        SwitchPreference("revanced_hide_surveys"),
        SwitchPreference("revanced_hide_ticket_shelf"),
//        SwitchPreference("revanced_hide_upload_time"),
        SwitchPreference("revanced_hide_video_recommendation_labels"),
//        SwitchPreference("revanced_hide_view_count"),
        SwitchPreference("revanced_hide_visual_spacer"),
//        SwitchPreference("revanced_hide_doodles"),
    )

    PreferenceScreen.GENERAL_LAYOUT.addPreferences(
        PreferenceScreenPreference(
            key = "revanced_custom_filter_screen",
            sorting = PreferenceScreenPreference.Sorting.UNSORTED,
            preferences = setOf(
                SwitchPreference("revanced_custom_filter"),
                TextPreference(
                    "revanced_custom_filter_strings", inputType = InputType.TEXT_MULTI_LINE
                ),
            ),
        ),
    )

    addLithoFilter(LayoutComponentsFilter())
    addLithoFilter(DescriptionComponentsFilter())
    addLithoFilter(CommentsFilter())
    addLithoFilter(KeywordContentFilter())
    addLithoFilter(CustomFilter())

    // region Mix playlists

    ::parseElementFromBufferFingerprint.hookMethod({
        val emptyComponent = ::emptyComponentClass.clazz.new()
        val conversionContextField = ::conversionContextField.field
        after {
            val conversionContext = conversionContextField.get(it.thisObject)
            val bytes = it.args[2] as ByteArray
            if (LayoutComponentsFilter.filterMixPlaylists(conversionContext, bytes)) {
                it.result = emptyComponent
            }
        }
    })

    // endregion

    // region Watermark (legacy code for old versions of YouTube)

    ::showWatermarkFingerprint.hookMethod(scopedHook(::showWatermarkSubFingerprint.member) {
        before { it.args[1] = LayoutComponentsFilter.showWatermark() }
    })

    // endregion

    // region hide Show more button, crowdfunding box and album cards

    DexMethod("Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;)Landroid/view/View;").hookMethod {
        after {
            val name = Utils.getContext().resources.getResourceName(it.args[0] as Int)
                .substringAfterLast('.')
            when {
                name.startsWith("expand_button_down") -> {
                    LayoutComponentsFilter.hideShowMoreButton(it.result as View)
                }

                name.startsWith("donation_companion") -> {
                    LayoutComponentsFilter.hideCrowdfundingBox(it.result as View)
                }

                name == "album_card" -> {
                    LayoutComponentsFilter.hideAlbumCard(it.result as View)
                }
            }
        }
    }

    // endregion

    // TODO hide floating microphone

    // TODO 'Yoodles'

    // TODO hide view count

    // region hide filter bar

    DexMethod("Landroid/content/res/Resources;->getDimensionPixelSize(I)I").hookMethod {
        val filterBarHeight = filterBarHeightId
        val barContainerHeightId = barContainerHeightId
        after {
            when (it.args[0]) {
                filterBarHeight -> it.result = LayoutComponentsFilter.hideInFeed(it.result as Int)
                barContainerHeightId -> it.result =
                    LayoutComponentsFilter.hideInSearch(it.result as Int)
            }
        }
    }
    DexMethod("Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;)Landroid/view/View;").hookMethod {
        val relatedChipCloudMarginId = relatedChipCloudMarginId
        after {
            if (it.args[0] == relatedChipCloudMarginId) LayoutComponentsFilter.hideInRelatedVideos(
                it.result as View
            )
        }
    }

    // endregion
}