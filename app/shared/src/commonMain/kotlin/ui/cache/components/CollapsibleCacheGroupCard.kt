/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.cache.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.ui.foundation.animation.AniAnimatedVisibility
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults
import me.him188.ani.utils.platform.annotations.TestOnly
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 可折叠的缓存组卡片，将同一剧集的多个缓存项合并为一个可展开的合集
 */
@Composable
fun CollapsibleCacheGroupCard(
    state: CacheGroupState,
    onPlay: (CacheEpisodeState) -> Unit,
    onResume: suspend (CacheEpisodeState) -> Unit,
    onPause: suspend (CacheEpisodeState) -> Unit,
    onDelete: suspend (CacheEpisodeState) -> Unit,
    modifier: Modifier = Modifier,
    layoutProperties: CacheGroupCardLayoutProperties = CacheGroupCardDefaults.LayoutProperties,
    shape: Shape = MaterialTheme.shapes.large,
) {
    val outerCardColors = AniThemeDefaults.primaryCardColors()
    var expanded: Boolean by rememberSaveable {
        mutableStateOf(false) // 默认折叠状态
    }

    Card(
        modifier,
        shape = shape,
        colors = outerCardColors,
    ) {
        Card(
            Modifier.fillMaxWidth()
                .clickable { expanded = !expanded },
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        ) {
            Column(
                Modifier.padding(
                    top = (layoutProperties.headerVerticalPadding - 8.dp).coerceAtLeast(0.dp),
                    bottom = layoutProperties.headerVerticalPadding,
                ),
            ) {
                Row(
                    Modifier
                        .padding(
                            start = layoutProperties.horizontalPadding,
                            end = (layoutProperties.horizontalPadding - 8.dp).coerceAtLeast(0.dp),
                            bottom = (layoutProperties.horizontalPadding - 8.dp).coerceAtLeast(0.dp),
                        ),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    val navigator = LocalNavigator.current
                    // 条目标题
                    Box(
                        Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                    ) {
                        ProvideTextStyle(MaterialTheme.typography.titleLarge) {
                            Crossfade(
                                state.cardTitle,
                                Modifier.animateContentSize(),
                            ) {
                                SelectionContainer {
                                    Column {
                                        Text(it ?: "")
                                        Text(
                                            "共 ${state.episodes.size} 集",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.alpha(0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(Modifier.align(Alignment.CenterVertically)) {
                        // 展开/收起按钮
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (expanded) "收起" else "展开"
                            )
                        }

                        state.cacheId?.let {
                            IconButton({ navigator.navigateCacheDetails(it) }) {
                                Icon(Icons.Outlined.Info, "更多缓存信息")
                            }
                        }

                        state.subjectId?.let { subjectId ->
                            IconButton({ navigator.navigateSubjectDetails(subjectId, placeholder = null) }) {
                                Icon(Icons.Outlined.ArrowOutward, "查看条目详情")
                            }
                        }
                    }
                }

                // 下载速度和上传速度
                FlowRow(
                    Modifier
                        .padding(horizontal = layoutProperties.horizontalPadding)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(
                        layoutProperties.headerInnerVerticalSpacing,
                        alignment = Alignment.CenterVertically,
                    ),
                ) {
                    ProvideTextStyleContentColor(MaterialTheme.typography.labelLarge) {
                        Row(
                            Modifier,
                            horizontalArrangement = Arrangement.spacedBy(
                                8.dp,
                                alignment = Alignment.Start,
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Download, null)
                            Box {
                                // 占用足够大的位置, 防止下载速度更新时导致上传速度的位置变了
                                Text("888.88 MB (888.88 MB/s)", Modifier.alpha(0f), softWrap = false)
                                Text(state.downloadSpeedText, softWrap = false)
                            }
                        }

                        Row(
                            Modifier,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Upload, null)
                            Text(state.uploadSpeedText, softWrap = false)
                        }
                    }
                }
            }
        }

        AniAnimatedVisibility(expanded) {
            Column(
                Modifier
                    .padding(
                        top = (layoutProperties.episodeListVerticalPadding - 8.dp).coerceAtLeast(0.dp),
                        bottom = layoutProperties.episodeListVerticalPadding,
                    )
                    .padding(horizontal = (layoutProperties.horizontalPadding - 16.dp).coerceAtLeast(0.dp)),
                verticalArrangement = Arrangement.spacedBy(layoutProperties.episodeItemSpacing), // each item already has inner paddings
            ) {
                for (episode in state.episodes) {
                    CacheEpisodeItem(
                        episode,
                        containerColor = outerCardColors.containerColor,
                        onPlay = {
                            onPlay(episode)
                        },
                        onResume = { onResume(episode) },
                        onPause = { onPause(episode) },
                        onDelete = { onDelete(episode) },
                    )
                }
            }
        }
    }
}

@OptIn(TestOnly::class)
@Preview
@Composable
private fun PreviewCollapsibleCacheGroupCard() {
    Box(Modifier.background(Color.DarkGray)) {
        CollapsibleCacheGroupCard(TestCacheGroupSates[0], {}, {}, {}, {})
    }
} 