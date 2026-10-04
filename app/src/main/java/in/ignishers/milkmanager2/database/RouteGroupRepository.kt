package `in`.ignishers.milkmanager2.database

import `in`.ignishers.milkmanager2.models.RouteGroup
import kotlinx.coroutines.flow.Flow

class RouteGroupRepository(private val dao: RouteGroupRoomDao) {
    fun getAllRouteGroups(): Flow<List<RouteGroup>> = dao.getAllRouteGroups()

    fun getRootGroups(): Flow<List<RouteGroup>> = dao.getRootGroups()

    fun getChildGroups(parentId: Long): Flow<List<RouteGroup>> = dao.getChildGroups(parentId)

    suspend fun getGroupById(id: Long): RouteGroup? = dao.getGroupById(id)

    suspend fun updateSortOrder(id: Long, newOrder: Int) {
        dao.updateSortOrder(id, newOrder)
    }

    suspend fun insert(routeGroup: RouteGroup): Long {
        routeGroup.syncIsSynced = 0
        routeGroup.syncUpdatedAt = System.currentTimeMillis()
        return dao.insert(routeGroup)
    }

    suspend fun update(routeGroup: RouteGroup) {
        routeGroup.syncIsSynced = 0
        routeGroup.syncUpdatedAt = System.currentTimeMillis()
        dao.update(routeGroup)
    }

    suspend fun delete(id: Long) {
        dao.delete(id)
    }
}
