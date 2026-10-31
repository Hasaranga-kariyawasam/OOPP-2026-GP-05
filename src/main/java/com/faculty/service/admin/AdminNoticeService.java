package com.faculty.service.admin;

import com.faculty.dao.admin.AdminNoticeDAO;
import com.faculty.dao.admin.AdminNoticeDAOImpl;
import com.faculty.exception.AdminValidationException;
import com.faculty.model.admin.AdminNotice;

import java.sql.SQLException;
import java.util.List;

/** Business rules for notice management. */
public class AdminNoticeService {

    /** Who a notice can be addressed to (the same values as notice.target_role). */
    public static final List<String> AUDIENCES = List.of("ALL", "UNDERGRADUATE", "LECTURER", "TECHNICAL_OFFICER", "ADMIN");

    private final AdminNoticeDAO noticeDAO = new AdminNoticeDAOImpl();

    public List<AdminNotice> findAll() throws SQLException {
        AdminSupport.requireAdmin();
        return noticeDAO.findAll();
    }

    public void add(String title, String audience, String content) throws AdminValidationException, SQLException {
        int adminId = AdminSupport.currentAdminId();
        noticeDAO.insert(validate(0, title, audience, content), adminId);
    }

    public void update(int noticeId, String title, String audience, String content)
            throws AdminValidationException, SQLException {
        AdminSupport.requireAdmin();
        AdminSupport.requireSelected(noticeId, "a notice");
        noticeDAO.update(validate(noticeId, title, audience, content));
    }

    public void delete(int noticeId) throws AdminValidationException, SQLException {
        AdminSupport.requireAdmin();
        AdminSupport.requireSelected(noticeId, "a notice");
        noticeDAO.delete(noticeId);
    }

    private AdminNotice validate(int id, String title, String audience, String content) throws AdminValidationException {
        String t = AdminSupport.text(title);
        String c = AdminSupport.text(content);
        AdminSupport.require(!t.isEmpty(), "Title is required.");
        AdminSupport.require(t.length() <= 150, "Title is too long (150 characters max).");
        AdminSupport.require(!c.isEmpty(), "Write the notice message.");
        AdminSupport.require(audience != null && AUDIENCES.contains(audience), "Choose who the notice is for.");
        return new AdminNotice(id, t, audience, null, null, c);
    }
}
