from app.csv_writer import render_product_csv


def test_render_product_csv_escapes_formula_cells():
    csv_bytes = render_product_csv(
        [
            {
                "id": 1,
                "name": "=cmd",
                "slug": "test-product",
            }
        ]
    )

    assert "'=cmd" in csv_bytes.decode("utf-8-sig")
