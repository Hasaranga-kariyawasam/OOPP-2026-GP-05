package com.faculty.model;

import java.time.LocalDateTime;

/** A notice posted by the admin (row of the notice table). */
public class Notice {

    private int noticeId;
    private String title;
    private String content;
    private LocalDateTime postedDate;
    private String targetRole;          // ALL, ADMIN, LECTURER, TECHNICAL_OFFICER, UNDERGRADUATE
    private String postedByName;

    public int getNoticeId() { return noticeId; }
    public void setNoticeId(int noticeId) { this.noticeId = noticeId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getPostedDate() { return postedDate; }
    public void setPostedDate(LocalDateTime postedDate) { this.postedDate = postedDate; }
    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
    public String getPostedByName() { return postedByName; }
    public void setPostedByName(String postedByName) { this.postedByName = postedByName; }
}
