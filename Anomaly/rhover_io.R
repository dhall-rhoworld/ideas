#
# Contains functions for file I/O in RhoVer directories and data files
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
  
  # Extracts data from given SAS data file
  #
  # Args:
  #     filePath : Absolute path to file
  #
  # Return:
  #     A data frame containing extracted data
  
}

testRhoverIo <- function() {
  
  # Tests all functions in this file
  
  getAllSasFilePaths("S:/RhoFED/ICAC2/PROSE/Statistics/Data/Complete")
}

