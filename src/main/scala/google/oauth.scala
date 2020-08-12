package google

import java.security.{Provider, Security}
import java.util
import java.util.Properties

import com.sun.mail.smtp.SMTPTransport
import javax.mail.Session
import javax.security.auth.callback.{Callback, CallbackHandler, NameCallback}
import javax.security.sasl.{SaslClient, SaslClientFactory}

// Dealing with java interfaces
// omitting error handling for brewity
class OAuth2SaslClientFactory extends SaslClientFactory {
  override def createSaslClient(mechanism: Array[String], authId: String, protocol: String, serverName: String, props: util.Map[String, _], callbackHandler: CallbackHandler): SaslClient = {
    if (mechanism.map(_.toUpperCase).contains("XOAUTH2")) {
      val token = props.get(OAuth2SaslClientFactory.OauthTokenProp).asInstanceOf[String]
      new OAuth2SaslClient(token, callbackHandler)
    } else null
  }

  override def getMechanismNames(map: util.Map[String, _]): Array[String] = Array("XOAUTH2")
}

object OAuth2SaslClientFactory {
  val OauthTokenProp = "mail.imaps.sasl.mechanisms.oauth2.oauthToken";
}

class OAuth2SaslClient(oauthToken: String, callbackHandler: CallbackHandler) extends SaslClient {
  private[this] var isComplete = false

  override def getMechanismName: String = "XOAUTH2"
  override def hasInitialResponse: Boolean = true
  override def evaluateChallenge(bytes: Array[Byte]): Array[Byte] = {
    if (isComplete) new Array[Byte](0)
    else {
      val nameCallback = new NameCallback("Enter name")
      val callbacks = Array[Callback](nameCallback)
      callbackHandler.handle(callbacks)
      val email = nameCallback.getName

      isComplete = true
      s"user=$email\u0001auth=Bearer $oauthToken\u0001\u0001".getBytes
    }
  }
  override def isComplete: Boolean = isComplete
  override def unwrap(bytes: Array[Byte], i: Int, i1: Int): Array[Byte] = throw new IllegalStateException()
  override def wrap(bytes: Array[Byte], i: Int, i1: Int): Array[Byte] = throw new IllegalStateException()
  override def getNegotiatedProperty(s: String): AnyRef = throw new IllegalStateException()
  override def dispose(): Unit = {}
}

object OAuth2Authenticator {
  val name = "OAuth2 Provider"
  val version = 1.0
  val info = "Provider for XOAuth2 SASL"

  private class OAuth2Provider extends Provider(name, version, info) {
    put("SaslClientFactory.XOAUTH2", "google.OAuth2SaslClientFactory")
  }

  // ensure it's initialized
  Security.addProvider(new OAuth2Provider)

  trait ConnectionResult {
    def session: Session
    def transport: SMTPTransport
  }

  def connectToSmtp(host: String, port: Int, userEmail: String, oauthToken: String, debug: Boolean): ConnectionResult = {
    val props = new Properties()
    props.put("mail.smtp.starttls.enable", "true")
    props.put("mail.smtp.starttls.required", "true")
    props.put("mail.smtp.sasl.enable", "true")
    props.put("mail.smtp.sasl.mechanisms", "XOAUTH2")
    props.put(OAuth2SaslClientFactory.OauthTokenProp, oauthToken)
    val sessionInstance = Session.getInstance(props)
    sessionInstance.setDebug(debug)
    val smptTransport = new SMTPTransport(sessionInstance, null)
    smptTransport.connect(host, port, userEmail, null)

    new ConnectionResult {
      def session: Session = sessionInstance
      def transport: SMTPTransport = smptTransport
    }
  }
}
