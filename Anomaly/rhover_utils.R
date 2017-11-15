#
# rhover_utils
#
# Contains general utility functions
#

getDate <- function(x) {
  dt <- NULL
  if (grepl("[0-9]+(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|NOV|DEC)[0-9]{4}", x, ignore.case = TRUE)) {
    dt <- as.Date(x, "%d%b%Y")
  }
  else if (grepl("", ))
  
  return (dt)
}
