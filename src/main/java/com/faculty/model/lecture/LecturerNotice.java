package com.faculty.model.lecture;

import java.time.LocalDateTime;

/** A notice that is visible to lecturers (target role ALL or LECTURER). */
public class LecturerNotice {

    private int noticeId;
    private String title;
    private String content;
    private LocalDateTime postedDate;
    private String postedBy;

    public int getNoticeId() { return noticeId; }
    public void setNoticeId(int noticeId) { this.noticeId = noticeId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getPostedDate() { return postedDate; }
    public void setPostedDate(LocalDateTime postedDate) { this.postedDate = postedDate; }
    public String getPostedBy() { return postedBy; }
    public void setPostedBy(String postedBy) { this.postedBy = postedBy; }
}
