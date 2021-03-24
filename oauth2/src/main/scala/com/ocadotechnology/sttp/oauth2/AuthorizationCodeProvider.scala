package com.kubukoz.ho2

import cats.effect.Concurrent
import org.http4s.Uri
import org.http4s.client.Client

/** Provides set of functions to simplify oauth2 identity provider integration.
  *  Use the `instance` companion object method to create instances.
  *
  * @tparam UriType type of returned uri
  * @tparam F effect wrapper
  */
trait AuthorizationCodeProvider[UriType, F[_]] {

  /** Returns login link to oauth2 provider for user authentication
    *
    * Uses redirect link provided to instance constructor.
    *  @param state optional parameter, the state will be
    *               provided back to the service after oauth2 redirect
    *  @param scope set of Scope objects specifying access privileges
    *               @see https://tools.ietf.org/html/rfc6749#page-23
    *  @return instance of UriType, use to redirect user to Oauth2 login page
    */
  def loginLink(state: Option[String] = None, scope: Set[String]): UriType

  /** Returns logout link for to oauth2 provider
    *
    *  @param postLogoutRedirect Optional override of redirect link. By default uses login redirect link.
    *  @return instance of UriType, use to redirect user to Oauth2 logout page
    */
  def logoutLink(postLogoutRedirect: Option[UriType] = None): UriType

  /** Returns token details wrapped in effect
    *
    *  @param authCode code provided by oauth2 provider redirect,
    *                  after user is authenticated correctly
    *  @return Oauth2TokenResponse details containing user info and additional information
    */
  def authCodeToToken(authCode: String): F[Oauth2TokenResponse]

  /** Performs the token refresh on oauth2 provider nad returns new token details wrapped in effect
    *
    *  @param refreshToken value from refresh_token field of previous access token
    *  @param scope optional parameter for overriding token scope, useful to narrow down the scope
    *               when not provided or ScopeSelection.KeepExisting passed,
    *               the new token will be issued for the same scope as the previous one
    *  @return Oauth2TokenResponse details containing user info and additional information
    */
  def refreshAccessToken(refreshToken: String, scope: ScopeSelection = ScopeSelection.KeepExisting): F[Oauth2TokenResponse]
}

object AuthorizationCodeProvider {

  def apply[U, F[_]](implicit ev: AuthorizationCodeProvider[U, F]): AuthorizationCodeProvider[U, F] = ev

  def uriInstance[F[_]: Client: Concurrent](
    baseUrl: Uri,
    redirectUri: Uri,
    clientId: String,
    clientSecret: Secret[String]
  ): AuthorizationCodeProvider[Uri, F] =
    new AuthorizationCodeProvider[Uri, F] {
      private val tokenUri = baseUrl.addPath("token")

      override def loginLink(state: Option[String] = None, scope: Set[String]): Uri =
        AuthorizationCode
          .loginLink(baseUrl, redirectUri, clientId, state, scope)

      override def authCodeToToken(authCode: String): F[Oauth2TokenResponse] =
        AuthorizationCode
          .authCodeToToken(tokenUri, redirectUri, clientId, clientSecret, authCode)

      override def logoutLink(postLogoutRedirect: Option[Uri]): Uri =
        AuthorizationCode
          .logoutLink(baseUrl, redirectUri, clientId, postLogoutRedirect)

      override def refreshAccessToken(
        refreshToken: String,
        scopeOverride: ScopeSelection = ScopeSelection.KeepExisting
      ): F[Oauth2TokenResponse] =
        AuthorizationCode
          .refreshAccessToken(tokenUri, clientId, clientSecret, refreshToken, scopeOverride)

    }

}
