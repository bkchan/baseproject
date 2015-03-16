package com.chanytime.baseproject.configuration;

import java.io.IOException;
import java.net.URL;

import org.apache.log4j.helpers.Loader;

import com.chanytime.baseproject.log.Logger;

/**
 * PropertiesBase is an abstract class that encapsulates the ability to read
 * properties from a properties file, project.properties, that is in the
 * classpath. This class supports the reading of arbitrary keys whose values are
 * strings, integers, or booleans.
 *
 * @author Bryant Chan
 * @version %I%, %G%
 */
abstract class PropertiesBase {
   private static final String DEFAULT_PROPERTIES_FILE = "project.properties";
   private java.util.Properties m_properties;

   /**
    * Constructor that loads the default properties file.
    * <p>
    * 
    * @return A constructed object of this class with the default properties
    *         file
    */
   protected PropertiesBase() {
      this(DEFAULT_PROPERTIES_FILE);
   }

   /**
    * Constructor that loads the specified properties file.
    * <p>
    * 
    * @param _file
    *           The name of the file, which is in the classpath, from which to
    *           load the properties
    * @return A constructed object of this class with the default properties
    *         file
    */
   protected PropertiesBase(String _file) {
      URL url = Loader.getResource(_file);
      if (url == null) {
         Logger.getLogger().fatal("Cannot find %s!", DEFAULT_PROPERTIES_FILE);
         throw new Error("Cannot open properties file " + DEFAULT_PROPERTIES_FILE + "!");
      }
      else {
         try {
            m_properties = new java.util.Properties();
            m_properties.load(url.openStream());
            if (!isValid()) {
               throw new Error("Properties file " + url.toString() + " is not valid!");
            }
         }
         catch (IOException e) {
            Logger.getLogger().fatal("Cannot open %s!", url.toString());
            throw new Error("Cannot open properties file " + url.toString() + "!");
         }
      }
   }

   /**
    * Checks the integrity of values in the properties file. Any subclass of
    * PropertiesBase will have knowledge of what combination of values are
    * considered invalid and therefore should not be used.
    *
    * @return true if the properties are valid false otherwise
    */
   protected abstract boolean isValid();

   /**
    * Retrieves an integer value from the properties file with the specified
    * key.
    *
    * @param _property
    *           the name of the key to retrieve
    * @return the value of the property with the named key
    * @throws IllegalArgumentException
    *            if the key doesn't exist
    */
   public int getInt(String _property) {
      String value = m_properties.getProperty(_property);
      if (value == null) {
         throw new IllegalArgumentException("Property " + _property + " doesn't exist!");
      }
      return Integer.parseInt(value);
   }

   /**
    * Retrieves a string value from the properties file with the specified key.
    *
    * @param _property
    *           the name of the key to retrieve
    * @return the value of the property with the named key
    * @throws IllegalArgumentException
    *            if the key doesn't exist
    */
   public String getString(String _property) {
      String value = m_properties.getProperty(_property);
      if (value == null) {
         throw new IllegalArgumentException("Property " + _property + " doesn't exist!");
      }
      return value;
   }

   /**
    * Retrieves a Boolean value from the properties file with the specified key.
    *
    * @param _property
    *           the name of the key to retrieve
    * @return the value of the property with the named key
    * @throws IllegalArgumentException
    *            if the key doesn't exist
    */
   public boolean getBoolean(String _property) {
      String value = m_properties.getProperty(_property);
      if (value == null) {
         throw new IllegalArgumentException("Property " + _property + " doesn't exist!");
      }
      return value.equalsIgnoreCase("true");
   }
}

/**
 * Properties encapsulates the ability to read properties from a properties
 * file, project.properties, that is in the classpath. Currently, only a handful
 * of core properties are retrievable, but the list can be expanded since the
 * base class supports the arbritrary reading of properties given a key. The
 * following are the properties that can be retrieved:
 * <ul>
 * <li>database_url_scheme
 * <li>database_url_hostname
 * <li>web_database_username
 * <li>web_database_password
 * <li>admin_database_username
 * <li>admin_database_password
 * <li>batch_database_username
 * <li>batch_database_password
 * <li>logger_identifier
 * <li>production
 * <li>nonproduction_prefix
 * <li>smtp_host
 * <li>email_from_field
 * <li>logger_email_destination
 * <li>sql_trace_enabled
 * </ul>
 * <p>
 * To use this class, there is a static instance always available. The following
 * is a sample calling sequence:
 * 
 * <pre>
 * {@code
 * Properties.getProperties().getDatabaseUrlScheme();
 * Properties.getProperties().getInt("some_key");
 * }
 * </pre>
 *
 * @author Bryant Chan
 * @version %I%, %G%
 */
public class Properties extends PropertiesBase {
   private static Properties m_propertiesFile;

   static {
      m_propertiesFile = new Properties();
   }

   /**
    * Constructor that loads the default properties file.
    *
    * @return A constructed object of this class with the default properties
    *         file
    */
   private Properties() {
      super();
   }

   /**
    * Retrieves the global, static instance of this class, which represents the
    * properties file for this project.
    *
    * @return the global instance of the Properties object
    */
   public static Properties getProperties() {
      return m_propertiesFile;
   }

   /**
    * Checks the integrity of values in the properties file.
    *
    * @return true if the properties are valid false otherwise
    */
   @Override
   protected boolean isValid() {
      if (getLoggerIdentifier() == null || getLoggerIdentifier().isEmpty()) {
         return false;
      }
      if (getProduction()) {
         if (getSqlTraceEnabled()) {
            return false;
         }
      }
      else {
         if (getNonproductionPrefix() == null || getNonproductionPrefix().isEmpty()) {
            return false;
         }
      }
      return true;
   }

   /**
    * Retrieves the specified database URL scheme.
    *
    * @return the database URL scheme
    */
   public String getDatabaseUrlScheme() {
      return getString("database_url_scheme");
   }

   /**
    * Retrieves the specified database URL hostname.
    *
    * @return the database URL hostname
    */
   public String getDatabaseUrlHostname() {
      return getString("database_url_hostname");
   }

   /**
    * Retrieves the specified username for the web user on the database
    *
    * @return the username of the web user for database access
    */
   public String getWebDatabaseUsername() {
      return getString("web_database_username");
   }

   /**
    * Retrieves the specified password for the web user on the database
    *
    * @return the password of the web user for database access
    */
   public String getWebDatabasePassword() {
      return getString("web_database_password");
   }

   /**
    * Retrieves the specified username for the admin user on the database
    *
    * @return the username of the admin user for database access
    */
   public String getAdminDatabaseUsername() {
      return getString("web_database_password");
   }

   /**
    * Retrieves the specified password for the admin user on the database
    *
    * @return the password of the admin user for database access
    */
   public String getAdminDatabasePassword() {
      return getString("admin_database_password");
   }

   /**
    * Retrieves the specified username for the batch user on the database
    *
    * @return the username of the batch user for database access
    */
   public String getBatchDatabaseUsername() {
      return getString("batch_database_username");
   }

   /**
    * Retrieves the specified password for the batch user on the database
    *
    * @return the password of the batch user for database access
    */
   public String getBatchDatabasePassword() {
      return getString("batch_database_password");
   }

   /**
    * Retrieves the specified string to identify this application's logger
    * output. This is especially useful is multiple applications are logging to
    * the same file.
    *
    * @return the logger identifier
    */
   public String getLoggerIdentifier() {
      return getString("logger_identifier");
   }

   /**
    * Retrieves whether or not this is a production system. The application may
    * specify different behavior on a production system vs. a development or
    * stage system.
    *
    * @return true if this system is a production system false otherwise
    */
   public boolean getProduction() {
      return getBoolean("production");
   }

   /**
    * Retrieves, if the system is not a production system, the prefix for this
    * instance of the application. This is especially useful if there are
    * multiple copies of this application running in the same environment (e.g.,
    * in a development environment).
    *
    * @return the prefix for this instance of the application
    */
   public String getNonproductionPrefix() {
      return getString("nonproduction_prefix");
   }

   /**
    * Retrieves the specified SMTP host for outgoing mail
    *
    * @return the SMTP host for outgoing mail
    */
   public String getSmtpHost() {
      return getString("smtp_host");
   }

   /**
    * Retrieves the specified email address for the "From" field for outgoing
    * mail.
    *
    * @return the "From:" email for outgoing mail
    */
   public String getEmailFromField() {
      return getString("email_from_field");
   }

   /**
    * Retrieves the specified email address to send logger output if specified
    * for email delivery.
    *
    * @return the destination email address for logger output
    */
   public String getLoggerEmailDestination() {
      return getString("logger_email_destination");
   }

   /**
    * Retrieves whether or not SQL and other trace output is enabled. WARNING:
    * turning this on may cause logger output to be excessive.
    *
    * @return true if SQL and other trace output is enabled false otherwise
    */
   public boolean getSqlTraceEnabled() {
      return getBoolean("sql_trace_enabled");
   }
}
