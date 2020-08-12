import google._
import javax.mail.internet._

object SendWithDifferentContent extends App {
  val token = Setup.getToken()
  val auth = OAuth2Authenticator.connectToSmtp("smtp.gmail.com", 587, token.email, token.encoded, true)
  val transport = auth.transport
  val session = auth.session


  transport.issueCommand(s"AUTH XOAUTH2 ${token.encoded}", 235)

  val msg1 = new MimeMessage(session)
  msg1.setFrom(new InternetAddress(token.email))
  msg1.setHeader("To", s"${Setup.Recipient1}, ${Setup.Recipient2}")
  msg1.setSubject("Multiple")
  msg1.setText("Some text for recipient1")
  transport.sendMessage(msg1, List(new InternetAddress(Setup.Recipient1)).toArray);

  val msg2 = new MimeMessage(session)
  msg2.setFrom(new InternetAddress(token.email))
  msg2.setHeader("To", s"${Setup.Recipient1}, ${Setup.Recipient2}")
  msg2.setSubject("Multiple")
  msg2.setText("Some text for recipient2")
  transport.sendMessage(msg2, List(new InternetAddress(Setup.Recipient2)).toArray);
}
