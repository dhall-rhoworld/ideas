findCorrelatedVariables <- function(x, threshold = 0.5, use.var.names = FALSE) {
  
  # Finds pairs of numeric variables in the given data frame that are correlated.
  #
  # Args:
  #         x   : A data frame
  #   threshold : Threshold correlation coefficient.  A pair of variables is
  #               considered correlated if the absolute value of their correlation coefficient is
  #               greater than or equal to threshold.
  #
  # Returns:
  #   A two column matrix of pairs of variable indices
  
  df <- data.frame()
  count <- 0
  col.names <- colnames(x)
  for (i in 2:ncol(x)) {
    for (j in 1:(i-1)) {
      if (isTrulyNumeric(x, i) && isTrulyNumeric(x, j)) {
        correlated = FALSE
        tryCatch(
          {
            correlated <- abs(cor(x[,i], x[,j], use="pairwise.complete.obs")) >= threshold
          }, warning = function(w) {
            
          }, error = function(e) {
            
          }
        )
        if (correlated) {
          count <- count + 1
          if (count == 1) {
            if (use.var.names) {
              df <- data.frame(col.names[i], col.names[j], stringsAsFactors = FALSE)
            }
            else {
              df <- data.frame(i, j)
            }
            colnames(df) = c("v1", "v2")
          }
          else {
            if (use.var.names) {
              df <- rbind(df, c(col.names[i], col.names[j]))
            }
            else {
              df <- rbind(df, c(i, j))
            }
          }
        }
      }
    }
  }
  return(df)
}

isTrulyNumeric <- function(x, colNum) {
  
  # Determines if given column in data frame is truly numeric and not a date
  #
  # Args:
  #        x : A data frame
  #   colNum : Column number
  #
  # Returns:
  #   TRUE/FALSE
  
  return(is.numeric(x[,colNum]) && length(unique(x[, colNum])) > 25 &&
  ! grepl("date|month[^s]|year[^s]", colnames(x)[colNum], ignore.case = TRUE))
}

isFloatingPoint <- function(x, colNum) {
  if(!is.numeric(x[, colNum]) || grepl("date|month[^s]|year[^s]", colnames(x)[colNum], ignore.case = TRUE)) {
    return (FALSE)
  }
  v <- na.omit(x[, colNum])
  return (sum(v - trunc(v)) != 0)
}

findTrulyNumericVariables <- function(x) {
  var.is.numeric = logical(length = ncol(x))
  for (i in 1:ncol(x)) {
    var.is.numeric[i] <- isTrulyNumeric(x, i)
  }
  return (var.is.numeric)
}

loadStudyName <- function(studyName, con) {
  sql <- sprintf("select study_id from study where study_name = '%s'", studyName)
  rs <- dbSendQuery(con, sql)
  result <- dbFetch(rs)
  if (nrow(result) == 0) {
    message("Study ", studyName, " not in database.  Loading.")
    sql <- sprintf("insert into study(study_name) values('%s')", studyName)
    dbSendQuery(con, sql)
    studyId <- dbGetQuery(con, "select last_insert_id()")[1, 1]
  }
  else {
    studyId = result[1,1]
  }
  return (studyId)
}

loadDataset <- function(datasetName, studyId, con) {
  sql <- sprintf("select dataset_id from dataset where dataset_name = '%s'", datasetName)
  rs <- dbSendQuery(con, sql)
  result <- dbFetch(rs)
  if (nrow(result) == 0) {
    message("Dataset ", datasetName, " not in database.  Loading.")
    sql <- sprintf("insert into dataset(dataset_name, study_id) values('%s', %d)", datasetName, studyId)
    dbSendQuery(con, sql)
    datasetId <- dbGetQuery(con, "select last_insert_id()")[1, 1]
  }
  else {
    datasetId = result[1,1]
  }
  return(datasetId)
}

# TODO: This is not working at all.  Additionally, the initial match must be on both dataset_version_name and dataset_id.
loadDatasetVersion <- function(datasetVersionName, datasetId, con) {
  sql <- sprintf("select dataset_version_id from dataset_version where dataset_version_name = '%s'", datasetVersionName)
  rs <- dbSendQuery(con, sql)
  result <- dbFetch(rs)
  if (nrow(result) == 0) {
    message("Dataset version ", datasetVersionName, " not in database.  Loading.")
    sql <- sprintf("insert into dataset_version(dataset_version_name, dataset_id) values('%s', %d)", datasetVersionName, datasetId)
    dbSendQuery(con, sql)
    datasetVersionId <- dbGetQuery(con, "select last_insert_id()")[1, 1]
  }
  else {
    datasetVersionId = result[1,1]
  }
  return(datasetVersionId)
}

loadDataField <- function(dataFieldName, datasetId, lowerThresh, upperThresh, firstQuartile, secondQuartile, thirdQuartile, con) {
  #msg = paste(dataFieldName, datasetId, lowerThresh, upperThresh, firstQuartile, secondQuartile, thirdQuartile)
  #message(msg)
  sql <- sprintf("select data_field_id from data_field where data_field_name = '%s' and dataset_id = %d",
                 dataFieldName, datasetId)
  #message(sql)
  rs <- dbSendQuery(con, sql)
  result <- dbFetch(rs)
  if (nrow(result) == 0) {
    message("Data field ", dataFieldName, " not in database.  Loading.")
    sql <- sprintf(
      "insert into data_field(data_field_name, dataset_id, lower_threshold, upper_threshold, first_quartile, second_quartile, third_quartile) values('%s', %d, %f, %f, %f, %f, %f)",
      dataFieldName, datasetId, lowerThresh, upperThresh, firstQuartile, secondQuartile, thirdQuartile)
    dbSendQuery(con, sql)
    dataFieldId <- dbGetQuery(con, "select last_insert_id()")[1, 1]
  }
  else {
    dataFieldId = result[1,1]
    sql <- sprintf("update data_field set lower_threshold = %f, upper_threshold = %f, first_quartile = %f, second_quartile = %f, third_quartile = %f where data_field_id = %d",
                   lowerThresh, upperThresh, firstQuartile, secondQuartile, thirdQuartile, dataFieldId);
    dbSendQuery(con, sql);
  }
  return(dataFieldId)
}

loadSite <- function(studyId, siteName, con) {
  sql = sprintf("select site_id from site where site_name = '%s' and study_id = %d", siteName, studyId);
  rs <- dbSendQuery(con, sql)
  result <- dbFetch(rs)
  if (nrow(result) == 0) {
    message("Site ", siteName, " not in database.  Loading.")
    sql <- sprintf("insert into site(site_name, study_id) values('%s', %d)", siteName, studyId)
    dbSendQuery(con, sql)
    siteId <- dbGetQuery(con, "select last_insert_id()")[1, 1]
  }
  else {
    siteId = result[1, 1]
  }
  return (siteId)
}

loadSubject <- function(studyId, subjectName, con) {
  sql = sprintf("select subject_id from subject where study_id = %d and subject_name = '%s'", studyId, subjectName);
  rs <- dbSendQuery(con, sql);
  result <- dbFetch(rs);
  if (nrow(result) == 0) {
    message("Subject ", subjectName, " not in database.  Loading.")
    sql <- sprintf("insert into subject(subject_name, study_id) values ('%s', %d)", subjectName, studyId);
    dbSendQuery(con, sql)
    subjectId <- dbGetQuery(con, "select last_insert_id()")[1, 1]
  }
  else {
    subjectId <- result[1, 1]
  }
  return (subjectId)
}

loadBivariateCheck <- function(datasetId1, datasetId2, fieldName1, fieldName2, filePath, con) {
  sql <- sprintf("select bivariate_check_id from bivariate_check where dataset_id_1 = %d and dataset_id_2 = %d and data_field_1 = '%s' and data_field_2 = '%s'",
                 datasetId1, datasetId2, fieldName1, fieldName2);
  rs <- dbSendQuery(con, sql)
  result <- dbFetch(rs)
  if (nrow(result) == 0) {
    sql <- sprintf("insert into bivariate_check(dataset_id_1, dataset_id_2, data_field_1, data_field_2) values(%d, %d, '%s', '%s')",
                   datasetId1, datasetId2, fieldName1, fieldName2)
    dbSendQuery(con, sql)
    bivariateCheckId <- dbGetQuery(con, "select last_insert_id()")[1, 1]
  }
  else {
    bivariateCheckId <- result[1, 1]
  }
  sql <- sprintf("update bivariate_check set file_path = '%s' where bivariate_check_id = %d", filePath, bivariateCheckId);
  dbSendQuery(con, sql)
  return (bivariateCheckId)
}

updateBivariateCheck <- function(bivariateCheckId, intercept, slope, residualThreshold,
                                 densityThreshold, con) {
  sql <- paste("update bivariate_check ",
                "set intercept = ", intercept, ", ",
                "slope = ", slope, ", ",
                "residual_threshold = ", residualThreshold, ", ",
                "density_threshold = ", densityThreshold, " ",
                "where bivariate_check_id = ", bivariateCheckId, sep="");
  dbSendQuery(con, sql);
}

updateBivariateCheckHet <- function(bivariateCheckId, isHet, lambda, con) {
  sql <- paste("update bivariate_check ",
                "set is_het = ", isHet, ", ",
                "lambda = ", lambda, " ",
                "where bivariate_check_id = ", bivariateCheckId, sep="");
  dbSendQuery(con, sql);
}

writeNumeridAndPrimaryKeyFieldsToFile <- function(df, numericCol, studyName, formName, rootDir) {
  
  # Prepare data
  outDf <- df[c("RecruitID", "event", "Site")]
  outDf$value <- df[,numericCol]
  outDf <- na.omit(outDf)
  outDf <- outDf[order(outDf$value),]
  
  # Prepare directory
  studyDir <- paste(rootDir, "/", studyName, sep="")
  if (!file.exists(studyDir)) {
    message("Creating directory: ", studyDir)
    dir.create(studyDir)
  }
  datasetDir <- paste(studyDir, "/", formName, sep="")
  if (!file.exists(datasetDir)) {
    message("Creating directory: ", datasetDir)
    dir.create(datasetDir)
  }
  
  # Write data
  cleanedFieldName = gsub("/", "_per_", colnames(df)[numericCol])
  filePath <- paste(datasetDir, "/", cleanedFieldName, ".csv", sep="")
  write.csv(outDf, file=filePath, quote=FALSE, append=FALSE)
}

findAndLoadUnivariateOutliers <- function(df, studyName, formName, datasetVersionName, rootDir, con) {
  col.names = colnames(df)
  studyId <- loadStudyName(studyName, con)
  datasetId <- loadDataset(formName, studyId, con)
  datasetVersionId <- loadDatasetVersion(datasetVersionName, datasetId, con)
  numericFields <- which(findTrulyNumericVariables(df))
  total <- 0
  totalExisting <- 0
  totalNew <- 0
  for (i in numericFields) {
    message("Processing field: ", col.names[i])
    writeNumeridAndPrimaryKeyFieldsToFile(df, i, studyName, formName, rootDir)
    fieldName <- col.names[i]
    outlier.dat <- findUnivariateOutliers(df, i)
    upperThresh <- outlier.dat$upperThresh
    lowerThresh <- outlier.dat$lowerThresh
    firstQuartile <- outlier.dat$firstQuartile
    secondQuartile <- outlier.dat$secondQuartile
    thirdQuartile <- outlier.dat$thirdQuartile
    dataFieldId <- loadDataField(fieldName, datasetId, lowerThresh, upperThresh, firstQuartile, secondQuartile, thirdQuartile, con)
    outlier.index <- outlier.dat$outlierIndex
    outliers <- which(outlier.index)
    for (j in outliers) {
      total <- total + 1
      recruitId <- df[j, "RecruitID"]
      siteName <- df[j, "Site"]
      siteId <- loadSite(studyId, siteName, con)
      subjectId <- loadSubject(studyId, recruitId, con)
      event <- df[j, "event"]
      fieldValue <- df[j, i]
      
      # TODO: Change query to use subject_id instead of recruit_id
      sql <- paste(
        "select anomaly_id ",
        "from anomaly ",
        "where data_field_id = ", dataFieldId, " ",
        "and field_value = '", fieldValue, "' ",
        "and recruit_id = '", recruitId, "' ",
        "and event = '", event, "'",
        sep = ""
      )
      rs <- dbSendQuery(con, sql)
      result <- dbFetch(rs)
      if (nrow(result) == 0) {
        totalNew <- totalNew + 1
        
        # TODO: Remove recruit_id from insert
        sql <- paste(
          "insert into anomaly(anomaly_type, data_field_id, field_value, recruit_id, subject_id, event, site_id, version_first_seen_in, version_last_seen_in) ",
          "values('U', ", dataFieldId, ", '", fieldValue, "', '", recruitId, "', ", subjectId, ", '",
          event, "', ", siteId, ", ", datasetVersionId, ", ", datasetVersionId, ")",
          sep = ""
        )
        #message(sql)
        dbSendQuery(con, sql)
      }
      else {
        totalExisting <- totalExisting + 1
        outlierId = result[1, 1]
        sql <- sprintf("update anomaly set version_last_seen_in = %d where anomaly_id = %d", datasetVersionId, outlierId);
        dbSendQuery(con, sql)
      }
    }
  }
  return(list(c(total_outliers = total, new_outliers = totalNew, exising_outliers = totalExisting)))
}

findUnivariateOutliers <- function(x, colNum, cutoff.sd = 2) {
  m <- mean(x[,colNum], na.rm = TRUE)
  deltas <- abs(x[,colNum] - m)
  stdev <- sd(x[,colNum], na.rm = TRUE)
  cutoff <- cutoff.sd * stdev
  quants = quantile(x[,colNum], na.rm=TRUE)
  outlierDat <- list();
  outlierDat$outlierIndex <- deltas >= cutoff & !is.na(x[,colNum])
  outlierDat$upperThresh <- m + cutoff
  outlierDat$lowerThresh <- m - cutoff
  outlierDat$firstQuartile <- quants[2]
  outlierDat$secondQuartile <- quants[3]
  outlierDat$thirdQuartile <- quants[4]
  return (outlierDat)
}

findAndLoadBivariateOutliers <- function(x, col1, col2, cutoff.residual = 2, cutoff.density = 8) {
  
}

findBivariateOutliers <- function(x, col1, col2, cutoff.residual = 2, cutoff.density = 8) {
  
  # Finds bivariate outliers in two columns of a data frame.  A data point is
  # considered an outlier if its residual under linear regression is
  # unusually large and it occurs in a low density region of the data space.
  #
  # Args:
  #   x : A data frame
  #   col1 : A column number
  #   col2 : A column number
  #
  # Returns:
  #   A list with the following attributes:
  #     outlierIndex : A logical vector indicating whether the associated observation is an outlier
  #     lm.fit : A linear model fit to the data points
  #     residual.threshold : Outlier boundary for residuals
  #     density.threshold : Outlier boundary for density
  #     is.het : A Boolean value indicating whether the data are heteroschedastic
  #     ----------- Remaining attributes only appear when data are heteroschedastic ---------
  #     het.fit : A linear model fit to Box-Cox transformed data points
  #     points.hi : A data frame containing (X,Y) coordinates for the upper inlier-outlier boundary
  #     points.lo : A data frame containing (X,Y) coordinates for the lower inlier-outlier boundary
  #     lambda : Box-Cox lambda parameter
  
  # Structure that is returned
  outlierData = list()
  
  # Generate a new data frame with no NaN
  df <- x[c(col1, col2)]
  colnames(df) <- c("X", "Y")
  df$origRowNum <- seq.int(nrow(df))
  df <- na.omit(df)
  
  # Fit a linear model
  fit.y <- lm(Y ~ X, data = df)
  
  # Test if data are hetereoschadistic
  is.het <- ncvTest(fit.y)$p <= .05
  
  # Generate initial outlier index
  if (is.het) {
    message("   Data are hetereoschedastic.  Performing BoxCox transformation.")
    fit.bct <- BoxCoxTrans(df$Y)
    df["Y2"] <- predict(fit.bct, df$Y)
    fit.y2 = lm(Y2 ~ X, data = df)
    res <- abs(residuals(fit.y2))
    outlierData$het.fit = fit.bct
  }
  else {
    res <- abs(residuals(fit.y))
  }
  cutoff.res <- mean(res) + cutoff.residual * sd(res)
  if (is.het) {
    cutoff.alt <- (max(df$Y2) - min(df$Y2)) * 0.1
  }
  else {
    cutoff.alt <- (max(df$Y) - min(df$Y)) * 0.1
  }
  if (cutoff.res < cutoff.alt) {
    message("   Low magnitude of variance.  Using alternate cutoff for first phase of outlier detection.")
    cutoff.res <- cutoff.alt
  }
  outlierData$residual.threshold = cutoff.res
  outlier.index <- res >= cutoff.res
  
  # Remove index of putative outliers that are in high-density regions
  matrix.dist <- nndist(df$X, df$Y, k=1:5)
  mean.dist <- rowMeans(matrix.dist)
  cutoff.dist <- mean(mean.dist) + cutoff.density * sd(mean.dist)
  outlier.index <- outlier.index & mean.dist >= cutoff.dist
  outlierData$density.threshold = cutoff.dist
  
  # Fit boundary curve for heteroschedastic data
  if (is.het) {
    
    # Pick 50 points on regression line
    p = data.frame(X = seq(from = min(df$X), to = max(df$X), length.out = 100))
    p$Y = predict(fit.y2, p)
    
    # Calculate y coordinates of the outlier boundary line above and below points
    p$YHi = p$Y + cutoff.res
    p$YLo = p$Y - cutoff.res
    
    # Scale boundary y coordinates back to the original data range
    lambda = fit.bct$lambda
    p$YHi = (p$YHi * lambda + 1)^(1.0 / lambda)
    p$YLo = (p$YLo * lambda + 1)^(1.0 / lambda)
    outlierData$lambda = lambda
    
    outlierData$points.hi = p[c("X", "YHi")]
    outlierData$points.lo = p[c("X", "YLo")]
    colnames(outlierData$points.hi) = c("X", "Y")
    colnames(outlierData$points.lo) = c("X", "Y")
  }
  
  # Generate return outlier index vector with a value for each row in the input data frame
  returnVect <- logical(nrow(x))
  returnVect[df[outlier.index, "origRowNum"]] <- TRUE
  
  # Generate output structure
  outlierData$outlierIndex = returnVect
  outlierData$lm.fit = fit.y
  outlierData$is.het = is.het
  
  return(outlierData)
}

plotBivariateOutliers <- function(x, col1, col2, outlierData, inlier.col="green", outlier.col="red") {
  
  # Generates a scatter plot of inliers and outliers with a regression line and
  # outlier thresholds.
  #
  # Args:
  #             x : A data frame
  #          col1 : X-axis column
  #          col2 : Y-axis column
  #      outliers : A logical vector
  #    inlier.col : Color of inlier data points
  #   outlier.col : Color of outlier data points
  
  allPoints <- (x[, c(col1, col2)])
  colnames(allPoints) <- c("X", "Y")
  inliers <- na.omit(allPoints[!outlierData$outlierIndex,])
  outliers <- na.omit(allPoints[outlierData$outlierIndex,])
  allPoints <- na.omit(allPoints)
  colname.x <- colnames(x)[col1]
  colname.y <- colnames(x)[col2]
  plot(allPoints$X, allPoints$Y, col="white", xlab=colname.x, ylab=colname.y)
  points(inliers$X, inliers$Y, col=inlier.col)
  points(outliers$X, outliers$Y, col=outlier.col)
  if (outlierData$is.het) {
    lines(outlierData$points.hi$X, outlierData$points.hi$Y)
    lines(outlierData$points.lo$X, outlierData$points.lo$Y)
  }
  else {
    abline(a = fit$coefficients[1] + cutoff.res, b = fit$coefficients[2])
    abline(a = fit$coefficients[1] - cutoff.res, b = fit$coefficients[2])
  }
}