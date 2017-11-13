#
# Contains functions for interacting with the RhoVer database
#

library(RMySQL)

openDbConnection <- function(user, password, database, host) {
  
  # Opens a connection with the RhoVer database
  #
  # Args:
  #     user : Username
  #     password : Password
  #     database : Name of database
  #     host : URL (include port if not default) of server
  #
  # Returns:
  #     A database connection
  
  return (dbConnect(MySQL(), user=user, password=password, dbname=database, host=host))
}

loadDataCheckerRun <- function(con) {
  
  sql <- "insert into data_checker_run() values()";
  dbSendQuery(con, sql)
  return (dbGetQuery(con, "select last_insert_id()")[1, 1])
}

insertLogMessage <- function(con, messageType, message, dataCheckerRunId) {
  
  # Insert a log message into the database
  #
  # Args:
  #     con : Database connection
  #     messageType: Message type code.  See rhover_logger.R for allowed values.
  #     message : Message to log
  #     dataCheckerRunId : ID of data checker run
  
  sql <- paste(
    "insert into data_checker_message(message_type, message, data_checker_run_id) ",
    "values('", messageType, "', '", message, "', ", dataCheckerRunId, ")",
    sep = ""
  
  )
  return (dbSendQuery(con, sql))
}
  
  message_type CHAR(1) NOT NULL,
message VARCHAR(500) NOT NULL,
data_checker_run_id BIGINT NOT NULL,

fetchAllUnivariateDataChecks <- function(con) {
  
  # Fetch all univariate data checks
  #
  # Args:
  #     con : Database connection
  #
  # Returns:
  #     A data frame with columns 'data_check_id' and 'data_check_code'
  
  sql <- "select data_check_id, data_check_code from data_check where num_variables = 1";
  return (dbGetQuery(con, sql))
}

fetchAllStudies <- function(con) {
  
  # Fetches all studies in the RhoVer database
  #
  # Args:
  #    con : A database connection
  #
  # Returns:
  #    A data frame with columns 'study_id' and 'study_name'
  
  sql <- "select study_id, study_name from study"
  return (dbGetQuery(con, sql))
}

fetchAllStudyFolders <- function(con, studyId) {
  
  # Fetches all study folders associated with the given studyId
  #
  # Args:
  #     con : Database connection
  #     studyId : Study ID
  #
  # Returns:
  #     A data frame with columns 'study_folder_id' and 'folder_path'
  
  sql <- sprintf("select study_folder_id, folder_path from study_folder where study_id = %d", studyId)
  return (dbGetQuery(con, sql))
}

fetchDataset <- function(con, filePath) {
  
  # Fetch dataset associated with given file path
  #
  # Args:
  #     con : Database connection
  #     filePath : Absolute path to a data file
  #
  # Returns:
  #     
}

testRhoverDb <- function() {
  
  # Tests all functions in this file
  
  con <- openDbConnection("rhover", "rhover", "rhover", "localhost")
  fetchAllDataChecks(con)
  fetchAllStudies(con)
  fetchAllStudyFolders(con, 1)
  fetchLatestGlobalParametersVersion(con, 1)
}
