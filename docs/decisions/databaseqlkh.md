1. trong file application.md hãy cấu hình database thứ 2 cho tôi
   url: jdbc:mysql://103.15.51.97:3306/cm_20211018
   username: luongtd
   password: 1234

2. Trong database này có 3 bảng dữ liệu :
   CREATE TABLE `customer` (
   `CustomerId` int NOT NULL AUTO*INCREMENT,
   `Name` varchar(200) DEFAULT NULL,
   `ShortName` varchar(50) DEFAULT NULL,
   `Address` varchar(200) DEFAULT NULL,
   `AddressOld` varchar(200) DEFAULT NULL,
   `BudgetRelationOfficeCode` varchar(40) DEFAULT NULL,
   `PassportNumber` varchar(20) DEFAULT NULL,
   `CitizenIdentificationCard` varchar(20) DEFAULT NULL,
   `Phone` varchar(40) DEFAULT NULL,
   `Fax` varchar(40) DEFAULT NULL,
   `Email` varchar(150) DEFAULT NULL,
   `Sms` varchar(16) DEFAULT NULL,
   `ContactName` varchar(70) DEFAULT NULL,
   `Description` varchar(200) DEFAULT NULL,
   `EntryDate` datetime DEFAULT NULL,
   `ExitDate` datetime DEFAULT NULL,
   `BankNo` varchar(80) DEFAULT NULL,
   `BankName` varchar(150) DEFAULT NULL,
   `TaxCode` varchar(20) DEFAULT NULL,
   `Type` int DEFAULT NULL,
   `OrderOnRoad` double DEFAULT NULL,
   `IsActive` tinyint unsigned DEFAULT NULL,
   `IsPayOnline` tinyint unsigned DEFAULT NULL,
   `EnvFee` double DEFAULT NULL,
   `TaxFee` double DEFAULT NULL,
   `ModifiedById` int DEFAULT NULL,
   `ModifiedDate` datetime DEFAULT NULL,
   `Code` varchar(20) DEFAULT NULL,
   `DigiCode` varchar(10) DEFAULT NULL,
   `ContactPhone` varchar(40) DEFAULT NULL,
   `ContactEmail` varchar(80) DEFAULT NULL,
   `PriceSchemaId` int DEFAULT NULL,
   `RoadId` int DEFAULT NULL,
   `Password` varchar(75) DEFAULT NULL,
   `PasswordEncrypted` tinyint unsigned DEFAULT NULL,
   `PasswordReset` tinyint unsigned DEFAULT NULL,
   `PasswordModifiedDate` datetime DEFAULT NULL,
   `Balance` double DEFAULT NULL,
   `IsSendSMSOverdue` tinyint unsigned DEFAULT NULL,
   `SendSMSOverdueDate` varchar(8) DEFAULT NULL,
   `SendSMSOverdueTimes` int DEFAULT NULL,
   `IsWaterCut` tinyint unsigned DEFAULT NULL,
   `WaterCutDate` varchar(8) DEFAULT NULL,
   `OldCode` varchar(20) DEFAULT NULL,
   `WaterCutOrderNum` varchar(40) DEFAULT NULL,
   `HasInstallContract` tinyint unsigned DEFAULT NULL,
   `HasSupplyContract` tinyint unsigned DEFAULT NULL,
   `Reserve1` varchar(100) DEFAULT NULL,
   `Reserve2` varchar(100) DEFAULT NULL,
   `Reserve3` varchar(100) DEFAULT NULL,
   `Reserve4` varchar(100) DEFAULT NULL,
   `Reserve5` varchar(100) DEFAULT NULL,
   `BalanceDate` varchar(14) DEFAULT NULL,
   `Status` int DEFAULT NULL,
   `SmsDate` datetime DEFAULT NULL,
   PRIMARY KEY (`CustomerId`),
   KEY `R_17` (`PriceSchemaId`),
   KEY `R_19` (`RoadId`),
   CONSTRAINT `R_17` FOREIGN KEY (`PriceSchemaId`) REFERENCES `supportingtable` (`SupportingTableId`),
   CONSTRAINT `R_19` FOREIGN KEY (`RoadId`) REFERENCES `supportingtable` (`SupportingTableId`)
   ) ENGINE=InnoDB AUTO_INCREMENT=13612 DEFAULT CHARSET=utf8mb3;
   /*!40101 SET character*set_client = @saved_cs_client */;

--
-- Table structure for table `monthinvoice`
--

DROP TABLE IF EXISTS `monthinvoice`;
/_!40101 SET @saved_cs_client = @@character_set_client _/;
/_!50503 SET character_set_client = utf8mb4 _/;
CREATE TABLE `monthinvoice` (
`MonthInvoiceId` int NOT NULL AUTO*INCREMENT,
`RootKey` varchar(30) DEFAULT NULL,
`Fkey` varchar(36) DEFAULT NULL,
`InvStatus` int DEFAULT NULL,
`SupplyingContractId` int DEFAULT NULL,
`CustomerId` int DEFAULT NULL,
`YearMonth` varchar(8) DEFAULT NULL,
`WaterMeterSerial` varchar(20) DEFAULT NULL,
`CreatedDate` varchar(8) DEFAULT NULL,
`StubNum` varchar(20) DEFAULT NULL,
`TimeToUsed` varchar(20) DEFAULT NULL,
`NumOfHouseHold` int DEFAULT NULL,
`Amount` double DEFAULT NULL,
`SentDate` varchar(8) DEFAULT NULL,
`EnvFee` double DEFAULT NULL,
`TaxFee` double DEFAULT NULL,
`ModifiedById` int DEFAULT NULL,
`ModifiedDate` datetime DEFAULT NULL,
`PaymentStatus` int DEFAULT NULL,
`PaymentLineId` mediumtext,
`Status` int DEFAULT NULL,
`Comment` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
`OldVal` int DEFAULT NULL,
`NewVal` int DEFAULT NULL,
`Range1` int DEFAULT NULL,
`Range2` int DEFAULT NULL,
`Range3` int DEFAULT NULL,
`PrintedById` int DEFAULT NULL,
`PrintedDate` datetime DEFAULT NULL,
`RoadId` int DEFAULT NULL,
`OrderOnRoad` double DEFAULT NULL,
`PrintedStatus` int DEFAULT NULL,
`PriceSchemaId` int DEFAULT NULL,
`Reserve1` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
`Reserve2` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
`MaxScale` bigint DEFAULT NULL,
`StartDate` varchar(8) DEFAULT NULL,
`EndDate` varchar(8) DEFAULT NULL,
`BlankNo` varchar(10) DEFAULT NULL,
`IsLastInvoice` tinyint unsigned DEFAULT NULL,
`TemplateCode` varchar(20) DEFAULT NULL,
`Serial` varchar(10) DEFAULT NULL,
`SignedDate` datetime DEFAULT NULL,
`RecordById` int DEFAULT '0',
`SmsDate` datetime DEFAULT NULL,
`SmsOut` int DEFAULT NULL,
PRIMARY KEY (`MonthInvoiceId`),
KEY `R_11` (`SupplyingContractId`),
KEY `R_12` (`YearMonth`),
KEY `R_13` (`RoadId`),
KEY `R_14` (`YearMonth`,`RoadId`),
KEY `R_C_C_I_P` (`CustomerId`,`CreatedDate`,`InvStatus`,`PaymentStatus`)
) ENGINE=InnoDB AUTO_INCREMENT=803200 DEFAULT CHARSET=latin1;
/*!40101 SET character*set_client = @saved_cs_client */;

--
-- Table structure for table `payment`
--

DROP TABLE IF EXISTS `payment`;
/_!40101 SET @saved_cs_client = @@character_set_client _/;
/_!50503 SET character_set_client = utf8mb4 _/;
CREATE TABLE `payment` (
`PaymentId` int NOT NULL AUTO_INCREMENT,
`YearMonth` varchar(8) DEFAULT NULL,
`PaymentNum` varchar(20) DEFAULT NULL,
`TotalAmount` double DEFAULT NULL,
`NumCustomers` int DEFAULT NULL,
`PaidDate` varchar(14) DEFAULT NULL,
`BankId` mediumtext,
`EmployeeId` mediumtext,
`RoadId` mediumtext,
`Remarks` varchar(100) DEFAULT NULL,
`Status` int DEFAULT NULL,
`ModifiedById` mediumtext,
`ModifiedDate` datetime DEFAULT NULL,
PRIMARY KEY (`PaymentId`)
) ENGINE=InnoDB AUTO_INCREMENT=3039 DEFAULT CHARSET=utf8mb3; 3. Đăng nhập → Lấy danh sách hóa đơn theo khách hàng → Kiểm tra trường trạng thái thanh toán → Hiển thị cho khách hàng. Viết API theo luồng này để frontend biết.
cấu trúc của api có dạng
hàm get: "data": {
"meta": {
"page": 1,
"pageSize": 20,
"pages": 1,
"total": 4
},
"result": [
{
"id": 1,
"name": "Super Admin",
"email": "luongtd@toctienltd.vn",
"age": 30,
"address": "Ho Chi Minh City",
"gender": "MALE",
"avatar": "/uploads/avatars/1775419057091_logocty1.jpg",
"company": {
"id": 1,
"name": "Công ty TNHH Cấp nước Tóc Tiên"
},
"roles": [
{
"id": 1,
"name": "SUPER_ADMIN"
}
],
"createdAt": "2026-04-05T18:40:26.449875Z",
"updatedAt": "2026-04-05T19:57:42.582335Z"
}

        ]
    },
    "message": "Lấy danh sách người dùng thành công",
    "statusCode": 200

}
