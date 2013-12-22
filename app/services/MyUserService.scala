package services

import securesocial.core.{IdentityId, Identity, UserServicePlugin}
import play.api.{Logger, Application}
import securesocial.core.providers.Token
import models.Author

class MyUserService(application: Application) extends UserServicePlugin(application){
  def find(id: IdentityId): Option[Identity] = {
    Logger.info("Looking up user by id: "+id)
    val user = Author.findByIdentity(id)
    Logger.info("User was "+user)
    user
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = Author.findByEmail(email)

  def save(user: Identity): Identity = {
    val id = Author(
      username = "a",
      identityId = user.identityId,
      firstName = user.firstName,
      lastName = user.lastName,
      fullName = user.fullName,
      email = user.email,
      avatarUrl = user.avatarUrl,
      authMethod = user.authMethod,
      oAuth1Info = user.oAuth1Info,
      oAuth2Info = user.oAuth2Info,
      passwordInfo = user.passwordInfo
    )
    Author.save(id)
    id
  }

  def save(token: Token): Unit = ???

  def findToken(token: String): Option[Token] = ???

  def deleteToken(uuid: String): Unit = ???

  def deleteExpiredTokens(): Unit = ???
}
