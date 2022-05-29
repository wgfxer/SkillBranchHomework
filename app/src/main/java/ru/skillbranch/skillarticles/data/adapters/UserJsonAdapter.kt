package ru.skillbranch.skillarticles.data.adapters

import ru.skillbranch.skillarticles.data.local.User
class UserJsonAdapter : JsonAdapter<User>{
    override fun fromJson(json: String): User? {
        if (json.isEmpty()) return null
        val id = json.getParam("id").orEmpty()
        val name = json.getParam("name").orEmpty()
        val avatar = json.getParam("avatar")
        val rating = json.getParam("rating")?.toIntOrNull() ?: 0
        val respect = json.getParam("respect")?.toIntOrNull() ?: 0
        val about = json.getParam("about")
        return User(id,name, avatar, rating, respect, about)
    }

    private fun String.getParam(argName: String): String? {
        val innerPart = this.substring(1, this.length - 1)
        val list = innerPart.split(',')
        list.forEach {
            if (it.contains(argName)) return it.substringAfter(':').tryClear()
        }
        return null
    }

    private fun String.tryClear(): String {
        if (this.first() == '"' && this.last() == '"') return this.substring(1, this.length - 1)
        return this
    }

    override fun toJson(obj: User?): String {
        if (obj == null) return "{}"
        val stringResult = StringBuilder("{")
        val id = obj.id
        stringResult.append("\"id\":\"$id\"")
        val name = obj.name
        stringResult.append(",\"name\":\"$name\"")
        if (obj.avatar != null) {
            val avatar = obj.avatar
            stringResult.append(",\"avatar\":\"$avatar\"")
        }
        val rating = obj.rating
        stringResult.append(",\"rating\":$rating")
        val respect = obj.respect
        stringResult.append(",\"respect\":$respect")
        if (obj.about != null) {
            val about = obj.about
            stringResult.append(",\"about\":\"$about\"")
        }
        stringResult.append("}")
        return stringResult.toString()
    }
}