create unique index ux_users_email_normalized
    on users (lower(btrim(email)));

alter table users
    add column password_hash varchar(255);
