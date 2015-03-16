package com.chanytime.baseproject.log;

import java.net.InetAddress;

import org.apache.log4j.Level;
import org.apache.log4j.MDC;
import org.apache.log4j.spi.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.chanytime.baseproject.configuration.Properties;
import com.chanytime.baseproject.util.MiscUtil;

/**
 * Logger is an extension of the log4j Logger class with some improvements. The
 * following is a list of such improvements.
 * <ul>
 * <li>Exposed the TRACE level for logging
 * <li>All levels take a Throwable object to print a backtrace for exceptions
 * <li>The ability to send an email log message for production systems
 * <li>The ability to use sprintf semantics
 * <li>sprintf penalties aren't incurred unless that log level is enabled
 * <li>Each logger statement has the method from which the logger was called
 * <li>Each logger statement can be correlated with a session ID
 * </ul>
 * <p>
 * The following is a way to call this logger.
 * 
 * <pre>
 * {@code
 *  Logger.getLogger().debug("a simple message");
 *   Logger.getLogger().debug("a number: %d", 5);
 * try {
 *   ...
 * }
 * catch (Exception caughtException) {
 *   Logger.getLogger().debug(caughtException,
 *     "this will put a backtrace in the logs and be sent via email");
 * }
 * Logger.getLogger().sendEmailOnProduction("this will be sent via email!");
 * }
 * </pre>
 *
 * @author brchan
 * @version %I%, %G%
 * @see org.apache.log4j.Logger
 */
public class Logger extends org.apache.log4j.Logger {

   /**
    * LoggerFactoryImpl serves as a private class to instantiate the base-class
    * Logger (as opposed to the log4j version).
    *
    * @author brchan
    * @version %I%, %G%
    */
   private static class LoggerFactoryImpl implements LoggerFactory {

      /**
       * Implements LoggerFactory to return an instance of the base-class
       * Logger.
       *
       * @param _name
       *           the name of the logger to create.
       * @return a new instance of the base-class logger with the specified name
       */
      @Override
      public Logger makeNewLoggerInstance(String _name) {
         try {
            return (Logger) Class.forName("com.chanytime.baseproject.log.Logger").newInstance();
         }
         catch (Exception _e) {
            System.out.println("Caught exception " + _e.toString() + " in makeNewLoggerInstance()");
            return null;
         }
      }
   }

   private final static LoggerFactory m_loggerFactory = new LoggerFactoryImpl();
   private final static String LOGGER_NAME = "mainLogger";
   private final static String MDC_SESSION_ID_KEY = "sessionID";
   private final static String MDC_CLASS_METHOD_KEY = "classMethod";

   /**
    * Constructs a default Logger.
    *
    * @return a default Logger
    */
   public Logger() {
      super(LOGGER_NAME);
   }

   private static boolean m_initialized = false;

   /**
    * Returns the static instance of the logger.
    *
    * @return the static instance of the logger
    */
   public static Logger getLogger() {

      // If we haven't initialized the MDC session ID key, we do so for the
      // first (and only) time. If the logging format wants to display log
      // statements, we must have at least a null key in there to prevent
      // an issue with logger at runtime.

      if (!m_initialized) {
         if (MDC.get(MDC_SESSION_ID_KEY) == null) {
            MDC.put(MDC_SESSION_ID_KEY, "null");
         }
         m_initialized = true;
      }

      return (Logger) org.apache.log4j.Logger.getLogger(LOGGER_NAME, m_loggerFactory);
   }

   /**
    * Sets the session ID to correlate multiple calls to the logger to a single
    * session, which is defined by the client. The client has the responsibility
    * of generating session IDs which do not collide and keeping it across
    * process boundaries.
    *
    * @param _sessionID
    *           the client-generated session ID for correlating this log
    *           statement
    */
   public static void setSessionID(String _sessionID) {
      String loggerIdentifier = Properties.getProperties().getLoggerIdentifier();
      if (loggerIdentifier != null && loggerIdentifier != "") {
         _sessionID += " - " + loggerIdentifier;
      }
      MDC.put(MDC_SESSION_ID_KEY, _sessionID);
   }

   /**
    * Sends an email to system operators on production systems. This will work
    * only on production systems, and the email destination is specified in the
    * properties file.
    *
    * @param _message
    *           the message to send in HTML format
    */
   public void sendEmailOnProduction(String _message) {
      if (Properties.getProperties().getProduction()) {
         try {
            String session = (String) MDC.get(MDC_SESSION_ID_KEY);
            if (session == null) {
               session = "null";
            }
            String computerName = "null";
            try {
               computerName = InetAddress.getLocalHost().getHostName();
            }
            catch (Exception _e) {
            }
            MiscUtil.sendHtmlEmail(Properties.getProperties().getLoggerEmailDestination(), "Session (" + session
                  + ") on " + computerName + " received a message to send email at "
                  + DateTime.now(DateTimeZone.UTC).toString() + "!", _message, DateTime.now(DateTimeZone.UTC));
         }
         catch (Exception _e) {
            error("Caught exception trying to send exception email, message: %s", _e.getMessage());
         }
      }
   }

   /**
    * Sends an email with the backtrace in the specified exception.
    *
    * @param _t
    *           the exception with the backtrace information to send
    */
   private void sendExceptionEmail(Throwable _t) {
      if (Properties.getProperties().getProduction()) {
         try {
            String session = (String) MDC.get(MDC_SESSION_ID_KEY);
            if (session == null) {
               session = "null";
            }
            String computerName = "null";
            try {
               computerName = InetAddress.getLocalHost().getHostName();
            }
            catch (Exception _e) {
            }
            MiscUtil.sendHtmlEmail(Properties.getProperties().getLoggerEmailDestination(), "Session (" + session
                  + ") on " + computerName + " encountered an exception at "
                  + DateTime.now(DateTimeZone.UTC).toString() + "!", getStackTrace(_t, "<br/>"),
                  DateTime.now(DateTimeZone.UTC));
         }
         catch (Exception _e) {
            error("Caught exception trying to send exception email, message: %s", _e.getMessage());
         }
      }
   }

   /**
    * Retrieves the class and method that called the logger to log a statement
    * to be output in the logger output.
    */
   private void addClassMethodNameToMDC() {
      StackTraceElement s = new Exception().getStackTrace()[2];
      String className = s.getClassName();
      String methodName = s.getMethodName();
      MDC.put(MDC_CLASS_METHOD_KEY, className.substring(className.lastIndexOf('.') + 1) + "." + methodName);
   }

   private static String getStackTrace(Throwable _t, String _lineSeparator) {
      final StringBuilder result = new StringBuilder();
      result.append("EXCEPTION (message: " + _t.getMessage() + ") -- details:");
      result.append(_lineSeparator);
      result.append("[EXCEPTION] ");
      result.append(_t.toString());
      for (StackTraceElement element : _t.getStackTrace()) {
         result.append(_lineSeparator);
         result.append("[EXCEPTION]    at ");
         result.append(element);
      }
      return result.toString();
   }

   /**
    * Logs a message at the FATAL level.
    *
    * @param _string
    *           the message
    */
   public void fatal(String _string) {
      if (isEnabledFor(Level.FATAL)) {
         addClassMethodNameToMDC();
         super.fatal(_string);
      }
   }

   /**
    * Logs a message at the FATAL level.
    *
    * @param _format
    *           the message format
    * @param _args
    *           arguments to the message format
    */
   public void fatal(String _format, Object... _args) {
      if (isEnabledFor(Level.FATAL)) {
         addClassMethodNameToMDC();
         super.fatal(String.format(_format, _args));
      }
   }

   /**
    * Logs a message with an exception backtrace at the FATAL level.
    *
    * @param _t
    *           the exception
    * @param _format
    *           the message format
    * @param _args
    *           arguments to the message format
    */
   public void fatal(Throwable _t, String _format, Object... _args) {
      if (isEnabledFor(Level.FATAL)) {
         addClassMethodNameToMDC();
         super.fatal(String.format(_format, _args));
         super.fatal(getStackTrace(_t, System.getProperty("line.separator")));
      }
      sendExceptionEmail(_t);
   }

   /**
    * Logs a message at the ERROR level.
    *
    * @param _string
    *           the message
    */
   public void error(String _string) {
      if (isEnabledFor(Level.ERROR)) {
         addClassMethodNameToMDC();
         super.error(_string);
      }
   }

   /**
    * Logs a message at the ERROR level.
    *
    * @param _format
    *           the message format
    * @param _args
    *           arguments to the message format
    */
   public void error(String _format, Object... _args) {
      if (isEnabledFor(Level.ERROR)) {
         addClassMethodNameToMDC();
         super.error(String.format(_format, _args));
      }
   }

   /**
    * Logs a message with an exception backtrace at the ERROR level.
    *
    * @param _t
    *           the exception
    * @param _format
    *           the message format
    * @param _args
    *           arguments to the message format
    */
   public void error(Throwable _t, String _format, Object... _args) {
      if (isEnabledFor(Level.ERROR)) {
         addClassMethodNameToMDC();
         super.error(String.format(_format, _args));
         super.error(getStackTrace(_t, System.getProperty("line.separator")));
      }
      sendExceptionEmail(_t);
   }

   /**
    * Logs a message at the WARN level.
    *
    * @param _string
    *           the message
    */
   public void warn(String _string) {
      if (isEnabledFor(Level.WARN)) {
         addClassMethodNameToMDC();
         super.warn(_string);
      }
   }

   /**
    * Logs a message at the WARN level.
    *
    * @param _format
    *           the message format
    * @param _args
    *           arguments to the message format
    */
   public void warn(String _format, Object... _args) {
      if (isEnabledFor(Level.WARN)) {
         addClassMethodNameToMDC();
         super.warn(String.format(_format, _args));
      }
   }

   /**
    * Logs a message with an exception backtrace at the WARN level.
    *
    * @param _t
    *           the exception
    * @param _format
    *           the message format
    * @param _args
    *           arguments to the message format
    */
   public void warn(Throwable _t, String _format, Object... _args) {
      if (isEnabledFor(Level.WARN)) {
         addClassMethodNameToMDC();
         super.warn(String.format(_format, _args));
         super.warn(getStackTrace(_t, System.getProperty("line.separator")));
      }
      sendExceptionEmail(_t);
   }

   /**
    * Logs a message at the DEBUG level.
    *
    * @param _string
    *           the message
    */
   public void debug(String _string) {
      if (isDebugEnabled()) {
         addClassMethodNameToMDC();
         super.debug(_string);
      }
   }

   /**
    * Logs a message at the DEBUG level.
    *
    * @param _format
    *           the message format
    * @param _args
    *           arguments to the message format
    */
   public void debug(String _format, Object... _args) {
      if (isDebugEnabled()) {
         addClassMethodNameToMDC();
         super.debug(String.format(_format, _args));
      }
   }

   /**
    * Logs a message with an exception backtrace at the DEBUG level.
    *
    * @param _t
    *           the exception
    * @param _format
    *           the message format
    * @param _args
    *           arguments to the message format
    */
   public void debug(Throwable _t, String _format, Object... _args) {
      if (isDebugEnabled()) {
         addClassMethodNameToMDC();
         super.debug(String.format(_format, _args));
         super.debug(getStackTrace(_t, System.getProperty("line.separator")));
      }
      sendExceptionEmail(_t);
   }

   /**
    * Logs a message at the INFO level.
    *
    * @param _string
    *           the message
    */
   public void info(String _string) {
      if (isInfoEnabled()) {
         addClassMethodNameToMDC();
         super.info(_string);
      }
   }

   /**
    * Logs a message at the INFO level.
    *
    * @param _format
    *           the message format
    * @param _args
    *           arguments to the message format
    */
   public void info(String _format, Object... _args) {
      if (isInfoEnabled()) {
         addClassMethodNameToMDC();
         super.info(String.format(_format, _args));
      }
   }

   /**
    * Logs a message with an exception backtrace at the INFO level.
    *
    * @param _t
    *           the exception
    * @param _format
    *           the message format
    * @param _args
    *           arguments to the message format
    */
   public void info(Throwable _t, String _format, Object... _args) {
      if (isInfoEnabled()) {
         addClassMethodNameToMDC();
         super.info(String.format(_format, _args));
         super.info(getStackTrace(_t, System.getProperty("line.separator")));
      }
      sendExceptionEmail(_t);
   }

   /**
    * Logs a message at the TRACE level.
    *
    * @param _string
    *           the message
    */
   public void trace(String _string) {
      if (isTraceEnabled()) {
         addClassMethodNameToMDC();
         super.trace(_string);
      }
   }

   /**
    * Logs a message at the TRACE level.
    *
    * @param _format
    *           the message format
    * @param _args
    *           arguments to the message format
    */
   public void trace(String _format, Object... _args) {
      if (isTraceEnabled()) {
         addClassMethodNameToMDC();
         super.trace(String.format(_format, _args));
      }
   }

   /**
    * Logs a message with an exception backtrace at the TRACE level.
    *
    * @param _t
    *           the exception
    * @param _format
    *           the message format
    * @param _args
    *           arguments to the message format
    */
   public void trace(Throwable _t, String _format, Object... _args) {
      if (isTraceEnabled()) {
         addClassMethodNameToMDC();
         super.trace(String.format(_format, _args));
         super.trace(getStackTrace(_t, System.getProperty("line.separator")));
      }
      sendExceptionEmail(_t);
   }
}
