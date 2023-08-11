package com.atrasvida.tooltip

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun ToolTipPreview() {

    var progress by remember {
        mutableStateOf(0f)
    }

    ToolTipContainer(Modifier, process = progress) {
        progress = it
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ToolTipContainer(
    modifier: Modifier,
    process: Float,
    onSeek: (position: Float) -> Unit
) {

    BoxWithConstraints {

        val width = this.constraints.maxWidth

        Column(
            modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {

            ToolTip(
                modifier = Modifier,
                process = width * process,
                contentPadding = PaddingValues(8.dp)
            ) {
                Text(
                    text = "val: ${process.toInt() * 100}",
                    modifier = Modifier,
                    color = Color.Gray,
                    fontSize = 32.sp
                )
            }

            Box(
                modifier = Modifier
                    .height(4.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .background(Color.Red)
                    .pointerInteropFilter {
                        when (it.action) {
                            MotionEvent.ACTION_DOWN,
                            MotionEvent.ACTION_MOVE,
                            MotionEvent.ACTION_UP -> {
                                onSeek(it.x / width)
                            }

                            else -> false
                        }
                        true
                    })
            {
                Track(width, process)
            }
        }

    }

}

@Composable
private fun Track(width: Int, process: Float) {
    Box(
        Modifier
            .width((width * process).pxToDp())
            .fillMaxHeight()
            .clip(RoundedCornerShape(size = 4.dp))
            .background(Color.White)
    )
}

@Composable
fun Float.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }


@Composable
fun ToolTip(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    arrowSize: Dp = 8.dp,
    process: Float,
    contentPadding: PaddingValues,
    content: @Composable BoxScope.() -> Unit
) {

    Box(
        Modifier
            .offset(process.pxToDp())
            .drawWithCache {
                onDrawBehind {
                    val width = size.width
                    val height = size.height

                    var topPadding = 0.dp.toPx()
                    var leftPadding = 0.dp.toPx() //+ process * width
                    var bottomPadding = 16.dp.toPx()
                    var rightPadding = 0.dp.toPx()

                    var radius = cornerRadius.toPx()
                    var arrowSizePx = arrowSize.toPx()

                    var X = 0


                    var path = Path().apply {
                        arcTo(
                            rect = Rect(
                                left = leftPadding,
                                top = topPadding,
                                right = leftPadding + radius,
                                bottom = topPadding + radius
                            ),
                            startAngleDegrees = -180f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )

                        arcTo(
                            rect = Rect(
                                left = width - rightPadding - radius,
                                top = topPadding,
                                right = width - rightPadding,
                                bottom = topPadding + radius
                            ),
                            startAngleDegrees = -90f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )

                        arcTo(
                            rect = Rect(
                                left = width - rightPadding - radius,
                                top = height - bottomPadding - radius,
                                right = width - rightPadding,
                                bottom = height - bottomPadding
                            ),
                            startAngleDegrees = 0f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )

                        lineTo(
                            x = rightPadding + (radius / 2) + arrowSizePx + X,
                            y = height - bottomPadding
                        )

                        lineTo(
                            x = rightPadding + (radius / 2) + (arrowSizePx / 2) + X,
                            y = (height - bottomPadding) + arrowSizePx
                        )

                        lineTo(
                            x = leftPadding + (radius / 2) + X,
                            y = height - bottomPadding
                        )

                        lineTo(
                            x = leftPadding + (radius / 2),
                            y = height - bottomPadding
                        )

                        arcTo(
                            rect = Rect(
                                left = leftPadding,
                                top = height - bottomPadding - radius,
                                right = leftPadding + radius,
                                bottom = height - bottomPadding
                            ),
                            startAngleDegrees = 90f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )

                        close()
                    }

                    clipPath(path) {
                        drawRect(color = Color.White)
                    }

                    drawPath(
                        path = path,
                        color = Color.Red,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            miter = 5f,
                            //pathEffect = PathEffect.cornerPathEffect(10f)
                        )
                    )
                }
            }
            .padding(bottom = arrowSize)
            .padding(contentPadding)
    ) {
        content()
    }

}