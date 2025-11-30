package com.smartrental.backend.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.smartrental.backend.entity.Contract;
import com.smartrental.backend.model.json.ServiceFee;
import com.smartrental.backend.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final ContractRepository contractRepository;

    public byte[] generateContractPdf(Long contractId) throws IOException {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Hợp đồng không tồn tại"));

        // 1. Khởi tạo Document (Khổ A4)
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // 2. Định dạng Font chữ (Cần font hỗ trợ tiếng Việt nếu muốn hiển thị đúng)
            // Ở đây dùng font mặc định có sẵn (có thể bị lỗi font tiếng Việt nếu không import font ttf)
            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 12);

            // 3. Tiêu đề
            Paragraph title = new Paragraph("HOP DONG THUE PHONG TRO", fontTitle);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // 4. Thông tin chung
            document.add(new Paragraph("Ma Hop Dong: #" + contract.getId(), fontHeader));
            document.add(new Paragraph("Ngay tao: " + contract.getCreatedAt().format(DateTimeFormatter.ISO_DATE), fontNormal));
            document.add(new Paragraph("----------------------------------------------------------"));

            // 5. Bên Cho Thuê (Chủ trọ)
            document.add(new Paragraph("BEN A: BEN CHO THUE (CHU TRO)", fontHeader));
            document.add(new Paragraph("Ho ten: " + contract.getRoom().getLandlord().getFullName(), fontNormal));
            document.add(new Paragraph("Dien thoai: " + contract.getRoom().getLandlord().getPhone(), fontNormal));
            document.add(new Paragraph("\n"));

            // 6. Bên Thuê (Khách)
            document.add(new Paragraph("BEN B: BEN THUE (KHACH HANG)", fontHeader));
            document.add(new Paragraph("Ho ten: " + contract.getTenant().getFullName(), fontNormal));
            document.add(new Paragraph("Email: " + contract.getTenant().getEmail(), fontNormal));
            document.add(new Paragraph("Dien thoai: " + contract.getTenant().getPhone(), fontNormal));
            document.add(new Paragraph("\n"));

            // 7. Thông tin Phòng & Giá
            document.add(new Paragraph("THONG TIN THUE", fontHeader));
            document.add(new Paragraph("Phong: " + contract.getRoom().getTitle(), fontNormal));
            document.add(new Paragraph("Dia chi: " + contract.getRoom().getAddress(), fontNormal));
            document.add(new Paragraph("Thoi han: Tu " + contract.getStartDate() + " den " + contract.getEndDate(), fontNormal));

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            document.add(new Paragraph("Gia thue hang thang: " + currencyFormat.format(contract.getMonthlyRent()), fontNormal));
            document.add(new Paragraph("Tien coc: " + currencyFormat.format(contract.getDepositAmount()), fontNormal));
            document.add(new Paragraph("\n"));

            // 8. Bảng Phí Dịch vụ
            document.add(new Paragraph("CAC KHOAN PHI DICH VU", fontHeader));
            PdfPTable table = new PdfPTable(3); // 3 cột
            table.setWidthPercentage(100);
            table.addCell("Ten Dich Vu");
            table.addCell("Don Gia");
            table.addCell("Don Vi");

            if (contract.getServiceFees() != null) {
                for (ServiceFee fee : contract.getServiceFees()) {
                    table.addCell(fee.getName());
                    table.addCell(currencyFormat.format(fee.getPrice()));
                    table.addCell(fee.getUnit());
                }
            }
            document.add(table);

            document.add(new Paragraph("\n\n"));

            // 9. Chữ ký
            PdfPTable signTable = new PdfPTable(2);
            signTable.setWidthPercentage(100);

            PdfPCell cellA = new PdfPCell(new Paragraph("DAI DIEN BEN A\n(Ky va ghi ro ho ten)", fontHeader));
            cellA.setBorder(Rectangle.NO_BORDER);
            cellA.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPCell cellB = new PdfPCell(new Paragraph("DAI DIEN BEN B\n(Ky va ghi ro ho ten)", fontHeader));
            cellB.setBorder(Rectangle.NO_BORDER);
            cellB.setHorizontalAlignment(Element.ALIGN_CENTER);

            signTable.addCell(cellA);
            signTable.addCell(cellB);

            document.add(signTable);

            document.close();
        } catch (DocumentException e) {
            throw new IOException(e.getMessage());
        }

        return out.toByteArray();
    }
}