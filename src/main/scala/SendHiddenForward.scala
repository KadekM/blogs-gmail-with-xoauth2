import google._
import javax.mail.internet._
import javax.mail._


object SendHiddenForward extends App {
  val token = Setup.getToken()
  val auth = OAuth2Authenticator.connectToSmtp("smtp.gmail.com", 587, token.email, token.encoded, true)
  val transport = auth.transport
  val session = auth.session

  val message = new MimeMessage(session)

  transport.issueCommand(s"AUTH XOAUTH2 ${token.encoded}", 235)
  message.setFrom(new InternetAddress(token.email))
  message.addRecipient(Message.RecipientType.TO, new InternetAddress(Setup.Recipient1))
  message.setSubject("Test of hidden forward")
  message.setText("Some testing message text")
  val recipients = List(new InternetAddress(Setup.Recipient1), new InternetAddress(Setup.Recipient2))
  transport.sendMessage(message, recipients.toArray)
}
