#!/bin/bash
# ============================================================
# Migrate specific tables from MySQL to PostgreSQL
# Source: MySQL (neki-mysql)
# Target: PostgreSQL (neki-postgres)
# ============================================================

set -e

echo "▶ Exporting product_similarity to TSV..."
docker exec -i -e MYSQL_PWD=root neki-mysql mysql --default-character-set=utf8mb4 -u root products_db -N -e "
SELECT id, product_id_1, product_id_2, score, created_at, IFNULL(updated_at, created_at)
FROM product_similarity;
" > /tmp/product_similarity.tsv

echo "▶ Exporting payment_methods to TSV..."
docker exec -i -e MYSQL_PWD=root neki-mysql mysql --default-character-set=utf8mb4 -u root orders_db -N -e "
SELECT id, name, description, is_active, created_at, created_at
FROM payment_methods;
" > /tmp/payment_methods.tsv

echo "▶ Exporting payments to TSV..."
# We need to cross-database join in MySQL. We can do that since the root user has access to all DBs.
docker exec -i -e MYSQL_PWD=root neki-mysql mysql --default-character-set=utf8mb4 -u root -N -e "
SELECT 
    p.id, 
    p.amount, 
    p.order_id, 
    p.payment_method_id, 
    IFNULL(p.transaction_id, 'NULL'), 
    IFNULL(p.status, 'NULL'), 
    IFNULL(p.paid_at, 'NULL'), 
    p.created_at, 
    p.created_at AS updated_at, 
    o.order_number, 
    u.email AS user_email, 
    o.user_id
FROM orders_db.payments p
JOIN orders_db.orders o ON p.order_id = o.id
JOIN users_db.users u ON o.user_id = u.id;
" > /tmp/payments.tsv

echo "▶ Replacing empty strings or 'NULL' with PostgreSQL nulls (\N) in TSVs..."
# In mysql text mode, NULL is written as NULL. PostgreSQL text mode expects \N for nulls, or we tell COPY NULL 'NULL'.
# We used IFNULL to handle some, and for the others native NULL will be output as 'NULL'.

echo "▶ Copying TSV to PostgreSQL container..."
docker cp /tmp/product_similarity.tsv neki-postgres:/tmp/product_similarity.tsv
docker cp /tmp/payment_methods.tsv neki-postgres:/tmp/payment_methods.tsv
docker cp /tmp/payments.tsv neki-postgres:/tmp/payments.tsv

echo "▶ Importing into recommendations_db (product_similarity)..."
docker exec -i -e PGPASSWORD=123456 neki-postgres psql -U postgres -d recommendations_db <<'EOF'
CREATE TEMP TABLE tmp_similarity (
    id int, product_id_1 int, product_id_2 int, score float, created_at timestamp, updated_at timestamp
);
\copy tmp_similarity(id, product_id_1, product_id_2, score, created_at, updated_at) FROM '/tmp/product_similarity.tsv' WITH (FORMAT text, NULL 'NULL');
INSERT INTO product_similarity (id, product_id_1, product_id_2, score, created_at, updated_at)
SELECT * FROM tmp_similarity ON CONFLICT (id) DO NOTHING;
SELECT setval('product_similarity_id_seq', (SELECT MAX(id) FROM product_similarity));
EOF

echo "▶ Importing into payments_db (payment_methods)..."
docker exec -i -e PGPASSWORD=123456 neki-postgres psql -U postgres -d payments_db <<'EOF'
CREATE TEMP TABLE tmp_pm (
    id int, name varchar, description text, is_active int, created_at timestamp, updated_at timestamp
);
\copy tmp_pm(id, name, description, is_active, created_at, updated_at) FROM '/tmp/payment_methods.tsv' WITH (FORMAT text, NULL 'NULL');
INSERT INTO payment_methods (id, name, description, is_active, created_at, updated_at)
SELECT id, name, description, is_active::boolean, created_at, updated_at FROM tmp_pm ON CONFLICT (id) DO NOTHING;
SELECT setval('payment_methods_id_seq', (SELECT MAX(id) FROM payment_methods));
EOF

echo "▶ Importing into payments_db (payments)..."
docker exec -i -e PGPASSWORD=123456 neki-postgres psql -U postgres -d payments_db <<'EOF'
CREATE TEMP TABLE tmp_p (
    id int, amount numeric, order_id int, payment_method_id int, transaction_id varchar, status varchar, paid_at timestamp, created_at timestamp, updated_at timestamp, order_number varchar, user_email varchar, user_id int
);
\copy tmp_p(id, amount, order_id, payment_method_id, transaction_id, status, paid_at, created_at, updated_at, order_number, user_email, user_id) FROM '/tmp/payments.tsv' WITH (FORMAT text, NULL 'NULL');
INSERT INTO payments (id, amount, order_id, payment_method_id, transaction_id, status, paid_at, created_at, updated_at, order_number, user_email, user_id)
SELECT id, amount, order_id, payment_method_id, transaction_id, status, paid_at, created_at, updated_at, order_number, user_email, user_id FROM tmp_p ON CONFLICT (id) DO NOTHING;
SELECT setval('payments_id_seq', (SELECT MAX(id) FROM payments));
EOF

echo "▶ Cleaning up..."
rm -f /tmp/product_similarity.tsv /tmp/payment_methods.tsv /tmp/payments.tsv
docker exec -i neki-postgres rm -f /tmp/product_similarity.tsv /tmp/payment_methods.tsv /tmp/payments.tsv

echo "✅ PostgreSQL Data Migration Complete!"
