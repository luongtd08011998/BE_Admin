const fs = require('fs');

let content = fs.readFileSync('src/main/java/vn/hoidanit/springrestwithai/qlkh/invoice/MonthInvoiceRepository.java', 'utf8');

// replace the query parts
content = content.replace(
    '            AND (:customerName IS NULL OR :customerName = \'\' OR LOWER(c.name) LIKE LOWER(CONCAT(\'%\', :customerName, \'%\')))\n            AND (:digiCode IS NULL OR :digiCode = \'\' OR LOWER(c.digiCode) LIKE LOWER(CONCAT(\'%\', :digiCode, \'%\')))',
    '            AND (:keyword IS NULL OR :keyword = \'\' OR LOWER(c.name) LIKE LOWER(CONCAT(\'%\', :keyword, \'%\')) OR LOWER(c.digiCode) LIKE LOWER(CONCAT(\'%\', :keyword, \'%\')))'
);

// replace signature
content = content.replace(
    '            @Param("paymentStatus") Integer paymentStatus,\n            @Param("customerName") String customerName,\n            @Param("digiCode") String digiCode,\n            @Param("remindStatus") Integer remindStatus,',
    '            @Param("paymentStatus") Integer paymentStatus,\n            @Param("keyword") String keyword,\n            @Param("remindStatus") Integer remindStatus,'
);

fs.writeFileSync('src/main/java/vn/hoidanit/springrestwithai/qlkh/invoice/MonthInvoiceRepository.java', content);
console.log('MonthInvoiceRepository updated');
