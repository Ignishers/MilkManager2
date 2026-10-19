package `in`.ignishers.milkmanager2.utils

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.Job

fun Modifier.dragAndDrop(
    listState: LazyListState,
    onMove: (Int, Int) -> Unit,
    onDragEnd: () -> Unit
): Modifier {
    var draggedDistance by mutableStateOf(0f)
    var draggingItemInitialOffset by mutableStateOf(0)
    var draggingItemIndex by mutableStateOf<Int?>(null)
    var overscrollJob by mutableStateOf<Job?>(null)

    return this.pointerInput(Unit) {
        detectDragGesturesAfterLongPress(
            onDragStart = { offset ->
                val itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { item ->
                    offset.y.toInt() in item.offset..(item.offset + item.size)
                }
                itemInfo?.let {
                    draggingItemIndex = it.index
                    draggingItemInitialOffset = it.offset
                }
            },
            onDragEnd = {
                draggingItemIndex = null
                draggedDistance = 0f
                overscrollJob?.cancel()
                onDragEnd()
            },
            onDragCancel = {
                draggingItemIndex = null
                draggedDistance = 0f
                overscrollJob?.cancel()
                onDragEnd()
            },
            onDrag = { change, dragAmount ->
                change.consume()
                draggedDistance += dragAmount.y
                val draggingIndex = draggingItemIndex ?: return@detectDragGesturesAfterLongPress

                val startOffset = draggingItemInitialOffset + draggedDistance
                val currentItem = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == draggingIndex }
                
                if (currentItem != null) {
                    val targetItemInfo = listState.layoutInfo.visibleItemsInfo.find { item ->
                        val itemCenter = item.offset + item.size / 2
                        val draggingCenter = startOffset + currentItem.size / 2
                        item.index != draggingIndex &&
                                (if (dragAmount.y > 0) draggingCenter > itemCenter else draggingCenter < itemCenter)
                    }

                    if (targetItemInfo != null) {
                        onMove(draggingIndex, targetItemInfo.index)
                        draggingItemIndex = targetItemInfo.index
                        draggedDistance += currentItem.offset - targetItemInfo.offset
                    }
                }
            }
        )
    }
}
