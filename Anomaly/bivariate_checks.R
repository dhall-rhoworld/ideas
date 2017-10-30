library(sas7bdat)
library(RMySQL)
source("outlier_scout.R")

clinical.dir <- "S:/RhoFED/ICAC2/PROSE/Statistics/Data/Complete"
output.dir <- "C:/RhoVer"
study <- "PROSE"

con <- dbConnect(MySQL(),
                 user="rhover", password="rhover",
                 dbname="rhover", host="localhost")

files <- list.files(clinical.dir, pattern = "*.sas7bdat")

for (file in files) {
  path <- paste(clinical.dir, "/", file, sep = "")
  message("Processing file: ", path)
  
  # Get file timestamp to use as version name
  version.name <- file.info(path)$mtime
  
  # Read file into dataframe
  data <- read.sas7bdat(path)
  
  # Make sure data has primary key variables RecruitID and event
  cnames = colnames(data)
  if (sum(cnames == "StudyID") == 0 || sum(cnames == 'event') == 0) {
    message("Dataset does not contain primary key fields StudyID and/or event.  Not processing.")
    next
  }
  
  # Replace variable names with labels
  #if (FALSE) {
  attrs = attr(data, "column.info")
  num.labels = length(attrs)
  labels = vector(length = num.labels)
  for (i in 1:num.labels) {
    if (is.null(attrs[[i]]$label)) {
      labels[i] = attrs[[i]]$name
    }
    else {
      labels[i] = attrs[[i]]$label
    }
  }
  colnames(data) = gsub(" ", "_", labels)
  #}
  
  # Extract dataset name
  dataset.name <- data[1, "form_name"]
  
}