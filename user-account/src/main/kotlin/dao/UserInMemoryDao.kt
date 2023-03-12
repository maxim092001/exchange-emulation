package dao

import domain.User
import java.util.concurrent.atomic.AtomicLong

class UserInMemoryDao : UserDao {
    private val userIdCounter = AtomicLong(0)
    private val mp: MutableMap<Long, User> = mutableMapOf()

    override fun addUser(user: User) {
        val userId = userIdCounter.incrementAndGet()
        mp[userId] = user.copy(id = userId)
    }

    override fun getUser(userId: Long): User? = mp[userId]

    override fun updateUser(userId: Long, user: User) {
        mp[userId] = user
    }

    override fun clear() = mp.clear()
}