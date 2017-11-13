#
# Functions for writing log messages to the database, console, and log file
#

l_con <<- NULL
l_dataCheckerRunId <<- -1

messageTypeNames <- data.frame(Code = c("F", "E", "W", "I"), Name = c("FATAL", "ERROR", "WARNING", "INFO"),
                               row.names = "Code")

initializeLogging <- function(con, dataCheckerRunId) {
  
  # Initialize logging system
  #
  # Args:
  #    con : Database connection
  #    dataCheckerRunId: ID of data checker run
  
  l_con <<- con
  l_dataCheckerRunId <<- dataCheckerRunId
}

logMessage <- function(messageType, message) {
  
  # Log a message to the console, database, and log file
  #
  # Args:
  #     messageType: A single character code as follows:
  #          'F' = Fatal
  #          'E' = Error
  #          'W' = Warning
  #          'I' = Information
  #     message : Message to log
  
  name <- messageTypeNames[messageType, 1]
  fullMessage <- paste(name, ": ", message, sep="");
  message(fullMessage);
  insertLogMessage(l_con, messageType, message, l_dataCheckerRunId)
}

logFatal <- function(message) {
  logMessage("F", message)
}

logError <- function(message) {
  logMessage("E", message)
}

logWarning <- function(message) {
  logMessage("W", message)
}

logInfo <- function(message) {
  logMessage("I", message)
}