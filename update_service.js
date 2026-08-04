const fs = require('fs');

let content = fs.readFileSync('src/main/java/vn/hoidanit/springrestwithai/qlkh/invoiceadmin/InvoiceAdminServiceImpl.java', 'utf8');

// replace the digiCode extraction and findAdminInvoices call
content = content.replace(
    '        String customerName = (filter != null) ? filter.getCustomerName() : null;\n        String digiCode = (filter != null) ? filter.getDigiCode() : null;',
    '        String keyword = (filter != null) ? filter.getDigiCode() : null;\n        if (keyword == null || keyword.isEmpty()) {\n            keyword = (filter != null) ? filter.getCustomerName() : null;\n        }'
);

content = content.replace(
    '        Page<AdminInvoiceResponse> page = monthInvoiceRepository.findAdminInvoices(yearMonth, paymentStatus, customerName, digiCode, remindStatus, roadId, remindedIds, overdueIds, cutwaterIds, invoiceNotifiedIds, invoiceNotifyStatus, invStatus, paymentNotifiedIds, paymentNotifyStatus, pageable);',
    '        Page<AdminInvoiceResponse> page = monthInvoiceRepository.findAdminInvoices(yearMonth, paymentStatus, keyword, remindStatus, roadId, remindedIds, overdueIds, cutwaterIds, invoiceNotifiedIds, invoiceNotifyStatus, invStatus, paymentNotifiedIds, paymentNotifyStatus, pageable);'
);

fs.writeFileSync('src/main/java/vn/hoidanit/springrestwithai/qlkh/invoiceadmin/InvoiceAdminServiceImpl.java', content);
console.log('InvoiceAdminServiceImpl updated');
