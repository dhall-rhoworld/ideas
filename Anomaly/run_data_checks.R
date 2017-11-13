#
# Main driver program that runs all data checks for RhoVer.
#

source("rhover_db.R")
source("rhover_io.R")
source("rhover_logger.R")


shouldCheckBeRunOnFile <- function(con, checkDf, filePath) {
  
  # Determines the (one) data check in the given data frame should be run on the given file.
  #
  # Args:
  #     con : Database connection
  #     checkDf : Data frame containing columns from the data_check database table and one row
  #
  # Returns:
  #   TRUE if check has never been run on the version of the file or there is a new version of parameters for the check at the
  #        global, study, or field level
  #   FALSE otherwise
  
}

shouldAnyCheckBeRunOnFile <- function(con, checkDf, filePath) {
  
  # Determines if any of the data checks in the given data frame should be run on the given file.
  #
  # Args:
  #     con : Database connection
  #     checkDf : Data frame containing columns from the data_check database table and one or more rows
  #
  # Returns:
  #   TRUE if checks have never been run on the version of the file or there is a new version of parameters for any of the checks at the
  #        global, study, or field level
  #   FALSE otherwise
}

shouldCheckBeRunOnDataField <- function(con, checkDf, dataFieldId) {
  
  # Determines the (one) data check in the given data frame should be run on the given field
  #
  # Args:
  #     con : Database connection
  #     dataFieldId : ID of a data field
  #
  # Returns:
  #   TRUE if check has never been run on the data field in the current version of the file or if there
  #        is a new version of parameters for the check at the global, study, or field level
  #   FALSE otherwise
  
}

runUnivariateChecksOnFile <- function(con, checksDf, filePath) {
  logInfo(paste("Processing file", filePath))
  
  return (0)
}

runUnivariateChecksOnStudyFolder <- function(con, checksDf, studyFolderDf) {
  path <- studyFolderDf[1, 2]
  logInfo(paste("Processing folder", path))
  
  # Fetch paths of all SAS data files
  sasFiles <- getAllSasFilePaths(path)
  
  # Process each SAS data file
  for (dataFile in sasFiles) {
    runUnivariateChecksOnFile(con, checksDf, dataFile)
  }
  
  return (0)
}

runUnivariateChecksOnStudy <- function(con, checksDf, studyDf) {
  
  # Run all univariate checks on a study
  #
  # Args:
  #     con : Database connection
  #     checksDf : Data frame containing checks to run
  #     studyDf : Data frame containing study to run
  
  studyName <- studyDf[1, 2]
  logInfo(paste("Processing study", studyName))
  
  # Fetch study folders
  studyId <- studyDf[1, 1]
  studyFoldersDf <- fetchAllStudyFolders(con, studyId)
  numFolders <- nrow(studyFoldersDf)
  if (numFolders == 0) {
    logWarning("No data folders")
    return (0)
  }
  
  # Process each study folder
  for (i in 1:numFolders) {
    runUnivariateChecksOnStudyFolder(con, checksDf, studyFoldersDf[i,])
  }
  
  return (0)
}

runUnivariateChecks <- function(con) {
  
  # Runs all univatate checks against all studies
  #
  # Args:
  #     con : Database connection

  # Fetch univariate data checks
  checksDf <- fetchAllUnivariateDataChecks(con)
  if (nrow(checks) == 0) {
    logWarning("No univariate checks defined")
    return (0)
  }
  
  # Fetch studies
  studiesDf <- fetchAllStudies(con)
  numStudies <- nrow(studiesDf)
  if (numStudies == 0) {
    logWarning("No studies")
    return (0)
  }
  
  # Process each study
  for (i in 1:numStudies) {
    runUnivariateChecksOnStudy(con, checksDf, studiesDf[i,])
  }
  
  return (0)
}

# --- TODO: Put below in proper order ---

# Run univariate checks
runUnivariateChecks(con)


con <- openDbConnection("rhover", "rhover", "rhover", "localhost")
dataCheckerRunId <- loadDataCheckerRun(con)
initializeLogging(con, dataCheckerRunId)
dbResult <- logInfo("Logging system initialized")
dbDisconnect(con)
