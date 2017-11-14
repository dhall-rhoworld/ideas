#
# rhover_io
#
# Contains functions for working with files and directories
#

getAllSasFilePaths <- function(folderPath) {
  
  # Get paths of all SAS data files in given folder.  Does not scan subfolders.
  #
  # Args:
  #     folderPath : Path to parent folder
  #
  # Return:
  #     A vector of file paths
  
  return (paste(folderPath, "/", list.files(folderPath, pattern = "*.sas7bdat"), sep=""))
}

extractSasData <- function(filePath) {
  
  # Extracts data from given SAS data file gracefully handling errors
  #
  # Args:
  #     filePath : Absolute path to file
  #
  # Return:
  #     A data frame containing extracted data, or NULL if there was a read error
  df = NULL
  tryCatch({
    df = read.sas7bdat(filePath)
  }, warning = function(w) {
    message(w)
  }, error = function(e) {
    message(e)
  }, finally = function(e) {
    
  })
  return (df)
}


getAllCommonVariables <- function(filePaths) {
  commonVars = character(length = 0)
  for (filePath in filePaths) {
    message("Processing", filePath)
    df = extractSasData(filePath)
    cnames = colnames(df)
    if (is.null(cnames)) {
      message("Error reading file.  Moving to next.")
      next
    }
    if (length(commonVars) == 0) {
      commonVars = cnames
    }
    else {
      commonVars = intersect(commonVars, cnames)
    }
  }
  return (commonVars)
}
