INSERT INTO categories (name, description) VALUES
('Electronics', 'Smartphones, laptops, tablets, and other electronic devices'),
('Clothing', 'Men and women fashion, casual and formal wear'),
('Books', 'Fiction, non-fiction, educational and professional books'),
('Home & Garden', 'Furniture, decor, kitchen appliances and garden tools'),
('Sports & Outdoors', 'Fitness equipment, outdoor gear and sportswear'),
('Beauty & Personal Care', 'Skincare, haircare, makeup and grooming products')
ON CONFLICT (name) DO NOTHING;

INSERT INTO products (name, description, price, category_id, stock_quantity, sku, image_url, active) VALUES
('ProBook Laptop 15', 'High-performance 15.6" laptop with Intel Core i7, 16GB RAM, 512GB SSD. Perfect for professionals and students.', 1299.99, (SELECT id FROM categories WHERE name='Electronics'), 45, 'ELEC-001', 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=500', true),
('UltraPhone X12', 'Flagship smartphone with 6.7" AMOLED display, 256GB storage, triple camera system and all-day battery.', 899.99, (SELECT id FROM categories WHERE name='Electronics'), 78, 'ELEC-002', 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=500', true),
('Wireless Noise-Cancelling Headphones', 'Premium over-ear headphones with 30-hour battery, Bluetooth 5.0 and studio-quality sound.', 249.99, (SELECT id FROM categories WHERE name='Electronics'), 120, 'ELEC-003', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500', true),
('SmartWatch Pro', 'Advanced fitness tracker with heart rate monitor, GPS, sleep tracking and 7-day battery life.', 299.99, (SELECT id FROM categories WHERE name='Electronics'), 60, 'ELEC-004', 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500', true),
('Men''s Classic Oxford Shirt', 'Premium cotton Oxford shirt available in multiple colors. Perfect for business casual occasions.', 59.99, (SELECT id FROM categories WHERE name='Clothing'), 200, 'CLTH-001', 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=500', true),
('Women''s Yoga Leggings', 'High-waist compression leggings with moisture-wicking fabric. Available in sizes XS-XL.', 45.99, (SELECT id FROM categories WHERE name='Clothing'), 150, 'CLTH-002', 'https://images.unsplash.com/photo-1506629082955-511b1aa562c8?w=500', true),
('The Art of Clean Code', 'A practical guide to writing maintainable, readable and efficient software. Essential for every developer.', 34.99, (SELECT id FROM categories WHERE name='Books'), 85, 'BOOK-001', 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500', true),
('Modern Architecture Patterns', 'Comprehensive guide to microservices, event-driven architecture and cloud-native design patterns.', 49.99, (SELECT id FROM categories WHERE name='Books'), 60, 'BOOK-002', 'https://images.unsplash.com/photo-1507842217343-583bb7270b66?w=500', true),
('Ergonomic Office Chair', 'Fully adjustable mesh office chair with lumbar support, armrests and breathable back. 5-year warranty.', 399.99, (SELECT id FROM categories WHERE name='Home & Garden'), 30, 'HOME-001', 'https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=500', true),
('Stainless Steel Kitchen Set', '10-piece premium stainless steel cookware set with non-stick coating, oven-safe up to 500°F.', 189.99, (SELECT id FROM categories WHERE name='Home & Garden'), 40, 'HOME-002', 'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=500', true),
('Adjustable Dumbbells Set', 'Space-saving adjustable dumbbells from 5 to 52.5 lbs. Replaces 15 sets of weights.', 349.99, (SELECT id FROM categories WHERE name='Sports & Outdoors'), 25, 'SPRT-001', 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=500', true),
('Trail Running Shoes', 'Lightweight trail running shoes with superior grip, waterproof membrane and cushioned midsole.', 129.99, (SELECT id FROM categories WHERE name='Sports & Outdoors'), 90, 'SPRT-002', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500', true),
('Vitamin C Serum', 'Brightening 20% Vitamin C serum with hyaluronic acid and niacinamide. Dermatologist tested.', 39.99, (SELECT id FROM categories WHERE name='Beauty & Personal Care'), 180, 'BEAU-001', 'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=500', true),
('Professional Hair Dryer', 'Ionic technology hair dryer with 3 heat settings, 2 speed settings and cool shot button. 2200W.', 79.99, (SELECT id FROM categories WHERE name='Beauty & Personal Care'), 110, 'BEAU-002', 'https://images.unsplash.com/photo-1522338140262-f46f5913618a?w=500', true),
('Tablet Pro 11"', '11-inch tablet with M2 chip, 256GB storage, USB-C, compatible with stylus pen. Ideal for creative work.', 749.99, (SELECT id FROM categories WHERE name='Electronics'), 55, 'ELEC-005', 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=500', true)
ON CONFLICT (sku) DO NOTHING;
