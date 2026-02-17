CREATE TABLE
IF NOT EXISTS reviews
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4
(),
    user_id UUID NOT NULL REFERENCES users
(id) ON
DELETE CASCADE,
    article_id UUID
NOT NULL REFERENCES articles
(id) ON
DELETE CASCADE,
    rating INTEGER
NOT NULL CHECK
(rating >= 1 AND rating <= 5),
    comment TEXT,
    created TIMESTAMP
WITH TIME ZONE DEFAULT NOW
(),
    updated TIMESTAMP
WITH TIME ZONE DEFAULT NOW
()
);

CREATE INDEX
IF NOT EXISTS reviews_article_id_idx ON reviews
(article_id);
CREATE INDEX
IF NOT EXISTS reviews_user_id_idx ON reviews
(user_id);
