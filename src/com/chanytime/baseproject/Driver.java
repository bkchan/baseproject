package com.chanytime.baseproject;

import com.chanytime.baseproject.configuration.Properties;
import com.chanytime.baseproject.log.Logger;
import com.chanytime.baseproject.util.MiscUtil;

public class Driver {

   public static void main(String args[]) {
      Logger.getLogger().debug("this demonstrates a class logger -- this is the default");
      Logger.getLogger("CHANMAN").debug("this demonstrates a logger named CHANMAN");
      int i = 199291082;
      Logger.getLogger().debug("this %s demonstrates printf functionality -- my SSN is %d, and 17/7 is %f",
                               "baby",
                               i,
                               17.0/7.0);
      System.out.println("DatabaseUrlScheme = " + Properties.getProperties().getDatabaseUrlScheme());
      System.out.println("DatabaseUrlHostname = " + Properties.getProperties().getDatabaseUrlHostname());
      System.out.println("WebDatabaseUsername = " + Properties.getProperties().getWebDatabaseUsername());
      System.out.println("WebDatabasePassword = " + Properties.getProperties().getWebDatabasePassword());
      System.out.println("AdminDatabaseUsername = " + Properties.getProperties().getAdminDatabaseUsername());
      System.out.println("AdminDatabasePassword = " + Properties.getProperties().getAdminDatabasePassword());
      System.out.println("BatchDatabaseUsername = " + Properties.getProperties().getBatchDatabaseUsername());
      System.out.println("BatchDatabasePassword = " + Properties.getProperties().getBatchDatabasePassword());
      System.out.println("LoggerIdentifier = " + Properties.getProperties().getLoggerIdentifier());
      System.out.println("Production = " + Properties.getProperties().getProduction());
      System.out.println("NonproductionPrefix = " + Properties.getProperties().getNonproductionPrefix());
      System.out.println("SmtpHost = " + Properties.getProperties().getSmtpHost());
      System.out.println("EmailFromField = " + Properties.getProperties().getEmailFromField());
      System.out.println("LoggerEmailDestination = " + Properties.getProperties().getLoggerEmailDestination());
      System.out.println("SqlTraceEnabled = " + Properties.getProperties().getSqlTraceEnabled());
      Logger.getLogger().debug("random string between 5 and 8 characters: %s", MiscUtil.generateRandomString(null, 5, 8));
      Logger.getLogger().debug("random string between 5 and 8 characters: %s", MiscUtil.generateRandomString(null, 5, 8));
      Logger.getLogger().debug("random string between 5 and 8 characters: %s", MiscUtil.generateRandomString(null, 5, 8));
      Logger.getLogger().debug("random string between 3 and 3 characters: %s", MiscUtil.generateRandomString(null, 3, 3));
      Logger.getLogger().debug("random string between 3 and 3 characters: %s", MiscUtil.generateRandomString(null, 3, 3));
      Logger.getLogger().trace("random string between 3 and 3 characters: %s", MiscUtil.generateRandomString(null, 3, 3));
   }

}

