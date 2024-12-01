package dms.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourceReponse<T> {

    private List<T> content;  // 실제 데이터 목록
    private int page;         // 현재 페이지 번호
    private int size;         // 페이지당 항목 수
    private long totalElements; // 전체 항목 수
    private int totalPages;
/*
    public SourceResponse(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
    }
 */

}
