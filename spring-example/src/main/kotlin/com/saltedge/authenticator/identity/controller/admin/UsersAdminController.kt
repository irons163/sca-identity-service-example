/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-identity-service-example).
 * Copyright (c) 2019 Salt Edge Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 or later.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * For the additional permissions granted for Salt Edge Authenticator
 * under Section 7 of the GNU General Public License see THIRD_PARTY_NOTICES.md
 */
package com.saltedge.authenticator.identity.controller.admin

import com.saltedge.authenticator.identity.model.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping(value = ["/admin/users"])
class UsersAdminController {
    @Autowired
    private var usersRepository: UsersRepository? = null
    @Autowired
    private var connectionsRepository: ConnectionsRepository? = null
    @Autowired
    private var authorizationsRepository: AuthorizationsRepository? = null

    @GetMapping
    fun showUsers(@RequestParam("user_id") userId: Long?): ModelAndView {
        if (userId == null) {
            return ModelAndView(
                "users",
                mapOf(
                    "users" to usersRepository?.findAll(),
                    "connections" to connectionsRepository?.findByUserIsNull()
                )
            )
        }

        val user: User = usersRepository?.findById(userId)?.get() ?: return ModelAndView("user")
        val connections: List<Connection> = connectionsRepository?.findByUser(user)?.sortedBy { it.revoked } ?: emptyList()
        val authorizations: List<Authorization> = authorizationsRepository?.findByUser(user)
            ?.sortedWith(compareBy({ it.isExpired() }, { it.confirmed == null })) ?: emptyList()

        return ModelAndView(
            "user",
            mapOf(
                "user" to user,
                "connections" to connections,
                "authorizations" to authorizations,
                "connectionsIsNotEmpty" to connections.isNotEmpty()
            )
        )
    }

    @PostMapping
    fun createUser(@RequestParam name: String, @RequestParam password: String): ModelAndView {
        if (name.isNotBlank() && password.isNotBlank()) {
            usersRepository?.save(User(name = name, password = password))
        }
        return ModelAndView("redirect:/admin/users")
    }
}
