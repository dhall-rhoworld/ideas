library(sas7bdat)
library(RMySQL)
source("outlier_scout.R")

clinical.dir <- "S:/RhoFED/ICAC2/PROSE/Statistics/Data/Complete"
output.dir.root <- "C:/RhoVer"
working.dir <- "C:/Working"
study <- "PROSE"
output.dir <- paste(output.dir.root, "/", study, "/_bivariate", sep="")

cutoffResidual <- 2
cutoffDensity <- 8

con <- dbConnect(MySQL(),
                 user="rhover", password="rhover",
                 dbname="rhover", host="localhost")

files <- list.files(clinical.dir, pattern = "*.sas7bdat")

fpCols <- data.frame(dataset = character(), fieldName = character(), fieldNum = integer(), workingFile = character())

for (file in files) {
  path <- paste(clinical.dir, "/", file, sep = "")
  message("Processing file: ", path)
  
  # Get file timestamp to use as version name
  version.name <- file.info(path)$mtime
  
  # Read file into dataframe
  data <- read.sas7bdat(path)
  
  # Make sure data has primary key variables RecruitID and event
  cnames = colnames(data)
  if (sum(cnames == "RecruitID") == 0 || sum(cnames == 'event') == 0) {
    message("Dataset does not contain primary key fields StudyID and/or event.  Not processing.")
    next
  }
  
  # Replace variable names with labels
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
  
  # Extract dataset name
  dataset.name <- as.character(data[1, "form_name"])
  
  n <- ncol(data)
  for (i in 1:n) {
    if (isFloatingPoint(data, i)) {
      cname <- colnames(data)[i]
      workingFile <- paste(working.dir, "/", sub("\\.sas7bdat", "", file), "-", i, ".csv", sep="")
      message("   ", cname, " is floating point")
      newRow = data.frame(dataset = c(dataset.name), fieldName = c(cname), fieldNum = c(i), workingFile = c(workingFile))
      fpCols <- rbind(fpCols, newRow)
      df = na.omit(data[,c("RecruitID", "event", cname)])
      write.csv(df, file=workingFile)
    }
  }
}

source("outlier_scout.R")
studyId <- loadStudyName(study, con)
count <- 0
for (i in 2:nrow(fpCols)) {
  file1 = as.character(fpCols[i, "workingFile"])
  data1 = read.csv(file1)
  for (j in 1:(i-1)) {
    file2 = as.character(fpCols[j, "workingFile"])
    data2 = read.csv(file2)
    data3 = merge(data1, data2, by=c("RecruitID", "event"))
    dataset1 <- fpCols[i, "dataset"]
    fieldName1 <- fpCols[i, "fieldName"]
    fieldNum1 <- fpCols[i, "fieldNum"]
    datasetId1 <- loadDataset(dataset1, studyId, con)
    if (nrow(data3) > 0) {
      c = cor(data3[,4], data3[,6])
      if (!is.na(c) && abs(c) > 0.5 && abs(c) < 0.99) {
        count <- count + 1
        dataset2 <- fpCols[j, "dataset"]
        fieldName2 <- fpCols[j, "fieldName"]
        fieldNum2 <- fpCols[j, "fieldNum"]
        datasetId2 <- loadDataset(dataset2, studyId, con)
        message(dataset1, ".", fieldName1, " and ", dataset2, ".", fieldName2, " correlated: ", c)
        
        # Generate processed data file containing all data points
        outFile <- paste(dataset1, ".", fieldNum1, "_X_", dataset2, ".", fieldNum2, ".csv", sep="")
        outPath <- paste(output.dir, "/", outFile, sep="")
        write.csv(data3[,c(1, 2, 4, 6)], file=outPath)
        
        # Save new record for bivariate check
        bivariateCheckId <- loadBivariateCheck(datasetId1, datasetId2, fieldName1, fieldName2, outPath, con)
        
        # Run bivariate check
        outlierData <- findBivariateOutliers(data3, 4, 6, cutoffResidual, cutoffDensity)
        
        # Save general data check parameters
        intercept <- outlierData$lm.fit$coefficients[1]
        slope <- outlierData$lm.fit$coefficients[2]
        residualThreshold <- outlierData$residual.threshold
        densityThreshold <- outlierData$density.threshold
        updateBivariateCheck(bivariateCheckId, intercept, slope, residualThreshold, densityThreshold, con)
        
        # Save heteroschedastic data check parameters
        if (outlierData$is.het) {
          lambda <- outlierData$lambda
          if (is.na(lambda)) {
            message("Lambda is NA")
          }
          else {
            updateBivariateCheckHet(bivariateCheckId, 1, lambda, con)
          }
        }
      }
    }
  }
}
message("Num correlated variables: ", count)
