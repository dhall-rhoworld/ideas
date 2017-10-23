library(sas7bdat)
library(RMySQL)
source("outlier_scout.R")

#clinical.dir <- "S:/RhoFED/ICAC2/PROSE/Statistics/Data/Complete"
#output.dir <- "C:/RhoVer"
clinical.dir <- "/Users/dhall/Data/Clinical"
output.dir <- "/Users/dhall/Data/Output"
study <- "PROSE"

# Open database connection
con <- dbConnect(MySQL(),
                 user="rhover", password="rhover",
                 dbname="rhover", host="localhost")

# Get list of SAS data files
#files <- list.files(clinical.dir, pattern = "*.sas7bdat")
files <- list.files(clinical.dir)

# Run univariate checks in each file
for (file in files) {
  path <- paste(clinical.dir, "/", file, sep = "")
  message("Processing file: ", path)
  
  # Get file timestamp to use as version name
  version.name <- file.info(path)$mtime
  
  # Read file into dataframe
  #data <- read.sas7bdat(path)
  data <- read.csv(path)
  
  # Replace variable names with labels
  if (FALSE) {
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
  }
  
  # Extract dataset name
  dataset.name <- data[1, "form_name"]
  
  # Find and load outliers
  findAndLoadUnivariateOutliers(data, study, dataset.name, version.name, con)
  
  # Write data to CSV file
  out.path <- paste(output.dir, "/", dataset.name, ".csv", sep = "")
  write.csv(data, file = out.path)
  
}

