CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO products (name, price, description, created_at, updated_at)
SELECT
    'Product ' || gs,
    round((random() * 100)::numeric, 2),
    'Description for Product ' || gs || ': ' || encode(digest(random()::text, 'sha1'), 'hex'),
    now() - (random() * interval '365 days'),
    now() - (random() * interval '30 days')
FROM generate_series(1, 10000000) gs;
