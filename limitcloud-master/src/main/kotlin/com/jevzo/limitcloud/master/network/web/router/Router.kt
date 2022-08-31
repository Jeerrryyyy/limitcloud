package com.jevzo.limitcloud.master.network.web.router

class Router {

    private val routes: MutableMap<String, Route> = mutableMapOf()

    fun registerRoute(path: String, route: Route) {
        if (routes.containsKey(path)) return
        routes[path] = route
    }

    fun getRoute(path: String): Route? {
        return routes[path]
    }
}