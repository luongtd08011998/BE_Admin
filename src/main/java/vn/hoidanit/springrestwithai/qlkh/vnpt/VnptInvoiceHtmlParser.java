package vn.hoidanit.springrestwithai.qlkh.vnpt;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import vn.hoidanit.springrestwithai.qlkh.dto.InvoiceViewResponse;

@Component
public class VnptInvoiceHtmlParser {

    public InvoiceViewResponse parse(String html, String status) {
        Document doc = Jsoup.parse(html);

        String invoiceType = textById(doc, "BTN-name");
        String serialNo = textById(doc, "BTN-SerialNo");

        String invoiceNo = "";
        Element invNoEl = doc.selectFirst(".BTN-invoice-no");
        if (invNoEl != null) {
            invoiceNo = invNoEl.text();
        }

        String invoiceDate = parseDate(doc);
        String formCode = parseLabelValue(doc, "Mẫu số");

        String sellerName = textById(doc, "BTN-ComName");
        String sellerTaxCode = textById(doc, "BTN-ComTaxCode");
        String sellerAddress = textById(doc, "BTN-ComAddress");
        String sellerPhone = textById(doc, "BTN-ComPhone");

        String buyerName = "";
        String buyerTaxCode = "";
        String buyerAddress = "";
        String buyerPhone = "";
        String buyerCode = "";
        String paymentPeriod = "";
        String year = "";

        Element buyerSection = doc.selectFirst(".BTN-buyer");
        if (buyerSection != null) {
            java.util.Map<String, String> buyerFields = parseBuyerFields(buyerSection);
            buyerName = buyerFields.getOrDefault("tên khách hàng", "");
            buyerAddress = buyerFields.getOrDefault("địa chỉ", "");
            buyerTaxCode = buyerFields.getOrDefault("mã số thuế", "");
            buyerPhone = buyerFields.getOrDefault("điện thoại", "");
            buyerCode = buyerFields.getOrDefault("mã khách hàng", "");
            paymentPeriod = buyerFields.getOrDefault("kỳ thanh toán", "");
            year = buyerFields.getOrDefault("năm", "");
        }

        String newMeterReading = "";
        String oldMeterReading = "";
        String waterConsumption = "";

        Element meterTable = doc.selectFirst("#chiSoMoiChiSoCu");
        if (meterTable != null) {
            Elements rows = meterTable.select("tr");
            for (Element row : rows) {
                if (row.hasClass("BTN-title") || row.hasClass("BTN-empty")) continue;
                Elements cells = row.select("td");
                if (cells.size() >= 3) {
                    String c0 = cells.get(0).text().trim();
                    String c2 = cells.get(2).text().trim();
                    if (!c0.isEmpty() && !c2.isEmpty()) {
                        newMeterReading = c0;
                        oldMeterReading = cells.get(1).text().trim();
                        waterConsumption = c2;
                        break;
                    }
                }
            }
        }

        String waterTaxableAmount = parseTotalByLabel(doc, "Tiền nước tính thuế");
        String vatAmount = parseTotalByLabel(doc, "Thuế GTGT");
        String envProtectionFee = parseTotalByLabel(doc, "Phí bảo vệ môi trường");
        String totalAmount = parseTotalByLabel(doc, "Tổng số tiền thanh toán");
        String totalInWords = parseTotalInWords(doc);
        String paymentMethod = textById(doc, "BTN-ComBankName")
                + " - " + textById(doc, "BTN-ComBankNo");

        return new InvoiceViewResponse(
                invoiceNo, invoiceDate, invoiceType, formCode, serialNo,
                sellerName, sellerTaxCode, sellerAddress, sellerPhone,
                buyerName, buyerTaxCode, buyerAddress, buyerPhone, buyerCode,
                paymentPeriod, year,
                newMeterReading, oldMeterReading, waterConsumption,
                waterTaxableAmount, vatAmount, envProtectionFee,
                totalAmount, totalInWords,
                paymentMethod, status);
    }

    private String textById(Document doc, String id) {
        Element el = doc.getElementById(id);
        return el != null ? el.text().trim() : "";
    }

    private String parseDate(Document doc) {
        Element dateDiv = doc.selectFirst(".BTN-arising-date");
        if (dateDiv == null) return "";
        Elements texts = dateDiv.select(".BTN-text");
        if (texts.size() >= 3) {
            return texts.get(0).text().trim() + "/"
                    + texts.get(1).text().trim() + "/"
                    + texts.get(2).text().trim();
        }
        return dateDiv.text().trim();
    }

    private String parseLabelValue(Document doc, String label) {
        Elements rows = doc.select(".BTN-info .BTN-row");
        for (Element row : rows) {
            String labelText = row.select(".BTN-label").text().trim();
            if (labelText.contains(label)) {
                String val = row.select(".BTN-text").text().trim();
                if (!val.isEmpty()) return val;
            }
        }
        return "";
    }

    private java.util.Map<String, String> parseBuyerFields(Element buyerSection) {
        java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
        String currentLabel = "";
        for (Element row : buyerSection.select(".BTN-row")) {
            currentLabel = "";
            for (Element child : row.children()) {
                String tag = child.tagName();
                String text = child.text().trim();
                if (text.isEmpty()) continue;
                if ("div".equals(tag) && child.hasClass("BTN-label")) {
                    currentLabel = text.toLowerCase().replaceAll("[:\\s]+$", "");
                } else if ("div".equals(tag) && child.hasClass("BTN-text")) {
                    if (text.endsWith(":")) {
                        currentLabel = text.toLowerCase().replaceAll("[:\\s]+$", "");
                    } else if (!currentLabel.isEmpty()) {
                        if (!map.containsKey(currentLabel)) {
                            map.put(currentLabel, text);
                        }
                        currentLabel = "";
                    }
                }
            }
        }
        return map;
    }

    private String parseTotalByLabel(Document doc, String label) {
        Element stats = doc.selectFirst(".BTN-statistics");
        if (stats == null) return "";
        for (Element td : stats.select("td")) {
            String text = td.text().trim();
            if (text.contains(label)) {
                Element nextTd = td.nextElementSibling();
                if (nextTd != null) {
                    String val = nextTd.text().trim();
                    if (!val.isEmpty() && val.matches(".*\\d.*")) return val;
                }
            }
        }
        return "";
    }

    private String parseTotalInWords(Document doc) {
        Element stats = doc.selectFirst(".BTN-statistics");
        if (stats == null) return "";
        String text = stats.html();
        int idx = text.indexOf("Bằng chữ:");
        if (idx < 0) return "";
        String after = text.substring(idx + "Bằng chữ:".length());
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile(">([^<]+)</").matcher(after);
        if (m.find()) return m.group(1).trim();
        return "";
    }
}
