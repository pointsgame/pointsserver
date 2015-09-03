package net.pointsgame.domain

import net.pointsgame.domain.services._

case class Services(accountService: AccountService, tokenService: TokenService)
