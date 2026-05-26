CREATE TABLE `payment` (
  `PaymentId` int(11) NOT NULL AUTO_INCREMENT,
  `YearMonth` varchar(8) DEFAULT NULL,
  `PaymentNum` varchar(20) DEFAULT NULL,
  `TotalAmount` double DEFAULT NULL,
  `NumCustomers` int(11) DEFAULT NULL,
  `PaidDate` varchar(14) DEFAULT NULL,
  `BankId` mediumtext,
  `EmployeeId` mediumtext,
  `RoadId` mediumtext,
  `Remarks` varchar(100) DEFAULT NULL,
  `Status` int(11) DEFAULT NULL,
  `ModifiedById` mediumtext,
  `ModifiedDate` datetime DEFAULT NULL,
  PRIMARY KEY (`PaymentId`)
) ENGINE=InnoDB AUTO_INCREMENT=3047 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `paymentline`
--

DROP TABLE IF EXISTS `paymentline`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paymentline` (
  `PaymentLineId` int(11) NOT NULL AUTO_INCREMENT,
  `PaymentId` mediumtext,
  `YearMonth` varchar(8) DEFAULT NULL,
  `PaidDate` varchar(14) DEFAULT NULL,
  `Amount` double DEFAULT NULL,
  `Remark` varchar(100) CHARACTER SET utf8 DEFAULT NULL,
  `ModifiedById` int(11) DEFAULT NULL,
  `ModifiedDate` datetime DEFAULT NULL,
  `EmployeeId` int(11) DEFAULT NULL,
  `PaymentNum` varchar(20) CHARACTER SET utf8 DEFAULT NULL,
  `ReferenceNumber` varchar(50) CHARACTER SET utf8 DEFAULT NULL,
  `CustomerId` int(11) DEFAULT NULL,
  `RoadId` mediumtext,
  `Status` int(11) DEFAULT NULL,
  `Source` int(11) DEFAULT NULL,
  `SmsDate` datetime DEFAULT NULL,
  `BankId` mediumtext,
  PRIMARY KEY (`PaymentLineId`),
  KEY `R_22` (`EmployeeId`),
  KEY `R_27` (`CustomerId`),
  CONSTRAINT `R_27` FOREIGN KEY (`CustomerId`) REFERENCES `customer` (`CustomerId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=308938 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;