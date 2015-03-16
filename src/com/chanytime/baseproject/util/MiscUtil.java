package com.chanytime.baseproject.util;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.joda.time.DateTime;

import com.chanytime.baseproject.configuration.Properties;
import com.chanytime.baseproject.log.Logger;

/**
 * MiscUtil is collection of unrelated static methods that are useful. As more
 * methods are added here, it may make sense to move these methods elsewhere if
 * there is a logical grouping.
 *
 * @author Bryant Chan
 * @version %I%, %G%
 */
public class MiscUtil {

   /**
    * Generates a random string. This method is useful for generating things
    * like salts or default passwords. The caller can pass in a specific
    * character set or use the default one, which includes all digits and
    * alphabetic characters.
    *
    * @param _characterSet
    *           the specified characters that can be used in the random string;
    *           if this is null or empty, this method will use a default
    *           character set of the 52 alphabetic characters and 10 digits.
    * @param _minChars
    *           the minimum length of the random string
    * @param _maxChars
    *           the maximum length of the random string
    * @return the random string
    */
   public static String generateRandomString(String _characterSet, int _minChars, int _maxChars) {
      if (_minChars == 0 || _minChars > _maxChars || _minChars < 0 || _maxChars < 0) {
         Logger.getLogger().error("Invalid parameters to generateRandomString!");
         _minChars = 1;
         _maxChars = 1;
      }
      if (_characterSet == null || _characterSet.length() == 0) {
         _characterSet = new String("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
      }
      Random random = new Random();
      StringBuilder sb = new StringBuilder();
      for (int numChars = _minChars + random.nextInt(_maxChars - _minChars + 1); --numChars >= 0;) {
         sb.append(_characterSet.charAt(random.nextInt(_characterSet.length())));
      }
      return sb.toString();
   }

   /**
    * Returns the SHA-1 hash of a string.
    *
    * @param _in
    *           the string to hash
    * @return the SHA1 hash of the string
    */
   public static String getShaHash(String _in) {
      try {
         MessageDigest md = MessageDigest.getInstance("SHA1");
         md.update(_in.getBytes());
         byte[] output = md.digest();
         return bytesToHex(output);
      }
      catch (NoSuchAlgorithmException _e) {
         Logger.getLogger().fatal(_e, "No such algorithm SHA1!");
         return "";
      }
   }

   /**
    * Returns a string representation in hex of the passed-in bytes
    *
    * @param _bytes
    *           the array of bytes for which to print hex values
    * @return a string representation in hex of the passed-in bytes
    */
   public static String bytesToHex(byte[] _bytes) {
      if (_bytes == null) {
         return null;
      }
      final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
      StringBuffer buf = new StringBuffer();
      for (int j = 0; j < _bytes.length; j++) {
         buf.append(hexDigit[(_bytes[j] >> 4) & 0x0f]);
         buf.append(hexDigit[_bytes[j] & 0x0f]);
      }
      return buf.toString();
   }

   /**
    * Obtains the header of a subject for an email to be sent from the system.
    * This method changes the subject only for a non-production system. When
    * there is a change, we add the prefix from the properties file as well as
    * the computer name if possible.
    *
    * @return a string with the prefix and computer name if the system is non-
    *         production empty string otherwise
    */
   private static String getSubjectPrefix() {
      if (Properties.getProperties().getProduction()) {
         return "";
      }
      else {
         String computerName = "null";
         try {
            computerName = InetAddress.getLocalHost().getHostName();
         }
         catch (Exception _e) {
         }
         return "(" + Properties.getProperties().getNonproductionPrefix() + "@" + computerName + ") ";
      }
   }

   /**
    * Sends an HTML email.
    *
    * @param _recipient
    *           the email address of the receipient
    * @param _subject
    *           the subject-line text
    * @param _message
    *           the HTML body
    * @param _sentDateTime
    *           timestamp of the sent message
    */
   public static void sendHtmlEmail(String _recipient, String _subject, String _message, DateTime _sentDateTime) {
      String subject = getSubjectPrefix() + _subject;

      java.util.Properties mailProperties = new java.util.Properties();
      mailProperties.setProperty("mail.transport.protocol", "smtp");
      mailProperties.setProperty("mail.host", Properties.getProperties().getSmtpHost());

      Session mailSession = Session.getInstance(mailProperties, null);
      try {
         Transport mailTransport = mailSession.getTransport();
         MimeMessage message = new MimeMessage(mailSession);

         InternetAddress from = new InternetAddress(Properties.getProperties().getEmailFromField());
         message.setFrom(from);

         InternetAddress[] recipients = new InternetAddress[1];
         recipients[0] = new InternetAddress(_recipient);
         message.setRecipients(Message.RecipientType.TO, recipients);
         message.setSubject(subject);
         message.setContent(_message, "text/html; charset=ISO-8859-1");
         message.setSentDate(new Date(_sentDateTime.getMillis()));

         mailTransport.connect();
         Transport.send(message);
         mailTransport.close();
      }
      catch (MessagingException _ex) {
         Logger.getLogger().error("Unable to send email!");
      }
   }

}
