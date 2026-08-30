package com.devcli.model;

public class Insight {
    private String category; // FOCUS, STACK, CONSISTENCY, MAINTENANCE
    private String title;
    private String detail;
    private String metric;
    private int importance; // 1 to 5

    public Insight() {}

    public Insight(String category, String title, String detail, String metric, int importance) {
        this.category = category;
        this.title = title;
        this.detail = detail;
        this.metric = metric;
        this.importance = importance;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }

    public int getImportance() { return importance; }
    public void setImportance(int importance) { this.importance = importance; }
}
