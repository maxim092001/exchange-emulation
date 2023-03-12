package dao

import domain.User

interface UserDao {
    fun addUser(user: User)

    fun getUser(userId: Long): User?

    fun updateUser(userId: Long, user: User)

    fun clear()
}