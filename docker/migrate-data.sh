#!/bin/bash
# Migrate data from monolithic DB → microservice DBs
# Uses exact schema from monolith via mysqldump
# Source: monolith-mysql container
# Target: micro-mysql container
set -e

MONO="monolith-mysql"
MICRO="micro-mysql"
SRC_DB="neki_ecommerce"

echo "=============================================="
echo " Neki Ecommerce Data Migration"
echo " Monolithic → Microservice"
echo "=============================================="

# ──── TABLE GROUPS ────
USERS_TABLES="roles users user_roles refresh_tokens"
PRODUCTS_TABLES="categories sub_categories brands colors sizes topics collections collection_subcategories products product_variants product_images product_collections product_topics inventory banners store_settings product_similarity"
ORDERS_TABLES="discounts carts cart_items wishlists wishlist_items orders order_items discount_usages payment_methods payments reviews"

echo ""
echo "▶ STEP 1: Cleaning target databases..."

for db in users_db products_db orders_db; do
    docker exec -e MYSQL_PWD=root $MICRO mysql -u root -e "DROP DATABASE IF EXISTS $db; CREATE DATABASE $db;" 2>/dev/null
    echo "  ✓ Recreated $db"
done


echo ""
echo "▶ STEP 2: Exporting from monolith..."

echo "  Exporting users tables..."
docker exec -e MYSQL_PWD=root $MONO mysqldump -u root \
    --single-transaction --set-gtid-purged=OFF --skip-triggers \
    --add-drop-table --routines=false \
    $SRC_DB $USERS_TABLES > /tmp/users_dump.sql
echo "  ✓ users_dump.sql ($(wc -c < /tmp/users_dump.sql) bytes)"

echo "  Exporting products tables..."
docker exec -e MYSQL_PWD=root $MONO mysqldump -u root \
    --single-transaction --set-gtid-purged=OFF --skip-triggers \
    --add-drop-table --routines=false \
    $SRC_DB $PRODUCTS_TABLES > /tmp/products_dump.sql
echo "  ✓ products_dump.sql ($(wc -c < /tmp/products_dump.sql) bytes)"

echo "  Exporting orders tables..."
docker exec -e MYSQL_PWD=root $MONO mysqldump -u root \
    --single-transaction --set-gtid-purged=OFF --skip-triggers \
    --add-drop-table --routines=false \
    $SRC_DB $ORDERS_TABLES > /tmp/orders_dump.sql
echo "  ✓ orders_dump.sql ($(wc -c < /tmp/orders_dump.sql) bytes)"


echo ""
echo "▶ STEP 3: Importing into microservice DBs..."

echo "  Importing users_db..."
docker cp /tmp/users_dump.sql $MICRO:/tmp/users_dump.sql
docker exec -e MYSQL_PWD=root $MICRO bash -c "mysql -u root users_db < /tmp/users_dump.sql"
echo "  ✓ users_db imported"

echo "  Importing products_db..."
docker cp /tmp/products_dump.sql $MICRO:/tmp/products_dump.sql
docker exec -e MYSQL_PWD=root $MICRO bash -c "mysql -u root products_db < /tmp/products_dump.sql"
echo "  ✓ products_db imported"

echo "  Importing orders_db..."
docker cp /tmp/orders_dump.sql $MICRO:/tmp/orders_dump.sql
docker exec -e MYSQL_PWD=root $MICRO bash -c "mysql -u root orders_db < /tmp/orders_dump.sql"
echo "  ✓ orders_db imported"

# ──────────────────────────────────────────────────
# STEP 4: Verify
# ──────────────────────────────────────────────────
echo ""
echo "▶ STEP 4: Verification..."

echo ""
echo "┌─── users_db ───────────────────────────────┐"
docker exec -e MYSQL_PWD=root $MICRO mysql -u root users_db -e "
SELECT 'roles' AS tbl, COUNT(*) AS cnt FROM roles
UNION ALL SELECT 'users', COUNT(*) FROM users
UNION ALL SELECT 'user_roles', COUNT(*) FROM user_roles
UNION ALL SELECT 'refresh_tokens', COUNT(*) FROM refresh_tokens;" 2>/dev/null
echo "└────────────────────────────────────────────┘"

echo ""
echo "┌─── products_db ────────────────────────────┐"
docker exec -e MYSQL_PWD=root $MICRO mysql -u root products_db -e "
SELECT 'categories' AS tbl, COUNT(*) AS cnt FROM categories
UNION ALL SELECT 'sub_categories', COUNT(*) FROM sub_categories
UNION ALL SELECT 'brands', COUNT(*) FROM brands
UNION ALL SELECT 'colors', COUNT(*) FROM colors
UNION ALL SELECT 'sizes', COUNT(*) FROM sizes
UNION ALL SELECT 'topics', COUNT(*) FROM topics
UNION ALL SELECT 'collections', COUNT(*) FROM collections
UNION ALL SELECT 'products', COUNT(*) FROM products
UNION ALL SELECT 'product_variants', COUNT(*) FROM product_variants
UNION ALL SELECT 'product_images', COUNT(*) FROM product_images
UNION ALL SELECT 'inventory', COUNT(*) FROM inventory
UNION ALL SELECT 'banners', COUNT(*) FROM banners
UNION ALL SELECT 'store_settings', COUNT(*) FROM store_settings
UNION ALL SELECT 'product_similarity', COUNT(*) FROM product_similarity;" 2>/dev/null
echo "└────────────────────────────────────────────┘"

echo ""
echo "┌─── orders_db ───────────────────────────────┐"
docker exec -e MYSQL_PWD=root $MICRO mysql -u root orders_db -e "
SELECT 'discounts' AS tbl, COUNT(*) AS cnt FROM discounts
UNION ALL SELECT 'carts', COUNT(*) FROM carts
UNION ALL SELECT 'cart_items', COUNT(*) FROM cart_items
UNION ALL SELECT 'wishlists', COUNT(*) FROM wishlists
UNION ALL SELECT 'wishlist_items', COUNT(*) FROM wishlist_items
UNION ALL SELECT 'orders', COUNT(*) FROM orders
UNION ALL SELECT 'order_items', COUNT(*) FROM order_items
UNION ALL SELECT 'payment_methods', COUNT(*) FROM payment_methods
UNION ALL SELECT 'payments', COUNT(*) FROM payments
UNION ALL SELECT 'reviews', COUNT(*) FROM reviews;" 2>/dev/null
echo "└────────────────────────────────────────────┘"

echo ""
echo "=============================================="
echo "  Migration Complete!"
echo "=============================================="
echo ""
echo "Next steps:"
echo "  1. Stop temporary containers: docker stop monolith-mysql micro-mysql"
echo "  2. Copy migrated data to microservice Docker volume"
echo "  3. Start microservice stack: docker compose -f docker-compose.dev.yml up -d"
