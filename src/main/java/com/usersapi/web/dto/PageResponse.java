package com.usersapi.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import java.util.List;

public class PageResponse<T> {
    private List<T> content;
    private PageMetadata metadata;

    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.metadata = new PageMetadata(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );
    }

    // Getters and setters
    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public PageMetadata getMetadata() { return metadata; }
    public void setMetadata(PageMetadata metadata) { this.metadata = metadata; }

    public static class PageMetadata {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;

        @JsonProperty("hasNext")
        private boolean hasNext;

        @JsonProperty("hasPrevious")
        private boolean hasPrevious;

        public PageMetadata(int page, int size, long totalElements, int totalPages,
                            boolean first, boolean last, boolean hasNext, boolean hasPrevious) {
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.first = first;
            this.last = last;
            this.hasNext = hasNext;
            this.hasPrevious = hasPrevious;
        }

        // Getters
        public int getPage() { return page; }
        public int getSize() { return size; }
        public long getTotalElements() { return totalElements; }
        public int getTotalPages() { return totalPages; }
        public boolean isFirst() { return first; }
        public boolean isLast() { return last; }
        public boolean isHasNext() { return hasNext; }
        public boolean isHasPrevious() { return hasPrevious; }
    }
}