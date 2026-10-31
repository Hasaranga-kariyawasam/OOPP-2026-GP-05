package com.faculty.model.admin;

import java.time.LocalDateTime;

/** A notice. noticeId 0 = not saved yet. postedBy / postedAt are only filled when reading from the database. */
public record AdminNotice(int noticeId, String title, String audience, String postedBy,
                          LocalDateTime postedAt, String content) {
}
