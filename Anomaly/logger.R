#
# Functions for writing log messages to the database, console, and log file
#

messageTypeNames <- data.frame(Code = c("F", "E", "W", "I"), Name = c("FATAL", "ERROR", "WARNING", "INFO"))

logMessage <- function(con, dataCheckerRunId, messageType, message) {
  
  # Log a message to the console, database, and log file
  #
  # Args:
  #     con : Database connection
  #     dataCheckerRunId: ID of data checker run
  #     messageType: A single character code as follows:
  #          'F' = Fatal
  #          'E' = Error
  #          'W' = Warning
  #          'I' = Information
  
  fullMessage <- paste(messageTypeNames[messageType, 2], ": ", message, sep="");
  message(fullMessage);
}

logFatal <- function(con, dataCheckerRunId, message) {
  logMessage(con, dataCheckerRunId, "F", message)
}

logError <- function(con, dataCheckerRunId, message) {
  logMessage(con, dataCheckerRunId, "E", message)
}

logWarning <- function(con, dataCheckerRunId, message) {
  logMessage(con, dataCheckerRunId, "W", message)
}

logInformation <- function(con, dataCheckerRunId, message) {
  logMessage(con, dataCheckerRunId, "I", message)
}