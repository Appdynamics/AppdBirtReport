
package org.appdynamics.birtreport;

import java.io.File;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailUtil
{
	/*
	 * Utility method to send simple HTML email
	 * 
	 * @param session
	 * @param toEmail
	 * @param subject
	 * @param body
	 */
/*	public static void sendEmail(Session session, String toEmail, String subject, String body)
	{
		try
		{
			MimeMessage msg = new MimeMessage(session);
			// set message headers
			msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
			msg.addHeader("format", "flowed");
			msg.addHeader("Content-Transfer-Encoding", "8bit");

			msg.setFrom(new InternetAddress("jfaronson@gmail.com", "jfaronson"));

			msg.setReplyTo(InternetAddress.parse("john@aronsonhome.us", false));

			msg.setSubject(subject, "UTF-8");

			msg.setText(body, "UTF-8");

			msg.setSentDate(new Date());

			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
			System.out.println("Message is ready");
			Transport.send(msg);

			System.out.println("EMail Sent Successfully!!");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
*/
	public static void sendMultiPartEmail(Properties sessionProps, String toEmail, String fromEmail, String emailPassword, String subject, String body, File attachment) throws MessagingException
	{
		final String from = fromEmail; 
		final String password = emailPassword; 

		// create Authenticator object to pass in Session.getInstance argument
		Authenticator auth = null;			
		if (password != null && password.trim().length() > 0)
		{
			auth = new Authenticator()
			{
				// override the getPasswordAuthentication method
				protected PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(from, password);
				}
			};
		}
		Session session = Session.getInstance(sessionProps, auth);

		MimeMessage msg = new MimeMessage(session);
		// set message headers
		msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
		msg.addHeader("format", "flowed");
		msg.addHeader("Content-Transfer-Encoding", "8bit");

		msg.setFrom(new InternetAddress(fromEmail));

		msg.setReplyTo(InternetAddress.parse(fromEmail, false));

		msg.setSubject(subject, "UTF-8");

		msg.setText(body, "UTF-8");

		msg.setSentDate(new Date());

		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));

         // Create the message body part
         BodyPart messageBodyPart = new MimeBodyPart();

         // Fill the message
         messageBodyPart.setText(body);
         
         // Create a multipart message for attachment
         Multipart multipart = new MimeMultipart();

         // Set text message part
         multipart.addBodyPart(messageBodyPart);

         // Second part is attachment
         messageBodyPart = new MimeBodyPart();
         DataSource source = new FileDataSource(attachment);
         messageBodyPart.setDataHandler(new DataHandler(source));
         messageBodyPart.setFileName(attachment.getName());
         multipart.addBodyPart(messageBodyPart);

         // Send the complete message parts
         msg.setContent(multipart);

        Logger.getAnonymousLogger().info("email message created");
		Transport.send(msg);

		Logger.getAnonymousLogger().info("email sent");
	}

	/**
	 * Outgoing Mail (SMTP) Server requires TLS or SSL: smtp.gmail.com (use
	 * authentication) Use Authentication: Yes Port for TLS/STARTTLS: 587
	 */
	public static void main(String[] args)
	{
		final String fromEmail = "jfaronson@gmail.com"; // requires valid gmail
		final String password = "w1lmotGe"; // correct password for gmail id
		final String toEmail = "john@aronsonhome.us"; // can be any email id
		
		File attachment = new File(args[0]);

		System.out.println("TLSEmail Start");
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com"); // SMTP Host
		props.put("mail.smtp.port", "587"); // TLS Port
		props.put("mail.smtp.auth", "true"); // enable authentication
		props.put("mail.smtp.starttls.enable", "true"); // enable STARTTLS


		try
		{
			EmailUtil.sendMultiPartEmail(props, toEmail, fromEmail, password, "TLSEmail Testing Subject", "TLSEmail Testing Body", attachment);
//			EmailUtil.sendEmail(session, toEmail, "TLSEmail Testing Subject", "TLSEmail Testing Body");
		} catch (MessagingException e)
		{
			e.printStackTrace();
		}
	}
}
