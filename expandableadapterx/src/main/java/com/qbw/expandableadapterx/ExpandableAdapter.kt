package com.qbw.expandableadapterx

import com.qbw.l.L
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Bond on 2016/4/2.
 */
abstract class ExpandableAdapter : BaseExpandableAdapter() {
    private val mList: MutableList<Any>
    var headerCount = 0
        private set
    var childCount = 0
        private set
    var groupCount = 0
        private set
    var groupChildCounts: MutableList<Int>? = null
        private set
    var groupAndGroupChildCount = 0
        private set
    var footerCount = 0
        private set

    override fun getItemViewType(position: Int): Int {
        val vt = getItemViewType(mList[position])
        return if (vt == -1) {
            L.GL.e("%s itemViewType is -1 ???", mList[position].javaClass.name)
            super.getItemViewType(position)
        } else {
            vt
        }
    }

    open fun getItemViewType(t: Any?): Int {
        return -1
    }

    override fun getItem(itemPosition: Int): Any {
        return mList[itemPosition]
    }

    fun getItemPosition(item: Any): Int {
        return mList.indexOf(item)
    }

    fun removeItem(itemPosition: Int) {
        if (!checkItemPosition(itemPosition)) {
            return
        }
        if (headerCount > 0 && itemPosition < headerCount) {
            headerCount--
        } else if (childCount > 0 && itemPosition < headerCount + childCount) {
            childCount--
        } else if (groupCount > 0 && itemPosition < headerCount + childCount + groupAndGroupChildCount) {
            val groupPosition = getGroupPosition(itemPosition)
            if (groupPosition != -1) {
                removeGroup(groupPosition)
                return
            } else {
                val groupChildPosition = getGroupChildPosition(itemPosition)
                removeGroupChild(groupChildPosition[0], groupChildPosition[1])
                return
            }
        } else if (footerCount > 0 && itemPosition >= mList.size - footerCount) {
            footerCount--
        } else {
            L.GL.w("Remove item failed!")
            return
        }
        mList.removeAt(itemPosition)
        notifyItemRemoved(itemPosition)
    }

    fun swapItem(sourcePosition: Int, targetPosition: Int) {
        val itemCount = itemCount
        if (sourcePosition < 0 || sourcePosition >= itemCount) {
            L.GL.e("Invalid sourcePosition %d", sourcePosition)
            return
        } else if (targetPosition < 0 || targetPosition >= itemCount) {
            L.GL.e("Invalid targetPosition %d", targetPosition)
            return
        }
        Collections.swap(mList, sourcePosition, targetPosition)
        notifyItemMoved(sourcePosition, targetPosition)
    }

    fun updateItem(itemPosition: Int, item: Any) {
        if (!checkItemPosition(itemPosition)) {
            return
        }
        mList[itemPosition] = item
        notifyItemChanged(itemPosition)
    }

    fun clear() {
        mList.clear()
        headerCount = 0
        childCount = 0
        groupCount = 0
        groupAndGroupChildCount = 0
        if (groupChildCounts != null) {
            groupChildCounts!!.clear()
        }
        footerCount = 0
        notifyDataSetChanged()
    }

    fun setHeader(header: Any) {
        val headers: MutableList<Any> = ArrayList(1)
        headers.add(header)
        setHeader(headers)
    }

    fun setHeader(newHeaderList: List<Any>) {
        setHeader(0, if (headerCount > 0) headerCount - 1 else 0, newHeaderList)
    }

    fun setHeader(beginIndex: Int, endIndex: Int, newHeaderList: List<Any>) {
        if (beginIndex < 0 || endIndex < 0 || beginIndex > endIndex) {
            L.GL.e("beginIndex %d, endIndex %d !!!", beginIndex, endIndex)
            return
        }
        val pendingSetCount = endIndex - beginIndex + 1
        val newDataSize = newHeaderList.size ?: 0
        if (newDataSize <= 0) {
            removeHeader(beginIndex, pendingSetCount)
        } else if (headerCount <= beginIndex) {
            addHeader(newHeaderList)
        } else {
            var i = 0
            while (i < pendingSetCount && i < newDataSize) {
                if (!isSameData(getHeader(i + beginIndex), newHeaderList[i])) {
                    updateHeader(i + beginIndex, newHeaderList[i])
                }
                i++
            }
            if (pendingSetCount > newDataSize) {
                clearHeader(newDataSize)
            } else if (pendingSetCount < newDataSize) {
                addHeader(
                    endIndex + 1, newHeaderList.subList(
                        pendingSetCount,
                        newDataSize
                    )
                )
            }
        }
    }

    fun addHeader(header: Any): Int {
        return addHeader(headerCount, header, null)
    }

    fun addHeader(headerPosition: Int, header: Any): Int {
        return addHeader(headerPosition, header, null)
    }

    fun addHeader(headerList: List<Any>): Int {
        return addHeader(headerCount, null, headerList)
    }

    fun addHeader(headerPosition: Int, headerList: List<Any>): Int {
        return addHeader(headerPosition, null, headerList)
    }

    private fun addHeader(headerPosition: Int, header: Any?, headerList: List<Any>?): Int {
        var headerPosition = headerPosition
        if (headerPosition < 0) {
            L.GL.e("Invalid header position %d", headerPosition)
            return -1
        } else if (header == null && (headerList == null || headerList.isEmpty())) {
            L.GL.e("Invalid header parameter")
            return -1
        }
        if (headerPosition > headerCount) {
            headerPosition = headerCount
        }
        val itemPosition = headerPosition
        val itemAddSize: Int = if (header != null) {
            mList.add(itemPosition, header)
            1
        } else {
            mList.addAll(itemPosition, headerList!!)
            headerList.size
        }
        L.GL.v("Notify item from %d, count is %d", itemPosition, itemAddSize)
        headerCount += itemAddSize
        notifyItemRangeInserted(itemPosition, itemAddSize)
        return headerPosition
    }

    fun removeHeader(header: Any) {
        val itemPosition = mList.indexOf(header)
        if (itemPosition == -1) {
            L.GL.e("Remove header fiiled for not finding the header position")
            return
        }
        mList.removeAt(itemPosition)
        headerCount--
        notifyItemRemoved(itemPosition)
    }

    fun removeHeaders(headers: List<Any>?) {
        val size = headers?.size ?: 0
        for (i in 0 until size) {
            removeHeader(headers!![i])
        }
    }

    fun removeHeader(headerPosition: Int) {
        removeHeader(headerPosition, 1)
    }

    fun clearHeader() {
        removeHeader(0, headerCount)
    }

    fun clearHeader(headerBeginPosition: Int) {
        removeHeader(headerBeginPosition, headerCount - headerBeginPosition)
    }

    fun removeHeader(headerBeginPosition: Int, removeCount: Int) {
        var removeCount = removeCount
        if (!checkHeaderPosition(headerBeginPosition)) {
            return
        } else if (removeCount <= 0) {
            L.GL.e("Invalid header removeCount %d", removeCount)
            return
        }
        var itemEndPosition = headerBeginPosition + removeCount
        if (itemEndPosition > headerCount) {
            itemEndPosition = headerCount
            val oldRemoveCount = removeCount
            removeCount = itemEndPosition - headerBeginPosition
            L.GL.i("Reset removeCount from %d to %d", oldRemoveCount, removeCount)
        }
        mList.subList(headerBeginPosition, itemEndPosition).clear()
        notifyItemRangeRemoved(headerBeginPosition, removeCount)
        headerCount -= removeCount
    }

    fun getHeaders(): List<Any>? {
        if (headerCount <= 0 || itemCount <= 0) {
            L.GL.w("No header items")
            return null
        }
        return ArrayList(mList.subList(0, headerCount))
    }

    fun getHeaders(viewType: Int): List<Any>? {
        val total = getHeaders()
        var match: MutableList<Any>? = null
        val size = total?.size ?: 0
        if (size > 0) {
            match = ArrayList()
        }
        var t: Any
        for (i in 0 until size) {
            t = total!![i]
            if (getItemViewType(t) == viewType) {
                match!!.add(t)
            }
        }
        return match
    }

    fun getHeader(headerPosition: Int): Any? {
        return if (!checkHeaderPosition(headerPosition)) {
            null
        } else mList[headerPosition]
    }

    fun updateHeader(headerPosition: Int, header: Any) {
        if (!checkHeaderPosition(headerPosition)) {
            return
        }
        mList[headerPosition] = header
        notifyItemChanged(headerPosition)
    }

    fun notifyHeaderChanged(headerPosition: Int) {
        if (!checkHeaderPosition(headerPosition)) {
            return
        }
        notifyItemChanged(headerPosition)
    }

    fun getHeaderCount(viewType: Int): Int {
        if (headerCount <= 0) {
            return 0
        }
        var count = 0
        for (i in 0 until headerCount) {
            if (getItemViewType(getHeader(i)) == viewType) {
                count++
            }
        }
        return count
    }

    private fun checkHeaderPosition(headerPosition: Int): Boolean {
        if (headerPosition < 0) {
            L.GL.w("Invalid header position %d", headerPosition)
            return false
        } else if (headerPosition >= headerCount) {
            L.GL.w("Invalid header position %d, header size is %d", headerPosition, headerCount)
            return false
        }
        return true
    }

    fun getHeaderPosition(itemPosition: Int): Int {
        return if (!checkItemPosition(itemPosition) || itemPosition >= headerCount) {
            -1
        } else itemPosition
    }

    fun getHeaderPosition(header: Any): Int {
        return mList.indexOf(header)
    }

    fun convertHeaderPosition(headerPosition: Int): Int {
        return if (!checkHeaderPosition(headerPosition)) {
            -1
        } else headerPosition
    }

    fun setChild(child: Any) {
        val childs: MutableList<Any> = ArrayList(1)
        childs.add(child)
        setChild(childs)
    }

    fun setChild(newChildList: List<Any>) {
        setChild(0, if (childCount > 0) childCount - 1 else 0, newChildList)
    }

    fun setChild(beginIndex: Int, endIndex: Int, newChildList: List<Any>) {
        if (beginIndex < 0 || endIndex < 0 || beginIndex > endIndex) {
            L.GL.e("beginIndex %d, endIndex %d !!!", beginIndex, endIndex)
            return
        }
        val pendingSetCount = endIndex - beginIndex + 1
        val newDataSize = newChildList?.size ?: 0
        if (newDataSize <= 0) {
            removeChild(beginIndex, pendingSetCount)
        } else if (childCount <= beginIndex) {
            addChild(newChildList)
        } else {
            var i = 0
            while (i < pendingSetCount && i < newDataSize) {
                if (!isSameData(getChild(i + beginIndex), newChildList!![i])) {
                    updateChild(i + beginIndex, newChildList[i])
                }
                i++
            }
            if (pendingSetCount > newDataSize) {
                clearChild(newDataSize)
            } else if (pendingSetCount < newDataSize) {
                addChild(
                    endIndex + 1, newChildList!!.subList(
                        pendingSetCount,
                        newDataSize
                    )
                )
            }
        }
    }

    fun addChild(child: Any): Int {
        return addChild(childCount, child, null)
    }

    fun addChild(childList: List<Any>): Int {
        return addChild(childCount, null, childList)
    }

    fun addChild(childPosition: Int, childList: List<Any>): Int {
        return addChild(childPosition, null, childList)
    }

    @JvmOverloads
    fun addChild(childPosition: Int, child: Any?, childList: List<Any>? = null): Int {
        var childPosition = childPosition
        if (childPosition < 0) {
            L.GL.e("Invalid child position %d", childPosition)
            return -1
        } else if (null == child && (null == childList || childList.isEmpty())) {
            L.GL.e("Invalid child parameter")
            return -1
        }
        if (childPosition > childCount) {
            childPosition = childCount
        }
        val itemPosition = headerCount + childPosition
        val addSize: Int
        addSize = if (child != null) {
            mList.add(itemPosition, child)
            1
        } else {
            mList.addAll(itemPosition, childList!!)
            childList.size
        }
        L.GL.v("Notify item from %d, count is %d", itemPosition, addSize)
        childCount += addSize
        notifyItemRangeInserted(itemPosition, addSize)
        return childPosition
    }

    fun removeChild(childPosition: Int) {
        removeChild(childPosition, 1)
    }

    fun removeChild(child: Any) {
        val itemPosition = indexOfChild(child)
        if (itemPosition == -1) {
            L.GL.e("Remove the child failed for not finding the child position")
            return
        }
        mList.removeAt(itemPosition)
        notifyItemRemoved(itemPosition)
        childCount--
    }

    fun removeChilds(childs: List<Any>?) {
        val size = childs?.size ?: 0
        for (i in 0 until size) {
            removeChild(childs!![i])
        }
    }

    fun clearChild(childBeginPosition: Int) {
        removeChild(childBeginPosition, childCount - childBeginPosition)
    }

    fun clearChild() {
        removeChild(0, childCount)
    }

    fun removeChild(childBeginPosition: Int, removeCount: Int) {
        var removeCount = removeCount
        if (!checkChildPosition(childBeginPosition)) {
            return
        } else if (removeCount <= 0) {
            L.GL.e("Invalid child removeCount %d", removeCount)
            return
        }
        val itemBeginPosition = convertChildPosition(childBeginPosition)
        val headerChildCount = headerCount + childCount
        var itemEndPosition = itemBeginPosition + removeCount
        if (itemEndPosition > headerChildCount) {
            itemEndPosition = headerChildCount
            val oldRemoveCount = removeCount
            removeCount = itemEndPosition - itemBeginPosition
            L.GL.i("Reset child removeCount from %d to %d", oldRemoveCount, removeCount)
        }
        mList.subList(itemBeginPosition, itemEndPosition).clear()
        childCount -= removeCount
        notifyItemRangeRemoved(itemBeginPosition, removeCount)
    }

    fun getChilds(): List<Any>? {
        if (childCount <= 0 || itemCount <= 0) {
            L.GL.w("No child items")
            return null
        }
        return ArrayList(mList.subList(headerCount, headerCount + childCount))
    }

    fun getChilds(viewType: Int): List<Any>? {
        val total = getChilds()
        var match: MutableList<Any>? = null
        val size = total?.size ?: 0
        if (size > 0) {
            match = ArrayList()
        }
        var t: Any
        for (i in 0 until size) {
            t = total!![i]
            if (getItemViewType(t) == viewType) {
                match!!.add(t)
            }
        }
        return match
    }

    fun getChild(childPosition: Int): Any? {
        return if (!checkChildPosition(childPosition)) {
            null
        } else mList[headerCount + childPosition]
    }

    fun updateChild(childPosition: Int, child: Any) {
        val itemPosition = convertChildPosition(childPosition)
        if (itemPosition == -1) {
            return
        }
        mList[itemPosition] = child
        notifyItemChanged(itemPosition)
    }

    fun notifyChildChanged(childPosition: Int) {
        val itemPosition = convertChildPosition(childPosition)
        if (itemPosition == -1) {
            return
        }
        notifyItemChanged(itemPosition)
    }

    fun getChildCount(viewType: Int): Int {
        if (childCount <= 0) {
            return 0
        }
        var count = 0
        for (i in 0 until childCount) {
            if (getItemViewType(getChild(i)) == viewType) {
                count++
            }
        }
        return count
    }

    private fun checkChildPosition(childPosition: Int): Boolean {
        if (childPosition < 0) {
            L.GL.w("Invalid child position %d", childPosition)
            return false
        } else if (childPosition >= childCount) {
            L.GL.w("invalid child position %d, child size is %d", childPosition, childCount)
            return false
        }
        return true
    }

    fun getChildPosition(itemPosition: Int): Int {
        if (!checkItemPosition(itemPosition)) {
            L.GL.e("invalid adapterPosition %d", itemPosition)
            return -1
        } else if (childCount <= 0 || itemPosition >= headerCount + childCount) {
            return -1
        }
        return itemPosition - headerCount
    }

    fun getChildPosition(child: Any): Int {
        val itemPosition = indexOfChild(child)
        return if (itemPosition == -1) {
            -1
        } else itemPosition - headerCount
    }

    fun convertChildPosition(childPosition: Int): Int {
        return if (!checkChildPosition(childPosition)) {
            -1
        } else headerCount + childPosition
    }

    fun indexOfChild(child: Any): Int {
        if (childCount <= 0) {
            return -1
        }
        var itemPosition = -1
        val itemBeginPosition = headerCount
        val itemEndPosition = itemBeginPosition + childCount
        for (i in itemBeginPosition until itemEndPosition) {
            if (mList[i] == child) {
                itemPosition = i
                break
            }
        }
        return itemPosition
    }

    fun addGroup(group: Any): Int {
        return addGroup(groupCount, group)
    }

    fun addGroup(groupPosition: Int, group: Any): Int {
        var groupPosition = groupPosition
        if (groupPosition < 0) {
            L.GL.e("Invalid group position %d", groupPosition)
            return -1
        } else if (indexOfGroup(group) != -1) {
            L.GL.e("Group is alread exist! You must use a different object to create a new group")
            return -1
        }
        if (groupPosition > groupCount) {
            L.GL.w("Reset group position from %d to %d", groupPosition, groupCount)
            groupPosition = groupCount
        }
        var itemPosition = 0
        for (i in 0 until groupPosition) {
            itemPosition += groupChildCounts!![i] + 1
        }
        itemPosition += headerCount + childCount
        mList.add(itemPosition, group)
        groupCount += 1
        groupAndGroupChildCount += 1
        if (groupChildCounts == null) {
            groupChildCounts = ArrayList()
        }
        groupChildCounts!!.add(groupPosition, 0)
        notifyItemInserted(itemPosition)
        return groupPosition
    }

    fun removeGroup(group: Any) {
        removeGroup(getGroupPosition(group))
    }

    fun removeGroups(groups: List<Any>?) {
        val size = groups?.size ?: 0
        for (i in 0 until size) {
            removeGroup(groups!![i])
        }
    }

    fun removeAllGroup() {
        val groupCount = groupCount
        for (i in 0 until groupCount) {
            removeGroup(0)
        }
    }

    fun clearGroup(beginIndex: Int) {
        if (beginIndex >= groupCount) {
            L.GL.w("clearGroup,beginIndex[%d] >= groupCount[%d]", beginIndex, groupCount)
            return
        }
        for (i in beginIndex until groupCount) {
            removeGroup(beginIndex)
        }
    }

    fun removeGroup(groupPosition: Int) {
        val itemPosition = convertGroupPosition(groupPosition)
        if (itemPosition == -1) {
            return
        }
        val groupChildCount = groupChildCounts!![groupPosition]
        mList.subList(itemPosition, itemPosition + groupChildCount + 1).clear()
        groupCount--
        groupAndGroupChildCount -= 1 + groupChildCounts!![groupPosition]
        groupChildCounts!!.removeAt(groupPosition)
        notifyItemRangeRemoved(itemPosition, groupChildCount + 1)
    }

    val groups: List<Any>?
        get() {
            if (groupCount <= 0) {
                return null
            }
            val groups: MutableList<Any> = ArrayList(groupCount)
            for (i in 0 until groupCount) {
                groups.add(mList[convertGroupPosition(i)])
            }
            return groups
        }

    fun getGroup(groupPosition: Int): Any? {
        val itemPosition = convertGroupPosition(groupPosition)
        return if (itemPosition == -1) {
            null
        } else mList[itemPosition]
    }

    fun updateGroup(groupPosition: Int, group: Any) {
        val itemPosition = convertGroupPosition(groupPosition)
        if (itemPosition == -1) {
            return
        }
        mList[itemPosition] = group
        notifyItemChanged(itemPosition)
    }

    fun getGroupPosition(itemPosition: Int): Int {
        if (!checkItemPosition(itemPosition) || itemPosition < headerCount + childCount) {
            L.GL.e("Invalid itemPosition %d", itemPosition)
            return -1
        }
        var groupPosition = -1
        for (i in 0 until groupCount) {
            if (itemPosition == convertGroupPosition(i)) {
                groupPosition = i
                break
            }
        }
        return groupPosition
    }

    fun getGroupPosition(group: Any): Int {
        return getGroupPosition(indexOfGroup(group))
    }

    fun convertGroupPosition(groupPosition: Int): Int {
        if (!checkGroupPosition(groupPosition)) {
            L.GL.e("Invalid group position %d", groupPosition)
            return -1
        }
        var itemPosition = 0
        for (i in 0 until groupPosition) {
            itemPosition += groupChildCounts!![i] + 1
        }
        return headerCount + childCount + itemPosition
    }

    fun indexOfGroup(group: Any): Int {
        if (group == null) {
            return -1
        }
        var itemPosition = -1
        var groupItemPosition: Int
        for (i in 0 until groupCount) {
            groupItemPosition = convertGroupPosition(i)
            if (mList[groupItemPosition] == group) {
                itemPosition = groupItemPosition
                break
            }
        }
        return itemPosition
    }

    @JvmOverloads
    fun notifyGroupChanged(groupPosition: Int, notNotifyGroup: Boolean = true): Int {
        if (!checkGroupPosition(groupPosition)) {
            L.GL.e("Invalid group position %d", groupPosition)
            return -1
        }
        if (!notNotifyGroup) {
            notifyItemChanged(convertGroupPosition(groupPosition))
        }
        val gcc = getGroupChildCount(groupPosition)
        for (i in 0 until gcc) {
            notifyGroupChildChanged(groupPosition, i)
        }
        return 0
    }

    private fun checkGroupPosition(groupPosition: Int): Boolean {
        if (groupPosition < 0) {
            L.GL.w("Invalid group position %d", groupPosition)
            return false
        } else if (groupPosition >= groupCount) {
            L.GL.w("Invalid group position %d, group size is %d", groupPosition, groupCount)
            return false
        }
        return true
    }

    fun setGroupChild(groupPosition: Int, groupChild: Any) {
        val groupChilds: MutableList<Any> = ArrayList(1)
        groupChilds.add(groupChild)
        setGroupChild(groupPosition, groupChilds)
    }

    fun setGroupChild(groupPosition: Int, newGroupChildList: List<Any>) {
        val newDataSize = newGroupChildList?.size ?: 0
        val groupChildSize = getGroupChildCount(groupPosition)
        if (newDataSize <= 0) {
            clearGroupChild(groupPosition)
        } else if (groupChildSize <= 0) {
            addGroupChild(groupPosition, newGroupChildList)
        } else {
            var i = 0
            while (i < groupChildSize && i < newDataSize) {
                if (!isSameData(getGroupChild(groupPosition, i), newGroupChildList!![i])) {
                    updateGroupChild(groupPosition, i, newGroupChildList[i])
                }
                i++
            }
            if (groupChildSize > newDataSize) {
                clearGroupChild(groupPosition, newDataSize)
            } else if (groupChildSize < newDataSize) {
                addGroupChild(
                    groupPosition, newGroupChildList!!.subList(
                        groupChildSize,
                        newDataSize
                    )
                )
            }
        }
    }

    fun addGroupChild(groupPosition: Int, groupChild: Any): IntArray {
        return if (!checkGroupPosition(groupPosition)) {
            intArrayOf(-1, -1)
        } else addGroupChild(
            groupPosition,
            groupChildCounts!![groupPosition],
            groupChild,
            null
        )
    }

    fun addGroupChild(groupPosition: Int, childList: List<Any>): IntArray {
        return if (!checkGroupPosition(groupPosition)) {
            intArrayOf(-1, -1)
        } else addGroupChild(
            groupPosition,
            groupChildCounts!![groupPosition],
            null,
            childList
        )
    }

    @JvmOverloads
    fun addGroupChild(
        groupPosition: Int,
        groupChildPosition: Int,
        groupChild: Any?,
        groupChildList: List<Any>? = null
    ): IntArray {
        var groupChildPosition = groupChildPosition
        if (!checkGroupPosition(groupPosition)) {
            return intArrayOf(-1, -1)
        } else if (groupChild == null && (groupChildList == null || groupChildList.isEmpty())) {
            L.GL.e("Invalid group child mList")
            return intArrayOf(-1, -1)
        } else if (groupChildPosition < 0) {
            L.GL.e("Invalid child position %d", groupChildPosition)
            return intArrayOf(-1, -1)
        }
        val oldGroupChildCount = groupChildCounts!![groupPosition]
        if (groupChildPosition > oldGroupChildCount) {
            groupChildPosition = oldGroupChildCount
        }
        var itemPosition = 0
        for (i in 0 until groupPosition) {
            itemPosition += groupChildCounts!![i] + 1
        }
        itemPosition += headerCount + childCount + 1 + groupChildPosition
        val addSize: Int
        addSize = if (groupChild != null) {
            mList.add(itemPosition, groupChild)
            1
        } else {
            mList.addAll(itemPosition, groupChildList!!)
            groupChildList.size
        }
        groupChildCounts!![groupPosition] = oldGroupChildCount + addSize
        groupAndGroupChildCount += addSize
        notifyItemRangeInserted(itemPosition, addSize)
        return intArrayOf(groupPosition, groupChildPosition)
    }

    fun removeGroupChild(groupPosition: Int, groupChildPosition: Int) {
        removeGroupChild(groupPosition, groupChildPosition, 1)
    }

    fun removeGroupChildByViewType(groupPosition: Int, viewType: Int) {
        if (!checkGroupPosition(groupPosition)) {
            return
        }
        val p = getGroupChildPositionByViewType(groupPosition, viewType)
        if (p != -1) {
            removeGroupChild(groupPosition, p)
        } else {
            L.GL.w("No groupChild for viewType %d", viewType)
        }
    }

    fun clearGroupChildByViewType(groupPosition: Int, viewType: Int) {
        if (!checkGroupPosition(groupPosition)) {
            return
        }
        while (true) {
            val p = getGroupChildPositionByViewType(groupPosition, viewType)
            if (p != -1) {
                removeGroupChild(groupPosition, p)
            } else {
                L.GL.w("No groupChild for viewType %d", viewType)
                break
            }
        }
    }

    @JvmOverloads
    fun clearGroupChild(groupPosition: Int, groupChildBeginPosition: Int = 0) {
        if (!checkGroupPosition(groupPosition)) {
            return
        }
        removeGroupChild(
            groupPosition,
            groupChildBeginPosition,
            groupChildCounts!![groupPosition] - groupChildBeginPosition
        )
    }

    private fun removeGroupChild(
        groupPosition: Int,
        groupChildBeingPosition: Int,
        removeCount: Int
    ) {
        var removeCount = removeCount
        if (!checkGroupChildPosition(groupPosition, groupChildBeingPosition)) {
            return
        }
        if (removeCount <= 0) {
            L.GL.e("Invalid group remove count %d", removeCount)
            return
        }
        val groupChildCount = groupChildCounts!![groupPosition]
        var groupChildEnd = groupChildBeingPosition + removeCount
        if (groupChildEnd > groupChildCount) {
            val oldRemoveCount = removeCount
            removeCount = groupChildCount - groupChildBeingPosition
            groupChildEnd = groupChildCount
            L.GL.i("Reset group removeCount from %d to %d", oldRemoveCount, removeCount)
        }
        L.GL.d(
            "groupPosition=%d, childStarPosition=%d, count=%d, childEnd=%d",
            groupPosition,
            groupChildBeingPosition,
            removeCount,
            groupChildEnd
        )
        val itemPosition = convertGroupPosition(groupPosition)
        val itemBeginPosition = itemPosition + groupChildBeingPosition + 1
        mList.subList(itemBeginPosition, itemBeginPosition + removeCount).clear()
        groupChildCounts!![groupPosition] = groupChildCount - removeCount
        groupAndGroupChildCount -= removeCount
        notifyItemRangeRemoved(itemBeginPosition, removeCount)
    }

    fun getGroupChilds(groupPosition: Int): List<Any>? {
        if (!checkGroupPosition(groupPosition)) {
            return null
        }
        val groupChildCount = groupChildCounts!![groupPosition]
        if (groupChildCount <= 0) {
            return null
        }
        val itemPosition = convertGroupPosition(groupPosition)
        val groupChilds: MutableList<Any> = ArrayList(groupChildCount)
        for (i in 0 until groupChildCount) {
            groupChilds.add(mList[itemPosition + 1 + i])
        }
        return groupChilds
    }

    fun getGroupChild(groupPosition: Int, groupChildPosition: Int): Any? {
        if (!checkGroupChildPosition(groupPosition, groupChildPosition)) {
            return null
        }
        val itemPosition = convertGroupChildPosition(groupPosition, groupChildPosition)
        return if (itemPosition == -1) {
            null
        } else mList[itemPosition]
    }

    fun updateGroupChild(groupPosition: Int, groupChildPosition: Int, groupChild: Any) {
        if (!checkGroupChildPosition(groupPosition, groupChildPosition)) {
            return
        }
        val itemPosition = convertGroupChildPosition(groupPosition, groupChildPosition)
        if (itemPosition == -1) {
            return
        }
        mList[itemPosition] = groupChild
        notifyItemChanged(itemPosition)
    }

    fun updateGroupChild(groupPosition: Int, groupChildPosition: Int) {
        val itemPosition = convertGroupChildPosition(groupPosition, groupChildPosition)
        if (itemPosition != -1) {
            notifyItemChanged(itemPosition)
        }
    }

    override fun getGroupChildCount(groupPosition: Int): Int {
        return if (!checkGroupPosition(groupPosition)) {
            0
        } else groupChildCounts!![groupPosition]
    }

    fun notifyGroupChildChanged(groupPosition: Int, childPosition: Int) {
        val itemPosition = convertGroupChildPosition(groupPosition, childPosition)
        if (itemPosition == -1) {
            return
        }
        notifyItemChanged(itemPosition)
    }

    private fun checkGroupChildPosition(groupPosition: Int, groupChildPosition: Int): Boolean {
        var groupChildCount: Int
        if (groupChildPosition < 0) {
            L.GL.w("Invalid group child position %d, %d", groupPosition, groupChildPosition)
            return false
        } else if (!checkGroupPosition(groupPosition)) {
            return false
        } else if (groupChildPosition >= groupChildCounts!![groupPosition].also {
                groupChildCount = it
            }) {
            L.GL.w(
                "Invalid group child position %d, %d, group %d child size is %d",
                groupPosition,
                groupChildPosition,
                groupPosition,
                groupChildCount
            )
            return false
        }
        return true
    }

    fun getGroupChildPosition(itemPosition: Int): IntArray {
        val groupChildPosition = intArrayOf(-1, -1)
        if (!checkItemPosition(itemPosition)) {
            L.GL.e("Invalid item position %d", itemPosition)
            return groupChildPosition
        }
        if (groupCount > 0) {
            var groupItemPosition = headerCount + childCount
            if (itemPosition <= groupItemPosition) {
                return groupChildPosition
            }
            var groupChildCount: Int
            for (i in 0 until groupCount) {
                groupChildCount = groupChildCounts!![i]
                if (itemPosition > groupItemPosition && itemPosition <= groupItemPosition + groupChildCount) {
                    for (j in 0 until groupChildCount) {
                        groupItemPosition += 1
                        if (groupItemPosition == itemPosition) {
                            groupChildPosition[0] = i
                            groupChildPosition[1] = j
                            break
                        }
                    }
                } else {
                    groupItemPosition += groupChildCount
                }
                groupItemPosition++
            }
        }
        return groupChildPosition
    }

    fun getGroupChildPosition(groupChild: Any): IntArray {
        return getGroupChildPosition(indexOfGroupChild(groupChild))
    }

    fun getGroupChildPositionByViewType(groupPosition: Int, viewType: Int): Int {
        if (!checkGroupPosition(groupPosition)) {
            return -1
        }
        var p = -1
        val gcc = getGroupChildCount(groupPosition)
        for (i in 0 until gcc) {
            if (getItemViewType(getGroupChild(groupPosition, i)) == viewType) {
                p = i
                break
            }
        }
        return p
    }

    fun indexOfGroupChild(groupChild: Any): Int {
        if (groupChild == null || groupCount == 0) {
            return -1
        }
        var itemPosition = -1
        val itemCount = itemCount
        val itemBeginPosition = headerCount + childCount
        val itemEndPosition = itemCount - footerCount
        for (i in itemBeginPosition until itemEndPosition) {
            if (mList[i] == groupChild) {
                itemPosition = i
                break
            }
        }
        return itemPosition
    }

    fun convertGroupChildPosition(groupPosition: Int, childPosition: Int): Int {
        return if (!checkGroupChildPosition(groupPosition, childPosition)) {
            -1
        } else convertGroupPosition(
            groupPosition
        ) + 1 + childPosition
    }

    fun setFooter(footer: Any) {
        val footers: MutableList<Any> = ArrayList(1)
        footers.add(footer)
        setFooter(footers)
    }

    fun setFooter(newFooterList: List<Any>) {
        setFooter(0, if (footerCount > 0) footerCount - 1 else 0, newFooterList)
    }

    fun setFooter(beginIndex: Int, endIndex: Int, newFooterList: List<Any>) {
        if (beginIndex < 0 || endIndex < 0 || beginIndex > endIndex) {
            L.GL.e("beginIndex %d, endIndex %d !!!", beginIndex, endIndex)
            return
        }
        val pendingSetCount = endIndex - beginIndex + 1
        val newDataSize = newFooterList.size
        if (newDataSize <= 0) {
            removeFooter(beginIndex, pendingSetCount)
        } else if (footerCount <= beginIndex) {
            addFooter(newFooterList)
        } else {
            var i = 0
            while (i < pendingSetCount && i < newDataSize) {
                if (!isSameData(getFooter(i + beginIndex), newFooterList[i])) {
                    updateFooter(i + beginIndex, newFooterList[i])
                }
                i++
            }
            if (pendingSetCount > newDataSize) {
                clearFooter(newDataSize)
            } else if (pendingSetCount < newDataSize) {
                addFooter(
                    endIndex + 1, newFooterList.subList(
                        pendingSetCount,
                        newDataSize
                    )
                )
            }
        }
    }

    fun addFooter(footer: Any) {
        addFooter(footerCount, footer, null)
    }

    fun addFooter(footerList: List<Any>) {
        addFooter(footerCount, null, footerList)
    }

    fun addFooter(footerPosition: Int, footer: Any) {
        addFooter(footerPosition, footer, null)
    }

    fun addFooter(footerPosition: Int, footerList: List<Any>) {
        addFooter(footerPosition, null, footerList)
    }

    private fun addFooter(footerPosition: Int, footer: Any?, footerList: List<Any>?): Int {
        var footerPosition = footerPosition
        if (footerPosition < 0) {
            L.GL.e("Invalid footer position %d", footerPosition)
            return -1
        } else if (footer == null && (null == footerList || footerList.isEmpty())) {
            L.GL.e("Wrong footer param")
            return -1
        }
        val oldFooterCount = footerCount
        if (footerPosition > oldFooterCount) {
            footerPosition = oldFooterCount
        }
        val itemPosition = headerCount + childCount + groupAndGroupChildCount + footerPosition
        val addSize: Int
        addSize = if (footer != null) {
            mList.add(itemPosition, footer)
            1
        } else {
            mList.addAll(itemPosition, footerList!!)
            footerList.size
        }
        L.GL.v("Notify item from %d, count is %d", itemPosition, addSize)
        footerCount += addSize
        notifyItemRangeInserted(itemPosition, addSize)
        return footerPosition
    }

    fun removeFooter(footer: Any) {
        val itemPosition = indexOfFooter(footer)
        if (itemPosition == -1) {
            return
        }
        mList.removeAt(itemPosition)
        notifyItemRemoved(itemPosition)
        footerCount--
    }

    fun removeFooters(footers: List<Any>) {
        val size = footers?.size ?: 0
        for (i in 0 until size) {
            removeFooter(footers!![i])
        }
    }

    fun removeFooter(footerPosition: Int) {
        removeFooter(footerPosition, 1)
    }

    @JvmOverloads
    fun clearFooter(footerBeginPosition: Int = 0) {
        removeFooter(footerBeginPosition, footerCount - footerBeginPosition)
    }

    fun removeFooter(footerBeginPosition: Int, removeCount: Int) {
        var removeCount = removeCount
        if (!checkFooterPosition(footerBeginPosition)) {
            return
        }
        val footerItemBeginPosition = convertFooterPosition(footerBeginPosition)
        var footerItemEndPosition = footerItemBeginPosition + removeCount
        if (footerItemEndPosition > mList.size) {
            footerItemEndPosition = mList.size
            val oldRemoveCount = removeCount
            removeCount = footerItemEndPosition - footerItemBeginPosition
            L.GL.i("Reset removeCount from %d to %d", oldRemoveCount, removeCount)
        }
        mList.subList(footerItemBeginPosition, footerItemEndPosition).clear()
        footerCount -= removeCount
        notifyItemRangeRemoved(footerItemBeginPosition, removeCount)
    }

    fun getFooters(): List<Any>? {
        val footerItemBeginPosition = convertFooterPosition(0)
        return if (footerItemBeginPosition == -1) {
            null
        } else ArrayList(
            mList.subList(
                footerItemBeginPosition,
                footerItemBeginPosition + footerCount
            )
        )
    }

    fun getFooter(footerPosition: Int): Any? {
        val itemPosition = convertFooterPosition(footerPosition)
        return if (itemPosition == -1) {
            null
        } else mList[itemPosition]
    }

    fun updateFooter(footerPosition: Int, footer: Any) {
        val itemPosition = convertFooterPosition(footerPosition)
        if (itemPosition == -1) {
            return
        }
        mList[itemPosition] = footer
        notifyItemChanged(itemPosition)
    }

    private fun checkFooterPosition(footerPosition: Int): Boolean {
        if (footerPosition < 0) {
            L.GL.w("Invalid footer position %d", footerPosition)
            return false
        } else if (footerPosition >= footerCount) {
            L.GL.w("Invalid footer position %d, footer size is %d", footerPosition, footerCount)
            return false
        }
        return true
    }

    fun getFooterPosition(itemPosition: Int): Int {
        if (!checkItemPosition(itemPosition)) {
            L.GL.e("Invalid item position %d", itemPosition)
            return -1
        } else if (footerCount <= 0) {
            return -1
        } else if (itemPosition < mList.size - footerCount) {
            return -1
        }
        return footerCount - (mList.size - itemPosition)
    }

    fun getFooterPosition(footer: Any): Int {
        return getFooterPosition(indexOfFooter(footer))
    }

    private fun indexOfFooter(footer: Any): Int {
        if (footer == null) {
            return -1
        }
        var itemPosition = -1
        val footerItemBeginPosition = convertFooterPosition(0)
        val itemCount = itemCount
        for (i in footerItemBeginPosition until itemCount) {
            if (mList[i] == footer) {
                itemPosition = i
                break
            }
        }
        return itemPosition
    }

    fun convertFooterPosition(footerPosition: Int): Int {
        return if (!checkFooterPosition(footerPosition)) {
            -1
        } else headerCount + childCount + groupAndGroupChildCount + footerPosition
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    private fun checkItemPosition(itemPosition: Int): Boolean {
        val itemCount = itemCount
        if (itemPosition < 0 || itemPosition >= itemCount) {
            L.GL.e("Invalid itemPosition %d, item count is %d", itemPosition, itemCount)
            return false
        }
        return true
    }

    fun getHeaderPositionByViewType(viewType: Int): Int {
        var p = -1
        var header: Any
        for (i in 0 until headerCount) {
            header = getHeader(i)!!
            if (viewType == getItemViewType(header) || viewType == getItemViewType(
                    getItemPosition(
                        header
                    )
                )
            ) {
                p = i
                break
            }
        }
        return p
    }

    fun getLastHeaderPositionByViewType(viewType: Int): Int {
        var p = -1
        var header: Any
        for (i in headerCount - 1 downTo 0) {
            header = getHeader(i)!!
            if (viewType == getItemViewType(header) || viewType == getItemViewType(
                    getItemPosition(
                        header
                    )
                )
            ) {
                p = i
                break
            }
        }
        return p
    }

    fun removeHeaderByViewType(viewType: Int) {
        val p = getHeaderPositionByViewType(viewType)
        if (p != -1) {
            removeHeader(p)
        } else {
            L.GL.w("No header's viewType is %d", viewType)
        }
    }

    fun clearHeaderByViewType(viewType: Int) {
        while (true) {
            val p = getHeaderPositionByViewType(viewType)
            if (p != -1) {
                removeHeader(p)
            } else {
                L.GL.w("No header's viewType is %d", viewType)
                break
            }
        }
    }

    fun getChildPositionByViewType(viewType: Int): Int {
        var p = -1
        if (childCount <= 0) {
            return p
        }
        var childItemPosition = headerCount
        for (i in 0 until childCount) {
            if (viewType == getItemViewType(childItemPosition)) {
                p = i
                break
            }
            childItemPosition++
        }
        return p
    }

    fun getLastChildPositionByViewType(viewType: Int): Int {
        var p = -1
        if (childCount <= 0) {
            return p
        }
        var childItemPosition = headerCount + childCount - 1
        for (i in childCount - 1 downTo 0) {
            if (viewType == getItemViewType(childItemPosition)) {
                p = i
                break
            }
            childItemPosition--
        }
        return p
    }

    fun removeChildByViewType(viewType: Int) {
        val p = getChildPositionByViewType(viewType)
        if (p != -1) {
            removeChild(p)
        } else {
            L.GL.w("No child's viewType is %d", viewType)
        }
    }

    fun clearChildByViewType(viewType: Int) {
        while (true) {
            val p = getChildPositionByViewType(viewType)
            if (p != -1) {
                removeChild(p)
            } else {
                L.GL.w("No child's viewType is %d", viewType)
                break
            }
        }
    }

    fun getGroupPositionByViewType(viewType: Int): Int {
        var p = -1
        if (groupCount <= 0) {
            return p
        }
        var groupItemPosition = headerCount + childCount
        for (i in 0 until groupCount) {
            if (viewType == getItemViewType(groupItemPosition)) {
                p = i
                break
            }
            groupItemPosition += groupChildCounts!![i] + 1
        }
        return p
    }

    fun getLastGroupPositionByViewType(viewType: Int): Int {
        var p = -1
        if (groupCount <= 0) {
            return p
        }
        var groupItemPosition = convertGroupPosition(groupCount - 1)
        for (i in groupCount - 1 downTo 0) {
            if (viewType == getItemViewType(groupItemPosition)) {
                p = i
                break
            }
            if (i - 1 >= 0) {
                groupItemPosition -= groupChildCounts!![i - 1] + 1
            }
        }
        return p
    }

    fun removeGroupByViewType(viewType: Int) {
        val gpos = getGroupPositionByViewType(viewType)
        if (-1 != gpos) {
            removeGroup(gpos)
        } else {
            L.GL.w("no group's viewType is %d", viewType)
        }
    }

    fun clearGroupByViewType(viewType: Int) {
        while (true) {
            val gpos = getGroupPositionByViewType(viewType)
            if (-1 != gpos) {
                removeGroup(gpos)
            } else {
                L.GL.w("no group's viewType is %d", viewType)
                break
            }
        }
    }

    fun getFooterPositionByViewType(viewType: Int): Int {
        var p = -1
        if (footerCount <= 0) {
            return p
        }
        var footerItemPosition = mList.size - footerCount
        for (i in 0 until footerCount) {
            if (viewType == getItemViewType(footerItemPosition)) {
                p = i
                break
            }
            footerItemPosition++
        }
        return p
    }

    fun getLastFooterPositionByViewType(viewType: Int): Int {
        var p = -1
        var footerItemPosition = mList.size - 1
        for (i in footerCount - 1 downTo 0) {
            if (viewType == getItemViewType(footerItemPosition)) {
                p = i
                break
            }
            footerItemPosition--
        }
        return p
    }

    fun removeFooterByViewType(viewType: Int) {
        val p = getFooterPositionByViewType(viewType)
        if (p != -1) {
            removeFooter(p)
        } else {
            L.GL.w("no footer's viewType is %d", viewType)
        }
    }

    fun clearFooterByViewType(viewType: Int) {
        while (true) {
            val p = getFooterPositionByViewType(viewType)
            if (p != -1) {
                removeFooter(p)
            } else {
                L.GL.w("no footer's viewType is %d", viewType)
                break
            }
        }
    }

    fun getHeaderPosition(currViewType: Int, headerListViewTypes: List<Int>?): Int {
        return getPosition(0, headerListViewTypes, currViewType)
    }

    fun getChildPosition(currViewType: Int, childListViewTypes: List<Int>?): Int {
        return getPosition(1, childListViewTypes, currViewType)
    }

    fun getGroupPosition(currViewType: Int, groupListViewTypes: List<Int>?): Int {
        return getPosition(2, groupListViewTypes, currViewType)
    }

    fun getFooterPosition(currViewType: Int, footerListViewTypes: List<Int>?): Int {
        return getPosition(3, footerListViewTypes, currViewType)
    }

    /**
     * @param type         0,header;1,child;2,group,3,footer
     * @param viewTypes    constrainted viewtype list
     * @param currViewType the viewType which you need to calculate the right position
     */
    private fun getPosition(type: Int, viewTypes: List<Int>?, currViewType: Int): Int {
        if (viewTypes == null || viewTypes.isEmpty()) {
            L.GL.w("Please call method setXXXViewTypePositionConstraints")
            return -1
        }
        val currViewTypePosition = viewTypes.indexOf(currViewType)
        if (currViewTypePosition == -1) {
            L.GL.w("ViewType %d not find in XXX constraint viewType list")
            return -1
        }
        if (currViewTypePosition == 0) {
            return 0
        }
        var tmp: Int
        var targetPos = -1
        for (i in currViewTypePosition - 1 downTo 0) {
            tmp = when (type) {
                0 -> getLastHeaderPositionByViewType(viewTypes[i])
                1 -> getLastChildPositionByViewType(viewTypes[i])
                2 -> getLastGroupPositionByViewType(viewTypes[i])
                3 -> getLastFooterPositionByViewType(viewTypes[i])
                else -> return -1
            }
            if (tmp >= 0) {
                targetPos = tmp + 1
                break
            }
        }
        if (targetPos == -1) {
            targetPos = 0
        }
        return targetPos
    }

    override fun isSameData(oldData: Any?, newData: Any?): Boolean {
        return false
    }

    init {
        mList = ArrayList()
    }
}