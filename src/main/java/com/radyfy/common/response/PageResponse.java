package com.radyfy.common.response;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public class PageResponse {

    private int totalPages;
    private boolean last;
    private long totalElements;
    private int numberOfElements;
    private boolean first;
    private int size;
    private int number;
    private boolean empty;

    private List<Object> content = new ArrayList<>();

    public PageResponse(Page<?> data, List<Object> content) {
        totalPages = data.getTotalPages();
        last = data.isLast();
        totalElements = data.getTotalElements();
        numberOfElements = data.getNumberOfElements();
        first = data.isFirst();
        size = data.getSize();
        number = data.getNumber();
        empty = data.isEmpty();
        this.content = content;
    }

    public List<Object> getContent() {
        return content;
    }

    public void setContent(List<Object> content) {
        this.content = content;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }
}