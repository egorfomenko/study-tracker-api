create unique index study_sessions_one_active_per_user
    on study_sessions (user_id)
    where study_sessions.status = 'ACTIVE';