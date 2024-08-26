CREATE TABLE IF NOT EXISTS reviews (
                                       review_id UUID PRIMARY KEY,
                                       order_id UUID NOT NULL,
                                       user_id UUID NOT NULL,
                                       restaurant_id UUID NOT NULL,
                                       stars INT NOT NULL CHECK (stars >= 1 AND stars <= 5),
                                       comment TEXT,
                                       date TIMESTAMP NOT NULL
);
